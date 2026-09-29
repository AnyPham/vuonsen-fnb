package vn.vuonsen.fnb.modules.booking;

// Loại sự kiện, chọn ở bước 1 của form đặt tiệc
public enum EventType {
    WEDDING("Tiệc cưới", "Wedding"),
    CORPORATE("Tiệc công ty / hội nghị", "Corporate event / conference"),
    BIRTHDAY("Sinh nhật / thôi nôi", "Birthday / first-birthday party"),
    FAMILY("Họp mặt gia đình / giỗ", "Family gathering / ancestral anniversary"),
    OTHER("Khác", "Other");

    private final String label;
    private final String labelEn;

    EventType(String label, String labelEn) {
        this.label = label;
        this.labelEn = labelEn;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelEn() {
        return labelEn;
    }
}
