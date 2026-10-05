package org.example.demobookforlease.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowForm {

    @NotNull(message = "Vui lòng chọn độc giả")
    private Long memberId;

    @NotNull(message = "Vui lòng chọn hạn trả sách")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @NotEmpty(message = "Phiếu mượn phải có ít nhất một đầu sách")
    @Valid
    private List<BorrowItemForm> items = new ArrayList<>();

    public BorrowForm() {
        this.items.add(new BorrowItemForm());
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<BorrowItemForm> getItems() {
        return items;
    }

    public void setItems(List<BorrowItemForm> items) {
        this.items = items;
    }
}
