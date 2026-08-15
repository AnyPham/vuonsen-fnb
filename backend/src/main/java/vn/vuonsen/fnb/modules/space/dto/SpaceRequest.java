package vn.vuonsen.fnb.modules.space.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.vuonsen.fnb.modules.space.SpaceType;

import java.math.BigDecimal;
import java.util.List;

// Dữ liệu tạo hoặc sửa không gian từ trang quản trị
public record SpaceRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 140) String slug,
        @NotNull SpaceType spaceType,
        @Size(max = 500) String shortDesc,
        String description,
        @NotNull @Min(1) Integer capacityMin,
        @NotNull @Min(1) Integer capacityMax,
        @NotNull @DecimalMin("0") BigDecimal rentalFee,
        String feeUnit,
        Integer unitCapacity,
        String thumbnailUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean active,
        Integer sortOrder,
        List<String> amenities
) {
}
