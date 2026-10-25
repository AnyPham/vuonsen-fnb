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
import java.time.LocalDateTime;

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

    // Để trống khi khách chỉ thuê không gian, không kèm gói tiệc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
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

    /*
     * Món khách tự chọn, chỉ có với tiệc nhỏ không lấy gói cố định.
     * Xóa đơn thì xóa luôn danh sách món, vì chúng không có nghĩa khi đứng một mình.
     */
    @jakarta.persistence.OneToMany(mappedBy = "booking",
            cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private java.util.List<BookingDish> monTuChon = new java.util.ArrayList<>();

    @Column(length = 1000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    /*
     * Tiền đặt cọc.
     *
     * Chưa nối cổng thanh toán nên khách vẫn chuyển khoản hoặc đóng tiền mặt, quản trị ghi
     * nhận lại bằng tay. depositAmount là khoản phải đóng, chốt lúc đặt nên đổi tỉ lệ cọc
     * về sau không làm thay đổi đơn cũ; depositPaid là khoản thật sự đã thu.
     */
    @Column(name = "deposit_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "deposit_paid", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal depositPaid = BigDecimal.ZERO;

    @Column(name = "deposit_paid_at")
    private LocalDateTime depositPaidAt;

    /*
     * Thời điểm hết hạn giữ chỗ tạm. NULL là không giữ chỗ.
     *
     * Đặt khi khách vừa gửi đơn, để buổi đó không ai đặt được trong lúc khách đi trả cọc.
     * Không cần tác vụ nền quét đơn hết hạn: mọi phép kiểm đều so mốc này với giờ hiện
     * tại, nên hết hạn là tự động hết tác dụng.
     */
    @Column(name = "giu_cho_den")
    private LocalDateTime giuChoDen;

    /*
     * Đơn này có đang chiếm buổi đó hay không.
     *
     * Ba trường hợp chiếm chỗ:
     *   - đã xác nhận, tức nhà hàng đã nhận đơn
     *   - đã trả cọc, dù nhân viên chưa kịp xác nhận. Khách trả tiền rồi mà vẫn để người
     *     khác giành mất chỗ thì không chấp nhận được
     *   - đang trong thời gian giữ chỗ tạm, tức vừa gửi đơn và còn hạn trả cọc
     *
     * Gom luật vào một chỗ duy nhất, vì cùng câu hỏi này được hỏi ở ba nơi: lúc tạo đơn,
     * lúc nhân viên xác nhận, và lúc trang đặt tiệc hiện nhãn buổi nào đã kín.
     */
    public boolean dangChiemCho() {
        if (status == BookingStatus.CONFIRMED) {
            return true;
        }
        if (status != BookingStatus.PENDING) {
            return false;
        }
        boolean daCoc = depositPaid != null && depositPaid.signum() > 0;
        boolean conHanGiuCho = giuChoDen != null && giuChoDen.isAfter(LocalDateTime.now());
        return daCoc || conHanGiuCho;
    }

    // CASH hoặc TRANSFER
    /*
     * Khoản hoàn lại khi hủy, chốt tại thời điểm hủy.
     *
     * Rỗng nghĩa là đơn chưa hủy, hoặc hủy khi chưa đóng đồng cọc nào. Chép cả tỉ lệ lẫn
     * số tiền để về sau đối chiếu được, vì nhà hàng có thể đổi chính sách hoàn tiền.
     */
    @Column(name = "refund_rate", precision = 5, scale = 2)
    private BigDecimal refundRate;

    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_note", length = 255)
    private String refundNote;

    @Column(name = "deposit_method", length = 20)
    private String depositMethod;

    /** Nhãn dùng thay tên gói ở mọi nơi hiển thị, khi đơn chỉ thuê không gian. */
    public static final String KHONG_KEM_GOI = "Chỉ thuê không gian";

    /** Tên gói để hiển thị. Đơn không kèm gói thì trả nhãn chỉ thuê không gian. */
    public String tenGoiHienThi() {
        return partyPackage == null ? KHONG_KEM_GOI : partyPackage.getName();
    }
}
