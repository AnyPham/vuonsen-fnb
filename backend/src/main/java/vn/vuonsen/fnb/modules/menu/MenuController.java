package vn.vuonsen.fnb.modules.menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.menu.dto.CategoryResponse;
import vn.vuonsen.fnb.modules.menu.dto.DishResponse;

import java.util.List;

// API thực đơn: lấy danh mục để dựng tab, lấy món theo từng tab
@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Tag(name = "4. Thực đơn")
public class MenuController {

    private final DishRepository dishRepository;
    private final DishCategoryRepository categoryRepository;

    @GetMapping("/categories")
    @Operation(summary = "Danh mục món ăn, dùng để dựng tab")
    public ResponseEntity<List<CategoryResponse>> categories() {
        return ResponseEntity.ok(categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream().map(CategoryResponse::from).toList());
    }

    @GetMapping("/dishes")
    @Transactional(readOnly = true)
    @Operation(summary = "Danh sách món ăn, lọc theo danh mục và từ khóa")
    public ResponseEntity<List<DishResponse>> dishes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(dishRepository.search(category, keyword)
                .stream().map(DishResponse::from).toList());
    }

    @GetMapping("/best-sellers")
    @Transactional(readOnly = true)
    @Operation(summary = "Các món được gọi nhiều nhất")
    public ResponseEntity<List<DishResponse>> bestSellers() {
        return ResponseEntity.ok(dishRepository.findByBestSellerTrueAndAvailableTrueOrderBySortOrderAsc()
                .stream().map(DishResponse::from).toList());
    }
}
