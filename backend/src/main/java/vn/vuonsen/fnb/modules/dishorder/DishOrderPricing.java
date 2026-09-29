package vn.vuonsen.fnb.modules.dishorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.config.props.DishOrderProperties;
import vn.vuonsen.fnb.common.i18n.NoiDungSongNgu;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscountLookup;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

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
    private final HolidayDiscountLookup ngayLe;

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
     * Bản tiếng Anh của câu trên.
     *
     * Viết thành hàm riêng thay vì luồn tham số ngôn ngữ vào hàm cũ: câu này có số tiền
     * chèn giữa, mà tiếng Việt viết 500.000 ₫ còn tiếng Anh viết 500,000 VND, nên hai bản
     * khác nhau cả ở cách định dạng số chứ không chỉ khác chữ.
     */
    public String giaiThichPhiGiaoEn(FulfillmentType hinhThuc, BigDecimal tienMon) {
        if (hinhThuc != FulfillmentType.DELIVERY) {
            return "There is no delivery fee on dine-in orders.";
        }
        if (tienMon.compareTo(props.freeDeliveryFrom()) >= 0) {
            return "This order reaches %s, so delivery is free.".formatted(tienEn(props.freeDeliveryFrom()));
        }
        BigDecimal conThieu = props.freeDeliveryFrom().subtract(tienMon);
        return "Add %s more and delivery becomes free.".formatted(tienEn(conThieu));
    }

    // Số tiền giảm kèm câu giải thích; không được giảm thì số tiền là 0 và câu để null
    public record GiamGia(BigDecimal soTien, String ghiChu, String ghiChuEn) {
    }

    /*
     * Giảm giá dịp lễ, xét theo ngày khách nhận món.
     *
     * Chỉ giảm trên tiền món, không giảm phí giao. Mức miễn phí giao và mức đơn tối thiểu
     * vẫn xét trên tiền món trước khi giảm, để khách không vì được giảm giá mà mất quyền
     * miễn phí giao hàng.
     */
    public GiamGia giamGiaNgayLe(BigDecimal tienMon, LocalDateTime nhanLuc) {
        if (nhanLuc == null) {
            return new GiamGia(BigDecimal.ZERO, null, null);
        }
        return ngayLe.find(nhanLuc.toLocalDate())
                .map(dip -> {
                    String phanTram = dip.rate().multiply(BigDecimal.valueOf(100))
                            .stripTrailingZeros().toPlainString();
                    return new GiamGia(
                            lamTron(tienMon.multiply(dip.rate())),
                            "Giảm %s%% dịp %s.".formatted(phanTram, dip.name()),
                            "%s%% off for %s.".formatted(phanTram, NoiDungSongNgu.ngayLe(dip.name())));
                })
                .orElse(new GiamGia(BigDecimal.ZERO, null, null));
    }

    /*
     * Thuế giá trị gia tăng tính trên tiền món sau khi trừ giảm giá, không tính trên phí giao.
     *
     * Phí giao là khoản thu hộ phần vận chuyển, gộp vào rồi đánh thuế lên nó sẽ làm
     * con số khó giải thích với khách khi họ đối chiếu hóa đơn.
     */
    public BigDecimal thue(BigDecimal tienMonSauGiam) {
        return lamTron(tienMonSauGiam.multiply(booking.vatRate()));
    }

    public BigDecimal tongCong(BigDecimal tienMon, BigDecimal giamGia, BigDecimal phiGiao, BigDecimal thue) {
        return lamTron(tienMon.subtract(giamGia).add(phiGiao).add(thue));
    }

    // Làm tròn tới đồng, không để lẻ xu vì tiền Việt không có đơn vị nhỏ hơn
    public BigDecimal lamTron(BigDecimal so) {
        return so.setScale(0, RoundingMode.HALF_UP);
    }

    private String tien(BigDecimal so) {
        return "%,d".formatted(so.setScale(0, RoundingMode.HALF_UP).longValue())
                .replace(',', '.') + "đ";
    }

    // Tiếng Anh dùng dấu phẩy phân nhóm và ghi mã VND sau số, giống cách giao diện hiển thị
    private String tienEn(BigDecimal so) {
        return "%,d VND".formatted(so.setScale(0, RoundingMode.HALF_UP).longValue());
    }
}
