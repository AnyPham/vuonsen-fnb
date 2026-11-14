package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Locale;

/*
 * Thư đi của nhà hàng.
 *
 * Ở đây chỉ có hai thứ của riêng ứng dụng: địa chỉ người gửi và địa chỉ website dùng để dựng
 * liên kết trong thư. Thông tin máy chủ thư nằm ở phần cấu hình chuẩn của Spring, khai bằng
 * biến môi trường SPRING_MAIL_HOST, SPRING_MAIL_USERNAME và SPRING_MAIL_PASSWORD.
 *
 * Không khai máy chủ thư thì Spring không dựng bộ gửi thư, và hệ thống tự hiểu là chạy ở chế
 * độ không gửi thật: nội dung thư chỉ ghi vào nhật ký. Nhờ vậy tải mã nguồn về là chạy được
 * ngay, không bị chặn ở bước phải có tài khoản email.
 *
 * Gửi thật bằng Gmail: bật xác thực hai bước cho tài khoản, tạo một "mật khẩu ứng dụng", rồi
 * khai các dòng spring.mail.* trong backend/tro-ly-ai.properties (xem tệp .example). Tệp đó
 * nằm trong .gitignore; không ghi mật khẩu vào application.yml vì tệp này nằm trong kho mã nguồn.
 *
 * mienChiGhi là các tên miền chỉ dùng cho kiểm thử. Thư gửi tới đó luôn chỉ ghi nhật ký kể cả
 * khi đã bật máy chủ thư: bộ Selenium đăng ký hàng trăm tài khoản @vuonsen.vn, test backend
 * dùng @example.com, gửi thật tới các địa chỉ không tồn tại đó thì Gmail coi là thư rác và
 * khóa tài khoản gửi.
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        String from,
        String baseUrl,
        List<String> mienChiGhi
) {
    public MailProperties {
        mienChiGhi = mienChiGhi == null ? List.of() : List.copyOf(mienChiGhi);
    }

    /** Địa chỉ thuộc tên miền kiểm thử thì không gửi ra ngoài. */
    public boolean chiGhi(String diaChi) {
        int a = diaChi == null ? -1 : diaChi.lastIndexOf('@');
        if (a < 0) {
            return true;
        }
        String mien = diaChi.substring(a + 1).trim().toLowerCase(Locale.ROOT);
        return mienChiGhi.stream().anyMatch(m -> mien.equals(m) || mien.endsWith("." + m));
    }
}
