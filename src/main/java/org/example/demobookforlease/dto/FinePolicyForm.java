package org.example.demobookforlease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class FinePolicyForm {

    @NotNull(message = "Vui lòng nhập mức phí phạt mỗi ngày")
    @DecimalMin(value = "0.0", message = "Phí phạt mỗi ngày phải lớn hơn hoặc bằng 0")
    private BigDecimal dailyFineAmount;

    @DecimalMin(value = "0.0", message = "Phí phạt tối đa phải lớn hơn hoặc bằng 0")
    private BigDecimal maxFineAmount;

    @NotNull(message = "Vui lòng nhập số ngày ân hạn")
    @Min(value = 0, message = "Số ngày ân hạn không được âm")
    private Integer graceDays = 0;

    private Boolean active = true;

    public FinePolicyForm() {
    }

    public FinePolicyForm(BigDecimal dailyFineAmount, BigDecimal maxFineAmount, Integer graceDays, Boolean active) {
        this.dailyFineAmount = dailyFineAmount;
        this.maxFineAmount = maxFineAmount;
        this.graceDays = graceDays;
        this.active = active;
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
}
