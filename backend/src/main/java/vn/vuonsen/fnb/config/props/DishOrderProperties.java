package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/*
 * Quy định nhận đơn đặt món lẻ, đọc từ mục app.dish-order trong application.yml.
 *
 * Để ở tệp cấu hình chứ không viết cứng vì đây là những con số bộ phận kinh doanh
 * hay đổi: phí giao hàng theo giá xăng, mức miễn phí giao theo chính sách khuyến mãi,
 * đơn tối thiểu theo mùa vắng khách.
 */
@ConfigurationProperties(prefix = "app.dish-order")
public record DishOrderProperties(
        BigDecimal deliveryFee,

        // Đơn từ mức này trở lên được miễn phí giao
        BigDecimal freeDeliveryFrom,

        // Đơn giao tận nhà phải đạt mức tối thiểu mới nhận, vì đi giao một phần nhỏ
        // thì tiền giao còn hơn tiền món
        BigDecimal minDeliveryAmount,

        /*
         * Số giờ tối thiểu phải báo trước.
         *
         * Đây là mức sàn chung. Thời gian thật còn phụ thuộc món nào lâu nhất trong
         * đơn: đơn có heo quay cần 3 tiếng thì không thể nhận nếu khách đặt trước 2
         * tiếng, dù mức sàn cho phép.
         */
        int minHoursAhead,

        // Không nhận đơn quá xa vì nguyên liệu tươi không giữ được lâu
        int maxDaysAhead,

        int minGuestsDineIn,
        int maxGuestsDineIn
) {
}
