package vn.vuonsen.fnb.modules.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/* Một món khách tự chọn, kèm số phần. */
public record MonTuChonRequest(
        @NotNull(message = "Thiếu mã món") Long dishId,
        @NotNull(message = "Thiếu số phần")
        @Min(value = 1, message = "Số phần phải lớn hơn 0")
        @Max(value = 200, message = "Mỗi món tối đa 200 phần") Integer soLuong) {
}
