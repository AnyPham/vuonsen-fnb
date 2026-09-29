package vn.vuonsen.fnb.modules.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;
import vn.vuonsen.fnb.modules.user.User;

import java.time.LocalDateTime;

/*
 * Phiếu cho phép đặt lại mật khẩu một lần.
 *
 * Chỉ lưu bản băm của mã, không lưu mã gốc. Ai đọc được bảng này cũng không dựng lại được
 * liên kết trong email để chiếm tài khoản người khác, đúng lý do mà mật khẩu cũng phải băm.
 *
 * Phiếu hết hạn sau một khoảng ngắn và chỉ dùng được một lần: dùng xong thì đánh dấu thời
 * điểm dùng, lần sau bấm lại vào cùng liên kết sẽ bị từ chối.
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public boolean conDungDuoc() {
        return usedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }
}
