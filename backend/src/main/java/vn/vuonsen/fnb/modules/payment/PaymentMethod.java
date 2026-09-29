package vn.vuonsen.fnb.modules.payment;

// Cách khách trả tiền
public enum PaymentMethod {
    CASH("Tiền mặt tại nhà hàng", "Cash at the restaurant"),
    TRANSFER("Chuyển khoản", "Bank transfer"),
    VIETQR("Chuyển khoản quét mã VietQR", "Bank transfer by VietQR code"),
    COD("Trả khi nhận món", "Pay on delivery"),
    VNPAY("Thanh toán qua cổng VNPay", "Payment through the VNPay gateway");

    private final String label;
    private final String labelEn;

    PaymentMethod(String label, String labelEn) {
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
