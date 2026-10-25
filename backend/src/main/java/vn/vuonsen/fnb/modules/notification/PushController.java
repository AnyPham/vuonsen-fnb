package vn.vuonsen.fnb.modules.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscount;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscountRepository;
import vn.vuonsen.fnb.modules.notification.dto.DangKyPushRequest;
import vn.vuonsen.fnb.modules.notification.dto.ThongBayResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Thông báo đẩy", description = "Đăng ký và gửi thông báo khuyến mãi tới trình duyệt")
public class PushController {

    private final WebPushService webPushService;
    private final HolidayDiscountRepository holidayRepository;

    /*
     * Giao diện hỏi khóa công khai trước khi xin quyền.
     *
     * Trả kèm cờ bật tắt để giao diện biết có nên hiện nút đăng ký hay không: máy chưa
     * khai khóa VAPID mà vẫn hiện nút thì khách bấm xong chẳng nhận được gì.
     */
    @GetMapping("/push/khoa-cong-khai")
    @Operation(summary = "Khóa công khai VAPID để trình duyệt đăng ký nhận thông báo")
    public ResponseEntity<Map<String, Object>> khoaCongKhai() {
        return ResponseEntity.ok(Map.of(
                "bat", webPushService.daCauHinh(),
                "khoa", webPushService.daCauHinh() ? webPushService.khoaCongKhai() : ""));
    }

    @PostMapping("/push/dang-ky")
    @Operation(summary = "Lưu đăng ký nhận thông báo của một trình duyệt")
    public ResponseEntity<Void> dangKy(@Valid @RequestBody DangKyPushRequest yeuCau) {
        webPushService.dangKy(yeuCau);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/push/dang-ky")
    @Operation(summary = "Hủy đăng ký nhận thông báo")
    public ResponseEntity<Void> huy(@RequestParam String endpoint) {
        webPushService.huyDangKy(endpoint);
        return ResponseEntity.noContent().build();
    }

    /*
     * Quản trị bấm gửi tin cho một dịp lễ.
     *
     * Cố ý để quản trị bấm tay chứ không tự gửi lúc tạo dịp lễ: dịp lễ hay được tạo trước
     * hàng tháng rồi còn sửa tên sửa mức giảm, tự gửi ngay thì khách nhận tin sai.
     */
    @PostMapping("/admin/push/khuyen-mai/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gửi thông báo khuyến mãi của một dịp lễ tới mọi máy đã đăng ký")
    public ResponseEntity<ThongBayResponse> guiKhuyenMai(@PathVariable Long id) {
        HolidayDiscount dip = holidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không có dịp lễ mang mã " + id));
        return ResponseEntity.ok(webPushService.guiTinKhuyenMai(dip));
    }
}
