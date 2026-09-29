package vn.vuonsen.fnb.modules.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.config.props.MailProperties;
import vn.vuonsen.fnb.modules.notification.EmailService;

import java.time.LocalDateTime;

/*
 * Quản trị tài khoản người dùng.
 *
 * Hai quy tắc tự bảo vệ, đặt ra để tránh tình huống dở khóc dở cười là quản trị viên duy
 * nhất tự khóa mình hoặc tự hạ quyền mình rồi không ai vào được khu quản trị nữa:
 *
 *   - Không tự đổi vai trò của chính mình
 *   - Không tự khóa tài khoản của chính mình
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final MailProperties mailProperties;

    public record TaiKhoanResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            String role,
            boolean enabled,
            LocalDateTime createdAt
    ) {
        static TaiKhoanResponse from(User u) {
            return new TaiKhoanResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                    u.getRole().name(), u.isEnabled(), u.getCreatedAt());
        }
    }

    public Page<TaiKhoanResponse> danhSach(Role role, String keyword, Pageable pageable) {
        String tuKhoa = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return userRepository.search(role, tuKhoa, pageable).map(TaiKhoanResponse::from);
    }

    @Transactional
    public TaiKhoanResponse tao(UserAdminController.TaoTaiKhoanRequest req) {
        String email = req.email().trim();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BusinessException("Email này đã có tài khoản");
        }

        Role vaiTro = req.role() == null || req.role().isBlank() ? Role.STAFF : Role.valueOf(req.role());

        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPhone(req.phone() == null || req.phone().isBlank() ? null : req.phone().trim());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(vaiTro);
        u.setEnabled(true);

        User daLuu = userRepository.save(u);
        log.info("Đã tạo tài khoản {} với vai trò {}", daLuu.getEmail(), vaiTro);

        /*
         * Báo cho người được cấp tài khoản biết, kèm lời nhắc đổi mật khẩu.
         *
         * Cố ý không gửi mật khẩu trong thư: mật khẩu do quản trị đặt thì quản trị tự báo
         * trực tiếp, gửi qua email là để lại mật khẩu nằm mãi trong hộp thư.
         */
        emailService.gui(daLuu.getEmail(), "Tài khoản Vườn Sen đã được tạo cho bạn", """
                Chào %s,

                Quản trị viên vừa tạo cho bạn một tài khoản trên hệ thống Vườn Sen với vai trò %s.

                Email đăng nhập: %s
                Mật khẩu: do quản trị viên cấp trực tiếp cho bạn, thư này không gửi kèm.

                Đăng nhập tại: %s/dang-nhap
                Nên đổi mật khẩu ngay sau lần đăng nhập đầu tiên.

                Vườn Sen
                """.formatted(daLuu.getFullName(), vaiTro.name(), daLuu.getEmail(),
                        mailProperties.baseUrl()), "TAI_KHOAN_MOI");

        return TaiKhoanResponse.from(daLuu);
    }

    @Transactional
    public TaiKhoanResponse doiVaiTro(Long id, Role vaiTroMoi, Long nguoiThucHien) {
        if (id.equals(nguoiThucHien)) {
            throw new BusinessException("Không tự đổi vai trò của chính mình được. "
                    + "Nhờ một quản trị viên khác làm giúp.");
        }
        User u = tim(id);
        u.setRole(vaiTroMoi);
        log.info("Tài khoản {} đổi sang vai trò {}", u.getEmail(), vaiTroMoi);
        return TaiKhoanResponse.from(u);
    }

    @Transactional
    public TaiKhoanResponse doiTrangThai(Long id, boolean moKhoa, Long nguoiThucHien) {
        if (id.equals(nguoiThucHien)) {
            throw new BusinessException("Không tự khóa tài khoản của chính mình được");
        }
        User u = tim(id);
        u.setEnabled(moKhoa);
        log.info("Tài khoản {} chuyển sang trạng thái {}", u.getEmail(), moKhoa ? "mở" : "khóa");
        return TaiKhoanResponse.from(u);
    }

    @Transactional
    public void datLaiMatKhau(Long id, String matKhauMoi) {
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new BusinessException("Mật khẩu phải từ 6 ký tự trở lên");
        }
        User u = tim(id);
        u.setPasswordHash(passwordEncoder.encode(matKhauMoi));
        log.info("Quản trị đặt lại mật khẩu cho tài khoản {}", u.getEmail());

        emailService.gui(u.getEmail(), "Mật khẩu Vườn Sen của bạn vừa được đặt lại", """
                Chào %s,

                Quản trị viên vừa đặt lại mật khẩu tài khoản của bạn theo yêu cầu. Mật khẩu mới
                sẽ được báo trực tiếp cho bạn, thư này không gửi kèm.

                Nếu không phải bạn yêu cầu, hãy liên hệ nhà hàng ngay.

                Vườn Sen
                """.formatted(u.getFullName()), "DAT_LAI_MAT_KHAU");
    }

    private User tim(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("tài khoản", id));
    }
}
