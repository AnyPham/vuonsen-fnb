package vn.vuonsen.fnb.modules.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.dto.PageResponse;
import vn.vuonsen.fnb.modules.booking.dto.BookingResponse;
import vn.vuonsen.fnb.modules.booking.dto.StatusUpdateRequest;
import vn.vuonsen.fnb.security.AppUserDetails;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

// API quản trị đơn đặt tiệc, chỉ ADMIN và STAFF dùng được
@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "10. Quản trị - Đơn đặt tiệc")
public class BookingAdminController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "Danh sách đơn, lọc theo trạng thái, khoảng ngày và từ khóa")
    public ResponseEntity<PageResponse<BookingResponse>> list(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var result = bookingService.search(status, from, to, keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(result, b -> b));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Chi tiết một đơn")
    public ResponseEntity<BookingResponse> detail(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getByCode(code));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Duyệt, hoàn thành hoặc hủy đơn")
    public ResponseEntity<BookingResponse> changeStatus(@PathVariable Long id,
                                                        @Valid @RequestBody StatusUpdateRequest request,
                                                        @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(bookingService.changeStatus(
                id, request.status(), request.note(), principal.getEmail()));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Số đơn theo từng trạng thái")
    public ResponseEntity<Map<String, Long>> statistics() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            stats.put(status.name(), bookingService.countByStatus(status));
        }
        return ResponseEntity.ok(stats);
    }
}
