package vn.vuonsen.fnb.modules.space;

// Loại không gian, dùng cho bộ lọc ngoài giao diện
public enum SpaceType {
    OUTDOOR("Ngoài trời"),
    INDOOR("Trong nhà"),
    PRIVATE("Riêng tư"),
    CONFERENCE("Hội nghị"),
    HUT("Chòi lá"),
    GARDEN("Sự kiện nhỏ");

    private final String label;

    SpaceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
