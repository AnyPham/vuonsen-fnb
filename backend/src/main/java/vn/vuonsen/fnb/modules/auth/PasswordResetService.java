package vn.vuonsen.fnb.modules.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.config.props.MailProperties;
import vn.vuonsen.fnb.modules.notification.EmailService;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/*
 * Quên mật khẩu và đặt lại mật khẩu.
 *
 * Luồng gồm hai bước tách rời: khách nhập email để xin liên kết, rồi bấm liên kết trong thư
 * để đặt mật khẩu mới. Tách làm hai vì bước một chỉ cần biết email, còn bước hai cần chứng
 * minh rằng người đặt lại chính là người đọc được hòm thư đó.
 *
 * Hai điểm về an toàn đáng nói:
 *
 *   - Nhập email không tồn tại vẫn báo thành công y hệt email có thật. Báo khác nhau thì
 *     người ngoài dò được email nào đã đăng ký trên hệ thống.
 *   - Mã trong liên kết sinh bằng bộ sinh số ngẫu nhiên an toàn và chỉ lưu bản băm. Trong cơ
 *     sở dữ liệu không có chỗ nào chứa mã gốc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    /** Liên kết sống 30 phút. Đủ để khách mở thư, mà lỡ lọt ra ngoài cũng chóng vô hiệu. */
    private static final int SO_PHUT_SONG = 30;

    /** 32 byte ngẫu nhiên, đủ dài để không ai đoán ra. */
    private static final int SO_BYTE_MA = 32;

    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final MailProperties mailProperties;

    /*
     * Bước một: khách nhập email, hệ thống gửi liên kết.
     *
     * Hàm này không bao giờ báo lỗi "email không tồn tại", để người ngoài không dò được danh
     * sách email đã đăng ký.
     */
    @Transactional
    public void xinDatLai(String email) {
        Optional<User> nguoiDung = userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim());
        if (nguoiDung.isEmpty()) {
            log.info("Có người xin đặt lại mật khẩu cho email chưa đăng ký: {}", email);
            return;
        }

        User u = nguoiDung.get();
        tokenRepository.voHieuHoaCacPhieuCu(u.getId(), LocalDateTime.now());

        String ma = sinhMa();
        tokenRepository.save(PasswordResetToken.builder()
                .user(u)
                .tokenHash(bam(ma))
                .expiresAt(LocalDateTime.now().plusMinutes(SO_PHUT_SONG))
                .build());

        String lienKet = "%s/dat-lai-mat-khau?ma=%s".formatted(mailProperties.baseUrl(), ma);
        emailService.gui(u.getEmail(), "Đặt lại mật khẩu tài khoản Vườn Sen", """
                Chào %s,

                Có người vừa yêu cầu đặt lại mật khẩu cho tài khoản này. Nếu là bạn, hãy mở
                liên kết dưới đây để đặt mật khẩu mới:

                %s

                Liên kết chỉ dùng được một lần và sẽ hết hạn sau %d phút.

                Nếu không phải bạn yêu cầu thì cứ bỏ qua thư này, mật khẩu hiện tại của bạn
                vẫn giữ nguyên.

                Vườn Sen
                """.formatted(u.getFullName(), lienKet, SO_PHUT_SONG), "DAT_LAI_MAT_KHAU");
    }

    /* Bước hai: khách mở liên kết và đặt mật khẩu mới. */
    @Transactional
    public void datLai(String ma, String matKhauMoi) {
        if (ma == null || ma.isBlank()) {
            throw new BusinessException("Thiếu mã đặt lại mật khẩu");
        }

        PasswordResetToken phieu = tokenRepository.findByTokenHash(bam(ma.trim()))
                .orElseThrow(() -> new BusinessException(
                        "Liên kết không đúng hoặc đã được dùng, vui lòng xin liên kết mới"));

        if (!phieu.conDungDuoc()) {
            throw new BusinessException(phieu.getUsedAt() != null
                    ? "Liên kết này đã được dùng rồi, vui lòng xin liên kết mới"
                    : "Liên kết đã hết hạn, vui lòng xin liên kết mới");
        }

        User u = phieu.getUser();
        u.setPasswordHash(passwordEncoder.encode(matKhauMoi));
        phieu.setUsedAt(LocalDateTime.now());

        log.info("Tài khoản {} vừa đặt lại mật khẩu", u.getEmail());
        emailService.gui(u.getEmail(), "Mật khẩu Vườn Sen đã được đổi", """
                Chào %s,

                Mật khẩu tài khoản của bạn vừa được đổi thành công.

                Nếu không phải bạn làm việc này, hãy liên hệ nhà hàng ngay để được hỗ trợ.

                Vườn Sen
                """.formatted(u.getFullName()), "DAT_LAI_MAT_KHAU");
    }

    /** Mã gửi trong liên kết: ngẫu nhiên an toàn, viết dạng base64 gọn cho địa chỉ web. */
    private String sinhMa() {
        byte[] bytes = new byte[SO_BYTE_MA];
        NGAU_NHIEN.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String bam(String ma) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(ma.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Máy không hỗ trợ SHA-256", e);
        }
    }
}
