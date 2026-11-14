package vn.vuonsen.fnb.modules.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.notification.EmailService;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/*
 * Cấp và kiểm mã xác nhận email khi đăng ký tài khoản.
 *
 * Vì sao cần: trước đây ai cũng đăng ký được bằng một địa chỉ bất kỳ, kể cả địa chỉ không
 * có thật hay của người khác. Hệ thống lại gửi hóa đơn và thư xác nhận đơn về chính địa
 * chỉ đó, nên một email sai là thư bay tới người lạ.
 *
 * Mã lưu thẳng trên bảng users thay vì dựng bảng riêng, vì mỗi tài khoản chỉ có nhiều nhất
 * một mã đang chờ: cấp mã mới thì mã cũ hết giá trị ngay.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XacThucEmailService {

    /*
     * Mã sáu chữ số, hạn 15 phút.
     *
     * Sáu chữ số là mức quen thuộc, gõ lại từ điện thoại không mệt. Khoảng sáu chữ số có
     * một triệu giá trị, cộng với hạn 15 phút và giới hạn số lần nhập sai, đủ chặn dò mò
     * cho mức rủi ro của việc xác thực email.
     */
    private static final int SO_CHU_SO = 6;
    private static final int PHUT_HIEU_LUC = 15;

    /*
     * Số lần nhập sai tối đa. Quá số này thì mã bị hủy, khách phải xin mã mới.
     *
     * Không có chốt này thì một triệu khả năng vẫn dò hết được bằng máy trong vài phút.
     */
    private static final int SO_LAN_SAI_TOI_DA = 5;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final HuyMaXacThuc huyMaXacThuc;

    /*
     * SecureRandom chứ không phải Random thường. Random thường sinh dãy đoán trước được
     * nếu biết hạt giống, mà đây là chuỗi dùng để chứng minh danh tính.
     */
    private final SecureRandom ngauNhien = new SecureRandom();

    /** Đếm số lần nhập sai trong bộ nhớ, đủ dùng vì mã chỉ sống 15 phút. */
    private final java.util.Map<Long, Integer> soLanSai = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional
    public void capMaVaGui(User user) {
        String ma = sinhMa();
        user.setVerificationCode(ma);
        user.setVerificationExpiresAt(LocalDateTime.now().plusMinutes(PHUT_HIEU_LUC));
        soLanSai.remove(user.getId());
        userRepository.save(user);

        emailService.guiHtml(user.getEmail(),
                "Mã xác nhận tài khoản Vườn Sen: " + ma,
                """
                Chào %s,

                Mã xác nhận tài khoản Vườn Sen của bạn là:

                    %s

                Mã có hiệu lực trong %d phút. Nhập mã này trên trang đăng ký để kích hoạt
                tài khoản.

                Nếu bạn không đăng ký tài khoản nào ở Vườn Sen thì bỏ qua thư này, tài
                khoản sẽ không được kích hoạt.

                Vườn Sen
                """.formatted(user.getFullName(), ma, PHUT_HIEU_LUC),
                thuMaHtml(user.getFullName(), ma),
                "XAC_THUC_EMAIL");

        log.info("Đã cấp mã xác nhận cho tài khoản {}", user.getEmail());
    }

    /*
     * Bản HTML của thư mã xác nhận: mã đặt trong ô chữ to, giãn cách để đọc và gõ lại không nhầm.
     * Mã cũng nằm luôn ở tiêu đề thư, nên trên điện thoại khách đọc được ngay ở thông báo mà
     * không cần mở thư. CSS đặt thẳng trên thẻ vì hộp thư bỏ phần lớn CSS khác.
     */
    private static String thuMaHtml(String ten, String ma) {
        String tenAnToan = ten == null ? "" : ten.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return """
                <!doctype html><html lang="vi"><head><meta charset="utf-8"></head>
                <body style="margin:0;padding:0;background:#f6f3ec;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f6f3ec;padding:24px 0;"><tr><td align="center">
                <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;background:#ffffff;border-radius:10px;overflow:hidden;font-family:Arial,Helvetica,sans-serif;color:#1f2937;">
                <tr><td style="background:#1f3a2e;padding:22px 28px;">
                <div style="font-size:22px;font-weight:bold;color:#c9a24d;letter-spacing:1px;">VƯỜN SEN</div>
                <div style="font-size:13px;color:#d1d5db;margin-top:4px;">Khu ẩm thực và tiệc sân vườn</div></td></tr>
                <tr><td style="padding:28px 28px 8px;">
                <div style="font-size:20px;font-weight:bold;color:#1f3a2e;">Xác nhận địa chỉ email</div>
                <p style="font-size:15px;line-height:1.6;margin:10px 0 0;">Chào %s, đây là mã xác nhận tài khoản Vườn Sen của bạn:</p></td></tr>
                <tr><td align="center" style="padding:18px 28px;">
                <div style="display:inline-block;background:#f6f3ec;border:1px dashed #c9a24d;border-radius:8px;padding:16px 28px;font-size:34px;font-weight:bold;letter-spacing:10px;color:#1f3a2e;font-family:'Courier New',monospace;">%s</div></td></tr>
                <tr><td style="padding:0 28px 8px;font-size:14px;line-height:1.6;">
                Mã có hiệu lực trong <b>%d phút</b>. Nhập mã này trên trang đăng ký để kích hoạt tài khoản. Nhập sai %d lần thì mã bị hủy, bấm gửi lại mã để nhận mã mới.</td></tr>
                <tr><td style="padding:18px 28px 24px;font-size:12px;line-height:1.6;color:#9ca3af;border-top:1px solid #eee;">
                Nếu bạn không đăng ký tài khoản nào ở Vườn Sen thì bỏ qua thư này, tài khoản sẽ không được kích hoạt. Thư được gửi tự động, vui lòng không trả lời.</td></tr>
                </table></td></tr></table></body></html>
                """.formatted(tenAnToan, ma, PHUT_HIEU_LUC, SO_LAN_SAI_TOI_DA);
    }

    /*
     * Kiểm mã rồi kích hoạt tài khoản.
     *
     * Thông báo lỗi cố ý tách bạch ba trường hợp — chưa đăng ký, mã hết hạn, mã sai — vì
     * ở bước này người dùng đã chứng minh được mình giữ địa chỉ email đó. Giấu nhẹm lý do
     * chỉ làm họ loay hoay, mà không giấu được gì thêm.
     */
    /*
     * noRollbackFor là chỗ quan trọng, không phải trang trí.
     *
     * Khi khách nhập sai lần thứ năm, hàm này hủy mã rồi ném lỗi báo phải xin mã mới. Để
     * @Transactional trần thì ngoại lệ làm cuộn ngược cả giao dịch, lệnh hủy mã bị xóa
     * theo, mã cũ vẫn còn nguyên và chốt chặn dò mã thành vô dụng. Bài kiểm thử
     * saiQuaNhieuLanThiHuyMa bắt đúng tình huống này.
     *
     * Các lỗi còn lại trong hàm đều ném trước khi ghi gì, nên không cuộn ngược cũng không
     * để lại thay đổi nào dở dang.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public User xacThuc(String email, String ma) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BusinessException("Email này chưa đăng ký tài khoản"));

        if (user.isEnabled()) {
            throw new BusinessException("Tài khoản đã được xác nhận, bạn đăng nhập bình thường");
        }
        if (!user.maXacThucConHan(LocalDateTime.now())) {
            throw new BusinessException("Mã đã hết hạn, vui lòng bấm gửi lại mã");
        }

        if (!user.getVerificationCode().equals(ma == null ? null : ma.trim())) {
            int lan = soLanSai.merge(user.getId(), 1, Integer::sum);
            if (lan >= SO_LAN_SAI_TOI_DA) {
                // Giao dịch riêng, để lệnh hủy không bị cuộn ngược theo ngoại lệ ngay dưới
                huyMaXacThuc.thucHien(user.getId());
                soLanSai.remove(user.getId());
                throw new BusinessException("Nhập sai quá nhiều lần, vui lòng bấm gửi lại mã");
            }
            throw new BusinessException("Mã xác nhận không đúng, còn %d lần thử"
                    .formatted(SO_LAN_SAI_TOI_DA - lan));
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationExpiresAt(null);
        soLanSai.remove(user.getId());
        log.info("Tài khoản {} đã xác nhận email", user.getEmail());
        return userRepository.save(user);
    }

    /*
     * Gửi lại mã.
     *
     * Email chưa đăng ký hoặc đã xác thực rồi thì KHÔNG báo lỗi, chỉ lặng lẽ không gửi gì.
     * Nếu báo khác nhau, người lạ gõ thử từng email là biết được email nào có tài khoản ở
     * đây. Khác với lúc nhập mã: ở đó họ đã cầm được mã trong hộp thư rồi.
     */
    @Transactional
    public void guiLaiMa(String email) {
        userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .filter(u -> !u.isEnabled())
                .ifPresentOrElse(this::capMaVaGui,
                        () -> log.info("Xin gửi lại mã cho email không chờ xác thực, bỏ qua"));
    }

    private String sinhMa() {
        int giaTri = ngauNhien.nextInt((int) Math.pow(10, SO_CHU_SO));
        return String.format("%0" + SO_CHU_SO + "d", giaTri);
    }
}
