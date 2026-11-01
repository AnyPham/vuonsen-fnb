package vn.vuonsen.fnb.modules.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.auth.dto.LoginRequest;
import vn.vuonsen.fnb.modules.auth.dto.RegisterRequest;
import vn.vuonsen.fnb.modules.notification.EmailLogRepository;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Luồng xác nhận email khi đăng ký.
 *
 * Mã xác nhận đọc thẳng từ bảng users thay vì bóc từ nội dung thư, vì bài này kiểm luồng
 * nghiệp vụ chứ không kiểm cách hành văn của thư. Nội dung thư được kiểm riêng ở bài
 * guiMaVeEmail bên dưới.
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Xác nhận email khi đăng ký")
class XacThucEmailTest {

    private static final AtomicInteger DEM = new AtomicInteger();

    @Autowired
    private AuthService authService;
    @Autowired
    private XacThucEmailService xacThucEmailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailLogRepository emailLogRepository;

    private String emailMoi() {
        return "xacthuc." + System.nanoTime() + DEM.incrementAndGet() + "@example.com";
    }

    private User dangKy(String email) {
        authService.register(new RegisterRequest("Khách Mới", email, "0901234567", "Matkhau123"));
        return userRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    @Test
    @DisplayName("Đăng ký xong tài khoản chưa bật và chưa được cấp token")
    void dangKyXongChuaDungDuoc() {
        String email = emailMoi();

        var kq = authService.register(
                new RegisterRequest("Khách Mới", email, "0901234567", "Matkhau123"));

        assertThat(kq.canXacThuc()).isTrue();
        assertThat(kq.email()).isEqualTo(email);

        User u = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(u.isEnabled()).isFalse();
        assertThat(u.getVerificationCode()).hasSize(6).containsOnlyDigits();
    }

    @Test
    @DisplayName("Mã được gửi về đúng email, nội dung có mã và hạn dùng")
    void guiMaVeEmail() {
        String email = emailMoi();
        dangKy(email);

        var thu = emailLogRepository.findAll().stream()
                .filter(l -> "XAC_THUC_EMAIL".equals(l.getKind()))
                .filter(l -> email.equals(l.getRecipient()))
                .reduce((a, b) -> b)
                .orElseThrow(() -> new AssertionError("Không thấy thư xác nhận nào được dựng"));

        String ma = userRepository.findByEmailIgnoreCase(email).orElseThrow().getVerificationCode();
        assertThat(thu.getBody()).contains(ma).contains("15 phút");
    }

    @Test
    @DisplayName("Chưa xác nhận thì không đăng nhập được, và báo đúng lý do")
    void chuaXacNhanThiKhongDangNhapDuoc() {
        String email = emailMoi();
        dangKy(email);

        assertThatThrownBy(() -> authService.login(new LoginRequest(email, "Matkhau123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chưa xác nhận email")
                // Không được nhầm sang câu dành cho tài khoản bị quản trị khóa
                .hasMessageNotContaining("đã bị khóa");
    }

    @Test
    @DisplayName("Nhập đúng mã thì tài khoản bật lên, xóa mã và cấp token luôn")
    void nhapDungMaThiKichHoat() {
        String email = emailMoi();
        User u = dangKy(email);

        var kq = authService.xacThucEmail(email, u.getVerificationCode());

        assertThat(kq.accessToken()).isNotBlank();
        User sau = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(sau.isEnabled()).isTrue();
        // Mã đã dùng phải xóa đi, không để chuỗi bí mật nằm lại trong cơ sở dữ liệu
        assertThat(sau.getVerificationCode()).isNull();
        assertThat(sau.getVerificationExpiresAt()).isNull();
    }

    @Test
    @DisplayName("Xác nhận xong thì đăng nhập được bình thường")
    void xacNhanXongDangNhapDuoc() {
        String email = emailMoi();
        User u = dangKy(email);
        authService.xacThucEmail(email, u.getVerificationCode());

        var kq = authService.login(new LoginRequest(email, "Matkhau123"));

        assertThat(kq.accessToken()).isNotBlank();
        assertThat(kq.user().email()).isEqualTo(email);
    }

    @Test
    @DisplayName("Nhập sai mã thì báo còn mấy lần thử, tài khoản vẫn tắt")
    void nhapSaiMaThiConDemLanThu() {
        String email = emailMoi();
        dangKy(email);

        assertThatThrownBy(() -> authService.xacThucEmail(email, "000000"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không đúng")
                .hasMessageContaining("lần thử");

        assertThat(userRepository.findByEmailIgnoreCase(email).orElseThrow().isEnabled()).isFalse();
    }

    /*
     * Sáu chữ số chỉ có một triệu khả năng, máy dò hết trong vài phút. Phải có chốt chặn
     * số lần nhập sai, nếu không thì hạn 15 phút cũng không cứu được.
     */
    @Test
    @DisplayName("Sai quá năm lần thì mã bị hủy, phải xin mã mới")
    void saiQuaNhieuLanThiHuyMa() {
        String email = emailMoi();
        dangKy(email);

        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> authService.xacThucEmail(email, "000000"))
                    .hasMessageContaining("không đúng");
        }
        assertThatThrownBy(() -> authService.xacThucEmail(email, "000000"))
                .hasMessageContaining("gửi lại mã");

        assertThat(userRepository.findByEmailIgnoreCase(email).orElseThrow()
                .getVerificationCode()).isNull();
    }

    @Test
    @DisplayName("Gửi lại mã thì cấp mã mới, mã cũ hết giá trị")
    void guiLaiMaThiDoiMaMoi() {
        String email = emailMoi();
        String maCu = dangKy(email).getVerificationCode();

        xacThucEmailService.guiLaiMa(email);

        String maMoi = userRepository.findByEmailIgnoreCase(email).orElseThrow().getVerificationCode();
        assertThat(maMoi).isNotEqualTo(maCu);
        assertThatThrownBy(() -> authService.xacThucEmail(email, maCu))
                .hasMessageContaining("không đúng");
    }

    /*
     * Email lạ hoặc đã xác thực rồi thì không được báo khác nhau, nếu không người ngoài gõ
     * thử từng địa chỉ là dò ra email nào có tài khoản ở đây.
     */
    @Test
    @DisplayName("Xin gửi lại mã cho email không chờ xác thực thì im lặng bỏ qua")
    void guiLaiMaChoEmailLaThiImLang() {
        xacThucEmailService.guiLaiMa("khong-ton-tai-" + System.nanoTime() + "@example.com");

        String email = emailMoi();
        User u = dangKy(email);
        authService.xacThucEmail(email, u.getVerificationCode());
        xacThucEmailService.guiLaiMa(email);

        // Đã xác thực rồi thì không cấp mã mới, tài khoản vẫn dùng bình thường
        assertThat(userRepository.findByEmailIgnoreCase(email).orElseThrow()
                .getVerificationCode()).isNull();
    }
}
