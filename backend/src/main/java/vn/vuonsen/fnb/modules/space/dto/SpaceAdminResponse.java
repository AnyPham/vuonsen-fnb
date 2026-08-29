package vn.vuonsen.fnb.modules.space.dto;

import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.util.List;

// Trang quản trị cần thấy cả không gian đã ngừng kinh doanh và thứ tự sắp xếp,
// nên trả về nhiều trường hơn SpaceResponse dùng cho khách.
public record SpaceAdminResponse(
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
        boolean active,
        Integer sortOrder,
        List<String> amenities
) {
    public static SpaceAdminResponse from(Space s) {
        return new SpaceAdminResponse(
                s.getId(), s.getCode(), s.getName(), s.getSlug(),
                s.getSpaceType().name(), s.getSpaceType().getLabel(),
                s.getShortDesc(), s.getDescription(),
                s.getCapacityMin(), s.getCapacityMax(),
                s.getRentalFee(), s.getFeeUnit(), s.getUnitCapacity(), s.getThumbnailUrl(),
                s.getLatitude(), s.getLongitude(),
                s.isActive(), s.getSortOrder(),
                List.copyOf(s.getAmenities()));
    }
}
