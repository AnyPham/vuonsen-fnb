package vn.vuonsen.fnb.modules.booking;

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
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;

// Đơn đặt tiệc.
// Các cột tiền lưu lại giá lúc đặt, sau này nhà hàng tăng giá thì đơn cũ không đổi.
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // Null khi khách đặt mà không đăng nhập
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private PartyPackage partyPackage;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20)
    private TimeSlot timeSlot;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "table_count", nullable = false)
    private Integer tableCount;

    // ---- Bang ke chi phi ----
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "food_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal foodAmount;

    @Column(name = "space_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal spaceFee;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal vatRate;

    @Column(name = "vat_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // ---- Thong tin lien he ----
    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 160)
    private String customerEmail;

    @Column(length = 1000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
}
