package vn.vuonsen.fnb.modules.dishorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderQuoteRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderRequest;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscount;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscountRepository;
import vn.vuonsen.fnb.modules.menu.DishRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử giảm giá dịp lễ trên luồng đặt món thật, có database.
 *
 * Dịp lễ thêm trong từng test và bị hoàn tác sau test. Món mẫu: Gỏi củ hũ dừa tôm thịt 185.000đ.
 * Các tạm tính dùng năm 2031, không trùng dịp lễ mẫu nào.
 */
@SpringBootTest
@Transactional
class GiamGiaNgayLeDonMonTest {

    private static final String GOI = "Gỏi củ hũ dừa tôm thịt";
    private static final LocalDate NGAY_LE = LocalDate.of(2031, 6, 15);

    @Autowired
    private DishOrderService service;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private HolidayDiscountRepository holidayRepository;

    private DishOrderRequest.ItemRequest dong(String ten, int soLuong) {
        long id = dishRepository.findAllWithCategory().stream()
                .filter(d -> d.getName().equals(ten))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dữ liệu mẫu không có món: " + ten))
                .getId();
        return new DishOrderRequest.ItemRequest(id, soLuong);
    }

    private void dipLe(String ten, LocalDate tu, LocalDate den, String tiLe, boolean dangApDung) {
        holidayRepository.save(HolidayDiscount.builder()
                .name(ten).startDate(tu).endDate(den)
                .discountRate(new BigDecimal(tiLe)).active(dangApDung)
                .build());
    }

    @Test
    @DisplayName("UT-NL-12 Tạm tính món nhận vào dịp lễ trả về tiền giảm, câu giải thích và tổng đã giảm")
    void tamTinhCoGiamGia() {
        dipLe("Lễ thử đơn món", NGAY_LE, NGAY_LE, "0.20", true);

        var kq = service.quote(new DishOrderQuoteRequest(FulfillmentType.DINE_IN,
                List.of(dong(GOI, 2)), NGAY_LE.atTime(12, 0)));

        assertThat(kq.subtotal()).isEqualByComparingTo("370000");
        assertThat(kq.discountAmount()).isEqualByComparingTo("74000");     // 20% của 370.000
        assertThat(kq.vatAmount()).isEqualByComparingTo("23680");          // 8% của 296.000
        assertThat(kq.total()).isEqualByComparingTo("319680");
        assertThat(kq.discountNote()).isEqualTo("Giảm 20% dịp Lễ thử đơn món.");
    }

    @Test
    @DisplayName("UT-NL-13 Đơn giao 555.000 giảm còn 444.000 vẫn miễn phí giao vì xét trên tiền trước giảm")
    void mienPhiGiaoXetTruocGiam() {
        dipLe("Lễ thử giao hàng", NGAY_LE, NGAY_LE, "0.20", true);

        var kq = service.quote(new DishOrderQuoteRequest(FulfillmentType.DELIVERY,
                List.of(dong(GOI, 3)), NGAY_LE.atTime(12, 0)));

        assertThat(kq.discountAmount()).isEqualByComparingTo("111000");
        assertThat(kq.deliveryFee()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("UT-NL-14 Tạm tính chưa có giờ nhận thì không giảm")
    void chuaCoGioNhanThiKhongGiam() {
        dipLe("Lễ thử không giờ", LocalDate.now(), LocalDate.now().plusDays(30), "0.20", true);

        var kq = service.quote(new DishOrderQuoteRequest(FulfillmentType.DINE_IN, List.of(dong(GOI, 2))));

        assertThat(kq.discountAmount()).isEqualByComparingTo("0");
        assertThat(kq.discountNote()).isNull();
        assertThat(kq.total()).isEqualByComparingTo("399600");
    }

    @Test
    @DisplayName("UT-NL-15 Dịp lễ đã tắt thì đơn món không được giảm")
    void dipLeTatThiKhongGiam() {
        LocalDate ngay = NGAY_LE.plusDays(1);
        dipLe("Lễ thử đã tắt", ngay, ngay, "0.20", false);

        var kq = service.quote(new DishOrderQuoteRequest(FulfillmentType.DINE_IN,
                List.of(dong(GOI, 2)), ngay.atTime(12, 0)));

        assertThat(kq.discountAmount()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("UT-NL-16 Gửi đơn món nhận vào dịp lễ thì lưu đúng tiền giảm, tra cứu lại vẫn giữ nguyên")
    void guiDonLuuTienGiam() {
        // Dùng mức 20% là mức trần, nên dù ngày mai trùng dịp lễ mẫu nào thì số liệu vẫn như nhau
        LocalDate ngayMai = LocalDate.now().plusDays(1);
        dipLe("Lễ thử gửi đơn", ngayMai, ngayMai, "0.20", true);
        LocalDateTime nhanLuc = ngayMai.atTime(12, 0);

        var don = service.create(new DishOrderRequest(FulfillmentType.DINE_IN, "Khách thử giảm giá",
                "0912345678", null, null, 4, nhanLuc, null, List.of(dong(GOI, 2))), null);

        assertThat(don.discountAmount()).isEqualByComparingTo("74000");
        assertThat(don.total()).isEqualByComparingTo("319680");
        var traCuu = service.getByCode(don.code());
        assertThat(traCuu.discountAmount()).isEqualByComparingTo("74000");
        assertThat(traCuu.total()).isEqualByComparingTo("319680");
    }
}
