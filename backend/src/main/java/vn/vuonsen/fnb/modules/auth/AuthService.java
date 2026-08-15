package vn.vuonsen.fnb.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.config.props.JwtProperties;
import vn.vuonsen.fnb.modules.auth.dto.AuthResponse;
import vn.vuonsen.fnb.modules.auth.dto.LoginRequest;
import vn.vuonsen.fnb.modules.auth.dto.RegisterRequest;
import vn.vuonsen.fnb.modules.auth.dto.UserResponse;
import vn.vuonsen.fnb.modules.user.Role;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;
import vn.vuonsen.fnb.security.JwtService;

import java.time.LocalDateTime;

// Đăng ký, đăng nhập và làm mới token
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Email này đã được đăng ký");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(request.email().toLowerCase().trim())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        return issueTokens(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Để Spring Security kiểm tra mật khẩu, sai thì tự ném lỗi
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ResourceNotFoundException.of("người dùng", request.email()));

        if (!user.isEnabled()) {
            throw new BusinessException("Tài khoản đã bị khóa");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("Refresh token không tồn tại"));

        if (!stored.isUsable()) {
            throw new BusinessException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
        }

        // Thu hồi token cũ ngay khi dùng để tránh bị dùng lại
        stored.setRevoked(true);
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(jwtProperties.refreshTokenDays()))
                .revoked(false)
                .build());

        return AuthResponse.of(accessToken, refreshToken,
                jwtService.accessTokenSeconds(), UserResponse.from(user));
    }
}
