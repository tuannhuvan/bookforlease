package org.example.demobookforlease.model;

import jakarta.annotation.Generated;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table (name = "borrowings")
public class Borrowing {
    @Id
    @Generated(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien ket vs bang member (thay vi dung truong memberId kieu Long, JPA dung Object Entity)
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column (name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column (name = "returned_date") // cho phep null khi chua tra sach
    private LocalDateTime returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status = BorrowingStatus.borrowing;

    @Column (nullable = false)
    private BigDecimal totalFine = BigDecimal.ZERO; // mac dinh phat bang 0

    // moi quan he: mot phieu muon co nhieu dong chi tiet sach
    @OneToMany(mappedBy = "borrowing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List <BorrowingDetail> details;

    // constructor

    public Borrowing(Long id, Member member, LocalDateTime borrowDate, LocalDateTime dueDate, LocalDateTime returnedDate, BorrowingStatus status, BigDecimal totalFine, List<BorrowingDetail> details) {
        this.id = id;
        this.member = member;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnedDate = returnedDate;
        this.status = status;
        this.totalFine = totalFine;
        this.details = details;
    }

    // getters & setters

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

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
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

    public BigDecimal getTotalFine() {
        return totalFine;
    }

    public void setTotalFine(BigDecimal totalFine) {
        this.totalFine = totalFine;
    }

    public List<BorrowingDetail> getDetails() {
        return details;
    }

    public void setDetails(List<BorrowingDetail> details) {
        this.details = details;
    }

    @PrePersist
    protected void onCreate (){
        if (this.borrowDate == null) {
            this.borrowDate = LocalDateTime.now();
        }
    }

}
