package vn.vuonsen.fnb.modules.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*
 * Khách bấm nút thanh toán cho một đơn.
 *
 * Mục đích để trống thì hiểu là đóng cọc, vì đó là việc khách làm ngay sau khi đặt tiệc.
 */
public record TaoThanhToanRequest(
        @NotBlank(message = "Vui lòng nhập mã đơn") String orderCode,

        @Pattern(regexp = "DEPOSIT|BALANCE|FULL",
                message = "Mục đích chỉ nhận DEPOSIT, BALANCE hoặc FULL")
        String purpose
) {
}
