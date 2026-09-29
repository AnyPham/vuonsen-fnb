package vn.vuonsen.fnb.modules.payment.dto;

import java.math.BigDecimal;
import java.util.List;

/*
 * Toàn cảnh thanh toán của một đơn, gói trong một lần gọi.
 *
 * Trang thanh toán cần đủ ba thứ cùng lúc: đơn này bao nhiêu tiền và đã trả tới đâu, phiếu
 * thu đang chờ đối soát nếu có, và lịch sử các khoản đã thu. Tách ra nhiều đường dẫn thì
 * giao diện phải gọi ba lần mà vẫn phải chờ đủ cả ba mới vẽ được.
 *
 * @param maDon        mã đơn đặt tiệc hoặc đơn đặt món
 * @param tongTien     tổng tiền của đơn
 * @param canCoc       khoản cọc phải đóng, đơn đặt món thì bằng 0 vì trả một lần
 * @param daThu        tổng tiền đã xác nhận nhận được
 * @param conPhaiTra   tổng tiền trừ đã thu
 * @param trangThai    CHUA_TRA / DA_COC / DA_TRA_DU
 * @param phieuChoDoiSoat phiếu khách vừa tạo, đang chờ nhân viên đối soát sao kê
 */
public record TinhHinhThanhToanResponse(
        String maDon,
        String loaiDon,
        String loaiDonEn,
        String tenKhach,
        BigDecimal tongTien,
        BigDecimal canCoc,
        BigDecimal daThu,
        BigDecimal conPhaiTra,
        /*
         * Mã trạng thái: CHUA_TRA / COC_THIEU / DA_COC / DA_TRA_DU.
         *
         * Giao diện dịch từ mã này chứ không dịch từ trangThaiLabel, nên không cần thêm
         * cột nhãn tiếng Anh ở đây. Nhãn tiếng Việt vẫn giữ cho các chỗ gọi cũ.
         */
        String trangThai,
        String trangThaiLabel,
        PaymentResponse phieuChoDoiSoat,
        List<PaymentResponse> lichSu,
        ThongTinChuyenKhoan chuyenKhoan
) {

    /**
     * Thông tin để khách chuyển khoản. Chưa cấu hình tài khoản thì trả về null ở chỗ này,
     * giao diện nhìn vào đó để chuyển sang nhắc khách gọi điện cho nhà hàng.
     *
     * @param chuoiQr chuỗi VietQR đem vẽ thành ảnh QR ở giao diện
     */
    public record ThongTinChuyenKhoan(
            String nganHang,
            String soTaiKhoan,
            String tenChuTaiKhoan,
            BigDecimal soTien,
            String noiDung,
            String chuoiQr,

            /** Đang dùng tài khoản thử, giao diện phải cảnh báo đừng chuyển tiền thật. */
            boolean chayThu
    ) {
    }
}
