package vn.vuonsen.fnb.modules.booking;

// Buổi tổ chức. Mỗi không gian chỉ nhận 1 tiệc cho mỗi buổi.
public enum TimeSlot {
    MORNING("Buổi sáng (8h - 12h)"),
    NOON("Buổi trưa (11h - 15h)"),
    EVENING("Buổi tối (17h - 22h)");

    private final String label;

    TimeSlot(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
