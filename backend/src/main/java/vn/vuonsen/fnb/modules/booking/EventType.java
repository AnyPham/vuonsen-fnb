package vn.vuonsen.fnb.modules.booking;

// Loại sự kiện, chọn ở bước 1 của form đặt tiệc
public enum EventType {
    WEDDING("Tiệc cưới"),
    CORPORATE("Tiệc công ty / hội nghị"),
    BIRTHDAY("Sinh nhật / thôi nôi"),
    FAMILY("Họp mặt gia đình / giỗ"),
    OTHER("Khác");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
