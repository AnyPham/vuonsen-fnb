package vn.vuonsen.fnb.modules.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;

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
}
