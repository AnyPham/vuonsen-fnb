package vn.vuonsen.fnb.modules.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;

import java.time.LocalDateTime;

// Tài khoản khách hàng và nhân viên, mật khẩu lưu dạng băm BCrypt
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 160, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /*
     * Mã xác nhận email, chỉ có giá trị trong lúc tài khoản còn chờ xác thực.
     *
     * Xác thực xong thì xóa cả hai cột về rỗng. Giữ lại mã đã dùng không được lợi gì, mà
     * còn để một chuỗi bí mật nằm trong cơ sở dữ liệu lâu hơn mức cần thiết.
     */
    @Column(name = "verification_code", length = 10)
    private String verificationCode;

    @Column(name = "verification_expires_at")
    private LocalDateTime verificationExpiresAt;

    /** Mã còn hạn dùng hay không. Chưa cấp mã hoặc đã xác thực xong đều tính là không. */
    public boolean maXacThucConHan(LocalDateTime bayGio) {
        return verificationCode != null
                && verificationExpiresAt != null
                && bayGio.isBefore(verificationExpiresAt);
    }
}
