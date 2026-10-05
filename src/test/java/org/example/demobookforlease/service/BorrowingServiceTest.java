package org.example.demobookforlease.service;

import org.example.demobookforlease.dto.*;
import org.example.demobookforlease.exception.BusinessException;
import org.example.demobookforlease.model.*;
import org.example.demobookforlease.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BorrowingServiceTest {

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FinePolicyRepository finePolicyRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    private Member member;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category(null, "Test Category", "Desc", true));

        book1 = bookRepository.save(new Book(null, "Java Core", "Author 1", "ISBN-101", category, BigDecimal.valueOf(10000), BigDecimal.valueOf(100000), 5, 5, BookStatus.AVAILABLE));
        book2 = bookRepository.save(new Book(null, "Spring Boot", "Author 2", "ISBN-102", category, BigDecimal.valueOf(15000), BigDecimal.valueOf(150000), 2, 2, BookStatus.AVAILABLE));

        member = memberRepository.save(new Member(null, "Test Member", "test@example.com", "0999999999", MemberStatus.ACTIVE));

        finePolicyRepository.save(new FinePolicy(BigDecimal.valueOf(10000), BigDecimal.valueOf(500000), 0, true));
    }

    @Test
    @DisplayName("Case 1: Mượn thành công nhiều đầu sách - Trừ tồn kho chính xác")
    void testCreateBorrowing_Success() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.now().plusDays(7));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 2));
        form.getItems().add(new BorrowItemForm(book2.getId(), 1));

        Borrowing borrowing = borrowingService.createBorrowing(form);

        assertNotNull(borrowing.getId());
        assertEquals(BorrowingStatus.BORROWING, borrowing.getStatus());

        Book updatedBook1 = bookRepository.findById(book1.getId()).orElseThrow();
        Book updatedBook2 = bookRepository.findById(book2.getId()).orElseThrow();

        assertEquals(3, updatedBook1.getAvailableQuantity());
        assertEquals(1, updatedBook2.getAvailableQuantity());
    }

    @Test
    @DisplayName("Case 2: Mượn sách không đủ số lượng tồn kho - Rollback toàn bộ phiếu")
    void testCreateBorrowing_InsufficientStock_Rollback() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.now().plusDays(7));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 2));
        form.getItems().add(new BorrowItemForm(book2.getId(), 10)); // Vượt quá tồn kho = 2

        assertThrows(BusinessException.class, () -> borrowingService.createBorrowing(form));

        // Kiểm tra book1 không bị trừ nhầm tồn kho một phần
        Book updatedBook1 = bookRepository.findById(book1.getId()).orElseThrow();
        assertEquals(5, updatedBook1.getAvailableQuantity());
    }

    @Test
    @DisplayName("Case 3: Trả toàn bộ đúng hạn - Không phát sinh phí, phiếu RETURNED")
    void testReturnBooks_OnTime_Full() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.now().plusDays(7));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 1));
        Borrowing borrowing = borrowingService.createBorrowing(form);

        ReturnForm returnForm = new ReturnForm(borrowing.getId());
        returnForm.setReturnDate(LocalDate.now().plusDays(5)); // Đúng hạn
        returnForm.setItems(new ArrayList<>());
        BorrowingDetail detail = borrowing.getDetails().get(0);
        returnForm.getItems().add(new ReturnItemForm(detail.getId(), book1.getId(), book1.getTitle(), 1, 0));
        returnForm.getItems().get(0).setReturnQuantity(1);

        Borrowing updated = borrowingService.returnBooks(returnForm);

        assertEquals(BorrowingStatus.RETURNED, updated.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getTotalFineAmount()));

        Book updatedBook1 = bookRepository.findById(book1.getId()).orElseThrow();
        assertEquals(5, updatedBook1.getAvailableQuantity());
    }

    @Test
    @DisplayName("Case 4: Trả một phần đúng hạn - Phiếu PARTIALLY_RETURNED")
    void testReturnBooks_OnTime_Partial() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.now().plusDays(7));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 2));
        Borrowing borrowing = borrowingService.createBorrowing(form);

        ReturnForm returnForm = new ReturnForm(borrowing.getId());
        returnForm.setReturnDate(LocalDate.now().plusDays(5));
        returnForm.setItems(new ArrayList<>());
        BorrowingDetail detail = borrowing.getDetails().get(0);
        returnForm.getItems().add(new ReturnItemForm(detail.getId(), book1.getId(), book1.getTitle(), 2, 0));
        returnForm.getItems().get(0).setReturnQuantity(1); // Trả 1 trong 2

        Borrowing updated = borrowingService.returnBooks(returnForm);

        assertEquals(BorrowingStatus.PARTIALLY_RETURNED, updated.getStatus());

        Book updatedBook1 = bookRepository.findById(book1.getId()).orElseThrow();
        assertEquals(4, updatedBook1.getAvailableQuantity()); // 3 + 1 = 4
    }

    @Test
    @DisplayName("Case 5: Trả muộn 3 ngày - Tính đúng lateDays và fineAmount (3 * 1 * 10,000 = 30,000 VNĐ)")
    void testReturnBooks_Late3Days() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.of(2026, 10, 10));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 1));
        Borrowing borrowing = borrowingService.createBorrowing(form);

        ReturnForm returnForm = new ReturnForm(borrowing.getId());
        returnForm.setReturnDate(LocalDate.of(2026, 10, 13)); // Trễ 3 ngày
        returnForm.setItems(new ArrayList<>());
        BorrowingDetail detail = borrowing.getDetails().get(0);
        returnForm.getItems().add(new ReturnItemForm(detail.getId(), book1.getId(), book1.getTitle(), 1, 0));
        returnForm.getItems().get(0).setReturnQuantity(1);

        Borrowing updated = borrowingService.returnBooks(returnForm);

        assertEquals(BorrowingStatus.FINE_PENDING, updated.getStatus());
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(updated.getTotalFineAmount()));
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(updated.getUnpaidFineAmount()));
    }

    @Test
    @DisplayName("Case 6: Trả từng phần sau hạn - Chỉ tính phí trên số lượng trả trong lần đó")
    void testReturnBooks_Late_PartialFineCalculation() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.of(2026, 10, 10));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 3)); // Mượn 3 cuốn
        Borrowing borrowing = borrowingService.createBorrowing(form);

        ReturnForm returnForm = new ReturnForm(borrowing.getId());
        returnForm.setReturnDate(LocalDate.of(2026, 10, 13)); // Trễ 3 ngày, chỉ trả 1 cuốn
        returnForm.setItems(new ArrayList<>());
        BorrowingDetail detail = borrowing.getDetails().get(0);
        returnForm.getItems().add(new ReturnItemForm(detail.getId(), book1.getId(), book1.getTitle(), 3, 0));
        returnForm.getItems().get(0).setReturnQuantity(1); // Chỉ trả 1 cuốn

        Borrowing updated = borrowingService.returnBooks(returnForm);

        // Phí chỉ tính trên 1 cuốn trả lần này: 3 ngày * 1 cuốn * 10,000 = 30,000 VNĐ
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(updated.getTotalFineAmount()));
    }

    @Test
    @DisplayName("Case 7: Thanh toán một phần phí phạt - paidFineAmount tăng, unpaidFineAmount giảm")
    void testPayFine_PartialPayment() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.of(2026, 10, 10));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 1));
        Borrowing borrowing = borrowingService.createBorrowing(form);

        // Trả muộn phát sinh 30,000 VNĐ phí
        ReturnForm returnForm = new ReturnForm(borrowing.getId());
        returnForm.setReturnDate(LocalDate.of(2026, 10, 13));
        returnForm.setItems(new ArrayList<>());
        BorrowingDetail detail = borrowing.getDetails().get(0);
        returnForm.getItems().add(new ReturnItemForm(detail.getId(), book1.getId(), book1.getTitle(), 1, 0));
        returnForm.getItems().get(0).setReturnQuantity(1);
        borrowingService.returnBooks(returnForm);

        // Thanh toán một phần: 20,000 VNĐ
        FinePaymentForm paymentForm = new FinePaymentForm(borrowing.getId(), BigDecimal.valueOf(20000));
        Borrowing updated = borrowingService.payFine(paymentForm);

        assertEquals(0, BigDecimal.valueOf(20000).compareTo(updated.getPaidFineAmount()));
        assertEquals(0, BigDecimal.valueOf(10000).compareTo(updated.getUnpaidFineAmount()));
        assertEquals(BorrowingStatus.FINE_PENDING, updated.getStatus());
    }

    @Test
    @DisplayName("Case 8: Thanh toán vượt quá số nợ - Báo lỗi BusinessException")
    void testPayFine_ExceedUnpaidAmount_ThrowsException() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.of(2026, 10, 10));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 1));
        Borrowing borrowing = borrowingService.createBorrowing(form);

        ReturnForm returnForm = new ReturnForm(borrowing.getId());
        returnForm.setReturnDate(LocalDate.of(2026, 10, 13)); // Trễ 30,000đ
        returnForm.setItems(new ArrayList<>());
        BorrowingDetail detail = borrowing.getDetails().get(0);
        returnForm.getItems().add(new ReturnItemForm(detail.getId(), book1.getId(), book1.getTitle(), 1, 0));
        returnForm.getItems().get(0).setReturnQuantity(1);
        borrowingService.returnBooks(returnForm);

        // Thanh toán 50,000đ trong khi nợ chỉ 30,000đ -> Phải báo lỗi
        FinePaymentForm paymentForm = new FinePaymentForm(borrowing.getId(), BigDecimal.valueOf(50000));
        assertThrows(BusinessException.class, () -> borrowingService.payFine(paymentForm));
    }

    @Test
    @DisplayName("Case 9: Độc giả còn nợ phí phạt chưa trả - Không cho tạo phiếu mới")
    void testCreateBorrowing_MemberHasUnpaidFine_Blocked() {
        BorrowForm form = new BorrowForm();
        form.setMemberId(member.getId());
        form.setDueDate(LocalDate.of(2026, 10, 10));
        form.setItems(new ArrayList<>());
        form.getItems().add(new BorrowItemForm(book1.getId(), 1));
        Borrowing borrowing = borrowingService.createBorrowing(form);

        // Trả muộn tạo nợ phí phạt
        ReturnForm returnForm = new ReturnForm(borrowing.getId());
        returnForm.setReturnDate(LocalDate.of(2026, 10, 13));
        returnForm.setItems(new ArrayList<>());
        BorrowingDetail detail = borrowing.getDetails().get(0);
        returnForm.getItems().add(new ReturnItemForm(detail.getId(), book1.getId(), book1.getTitle(), 1, 0));
        returnForm.getItems().get(0).setReturnQuantity(1);
        borrowingService.returnBooks(returnForm);

        // Thử tạo phiếu mượn mới khi chưa trả nợ phí -> Phải bị chặn
        BorrowForm newForm = new BorrowForm();
        newForm.setMemberId(member.getId());
        newForm.setDueDate(LocalDate.now().plusDays(7));
        newForm.setItems(new ArrayList<>());
        newForm.getItems().add(new BorrowItemForm(book2.getId(), 1));

        assertThrows(BusinessException.class, () -> borrowingService.createBorrowing(newForm));
    }
}
