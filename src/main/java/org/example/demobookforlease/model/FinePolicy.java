package org.example.demobookforlease.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fine_policies")
public class FinePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_fine_amount", nullable = false)
    private BigDecimal dailyFineAmount = BigDecimal.ZERO;

    @Column(name = "max_fine_amount")
    private BigDecimal maxFineAmount;

    @Column(name = "grace_days", nullable = false)
    private Integer graceDays = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public FinePolicy() {
    }

    public FinePolicy(BigDecimal dailyFineAmount, BigDecimal maxFineAmount, Integer graceDays, Boolean active) {
        this.dailyFineAmount = dailyFineAmount;
        this.maxFineAmount = maxFineAmount;
        this.graceDays = graceDays != null ? graceDays : 0;
        this.active = active != null ? active : true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getDailyFineAmount() {
        return dailyFineAmount;
    }

    public void setDailyFineAmount(BigDecimal dailyFineAmount) {
        this.dailyFineAmount = dailyFineAmount;
    }

    public BigDecimal getMaxFineAmount() {
        return maxFineAmount;
    }

    public void setMaxFineAmount(BigDecimal maxFineAmount) {
        this.maxFineAmount = maxFineAmount;
    }

    public Integer getGraceDays() {
        return graceDays;
    }

    public void setGraceDays(Integer graceDays) {
        this.graceDays = graceDays;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
