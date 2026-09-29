package vn.vuonsen.fnb.modules.holiday;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.holiday.dto.HolidayDiscountResponse;

import java.time.LocalDate;
import java.util.List;

/*
 * Ưu đãi dịp lễ cho khách xem.
 *
 * Trước đây các dịp giảm giá chỉ có ở trang quản trị: khách chỉ biết mình được giảm khi bấm
 * tới bước báo giá và trùng ngày. Đưa danh sách ra công khai để khách chủ động chọn ngày có
 * ưu đãi, giống cách các trung tâm tiệc và trang đặt chỗ vẫn làm.
 *
 * Chỉ trả dịp đang bật và chưa kết thúc. Dịp đã tắt hoặc đã qua không còn ý nghĩa với khách.
 */
@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@Tag(name = "18. Ưu đãi dịp lễ")
public class HolidayController {

    /** Đủ để phủ hết các dịp trong một năm mà không làm trang dài lê thê. */
    private static final int SO_DIP_TOI_DA = 12;

    private final HolidayDiscountRepository repository;

    @GetMapping
    @Operation(summary = "Các dịp lễ đang áp dụng hoặc sắp tới")
    public ResponseEntity<List<HolidayDiscountResponse>> dangApDung() {
        return ResponseEntity.ok(repository
                .findByActiveTrueAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        LocalDate.now(), PageRequest.of(0, SO_DIP_TOI_DA))
                .stream()
                .map(HolidayDiscountResponse::from)
                .toList());
    }
}
