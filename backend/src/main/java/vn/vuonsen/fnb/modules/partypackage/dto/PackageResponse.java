package vn.vuonsen.fnb.modules.partypackage.dto;

import vn.vuonsen.fnb.modules.partypackage.PartyPackage;

import java.math.BigDecimal;
import java.util.List;

public record PackageResponse(
        Long id,
        String code,
        String name,
        String tagline,
        BigDecimal pricePerTable,
        Integer dishCount,
        Integer hoursIncluded,
        boolean featured,
        List<String> features
) {
    public static PackageResponse from(PartyPackage p) {
        return new PackageResponse(
                p.getId(), p.getCode(), p.getName(), p.getTagline(),
                p.getPricePerTable(), p.getDishCount(), p.getHoursIncluded(),
                p.isFeatured(), List.copyOf(p.getFeatures()));
    }
}
