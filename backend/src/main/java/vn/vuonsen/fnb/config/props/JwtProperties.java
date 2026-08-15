package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Đọc cấu hình JWT từ mục app.jwt trong application.yml
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenMinutes,
        long refreshTokenDays,
        String issuer
) {
}
