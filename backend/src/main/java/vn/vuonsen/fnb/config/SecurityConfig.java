package vn.vuonsen.fnb.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.vuonsen.fnb.config.props.CorsProperties;
import vn.vuonsen.fnb.modules.user.Role;
import vn.vuonsen.fnb.security.JwtAuthenticationFilter;

import java.util.List;

// Cấu hình bảo mật: dùng JWT, không dùng session
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // bật @PreAuthorize ở controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer -> AbstractHttpConfigurer.disable())   // API không dùng session nên tắt CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))                  // Swagger UI nhúng khung cùng nguồn
                .authorizeHttpRequests(auth -> auth
                        // Đăng nhập, tài liệu API và điểm kiểm tra sống
                        .requestMatchers("/api/auth/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/actuator/health").permitAll()

                        // Nội dung công khai, ai xem cũng được
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/spaces/**", "/api/v1/menu/**", "/api/v1/packages/**",
                                "/api/v1/reviews/**", "/api/v1/gallery/**").permitAll()

                        /*
                         * Báo giá, gợi ý và hỏi trợ lý vẫn mở cho khách chưa đăng nhập.
                         *
                         * Ba đường này chỉ đọc và tính toán, không tạo ra bản ghi nào. Khách
                         * phải xem được giá trước khi quyết định có lập tài khoản hay không;
                         * bắt đăng nhập mới cho xem giá thì mất khách ngay từ bước tìm hiểu.
                         *
                         * Riêng POST /api/v1/bookings đã chuyển xuống nhóm bắt buộc đăng nhập.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/quote",
                                "/api/v1/recommendations", "/api/v1/assistant/ask").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/options",
                                "/api/v1/bookings/track/**",
                                // Xem buổi nào còn trống và có ưu đãi gì, không cần đăng nhập
                                "/api/v1/bookings/availability",
                                "/api/v1/holidays",
                                /*
                                 * Màn mở đầu hộp thoại trợ lý. Mở cho cả khách chưa đăng nhập,
                                 * khi đó trả bản dành cho khách mới. Khách đã đăng nhập thì bộ
                                 * lọc JWT đã gắn danh tính vào ngữ cảnh trước khi tới đây, nên
                                 * controller vẫn đọc được principal và trả bản cá nhân hóa.
                                 */
                                "/api/v1/assistant/loi-mo-dau").permitAll()
                        /* Khách vãng lai cũng đăng ký nhận khuyến mãi được, không bắt tạo tài khoản. */
                        .requestMatchers(HttpMethod.GET, "/api/v1/push/khoa-cong-khai").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/push/dang-ky").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/push/dang-ky").permitAll()

                        // Gửi đánh giá không bắt đăng nhập: đã phải nhập mã đơn có thật, và
                        // đánh giá còn phải qua bước quản trị duyệt mới hiện công khai
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").permitAll()

                        // Tính tiền giỏ món thì mở, còn gửi đơn phải đăng nhập
                        .requestMatchers(HttpMethod.POST, "/api/v1/dish-orders/quote").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/dish-orders/track/**").permitAll()

                        /*
                         * Gửi đơn thì bắt buộc đăng nhập.
                         *
                         * Trước đây khách vãng lai đặt được, nên đơn không gắn tài khoản nào:
                         * trong cơ sở dữ liệu hiện có 70 đơn như vậy. Hệ quả là chính người
                         * đặt cũng không xem lại được đơn của mình ở mục Đơn của tôi, và
                         * không có cách nào xác minh địa chỉ email họ khai là thật.
                         *
                         * Đặt ngay trước quy tắc chung để không bị dòng permitAll nào ở trên
                         * cướp mất; thứ tự trong chuỗi này quyết định quy tắc nào thắng.
                         */
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings", "/api/v1/dish-orders")
                        .authenticated()

                        // Thanh toán không bắt đăng nhập, giống tra cứu đơn: có mã đơn là đủ.
                        // Hai đường dẫn này chỉ đọc tình hình và tạo phiếu chờ đối soát,
                        // không tự cộng tiền vào đơn nên biết mã đơn cũng không gian lận được.
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/order/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/sandbox/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/vnpay", "/api/v1/payments/vnpay/verify").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/vnpay/trang-thai", "/api/v1/payments/vnpay/ipn").permitAll()

                        // Khu vực quản trị
                        .requestMatchers("/api/v1/admin/**")
                        .hasAnyAuthority(Role.ADMIN.authority(), Role.STAFF.authority())

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // BCrypt băm mật khẩu một chiều, không lưu mật khẩu gốc
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
