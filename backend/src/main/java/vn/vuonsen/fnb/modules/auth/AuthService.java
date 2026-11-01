package vn.vuonsen.fnb.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.concurrency.ThuLaiKhiTrungMa;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.config.props.JwtProperties;
import vn.vuonsen.fnb.modules.auth.dto.AuthResponse;
import vn.vuonsen.fnb.modules.auth.dto.DangKyResponse;
import vn.vuonsen.fnb.modules.auth.dto.LoginRequest;
import vn.vuonsen.fnb.modules.auth.dto.RegisterRequest;
import vn.vuonsen.fnb.modules.auth.dto.UserResponse;
import vn.vuonsen.fnb.modules.user.Role;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;
import vn.vuonsen.fnb.security.JwtService;

import java.time.LocalDateTime;

// Đăng ký, đăng nhập và làm mới token
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final XacThucEmailService xacThucEmailService;

    @Transactional
    public DangKyResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Email này đã được đăng ký");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(request.email().toLowerCase().trim())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                /*
                 * Tài khoản phải xác nhận email mới dùng được.
                 *
                 * Trước đây đăng ký xong là vào thẳng, nên ai cũng ghi được một địa chỉ bất
                 * kỳ, kể cả địa chỉ của người khác. Mà hệ thống lại gửi thư xác nhận đơn và
                 * hóa đơn thanh toán về chính địa chỉ đó.
                 */
                .enabled(false)
                .build();

        User daLuu;
        try {
            // saveAndFlush để lệnh ghi chạy ngay tại đây, lỗi trùng lộ ra đúng chỗ bắt được
            daLuu = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            /*
             * Hai người đăng ký cùng một email cùng lúc: cả hai đều qua bước kiểm tra ở
             * trên vì chưa bên nào kịp lưu. Database chặn người sau bằng ràng buộc UNIQUE.
             * Báo đúng lý do thay vì để lọt thành lỗi 500.
             */
            if (ThuLaiKhiTrungMa.viPham(e, "uk_users_email")) {
                throw new BusinessException("Email này đã được đăng ký");
            }
            throw e;
        }

        /*
         * Không cấp token ở đây nữa. Tài khoản chỉ dùng được sau khi nhập đúng mã gửi về
         * email, nên đăng ký xong khách ở lại màn nhập mã chứ không vào thẳng.
         */
        xacThucEmailService.capMaVaGui(daLuu);
        return DangKyResponse.choXacThuc(daLuu.getEmail());
    }

    /*
     * Nhập mã xác nhận xong thì kích hoạt tài khoản và cấp token luôn.
     *
     * Cấp token ngay tại đây để khách không phải gõ lại mật khẩu vừa đặt cách đó một phút.
     */
    @Transactional
    public AuthResponse xacThucEmail(String email, String ma) {
        return issueTokens(xacThucEmailService.xacThuc(email, ma));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Để Spring Security kiểm tra mật khẩu, sai thì tự ném lỗi
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (DisabledException e) {
            /*
             * Spring Security chặn tài khoản chưa bật ngay tại bước này, trước khi code bên
             * dưới chạy. Phải bắt lại để phân biệt hai lý do rất khác nhau: tài khoản mới
             * đăng ký còn chờ xác nhận email, và tài khoản bị quản trị khóa.
             *
             * Gộp chung thành "tài khoản đã bị khóa" thì người vừa đăng ký xong đọc vào sẽ
             * tưởng mình bị cấm, trong khi chỉ cần mở hộp thư lấy mã.
             */
            throw new BusinessException(lyDoKhongDangNhapDuoc(request.email()));
        }

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ResourceNotFoundException.of("người dùng", request.email()));

        if (!user.isEnabled()) {
            throw new BusinessException(lyDoKhongDangNhapDuoc(request.email()));
        }
        return issueTokens(user);
    }

    /** Tài khoản còn mã chờ thì là chưa xác nhận email, còn lại mới là bị khóa. */
    private String lyDoKhongDangNhapDuoc(String email) {
        boolean choXacThuc = userRepository.findByEmailIgnoreCase(email)
                .map(u -> u.getVerificationCode() != null)
                .orElse(false);
        return choXacThuc
                ? "Tài khoản chưa xác nhận email. Mở hộp thư lấy mã, hoặc bấm gửi lại mã."
                : "Tài khoản đã bị khóa";
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("Refresh token không tồn tại"));

        if (!stored.isUsable()) {
            throw new BusinessException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
        }

        // Thu hồi token cũ ngay khi dùng để tránh bị dùng lại
        stored.setRevoked(true);
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(jwtProperties.refreshTokenDays()))
                .revoked(false)
                .build());

        return AuthResponse.of(accessToken, refreshToken,
                jwtService.accessTokenSeconds(), UserResponse.from(user));
    }
}
