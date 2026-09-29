/*
 * Bù cột updated_at cho hai bảng vừa thêm ở V19 trước đó.
 *
 * Hai entity PasswordResetToken và EmailLog đều kế thừa BaseEntity, mà lớp cha đó có sẵn hai
 * cột thời gian là created_at và updated_at. Migration V18 chỉ tạo created_at nên Hibernate
 * kiểm tra cấu trúc bảng lúc khởi động và từ chối chạy.
 *
 * Đây là loại lỗi chỉ lộ ra khi khởi động thật chứ trình biên dịch không bắt được: viết SQL
 * và viết entity là hai nơi tách rời, quên đồng bộ một cột là hỏng. Sửa bằng migration mới
 * chứ không sửa V18, vì V18 đã chạy rồi và Flyway lưu mã kiểm tra của nó.
 */
ALTER TABLE password_reset_tokens ADD COLUMN updated_at DATETIME NULL;
ALTER TABLE email_logs            ADD COLUMN updated_at DATETIME NULL;
