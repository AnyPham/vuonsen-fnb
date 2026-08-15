package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// Danh sách địa chỉ frontend được phép gọi API
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
