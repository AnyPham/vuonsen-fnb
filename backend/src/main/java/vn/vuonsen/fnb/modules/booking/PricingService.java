package vn.vuonsen.fnb.modules.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// Tính giá tiệc:
// tổng = tiền ăn + phí không gian - giảm giá + VAT
@Service
@RequiredArgsConstructor
public class PricingService {

    // Tiền Việt không có số lẻ nên làm tròn về đồng
    private static final int MONEY_SCALE = 0;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BookingProperties properties;

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
            List<String> appliedRules
    ) {
    }

    public Quote calculate(Space space, PartyPackage partyPackage, int guestCount, LocalDate eventDate) {
        List<String> rules = new ArrayList<>();

        int guestTables = tableCountFor(guestCount);
        int tableCount = billedTablesFor(space, guestTables, rules);
        BigDecimal unitPrice = partyPackage.getPricePerTable();
        BigDecimal foodAmount = money(unitPrice.multiply(BigDecimal.valueOf(tableCount)));

        BigDecimal spaceFee = spaceFeeFor(space, guestCount, foodAmount, rules);
        BigDecimal subtotal = foodAmount.add(spaceFee);

        BigDecimal discount = earlyBirdDiscountFor(subtotal, eventDate, rules);
        BigDecimal taxable = subtotal.subtract(discount);

        BigDecimal vatRate = properties.vatRate();
        BigDecimal vatAmount = money(taxable.multiply(vatRate));
        BigDecimal total = money(taxable.add(vatAmount));
        BigDecimal deposit = money(total.multiply(properties.depositRate()));

        return new Quote(guestCount, tableCount, unitPrice, foodAmount, spaceFee,
                discount, vatRate, vatAmount, total, deposit, rules);
    }

    /*
     * Mỗi không gian nhận đặt tối thiểu một số mâm, suy ra từ sức chứa tối thiểu.
     * Khách mời ít hơn vẫn tính tiền theo mức tối thiểu, giống cách các trung tâm
     * tiệc cưới làm, thay vì từ chối nhận tiệc.
     */
    public int minimumTablesFor(Space space) {
        return (int) Math.ceil((double) space.getCapacityMin() / properties.guestsPerTable());
    }

    private int billedTablesFor(Space space, int guestTables, List<String> rules) {
        int minimum = minimumTablesFor(space);
        if (guestTables >= minimum) {
            return guestTables;
        }
        rules.add("%s nhận tối thiểu %d mâm, tiệc %d mâm của bạn vẫn tính theo %d mâm"
                .formatted(space.getName(), minimum, guestTables, minimum));
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
    private BigDecimal spaceFeeFor(Space space, int guestCount, BigDecimal foodAmount, List<String> rules) {
        BigDecimal rentalFee = space.getRentalFee();

        // Không gian tính theo chòi thì thuê bao nhiêu chòi trả bấy nhiêu, không có miễn giảm
        if ("HUT".equals(space.getFeeUnit()) && space.getUnitCapacity() != null && space.getUnitCapacity() > 0) {
            int units = (int) Math.ceil((double) guestCount / space.getUnitCapacity());
            rules.add("Thuê %d chòi cho %d khách".formatted(units, guestCount));
            return money(rentalFee.multiply(BigDecimal.valueOf(units)));
        }

        BigDecimal minimumSpend = rentalFee.multiply(BigDecimal.valueOf(properties.minimumSpendMultiplier()));
        if (minimumSpend.signum() <= 0) {
            return money(rentalFee);
        }

        if (foodAmount.compareTo(minimumSpend) >= 0) {
            rules.add("Miễn phí thuê không gian do tiền ăn đạt %s".formatted(readable(minimumSpend)));
            return money(BigDecimal.ZERO);
        }

        // Còn thiếu bao nhiêu phần trăm doanh thu tối thiểu thì trả bấy nhiêu phần trăm phí thuê
        BigDecimal remainingRatio = BigDecimal.ONE.subtract(
                foodAmount.divide(minimumSpend, 4, ROUNDING));
        BigDecimal fee = money(rentalFee.multiply(remainingRatio));

        if (fee.compareTo(money(rentalFee)) < 0) {
            rules.add("Giảm phí thuê không gian, thêm %s tiền ăn nữa là được miễn phí"
                    .formatted(readable(minimumSpend.subtract(foodAmount))));
        }
        return fee;
    }

    private BigDecimal earlyBirdDiscountFor(BigDecimal subtotal, LocalDate eventDate, List<String> rules) {
        if (eventDate == null) {
            return money(BigDecimal.ZERO);
        }
        long daysAhead = ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        if (daysAhead < properties.earlyBirdDays()) {
            return money(BigDecimal.ZERO);
        }
        rules.add("Giảm %s%% do đặt trước %d ngày"
                .formatted(properties.earlyBirdRate().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString(),
                        properties.earlyBirdDays()));
        return money(subtotal.multiply(properties.earlyBirdRate()));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, ROUNDING);
    }

    // Đổi 150000000 thành "150 triệu" cho dễ đọc trong câu thông báo
    private String readable(BigDecimal amount) {
        BigDecimal million = amount.divide(BigDecimal.valueOf(1_000_000), 1, ROUNDING);
        return million.stripTrailingZeros().toPlainString() + " triệu";
    }
}
