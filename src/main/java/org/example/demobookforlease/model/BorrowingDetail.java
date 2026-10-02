package org.example.demobookforlease.model;

import jakarta.persistence.*;

import javax.annotation.processing.Generated;
import java.math.BigDecimal;

@Entity
@Table (name = "borrowing_details")
public class BorrowingDetail {
    @Id
    @Generated(strategy = GenerationType.IDENTITY)
    private Long id;

    // moi quan he: nhieu dong chi tiet thuoc ve mot phieu muon
    @ManyToMany (fetch = FetchType.LAZY)
    @JoinColumn (name = "borrowing_id", nullable = false)
    private Borrowing borrowing;

    @Column (nullable = false)
    private Integer quantity; // so luong dang ky muon ban dau

    @Column (name = "returned_quantity", nullable = false)
    private Integer returnedQuantity = 0; // so luong thuc te da tra (mac dinh bang 0)

    @Column(name = "fine_amount", nullable = false)
    private BigDecimal fineAmount = BigDecimal.ZERO; // tien phat rieng cua dau sach nay neu qua han/ hong

    // constructor


    public BorrowingDetail(Long id, Borrowing borrowing, Integer quantity, Integer returnedQuantity, BigDecimal fineAmount) {
        this.id = id;
        this.borrowing = borrowing;
        this.quantity = quantity;
        this.returnedQuantity = returnedQuantity;
        this.fineAmount = fineAmount;
    }

    // getters & setters

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

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }
}
