package org.example.demobookforlease.model;

import jakarta.annotation.Generated;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table (name = "members")
public class Member {
    @Id
    @Generated(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column (nullable = false, unique = true, length = 100)
    private String email;

    @Column (nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private MemberStatus status = MemberStatus.active;

    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    // Môí quan hệ
    @OneToMany (mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Borrowing> borrowings;
    // Constructor


    public Member(Long id, String fullName, String email, String phone, MemberStatus status, LocalDateTime createAt, List<Borrowing> borrowings) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createAt = createAt;
        this.borrowings = borrowings;
    }

    // Getters & Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public List<Borrowing> getBorrowings() {
        return borrowings;
    }

    public void setBorrowings(List<Borrowing> borrowings) {
        this.borrowings = borrowings;
    }

    @PrePersist
    protected void onCreate(){
        this.createAt = LocalDateTime.now();
    }
}
