package org.example.demobookforlease.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReturnItemForm {

    @NotNull
    private Long detailId;

    private Long bookId;
    private String bookTitle;
    private Integer totalBorrowed;
    private Integer alreadyReturned;
    private Integer remainingToReturn;

    @Min(value = 0, message = "Số lượng trả không thể nhỏ hơn 0")
    private Integer returnQuantity = 0;

    public ReturnItemForm() {
    }

    public ReturnItemForm(Long detailId, Long bookId, String bookTitle, Integer totalBorrowed, Integer alreadyReturned) {
        this.detailId = detailId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.totalBorrowed = totalBorrowed;
        this.alreadyReturned = alreadyReturned;
        this.remainingToReturn = totalBorrowed - alreadyReturned;
        this.returnQuantity = 0;
    }

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Integer getTotalBorrowed() {
        return totalBorrowed;
    }

    public void setTotalBorrowed(Integer totalBorrowed) {
        this.totalBorrowed = totalBorrowed;
    }

    public Integer getAlreadyReturned() {
        return alreadyReturned;
    }

    public void setAlreadyReturned(Integer alreadyReturned) {
        this.alreadyReturned = alreadyReturned;
    }

    public Integer getRemainingToReturn() {
        return remainingToReturn;
    }

    public void setRemainingToReturn(Integer remainingToReturn) {
        this.remainingToReturn = remainingToReturn;
    }

    public Integer getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Integer returnQuantity) {
        this.returnQuantity = returnQuantity;
    }
}
