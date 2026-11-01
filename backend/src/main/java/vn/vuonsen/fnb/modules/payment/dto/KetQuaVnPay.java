package vn.vuonsen.fnb.modules.payment.dto;

import vn.vuonsen.fnb.modules.payment.Payment;

/*
 * Kết quả xử lý một báo cáo của VNPay.
 *
 * Mã phản hồi và câu thông báo lấy nguyên văn từ tài liệu của VNPay, vì máy của họ đọc mã này
 * để quyết định có gọi lại không: 00 và 02 là dừng, các mã còn lại sẽ bị gọi lại tối đa
 * 10 lần, mỗi lần cách 5 phút.
 */
public record KetQuaVnPay(String rspCode, String message, PaymentResponse phieu) {

    public static final String THANH_CONG = "00";
    public static final String KHONG_THAY_PHIEU = "01";
    public static final String DA_XU_LY = "02";
    public static final String SAI_SO_TIEN = "04";
    public static final String SAI_CHU_KY = "97";
    public static final String LOI_KHAC = "99";

    public static KetQuaVnPay of(String rspCode, String message) {
        return new KetQuaVnPay(rspCode, message, null);
    }

    public static KetQuaVnPay of(String rspCode, String message, Payment phieu) {
        return new KetQuaVnPay(rspCode, message, PaymentResponse.from(phieu));
    }
}
