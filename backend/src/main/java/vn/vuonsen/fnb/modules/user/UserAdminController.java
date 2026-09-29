package vn.vuonsen.fnb.modules.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.dto.PageResponse;
import vn.vuonsen.fnb.security.AppUserDetails;

/*
 * Quản lý tài khoản người dùng, dành riêng cho quản trị.
 *
 * Trước đây muốn có một tài khoản nhân viên thì phải sửa thẳng cột role trong cơ sở dữ liệu,
 * vì trang đăng ký chỉ tạo ra tài khoản khách hàng. Cách đó vừa bất tiện vừa nguy hiểm: mở
 * công cụ quản trị cơ sở dữ liệu ra là có quyền sửa mọi thứ.
 *
 * Chỉ ADMIN vào được, STAFF không tự cấp quyền cho ai được.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "21. Quản trị - Tài khoản")
public class UserAdminController {

    private final UserAdminService service;

    /** Tạo tài khoản cho nhân viên hoặc cho quản trị viên mới. */
    public record TaoTaiKhoanRequest(
            @NotBlank(message = "Vui lòng nhập họ tên")
            @Size(max = 120) String fullName,

            @NotBlank(message = "Vui lòng nhập email")
            @Email(message = "Email không đúng định dạng")
            @Size(max = 160) String email,

            @Pattern(regexp = "^$|^[0-9\\s.+()-]{9,15}$", message = "Số điện thoại không hợp lệ")
            String phone,

            @NotBlank(message = "Vui lòng nhập mật khẩu")
            @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
            String password,

            @Pattern(regexp = "STAFF|ADMIN", message = "Vai trò chỉ nhận STAFF hoặc ADMIN")
            String role
    ) {
    }

    @GetMapping
    @Operation(summary = "Danh sách tài khoản, lọc theo vai trò và tìm theo tên hoặc email")
    public ResponseEntity<PageResponse<UserAdminService.TaiKhoanResponse>> danhSach(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var kq = service.danhSach(role, keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(kq, u -> u));
    }

    @PostMapping
    @Operation(summary = "Tạo tài khoản nhân viên hoặc quản trị")
    public ResponseEntity<UserAdminService.TaiKhoanResponse> tao(
            @Valid @RequestBody TaoTaiKhoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.tao(request));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Đổi vai trò của một tài khoản")
    public ResponseEntity<UserAdminService.TaiKhoanResponse> doiVaiTro(
            @PathVariable Long id,
            @RequestParam Role role,
            @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(service.doiVaiTro(id, role, principal.getUserId()));
    }

    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Khóa hoặc mở khóa một tài khoản")
    public ResponseEntity<UserAdminService.TaiKhoanResponse> doiTrangThai(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(service.doiTrangThai(id, enabled, principal.getUserId()));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Đặt lại mật khẩu hộ khách khi khách không tự lấy lại được")
    public ResponseEntity<Void> datLaiMatKhau(@PathVariable Long id,
                                               @RequestParam String newPassword) {
        service.datLaiMatKhau(id, newPassword);
        return ResponseEntity.noContent().build();
    }
}
