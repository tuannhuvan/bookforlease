package org.example.demobookforlease.model;

// thành viên cũng cần một Enum để quản lý trạng thái tài khoản (ví dụ: đang hoạt động, bị khóa, hoặc quá hạn)
public enum MemberStatus {
    active, // Đang hoạt động bình thường
    locked, // Bị khóa tài khoản (do vi phạm hoặc mất thẻ)
    expired // Thẻ thành viên đã hết hạn
}
