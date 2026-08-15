package vn.vuonsen.fnb.modules.partypackage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.partypackage.dto.PackageResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@Tag(name = "5. Gói tiệc")
public class PackageController {

    private final PartyPackageRepository packageRepository;

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Danh sách gói tiệc đang bán")
    public ResponseEntity<List<PackageResponse>> list() {
        return ResponseEntity.ok(packageRepository.findByActiveTrueOrderBySortOrderAsc()
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
