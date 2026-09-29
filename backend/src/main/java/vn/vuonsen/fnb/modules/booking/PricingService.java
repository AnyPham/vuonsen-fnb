package vn.vuonsen.fnb.modules.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.common.i18n.NoiDungSongNgu;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscountLookup;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Tính giá tiệc:
// tổng = tiền ăn + phí không gian - giảm giá + VAT
@Service
@RequiredArgsConstructor
public class PricingService {

    // Tiền Việt không có số lẻ nên làm tròn về đồng
    private static final int MONEY_SCALE = 0;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BookingProperties properties;
    private final HolidayDiscountLookup holidayDiscounts;

    // Kết quả báo giá gửi về cho giao diện
    public record Quote(
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
            List<String> appliedRules,
            List<String> appliedRulesEn
    ) {
    }

    /*
     * Hai danh sách quy tắc chạy song song, tiếng Việt và tiếng Anh.
     *
     * Gom vào một lớp nhỏ để mỗi chỗ ghi chỉ gọi một lần them(vi, en), không thể quên một
     * bên. Nếu để hai List rời rồi tự nhớ add vào cả hai thì sớm muộn cũng lệch nhau, mà
     * lệch kiểu đó chỉ lộ ra khi có người đọc bản tiếng Anh.
     */
    private static final class BangQuyTac {
        private final List<String> vi = new ArrayList<>();
        private final List<String> en = new ArrayList<>();

        void them(String cauViet, String cauAnh) {
            vi.add(cauViet);
            en.add(cauAnh);
        }
    }

    public Quote calculate(Space space, PartyPackage partyPackage, int guestCount, LocalDate eventDate) {
        BangQuyTac rules = new BangQuyTac();

        /*
         * Không kèm gói tiệc nghĩa là khách chỉ thuê không gian: không có mâm, không có
         * tiền ăn, và không áp mức mâm tối thiểu của sảnh vì không phục vụ tiệc. Phí thuê
         * lúc này tính đủ, vì mức giảm phí thuê vốn dựa trên tiền ăn.
         */
        int tableCount;
        BigDecimal unitPrice;
        if (partyPackage == null) {
            tableCount = 0;
            unitPrice = BigDecimal.ZERO;
            rules.them("Chỉ thuê không gian, chưa gồm gói tiệc",
                    "Venue hire only, no catering package included");
        } else {
            tableCount = billedTablesFor(space, tableCountFor(guestCount), rules);
            unitPrice = partyPackage.getPricePerTable();
        }
        BigDecimal foodAmount = money(unitPrice.multiply(BigDecimal.valueOf(tableCount)));

        BigDecimal spaceFee = spaceFeeFor(space, guestCount, foodAmount, rules);
        BigDecimal subtotal = foodAmount.add(spaceFee);

        BigDecimal discount = discountFor(subtotal, eventDate, rules);
        BigDecimal taxable = subtotal.subtract(discount);

        BigDecimal vatRate = properties.vatRate();
        BigDecimal vatAmount = money(taxable.multiply(vatRate));
        BigDecimal total = money(taxable.add(vatAmount));
        BigDecimal deposit = money(total.multiply(properties.depositRate()));

        return new Quote(guestCount, tableCount, unitPrice, foodAmount, spaceFee,
                discount, vatRate, vatAmount, total, deposit, rules.vi, rules.en);
    }

    /*
     * Mỗi không gian nhận đặt tối thiểu một số mâm, suy ra từ sức chứa tối thiểu.
     * Khách mời ít hơn vẫn tính tiền theo mức tối thiểu, giống cách các trung tâm
     * tiệc cưới làm, thay vì từ chối nhận tiệc.
     */
    public int minimumTablesFor(Space space) {
        return (int) Math.ceil((double) space.getCapacityMin() / properties.guestsPerTable());
    }

    // Tên tiếng Anh của không gian, chưa dịch thì lùi về tên tiếng Việt
    private static String nameEn(Space space) {
        return space.getNameEn() == null || space.getNameEn().isBlank()
                ? space.getName() : space.getNameEn();
    }

    private int billedTablesFor(Space space, int guestTables, BangQuyTac rules) {
        int minimum = minimumTablesFor(space);
        if (guestTables >= minimum) {
            return guestTables;
        }
        rules.them(
                "%s nhận tối thiểu %d mâm, tiệc %d mâm của bạn vẫn tính theo %d mâm"
                        .formatted(space.getName(), minimum, guestTables, minimum),
                "%s has a %d-table minimum, so your %d tables are billed as %d"
                        .formatted(nameEn(space), minimum, guestTables, minimum));
        return minimum;
    }

