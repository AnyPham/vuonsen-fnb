package vn.vuonsen.fnb.modules.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Dữ liệu thêm hoặc sửa một món ăn từ trang quản trị
public record DishRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 500) String description,
        @DecimalMin("0") BigDecimal price,
        @Size(max = 60) String priceNote,
        @Size(max = 500) String imageUrl,
        Boolean bestSeller,
        Boolean available,
        Integer sortOrder
) {
}
