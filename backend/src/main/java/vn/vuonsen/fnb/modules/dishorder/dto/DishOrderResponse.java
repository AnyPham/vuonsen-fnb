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

        String customerName,
        String customerPhone,
        String customerEmail,
        String deliveryAddress,
        Integer guestCount,

        LocalDateTime serveAt,
        String note,

        List<LineResponse> items,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal vatAmount,
        BigDecimal total,

        String status,
        String statusLabel,
        LocalDateTime createdAt
) {

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
                o.getFulfillmentType().name(), o.getFulfillmentType().getLabel(),
                o.getCustomerName(), o.getCustomerPhone(), o.getCustomerEmail(),
                o.getDeliveryAddress(), o.getGuestCount(),
                o.getServeAt(), o.getNote(),
                o.getItems().stream().map(LineResponse::from).toList(),
                o.getSubtotal(), o.getDeliveryFee(), o.getVatAmount(), o.getTotal(),
                o.getStatus().name(), o.getStatus().getLabel(), o.getCreatedAt());
    }
}
