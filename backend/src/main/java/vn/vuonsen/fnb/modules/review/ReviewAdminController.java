package vn.vuonsen.fnb.modules.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.dto.PageResponse;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;

import java.time.LocalDateTime;

// API quản trị đánh giá, chỉ ADMIN và STAFF dùng được
@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "11. Quản trị - Đánh giá")
public class ReviewAdminController {

    private final ReviewRepository reviewRepository;

    // Quản trị cần thấy thêm mã đơn và trạng thái duyệt so với bản công khai
    public record AdminReviewResponse(
            Long id,
            String customerName,
            Integer rating,
            String content,
            String eventType,
            String bookingCode,
            boolean approved,
            LocalDateTime createdAt
    ) {
        static AdminReviewResponse from(Review r) {
            return new AdminReviewResponse(
                    r.getId(), r.getCustomerName(), r.getRating(), r.getContent(),
                    r.getEventType(),
                    r.getBooking() == null ? null : r.getBooking().getCode(),
                    r.isApproved(), r.getCreatedAt());
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Danh sách đánh giá, lọc theo đã duyệt hoặc chờ duyệt")
    public ResponseEntity<PageResponse<AdminReviewResponse>> list(
            @RequestParam(defaultValue = "false") boolean approved,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var result = reviewRepository.findByApprovedOrderByCreatedAtDesc(approved, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(result, AdminReviewResponse::from));
    }

    @PatchMapping("/{id}/approve")
    @Transactional
    @Operation(summary = "Duyệt đánh giá để hiển thị lên website")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("đánh giá", id));
        review.setApproved(true);
        reviewRepository.save(review);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Từ chối và xóa đánh giá không phù hợp")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) {
            throw ResourceNotFoundException.of("đánh giá", id);
        }
        reviewRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
