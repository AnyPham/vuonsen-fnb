package vn.vuonsen.fnb.modules.partypackage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

// Dữ liệu thêm hoặc sửa một gói tiệc từ trang quản trị
public record PackageRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String tagline,
        @NotNull @DecimalMin("0") BigDecimal pricePerTable,
        @Min(1) Integer dishCount,
        @Min(1) Integer hoursIncluded,
        Boolean featured,
        Boolean active,
        Integer sortOrder,
        List<String> features
) {
}
