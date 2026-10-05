package org.example.demobookforlease.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrowings")
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "borrowed_date", nullable = false)
    private LocalDateTime borrowedDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "returned_date")
    private LocalDateTime returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status = BorrowingStatus.BORROWING;

    @Column(name = "total_fine_amount", nullable = false)
    private BigDecimal totalFineAmount = BigDecimal.ZERO;

    @Column(name = "paid_fine_amount", nullable = false)
    private BigDecimal paidFineAmount = BigDecimal.ZERO;

    @Column(name = "unpaid_fine_amount", nullable = false)
    private BigDecimal unpaidFineAmount = BigDecimal.ZERO;

    @Column(name = "waived_fine_amount", nullable = false)
    private BigDecimal waivedFineAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<BorrowingDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FinePayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FineWaiver> waivers = new ArrayList<>();

    public Borrowing() {
    }

    public Borrowing(Member member, LocalDateTime dueDate) {
        this.member = member;
        this.borrowedDate = LocalDateTime.now();
        this.dueDate = dueDate;
        this.status = BorrowingStatus.BORROWING;
    }

    @PrePersist
    protected void onCreate() {
        if (this.borrowedDate == null) {
            this.borrowedDate = LocalDateTime.now();
        }
    }

    public void recalculateUnpaidFine() {
        if (totalFineAmount == null) totalFineAmount = BigDecimal.ZERO;
        if (paidFineAmount == null) paidFineAmount = BigDecimal.ZERO;
        if (waivedFineAmount == null) waivedFineAmount = BigDecimal.ZERO;
        
        BigDecimal remaining = totalFineAmount.subtract(paidFineAmount).subtract(waivedFineAmount);
        this.unpaidFineAmount = remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    // Helper to calculate total borrowed copies not yet returned
    public int getRemainingBooksCount() {
        if (details == null) return 0;
        return details.stream()
                .mapToInt(d -> Math.max(0, d.getQuantity() - d.getReturnedQuantity()))
                .sum();
    }

    public boolean isFullyReturned() {
        return getRemainingBooksCount() == 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDateTime getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDateTime borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDateTime returnedDate) {
        this.returnedDate = returnedDate;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalFineAmount() {
        return totalFineAmount;
    }

    public void setTotalFineAmount(BigDecimal totalFineAmount) {
        this.totalFineAmount = totalFineAmount;
    }

    public BigDecimal getPaidFineAmount() {
        return paidFineAmount;
    }

    public void setPaidFineAmount(BigDecimal paidFineAmount) {
        this.paidFineAmount = paidFineAmount;
    }

    public BigDecimal getUnpaidFineAmount() {
        return unpaidFineAmount;
    }

    public void setUnpaidFineAmount(BigDecimal unpaidFineAmount) {
        this.unpaidFineAmount = unpaidFineAmount;
    }

    public BigDecimal getWaivedFineAmount() {
        return waivedFineAmount;
    }

    public void setWaivedFineAmount(BigDecimal waivedFineAmount) {
        this.waivedFineAmount = waivedFineAmount;
    }

    public List<BorrowingDetail> getDetails() {
        return details;
    }

    public void setDetails(List<BorrowingDetail> details) {
        this.details = details;
    }

    public List<FinePayment> getPayments() {
        return payments;
    }

    public void setPayments(List<FinePayment> payments) {
        this.payments = payments;
    }

    public List<FineWaiver> getWaivers() {
        return waivers;
    }

    public void setWaivers(List<FineWaiver> waivers) {
        this.waivers = waivers;
    }
}
