package vn.vuonsen.fnb.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Khách không nhận được mã, hoặc mã đã hết hạn, xin cấp mã mới
public record GuiLaiMaRequest(
        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        String email
) {
}
