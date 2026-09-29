package vn.vuonsen.fnb.modules.holiday;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vuonsen.fnb.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * Một dịp lễ được giảm giá, ví dụ Tết Nguyên Đán 2027 từ 06/02 đến 08/02 giảm 20%.
 *
 * Mỗi dòng là một khoảng ngày dương lịch của một năm cụ thể. Lễ âm lịch mỗi năm rơi vào
 * ngày dương khác nhau, nên sang năm mới quản trị thêm dòng mới chứ không sửa dòng cũ.
 */
@Entity
@Table(name = "holiday_discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayDiscount extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // 0.15 nghĩa là giảm 15%
    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal discountRate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
