package org.example.demobookforlease.controller;

import jakarta.validation.Valid;
import org.example.demobookforlease.dto.*;
import org.example.demobookforlease.exception.BusinessException;
import org.example.demobookforlease.model.Borrowing;
import org.example.demobookforlease.model.BorrowingDetail;
import org.example.demobookforlease.model.BorrowingStatus;
import org.example.demobookforlease.service.BookService;
import org.example.demobookforlease.service.BorrowingService;
import org.example.demobookforlease.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
@RequestMapping("/borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final MemberService memberService;
    private final BookService bookService;

    public BorrowingController(BorrowingService borrowingService,
                               MemberService memberService,
                               BookService bookService) {
        this.borrowingService = borrowingService;
        this.memberService = memberService;
        this.bookService = bookService;
    }

    @GetMapping
    public String listBorrowings(@RequestParam(required = false) Long memberId,
                                 @RequestParam(required = false) BorrowingStatus status,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) {

        // Auto update overdue statuses before rendering
        borrowingService.checkAndUpdateOverdueStatuses();

        LocalDateTime fromLdt = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toLdt = toDate != null ? toDate.atTime(23, 59, 59) : null;

        Page<Borrowing> borrowingsPage = borrowingService.searchBorrowings(memberId, status, fromLdt, toLdt, PageRequest.of(page, size));

        model.addAttribute("borrowingsPage", borrowingsPage);
        model.addAttribute("members", memberService.getActiveMembers());
        model.addAttribute("statuses", BorrowingStatus.values());
        model.addAttribute("memberId", memberId);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "borrowings/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        BorrowForm form = new BorrowForm();
        form.setDueDate(LocalDate.now().plusDays(14)); // Mặc định hạn mượn 14 ngày

        model.addAttribute("borrowForm", form);
        model.addAttribute("members", memberService.getActiveMembers());
        model.addAttribute("availableBooks", bookService.getAvailableBooks());
        return "borrowings/create";
    }

    @PostMapping
    public String createBorrowing(@Valid @ModelAttribute("borrowForm") BorrowForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("members", memberService.getActiveMembers());
            model.addAttribute("availableBooks", bookService.getAvailableBooks());
            return "borrowings/create";
        }

        try {
            Borrowing borrowing = borrowingService.createBorrowing(form);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu mượn #" + borrowing.getId() + " thành công!");
            return "redirect:/borrowings/" + borrowing.getId();
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("members", memberService.getActiveMembers());
            model.addAttribute("availableBooks", bookService.getAvailableBooks());
            return "borrowings/create";
        }
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        Borrowing borrowing = borrowingService.getBorrowingById(id);
        model.addAttribute("borrowing", borrowing);
        model.addAttribute("paymentForm", new FinePaymentForm(borrowing.getId(), borrowing.getUnpaidFineAmount()));
        model.addAttribute("waiverForm", new FineWaiverForm(borrowing.getId(), borrowing.getUnpaidFineAmount()));
        return "borrowings/detail";
    }

    @GetMapping("/{id}/return")
    public String showReturnForm(@PathVariable Long id, Model model) {
        Borrowing borrowing = borrowingService.getBorrowingById(id);
        ReturnForm form = new ReturnForm(borrowing.getId());
        form.setItems(new ArrayList<>());

        for (BorrowingDetail detail : borrowing.getDetails()) {
            int remaining = detail.getQuantity() - detail.getReturnedQuantity();
            if (remaining > 0) {
                ReturnItemForm itemForm = new ReturnItemForm(
                        detail.getId(),
                        detail.getBook().getId(),
                        detail.getBook().getTitle(),
                        detail.getQuantity(),
                        detail.getReturnedQuantity()
                );
                form.getItems().add(itemForm);
            }
        }

        model.addAttribute("borrowing", borrowing);
        model.addAttribute("returnForm", form);
        return "borrowings/return";
    }

    @PostMapping("/{id}/return")
    public String executeReturn(@PathVariable Long id,
                                @Valid @ModelAttribute("returnForm") ReturnForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        Borrowing borrowing = borrowingService.getBorrowingById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("borrowing", borrowing);
            return "borrowings/return";
        }

        try {
            form.setBorrowingId(id);
            Borrowing updated = borrowingService.returnBooks(form);
            redirectAttributes.addFlashAttribute("successMessage", "Xử lý trả sách cho phiếu mượn #" + updated.getId() + " thành công!");
            return "redirect:/borrowings/" + updated.getId();
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("borrowing", borrowing);
            return "borrowings/return";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelBorrowing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowingService.cancelBorrowing(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy phiếu mượn #" + id + " thành công.");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borrowings/" + id;
    }

    @GetMapping("/{id}/payments/new")
    public String showPaymentForm(@PathVariable Long id, Model model) {
        Borrowing borrowing = borrowingService.getBorrowingById(id);
        model.addAttribute("borrowing", borrowing);
        model.addAttribute("paymentForm", new FinePaymentForm(borrowing.getId(), borrowing.getUnpaidFineAmount()));
        return "borrowings/payment";
    }

    @PostMapping("/{id}/payments")
    public String executePayment(@PathVariable Long id,
                                 @Valid @ModelAttribute("paymentForm") FinePaymentForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        Borrowing borrowing = borrowingService.getBorrowingById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("borrowing", borrowing);
            return "borrowings/payment";
        }

        try {
            form.setBorrowingId(id);
            borrowingService.payFine(form);
            redirectAttributes.addFlashAttribute("successMessage", "Ghi nhận thanh toán phí phạt thành công!");
            return "redirect:/borrowings/" + id;
        } catch (BusinessException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("borrowing", borrowing);
            return "borrowings/payment";
        }
    }

    @PostMapping("/{id}/waivers")
    public String executeWaiver(@PathVariable Long id,
                               @Valid @ModelAttribute("waiverForm") FineWaiverForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thông tin miễn giảm phí không hợp lệ!");
            return "redirect:/borrowings/" + id;
        }

        try {
            form.setBorrowingId(id);
            borrowingService.waiveFine(form);
            redirectAttributes.addFlashAttribute("successMessage", "Ghi nhận miễn/giảm phí phạt thành công!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/borrowings/" + id;
    }
}
