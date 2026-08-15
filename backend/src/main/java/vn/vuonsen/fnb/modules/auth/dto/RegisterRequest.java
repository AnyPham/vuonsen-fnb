package vn.vuonsen.fnb.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(min = 2, max = 120, message = "Họ tên từ 2 đến 120 ký tự")
        String fullName,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 160)
        String email,

        @Pattern(regexp = "^0[0-9]{8,10}$", message = "Số điện thoại không hợp lệ")
        String phone,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @Size(min = 6, max = 72, message = "Mật khẩu tối thiểu 6 ký tự")
        String password
) {
}
