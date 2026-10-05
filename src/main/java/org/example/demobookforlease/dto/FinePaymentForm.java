package org.example.demobookforlease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class FinePaymentForm {

    @NotNull
    private Long borrowingId;

    @NotNull(message = "Vui lòng nhập số tiền thanh toán")
    @DecimalMin(value = "0.01", message = "Số tiền thanh toán phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Vui lòng chọn hình thức thanh toán")
    private String method = "Tiền mặt";

    private String note;

    public FinePaymentForm() {
    }

    public FinePaymentForm(Long borrowingId, BigDecimal amount) {
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
