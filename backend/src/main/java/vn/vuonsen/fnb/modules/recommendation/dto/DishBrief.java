package vn.vuonsen.fnb.modules.recommendation.dto;

import vn.vuonsen.fnb.modules.menu.Dish;

import java.math.BigDecimal;

// Một món trong thực đơn gợi ý, chỉ lấy vừa đủ trường để hiện lên giao diện
public record DishBrief(
        Long id,
        String name,
        String categoryName,
        BigDecimal price,
        String priceNote
) {
    public static DishBrief from(Dish d) {
        return new DishBrief(d.getId(), d.getName(), d.getCategory().getName(),
                d.getPrice(), d.getPriceNote());
    }
}
