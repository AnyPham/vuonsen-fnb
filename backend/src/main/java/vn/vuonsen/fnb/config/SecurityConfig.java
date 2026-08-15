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
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))                  // cho phép mở H2 console
                .authorizeHttpRequests(auth -> auth
                        // Swagger và H2 console
                        .requestMatchers("/api/auth/**", "/h2-console/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/actuator/health").permitAll()

                        // Nội dung công khai, ai xem cũng được
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/spaces/**", "/api/v1/menu/**", "/api/v1/packages/**",
                                "/api/v1/reviews/**", "/api/v1/gallery/**").permitAll()

                        // khách chưa đăng nhập vẫn đặt tiệc được
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings", "/api/v1/bookings/quote").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/track/**").permitAll()

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
