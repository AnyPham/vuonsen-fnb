package vn.vuonsen.fnb.modules.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingRepository;
import vn.vuonsen.fnb.modules.booking.BookingService;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.booking.TimeSlot;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.notification.EmailLog;
import vn.vuonsen.fnb.modules.notification.EmailLogRepository;
import vn.vuonsen.fnb.modules.payment.dto.TinhHinhThanhToanResponse;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Luồng thanh toán VNPay từ lúc tạo giao dịch tới lúc tiền vào đơn.
 *
 * Không gọi VNPay thật: lớp này đóng vai cổng, tự ký kết quả bằng khóa bí mật thử rồi gửi vào
 * hệ thống, nên kiểm được cả những tình huống khó tạo bằng tay như gửi trùng, sai số tiền.
 * Hàm ky() viết riêng, không gọi lại VnPayService, để ký sai thì bài kiểm phát hiện được.
 *
 * Điều quan trọng nhất ở đây không phải là "trả tiền được" mà là: báo cáo gửi lặp, gửi đồng
 * thời hay bị giả mạo thì tiền trên đơn vẫn đúng. Vì vậy mỗi bài đều kiểm lại số tiền trên đơn.
 *
 * Không đánh dấu @Transactional vì mỗi lời gọi phải tự cam kết, giống lúc chạy thật.
 */
@SpringBootTest(properties = {
        "app.vnpay.tmn-code=KIEMTHU1",
        "app.vnpay.hash-secret=" + VnPayThanhToanTest.KHOA})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("Luồng thanh toán VNPay")
class VnPayThanhToanTest {

    static final String KHOA = "KHOABIMATCHOKIEMTHU";

    private static final String IPN = "/api/v1/payments/vnpay/ipn";
    private static final String VERIFY = "/api/v1/payments/vnpay/verify";

    /* Mỗi bài một ngày tiệc riêng, tránh trùng lịch sảnh với nhau và với các lớp kiểm thử khác. */
    private static final AtomicInteger NGAY = new AtomicInteger(200);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private VnPayService vnPayService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private EmailLogRepository emailLogRepository;

    private Booking donMoi() {
        var don = bookingService.create(new BookingRequest(
                EventType.OTHER, LocalDate.now().plusDays(NGAY.getAndIncrement()), TimeSlot.NOON, 200,
                spaceRepository.findBySlug("sanh-sen-vang").orElseThrow().getId(), null, null,
                "Khách VNPay", "0901234567", null, null), null);
        return bookingRepository.findById(don.id()).orElseThrow();
    }

    /* Tạo giao dịch như lúc khách bấm nút, trả về đúng các tham số gửi sang cổng. */
    private Map<String, String> sangCong(Booking b) {
        String duongDan = paymentService.taoThanhToanVnPay(b.getCode(), PaymentPurpose.DEPOSIT, "127.0.0.1");
        return Arrays.stream(URI.create(duongDan).getRawQuery().split("&"))
                .map(cap -> cap.split("=", 2))
                .collect(Collectors.toMap(c -> c[0], c -> URLDecoder.decode(c[1], StandardCharsets.UTF_8)));
    }

    private Map<String, String> ketQua(Map<String, String> guiDi, String maPhanHoi, String trangThai) {
        return ketQua(guiDi.get("vnp_TxnRef"), guiDi.get("vnp_Amount"), maPhanHoi, trangThai);
    }

    private Map<String, String> ketQua(String maGiaoDich, String soTien, String maPhanHoi, String trangThai) {
        Map<String, String> m = new HashMap<>();
        m.put("vnp_TmnCode", "KIEMTHU1");
        m.put("vnp_TxnRef", maGiaoDich);
        m.put("vnp_Amount", soTien);
        m.put("vnp_OrderInfo", "Thanh toan don");
        m.put("vnp_ResponseCode", maPhanHoi);
        m.put("vnp_TransactionStatus", trangThai);
        m.put("vnp_TransactionNo", "14000001");
        m.put("vnp_BankCode", "NCB");
        m.put("vnp_SecureHash", ky(m));
        return m;
    }

