package vn.vuonsen.fnb.modules.payment;

// Phiếu thu thuộc loại đơn nào
public enum OrderType {
    BOOKING("Đơn đặt tiệc", "Event booking"),
    DISH_ORDER("Đơn đặt món", "Food order");

    private final String label;
    private final String labelEn;

    OrderType(String label, String labelEn) {
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
