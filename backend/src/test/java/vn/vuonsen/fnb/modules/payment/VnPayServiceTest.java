package vn.vuonsen.fnb.modules.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.config.props.VnPayProperties;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử phần ký và đọc tham số của cổng VNPay.
 *
 * Hai chữ ký mẫu dưới đây không do chính mã Java sinh ra mà tính bằng module crypto của
 * Node.js trên chuỗi đã mã hóa theo tài liệu VNPay. Nếu lấy chữ ký từ chính hàm đang kiểm thì
 * hàm có sai quy tắc nối chuỗi hay mã hóa, bài kiểm vẫn đạt, vì nó chỉ so hàm với chính nó.
 */
@DisplayName("Chữ ký và mã giao dịch VNPay")
class VnPayServiceTest {

    private static final String KHOA = "KHOABIMATCHOKIEMTHU";

    /** HMAC-SHA512 của chuỗi vnp_Amount=15000000&vnp_OrderInfo=Thanh+toan+don+BK+1&... */
    private static final String CHU_KY_MAU =
            "16a311fd3ad1b4a7296909524bcb19e83187f3a0ab6326b943597d9a047199df"
                    + "7077699a62300cdbde7a41e5933fb01bb0a9122c43de6de32c06d5e0998c05e5";

    /** HMAC-SHA512 của chuỗi vnp_OrderInfo=Don+so+1%2F2, để kiểm cách mã hóa ký tự đặc biệt. */
    private static final String CHU_KY_MA_HOA =
            "5a7c40183c7f839715d5bd587e3928ba9ee7f834d06b4869e41287d4322449169"
                    + "c4ba5fc02d79e91cb0d88ace1946c96698f10c38ef04f68ed0948d0bd7612d8";

    private final VnPayService vnPay = new VnPayService(new VnPayProperties(
            "KIEMTHU1", KHOA, "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
            "http://localhost:5173/thanh-toan/ket-qua", 15));

    /* Không có tham số chữ ký, thứ tự trong Map cũng cố ý không theo bảng chữ cái. */
    private Map<String, String> ketQuaMau() {
        Map<String, String> m = new HashMap<>();
        m.put("vnp_TxnRef", "VS1T261030120000000");
        m.put("vnp_TransactionStatus", "00");
        m.put("vnp_Amount", "15000000");
        m.put("vnp_ResponseCode", "00");
        m.put("vnp_OrderInfo", "Thanh toan don BK 1");
        m.put("vnp_TransactionNo", "14000001");
        return m;
    }

    private Map<String, String> daKy() {
        Map<String, String> m = ketQuaMau();
        m.put("vnp_SecureHash", CHU_KY_MAU);
        return m;
    }

    @Test
    @DisplayName("Chữ ký khớp với giá trị tính độc lập theo tài liệu VNPay")
    void chuKyKhopGiaTriDocLap() {
        assertThat(vnPay.chuKyHopLe(daKy())).isTrue();
    }

    @Test
    @DisplayName("Ký tự đặc biệt được mã hóa đúng như VNPay yêu cầu")
    void maHoaKyTuDacBiet() {
        Map<String, String> m = new HashMap<>();
        m.put("vnp_OrderInfo", "Don so 1/2");
        m.put("vnp_SecureHash", CHU_KY_MA_HOA);

        assertThat(vnPay.chuKyHopLe(m)).isTrue();
    }

    @Test
    @DisplayName("Chữ ký viết hoa vẫn được chấp nhận vì đây là chuỗi hex")
    void chuKyVietHoa() {
        Map<String, String> m = ketQuaMau();
        m.put("vnp_SecureHash", CHU_KY_MAU.toUpperCase());

        assertThat(vnPay.chuKyHopLe(m)).isTrue();
    }

    @Test
    @DisplayName("Sửa số tiền trên địa chỉ thì chữ ký không còn khớp")
    void suaSoTienBiPhatHien() {
        Map<String, String> m = daKy();
        m.put("vnp_Amount", "100");

        assertThat(vnPay.chuKyHopLe(m)).isFalse();
    }

    @Test
    @DisplayName("Thiếu chữ ký thì từ chối")
    void thieuChuKy() {
        assertThat(vnPay.chuKyHopLe(ketQuaMau())).isFalse();
    }

    @Test
    @DisplayName("Tham số không thuộc VNPay bị bỏ qua khi kiểm chữ ký")
    void boQuaThamSoLa() {
        Map<String, String> m = daKy();
        m.put("utm_source", "facebook");

        assertThat(vnPay.chuKyHopLe(m)).isTrue();
    }

    @Test
    @DisplayName("Chưa khai báo mã đơn vị thì không tin bất kỳ kết quả nào")
    void chuaCauHinh() {
        VnPayService chuaMo = new VnPayService(new VnPayProperties("", "", "x", "y", 15));

        assertThat(chuaMo.daMoCong()).isFalse();
        assertThat(chuaMo.chuKyHopLe(daKy())).isFalse();
    }

