package vn.vuonsen.fnb.modules.space.dto;

import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.util.List;

public record SpaceResponse(
        Long id,
        String code,
        String name,
        String slug,
        String type,
        String typeLabel,
        String shortDesc,
        String description,
        Integer capacityMin,
        Integer capacityMax,
        BigDecimal rentalFee,
        String feeUnit,
        Integer unitCapacity,
        String thumbnailUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        List<String> amenities
) {
    public static SpaceResponse from(Space s) {
        return new SpaceResponse(
                s.getId(), s.getCode(), s.getName(), s.getSlug(),
                s.getSpaceType().name(), s.getSpaceType().getLabel(),
                s.getShortDesc(), s.getDescription(),
                s.getCapacityMin(), s.getCapacityMax(),
                s.getRentalFee(), s.getFeeUnit(), s.getUnitCapacity(), s.getThumbnailUrl(),
                s.getLatitude(), s.getLongitude(),
                List.copyOf(s.getAmenities()));
    }
}
