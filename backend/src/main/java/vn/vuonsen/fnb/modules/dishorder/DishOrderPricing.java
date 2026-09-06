package vn.vuonsen.fnb.modules.dishorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.config.props.DishOrderProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * Tính tiền cho đơn đặt món.
 *
 * Tách khỏi service và không đụng tới cơ sở dữ liệu, chỉ nhận vào con số. Nhờ vậy
 * kiểm thử công thức tính giá không cần dựng cả ứng dụng, mà đây lại là phần dễ sai
 * và sai thì mất tiền thật.
 *
 * Thuế suất dùng chung với đơn đặt tiệc, vì cùng một nhà hàng thì không có lý do gì
 * hai loại đơn chịu hai mức thuế khác nhau.
 */
@Component
@RequiredArgsConstructor
public class DishOrderPricing {

    private final DishOrderProperties props;
    private final BookingProperties booking;

    /*
     * Phí giao hàng.
     *
     * Đơn ăn tại chỗ không có phí. Đơn giao tận nhà được miễn phí khi tiền món đạt
     * mức quy định, dưới mức đó thì tính phí cố định.
     */
    public BigDecimal phiGiao(FulfillmentType hinhThuc, BigDecimal tienMon) {
        if (hinhThuc != FulfillmentType.DELIVERY) {
            return BigDecimal.ZERO;
        }
        if (tienMon.compareTo(props.freeDeliveryFrom()) >= 0) {
            return BigDecimal.ZERO;
        }
        return props.deliveryFee();
    }

    // Câu giải thích đi kèm khoản phí giao, để khách hiểu vì sao có hoặc không có
    public String giaiThichPhiGiao(FulfillmentType hinhThuc, BigDecimal tienMon) {
        if (hinhThuc != FulfillmentType.DELIVERY) {
            return "Đơn ăn tại chỗ không tính phí giao hàng.";
        }
        if (tienMon.compareTo(props.freeDeliveryFrom()) >= 0) {
            return "Đơn đạt %s nên được miễn phí giao hàng.".formatted(tien(props.freeDeliveryFrom()));
        }
        BigDecimal conThieu = props.freeDeliveryFrom().subtract(tienMon);
        return "Đặt thêm %s nữa là được miễn phí giao hàng.".formatted(tien(conThieu));
    }

    /*
     * Thuế giá trị gia tăng tính trên tiền món, không tính trên phí giao.
     *
     * Phí giao là khoản thu hộ phần vận chuyển, gộp vào rồi đánh thuế lên nó sẽ làm
     * con số khó giải thích với khách khi họ đối chiếu hóa đơn.
     */
    public BigDecimal thue(BigDecimal tienMon) {
        return lamTron(tienMon.multiply(booking.vatRate()));
    }

    public BigDecimal tongCong(BigDecimal tienMon, BigDecimal phiGiao, BigDecimal thue) {
        return lamTron(tienMon.add(phiGiao).add(thue));
    }

    // Làm tròn tới đồng, không để lẻ xu vì tiền Việt không có đơn vị nhỏ hơn
    public BigDecimal lamTron(BigDecimal so) {
        return so.setScale(0, RoundingMode.HALF_UP);
    }

    private String tien(BigDecimal so) {
        return "%,d".formatted(so.setScale(0, RoundingMode.HALF_UP).longValue())
                .replace(',', '.') + "đ";
    }
}
