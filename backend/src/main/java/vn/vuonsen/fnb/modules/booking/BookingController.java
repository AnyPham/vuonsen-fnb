package vn.vuonsen.fnb.modules.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.dto.PageResponse;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.booking.dto.BookingResponse;
import vn.vuonsen.fnb.modules.booking.dto.QuoteRequest;
import vn.vuonsen.fnb.modules.booking.dto.QuoteResponse;
import vn.vuonsen.fnb.security.AppUserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// API đặt tiệc cho khách hàng
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "6. Đặt tiệc")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/quote")
    @Operation(summary = "Báo giá tạm tính, không tạo đơn")
    public ResponseEntity<QuoteResponse> quote(@Valid @RequestBody QuoteRequest request) {
        return ResponseEntity.ok(bookingService.quote(request));
    }

    @PostMapping
    @Operation(summary = "Gửi yêu cầu đặt tiệc, khách chưa đăng nhập vẫn đặt được")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request,
                                                  @AuthenticationPrincipal AppUserDetails principal) {
        Long userId = principal == null ? null : principal.getUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(request, userId));
    }

    @GetMapping("/track/{code}")
    @Operation(summary = "Tra cứu đơn bằng mã đơn")
    public ResponseEntity<BookingResponse> track(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getByCode(code));
    }

    @GetMapping("/my")
    @Operation(summary = "Lịch sử đặt tiệc của tài khoản đang đăng nhập")
    public ResponseEntity<PageResponse<BookingResponse>> myBookings(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = bookingService.listOfUser(principal.getUserId(), PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(result, b -> b));
    }

    @GetMapping("/options")
    @Operation(summary = "Danh mục loại sự kiện và buổi tổ chức cho form đặt tiệc")
    public ResponseEntity<Map<String, List<Map<String, String>>>> options() {
        return ResponseEntity.ok(Map.of(
                "eventTypes", Arrays.stream(EventType.values())
                        .map(e -> Map.of("value", e.name(), "label", e.getLabel())).toList(),
                "timeSlots", Arrays.stream(TimeSlot.values())
                        .map(t -> Map.of("value", t.name(), "label", t.getLabel())).toList()));
    }
}
