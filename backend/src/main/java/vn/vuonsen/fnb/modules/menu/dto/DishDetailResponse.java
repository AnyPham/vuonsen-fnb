package vn.vuonsen.fnb.modules.menu.dto;

import vn.vuonsen.fnb.modules.menu.Dish;

import java.math.BigDecimal;
import java.util.List;

/*
 * Dữ liệu cho trang chi tiết một món.
 *
 * Tách riêng khỏi DishResponse vì trang danh sách không cần nguyên liệu, cách chế
 * biến hay thư viện ảnh. Gộp chung thì mỗi lần mở trang thực đơn lại kéo theo mấy
 * nghìn ký tự chữ không ai đọc.
 */
public record DishDetailResponse(
        Long id,
        String name,
        String nameEn,
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
        String categoryNameEn,

        String ingredients,
        String ingredientsEn,
        String preparation,
        String preparationEn,
        // Những điều khách nên biết trước khi đặt món này
        String orderNote,
        String orderNoteEn,
        String portionDesc,
        String portionDescEn,
        Integer prepMinutes,
        List<ImageResponse> images
) {

    public record ImageResponse(String url, String caption) {
    }

    public static DishDetailResponse from(Dish d) {
        return new DishDetailResponse(
                d.getId(), d.getName(), d.getNameEn(), d.getSlug(),
                d.getDescription(), d.getDescriptionEn(),
                d.getPrice(), d.getPriceNote(), d.getPriceNoteEn(),
                d.getImageUrl(), d.isBestSeller(),
                d.getCategory().getCode(), d.getCategory().getName(), d.getCategory().getNameEn(),
                d.getIngredients(), d.getIngredientsEn(),
                d.getPreparation(), d.getPreparationEn(),
                d.getOrderNote(), d.getOrderNoteEn(),
                d.getPortionDesc(), d.getPortionDescEn(), d.getPrepMinutes(),
                d.getImages().stream()
                        .map(a -> new ImageResponse(a.getUrl(), a.getCaption()))
                        .toList());
    }
}
