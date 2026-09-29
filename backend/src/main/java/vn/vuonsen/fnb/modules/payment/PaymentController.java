package vn.vuonsen.fnb.modules.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.payment.dto.PaymentResponse;
import vn.vuonsen.fnb.modules.payment.dto.TaoThanhToanRequest;
import vn.vuonsen.fnb.modules.payment.dto.TinhHinhThanhToanResponse;

/*
 * Thanh toán từ phía khách.
 *
 * Không bắt đăng nhập, giống như tra cứu đơn: khách đã có mã đơn thì coi như đã là chủ đơn.
 * Đường dẫn này chỉ đọc tình hình và tạo phiếu chờ đối soát, không tự cộng tiền vào đơn,
 * nên biết mã đơn cũng không tự làm đơn thành đã thanh toán được.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "19. Thanh toán")
public class PaymentController {

    private final PaymentService paymentService;
    private final VnPayService vnPayService;

    @GetMapping("/order/{maDon}")
    @Operation(summary = "Tình hình thanh toán của một đơn, kèm mã VietQR nếu đang chờ trả")
    public ResponseEntity<TinhHinhThanhToanResponse> tinhHinh(@PathVariable String maDon) {
        return ResponseEntity.ok(paymentService.tinhHinh(maDon));
    }

    @PostMapping
    @Operation(summary = "Tạo yêu cầu thanh toán và lấy mã VietQR để quét")
    public ResponseEntity<TinhHinhThanhToanResponse> taoYeuCau(
            @Valid @RequestBody TaoThanhToanRequest request) {
        PaymentPurpose mucDich = request.purpose() == null || request.purpose().isBlank()
                ? null
                : PaymentPurpose.valueOf(request.purpose());
        return ResponseEntity.ok(paymentService.taoYeuCau(request.orderCode(), mucDich));
    }

    /*
     * Chuyển sang cổng VNPay.
     *
     * Trả về địa chỉ chứ không tự chuyển hướng, để giao diện chủ động: mở tab mới hay chuyển
     * cả trang là việc của phía trình duyệt.
     */
    @PostMapping("/vnpay")
    @Operation(summary = "Tạo giao dịch VNPay, trả về địa chỉ trang thanh toán của cổng")
    public ResponseEntity<Map<String, String>> taoVnPay(@Valid @RequestBody TaoThanhToanRequest request,
                                                         HttpServletRequest http) {
        PaymentPurpose mucDich = request.purpose() == null || request.purpose().isBlank()
                ? null
                : PaymentPurpose.valueOf(request.purpose());
        String duongDan = paymentService.taoThanhToanVnPay(request.orderCode(), mucDich, ipKhach(http));
        return ResponseEntity.ok(Map.of("payUrl", duongDan));
    }

    /*
     * Nhận kết quả VNPay trả về.
     *
     * Giao dịch xong, VNPay đưa khách quay lại giao diện kèm một loạt tham số; giao diện gửi
     * nguyên chỗ tham số đó sang đây để máy chủ kiểm chữ ký rồi mới ghi nhận. Không để giao
     * diện tự kết luận thành công hay thất bại, vì tham số trên thanh địa chỉ ai cũng sửa được.
     */
    @PostMapping("/vnpay/verify")
    @Operation(summary = "Kiểm chữ ký kết quả VNPay và ghi nhận khoản đã thu")
    public ResponseEntity<PaymentResponse> xacNhanVnPay(@RequestBody Map<String, String> thamSo) {
        return ResponseEntity.ok(paymentService.xacNhanVnPay(thamSo));
    }

    @GetMapping("/vnpay/trang-thai")
    @Operation(summary = "Cổng VNPay đã được cấu hình hay chưa, giao diện dựa vào đây để ẩn hiện nút")
    public ResponseEntity<Map<String, Boolean>> trangThaiVnPay() {
        return ResponseEntity.ok(Map.of("daMoCong", vnPayService.daMoCong()));
    }

    /*
     * Địa chỉ máy khách, VNPay bắt buộc gửi kèm.
     *
     * Đọc X-Forwarded-For trước vì khi chạy sau máy chủ proxy thì địa chỉ thật nằm ở đó, còn
     * getRemoteAddr chỉ trả về địa chỉ của chính máy proxy.
     */
    private String ipKhach(HttpServletRequest http) {
        String chuyenTiep = http.getHeader("X-Forwarded-For");
        if (chuyenTiep != null && !chuyenTiep.isBlank()) {
            return chuyenTiep.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }

    /*
     * Giả lập ngân hàng báo có, chỉ mở ở chế độ chạy thử.
     *
     * Chạy thật thì đường dẫn này từ chối ngay trong service, nên để công khai cũng không
     * thành lỗ hổng. Có nó thì bản trình diễn và bộ kiểm thử tự động mới đi hết được luồng
     * thanh toán mà không phải chuyển tiền thật.
     */
    @PostMapping("/sandbox/{maDon}/xac-nhan")
    @Operation(summary = "Chế độ thử: giả lập ngân hàng báo có cho phiếu đang chờ")
    public ResponseEntity<TinhHinhThanhToanResponse> giaLapBaoCo(@PathVariable String maDon) {
        paymentService.giaLapNganHangBaoCo(maDon);
        return ResponseEntity.ok(paymentService.tinhHinh(maDon));
    }
}
