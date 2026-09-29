package vn.vuonsen.fnb.modules.dishorder.dto;

import vn.vuonsen.fnb.modules.dishorder.DishOrder;
import vn.vuonsen.fnb.modules.dishorder.DishOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Đơn đặt món trả về cho khách tra cứu và cho trang quản trị
public record DishOrderResponse(
        Long id,
        String code,
        String fulfillmentType,
        String fulfillmentLabel,
        String fulfillmentLabelEn,

        String customerName,
        String customerPhone,
        String customerEmail,
        String deliveryAddress,
        Integer guestCount,

        LocalDateTime serveAt,
        String note,

        List<LineResponse> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal deliveryFee,
        BigDecimal vatAmount,
        BigDecimal total,

        String status,
        String statusLabel,
        String statusLabelEn,
        LocalDateTime createdAt
) {

    /*
     * Tên món trên từng dòng đơn cố ý giữ nguyên tiếng Việt.
     *
     * Đây là bản sao chụp lại lúc khách đặt, không phải tham chiếu tới bảng món. Hóa đơn
     * phải giữ đúng tên và giá tại thời điểm đặt, kể cả sau này nhà hàng đổi tên món.
     */
    public record LineResponse(String dishName, BigDecimal unitPrice,
                               Integer quantity, BigDecimal lineTotal) {
        static LineResponse from(DishOrderItem i) {
            return new LineResponse(i.getDishName(), i.getUnitPrice(),
                    i.getQuantity(), i.getLineTotal());
        }
    }

    public static DishOrderResponse from(DishOrder o) {
        return new DishOrderResponse(
                o.getId(), o.getCode(),
                o.getFulfillmentType().name(), o.getFulfillmentType().getLabel(), o.getFulfillmentType().getLabelEn(),
                o.getCustomerName(), o.getCustomerPhone(), o.getCustomerEmail(),
                o.getDeliveryAddress(), o.getGuestCount(),
                o.getServeAt(), o.getNote(),
                o.getItems().stream().map(LineResponse::from).toList(),
                o.getSubtotal(), o.getDiscountAmount(), o.getDeliveryFee(), o.getVatAmount(), o.getTotal(),
                o.getStatus().name(), o.getStatus().getLabel(), o.getStatus().getLabelEn(), o.getCreatedAt());
    }
}
