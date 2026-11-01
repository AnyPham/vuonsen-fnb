package vn.vuonsen.fnb.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.vuonsen.fnb.modules.auth.LuatMatKhau;

// Khách mở liên kết trong email rồi đặt mật khẩu mới
public record DatLaiMatKhauRequest(
        @NotBlank(message = "Thiếu mã đặt lại mật khẩu") String token,

        /*
         * Dùng chung luật với lúc đăng ký. Nới lỏng ở đây thì khách đặt mật khẩu yếu được
         * bằng đường quên mật khẩu, luật ở trang đăng ký thành ra vô nghĩa.
         */
        @NotBlank(message = "Vui lòng nhập mật khẩu mới")
        @Size(max = LuatMatKhau.DAI_TOI_DA, message = LuatMatKhau.THONG_BAO)
        @Pattern(regexp = LuatMatKhau.BIEU_THUC, message = LuatMatKhau.THONG_BAO)
        String newPassword
) {
}
