package vn.vuonsen.fnb.modules.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
 * Một đăng ký nhận thông báo đẩy của trình duyệt.
 *
 * Không gắn với tài khoản: khách vãng lai cũng đăng ký nhận khuyến mãi được mà không cần
 * tạo tài khoản. Định danh là endpoint do dịch vụ push của hãng trình duyệt cấp.
 */
@Entity
@Table(name = "push_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String endpoint;

    /* Hai khóa do trình duyệt sinh, dùng để mã hóa nội dung thông báo cho riêng máy đó. */
    @Column(nullable = false)
    private String p256dh;

    @Column(nullable = false)
    private String auth;

    @Column(name = "ngon_ngu", nullable = false, length = 8)
    private String ngonNgu;

    @Column(name = "tao_luc", nullable = false)
    private LocalDateTime taoLuc;

    /* Lần gần nhất dịch vụ push từ chối. Dùng để dọn các đăng ký đã chết. */
    @Column(name = "gui_loi_luc")
    private LocalDateTime guiLoiLuc;
}
