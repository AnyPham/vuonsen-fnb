package vn.vuonsen.fnb.modules.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.auth.dto.AuthResponse;
import vn.vuonsen.fnb.modules.auth.dto.LoginRequest;
import vn.vuonsen.fnb.modules.auth.dto.RefreshRequest;
import vn.vuonsen.fnb.modules.auth.dto.RegisterRequest;
import vn.vuonsen.fnb.security.AppUserDetails;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Xác thực", description = "Đăng ký, đăng nhập, làm mới token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản khách hàng")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập, trả về access token và refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Đổi refresh token lấy access token mới")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất, thu hồi toàn bộ refresh token của tài khoản")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AppUserDetails principal) {
        if (principal != null) {
            authService.logout(principal.getUserId());
        }
        return ResponseEntity.noContent().build();
    }
}
