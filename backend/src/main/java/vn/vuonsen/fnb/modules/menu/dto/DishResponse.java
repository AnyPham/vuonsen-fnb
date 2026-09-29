package vn.vuonsen.fnb.modules.menu.dto;

import vn.vuonsen.fnb.modules.menu.Dish;

import java.math.BigDecimal;

public record DishResponse(
        Long id,
        String name,
        String nameEn,
        // Đường dẫn sang trang chi tiết món
        String slug,
        String description,
        String descriptionEn,
        BigDecimal price,
        String priceNote,
        String priceNoteEn,
        String imageUrl,
        boolean bestSeller,
        String categoryCode,
        String categoryName,
        String categoryNameEn
) {
    public static DishResponse from(Dish d) {
        return new DishResponse(
                d.getId(), d.getName(), d.getNameEn(), d.getSlug(),
                d.getDescription(), d.getDescriptionEn(),
                d.getPrice(), d.getPriceNote(), d.getPriceNoteEn(),
                d.getImageUrl(), d.isBestSeller(),
                d.getCategory().getCode(), d.getCategory().getName(), d.getCategory().getNameEn());
    }
}
