package vn.vuonsen.fnb.modules.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Báo giá nhanh, chưa cần thông tin liên hệ
public record QuoteRequest(
        @NotNull(message = "Vui lòng chọn không gian") Long spaceId,
        // Để trống nghĩa là chỉ thuê không gian, không kèm gói tiệc
        Long packageId,
        @NotNull(message = "Vui lòng nhập số khách") @Min(value = 1, message = "Số khách phải lớn hơn 0") Integer guestCount,
        LocalDate eventDate,
        /* Món khách tự chọn thay cho gói tiệc. Để trống nghĩa là không dùng thực đơn tự chọn. */
        @jakarta.validation.Valid java.util.List<MonTuChonRequest> monTuChon
) {
}
