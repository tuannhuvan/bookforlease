package org.example.demobookforlease.model;

import jakarta.annotation.Generated;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table (name = "book_for_lease")
public class bookforlease {

    @Id
    @Generated(Strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 255)
    private String title;

    @Column (nullable = false, length = 150)
    private String author;

    @Column (unique = true, nullable = false, length = 50)
    private String isbn;

    @Column (name = "price_per_day", nullable = false)
    private BigDecimal pricePerDay;

    @Column (name = "deposit_price", nullable = false)
    private BigDecimal depositPrice;

    @Column (name = "available_copies",nullable = false)
    private Integer availableCopies;

    @Column (name = "total_quantity", nullable = false)
    private BigDecimal totalQuantity;

    @Column (name = "available_quantity", nullable = false)
    private BigDecimal availableQuantity;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private BookStatus status = BookStatus.available;

    // moi quan he: nhieu sach thuoc ve mot the loai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "category_id", nullable = false) // khoa ngoai
    private Category category;

    @Column (name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // 1. Sau khi nhap ten cot xong thi click chuot phai chon generate -> chon constructor Constructor (hàm khởi tạo) là một phương thức đặc biệt trong lập trình hướng đối tượng dùng để khởi tạo giá trị ban đầu cho đối tượng khi nó được tạo ra
    // Ý nghĩa chính của Constructor
    // Khởi tạo đối tượng: Tự động chạy khi dùng từ khóa tạo đối tượng (như new) để cấp phát bộ nhớ và gán dữ liệu ban đầu.
    // Thiết lập trạng thái: Giúp đối tượng mang giá trị hợp lệ ngay từ lúc sinh ra, tránh tình trạng dữ liệu rỗng (null) hoặc sai lệch.
    // Tiết kiệm mã nguồn: Thay vì phải gọi từng hàm gán giá trị sau khi tạo đối tượng, constructor giúp gán giá trị trực tiếp qua tham số truyền vào.
    public bookforlease(Long id, String title, String author, BigDecimal pricePerDay, String isbn, BigDecimal depositPrice, Integer availableCopies, BigDecimal totalQuantity, BigDecimal availableQuantity, BookStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.pricePerDay = pricePerDay;
        this.isbn = isbn;
        this.depositPrice = depositPrice;
        this.availableCopies = availableCopies;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.status = status;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public bookforlease() {

    }

    // click chuot phai chon generate -> chon getter & setter
    // Getter và setter là các phương thức dùng để lấy (get) và gán/cập nhật (set) giá trị của các thuộc tính được bảo vệ (private) bên trong một đối tượng
    // Ý nghĩa chính
    // Bảo vệ dữ liệu (Encapsulation): Giúp che giấu dữ liệu gốc bên trong đối tượng, không cho phép bên ngoài can thiệp trực tiếp vào biến private.
    // Kiểm soát giá trị (Validation): Hàm setter đóng vai trò như "lính gác", giúp kiểm tra tính hợp lệ của dữ liệu trước khi gán mới (ví dụ: tuổi phải lớn hơn 0).
    // Quản lý quyền truy cập: Cho phép cấu hình chỉ đọc (chỉ viết getter mà không viết setter cho các trường như mã giao dịch) hoặc chỉ ghi tùy theo nghiệp vụ.
    // Dễ bảo trì mã nguồn: Khi cần thay đổi logic hiển thị hoặc lưu trữ dữ liệu, bạn chỉ cần sửa bên trong hàm getter/setter mà không ảnh hưởng đến các đoạn code ở bên ngoài

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public BigDecimal getDepositPrice() {
        return depositPrice;
    }

    public void setDepositPrice(BigDecimal depositPrice) {
        this.depositPrice = depositPrice;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public Category getCategory () {return category; }

    public void setCategory (Category category) {this.category = category; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    // @PrePersist và @PreUpdate là các chú thích (annotations) dùng để tự động kích hoạt một hàm xử lý trước khi dữ liệu được lưu vào cơ sở dữ liệu.
    // Chúng giúp tự động hóa các tác vụ như ghi nhận thời gian tạo, thời gian cập nhật hoặc chuẩn hóa dữ liệu mà không cần viết mã thủ công nhiều lần
    // @PrePersist (Trước khi thêm mới): Hàm được gắn @PrePersist sẽ tự động chạy ngay trước khi một thực thể (Entity) mới được thêm vào database (khi gọi hàm persist(), save(),
    // hoặc khi lệnh INSERT chuẩn bị chạy)
    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected  void  onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

}
