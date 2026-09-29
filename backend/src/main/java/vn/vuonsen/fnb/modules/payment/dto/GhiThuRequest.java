package vn.vuonsen.fnb.modules.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Nhân viên ghi nhận một khoản vừa thu tại quầy hoặc vừa thấy trong sao kê
public record GhiThuRequest(
        @NotBlank(message = "Vui lòng nhập mã đơn") String orderCode,

        @NotNull(message = "Thiếu số tiền")
        @DecimalMin(value = "1000", message = "Số tiền phải từ 1.000đ trở lên")
        BigDecimal amount,

        @Pattern(regexp = "DEPOSIT|BALANCE|FULL", message = "Mục đích không hợp lệ")
        String purpose,

        @Pattern(regexp = "CASH|TRANSFER|VIETQR|COD", message = "Hình thức không hợp lệ")
        String method,

        @Size(max = 500) String note
) {
}
