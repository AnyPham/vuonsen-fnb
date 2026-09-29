package vn.vuonsen.fnb.modules.payment;

// Khoản tiền này trả cho phần nào của đơn
public enum PaymentPurpose {
    DEPOSIT("Tiền cọc giữ ngày", "Deposit to hold the date"),
    BALANCE("Thanh toán phần còn lại", "Payment of the remaining balance"),
    FULL("Thanh toán toàn bộ đơn", "Payment of the whole order");

    private final String label;
    private final String labelEn;

    PaymentPurpose(String label, String labelEn) {
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
