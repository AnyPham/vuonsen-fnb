package vn.vuonsen.fnb.modules.space.dto;

import vn.vuonsen.fnb.common.i18n.NoiDungSongNgu;
import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.util.List;

public record SpaceResponse(
        Long id,
        String code,
        String name,
        String nameEn,
        String slug,
        String type,
        String typeLabel,
        String typeLabelEn,
        String shortDesc,
        String shortDescEn,
        String description,
        String descriptionEn,
        Integer capacityMin,
        Integer capacityMax,
        BigDecimal rentalFee,
        String feeUnit,
        Integer unitCapacity,
        String thumbnailUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        List<String> amenities,
        List<String> amenitiesEn,
        // Thư viện ảnh, chỉ trả về ở trang chi tiết
        List<ImageResponse> images
) {

    public record ImageResponse(String url, String caption) {
    }

    /*
     * Dùng cho danh sách không gian.
     *
     * Cố ý bỏ trống thư viện ảnh: trang danh sách chỉ hiện một ảnh đại diện mỗi thẻ,
     * lấy thêm ảnh chi tiết vừa tốn một câu truy vấn cho mỗi không gian vừa làm phình
     * dữ liệu trả về mà không ai dùng tới.
     */
    public static SpaceResponse from(Space s) {
        return dung(s, List.of());
    }

    // Dùng cho trang chi tiết, nơi khách thực sự muốn xem nhiều góc ảnh
    public static SpaceResponse withImages(Space s) {
        return dung(s, s.getImages().stream()
                .map(a -> new ImageResponse(a.getUrl(), a.getCaption()))
                .toList());
    }

    private static SpaceResponse dung(Space s, List<ImageResponse> anh) {
        return new SpaceResponse(
                s.getId(), s.getCode(), s.getName(), s.getNameEn(), s.getSlug(),
                s.getSpaceType().name(), s.getSpaceType().getLabel(), s.getSpaceType().getLabelEn(),
                s.getShortDesc(), s.getShortDescEn(),
                s.getDescription(), s.getDescriptionEn(),
                s.getCapacityMin(), s.getCapacityMax(),
                s.getRentalFee(), s.getFeeUnit(), s.getUnitCapacity(), s.getThumbnailUrl(),
                s.getLatitude(), s.getLongitude(),
                List.copyOf(s.getAmenities()),
                NoiDungSongNgu.tienIch(s.getAmenities()),
                anh);
    }
}
