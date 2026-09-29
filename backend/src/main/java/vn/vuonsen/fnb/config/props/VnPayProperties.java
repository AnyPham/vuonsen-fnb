package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * Cổng thanh toán VNPay.
 *
 * Để trống mã đơn vị thì coi như chưa mở cổng: website vẫn nhận đơn và vẫn thanh toán được
 * bằng mã VietQR, chỉ không hiện lựa chọn trả qua VNPay.
 *
 * Lấy mã thử nghiệm ở đâu: đăng ký tài khoản sandbox miễn phí tại sandbox.vnpayment.vn, hệ
 * thống sẽ cấp vnp_TmnCode và vnp_HashSecret. Dán hai giá trị đó vào biến môi trường
 * VNPAY_TMN_CODE và VNPAY_HASH_SECRET, tuyệt đối không ghi vào tệp cấu hình vì tệp nằm trong
 * kho mã nguồn.
 *
 * @param payUrl        địa chỉ trang thanh toán, mặc định trỏ vào môi trường thử nghiệm
 * @param returnUrl     nơi VNPay đưa khách quay lại sau khi trả xong
 * @param expireMinutes quá thời gian này mà chưa trả thì giao dịch hết hiệu lực
 */
@ConfigurationProperties(prefix = "app.vnpay")
public record VnPayProperties(
        String tmnCode,
        String hashSecret,
        String payUrl,
        String returnUrl,
        int expireMinutes
) {

    public boolean daCauHinh() {
        return tmnCode != null && !tmnCode.isBlank()
                && hashSecret != null && !hashSecret.isBlank();
    }
}
