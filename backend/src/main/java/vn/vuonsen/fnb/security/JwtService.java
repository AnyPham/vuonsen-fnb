package vn.vuonsen.fnb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vuonsen.fnb.config.props.JwtProperties;
import vn.vuonsen.fnb.modules.user.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

// Tạo và kiểm tra JWT bằng thuật toán HS256
@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_TYPE = "typ";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(properties.issuer())
                .claims(Map.of(
                        CLAIM_USER_ID, user.getId(),
                        CLAIM_NAME, user.getFullName(),
                        CLAIM_ROLE, user.getRole().name(),
                        CLAIM_TYPE, "access"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(properties.issuer())
                .claim(CLAIM_TYPE, "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.refreshTokenDays(), ChronoUnit.DAYS)))
                .signWith(key)
                .compact();
    }

    // Trả về claims nếu token hợp lệ, null nếu sai chữ ký hoặc hết hạn
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token không hợp lệ: {}", ex.getMessage());
            return null;
        }
    }

    public String extractEmail(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.getSubject();
    }

    public long accessTokenSeconds() {
        return properties.accessTokenMinutes() * 60;
    }
}
