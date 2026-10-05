package org.example.demobookforlease.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BorrowItemForm {

    @NotNull(message = "Vui lòng chọn sách")
    private Long bookId;

    @NotNull(message = "Vui lòng nhập số lượng")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity = 1;

    public BorrowItemForm() {
    }

    public BorrowItemForm(Long bookId, Integer quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
