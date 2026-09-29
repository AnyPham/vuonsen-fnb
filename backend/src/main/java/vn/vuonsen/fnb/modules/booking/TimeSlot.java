package vn.vuonsen.fnb.modules.booking;

// Buổi tổ chức. Các buổi không được trùng giờ nhau, giữa hai buổi chừa thời gian dọn dẹp
// và trải bàn lại. Mỗi không gian chỉ nhận một tiệc cho mỗi buổi.
public enum TimeSlot {
    // Giờ trong nhãn tiếng Anh viết theo kiểu 12 giờ sáng/chiều, người dùng tiếng Anh quen đọc như vậy
    MORNING("Buổi sáng (7h00 - 11h00)", "Morning (7:00 AM – 11:00 AM)", 4),
    NOON("Buổi trưa (11h30 - 16h30)", "Midday (11:30 AM – 4:30 PM)", 5),
    EVENING("Buổi tối (17h30 - 22h30)", "Evening (5:30 PM – 10:30 PM)", 5);

    private final String label;
    private final String labelEn;
    private final int durationHours;

    TimeSlot(String label, String labelEn, int durationHours) {
        this.label = label;
        this.labelEn = labelEn;
        this.durationHours = durationHours;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public int getDurationHours() {
        return durationHours;
    }
}
