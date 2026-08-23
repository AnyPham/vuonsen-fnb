package vn.vuonsen.fnb.modules.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.common.dto.PageResponse;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingRepository;
import vn.vuonsen.fnb.modules.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "7. Đánh giá")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public record ReviewRequest(
            @NotBlank(message = "Vui lòng nhập mã đơn đã tổ chức") String bookingCode,
            @NotBlank(message = "Vui lòng nhập tên") @Size(max = 120) String customerName,
            @NotNull @Min(value = 1, message = "Điểm từ 1 đến 5")
            @Max(value = 5, message = "Điểm từ 1 đến 5") Integer rating,
            @NotBlank(message = "Vui lòng nhập nội dung") @Size(max = 1000) String content
    ) {
    }

    public record ReviewResponse(Long id, String customerName, Integer rating, String content,
                                 String eventType, LocalDateTime createdAt) {
        static ReviewResponse from(Review r) {
            return new ReviewResponse(r.getId(), r.getCustomerName(), r.getRating(),
                    r.getContent(), r.getEventType(), r.getCreatedAt());
        }
    }

    @GetMapping
    @Operation(summary = "Danh sách đánh giá đã duyệt")
    public ResponseEntity<PageResponse<ReviewResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        var result = reviewRepository.findByApprovedTrueOrderByCreatedAtDesc(PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(result, ReviewResponse::from));
    }

    @GetMapping("/summary")
    @Operation(summary = "Điểm trung bình")
    public ResponseEntity<Map<String, Object>> summary() {
        Double average = reviewRepository.averageRating();
        return ResponseEntity.ok(Map.of("average", average == null ? 0d : average));
    }

    @PostMapping
    @Operation(summary = "Gửi đánh giá cho một tiệc đã tổ chức, chờ quản trị duyệt")
    public ResponseEntity<Void> create(@Valid @RequestBody ReviewRequest request) {
        // Chỉ khách đã thật sự tổ chức tiệc mới được đánh giá
        Booking booking = bookingRepository.findByCode(request.bookingCode().trim())
                .orElseThrow(() -> ResourceNotFoundException.of("đơn đặt tiệc", request.bookingCode()));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("Chỉ đánh giá được sau khi tiệc đã hoàn thành");
        }
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BusinessException("Đơn này đã được đánh giá rồi");
        }

        reviewRepository.save(Review.builder()
                .booking(booking)
                .user(booking.getUser())
                .customerName(request.customerName().trim())
                .rating(request.rating())
                .content(request.content().trim())
                .eventType(booking.getEventType().name())
                .approved(false)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
