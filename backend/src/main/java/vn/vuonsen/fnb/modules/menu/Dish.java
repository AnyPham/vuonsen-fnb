package vn.vuonsen.fnb.modules.menu;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Món ăn trong thực đơn
@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private DishCategory category;

    @Column(nullable = false, length = 160)
    private String name;

    // Dùng làm đường dẫn trang chi tiết món, ví dụ /thuc-don/ga-ta-hap-la-chanh
    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(length = 500)
    private String description;

    // Để null với món tính giá linh hoạt, khi đó hiện priceNote (ví dụ "Theo cân")
    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "price_note", length = 60)
    private String priceNote;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "best_seller", nullable = false)
    @Builder.Default
    private boolean bestSeller = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // ---------------- Nội dung cho trang chi tiết món ----------------

    @Column(length = 1000)
    private String ingredients;

    @Column(length = 1500)
    private String preparation;

    /*
     * Những điều khách nên biết trước khi đặt: món nào phải báo trước, món nào cay,
     * món nào còn xương, món nào có đồ dễ gây dị ứng.
     *
     * Viết thẳng những điểm trừ chứ không chỉ khen, vì khách biết trước thì bớt gọi
     * điện hỏi, mà cũng bớt chuyện đặt xong mới phát hiện không ăn được.
     */
    @Column(name = "order_note", length = 800)
    private String orderNote;

    // Khẩu phần, ví dụ "Phần 3-4 người" hoặc "Nồi 4-6 người"
    @Column(name = "portion_desc", length = 120)
    private String portionDesc;

    // Thời gian bếp cần để làm xong, dùng để cảnh báo món đặt sát giờ
    @Column(name = "prep_minutes")
    private Integer prepMinutes;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "dish_images", joinColumns = @JoinColumn(name = "dish_id"))
    @OrderColumn(name = "sort_order")
    @Builder.Default
    private List<DishImage> images = new ArrayList<>();
}