    // 1 mâm 10 khách, dư mấy khách cũng tính thêm 1 mâm
    public int tableCountFor(int guestCount) {
        if (guestCount <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) guestCount / properties.guestsPerTable());
    }

    /*
     * Phí thuê không gian giảm dần theo tiền ăn.
     *
     * Mỗi không gian có một mức doanh thu tối thiểu = phí thuê x hệ số. Tiền ăn đạt mức đó
     * thì miễn phí thuê, chưa đạt thì trả phần còn thiếu theo tỉ lệ. Cách này giống mức
     * "minimum spend" các trung tâm tiệc đang dùng, và tránh được lỗi cũ: khách đặt 300
     * khách trả ít tiền hơn khách đặt 290 khách.
     */
    private BigDecimal spaceFeeFor(Space space, int guestCount, BigDecimal foodAmount, BangQuyTac rules) {
        BigDecimal rentalFee = space.getRentalFee();

        // Không gian tính theo chòi thì thuê bao nhiêu chòi trả bấy nhiêu, không có miễn giảm
        if ("HUT".equals(space.getFeeUnit()) && space.getUnitCapacity() != null && space.getUnitCapacity() > 0) {
            int units = (int) Math.ceil((double) guestCount / space.getUnitCapacity());
            rules.them("Thuê %d chòi cho %d khách".formatted(units, guestCount),
                    "%d huts hired for %d guests".formatted(units, guestCount));
            return money(rentalFee.multiply(BigDecimal.valueOf(units)));
        }

        BigDecimal minimumSpend = rentalFee.multiply(BigDecimal.valueOf(properties.minimumSpendMultiplier()));
        if (minimumSpend.signum() <= 0) {
            return money(rentalFee);
        }

        if (foodAmount.compareTo(minimumSpend) >= 0) {
            rules.them("Miễn phí thuê không gian do tiền ăn đạt %s".formatted(readable(minimumSpend)),
                    "Venue hire waived: food spend reaches %s".formatted(readableEn(minimumSpend)));
            return money(BigDecimal.ZERO);
        }

        // Còn thiếu bao nhiêu phần trăm doanh thu tối thiểu thì trả bấy nhiêu phần trăm phí thuê
        BigDecimal remainingRatio = BigDecimal.ONE.subtract(
                foodAmount.divide(minimumSpend, 4, ROUNDING));
        BigDecimal fee = money(rentalFee.multiply(remainingRatio));

        if (fee.compareTo(money(rentalFee)) < 0) {
            rules.them(
                    "Giảm phí thuê không gian, thêm %s tiền ăn nữa là được miễn phí"
                            .formatted(readable(minimumSpend.subtract(foodAmount))),
                    "Venue hire reduced; %s more in food and it is waived entirely"
                            .formatted(readableEn(minimumSpend.subtract(foodAmount))));
        }
        return fee;
    }

    /*
     * Giảm giá đặt sớm hoặc giảm giá dịp lễ, không cộng dồn.
     *
     * Tiệc vừa đặt sớm vừa rơi vào dịp lễ thì chỉ lấy mức cao hơn. Cộng dồn thì một tiệc
     * ngày Tết đặt trước hai tháng được giảm tới 25%, sâu hơn mức 20% chính sách cho phép.
     * Ngày xét dịp lễ là ngày tổ chức tiệc, tức ngày khách tới, không phải ngày đặt.
     */
    private BigDecimal discountFor(BigDecimal subtotal, LocalDate eventDate, BangQuyTac rules) {
        if (eventDate == null) {
            return money(BigDecimal.ZERO);
        }
        boolean earlyBird = ChronoUnit.DAYS.between(LocalDate.now(), eventDate) >= properties.earlyBirdDays();
        BigDecimal earlyBirdRate = earlyBird ? properties.earlyBirdRate() : BigDecimal.ZERO;
        Optional<HolidayDiscountLookup.AppliedHoliday> holiday = holidayDiscounts.find(eventDate);

        BigDecimal rate;
        if (holiday.isPresent() && holiday.get().rate().compareTo(earlyBirdRate) >= 0) {
            rate = holiday.get().rate();
            rules.them("Giảm %s%% dịp %s".formatted(percent(rate), holiday.get().name()),
                    "%s%% off for %s".formatted(percent(rate), NoiDungSongNgu.ngayLe(holiday.get().name())));
        } else if (earlyBird) {
            rate = earlyBirdRate;
            rules.them("Giảm %s%% do đặt trước %d ngày".formatted(percent(rate), properties.earlyBirdDays()),
                    "%s%% off for booking %d days ahead".formatted(percent(rate), properties.earlyBirdDays()));
        } else {
            return money(BigDecimal.ZERO);
        }

        if (earlyBird && holiday.isPresent()) {
            rules.them("Ưu đãi đặt sớm và ưu đãi dịp lễ không cộng dồn, áp dụng mức cao hơn",
                    "Early-booking and holiday offers do not stack; the higher one applies");
        }
        return money(subtotal.multiply(rate));
    }

    // 0.05 thành "5", 0.15 thành "15"
    private String percent(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, ROUNDING);
    }

    // Đổi 150000000 thành "150 triệu" cho dễ đọc trong câu thông báo
    private String readable(BigDecimal amount) {
        BigDecimal million = amount.divide(BigDecimal.valueOf(1_000_000), 1, ROUNDING);
        return million.stripTrailingZeros().toPlainString() + " triệu";
    }

    /*
     * Bản tiếng Anh của cách đọc số tiền.
     *
     * Tiếng Việt nói "30 triệu", tiếng Anh không có đơn vị tương đương nên viết thẳng
     * "30,000,000 VND". Dịch thành "30 million VND" nghe tự nhiên nhưng khách nước ngoài
     * đang cân nhắc chi tiêu thì cần con số đầy đủ để đối chiếu với bảng giá.
     */
    private String readableEn(BigDecimal amount) {
        return "%,d VND".formatted(amount.setScale(0, ROUNDING).longValue());
    }
}
