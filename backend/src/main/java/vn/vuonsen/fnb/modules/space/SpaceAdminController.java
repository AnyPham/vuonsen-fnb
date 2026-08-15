package vn.vuonsen.fnb.modules.space;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.space.dto.SpaceRequest;
import vn.vuonsen.fnb.modules.space.dto.SpaceResponse;

@RestController
@RequestMapping("/api/v1/admin/spaces")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "9. Quan tri - Khong gian")
public class SpaceAdminController {

    private final SpaceService spaceService;

    @PostMapping
    @Operation(summary = "Them khong gian moi")
    public ResponseEntity<SpaceResponse> create(@Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cap nhat khong gian")
    public ResponseEntity<SpaceResponse> update(@PathVariable Long id, @Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.ok(spaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ngung kinh doanh mot khong gian (xoa mem)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        spaceService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
