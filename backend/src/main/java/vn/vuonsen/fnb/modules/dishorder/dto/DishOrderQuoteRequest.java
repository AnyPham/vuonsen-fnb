package vn.vuonsen.fnb.modules.dishorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import vn.vuonsen.fnb.modules.dishorder.FulfillmentType;

import java.util.List;

/*
 * Xin bảng tạm tính trước khi điền thông tin liên hệ.
 *
 * Tách riêng khỏi DishOrderRequest để khách xem được số tiền ngay lúc chọn món, chưa
 * cần khai tên và số điện thoại. Bắt điền thông tin cá nhân rồi mới cho biết giá là
 * cách nhanh nhất làm khách bỏ giữa chừng.
 */
public record DishOrderQuoteRequest(
        @NotNull FulfillmentType fulfillmentType,
        @NotEmpty(message = "Vui lòng chọn ít nhất một món")
        @Valid List<DishOrderRequest.ItemRequest> items
) {
}
