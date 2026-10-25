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
        int fullDayPackageHours,

        /*
         * Mức hoàn tiền cọc khi khách hủy, theo số ngày còn lại tới ngày tổ chức.
         *
         * Để trong cấu hình chứ không ghi cứng trong mã: đây là điều khoản thương mại,
         * nhà hàng đổi chính sách thì sửa một dòng cấu hình, không phải sửa mã rồi dựng lại.
         */
        int refundFullDays,
        int refundHalfDays,

        /*
         * Số phút giữ chỗ tạm cho đơn vừa gửi, trong lúc khách đi trả tiền cọc.
         *
         * Hết khoảng này mà chưa có cọc thì buổi đó trống lại cho người khác.
         *
         * Đặt bao nhiêu là một đánh đổi: ngắn quá thì khách chưa kịp mở ứng dụng ngân
         * hàng đã mất chỗ; dài quá thì một người bỏ ngang chặn người khác quá lâu.
         */
        int holdMinutes
) {
}
