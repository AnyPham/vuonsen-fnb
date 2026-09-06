package vn.vuonsen.fnb.modules.dishorder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import vn.vuonsen.fnb.modules.menu.Dish;

import java.math.BigDecimal;

/*
 * Một dòng món trong đơn đặt món.
 *
 * Tên món và đơn giá được chép lại vào đây chứ không chỉ giữ khóa ngoại sang bảng
 * dishes. Bảng giá thay đổi theo thời gian, mà hóa đơn cũ phải giữ đúng con số đã
 * chốt với khách. Nếu đọc giá qua khóa ngoại thì hôm sau tăng giá là đơn cũ tự đổi
 * theo, khách tra cứu lại sẽ thấy khác số tiền đã trả.
 *
 * Khóa ngoại vẫn giữ để biết đơn này gọi món nào, phục vụ thống kê món bán chạy.
 * Món bị xóa hẳn thì khóa ngoại thành null nhưng tên món vẫn còn, đơn không mất dữ liệu.
 */
@Entity
@Table(name = "dish_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private DishOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Column(name = "dish_name", nullable = false, length = 160)
    private String dishName;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;
}
