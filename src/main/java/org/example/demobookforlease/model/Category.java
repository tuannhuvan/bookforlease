package org.example.demobookforlease.model;

import jakarta.annotation.Generated;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table (name = "categories")
public class Category {
    public Category() {
    }

    @Id
    @Generated(Strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 255)
    private String name;

    @Column (length = 500)
    private String description;

    @Column (nullable = false)
    private Boolean active;

    @Column (name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column (name = "update_at")
    private LocalDateTime updateAt;

    // Quan he: mot the loai co nhieu sach
    @OneToMany (mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<bookforlease> books;

    // constructor


    public Category(Long id, String name, String description, Boolean active, LocalDateTime createAt, LocalDateTime updateAt, List<bookforlease> books) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.books = books;
    }

    // Getters & Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public List<bookforlease> getBooks() {
        return books;
    }

    public void setBooks(List<bookforlease> books) {
        this.books = books;
    }

    // Tự động kích hoạt danh mục (active = true) trước khi INSERT vào database
    // nếu thuộc tính này đang bị để trống (null)
    @PrePersist
    protected void onCreate () {
        // NHIỆM VỤ: Đảm bảo mọi danh mục khi mới tạo ra, nếu lập trình viên
        // quên không gán trạng thái, thì mặc định luôn ở trạng thái "Kích hoạt" (true).
//        if (this.active == null) {
//            this.active = true;
//        }
//        // Cũng có thể chuẩn hóa dữ liệu tại đây, ví dụ: viết hoa chữ cái đầu hoặc trim() khoảng trắng
//        if (this.name != null) {
//            this.name = this.name.trim();
//        }
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        // Chuẩn hóa dữ liệu trước khi lệnh UPDATE chạy
//        if (this.name != null) {
//            this.name = this.name.trim();
//        }

        // Ví dụ logic doanh nghiệp: Nếu một Danh mục bị tắt (active = false),
        // Có thể tự động ghi nhận thêm lý do hoặc xử lý logic ẩn danh mục tại đây (nếu cần).
        this.updateAt = LocalDateTime.now();
    }
}
