package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

// Các tham số tính giá, đọc từ mục app.booking trong application.yml
@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(
        int guestsPerTable,
        BigDecimal vatRate,
        int freeSpaceFromTables,
        int earlyBirdDays,
        BigDecimal earlyBirdRate,
        int minGuests,
        int maxGuests
) {
}
