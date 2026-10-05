package org.example.demobookforlease.dto;

import java.math.BigDecimal;

public class OverdueReportDTO {

    private Long borrowingId;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String memberPhone;
    private Long daysOverdue;
    private Integer remainingBooksCount;
    private BigDecimal estimatedFine;
    private BigDecimal unpaidFineAmount;

    public OverdueReportDTO() {
    }

    public OverdueReportDTO(Long borrowingId, Long memberId, String memberName, String memberEmail, String memberPhone, Long daysOverdue, Integer remainingBooksCount, BigDecimal estimatedFine, BigDecimal unpaidFineAmount) {
        this.borrowingId = borrowingId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.memberPhone = memberPhone;
        this.daysOverdue = daysOverdue;
        this.remainingBooksCount = remainingBooksCount;
        this.estimatedFine = estimatedFine;
        this.unpaidFineAmount = unpaidFineAmount;
    }

    public Long getBorrowingId() {
        return borrowingId;
    }

    public void setBorrowingId(Long borrowingId) {
        this.borrowingId = borrowingId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public Long getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Long daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public Integer getRemainingBooksCount() {
        return remainingBooksCount;
    }

    public void setRemainingBooksCount(Integer remainingBooksCount) {
        this.remainingBooksCount = remainingBooksCount;
    }

    public BigDecimal getEstimatedFine() {
        return estimatedFine;
    }

    public void setEstimatedFine(BigDecimal estimatedFine) {
        this.estimatedFine = estimatedFine;
    }

    public BigDecimal getUnpaidFineAmount() {
        return unpaidFineAmount;
    }

    public void setUnpaidFineAmount(BigDecimal unpaidFineAmount) {
        this.unpaidFineAmount = unpaidFineAmount;
    }
}
