package vn.vuonsen.fnb.modules.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.config.props.VnPayProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*
 * Cổng thanh toán VNPay.
 *
 * Khác với VietQR ở chỗ: VietQR chỉ là mã chuyển khoản, tiền về rồi nhân viên phải tự đối
 * soát; còn VNPay là cổng thật, khách trả xong thì cổng gọi ngược về báo kết quả nên hệ
 * thống biết ngay mà không cần ai soát sao kê.
 *
 * Đổi lại, VNPay cần đăng ký để có mã đơn vị và khóa bí mật. Chưa đăng ký thì để trống cấu
 * hình, hệ thống vẫn chạy bình thường và chỉ báo chưa mở cổng khi khách bấm chọn.
 *
 * Toàn bộ an toàn của luồng này nằm ở chữ ký HMAC-SHA512: mọi tham số được xếp theo thứ tự
 * bảng chữ cái rồi ký bằng khóa bí mật. Khách sửa số tiền trên địa chỉ web thì chữ ký không
 * còn khớp và hệ thống từ chối. Vì vậy khi nhận kết quả trả về phải kiểm chữ ký trước, tin
 * vào tham số trước rồi mới kiểm là sai thứ tự và mất luôn ý nghĩa bảo vệ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId GIO_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    /** VNPay tính tiền theo đơn vị nhỏ nhất, tức nhân một trăm lần. */
    private static final int HE_SO_TIEN = 100;

    private static final DateTimeFormatter DINH_DANG_LUOT = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");

    /** Dạng mã giao dịch gửi VNPay: VS, mã phiếu, T, rồi thời điểm tạo. */
    private static final Pattern MA_GIAO_DICH = Pattern.compile("VS(\\d{1,18})T\\d{15}");

    // Nguyên văn bảng mã phản hồi trong tài liệu VNPay, ghi vào phiếu để nhân viên biết vì sao hỏng
    private static final Map<String, String> LY_DO_THAT_BAI = Map.ofEntries(
            Map.entry("07", "Đã trừ tiền nhưng giao dịch bị nghi ngờ, cần kiểm tra với VNPay"),
            Map.entry("09", "Thẻ hoặc tài khoản chưa đăng ký Internet Banking"),
            Map.entry("10", "Xác thực thông tin thẻ sai quá 3 lần"),
            Map.entry("11", "Hết hạn chờ thanh toán"),
            Map.entry("12", "Thẻ hoặc tài khoản bị khóa"),
            Map.entry("13", "Nhập sai mã OTP"),
            Map.entry("24", "Khách hủy giao dịch"),
            Map.entry("51", "Tài khoản không đủ số dư"),
            Map.entry("65", "Vượt hạn mức giao dịch trong ngày"),
            Map.entry("75", "Ngân hàng thanh toán đang bảo trì"),
            Map.entry("79", "Nhập sai mật khẩu thanh toán quá số lần quy định"));

    private final VnPayProperties properties;

    public boolean daMoCong() {
        return properties.daCauHinh();
    }

    /*
     * Sinh mã giao dịch cho một lần chuyển khách sang cổng.
     *
     * Không dùng thẳng mã phiếu, vì hai lý do. VNPay cấm trùng mã trong ngày, mà khách bỏ dở
     * rồi bấm thanh toán lại thì vẫn là phiếu cũ, dùng lại mã là cổng từ chối. Tài liệu của
     * VNPay cũng quy định mã chỉ gồm chữ và số, còn mã phiếu có dấu gạch ngang.
     */
    public String sinhMaGiaoDich(Long idPhieu) {
        return "VS" + idPhieu + "T" + LocalDateTime.now(GIO_VN).format(DINH_DANG_LUOT);
    }

    /** Đọc ngược mã phiếu từ mã giao dịch. Mã không đúng dạng thì coi như không có phiếu nào. */
    public Optional<Long> idPhieu(String maGiaoDich) {
        if (maGiaoDich == null) {
            return Optional.empty();
        }
        Matcher khop = MA_GIAO_DICH.matcher(maGiaoDich);
        return khop.matches() ? Optional.of(Long.valueOf(khop.group(1))) : Optional.empty();
    }

    /*
     * Dựng địa chỉ để chuyển khách sang trang thanh toán của VNPay.
     *
     * @param maGiaoDich mã do sinhMaGiaoDich tạo, VNPay gọi là mã tham chiếu của đơn vị
     * @param soTien     số tiền đồng
     * @param moTa       nội dung hiện trên màn hình thanh toán
     * @param ipKhach    địa chỉ máy khách, VNPay bắt buộc gửi kèm để chống gian lận
     */
    public String dungDuongDanThanhToan(String maGiaoDich, BigDecimal soTien, String moTa, String ipKhach) {
        if (!properties.daCauHinh()) {
            throw new BusinessException("Chưa mở cổng thanh toán VNPay. "
                    + "Vui lòng chọn chuyển khoản quét mã QR hoặc trả tiền mặt tại nhà hàng.");
        }

        LocalDateTime bayGio = LocalDateTime.now(GIO_VN);

        // TreeMap để tham số tự xếp theo thứ tự bảng chữ cái, đúng yêu cầu khi ký
        Map<String, String> thamSo = new TreeMap<>();
        thamSo.put("vnp_Version", "2.1.0");
        thamSo.put("vnp_Command", "pay");
        thamSo.put("vnp_TmnCode", properties.tmnCode());
        thamSo.put("vnp_Amount", soTienGuiCong(soTien));
        thamSo.put("vnp_CurrCode", "VND");
        thamSo.put("vnp_TxnRef", maGiaoDich);
        thamSo.put("vnp_OrderInfo", khongDau(moTa));
        thamSo.put("vnp_OrderType", "other");
        thamSo.put("vnp_Locale", "vn");
        thamSo.put("vnp_ReturnUrl", properties.returnUrl());
        thamSo.put("vnp_IpAddr", ipHopLe(ipKhach));
        thamSo.put("vnp_CreateDate", bayGio.format(DINH_DANG_GIO));
        thamSo.put("vnp_ExpireDate", bayGio.plusMinutes(properties.expireMinutes()).format(DINH_DANG_GIO));

        String duLieuKy = noiThamSo(thamSo, true);
        String chuKy = hmacSha512(properties.hashSecret(), duLieuKy);

        return properties.payUrl() + "?" + duLieuKy + "&vnp_SecureHash=" + chuKy;
    }

    /*
     * Kiểm chữ ký của kết quả VNPay trả về.
     *
     * Chỉ lấy các tham số bắt đầu bằng vnp_ như tài liệu quy định, bỏ hai tham số chữ ký, phần
     * còn lại ký lại rồi so. So bằng hàm không phụ thuộc thời gian để không lộ thông tin qua
     * thời gian xử lý, và không phân biệt hoa thường vì đây là chuỗi hex.
     */
    public boolean chuKyHopLe(Map<String, String> thamSo) {
        if (!properties.daCauHinh()) {
            return false;
        }
        String chuKyNhanDuoc = thamSo.get("vnp_SecureHash");
        if (chuKyNhanDuoc == null || chuKyNhanDuoc.isBlank()) {
            return false;
        }

        Map<String, String> deKy = new TreeMap<>(thamSo);
        deKy.keySet().removeIf(ten -> !ten.startsWith("vnp_"));
        deKy.remove("vnp_SecureHash");
        deKy.remove("vnp_SecureHashType");

        String tinhLai = hmacSha512(properties.hashSecret(), noiThamSo(deKy, true));
        return MessageDigestEquals(tinhLai, chuKyNhanDuoc.toLowerCase(Locale.ROOT));
    }

    /** Mã 00 nghĩa là khách đã trả tiền thành công, mọi mã khác đều là không thành công. */
    public boolean thanhCong(Map<String, String> thamSo) {
        return "00".equals(thamSo.get("vnp_ResponseCode"))
                && "00".equals(thamSo.get("vnp_TransactionStatus"));
    }

    /** Số tiền VNPay báo về phải đúng bằng số tiền của phiếu, tính theo cùng một cách lúc gửi đi. */
    public boolean dungSoTien(Map<String, String> thamSo, BigDecimal soTien) {
        return soTienGuiCong(soTien).equals(thamSo.get("vnp_Amount"));
    }

    /*
     * Mã 07 khác mọi mã thất bại khác: tiền đã bị trừ nhưng giao dịch đang bị nghi ngờ. Không
     * được hủy phiếu và báo khách là chưa mất tiền, phải để nhân viên kiểm tra.
     */
    public boolean canKiemTra(Map<String, String> thamSo) {
        return "07".equals(thamSo.get("vnp_ResponseCode"));
    }

    public String lyDoThatBai(Map<String, String> thamSo) {
        String ma = thamSo.get("vnp_ResponseCode");
        return "VNPay: " + LY_DO_THAT_BAI.getOrDefault(ma, "Giao dịch không thành công") + " (mã " + ma + ")";
    }

    // ---------------------------------------------------------------- phụ trợ

    private String soTienGuiCong(BigDecimal soTien) {
        return soTien.setScale(0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(HE_SO_TIEN)).toPlainString();
    }

    /*
     * Nối tham số thành chuỗi dạng a=1&b=2.
     *
     * Giá trị phải mã hóa theo chuẩn địa chỉ web trước khi nối, và phải mã hóa y hệt nhau ở
     * cả lúc ký lẫn lúc kiểm, chỉ lệch một dấu cách là chữ ký khác nhau ngay.
     */
    /*
     * Đưa địa chỉ máy khách về dạng VNPay nhận được.
     *
     * Cổng VNPay chỉ nhận IPv4. Chạy trên máy cá nhân thì getRemoteAddr trả về địa chỉ
     * vòng lặp IPv6 là 0:0:0:0:0:0:0:1, gửi nguyên như vậy sang cổng thì giao dịch bị từ
     * chối ngay từ bước tạo đường dẫn, mà thông báo lỗi của cổng không nói rõ vì sao.
     *
     * Đây là bài học rút từ dự án BookShop của cùng tác giả, nơi đã chạy thật với sandbox:
     * phải đổi địa chỉ vòng lặp IPv6 về 127.0.0.1 thì cổng mới chấp nhận.
     */
    static String ipHopLe(String ip) {
        if (ip == null || ip.isBlank()) {
            return "127.0.0.1";
        }
        String s = ip.trim();
        if ("::1".equals(s) || "0:0:0:0:0:0:0:1".equals(s)) {
            return "127.0.0.1";
        }
        // Còn lại mà vẫn là IPv6 thì cũng không dùng được, lùi về địa chỉ vòng lặp
        return s.contains(":") ? "127.0.0.1" : s;
    }

    private String noiThamSo(Map<String, String> thamSo, boolean maHoa) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : thamSo.entrySet()) {
            if (e.getValue() == null || e.getValue().isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=');
            sb.append(maHoa ? URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII) : e.getValue());
        }
        return sb.toString();
    }

    private String hmacSha512(String khoa, String duLieu) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(khoa.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bam = mac.doFinal(duLieu.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bam.length * 2);
            for (byte b : bam) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không ký được dữ liệu gửi VNPay", e);
        }
    }

    /** So hai chuỗi trong thời gian không đổi, tránh lộ thông tin qua thời gian so sánh. */
    private boolean MessageDigestEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    /** VNPay không nhận tiếng Việt có dấu ở nội dung đơn hàng. */
    private String khongDau(String chuoi) {
        if (chuoi == null) {
            return "";
        }
        return java.text.Normalizer.normalize(chuoi, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
