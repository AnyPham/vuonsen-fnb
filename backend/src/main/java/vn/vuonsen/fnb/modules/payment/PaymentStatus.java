package vn.vuonsen.fnb.modules.payment;

/*
 * Trạng thái một phiếu thu.
 *
 * Khách quét mã chuyển khoản xong thì phiếu ở trạng thái chờ đối soát, vì hệ thống không nối
 * với ngân hàng nên không tự biết tiền đã về hay chưa. Nhân viên mở sao kê thấy tiền thì mới
 * xác nhận. Cách này chậm hơn cổng thanh toán tự động nhưng không sai số tiền bao giờ.
 */
public enum PaymentStatus {
    PENDING("Chờ đối soát", "Awaiting reconciliation"),
    CONFIRMED("Đã nhận tiền", "Received"),
    CANCELLED("Đã hủy", "Cancelled");

    private final String label;
    private final String labelEn;

    PaymentStatus(String label, String labelEn) {
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
