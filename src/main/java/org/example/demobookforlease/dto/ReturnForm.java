package org.example.demobookforlease.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReturnForm {

    @NotNull
    private Long borrowingId;

    @NotNull(message = "Vui lòng chọn ngày trả thực tế")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDate = LocalDate.now();

    @Valid
    private List<ReturnItemForm> items = new ArrayList<>();

    public ReturnForm() {
    }

    public ReturnForm(Long borrowingId) {
        this.borrowingId = borrowingId;
        this.returnDate = LocalDate.now();
    }

    public Long getBorrowingId() {
        return borrowingId;
    }

    public void setBorrowingId(Long borrowingId) {
        this.borrowingId = borrowingId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public List<ReturnItemForm> getItems() {
        return items;
    }

    public void setItems(List<ReturnItemForm> items) {
        this.items = items;
    }
}
