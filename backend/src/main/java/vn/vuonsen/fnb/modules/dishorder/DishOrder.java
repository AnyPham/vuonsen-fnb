package vn.vuonsen.fnb.modules.dishorder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;
import vn.vuonsen.fnb.modules.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Đơn đặt món lẻ: khách tự chọn từng món thay vì lấy trọn một gói tiệc.
 *
 * Hai hình thức nhận món dùng chung một bảng vì phần lớn dữ liệu giống nhau, chỉ
 * khác vài trường: đơn giao tận nhà cần địa chỉ, đơn ăn tại chỗ cần số khách.
 * Tách thành hai bảng thì phải nhân đôi toàn bộ phần thông tin khách và tiền bạc.
 */
@Entity
@Table(name = "dish_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishOrder extends BaseEntity {

    @Column(nullable = false, length = 30, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, length = 20)
    private FulfillmentType fulfillmentType;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 160)
    private String customerEmail;

    // Chỉ đơn giao tận nhà mới có
    @Column(name = "delivery_address", length = 400)
    private String deliveryAddress;

    // Chỉ đơn ăn tại chỗ mới có
    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "serve_at", nullable = false)
    private LocalDateTime serveAt;

    @Column(length = 600)
    private String note;

    /*
     * Tiền chốt tại thời điểm đặt.
     *
     * Lưu con số thay vì tính lại mỗi lần đọc đơn, vì bảng giá và phí giao hàng có
     * thể đổi. Đơn đã gửi phải giữ nguyên số tiền đã báo với khách.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    // Tiền giảm dịp lễ trên tiền món
    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "vat_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal vatAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DishOrderStatus status = DishOrderStatus.PENDING;

    /*
     * Tiền đã thu của đơn này và cách khách chọn trả.
     *
     * Đơn trả khi nhận thì lúc đặt chưa thu đồng nào, tới lúc giao mới thu; đơn chuyển khoản
     * thì thu trước. Giữ lại đã thu bao nhiêu để màn hình đơn trả lời được ngay, còn chi tiết
     * từng lần thu nằm ở sổ thanh toán.
     */
    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    // Khách không cần tài khoản vẫn đặt được, nên trường này để trống được
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /*
     * Các dòng món trong đơn.
     *
     * Để cascade ALL và orphanRemoval vì dòng món không sống tách khỏi đơn: xóa đơn
     * thì dòng món cũng không còn ý nghĩa gì.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("id")
    @Builder.Default
    private List<DishOrderItem> items = new ArrayList<>();

    // Thêm dòng món và giữ hai chiều quan hệ đồng bộ
    public void themMon(DishOrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
