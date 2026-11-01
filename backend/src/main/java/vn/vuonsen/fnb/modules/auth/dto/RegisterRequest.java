package vn.vuonsen.fnb.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.vuonsen.fnb.modules.auth.LuatMatKhau;

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
        @Size(max = LuatMatKhau.DAI_TOI_DA, message = LuatMatKhau.THONG_BAO)
        @Pattern(regexp = LuatMatKhau.BIEU_THUC, message = LuatMatKhau.THONG_BAO)
        String password
) {
}
