package vn.vuonsen.fnb.modules.dishorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import vn.vuonsen.fnb.modules.dishorder.FulfillmentType;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Xin bảng tạm tính trước khi điền thông tin liên hệ.
 *
 * Tách riêng khỏi DishOrderRequest để khách xem được số tiền ngay lúc chọn món, chưa
 * cần khai tên và số điện thoại. Bắt điền thông tin cá nhân rồi mới cho biết giá là
 * cách nhanh nhất làm khách bỏ giữa chừng.
 *
 * Giờ nhận món không bắt buộc. Có giờ nhận thì bảng tạm tính mới biết ngày đó có được
 * giảm giá dịp lễ không; chưa chọn giờ thì tạm tính theo giá ngày thường.
 */
public record DishOrderQuoteRequest(
        @NotNull FulfillmentType fulfillmentType,
        @NotEmpty(message = "Vui lòng chọn ít nhất một món")
        @Valid List<DishOrderRequest.ItemRequest> items,
        LocalDateTime serveAt
) {
    // Tạm tính khi chưa có giờ nhận món
    public DishOrderQuoteRequest(FulfillmentType fulfillmentType, List<DishOrderRequest.ItemRequest> items) {
        this(fulfillmentType, items, null);
    }
}
