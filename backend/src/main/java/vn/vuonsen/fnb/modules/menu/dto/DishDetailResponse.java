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
        String slug,
        String description,
        BigDecimal price,
        String priceNote,
        String imageUrl,
        boolean bestSeller,
        String categoryCode,
        String categoryName,

        String ingredients,
        String preparation,
        // Những điều khách nên biết trước khi đặt món này
        String orderNote,
        String portionDesc,
        Integer prepMinutes,
        List<ImageResponse> images
) {

    public record ImageResponse(String url, String caption) {
    }

    public static DishDetailResponse from(Dish d) {
        return new DishDetailResponse(
                d.getId(), d.getName(), d.getSlug(), d.getDescription(),
                d.getPrice(), d.getPriceNote(), d.getImageUrl(), d.isBestSeller(),
                d.getCategory().getCode(), d.getCategory().getName(),
                d.getIngredients(), d.getPreparation(), d.getOrderNote(),
                d.getPortionDesc(), d.getPrepMinutes(),
                d.getImages().stream()
                        .map(a -> new ImageResponse(a.getUrl(), a.getCaption()))
                        .toList());
    }
}
