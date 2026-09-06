package vn.vuonsen.fnb.modules.dishorder;

// Khách nhận món bằng cách nào
public enum FulfillmentType {
    DELIVERY("Giao tận nhà"),
    DINE_IN("Đặt trước, tới ăn tại chỗ");

    private final String label;

    FulfillmentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
