package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
 * đặt ba biến môi trường ở trên với host là smtp.gmail.com. Không ghi mật khẩu vào tệp cấu
 * hình vì tệp đó nằm trong kho mã nguồn.
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        String from,
        String baseUrl
) {
}
