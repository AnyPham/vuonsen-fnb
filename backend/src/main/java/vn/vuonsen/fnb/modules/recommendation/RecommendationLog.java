package vn.vuonsen.fnb.modules.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Ghi lại mỗi lần hệ thống gợi ý và khách có chọn theo gợi ý hay không.
// Nhờ bảng này mới đo được độ chính xác của hệ thống gợi ý trong chương thực nghiệm.
@Entity
@Table(name = "recommendation_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 40)
    private EventType eventType;

    @Column(precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_space_id")
    private Space suggestedSpace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_package_id")
    private PartyPackage suggestedPackage;

    @Column(precision = 6, scale = 2)
    private BigDecimal score;

    // Bật lên khi khách đặt tiệc đúng tổ hợp mà hệ thống đã gợi ý
    @Column(nullable = false)
    @Builder.Default
    private boolean accepted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
