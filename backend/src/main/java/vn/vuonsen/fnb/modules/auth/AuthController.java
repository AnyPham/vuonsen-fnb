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
@Tag(name = "1. Xac thuc", description = "Dang ky, dang nhap, lam moi token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Dang ky tai khoan khach hang")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Dang nhap, tra ve access token + refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Doi refresh token lay access token moi")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Dang xuat - thu hoi toan bo refresh token cua tai khoan")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AppUserDetails principal) {
        if (principal != null) {
            authService.logout(principal.getUserId());
        }
        return ResponseEntity.noContent().build();
    }
}
