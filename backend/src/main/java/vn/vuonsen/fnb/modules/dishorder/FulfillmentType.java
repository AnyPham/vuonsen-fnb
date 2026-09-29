package vn.vuonsen.fnb.modules.dishorder;

// Khách nhận món bằng cách nào
public enum FulfillmentType {
    DELIVERY("Giao tận nhà", "Home delivery"),
    DINE_IN("Đặt trước, tới ăn tại chỗ", "Order ahead, dine in");

    private final String label;
    private final String labelEn;

    FulfillmentType(String label, String labelEn) {
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
