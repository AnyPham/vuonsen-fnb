package vn.vuonsen.fnb.modules.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.recommendation.dto.RecommendationRequest;
import vn.vuonsen.fnb.modules.recommendation.dto.Suggestion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Kiểm thử hệ thống gợi ý trên dữ liệu mẫu
@SpringBootTest
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;

    @Test
    @DisplayName("Trả về đúng ba gợi ý, xếp theo điểm giảm dần")
    void returnsTopThreeSortedByScore() {
        var result = recommendationService.suggest(new RecommendationRequest(
                300, EventType.WEDDING, null, LocalDate.now().plusDays(30)));

        assertThat(result).hasSize(3);
        for (int i = 1; i < result.size(); i++) {
            assertThat(result.get(i - 1).score())
                    .isGreaterThanOrEqualTo(result.get(i).score());
        }
    }

    @Test
    @DisplayName("Không gợi ý không gian nhỏ hơn số khách")
    void skipsSpacesThatCannotFitGuests() {
        // 500 khách thì Nhà Rường Gỗ (tối đa 60) và Vườn Cau (tối đa 150) phải bị loại
        var result = recommendationService.suggest(new RecommendationRequest(
                500, EventType.WEDDING, null, LocalDate.now().plusDays(30)));

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(s ->
                assertThat(s.space().getCapacityMax()).isGreaterThanOrEqualTo(500));
    }

    @Test
    @DisplayName("Tiệc gia đình nhỏ được gợi ý không gian ấm cúng, không phải sảnh lớn")
    void familyPartyGetsCosySpace() {
        var result = recommendationService.suggest(new RecommendationRequest(
                40, EventType.FAMILY, null, LocalDate.now().plusDays(30)));

        // Nhà Rường Gỗ và Cụm Chòi Sen đều chấm 5 điểm cho họp mặt gia đình
        assertThat(result.get(0).space().getCode())
                .isIn("NHA-RUONG-GO", "CUM-CHOI-SEN");
    }

    @Test
    @DisplayName("Hội nghị được gợi ý phòng hội nghị")
    void corporateEventGetsConferenceRoom() {
        var result = recommendationService.suggest(new RecommendationRequest(
                100, EventType.CORPORATE, null, LocalDate.now().plusDays(30)));

        assertThat(result).anySatisfy(s ->
                assertThat(s.space().getCode()).isEqualTo("PHONG-HOI-NGHI"));
    }

    @Test
    @DisplayName("Khai ngân sách thì gợi ý đầu tiên phải nằm trong ngân sách")
    void respectsBudget() {
        var result = recommendationService.suggest(new RecommendationRequest(
                200, EventType.WEDDING, new BigDecimal("120000000"), LocalDate.now().plusDays(30)));

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).totalAmount())
                .isLessThanOrEqualTo(new BigDecimal("120000000"));
        assertThat(result.get(0).reasons()).contains("Nằm trong ngân sách");
    }

    @Test
    @DisplayName("Mỗi gợi ý đều kèm lý do để khách hiểu vì sao được đề xuất")
    void everySuggestionHasReasons() {
        List<Suggestion> result = recommendationService.suggest(new RecommendationRequest(
                250, EventType.BIRTHDAY, null, LocalDate.now().plusDays(30)));

        assertThat(result).allSatisfy(s -> assertThat(s.reasons()).isNotEmpty());
    }
}
