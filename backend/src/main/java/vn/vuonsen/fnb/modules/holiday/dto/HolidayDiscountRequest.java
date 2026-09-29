package vn.vuonsen.fnb.modules.holiday.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// Dữ liệu thêm hoặc sửa một dịp lễ giảm giá từ trang quản trị
public record HolidayDiscountRequest(
        @NotBlank(message = "Vui lòng nhập tên dịp lễ")
        @Size(max = 120, message = "Tên dịp lễ tối đa 120 ký tự")
        String name,

        @NotNull(message = "Vui lòng chọn ngày bắt đầu")
        LocalDate startDate,

        @NotNull(message = "Vui lòng chọn ngày kết thúc")
        LocalDate endDate,

        // Chính sách chỉ cho phép giảm từ 10% đến 20%, gửi dạng tỉ lệ: 0.15 là 15%
        @NotNull(message = "Vui lòng nhập mức giảm")
        @DecimalMin(value = "0.10", message = "Mức giảm tối thiểu là 10%")
        @DecimalMax(value = "0.20", message = "Mức giảm tối đa là 20%")
        BigDecimal discountRate,

        Boolean active
) {
}
