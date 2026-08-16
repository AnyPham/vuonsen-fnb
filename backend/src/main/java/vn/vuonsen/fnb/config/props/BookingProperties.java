package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

// Các tham số tính giá và quy định nhận tiệc, đọc từ mục app.booking trong application.yml
@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(
        int guestsPerTable,
        BigDecimal vatRate,

        // Tiền ăn đạt (phí thuê x hệ số này) thì được miễn phí thuê không gian.
        // Chưa đạt thì giảm theo tỉ lệ, không giảm đột ngột.
        int minimumSpendMultiplier,

        int earlyBirdDays,
        BigDecimal earlyBirdRate,

        // Tỉ lệ đặt cọc để giữ ngày
        BigDecimal depositRate,

        int minGuests,
        int maxGuests,

        // Số ngày phải báo trước
        int minDaysAhead,
        int largePartyTables,
        int largePartyMinDays,

        // Gói tiệc từ số giờ này trở lên được tính là thuê trọn ngày
        int fullDayPackageHours
) {
}
