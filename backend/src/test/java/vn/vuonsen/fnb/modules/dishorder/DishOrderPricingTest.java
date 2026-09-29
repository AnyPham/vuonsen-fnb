package vn.vuonsen.fnb.modules.dishorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.config.props.DishOrderProperties;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscountLookup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Kiểm thử công thức giảm giá dịp lễ của đơn đặt món, không cần database
class DishOrderPricingTest {

    private static final DishOrderProperties PROPS = new DishOrderProperties(
            new BigDecimal("30000"), new BigDecimal("500000"), new BigDecimal("150000"), 2, 30, 1, 40);

    private static final BookingProperties BOOKING = new BookingProperties(
            10, new BigDecimal("0.08"), 10, 60, new BigDecimal("0.05"), new BigDecimal("0.3"),
            10, 800, 3, 20, 14, 8);

    private static final LocalDateTime NHAN_LUC = LocalDateTime.of(2031, 9, 2, 12, 0);

    private static DishOrderPricing voiTraCuu(HolidayDiscountLookup traCuu) {
        return new DishOrderPricing(PROPS, BOOKING, traCuu);
    }

    private static DishOrderPricing ngayNaoCungLa(String ten, String tiLe) {
        return voiTraCuu(ngay -> Optional.of(new HolidayDiscountLookup.AppliedHoliday(ten, new BigDecimal(tiLe))));
    }

    @Test
    @DisplayName("UT-NL-07 Nhận món vào dịp lễ thì giảm trên tiền món, kèm câu giải thích")
    void giamTrenTienMon() {
        var giam = ngayNaoCungLa("Quốc khánh", "0.10").giamGiaNgayLe(new BigDecimal("370000"), NHAN_LUC);

        assertThat(giam.soTien()).isEqualByComparingTo("37000");
        assertThat(giam.ghiChu()).isEqualTo("Giảm 10% dịp Quốc khánh.");
    }

    @Test
    @DisplayName("UT-NL-08 Chưa chọn giờ nhận món thì không tra dịp lễ và không giảm")
    void chuaCoGioNhan() {
        DishOrderPricing pricing = voiTraCuu(ngay -> {
            throw new AssertionError("Không được tra dịp lễ khi chưa có giờ nhận");
        });

        var giam = pricing.giamGiaNgayLe(new BigDecimal("370000"), null);

        assertThat(giam.soTien()).isEqualByComparingTo("0");
        assertThat(giam.ghiChu()).isNull();
    }

    @Test
    @DisplayName("UT-NL-09 Ngày nhận món là ngày thường thì không giảm")
    void ngayThuongKhongGiam() {
        var giam = voiTraCuu(ngay -> Optional.empty()).giamGiaNgayLe(new BigDecimal("370000"), NHAN_LUC);

        assertThat(giam.soTien()).isEqualByComparingTo("0");
        assertThat(giam.ghiChu()).isNull();
    }

    @Test
    @DisplayName("UT-NL-10 Thuế tính trên tiền món sau giảm, tổng cộng trừ đúng tiền giảm")
    void thueVaTongSauGiam() {
        DishOrderPricing pricing = ngayNaoCungLa("Quốc khánh", "0.10");

        assertThat(pricing.thue(new BigDecimal("333000"))).isEqualByComparingTo("26640");
        assertThat(pricing.tongCong(new BigDecimal("370000"), new BigDecimal("37000"),
                new BigDecimal("30000"), new BigDecimal("26640"))).isEqualByComparingTo("389640");
    }

    @Test
    @DisplayName("UT-NL-11 Tiền giảm được làm tròn tới đồng")
    void lamTronTienGiam() {
        var giam = ngayNaoCungLa("Tết Nguyên Đán", "0.15").giamGiaNgayLe(new BigDecimal("123457"), NHAN_LUC);

        // 123.457 x 15% = 18.518,55
        assertThat(giam.soTien()).isEqualByComparingTo("18519");
    }
}
