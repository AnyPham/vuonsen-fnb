package vn.vuonsen.fnb.modules.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.config.props.MailProperties;

/*
 * Gửi thư cho khách.
 *
 * Website phải chạy được ngay cả khi chưa có tài khoản máy chủ thư, vì đó là thứ phải đăng ký
 * bên ngoài. Nên dịch vụ này có hai chế độ:
 *
 *   - Đã khai báo máy chủ thư: gửi thật, rồi ghi lại vào nhật ký.
 *   - Chưa khai báo: không gửi đi đâu cả, chỉ ghi toàn văn nội dung vào nhật ký và in ra
 *     màn hình. Luồng nghiệp vụ vẫn chạy trọn vẹn, lập trình viên vẫn đọc được liên kết đặt
 *     lại mật khẩu để thử, mà không cần tài khoản nào.
 *
 * Gửi thư hỏng thì chỉ ghi nhận rồi đi tiếp, không ném lỗi ra ngoài. Đơn đặt tiệc đã lưu
 * thành công rồi thì không có lý do gì báo lỗi cho khách chỉ vì máy chủ thư trục trặc.
 */
@Slf4j
@Service
public class EmailService {

    private static final String DA_GUI = "SENT";
    private static final String CHI_GHI = "LOGGED";
    private static final String HONG = "FAILED";

    private final ObjectProvider<JavaMailSender> mailSender;
    private final EmailLogRepository logRepository;
    private final MailProperties properties;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        EmailLogRepository logRepository,
                        MailProperties properties) {
        this.mailSender = mailSender;
        this.logRepository = logRepository;
        this.properties = properties;
    }

    @Transactional
    public void gui(String nguoiNhan, String tieuDe, String noiDung, String loai) {
        guiHtml(nguoiNhan, tieuDe, noiDung, null, loai);
    }

    /*
     * Gửi thư có hai bản: chữ trơn và HTML. Hộp thư nào hiển thị được HTML (Gmail, Outlook)
     * thì hiện bản HTML, còn lại hiện bản chữ trơn. Nhật ký luôn ghi bản chữ trơn để đọc được
     * ngay trong cơ sở dữ liệu. html để null thì gửi thư chữ trơn như cũ.
     */
    @Transactional
    public void guiHtml(String nguoiNhan, String tieuDe, String noiDung, String html, String loai) {
        if (nguoiNhan == null || nguoiNhan.isBlank()) {
            return;
        }

        /*
         * Không khai báo máy chủ thư thì Spring không dựng bộ gửi thư. Lấy ngay điều đó làm
         * dấu hiệu để chuyển sang chế độ chỉ ghi nhật ký, khỏi phải khai thêm một cờ bật tắt
         * riêng rồi lại có nguy cơ hai chỗ khai lệch nhau.
         */
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null || properties.chiGhi(nguoiNhan)) {
            ghiNhat(nguoiNhan, tieuDe, noiDung, html, loai, CHI_GHI, null);
            log.info("{} nên không gửi đi. Nội dung thư gửi {}:\n--- {} ---\n{}",
                    sender == null ? "Chưa khai báo máy chủ thư" : "Địa chỉ thuộc tên miền kiểm thử",
                    nguoiNhan, tieuDe, noiDung);
            return;
        }

        try {
            /*
             * MimeMessageHelper thay cho SimpleMailMessage: mã hóa UTF-8 cho tiêu đề và tên
             * người gửi có dấu ("Vườn Sen"), và ghép được hai bản chữ trơn và HTML vào một thư.
             */
            MimeMessage thu = sender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(thu, html != null, "UTF-8");
            h.setFrom(properties.from());
            h.setTo(nguoiNhan);
            h.setSubject(tieuDe);
            if (html == null) {
                h.setText(noiDung);
            } else {
                h.setText(noiDung, html);
            }
            sender.send(thu);

            ghiNhat(nguoiNhan, tieuDe, noiDung, html, loai, DA_GUI, null);
            log.info("Đã gửi thư {} tới {}", loai, nguoiNhan);
        } catch (Exception e) {
            // Gửi hỏng không được làm hỏng nghiệp vụ đang chạy, chỉ ghi lại để còn tra
            log.warn("Gửi thư tới {} không thành công: {}", nguoiNhan, e.getMessage());
            ghiNhat(nguoiNhan, tieuDe, noiDung, html, loai, HONG, cat(e.getMessage()));
        }
    }

    private void ghiNhat(String nguoiNhan, String tieuDe, String noiDung, String html,
                         String loai, String trangThai, String loi) {
        logRepository.save(EmailLog.builder()
                .recipient(nguoiNhan)
                .subject(tieuDe)
                .body(noiDung)
                .htmlBody(html)
                .kind(loai)
                .status(trangThai)
                .errorDetail(loi)
                .build());
    }

    private String cat(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}
