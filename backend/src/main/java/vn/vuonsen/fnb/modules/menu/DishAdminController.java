package vn.vuonsen.fnb.modules.menu;

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
import vn.vuonsen.fnb.modules.menu.dto.DishAdminResponse;
import vn.vuonsen.fnb.modules.menu.dto.DishRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dishes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "13. Quản trị - Thực đơn")
public class DishAdminController {

    private final MenuAdminService menuAdminService;

    @GetMapping
    @Operation(summary = "Danh sách toàn bộ món ăn, gồm cả món đã ngừng bán")
    public ResponseEntity<List<DishAdminResponse>> list() {
        return ResponseEntity.ok(menuAdminService.listAll());
    }

    @PostMapping
    @Operation(summary = "Thêm món ăn mới")
    public ResponseEntity<DishAdminResponse> create(@Valid @RequestBody DishRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuAdminService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật món ăn")
    public ResponseEntity<DishAdminResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody DishRequest request) {
        return ResponseEntity.ok(menuAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ngừng bán một món ăn")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        menuAdminService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
