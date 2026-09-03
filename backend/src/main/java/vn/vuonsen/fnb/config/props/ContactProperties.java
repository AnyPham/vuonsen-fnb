package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Thông tin liên hệ của nhà hàng, trợ lý dùng để trả lời khách hỏi địa chỉ và giờ mở cửa
@ConfigurationProperties(prefix = "app.contact")
public record ContactProperties(
        String address,
        String phone,
        String email,
        String openingHours
) {
}
