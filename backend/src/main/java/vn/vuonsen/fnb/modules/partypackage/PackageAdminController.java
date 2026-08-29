package vn.vuonsen.fnb.modules.partypackage;

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
import vn.vuonsen.fnb.modules.partypackage.dto.PackageAdminResponse;
import vn.vuonsen.fnb.modules.partypackage.dto.PackageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/packages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "14. Quản trị - Gói tiệc")
public class PackageAdminController {

    private final PackageAdminService packageAdminService;

    @GetMapping
    @Operation(summary = "Danh sách toàn bộ gói tiệc, gồm cả gói đã ngừng bán")
    public ResponseEntity<List<PackageAdminResponse>> list() {
        return ResponseEntity.ok(packageAdminService.listAll());
    }

    @PostMapping
    @Operation(summary = "Thêm gói tiệc mới")
    public ResponseEntity<PackageAdminResponse> create(@Valid @RequestBody PackageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(packageAdminService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật gói tiệc")
    public ResponseEntity<PackageAdminResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ngừng bán một gói tiệc")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        packageAdminService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
