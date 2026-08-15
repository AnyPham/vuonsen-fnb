package vn.vuonsen.fnb.modules.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.vuonsen.fnb.modules.booking.BookingStatus;

public record StatusUpdateRequest(
        @NotNull(message = "Thiếu trạng thái đích") BookingStatus status,
        @Size(max = 500) String note
) {
}
