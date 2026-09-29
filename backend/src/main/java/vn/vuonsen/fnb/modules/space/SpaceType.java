package vn.vuonsen.fnb.modules.space;

// Loại không gian, dùng cho bộ lọc ngoài giao diện
public enum SpaceType {
    OUTDOOR("Ngoài trời", "Outdoor"),
    INDOOR("Trong nhà", "Indoor"),
    PRIVATE("Riêng tư", "Private"),
    CONFERENCE("Hội nghị", "Conference"),
    HUT("Chòi lá", "Thatched hut"),
    GARDEN("Sự kiện nhỏ", "Small events");

    private final String label;
    private final String labelEn;

    SpaceType(String label, String labelEn) {
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
