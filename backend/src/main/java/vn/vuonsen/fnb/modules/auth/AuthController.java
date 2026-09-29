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
import vn.vuonsen.fnb.modules.auth.dto.DatLaiMatKhauRequest;
import vn.vuonsen.fnb.modules.auth.dto.LoginRequest;
import vn.vuonsen.fnb.modules.auth.dto.QuenMatKhauRequest;
import vn.vuonsen.fnb.modules.auth.dto.RefreshRequest;
import vn.vuonsen.fnb.modules.auth.dto.RegisterRequest;
import vn.vuonsen.fnb.security.AppUserDetails;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Xác thực", description = "Đăng ký, đăng nhập, làm mới token")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

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

    /*
     * Xin liên kết đặt lại mật khẩu.
     *
     * Luôn trả về không nội dung, kể cả khi email chưa đăng ký. Báo khác nhau giữa hai trường
     * hợp thì người ngoài dò được email nào đã có tài khoản trên hệ thống.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Gửi liên kết đặt lại mật khẩu vào email")
    public ResponseEntity<Void> quenMatKhau(@Valid @RequestBody QuenMatKhauRequest request) {
        passwordResetService.xinDatLai(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt mật khẩu mới bằng mã trong liên kết đã gửi qua email")
    public ResponseEntity<Void> datLaiMatKhau(@Valid @RequestBody DatLaiMatKhauRequest request) {
        passwordResetService.datLai(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
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
