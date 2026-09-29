package vn.vuonsen.fnb.modules.partypackage.dto;

import vn.vuonsen.fnb.common.i18n.NoiDungSongNgu;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;

import java.math.BigDecimal;
import java.util.List;

public record PackageResponse(
        Long id,
        String code,
        String name,
        String nameEn,
        String tagline,
        String taglineEn,
        BigDecimal pricePerTable,
        Integer dishCount,
        Integer hoursIncluded,
        boolean featured,
        List<String> features,
        List<String> featuresEn
) {
    public static PackageResponse from(PartyPackage p) {
        return new PackageResponse(
                p.getId(), p.getCode(), p.getName(), p.getNameEn(),
                p.getTagline(), p.getTaglineEn(),
                p.getPricePerTable(), p.getDishCount(), p.getHoursIncluded(),
                p.isFeatured(), List.copyOf(p.getFeatures()),
                NoiDungSongNgu.hangMucGoi(p.getFeatures()));
    }
}
