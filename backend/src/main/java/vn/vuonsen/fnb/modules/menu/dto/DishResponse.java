package vn.vuonsen.fnb.modules.menu.dto;

import vn.vuonsen.fnb.modules.menu.Dish;

import java.math.BigDecimal;

public record DishResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String priceNote,
        String imageUrl,
        boolean bestSeller,
        String categoryCode,
        String categoryName
) {
    public static DishResponse from(Dish d) {
        return new DishResponse(
                d.getId(), d.getName(), d.getDescription(),
                d.getPrice(), d.getPriceNote(), d.getImageUrl(), d.isBestSeller(),
                d.getCategory().getCode(), d.getCategory().getName());
    }
}
