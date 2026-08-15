package vn.vuonsen.fnb.modules.gallery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gallery")
@RequiredArgsConstructor
@Tag(name = "8. Thư viện ảnh")
public class GalleryController {

    public record GalleryResponse(Long id, String url, String caption, String category) {
        static GalleryResponse from(GalleryImage g) {
            return new GalleryResponse(g.getId(), g.getUrl(), g.getCaption(), g.getCategory());
        }
    }

    private final GalleryImageRepository repository;

    @GetMapping
    @Operation(summary = "Danh sách ảnh, lọc theo chủ đề")
    public ResponseEntity<List<GalleryResponse>> list(@RequestParam(required = false) String category) {
        List<GalleryImage> images = (category == null || category.isBlank())
                ? repository.findByActiveTrueOrderBySortOrderAsc()
                : repository.findByActiveTrueAndCategoryOrderBySortOrderAsc(category);
        return ResponseEntity.ok(images.stream().map(GalleryResponse::from).toList());
    }
}
