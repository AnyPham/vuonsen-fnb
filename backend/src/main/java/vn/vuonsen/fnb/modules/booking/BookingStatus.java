package vn.vuonsen.fnb.modules.booking;

import java.util.EnumSet;
import java.util.Set;

// Trạng thái đơn: chờ xác nhận -> đã xác nhận -> hoàn thành (hoặc bị hủy)
public enum BookingStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Từ trạng thái hiện tại được phép chuyển sang những trạng thái nào
    public Set<BookingStatus> allowedTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(BookingStatus.class);
        };
    }

    public boolean canTransitionTo(BookingStatus target) {
        return allowedTransitions().contains(target);
    }

    // Đơn đã xác nhận thì chiếm chỗ, không cho đơn khác đặt trùng
    public boolean occupiesSlot() {
        return this == CONFIRMED;
    }
}
