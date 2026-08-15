package vn.vuonsen.fnb.modules.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Báo giá nhanh, chưa cần thông tin liên hệ
public record QuoteRequest(
        @NotNull(message = "Vui lòng chọn không gian") Long spaceId,
        @NotNull(message = "Vui lòng chọn gói tiệc") Long packageId,
        @NotNull(message = "Vui lòng nhập số khách") @Min(value = 1, message = "Số khách phải lớn hơn 0") Integer guestCount,
        LocalDate eventDate
) {
}
