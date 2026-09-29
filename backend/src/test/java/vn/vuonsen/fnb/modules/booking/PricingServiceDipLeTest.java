package vn.vuonsen.fnb.modules.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscountLookup;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử giảm giá dịp lễ khi báo giá tiệc, không cần database.
 *
 * Dịp lễ được giả lập bằng một hàm tra cứu trả sẵn kết quả cho một khoảng ngày. Số liệu chuẩn:
 * 200 khách, Sảnh Sen Vàng, Gói Sen Vàng: tiền ăn 90.000.000 + phí thuê 3.000.000 = 93.000.000.
 */
class PricingServiceDipLeTest {

    private static final BookingProperties PROPS = new BookingProperties(
            10, new BigDecimal("0.08"), 10,
            60, new BigDecimal("0.05"),      // đặt trước 60 ngày giảm 5%
            new BigDecimal("0.3"),
            10, 800, 3, 20, 14, 8);

    private final Space sanhSenVang = Space.builder()
            .name("Sảnh Sen Vàng").spaceType(SpaceType.INDOOR)
            .capacityMin(200).capacityMax(500)
            .rentalFee(new BigDecimal("12000000")).feeUnit("SESSION")
            .build();

    private final PartyPackage goiSenVang = PartyPackage.builder()
            .name("Gói Sen Vàng")
            .pricePerTable(new BigDecimal("4500000"))
            .build();

    private static LocalDate sau(int soNgay) {
        return LocalDate.now().plusDays(soNgay);
    }

    // Mọi ngày trong khoảng [tu, den] thuộc dịp lễ có tên và mức giảm cho trước
    private static PricingService voiDipLe(String ten, LocalDate tu, LocalDate den, String tiLe) {
        return new PricingService(PROPS, ngay -> ngay.isBefore(tu) || ngay.isAfter(den)
                ? Optional.empty()
                : Optional.of(new HolidayDiscountLookup.AppliedHoliday(ten, new BigDecimal(tiLe))));
    }

    @Test
    @DisplayName("UT-NL-01 Tiệc rơi vào dịp lễ khi chưa đủ ngày đặt sớm thì giảm theo mức dịp lễ")
    void giamTheoDipLe() {
        var q = voiDipLe("Tết Nguyên Đán", sau(20), sau(22), "0.15")
                .calculate(sanhSenVang, goiSenVang, 200, sau(20));

        assertThat(q.discountAmount()).isEqualByComparingTo("13950000");   // 15% của 93.000.000
        assertThat(q.vatAmount()).isEqualByComparingTo("6324000");         // 8% của 79.050.000
        assertThat(q.totalAmount()).isEqualByComparingTo("85374000");
        assertThat(q.depositAmount()).isEqualByComparingTo("25612200");    // cọc 30% trên tổng đã giảm
        assertThat(q.appliedRules()).contains("Giảm 15% dịp Tết Nguyên Đán")
                .noneMatch(r -> r.contains("cộng dồn"));
    }

    @Test
    @DisplayName("UT-NL-02 Tiệc vừa đặt sớm vừa trùng dịp lễ thì chỉ lấy mức cao hơn, không cộng dồn")
    void khongCongDonKhiDipLeCaoHon() {
        var q = voiDipLe("Quốc khánh", sau(70), sau(70), "0.20")
                .calculate(sanhSenVang, goiSenVang, 200, sau(70));

        assertThat(q.discountAmount()).isEqualByComparingTo("18600000");   // 20%, không phải 25%
        assertThat(q.totalAmount()).isEqualByComparingTo("80352000");
        assertThat(q.appliedRules())
                .contains("Giảm 20% dịp Quốc khánh")
                .anyMatch(r -> r.contains("không cộng dồn"))
                .noneMatch(r -> r.contains("do đặt trước"));
    }

    @Test
    @DisplayName("UT-NL-03 Dịp lễ giảm thấp hơn ưu đãi đặt sớm thì giữ ưu đãi đặt sớm")
    void giuUuDaiDatSomKhiCaoHon() {
        // Chính sách không cho mức dưới 10%, ở đây cố ý dùng 3% để thử nhánh so sánh
        var q = voiDipLe("Dịp giảm ít", sau(70), sau(70), "0.03")
                .calculate(sanhSenVang, goiSenVang, 200, sau(70));

        assertThat(q.discountAmount()).isEqualByComparingTo("4650000");    // 5% của 93.000.000
        assertThat(q.appliedRules())
                .contains("Giảm 5% do đặt trước 60 ngày")
                .anyMatch(r -> r.contains("không cộng dồn"))
                .noneMatch(r -> r.contains("dịp Dịp giảm ít"));
    }

    @Test
    @DisplayName("UT-NL-04 Ngày đầu và ngày cuối của dịp lễ đều được giảm, ngày ngay sau thì không")
    void bienCuaKhoangNgay() {
        PricingService pricing = voiDipLe("Giỗ Tổ Hùng Vương", sau(20), sau(22), "0.10");

        assertThat(pricing.calculate(sanhSenVang, goiSenVang, 200, sau(20)).discountAmount())
                .isEqualByComparingTo("9300000");
        assertThat(pricing.calculate(sanhSenVang, goiSenVang, 200, sau(22)).discountAmount())
                .isEqualByComparingTo("9300000");
        assertThat(pricing.calculate(sanhSenVang, goiSenVang, 200, sau(23)).discountAmount())
                .isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("UT-NL-05 Thuê riêng không gian vào dịp lễ thì giảm trên phí thuê")
    void chiThueKhongGianCungDuocGiam() {
        var q = voiDipLe("Tết Dương lịch", sau(20), sau(20), "0.10")
                .calculate(sanhSenVang, null, 200, sau(20));

        assertThat(q.spaceFee()).isEqualByComparingTo("12000000");
        assertThat(q.discountAmount()).isEqualByComparingTo("1200000");
        assertThat(q.vatAmount()).isEqualByComparingTo("864000");
        assertThat(q.totalAmount()).isEqualByComparingTo("11664000");
    }

    @Test
    @DisplayName("UT-NL-06 Chưa chọn ngày tổ chức thì không tra dịp lễ và không giảm")
    void khongCoNgayThiKhongTra() {
        PricingService pricing = new PricingService(PROPS, ngay -> {
            throw new AssertionError("Không được tra dịp lễ khi chưa có ngày");
        });

        var q = pricing.calculate(sanhSenVang, goiSenVang, 200, null);

        assertThat(q.discountAmount()).isEqualByComparingTo("0");
        assertThat(q.totalAmount()).isEqualByComparingTo("100440000");
    }
}
