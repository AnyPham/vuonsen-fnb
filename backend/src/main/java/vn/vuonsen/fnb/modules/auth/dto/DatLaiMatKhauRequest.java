package vn.vuonsen.fnb.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Khách mở liên kết trong email rồi đặt mật khẩu mới
public record DatLaiMatKhauRequest(
        @NotBlank(message = "Thiếu mã đặt lại mật khẩu") String token,

        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
        String newPassword
) {
}
