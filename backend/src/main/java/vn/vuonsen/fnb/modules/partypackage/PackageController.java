package vn.vuonsen.fnb.modules.partypackage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.partypackage.dto.PackageResponse;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@Tag(name = "5. Gói tiệc")
public class PackageController {

    private final PartyPackageRepository packageRepository;

    /*
     * Không khai tham số nào thì trả về toàn bộ gói đang bán, đúng như trước khi có bộ lọc.
     * Giữ nguyên hành vi cũ để những chỗ đang gọi sẵn, như bước chọn gói trong form đặt
     * tiệc và khối gợi ý, không phải sửa theo.
     */
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Danh sách gói tiệc đang bán, lọc theo giá mỗi mâm, số món và số giờ")
    public ResponseEntity<List<PackageResponse>> list(
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minDishes,
            @RequestParam(required = false) Integer minHours) {
        return ResponseEntity.ok(packageRepository.search(maxPrice, minDishes, minHours)
                .stream().map(PackageResponse::from).toList());
    }

    @GetMapping("/{code}")
    @Transactional(readOnly = true)
    @Operation(summary = "Chi tiết một gói tiệc")
    public ResponseEntity<PackageResponse> detail(@PathVariable String code) {
        return ResponseEntity.ok(packageRepository.findByCode(code)
                .map(PackageResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("gói tiệc", code)));
    }
}
