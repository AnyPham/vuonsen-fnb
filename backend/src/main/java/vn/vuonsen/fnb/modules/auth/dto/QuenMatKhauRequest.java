package vn.vuonsen.fnb.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Khách nhập email để xin liên kết đặt lại mật khẩu
public record QuenMatKhauRequest(
        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không đúng định dạng")
        String email
) {
}
