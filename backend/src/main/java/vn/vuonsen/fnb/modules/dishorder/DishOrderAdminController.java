package vn.vuonsen.fnb.modules.dishorder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.dto.PageResponse;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderResponse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

// Quản trị đơn đặt món. Nhân viên và quản trị đều xử lý được, giống đơn đặt tiệc.
@RestController
@RequestMapping("/api/v1/admin/dish-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@Tag(name = "17. Quản trị - Đơn đặt món")
public class DishOrderAdminController {

    private final DishOrderService dishOrderService;
    private final DishOrderRepository dishOrderRepository;

    @GetMapping
    @Operation(summary = "Danh sách đơn đặt món, lọc theo trạng thái, hình thức nhận và từ khóa")
    public ResponseEntity<PageResponse<DishOrderResponse>> list(
            @RequestParam(required = false) DishOrderStatus status,
            @RequestParam(required = false) FulfillmentType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = dishOrderService.search(status, type, keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(result, o -> o));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái đơn: xác nhận, hoàn thành hoặc hủy")
    public ResponseEntity<DishOrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam DishOrderStatus status) {
        return ResponseEntity.ok(dishOrderService.updateStatus(id, status));
    }

    @GetMapping("/stats")
    @Operation(summary = "Đếm số đơn theo từng trạng thái")
    public ResponseEntity<Map<String, Long>> stats() {
        Map<String, Long> ra = new LinkedHashMap<>();
        Arrays.stream(DishOrderStatus.values())
                .forEach(s -> ra.put(s.name(), dishOrderRepository.countByStatus(s)));
        return ResponseEntity.ok(ra);
    }
}
