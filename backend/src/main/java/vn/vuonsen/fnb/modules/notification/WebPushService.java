package vn.vuonsen.fnb.modules.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscount;
import vn.vuonsen.fnb.modules.notification.dto.DangKyPushRequest;
import vn.vuonsen.fnb.modules.notification.dto.ThongBayResponse;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Gửi thông báo đẩy tới trình duyệt của khách đã đăng ký.
 *
 * Cách hoạt động: trình duyệt đăng ký với dịch vụ push của chính hãng nó (Google cho
 * Chrome, Mozilla cho Firefox) rồi trả về một endpoint kèm hai khóa. Máy chủ ký yêu cầu
 * bằng khóa VAPID để chứng minh mình là bên đã được khách cho phép, mã hóa nội dung bằng
 * hai khóa kia rồi gửi tới endpoint đó. Toàn bộ đường đi này miễn phí, không qua dịch vụ
 * trả tiền nào.
 *
 * Khóa VAPID để trong tệp cấu hình đã gitignore, giống khóa của trợ lý tư vấn.
 */
@Service
@Slf4j
public class WebPushService {

    /* Thông báo sống 24 giờ trên dịch vụ push. Máy tắt lâu hơn thế thì tin khuyến mãi cũng cũ rồi. */
    private static final int HAN_SONG_GIAY = 24 * 60 * 60;

    private final PushSubscriptionRepository repository;
    private final ObjectMapper objectMapper;
    private final String khoaCongKhai;
    private final String khoaRieng;
    private final String lienHe;

    private PushService pushService;

    public WebPushService(PushSubscriptionRepository repository,
                          ObjectMapper objectMapper,
                          @Value("${thong-bao.vapid.cong-khai:}") String khoaCongKhai,
                          @Value("${thong-bao.vapid.rieng:}") String khoaRieng,
                          @Value("${thong-bao.vapid.lien-he:mailto:datban@vuonsen.vn}") String lienHe) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.khoaCongKhai = khoaCongKhai;
        this.khoaRieng = khoaRieng;
        this.lienHe = lienHe;
    }

    @PostConstruct
    void khoiTao() {
        if (!daCauHinh()) {
            log.info("Thông báo đẩy: chưa khai khóa VAPID, chức năng tắt. "
                    + "Sinh một cặp khóa P-256 rồi dán vào backend/tro-ly-ai.properties để bật.");
            return;
        }
        Security.addProvider(new BouncyCastleProvider());
        try {
            pushService = new PushService(khoaCongKhai, khoaRieng, lienHe);
            log.info("Thông báo đẩy: đã bật, hiện có {} máy đăng ký nhận", repository.count());
        } catch (Exception loi) {
            log.warn("Thông báo đẩy: khóa VAPID không hợp lệ nên tắt chức năng ({})",
                    loi.getClass().getSimpleName());
            pushService = null;
        }
    }

    public boolean daCauHinh() {
        return khoaCongKhai != null && !khoaCongKhai.isBlank()
                && khoaRieng != null && !khoaRieng.isBlank();
    }

    public String khoaCongKhai() {
        return khoaCongKhai;
    }

    /*
     * Lưu đăng ký. Cùng một endpoint gửi lên nhiều lần thì cập nhật chứ không thêm dòng,
     * vì trình duyệt trả lại đúng endpoint cũ mỗi lần khách mở lại trang.
     */
    @Transactional
    public void dangKy(DangKyPushRequest yeuCau) {
        PushSubscription dk = repository.findByEndpoint(yeuCau.endpoint())
                .orElseGet(() -> PushSubscription.builder()
                        .endpoint(yeuCau.endpoint())
                        .taoLuc(LocalDateTime.now())
                        .build());
        dk.setP256dh(yeuCau.p256dh());
        dk.setAuth(yeuCau.auth());
        dk.setNgonNgu(yeuCau.ngonNgu() == null || yeuCau.ngonNgu().isBlank() ? "vi" : yeuCau.ngonNgu());
        dk.setGuiLoiLuc(null);
        repository.save(dk);
    }

    @Transactional
    public void huyDangKy(String endpoint) {
        repository.deleteByEndpoint(endpoint);
    }

    /*
     * Gửi tin khuyến mãi của một dịp lễ tới mọi máy đã đăng ký.
     *
     * Dịch vụ push trả mã 404 hoặc 410 nghĩa là đăng ký đó đã chết: khách gỡ quyền hoặc
     * xóa dữ liệu trình duyệt. Xóa luôn khỏi bảng thay vì giữ lại thử mãi, không thì danh
     * sách phình lên toàn địa chỉ chết và mỗi đợt gửi lại chậm thêm.
     */
    @Transactional
    public ThongBayResponse guiTinKhuyenMai(HolidayDiscount dip) {
        if (pushService == null) {
            return new ThongBayResponse(0, 0, 0);
        }

        int phanTram = dip.getDiscountRate().multiply(java.math.BigDecimal.valueOf(100)).intValue();
        List<PushSubscription> danhSach = repository.findAll();
        int daGui = 0;
        int thatBai = 0;
        int daDon = 0;

        for (PushSubscription dk : danhSach) {
            Map<String, Object> than = new LinkedHashMap<>();
            boolean tiengAnh = "en".equals(dk.getNgonNgu());
            than.put("tieuDe", tiengAnh
                    ? "Vuon Sen · " + phanTram + "% off"
                    : "Vườn Sen · giảm " + phanTram + "%");
            than.put("noiDung", tiengAnh
                    ? dip.getName() + " · book now for the best space"
                    : dip.getName() + " · đặt sớm để chọn được không gian ưng ý");
            than.put("duongDan", "/uu-dai");

            try {
                HttpResponse phanHoi = pushService.send(new Notification(
                        dk.getEndpoint(), dk.getP256dh(), dk.getAuth(),
                        objectMapper.writeValueAsBytes(than), HAN_SONG_GIAY));
                int ma = phanHoi.getStatusLine().getStatusCode();
                if (ma == 404 || ma == 410) {
                    repository.delete(dk);
                    daDon++;
                } else if (ma >= 200 && ma < 300) {
                    daGui++;
                } else {
                    dk.setGuiLoiLuc(LocalDateTime.now());
                    thatBai++;
                }
            } catch (Exception loi) {
                dk.setGuiLoiLuc(LocalDateTime.now());
                thatBai++;
            }
        }

        log.info("Thông báo đẩy dịp {}: gửi được {}, hỏng {}, dọn {} đăng ký chết",
                dip.getName(), daGui, thatBai, daDon);
        return new ThongBayResponse(daGui, thatBai, daDon);
    }
}
