package vn.vuonsen.fnb.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Khách nhập mã nhận được trong email để kích hoạt tài khoản
public record XacThucEmailRequest(
        @NotBlank(message = "Thiếu email cần xác nhận")
        @Email(message = "Email không hợp lệ")
        String email,

        /*
         * Chặn ngay ở đây những chuỗi không thể là mã, để không phải đụng tới cơ sở dữ liệu
         * và cũng không tính vào số lần nhập sai của khách.
         */
        @NotBlank(message = "Vui lòng nhập mã xác nhận")
        @Pattern(regexp = "^[0-9]{6}$", message = "Mã xác nhận gồm 6 chữ số")
        String code
) {
}
