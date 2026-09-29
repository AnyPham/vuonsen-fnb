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
    PENDING("Chờ xác nhận", "Awaiting confirmation"),
    CONFIRMED("Đã xác nhận", "Confirmed"),
    COMPLETED("Đã hoàn thành", "Completed"),
    CANCELLED("Đã hủy", "Cancelled");

    private final String label;
    private final String labelEn;

    DishOrderStatus(String label, String labelEn) {
        this.label = label;
        this.labelEn = labelEn;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelEn() {
        return labelEn;
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
