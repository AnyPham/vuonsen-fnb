package vn.vuonsen.fnb.modules.holiday;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.holiday.dto.HolidayDiscountRequest;
import vn.vuonsen.fnb.modules.holiday.dto.HolidayDiscountResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/holidays")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "18. Quản trị - Ngày lễ")
public class HolidayAdminController {

    private final HolidayAdminService holidayAdminService;

    @GetMapping
    @Operation(summary = "Danh sách dịp lễ giảm giá, gồm cả dịp đã tắt và dịp đã qua")
    public ResponseEntity<List<HolidayDiscountResponse>> list() {
        return ResponseEntity.ok(holidayAdminService.listAll());
    }

    @PostMapping
    @Operation(summary = "Thêm dịp lễ giảm giá, mức giảm từ 10% đến 20%")
    public ResponseEntity<HolidayDiscountResponse> create(@Valid @RequestBody HolidayDiscountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayAdminService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sửa dịp lễ giảm giá")
    public ResponseEntity<HolidayDiscountResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody HolidayDiscountRequest request) {
        return ResponseEntity.ok(holidayAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa dịp lễ giảm giá, đơn đã đặt vẫn giữ nguyên số tiền đã chốt")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        holidayAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
