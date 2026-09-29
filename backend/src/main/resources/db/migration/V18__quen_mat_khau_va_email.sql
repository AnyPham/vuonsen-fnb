/*
 * Đặt lại mật khẩu và nhật ký email.
 *
 * Trước đây khách quên mật khẩu là hết cách, không có đường nào lấy lại tài khoản. Nay khách
 * nhập email, hệ thống gửi một liên kết kèm mã dùng một lần, bấm vào đặt mật khẩu mới.
 *
 * Mã lưu dưới dạng băm chứ không lưu nguyên văn: lỡ có người đọc được bảng này thì cũng không
 * dựng lại được liên kết để chiếm tài khoản người khác, giống cách lưu mật khẩu.
 */
CREATE TABLE password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,

    -- Băm SHA-256 của mã gửi trong email, không lưu mã gốc
    token_hash  VARCHAR(64)  NOT NULL,

    expires_at  DATETIME     NOT NULL,
    used_at     DATETIME     NULL,
    created_at  DATETIME     NOT NULL,

    CONSTRAINT uk_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_reset_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_reset_token_user ON password_reset_tokens (user_id, expires_at);

/*
 * Nhật ký email.
 *
 * Hệ thống có thể chạy ở chế độ không gửi thư thật, khi đó nội dung thư chỉ ghi vào bảng này.
 * Kể cả khi gửi thật thì vẫn ghi lại, để sau này khách báo "tôi không nhận được email" thì
 * còn có chỗ tra xem hệ thống đã gửi gì, lúc nào, và gửi có lỗi không.
 */
CREATE TABLE email_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient    VARCHAR(160)  NOT NULL,
    subject      VARCHAR(255)  NOT NULL,
    body         TEXT          NOT NULL,

    -- XAC_NHAN_DON / DAT_LAI_MAT_KHAU / KHAC
    kind         VARCHAR(40)   NOT NULL,

    -- SENT nếu máy chủ thư nhận, LOGGED nếu chỉ ghi lại, FAILED nếu gửi hỏng
    status       VARCHAR(20)   NOT NULL,
    error_detail VARCHAR(500)  NULL,
    created_at   DATETIME      NOT NULL
);

CREATE INDEX idx_email_log_recipient ON email_logs (recipient, created_at);
