package vn.vuonsen.fnb.modules.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Một lần thu tiền của một đơn.
 *
 * Đơn có thể thu làm nhiều lần: cọc trước, phần còn lại thanh toán sau khi tiệc xong. Mỗi
 * lần là một dòng ở đây, nên mở đơn ra là thấy đủ lịch sử tiền nong chứ không chỉ một con số.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    // Đúng một trong hai trường dưới đây có giá trị, tùy orderType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_order_id")
    private DishOrder dishOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentPurpose purpose;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // Nội dung chuyển khoản hoặc mã giao dịch ngân hàng, dùng khi đối soát sao kê
    @Column(length = 120)
    private String reference;

    @Column(length = 500)
    private String note;

    @Column(name = "confirmed_by", length = 160)
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /** Mã đơn mà phiếu thu này thuộc về, dùng để hiển thị mà không phải nạp cả đơn. */
    public String maDon() {
        if (orderType == OrderType.BOOKING) {
            return booking == null ? null : booking.getCode();
        }
        return dishOrder == null ? null : dishOrder.getCode();
    }
}
