package vn.vuonsen.fnb.modules.space;

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
import vn.vuonsen.fnb.modules.space.dto.SpaceAdminResponse;
import vn.vuonsen.fnb.modules.space.dto.SpaceRequest;
import vn.vuonsen.fnb.modules.space.dto.SpaceResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/spaces")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "9. Quản trị - Không gian")
public class SpaceAdminController {

    private final SpaceService spaceService;

    @GetMapping
    @Operation(summary = "Danh sách toàn bộ không gian, gồm cả không gian đã ngừng kinh doanh")
    public ResponseEntity<List<SpaceAdminResponse>> list() {
        return ResponseEntity.ok(spaceService.listForAdmin());
    }

    @PostMapping
    @Operation(summary = "Thêm không gian mới")
    public ResponseEntity<SpaceResponse> create(@Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật không gian")
    public ResponseEntity<SpaceResponse> update(@PathVariable Long id, @Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.ok(spaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ngừng kinh doanh một không gian")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        spaceService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
