package org.example.demobookforlease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class FineWaiverForm {

    @NotNull
    private Long borrowingId;

    @NotNull(message = "Vui lòng nhập số tiền miễn/giảm")
    @DecimalMin(value = "0.01", message = "Số tiền miễn/giảm phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Lý do miễn giảm là bắt buộc")
    private String reason;

    @NotBlank(message = "Người duyệt là bắt buộc")
    private String approvedBy;

    public FineWaiverForm() {
    }

    public FineWaiverForm(Long borrowingId, BigDecimal amount) {
        this.borrowingId = borrowingId;
        this.amount = amount;
    }

    public Long getBorrowingId() {
        return borrowingId;
    }

    public void setBorrowingId(Long borrowingId) {
        this.borrowingId = borrowingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
