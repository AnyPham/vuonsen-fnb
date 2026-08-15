package vn.vuonsen.fnb.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.vuonsen.fnb.config.props.AdminProperties;
import vn.vuonsen.fnb.modules.user.Role;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

// Tạo tài khoản admin đầu tiên khi chạy lần đầu
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Bean
    public ApplicationRunner seedAdminAccount() {
        return args -> {
            String email = adminProperties.defaultEmail();
            if (userRepository.existsByEmailIgnoreCase(email)) {
                return;
            }
            User admin = User.builder()
                    .fullName("Quan tri he thong")
                    .email(email)
                    .passwordHash(passwordEncoder.encode(adminProperties.defaultPassword()))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Da tao tai khoan quan tri mac dinh: {}", email);
        };
    }
}
