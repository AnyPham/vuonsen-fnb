package vn.vuonsen.fnb.modules.review;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.user.User;

import java.util.ArrayList;
import java.util.List;

// Đánh giá của khách, phải được duyệt mới hiện lên web
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "event_type", length = 40)
    private String eventType;

    @Column(nullable = false)
    @Builder.Default
    private boolean approved = false;

    /*
     * Ảnh khách chụp tại tiệc, gửi kèm đánh giá.
     *
     * Ảnh thật của khách thuyết phục người đọc hơn hẳn lời khen suông, nên cho phép
     * kèm nhiều ảnh chứ không chỉ một. Ảnh đi theo đánh giá: đánh giá bị xóa thì ảnh
     * xóa theo, và cũng chỉ hiện lên web sau khi đánh giá được duyệt.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "url", nullable = false, length = 500)
    @Builder.Default
    private List<String> images = new ArrayList<>();
}
