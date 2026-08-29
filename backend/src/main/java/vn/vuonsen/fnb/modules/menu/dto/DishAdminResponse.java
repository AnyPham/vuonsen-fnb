package vn.vuonsen.fnb.modules.menu.dto;

import vn.vuonsen.fnb.modules.menu.Dish;

import java.math.BigDecimal;

// Trang quản trị cần thấy cả món đã ngừng bán và thứ tự sắp xếp,
// nên trả về nhiều trường hơn DishResponse dùng cho khách.
public record DishAdminResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String priceNote,
        String imageUrl,
        boolean bestSeller,
        boolean available,
        Integer sortOrder,
        Long categoryId,
        String categoryName
) {
    public static DishAdminResponse from(Dish d) {
        return new DishAdminResponse(
                d.getId(), d.getName(), d.getDescription(),
                d.getPrice(), d.getPriceNote(), d.getImageUrl(),
                d.isBestSeller(), d.isAvailable(), d.getSortOrder(),
                d.getCategory().getId(), d.getCategory().getName());
    }
}
