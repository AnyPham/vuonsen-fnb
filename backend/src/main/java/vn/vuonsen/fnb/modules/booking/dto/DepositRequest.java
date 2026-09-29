package vn.vuonsen.fnb.modules.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/*
 * Một lần thu tiền cọc do quản trị ghi nhận.
 *
 * Hình thức để trống thì coi như chuyển khoản, vì đó là cách đa số khách đang dùng.
 */
public record DepositRequest(
        @NotNull(message = "Thiếu số tiền cọc")
        @DecimalMin(value = "1000", message = "Số tiền cọc phải từ 1.000đ trở lên")
        BigDecimal amount,

        @Pattern(regexp = "CASH|TRANSFER", message = "Hình thức chỉ nhận CASH hoặc TRANSFER")
        String method
) {
}
