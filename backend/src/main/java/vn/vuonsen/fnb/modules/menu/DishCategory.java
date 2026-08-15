package vn.vuonsen.fnb.modules.menu;

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

// Danh mục món ăn: khai vị, món chính, lẩu & nướng, tráng miệng, đồ uống
@Entity
@Table(name = "dish_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
