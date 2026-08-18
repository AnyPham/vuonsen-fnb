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
    private Space sanhSenVang;
    private Space cumChoiSen;
    private PartyPackage senVang;

    @BeforeEach
    void setUp() {
        BookingProperties properties = new BookingProperties(
                10,                          // 10 khách / mâm
                new BigDecimal("0.08"),      // VAT 8%
                10,                          // tiền ăn đạt phí thuê x 10 thì miễn phí thuê
                60,                          // đặt trước 60 ngày
                new BigDecimal("0.05"),      // giảm 5%
                new BigDecimal("0.3"),       // cọc 30%
                10, 800,
                3, 20, 14,                   // báo trước 3 ngày, tiệc lớn 20 mâm báo trước 14 ngày
                8);                          // gói từ 8 tiếng là thuê trọn ngày
        pricingService = new PricingService(properties);

        sanhVenSong = Space.builder()
                .name("Sảnh Ven Sông").spaceType(SpaceType.OUTDOOR)
                .capacityMin(300).capacityMax(800)
                .rentalFee(new BigDecimal("15000000")).feeUnit("SESSION")
                .build();

        sanhSenVang = Space.builder()
                .name("Sảnh Sen Vàng").spaceType(SpaceType.INDOOR)
                .capacityMin(200).capacityMax(500)
                .rentalFee(new BigDecimal("12000000")).feeUnit("SESSION")
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
    @DisplayName("Tiền ăn chưa đạt mức tối thiểu thì phí thuê giảm theo tỉ lệ")
    void spaceFeeReducesGradually() {
        // 200 khách = 20 mâm = 90 triệu tiền ăn, mức tối thiểu của sảnh là 120 triệu
        var quote = pricingService.calculate(sanhSenVang, senVang, 200, LocalDate.now().plusDays(10));

        assertThat(quote.tableCount()).isEqualTo(20);
        assertThat(quote.foodAmount()).isEqualByComparingTo("90000000");
        // còn thiếu 25% doanh thu nên trả 25% phí thuê
        assertThat(quote.spaceFee()).isEqualByComparingTo("3000000");
    }

    @Test
    @DisplayName("Tiền ăn đạt mức tối thiểu thì miễn phí thuê không gian")
    void freeSpaceWhenMinimumSpendReached() {
        // 270 khách = 27 mâm = 121,5 triệu, vượt mức 120 triệu
        var quote = pricingService.calculate(sanhSenVang, senVang, 270, LocalDate.now().plusDays(10));

        assertThat(quote.spaceFee()).isEqualByComparingTo("0");
        assertThat(quote.appliedRules()).anyMatch(r -> r.contains("Miễn phí thuê không gian"));
    }

    @Test
    @DisplayName("Sảnh lớn vẫn thu được phí thuê khi khách đặt ở mức tối thiểu")
    void largeHallStillChargesRentAtMinimumCapacity() {
        // Lỗi cũ: sảnh này nhận tối thiểu 300 khách nên luôn đạt 30 mâm và luôn được miễn phí
        var quote = pricingService.calculate(sanhVenSong, senVang, 300, LocalDate.now().plusDays(10));

        assertThat(quote.foodAmount()).isEqualByComparingTo("135000000");
        assertThat(quote.spaceFee()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Thêm khách thì tổng tiền không bao giờ giảm")
    void totalNeverDropsWhenGuestsIncrease() {
        LocalDate date = LocalDate.now().plusDays(10);
        BigDecimal previous = BigDecimal.ZERO;

        for (int guests = 200; guests <= 500; guests += 10) {
            BigDecimal total = pricingService.calculate(sanhSenVang, senVang, guests, date).totalAmount();
            assertThat(total)
                    .as("tổng tiền tại %d khách", guests)
                    .isGreaterThanOrEqualTo(previous);
            previous = total;
        }
    }

    @Test
    @DisplayName("Đặt trước 60 ngày được giảm thêm 5% trước khi tính VAT")
    void appliesEarlyBirdDiscount() {
        var near = pricingService.calculate(sanhSenVang, senVang, 200, LocalDate.now().plusDays(10));
        var early = pricingService.calculate(sanhSenVang, senVang, 200, LocalDate.now().plusDays(90));

        BigDecimal subtotal = near.foodAmount().add(near.spaceFee());
        assertThat(early.discountAmount()).isEqualByComparingTo(subtotal.multiply(new BigDecimal("0.05")));
        assertThat(early.totalAmount()).isLessThan(near.totalAmount());
        assertThat(early.appliedRules()).anyMatch(r -> r.contains("đặt trước"));
    }

    @Test
    @DisplayName("Không gian tính theo chòi: 30 khách thành 3 chòi")
    void chargesPerHutForHutSpaces() {
        var quote = pricingService.calculate(cumChoiSen, senVang, 30, LocalDate.now().plusDays(5));

        assertThat(quote.spaceFee()).isEqualByComparingTo("1500000");   // 3 chòi x 500.000
        assertThat(quote.appliedRules()).anyMatch(r -> r.contains("3 chòi"));
    }

    @Test
    @DisplayName("Tiền cọc bằng 30% tổng hóa đơn")
    void depositIsThirtyPercent() {
        var quote = pricingService.calculate(sanhSenVang, senVang, 200, LocalDate.now().plusDays(10));

        assertThat(quote.depositAmount())
                .isEqualByComparingTo(quote.totalAmount().multiply(new BigDecimal("0.3"))
                        .setScale(0, java.math.RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Khách ít hơn mức tối thiểu vẫn nhận, tính tiền theo số mâm tối thiểu")
    void chargesMinimumTablesWhenGuestsAreFewer() {
        // Sảnh Ven Sông nhận tối thiểu 300 khách tức 30 mâm
        var quote = pricingService.calculate(sanhVenSong, senVang, 250, LocalDate.now().plusDays(10));

        assertThat(pricingService.tableCountFor(250)).isEqualTo(25);
        assertThat(quote.tableCount()).isEqualTo(30);
        assertThat(quote.foodAmount()).isEqualByComparingTo("135000000");  // 30 x 4.500.000
        assertThat(quote.appliedRules()).anyMatch(r -> r.contains("tối thiểu 30 mâm"));
    }

    @Test
    @DisplayName("Khách đủ mức tối thiểu thì tính đúng số mâm thật")
    void billsActualTablesWhenAboveMinimum() {
        var quote = pricingService.calculate(sanhVenSong, senVang, 420, LocalDate.now().plusDays(10));

        assertThat(quote.tableCount()).isEqualTo(42);
        assertThat(quote.appliedRules()).noneMatch(r -> r.contains("tối thiểu"));
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
