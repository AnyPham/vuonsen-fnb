package vn.vuonsen.fnb.modules.dishorder;

import java.util.EnumSet;
import java.util.Set;

/*
 * Trạng thái đơn đặt món.
 *
 * Giữ đúng bốn bước như đơn đặt tiệc để nhân viên không phải nhớ hai bộ quy tắc
 * khác nhau cho hai loại đơn.
 */
public enum DishOrderStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String label;

    DishOrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Set<DishOrderStatus> allowedTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(DishOrderStatus.class);
        };
    }

    public boolean canTransitionTo(DishOrderStatus target) {
        return allowedTransitions().contains(target);
    }
}
