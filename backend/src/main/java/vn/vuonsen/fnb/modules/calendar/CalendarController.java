package vn.vuonsen.fnb.modules.calendar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.calendar.dto.CalendarDayResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/calendar")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
@RequiredArgsConstructor
@Tag(name = "Lịch đặt chỗ", description = "Lịch gộp đơn đặt tiệc và đơn đặt món cho bộ phận quản trị")
public class CalendarController {

    /* Trần 92 ngày, đủ cho khung xem ba tháng mà không để ai kéo cả năm trong một lần gọi. */
    private static final int SO_NGAY_TOI_DA = 92;

    private final CalendarService calendarService;

    @GetMapping
    @Operation(summary = "Lịch đặt chỗ trong một khoảng ngày")
    public ResponseEntity<List<CalendarDayResponse>> lich(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {

        if (tuNgay.plusDays(SO_NGAY_TOI_DA).isBefore(denNgay)) {
            throw new IllegalArgumentException("Khoảng xem tối đa là " + SO_NGAY_TOI_DA + " ngày");
        }
        return ResponseEntity.ok(calendarService.trongKhoang(tuNgay, denNgay));
    }
}
