package org.example.demobookforlease.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowing_details")
public class BorrowingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrowing_id", nullable = false)
    private Borrowing borrowing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer quantity; // Số lượng đăng ký mượn ban đầu

    @Column(name = "returned_quantity", nullable = false)
    private Integer returnedQuantity = 0; // Số lượng thực tế đã trả

    @Column(name = "last_returned_date")
    private LocalDateTime lastReturnedDate;

    @Column(name = "late_days", nullable = false)
    private Integer lateDays = 0;

    @Column(name = "fine_amount", nullable = false)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    public BorrowingDetail() {
    }

    public BorrowingDetail(Borrowing borrowing, Book book, Integer quantity) {
        this.borrowing = borrowing;
        this.book = book;
        this.quantity = quantity;
        this.returnedQuantity = 0;
        this.fineAmount = BigDecimal.ZERO;
        this.lateDays = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Borrowing getBorrowing() {
        return borrowing;
    }

    public void setBorrowing(Borrowing borrowing) {
        this.borrowing = borrowing;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(Integer returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public LocalDateTime getLastReturnedDate() {
        return lastReturnedDate;
    }

    public void setLastReturnedDate(LocalDateTime lastReturnedDate) {
        this.lastReturnedDate = lastReturnedDate;
    }

    public Integer getLateDays() {
        return lateDays;
    }

    public void setLateDays(Integer lateDays) {
        this.lateDays = lateDays;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }
}
