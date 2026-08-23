package vn.vuonsen.fnb.modules.recommendation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.recommendation.dto.RecommendationRequest;
import vn.vuonsen.fnb.modules.recommendation.dto.Suggestion;
import vn.vuonsen.fnb.modules.recommendation.dto.SuggestionResponse;

import java.util.List;

// API gợi ý không gian và gói tiệc, khách chưa đăng nhập cũng dùng được
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "12. Gợi ý")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "Gợi ý ba tổ hợp không gian và gói tiệc phù hợp nhất")
    public ResponseEntity<List<SuggestionResponse>> suggest(
            @Valid @RequestBody RecommendationRequest request) {

        List<Suggestion> suggestions = recommendationService.suggest(request);

        // Ghi lại gợi ý đứng đầu để sau này đối chiếu với đơn khách thật sự đặt
        if (!suggestions.isEmpty()) {
            recommendationService.log(request, suggestions.get(0));
        }

        return ResponseEntity.ok(suggestions.stream().map(SuggestionResponse::from).toList());
    }
}
