package org.example.demobookforlease.service;

import org.example.demobookforlease.dto.*;
import org.example.demobookforlease.exception.BusinessException;
import org.example.demobookforlease.exception.ResourceNotFoundException;
import org.example.demobookforlease.model.*;
import org.example.demobookforlease.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final FinePaymentRepository finePaymentRepository;
    private final FineWaiverRepository fineWaiverRepository;
    private final FinePolicyService finePolicyService;
    private final FineCalculatorService fineCalculatorService;

    public BorrowingService(BorrowingRepository borrowingRepository,
                            BorrowingDetailRepository borrowingDetailRepository,
                            MemberRepository memberRepository,
                            BookRepository bookRepository,
                            FinePaymentRepository finePaymentRepository,
                            FineWaiverRepository fineWaiverRepository,
                            FinePolicyService finePolicyService,
                            FineCalculatorService fineCalculatorService) {
        this.borrowingRepository = borrowingRepository;
        this.borrowingDetailRepository = borrowingDetailRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.finePaymentRepository = finePaymentRepository;
        this.fineWaiverRepository = fineWaiverRepository;
        this.finePolicyService = finePolicyService;
        this.fineCalculatorService = fineCalculatorService;
    }

    @Transactional(readOnly = true)
    public Page<Borrowing> searchBorrowings(Long memberId, BorrowingStatus status, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        return borrowingRepository.searchBorrowings(memberId, status, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public Borrowing getBorrowingById(Long id) {
        return borrowingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn có ID: " + id));
    }

    @Transactional
    public Borrowing createBorrowing(BorrowForm form) {
        Member member = memberRepository.findById(form.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả"));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("Độc giả hiện không ở trạng thái hoạt động (ACTIVE)");
        }

        // Check rule 1: Max 5 unreturned books
        int currentActiveCount = borrowingRepository.countActiveUnreturnedBooksByMemberId(member.getId());
        int requestedTotalCount = form.getItems().stream()
                .filter(i -> i.getBookId() != null && i.getQuantity() != null)
                .mapToInt(BorrowItemForm::getQuantity)
                .sum();

        if (currentActiveCount + requestedTotalCount > 5) {
            throw new BusinessException("Độc giả đã mượn " + currentActiveCount + " cuốn chưa trả. Tổng số mượn tối đa là 5 cuốn.");
        }

        // Check rule 2: Overdue borrowings check
        long overdueCount = borrowingRepository.countOverdueBorrowingsByMemberId(member.getId(), LocalDateTime.now());
        if (overdueCount > 0) {
            throw new BusinessException("Độc giả đang có phiếu mượn quá hạn chưa trả, không thể tạo phiếu mượn mới.");
        }

        // Check rule 3: Unpaid fine debt check
        long unpaidFineCount = borrowingRepository.countUnpaidFineBorrowingsByMemberId(member.getId());
        if (unpaidFineCount > 0) {
            throw new BusinessException("Độc giả còn phí phạt chưa thanh toán, không thể tạo phiếu mượn mới.");
        }

        // Check rule 4: Validate items list & duplicate book check
        if (form.getItems() == null || form.getItems().isEmpty()) {
            throw new BusinessException("Phiếu mượn phải chứa ít nhất một đầu sách.");
        }

        Set<Long> uniqueBookIds = new HashSet<>();
        List<BorrowItemForm> validItems = new ArrayList<>();

        for (BorrowItemForm item : form.getItems()) {
            if (item.getBookId() == null) continue;
            if (!uniqueBookIds.add(item.getBookId())) {
                throw new BusinessException("Phiếu mượn chứa các dòng trùng cùng một đầu sách.");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("Số lượng mượn phải lớn hơn 0.");
            }
            validItems.add(item);
        }

        if (validItems.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất một đầu sách hợp lệ.");
        }

        // Validate due date
        if (form.getDueDate() == null || form.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Hạn trả sách phải ở ngày hiện tại hoặc tương lai.");
        }

        Borrowing borrowing = new Borrowing(member, form.getDueDate().atTime(23, 59, 59));

        for (BorrowItemForm item : validItems) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách ID: " + item.getBookId()));

            if (book.getStatus() != BookStatus.AVAILABLE) {
                throw new BusinessException("Sách '" + book.getTitle() + "' hiện không ở trạng thái sẵn sàng cho mượn.");
            }

            if (book.getAvailableQuantity() < item.getQuantity()) {
                throw new BusinessException("Sách '" + book.getTitle() + "' không đủ số lượng mượn (Khả dụng: " + book.getAvailableQuantity() + ", yêu cầu: " + item.getQuantity() + ")");
            }

            // Reduce stock inside transaction
            book.setAvailableQuantity(book.getAvailableQuantity() - item.getQuantity());
            bookRepository.save(book);

            BorrowingDetail detail = new BorrowingDetail(borrowing, book, item.getQuantity());
            borrowing.getDetails().add(detail);
        }

        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing returnBooks(ReturnForm form) {
        Borrowing borrowing = getBorrowingById(form.getBorrowingId());

        if (borrowing.getStatus() == BorrowingStatus.RETURNED || borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new BusinessException("Phiếu mượn đã hoàn thành hoặc bị hủy, không thể thực hiện trả sách.");
        }

        FinePolicy activePolicy = finePolicyService.getActivePolicy();
        LocalDate actualReturnDate = form.getReturnDate() != null ? form.getReturnDate() : LocalDate.now();

        boolean processedAnyItem = false;

        for (ReturnItemForm item : form.getItems()) {
            if (item.getReturnQuantity() == null || item.getReturnQuantity() <= 0) {
                continue;
            }

            BorrowingDetail detail = borrowingDetailRepository.findById(item.getDetailId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết mượn ID: " + item.getDetailId()));

            int remaining = detail.getQuantity() - detail.getReturnedQuantity();
            if (item.getReturnQuantity() > remaining) {
                throw new BusinessException("Số lượng trả (" + item.getReturnQuantity() + ") vượt quá số lượng còn mượn (" + remaining + ") của sách '" + detail.getBook().getTitle() + "'");
            }

            processedAnyItem = true;

            // Calculate fine for this batch of returned books
            FineCalculatorService.FineResult fineResult = fineCalculatorService.calculateFine(
                    borrowing.getDueDate().toLocalDate(),
                    actualReturnDate,
                    item.getReturnQuantity(),
                    activePolicy
            );

            // Update detail
            detail.setReturnedQuantity(detail.getReturnedQuantity() + item.getReturnQuantity());
            detail.setLastReturnedDate(actualReturnDate.atStartOfDay());
            detail.setLateDays(Math.max(detail.getLateDays(), fineResult.getCalendarLateDays()));
            detail.setFineAmount(detail.getFineAmount().add(fineResult.getFineAmount()));
            borrowingDetailRepository.save(detail);

            // Return stock to Book
            Book book = detail.getBook();
            book.setAvailableQuantity(book.getAvailableQuantity() + item.getReturnQuantity());
            bookRepository.save(book);

            // Accumulate total fine on borrowing
            borrowing.setTotalFineAmount(borrowing.getTotalFineAmount().add(fineResult.getFineAmount()));
        }

        if (!processedAnyItem) {
            throw new BusinessException("Vui lòng nhập số lượng trả lớn hơn 0 cho ít nhất một đầu sách.");
        }

        borrowing.recalculateUnpaidFine();

        // Update overall borrowing status
        if (borrowing.isFullyReturned()) {
            borrowing.setReturnedDate(actualReturnDate.atStartOfDay());
            if (borrowing.getUnpaidFineAmount().compareTo(BigDecimal.ZERO) > 0) {
                borrowing.setStatus(BorrowingStatus.FINE_PENDING);
            } else {
                borrowing.setStatus(BorrowingStatus.RETURNED);
            }
        } else {
            if (actualReturnDate.isAfter(borrowing.getDueDate().toLocalDate())) {
                borrowing.setStatus(BorrowingStatus.OVERDUE);
            } else {
                borrowing.setStatus(BorrowingStatus.PARTIALLY_RETURNED);
            }
        }

        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing payFine(FinePaymentForm form) {
        Borrowing borrowing = getBorrowingById(form.getBorrowingId());

        if (borrowing.getUnpaidFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Phiếu mượn này không có phí phạt cần thanh toán.");
        }

        if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thanh toán phải lớn hơn 0.");
        }

        if (form.getAmount().compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new BusinessException("Số tiền thanh toán (" + form.getAmount() + " VNĐ) không được vượt quá số phí còn phải thu (" + borrowing.getUnpaidFineAmount() + " VNĐ).");
        }

        FinePayment payment = new FinePayment(
                borrowing,
                form.getAmount(),
                form.getMethod() != null ? form.getMethod() : "Tiền mặt",
                form.getNote()
        );
        finePaymentRepository.save(payment);

        borrowing.setPaidFineAmount(borrowing.getPaidFineAmount().add(form.getAmount()));
        borrowing.recalculateUnpaidFine();

        if (borrowing.getUnpaidFineAmount().compareTo(BigDecimal.ZERO) == 0 && borrowing.isFullyReturned()) {
            borrowing.setStatus(BorrowingStatus.FINE_PAID);
        }

        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing waiveFine(FineWaiverForm form) {
        Borrowing borrowing = getBorrowingById(form.getBorrowingId());

        if (borrowing.getUnpaidFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Phiếu mượn này không có phí phạt cần miễn giảm.");
        }

        if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền miễn giảm phải lớn hơn 0.");
        }

        if (form.getAmount().compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new BusinessException("Số tiền miễn giảm không được vượt quá số phí còn lại (" + borrowing.getUnpaidFineAmount() + " VNĐ).");
        }

        FineWaiver waiver = new FineWaiver(
                borrowing,
                form.getAmount(),
                form.getReason(),
                form.getApprovedBy()
        );
        fineWaiverRepository.save(waiver);

        borrowing.setWaivedFineAmount(borrowing.getWaivedFineAmount().add(form.getAmount()));
        borrowing.recalculateUnpaidFine();

        if (borrowing.getUnpaidFineAmount().compareTo(BigDecimal.ZERO) == 0 && borrowing.isFullyReturned()) {
            borrowing.setStatus(BorrowingStatus.FINE_PAID);
        }

        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing cancelBorrowing(Long id) {
        Borrowing borrowing = getBorrowingById(id);

        for (BorrowingDetail detail : borrowing.getDetails()) {
            if (detail.getReturnedQuantity() > 0) {
                throw new BusinessException("Phiếu mượn đã thực hiện trả sách, không thể hủy phiếu.");
            }
        }

        if (borrowing.getTotalFineAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Phiếu mượn đã phát sinh phí phạt, không thể hủy phiếu.");
        }

        // Return stock for all reserved books
        for (BorrowingDetail detail : borrowing.getDetails()) {
            Book book = detail.getBook();
            book.setAvailableQuantity(book.getAvailableQuantity() + detail.getQuantity());
            bookRepository.save(book);
        }

        borrowing.setStatus(BorrowingStatus.CANCELLED);
        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public void checkAndUpdateOverdueStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Borrowing> pendingOverdue = borrowingRepository.findPendingOverdueBorrowings(now);
        for (Borrowing b : pendingOverdue) {
            b.setStatus(BorrowingStatus.OVERDUE);
            borrowingRepository.save(b);
        }
    }
}
