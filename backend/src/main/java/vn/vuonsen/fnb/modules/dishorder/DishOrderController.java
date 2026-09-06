package vn.vuonsen.fnb.modules.dishorder;

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
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderQuoteRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderQuoteResponse;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderResponse;
import vn.vuonsen.fnb.security.AppUserDetails;

// API đặt món lẻ: giao tận nhà hoặc đặt trước rồi tới ăn tại chỗ
@RestController
@RequestMapping("/api/v1/dish-orders")
@RequiredArgsConstructor
@Tag(name = "16. Đặt món lẻ")
public class DishOrderController {

    private final DishOrderService dishOrderService;

    @PostMapping("/quote")
    @Operation(summary = "Bảng tạm tính cho giỏ món, chưa cần thông tin liên hệ")
    public ResponseEntity<DishOrderQuoteResponse> quote(@Valid @RequestBody DishOrderQuoteRequest request) {
        return ResponseEntity.ok(dishOrderService.quote(request));
    }

    @PostMapping
    @Operation(summary = "Gửi đơn đặt món, khách chưa đăng nhập vẫn đặt được")
    public ResponseEntity<DishOrderResponse> create(
            @Valid @RequestBody DishOrderRequest request,
            @AuthenticationPrincipal AppUserDetails principal) {
        Long userId = principal == null ? null : principal.getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dishOrderService.create(request, userId));
    }

    @GetMapping("/track/{code}")
    @Operation(summary = "Tra cứu đơn đặt món bằng mã đơn")
    public ResponseEntity<DishOrderResponse> track(@PathVariable String code) {
        return ResponseEntity.ok(dishOrderService.getByCode(code));
    }

    @GetMapping("/my")
    @Operation(summary = "Lịch sử đặt món của tài khoản đang đăng nhập")
    public ResponseEntity<PageResponse<DishOrderResponse>> myOrders(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = dishOrderService.listOfUser(principal.getUserId(), PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(result, o -> o));
    }
}
