package vn.vuonsen.fnb.modules.booking.dto;

import vn.vuonsen.fnb.modules.booking.PricingService;

import java.math.BigDecimal;
import java.util.List;

// Bảng kê chi phí hiện ở khối Tạm tính ngoài giao diện
public record QuoteResponse(
        int guestCount,
        int tableCount,
        BigDecimal unitPrice,
        BigDecimal foodAmount,
        BigDecimal spaceFee,
        BigDecimal discountAmount,
        BigDecimal vatRate,
        BigDecimal vatAmount,
        BigDecimal totalAmount,
        BigDecimal depositAmount,
        List<String> appliedRules
) {
    public static QuoteResponse from(PricingService.Quote q) {
        return new QuoteResponse(
                q.guestCount(), q.tableCount(), q.unitPrice(), q.foodAmount(), q.spaceFee(),
                q.discountAmount(), q.vatRate(), q.vatAmount(), q.totalAmount(),
                q.depositAmount(), q.appliedRules());
    }
}
