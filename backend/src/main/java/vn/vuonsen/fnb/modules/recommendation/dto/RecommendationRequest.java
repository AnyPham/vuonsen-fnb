package vn.vuonsen.fnb.modules.recommendation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import vn.vuonsen.fnb.modules.booking.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;

// Nhu cầu khách khai để hệ thống gợi ý. Chỉ số khách là bắt buộc.
public record RecommendationRequest(
        @NotNull(message = "Vui lòng nhập số khách")
        @Min(value = 1, message = "Số khách phải lớn hơn 0") Integer guestCount,
        EventType eventType,
        BigDecimal budget,
        LocalDate eventDate
) {
}
