package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Tài khoản ngân hàng nhận tiền và cách hiển thị mã VietQR.
 *
 * Để trống số tài khoản thì hệ thống tự hiểu là chưa cấu hình: giao diện vẫn cho đặt đơn
 * nhưng không hiện mã QR, thay vào đó nhắc khách liên hệ nhà hàng. Cách này an toàn hơn
 * ghi sẵn một số tài khoản giả, vì số tài khoản giả có thể trùng tài khoản của người khác.
 *
 * @param bankBin      mã ngân hàng 6 chữ số do Napas cấp, ví dụ 970422 là MB, 970415 là VietinBank
 * @param accountNumber số tài khoản thụ hưởng
 * @param accountName  tên chủ tài khoản, hiện cho khách đối chiếu trước khi chuyển
 * @param bankName     tên ngân hàng để hiển thị
 * @param depositRate  tỉ lệ cọc, lấy lại từ cấu hình đặt tiệc để hai nơi không lệch nhau
 */
@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
        String bankBin,
        String accountNumber,
        String accountName,
        String bankName,
        boolean sandbox
) {

    /*
     * Số tài khoản dùng khi chạy thử.
     *
     * Toàn số 0 nên chắc chắn không phải tài khoản của ai: ứng dụng ngân hàng quét mã sẽ
     * báo tài khoản không tồn tại chứ không chuyển nhầm tiền cho người lạ. Đúng thứ cần cho
     * một bản chạy thử: mã QR vẫn dựng được, luồng vẫn đi hết, mà không ai mất tiền.
     */
    private static final String TAI_KHOAN_THU = "0000000000";

    /** Có đủ thông tin để dựng mã QR hay chưa. Chế độ thử thì luôn có. */
    public boolean daCauHinh() {
        return sandbox || (bankBin != null && !bankBin.isBlank()
                && accountNumber != null && !accountNumber.isBlank());
    }

    /** Số tài khoản đem vào mã QR: tài khoản thật nếu đã khai, chưa khai thì lấy số thử. */
    public String soTaiKhoanHieuLuc() {
        return accountNumber == null || accountNumber.isBlank() ? TAI_KHOAN_THU : accountNumber;
    }

    public String tenChuTaiKhoanHieuLuc() {
        String ten = accountName == null || accountName.isBlank() ? "VUON SEN" : accountName;
        return sandbox && (accountNumber == null || accountNumber.isBlank())
                ? ten + " (tài khoản thử nghiệm)"
                : ten;
    }

    /** Đang chạy bằng tài khoản thử chứ không phải tài khoản thật của nhà hàng. */
    public boolean dangChayThu() {
        return sandbox && (accountNumber == null || accountNumber.isBlank());
    }
}