    @Test
    @DisplayName("Địa chỉ thanh toán đủ tham số, số tiền nhân 100 và tự kiểm chữ ký được")
    void dungDuongDanThanhToan() {
        String duongDan = vnPay.dungDuongDanThanhToan("VS7T261030120000000",
                new BigDecimal("1500000.00"), "Thanh toán đơn BK-1", "127.0.0.1");

        Map<String, String> thamSo = Arrays.stream(URI.create(duongDan).getRawQuery().split("&"))
                .map(cap -> cap.split("=", 2))
                .collect(Collectors.toMap(c -> c[0], c -> URLDecoder.decode(c[1], StandardCharsets.UTF_8)));

        assertThat(duongDan).startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        assertThat(thamSo).containsEntry("vnp_Amount", "150000000")
                .containsEntry("vnp_Version", "2.1.0")
                .containsEntry("vnp_Command", "pay")
                .containsEntry("vnp_CurrCode", "VND")
                .containsEntry("vnp_TmnCode", "KIEMTHU1")
                .containsEntry("vnp_TxnRef", "VS7T261030120000000")
                // VNPay không nhận tiếng Việt có dấu trong nội dung đơn hàng
                .containsEntry("vnp_OrderInfo", "Thanh toan don BK 1");
        assertThat(vnPay.chuKyHopLe(thamSo)).isTrue();
    }

    @Test
    @DisplayName("Mã giao dịch chỉ gồm chữ và số, đọc ngược được ra mã phiếu")
    void maGiaoDichDocNguocDuoc() {
        String ma = vnPay.sinhMaGiaoDich(1234L);

        assertThat(ma).matches("[A-Za-z0-9]+");
        assertThat(vnPay.idPhieu(ma)).contains(1234L);
    }

    @Test
    @DisplayName("Cùng một phiếu, mỗi lần chuyển sang cổng có mã giao dịch khác nhau")
    void moiLanThuMotMaKhacNhau() throws InterruptedException {
        String lan1 = vnPay.sinhMaGiaoDich(7L);
        Thread.sleep(2);
        String lan2 = vnPay.sinhMaGiaoDich(7L);

        assertThat(lan1).isNotEqualTo(lan2);
    }

    @Test
    @DisplayName("Mã không đúng dạng thì không ra phiếu nào")
    void maSaiDang() {
        assertThat(vnPay.idPhieu("TT-B-20260927-0001")).isEmpty();
        assertThat(vnPay.idPhieu("VS1T123")).isEmpty();
        assertThat(vnPay.idPhieu(null)).isEmpty();
    }

    @Test
    @DisplayName("So số tiền theo đúng cách làm tròn lúc gửi đi")
    void soSoTien() {
        Map<String, String> m = new HashMap<>();
        m.put("vnp_Amount", "123456800");

        assertThat(vnPay.dungSoTien(m, new BigDecimal("1234567.50"))).isTrue();
        assertThat(vnPay.dungSoTien(m, new BigDecimal("1234567.00"))).isFalse();
        assertThat(vnPay.dungSoTien(new HashMap<>(), BigDecimal.TEN)).isFalse();
    }

    @Test
    @DisplayName("Thành công phải có cả mã phản hồi và trạng thái giao dịch là 00")
    void thanhCongCanCaHaiMa() {
        Map<String, String> m = ketQuaMau();
        assertThat(vnPay.thanhCong(m)).isTrue();

        m.put("vnp_TransactionStatus", "02");
        assertThat(vnPay.thanhCong(m)).isFalse();
    }

    @Test
    @DisplayName("Lý do thất bại ghi bằng lời, mã lạ vẫn có câu chung")
    void lyDoThatBai() {
        assertThat(vnPay.lyDoThatBai(Map.of("vnp_ResponseCode", "24"))).contains("Khách hủy");
        assertThat(vnPay.lyDoThatBai(Map.of("vnp_ResponseCode", "99"))).contains("không thành công");
    }

    // ---------------- Địa chỉ máy khách ----------------

    /*
     * Cổng VNPay chỉ nhận IPv4. Chạy trên máy cá nhân thì địa chỉ máy khách là IPv6 vòng
     * lặp, gửi nguyên sang cổng thì giao dịch hỏng ngay từ bước tạo đường dẫn, mà thông
     * báo lỗi của cổng không nói rõ vì sao. Bài học lấy từ dự án BookShop đã chạy sandbox.
     */
    @Test
    @DisplayName("Địa chỉ IPv6 vòng lặp đổi về IPv4 trước khi gửi cổng")
    void doiIpV6VongLapVeIpV4() {
        assertThat(VnPayService.ipHopLe("::1")).isEqualTo("127.0.0.1");
        assertThat(VnPayService.ipHopLe("0:0:0:0:0:0:0:1")).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("Địa chỉ rỗng hoặc IPv6 khác cũng lùi về địa chỉ vòng lặp")
    void ipRongHoacIpV6KhacCungLui() {
        assertThat(VnPayService.ipHopLe(null)).isEqualTo("127.0.0.1");
        assertThat(VnPayService.ipHopLe("   ")).isEqualTo("127.0.0.1");
        assertThat(VnPayService.ipHopLe("2001:0db8::1")).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("Địa chỉ IPv4 thật thì giữ nguyên")
    void ipV4GiuNguyen() {
        assertThat(VnPayService.ipHopLe("203.113.45.7")).isEqualTo("203.113.45.7");
        assertThat(VnPayService.ipHopLe(" 192.168.1.10 ")).isEqualTo("192.168.1.10");
    }
}
