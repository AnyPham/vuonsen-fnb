package vn.vuonsen.fnb.modules.booking;

// Buổi tổ chức. Các buổi không được trùng giờ nhau, giữa hai buổi chừa thời gian dọn dẹp
// và trải bàn lại. Mỗi không gian chỉ nhận một tiệc cho mỗi buổi.
public enum TimeSlot {
    MORNING("Buổi sáng (7h00 - 11h00)", 4),
    NOON("Buổi trưa (11h30 - 16h30)", 5),
    EVENING("Buổi tối (17h30 - 22h30)", 5);

    private final String label;
    private final int durationHours;

    TimeSlot(String label, int durationHours) {
        this.label = label;
        this.durationHours = durationHours;
    }

    public String getLabel() {
        return label;
    }

    public int getDurationHours() {
        return durationHours;
    }
}
