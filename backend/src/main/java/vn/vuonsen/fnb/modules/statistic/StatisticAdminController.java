package vn.vuonsen.fnb.modules.statistic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.statistic.dto.ThongKeResponse;

import java.time.LocalDate;

/*
 * API thống kê cho trang quản trị.
 *
 * Nằm dưới /api/v1/admin nên đã được SecurityConfig chặn sẵn, chỉ ADMIN và STAFF gọi được.
 */
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "14. Quản trị - Thống kê")
public class StatisticAdminController {

    private final ThongKeService thongKeService;

    @GetMapping
    @Operation(summary = "Số liệu tổng quan và dữ liệu cho bảy biểu đồ. "
            + "Bỏ trống khoảng ngày thì lấy 12 tháng gần nhất.")
    public ResponseEntity<ThongKeResponse> thongKe(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(thongKeService.thongKe(from, to));
    }
}
