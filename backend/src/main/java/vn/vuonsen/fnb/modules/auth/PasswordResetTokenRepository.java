package vn.vuonsen.fnb.modules.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /*
     * Xin liên kết mới thì các liên kết cũ chưa dùng phải hết hiệu lực ngay.
     *
     * Không làm vậy thì một người từng xin liên kết rồi bỏ đó vẫn còn một đường vào tài khoản
     * mở suốt cho tới khi hết hạn.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :bayGio "
            + "WHERE t.user.id = :userId AND t.usedAt IS NULL")
    void voHieuHoaCacPhieuCu(@Param("userId") Long userId, @Param("bayGio") LocalDateTime bayGio);
}
