package vn.vuonsen.fnb.modules.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// Test công thức tính giá, không cần database
class PricingServiceTest {

    private PricingService pricingService;
    private Space sanhVenSong;
    private Space cumChoiSen;
    private PartyPackage senVang;

    @BeforeEach
    void setUp() {
        BookingProperties properties = new BookingProperties(
                10,                          // 10 khách / mâm
                new BigDecimal("0.08"),      // VAT 8%
                30,                          // miễn phí không gian từ 30 mâm
                60,                          // đặt trước 60 ngày
                new BigDecimal("0.05"),      // giảm 5%
                10, 800);
        pricingService = new PricingService(properties);

        sanhVenSong = Space.builder()
                .name("Sảnh Ven Sông").spaceType(SpaceType.OUTDOOR)
                .capacityMin(300).capacityMax(800)
                .rentalFee(new BigDecimal("15000000")).feeUnit("SESSION")
                .build();

        cumChoiSen = Space.builder()
                .name("Cụm Chòi Sen").spaceType(SpaceType.HUT)
                .capacityMin(8).capacityMax(144)
                .rentalFee(new BigDecimal("500000")).feeUnit("HUT").unitCapacity(12)
                .build();

        senVang = PartyPackage.builder()
                .name("Gói Sen Vàng")
                .pricePerTable(new BigDecimal("4500000"))
                .build();
    }

    @Test
    @DisplayName("Số mâm làm tròn lên: 145 khách thành 15 mâm")
    void tableCountRoundsUp() {
        assertThat(pricingService.tableCountFor(145)).isEqualTo(15);
        assertThat(pricingService.tableCountFor(150)).isEqualTo(15);
        assertThat(pricingService.tableCountFor(151)).isEqualTo(16);
    }

    @Test
    @DisplayName("Dưới 30 mâm vẫn tính phí thuê không gian")
    void chargesSpaceFeeBelowThreshold() {
        // 200 khách = 20 mâm, ngày tổ chức gần nên không được giảm sớm
        var quote = pricingService.calculate(sanhVenSong, senVang, 200, LocalDate.now().plusDays(10));

        assertThat(quote.tableCount()).isEqualTo(20);
        assertThat(quote.foodAmount()).isEqualByComparingTo("90000000");   // 20 x 4.500.000
        assertThat(quote.spaceFee()).isEqualByComparingTo("15000000");
        assertThat(quote.discountAmount()).isEqualByComparingTo("0");
        assertThat(quote.vatAmount()).isEqualByComparingTo("8400000");     // 8% x 105.000.000
        assertThat(quote.totalAmount()).isEqualByComparingTo("113400000");
    }

    @Test
    @DisplayName("Từ 30 mâm trở lên được miễn phí thuê không gian")
    void freeSpaceFromThirtyTables() {
        var quote = pricingService.calculate(sanhVenSong, senVang, 300, LocalDate.now().plusDays(10));

        assertThat(quote.tableCount()).isEqualTo(30);
        assertThat(quote.spaceFee()).isEqualByComparingTo("0");
        assertThat(quote.appliedRules()).anyMatch(r -> r.contains("Miễn phí thuê không gian"));
    }

    @Test
    @DisplayName("Đặt trước 60 ngày được giảm thêm 5% trước khi tính VAT")
    void appliesEarlyBirdDiscount() {
        var quote = pricingService.calculate(sanhVenSong, senVang, 200, LocalDate.now().plusDays(90));

        // (90.000.000 + 15.000.000) x 5% = 5.250.000
        assertThat(quote.discountAmount()).isEqualByComparingTo("5250000");
        // VAT tính trên 99.750.000
        assertThat(quote.vatAmount()).isEqualByComparingTo("7980000");
        assertThat(quote.totalAmount()).isEqualByComparingTo("107730000");
        assertThat(quote.appliedRules()).anyMatch(r -> r.contains("đặt trước"));
    }

    @Test
    @DisplayName("Không gian tính theo chòi: 30 khách thành 3 chòi")
    void chargesPerHutForHutSpaces() {
        var quote = pricingService.calculate(cumChoiSen, senVang, 30, LocalDate.now().plusDays(5));

        assertThat(quote.spaceFee()).isEqualByComparingTo("1500000");   // 3 chòi x 500.000
        assertThat(quote.appliedRules()).anyMatch(r -> r.contains("3 chòi"));
    }

    @Test
    @DisplayName("Tổng tiền = tiền ăn + phí không gian - giảm giá + VAT")
    void totalIsConsistent() {
        var quote = pricingService.calculate(sanhVenSong, senVang, 350, LocalDate.now().plusDays(70));

        BigDecimal expected = quote.foodAmount()
                .add(quote.spaceFee())
                .subtract(quote.discountAmount())
                .add(quote.vatAmount());
        assertThat(quote.totalAmount()).isEqualByComparingTo(expected);
    }
}