    private String ky(Map<String, String> thamSo) {
        String duLieu = new TreeMap<>(thamSo).entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(KHOA.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(duLieu.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private ResultActions ipn(Map<String, String> thamSo) throws Exception {
        MockHttpServletRequestBuilder yeuCau = get(IPN);
        thamSo.forEach(yeuCau::param);
        return mockMvc.perform(yeuCau).andExpect(status().isOk());
    }

    private ResultActions quayVe(Map<String, String> thamSo) throws Exception {
        return mockMvc.perform(post(VERIFY).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(thamSo)));
    }

    private Payment phieu(Map<String, String> guiDi) {
        return paymentRepository.findById(vnPayService.idPhieu(guiDi.get("vnp_TxnRef")).orElseThrow()).orElseThrow();
    }

    private BigDecimal daThu(Booking b) {
        return bookingRepository.findById(b.getId()).orElseThrow().getDepositPaid();
    }

    @Test
    @DisplayName("IPN báo thành công thì ghi nhận tiền, không cần trình duyệt của khách")
    void ipnThanhCong() throws Exception {
        Booking don = donMoi();
        Map<String, String> guiDi = sangCong(don);

        ipn(ketQua(guiDi, "00", "00"))
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));

        Payment p = phieu(guiDi);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(p.getMethod()).isEqualTo(PaymentMethod.VNPAY);
        assertThat(p.getReference()).isEqualTo("14000001");
        assertThat(daThu(don)).isEqualByComparingTo(don.getDepositAmount());
    }

    @Test
    @DisplayName("VNPay gửi lại IPN thì trả 02 và không cộng tiền lần hai")
    void ipnGuiLai() throws Exception {
        Booking don = donMoi();
        Map<String, String> kq = ketQua(sangCong(don), "00", "00");

        ipn(kq).andExpect(jsonPath("$.RspCode").value("00"));
        ipn(kq).andExpect(jsonPath("$.RspCode").value("02"));

        assertThat(daThu(don)).isEqualByComparingTo(don.getDepositAmount());
    }

