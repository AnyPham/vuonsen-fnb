package vn.vuonsen.fnb.modules.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.auth.dto.UserResponse;
import vn.vuonsen.fnb.security.AppUserDetails;

// Thông tin cá nhân của người đang đăng nhập
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "2. Ho so ca nhan")
public class UserController {

    private final UserRepository userRepository;

    public record UpdateProfileRequest(
            @Size(min = 2, max = 120, message = "Họ tên từ 2 đến 120 ký tự") String fullName,
            @Pattern(regexp = "^0[0-9]{8,10}$", message = "Số điện thoại không hợp lệ") String phone,
            @Size(max = 255) String address
    ) {
    }

    @GetMapping
    @Operation(summary = "Thong tin tai khoan dang dang nhap")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AppUserDetails principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.of("người dùng", principal.getUserId()));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Cap nhat ho so ca nhan")
    public ResponseEntity<UserResponse> update(@AuthenticationPrincipal AppUserDetails principal,
                                               @Valid @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.of("người dùng", principal.getUserId()));

        if (request.fullName() != null) user.setFullName(request.fullName().trim());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.address() != null) user.setAddress(request.address());

        return ResponseEntity.ok(UserResponse.from(userRepository.save(user)));
    }
}
