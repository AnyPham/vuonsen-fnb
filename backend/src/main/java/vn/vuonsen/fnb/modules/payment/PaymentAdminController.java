package vn.vuonsen.fnb.modules.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.dto.PageResponse;
import vn.vuonsen.fnb.modules.payment.dto.GhiThuRequest;
import vn.vuonsen.fnb.modules.payment.dto.PaymentResponse;
import vn.vuonsen.fnb.security.AppUserDetails;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

// Sổ thanh toán và màn hình đối soát, chỉ ADMIN và STAFF dùng được
@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@Tag(name = "20. Quản trị - Thanh toán")
public class PaymentAdminController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Sổ thanh toán, lọc theo trạng thái và tìm theo mã phiếu hoặc mã đơn")
    public ResponseEntity<PageResponse<PaymentResponse>> danhSach(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var kq = paymentService.danhSach(status, keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(kq, p -> p));
    }

    @GetMapping("/stats")
    @Operation(summary = "Đếm số phiếu theo từng trạng thái")
    public ResponseEntity<Map<String, Long>> thongKe() {
        Map<String, Long> ra = new LinkedHashMap<>();
        Arrays.stream(PaymentStatus.values()).forEach(s -> ra.put(s.name(), paymentService.dem(s)));
        return ResponseEntity.ok(ra);
    }

    @PostMapping
    @Operation(summary = "Ghi nhận khoản vừa thu tại quầy, vào sổ luôn ở trạng thái đã nhận tiền")
    public ResponseEntity<PaymentResponse> ghiThu(@Valid @RequestBody GhiThuRequest request,
                                                  @AuthenticationPrincipal AppUserDetails principal) {
        PaymentPurpose mucDich = request.purpose() == null || request.purpose().isBlank()
                ? null : PaymentPurpose.valueOf(request.purpose());
        PaymentMethod hinhThuc = request.method() == null || request.method().isBlank()
                ? null : PaymentMethod.valueOf(request.method());

        return ResponseEntity.ok(paymentService.ghiThuTaiQuay(
                request.orderCode(), request.amount(), mucDich, hinhThuc,
                request.note(), principal.getEmail()));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Đối soát xong, xác nhận đã nhận được tiền")
    public ResponseEntity<PaymentResponse> xacNhan(
            @PathVariable Long id,
            @RequestParam(required = false) String reference,
            @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(paymentService.xacNhan(id, principal.getEmail(), reference));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Hủy phiếu chờ đối soát, ví dụ khách báo không chuyển nữa")
    public ResponseEntity<PaymentResponse> huy(@PathVariable Long id,
                                                @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(paymentService.huy(id, reason));
    }
}
