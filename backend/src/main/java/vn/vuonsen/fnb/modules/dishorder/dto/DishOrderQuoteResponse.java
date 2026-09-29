package vn.vuonsen.fnb.modules.dishorder.dto;

import java.math.BigDecimal;
import java.util.List;

/*
 * Bảng tạm tính.
 *
 * Trả về cả từng dòng món để khách đối chiếu, và trả kèm phần giải thích vì sao có
 * hoặc không có phí giao hàng, thay vì chỉ ném ra một con số tổng.
 */
public record DishOrderQuoteResponse(
        List<LineResponse> lines,
        BigDecimal subtotal,

        // Tiền giảm dịp lễ trên tiền món, 0 nếu ngày nhận là ngày thường
        BigDecimal discountAmount,
        // Ví dụ "Giảm 20% dịp Tết Nguyên Đán.", null nếu không được giảm
        String discountNote,
        String discountNoteEn,

        BigDecimal deliveryFee,
        // Lý do của khoản phí giao, ví dụ "Đơn đủ 500.000đ nên được miễn phí giao"
        String deliveryNote,
        String deliveryNoteEn,
        BigDecimal vatAmount,
        BigDecimal total,

        // Thời điểm sớm nhất có thể nhận món, tính theo món lâu nhất trong đơn
        String leadTimeNote,
        String leadTimeNoteEn
) {

    public record LineResponse(Long dishId, String dishName, BigDecimal unitPrice,
                               Integer quantity, BigDecimal lineTotal) {
    }
}
