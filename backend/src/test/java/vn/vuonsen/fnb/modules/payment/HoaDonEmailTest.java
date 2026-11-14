package vn.vuonsen.fnb.modules.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.config.props.MailProperties;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.booking.TimeSlot;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;
import vn.vuonsen.fnb.modules.dishorder.DishOrderItem;
import vn.vuonsen.fnb.modules.dishorder.FulfillmentType;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Thư hóa đơn dựng từ đối tượng tạo tay, không cần cơ sở dữ liệu.
 * Hai bản HTML được ghi ra target/hoa-don-mau-*.html để mở trình duyệt xem giao diện thư.
 */
class HoaDonEmailTest {

    private static final String WEB = "http://localhost:5173";

    private static Payment phieuTiec() {
        Booking b = Booking.builder()
                .code("VS-20261114-0001")
                .space(Space.builder().name("Sảnh Sen Vàng").build())
                .partyPackage(PartyPackage.builder().name("Gói Sen Vàng").build())
                .eventType(EventType.WEDDING)
                .eventDate(LocalDate.of(2027, 1, 16))
                .timeSlot(TimeSlot.EVENING)
                .guestCount(200).tableCount(20)
                .unitPrice(new BigDecimal("4500000"))
                .foodAmount(new BigDecimal("90000000"))
                .spaceFee(new BigDecimal("3000000"))
                .discountAmount(new BigDecimal("4650000"))
                .vatRate(new BigDecimal("0.08"))
                .vatAmount(new BigDecimal("7068000"))
                .totalAmount(new BigDecimal("95418000"))
                .depositPaid(new BigDecimal("28625400"))
                .customerName("Nguyễn Minh Anh <script>")
                .build();
        return Payment.builder().code("PT-20261114-0001").orderType(OrderType.BOOKING).booking(b)
                .method(PaymentMethod.VNPAY).amount(new BigDecimal("28625400"))
                .confirmedAt(LocalDateTime.of(2026, 11, 14, 20, 15)).build();
    }

    private static Payment phieuMon() {
        DishOrder o = DishOrder.builder()
                .code("VM-20261114-0002")
                .fulfillmentType(FulfillmentType.DELIVERY)
                .serveAt(LocalDateTime.of(2026, 11, 15, 18, 30))
                .deliveryAddress("12 Nguyễn Văn Bảo, Gò Vấp")
                .subtotal(new BigDecimal("1445000"))
                .deliveryFee(BigDecimal.ZERO)
                .total(new BigDecimal("1445000"))
                .paidAmount(new BigDecimal("1445000"))
                .customerName("Trần Thu Hà")
                .build();
        o.setItems(List.of(
                DishOrderItem.builder().dishName("Combo nướng than hoa").quantity(1)
                        .unitPrice(new BigDecimal("690000")).lineTotal(new BigDecimal("690000")).build(),
                DishOrderItem.builder().dishName("Tôm càng nướng muối ớt").quantity(1)
                        .unitPrice(new BigDecimal("620000")).lineTotal(new BigDecimal("620000")).build(),
                DishOrderItem.builder().dishName("Cơm cháy chà bông kho quẹt").quantity(1)
                        .unitPrice(new BigDecimal("135000")).lineTotal(new BigDecimal("135000")).build()));
        return Payment.builder().code("PT-20261114-0002").orderType(OrderType.DISH_ORDER).dishOrder(o)
                .method(PaymentMethod.VIETQR).amount(new BigDecimal("1445000"))
                .confirmedAt(LocalDateTime.of(2026, 11, 14, 20, 20)).build();
    }

    @Test
    @DisplayName("Hóa đơn tiệc kê đủ tiền ăn, phí thuê, giảm giá, VAT và số còn phải trả")
    void hoaDonTiec() throws Exception {
        HoaDonEmail.NoiDung thu = HoaDonEmail.dung(phieuTiec(), WEB);

        assertThat(thu.html())
                .contains("VS-20261114-0001", "Sảnh Sen Vàng", "Gói Sen Vàng", "200 khách (20 mâm)")
                .contains("Tiền ăn: 20 mâm × 4.500.000 đ", "90.000.000 đ", "3.000.000 đ")
                .contains("− 4.650.000 đ", "Thuế VAT 8%", "95.418.000 đ", "28.625.400 đ", "66.792.600 đ")
                .contains(WEB + "/tra-cuu?code=VS-20261114-0001")
                .doesNotContain("<script>").contains("&lt;script&gt;");
        assertThat(thu.vanBan())
                .contains("đã nhận được thanh toán", "Còn phải trả: 66.792.600 đ", "Phí thuê không gian");
        Files.writeString(Path.of("target", "hoa-don-mau-tiec.html"), thu.html());
    }

    @Test
    @DisplayName("Hóa đơn món lẻ kê từng món; trả đủ thì còn phải trả bằng 0")
    void hoaDonMon() throws Exception {
        HoaDonEmail.NoiDung thu = HoaDonEmail.dung(phieuMon(), WEB);

        assertThat(thu.html())
                .contains("Combo nướng than hoa × 1", "690.000 đ", "Tôm càng nướng muối ớt × 1")
                .contains("Giao tận nhà", "12 Nguyễn Văn Bảo, Gò Vấp", "1.445.000 đ", "0 đ")
                .contains(WEB + "/tra-cuu-mon?ma=VM-20261114-0002")
                .doesNotContain("Phí giao hàng");
        Files.writeString(Path.of("target", "hoa-don-mau-mon.html"), thu.html());
    }

    @Test
    @DisplayName("Thư tới tên miền kiểm thử chỉ ghi nhật ký, địa chỉ thật thì được gửi")
    void tenMienKiemThu() {
        MailProperties p = new MailProperties("Vuon Sen <a@gmail.com>", WEB, List.of("example.com", "vuonsen.vn"));

        assertThat(p.chiGhi("khach.123@vuonsen.vn")).isTrue();
        assertThat(p.chiGhi("a@Example.COM")).isTrue();
        assertThat(p.chiGhi("a@mail.example.com")).isTrue();
        assertThat(p.chiGhi("khach@gmail.com")).isFalse();
        assertThat(p.chiGhi("khach@notexample.com")).isFalse();
        assertThat(p.chiGhi("khong-co-a-cong")).isTrue();
    }
}