    @Test
    @DisplayName("Khách quay về sau IPN thấy thành công, tiền không bị cộng thêm")
    void quayVeSauIpn() throws Exception {
        Booking don = donMoi();
        Map<String, String> kq = ketQua(sangCong(don), "00", "00");

        ipn(kq);
        quayVe(kq).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        quayVe(kq).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));

        assertThat(daThu(don)).isEqualByComparingTo(don.getDepositAmount());
    }

    @Test
    @DisplayName("Không có IPN vì chạy trên localhost thì trang quay về vẫn ghi nhận được")
    void chiCoTrangQuayVe() throws Exception {
        Booking don = donMoi();

        quayVe(ketQua(sangCong(don), "00", "00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));

        assertThat(daThu(don)).isEqualByComparingTo(don.getDepositAmount());
    }

    @Test
    @DisplayName("IPN và trang quay về đến cùng lúc thì tiền chỉ được cộng một lần")
    void dongThoiChiCongMotLan() throws Exception {
        Booking don = donMoi();
        Map<String, String> kq = ketQua(sangCong(don), "00", "00");

        int soLuong = 6;
        ExecutorService pool = Executors.newFixedThreadPool(soLuong);
        CountDownLatch vachXuatPhat = new CountDownLatch(1);
        List<Future<String>> viec = new ArrayList<>();
        for (int i = 0; i < soLuong; i++) {
            viec.add(pool.submit(() -> {
                vachXuatPhat.await();
                return paymentService.xuLyKetQuaVnPay(kq).rspCode();
            }));
        }
        vachXuatPhat.countDown();

        List<String> ma = new ArrayList<>();
        for (Future<String> f : viec) {
            ma.add(f.get());
        }
        pool.shutdown();

        assertThat(ma).filteredOn("00"::equals).hasSize(1);
        assertThat(ma).filteredOn("02"::equals).hasSize(soLuong - 1);
        assertThat(daThu(don)).isEqualByComparingTo(don.getDepositAmount());
    }

    @Test
    @DisplayName("Sai chữ ký thì trả 97, phiếu và đơn không đổi")
    void saiChuKy() throws Exception {
        Booking don = donMoi();
        Map<String, String> guiDi = sangCong(don);
        Map<String, String> kq = ketQua(guiDi, "00", "00");
        kq.put("vnp_SecureHash", "a".repeat(128));

        ipn(kq).andExpect(jsonPath("$.RspCode").value("97"));
        quayVe(kq).andExpect(status().is4xxClientError());

        assertThat(phieu(guiDi).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(daThu(don)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Sửa số tiền trên địa chỉ mà không ký lại được thì bị chặn ở chữ ký")
    void suaSoTien() throws Exception {
        Booking don = donMoi();
        Map<String, String> guiDi = sangCong(don);
        Map<String, String> kq = ketQua(guiDi, "00", "00");
        kq.put("vnp_Amount", "1000");

        ipn(kq).andExpect(jsonPath("$.RspCode").value("97"));

        assertThat(daThu(don)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Chữ ký đúng nhưng số tiền lệch phiếu thì trả 04, không ghi nhận")
    void soTienLech() throws Exception {
        Booking don = donMoi();
        Map<String, String> guiDi = sangCong(don);

        ipn(ketQua(guiDi.get("vnp_TxnRef"), "100000", "00", "00"))
                .andExpect(jsonPath("$.RspCode").value("04"));

        assertThat(phieu(guiDi).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(daThu(don)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---------------- Hóa đơn gửi về email ----------------

    /*
     * Thu tiền xong phải gửi hóa đơn về email của khách.
     *
     * Chưa cấu hình máy chủ thư thì EmailService ghi toàn văn vào bảng email_logs, nên bài
     * này đọc đúng bảng đó. Nhờ vậy kiểm được nội dung thư mà không cần máy chủ thư thật,
     * và cũng là cách chứng minh trước hội đồng rằng thư đã được dựng đúng.
     */
    @Test
    @DisplayName("Xác nhận tiền thì gửi hóa đơn về email ghi trên đơn")
    void guiHoaDonSauKhiThuTien() throws Exception {
        String email = "khach.hoadon." + NGAY.get() + "@example.com";
        Booking don = donCoEmail(email);
        Map<String, String> guiDi = sangCong(don);

        ipn(ketQua(guiDi, "00", "00"))
                .andExpect(jsonPath("$.RspCode").value("00"));

        EmailLog thu = emailLogRepository.findAll().stream()
                .filter(l -> "HOA_DON".equals(l.getKind()))
                .filter(l -> email.equals(l.getRecipient()))
                .reduce((a, b) -> b)
                .orElseThrow(() -> new AssertionError("Không thấy thư hóa đơn nào được dựng"));

        assertThat(thu.getSubject()).contains(don.getCode());
        assertThat(thu.getBody())
                .contains(don.getCode())
                .contains("đã nhận được thanh toán")
                .contains("Còn phải trả");
    }

    @Test
    @DisplayName("Đơn không ghi email thì bỏ qua bước gửi, tiền vẫn thu bình thường")
    void khongCoEmailThiVanThuTienBinhThuong() throws Exception {
        Booking don = donMoi();              // donMoi dựng đơn không có email
        Map<String, String> guiDi = sangCong(don);

        ipn(ketQua(guiDi, "00", "00"))
                .andExpect(jsonPath("$.RspCode").value("00"));

        assertThat(phieu(guiDi).getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(daThu(don)).isGreaterThan(BigDecimal.ZERO);
    }

    private Booking donCoEmail(String email) {
        var don = bookingService.create(new BookingRequest(
                EventType.OTHER, LocalDate.now().plusDays(NGAY.getAndIncrement()), TimeSlot.NOON, 200,
                spaceRepository.findBySlug("sanh-sen-vang").orElseThrow().getId(), null, null,
                "Khách Hóa Đơn", "0901234567", email, null), null);
        return bookingRepository.findById(don.id()).orElseThrow();
    }

    @Test
    @DisplayName("Mã giao dịch không có trong hệ thống thì trả 01")
    void khongThayPhieu() throws Exception {
        ipn(ketQua("VS999999999T261030120000000", "100000", "00", "00"))
                .andExpect(jsonPath("$.RspCode").value("01"));
        ipn(ketQua("TT-B-20260927-0001", "100000", "00", "00"))
                .andExpect(jsonPath("$.RspCode").value("01"));
    }

    @Test
    @DisplayName("Khách bấm hủy trên cổng thì phiếu hủy, ghi rõ lý do và chưa thu đồng nào")
    void khachHuy() throws Exception {
        Booking don = donMoi();
        Map<String, String> guiDi = sangCong(don);

        ipn(ketQua(guiDi, "24", "02")).andExpect(jsonPath("$.RspCode").value("00"));

        Payment p = phieu(guiDi);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(p.getNote()).contains("Khách hủy giao dịch");
        assertThat(daThu(don)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Thất bại rồi thử lại thì thanh toán được bằng phiếu mới")
    void thatBaiRoiThuLai() throws Exception {
        Booking don = donMoi();
        ipn(ketQua(sangCong(don), "51", "02"));

        Map<String, String> lanHai = sangCong(don);
        ipn(ketQua(lanHai, "00", "00")).andExpect(jsonPath("$.RspCode").value("00"));

        assertThat(daThu(don)).isEqualByComparingTo(don.getDepositAmount());
    }

    @Test
    @DisplayName("Khách bỏ dở rồi bấm lại thì cùng phiếu nhưng khác mã, không bị VNPay báo trùng")
    void boDoRoiBamLai() throws Exception {
        Booking don = donMoi();
        Map<String, String> lan1 = sangCong(don);
        Thread.sleep(2);
        Map<String, String> lan2 = sangCong(don);

        assertThat(lan2.get("vnp_TxnRef")).isNotEqualTo(lan1.get("vnp_TxnRef"));
        assertThat(phieu(lan2).getId()).isEqualTo(phieu(lan1).getId());
    }

    @Test
    @DisplayName("Lần thử cũ báo hủy, lần thử sau trả được tiền thì vẫn phải ghi nhận khoản đó")
    void traDuocSauKhiPhieuDaHuy() throws Exception {
        Booking don = donMoi();
        Map<String, String> lan1 = sangCong(don);
        Thread.sleep(2);
        Map<String, String> lan2 = sangCong(don);

        ipn(ketQua(lan1, "24", "02"));
        ipn(ketQua(lan2, "00", "00")).andExpect(jsonPath("$.RspCode").value("00"));

        assertThat(phieu(lan2).getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(daThu(don)).isEqualByComparingTo(don.getDepositAmount());
    }

    @Test
    @DisplayName("Mã 07 đã trừ tiền nhưng bị nghi ngờ: giữ phiếu chờ để nhân viên kiểm tra")
    void nghiNgoGianLan() throws Exception {
        Booking don = donMoi();
        Map<String, String> guiDi = sangCong(don);

        ipn(ketQua(guiDi, "07", "07"));

        Payment p = phieu(guiDi);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(p.getNote()).contains("nghi ngờ");
        assertThat(daThu(don)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Khách bỏ VNPay rồi quay sang quét mã thì phiếu ghi đúng cách trả mới")
    void doiSangQuetMa() {
        Booking don = donMoi();
        Map<String, String> guiDi = sangCong(don);
        assertThat(phieu(guiDi).getMethod()).isEqualTo(PaymentMethod.VNPAY);

        TinhHinhThanhToanResponse tinhHinh = paymentService.taoYeuCau(don.getCode(), PaymentPurpose.DEPOSIT);

        assertThat(tinhHinh.phieuChoDoiSoat().id()).isEqualTo(phieu(guiDi).getId());
        assertThat(phieu(guiDi).getMethod()).isEqualTo(PaymentMethod.VIETQR);
    }
}
