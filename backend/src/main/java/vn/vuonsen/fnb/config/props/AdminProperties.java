package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Tài khoản admin tạo lần đầu
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String defaultEmail, String defaultPassword) {
}
