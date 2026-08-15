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
@Tag(name = "3. Khong gian su kien")
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping
    @Operation(summary = "Danh sach khong gian, ho tro loc theo so khach / loai / gia toi da")
    public ResponseEntity<List<SpaceResponse>> list(
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) SpaceType type,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(spaceService.search(guests, type, maxPrice));
    }

    @GetMapping("/types")
    @Operation(summary = "Danh sach loai khong gian de dung cho tab/filter")
    public ResponseEntity<List<Map<String, String>>> types() {
        return ResponseEntity.ok(Arrays.stream(SpaceType.values())
                .map(t -> Map.of("value", t.name(), "label", t.getLabel()))
                .toList());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiet mot khong gian theo slug")
    public ResponseEntity<SpaceResponse> detail(@PathVariable String slug) {
        return ResponseEntity.ok(spaceService.getBySlug(slug));
    }
}
