package vn.vuonsen.fnb.modules.recommendation.dto;

import java.math.BigDecimal;
import java.util.List;

// Dạng JSON trả về cho giao diện
public record SuggestionResponse(
        Long spaceId,
        String spaceName,
        String spaceSlug,
        Long packageId,
        String packageName,
        int tableCount,
        BigDecimal totalAmount,
        BigDecimal score,
        List<String> reasons,
        // Thực đơn gợi ý sẵn cho gói này
        List<DishBrief> menu
) {
    public static SuggestionResponse from(Suggestion s) {
        return new SuggestionResponse(
                s.space().getId(), s.space().getName(), s.space().getSlug(),
                s.partyPackage().getId(), s.partyPackage().getName(),
                s.tableCount(), s.totalAmount(), s.score(), s.reasons(),
                s.dishes().stream().map(DishBrief::from).toList());
    }
}
