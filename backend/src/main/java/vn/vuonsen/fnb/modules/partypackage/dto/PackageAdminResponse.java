package vn.vuonsen.fnb.modules.partypackage.dto;

import vn.vuonsen.fnb.modules.partypackage.PartyPackage;

import java.math.BigDecimal;
import java.util.List;

// Trang quản trị cần thấy cả gói đã ngừng bán và thứ tự sắp xếp
public record PackageAdminResponse(
        Long id,
        String code,
        String name,
        String tagline,
        BigDecimal pricePerTable,
        Integer dishCount,
        Integer hoursIncluded,
        boolean featured,
        boolean active,
        Integer sortOrder,
        List<String> features
) {
    public static PackageAdminResponse from(PartyPackage p) {
        return new PackageAdminResponse(
                p.getId(), p.getCode(), p.getName(), p.getTagline(),
                p.getPricePerTable(), p.getDishCount(), p.getHoursIncluded(),
                p.isFeatured(), p.isActive(), p.getSortOrder(),
                List.copyOf(p.getFeatures()));
    }
}
