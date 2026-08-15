package vn.vuonsen.fnb.modules.space;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.space.dto.SpaceResponse;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// API xem không gian, không cần đăng nhập
@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Tag(name = "3. Không gian sự kiện")
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping
    @Operation(summary = "Danh sách không gian, lọc theo số khách, loại và giá tối đa")
    public ResponseEntity<List<SpaceResponse>> list(
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) SpaceType type,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(spaceService.search(guests, type, maxPrice));
    }

    @GetMapping("/types")
    @Operation(summary = "Danh sách loại không gian dùng cho bộ lọc")
    public ResponseEntity<List<Map<String, String>>> types() {
        return ResponseEntity.ok(Arrays.stream(SpaceType.values())
                .map(t -> Map.of("value", t.name(), "label", t.getLabel()))
                .toList());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết một không gian theo slug")
    public ResponseEntity<SpaceResponse> detail(@PathVariable String slug) {
        return ResponseEntity.ok(spaceService.getBySlug(slug));
    }
}
