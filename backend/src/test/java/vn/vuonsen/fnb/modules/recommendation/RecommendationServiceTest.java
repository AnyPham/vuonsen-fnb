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

    // ---------- Các kiểm thử thêm sau đợt tinh chỉnh trọng số ----------

    @Test
    @DisplayName("Đủ không gian thì ba phương án là ba không gian khác nhau")
    void topThreeCoverDifferentSpaces() {
        // 40 khách thì cả sáu không gian đều chứa được, nên phải ra ba sảnh khác nhau.
        // Trước khi tinh chỉnh, cả ba phương án rơi vào cùng một sảnh chỉ khác gói
        // tiệc, khách không có gì để so sánh.
        var result = recommendationService.suggest(new RecommendationRequest(
                40, EventType.FAMILY, null, LocalDate.now().plusDays(30)));

        assertThat(result).extracting(s -> s.space().getId())
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Ít không gian đủ sức chứa thì vẫn trả đủ ba phương án")
    void stillReturnsThreeWhenFewSpacesFit() {
        // 300 khách thì chỉ Sảnh Ven Sông và Sảnh Sen Vàng chứa nổi. Không đủ ba
        // sảnh khác nhau nên hệ thống nới giới hạn, lấy thêm gói tiệc khác của
        // sảnh đã dùng, miễn là vẫn đủ ba phương án cho khách chọn.
        var result = recommendationService.suggest(new RecommendationRequest(
                300, EventType.WEDDING, null, LocalDate.now().plusDays(30)));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(s -> s.space().getId())
                .as("hai phương án đầu vẫn phải là hai sảnh khác nhau")
                .containsAnyOf(result.get(0).space().getId());
        assertThat(result.get(0).space().getId())
                .isNotEqualTo(result.get(1).space().getId());
    }

    @Test
    @DisplayName("Điểm ba phương án phải khác nhau, không được hòa hết")
    void scoresMustDiscriminate() {
        var result = recommendationService.suggest(new RecommendationRequest(
                300, EventType.WEDDING, null, LocalDate.now().plusDays(30)));

        // Lỗi cũ: khách không khai ngân sách thì mọi tổ hợp cùng 62,5 điểm,
        // thứ tự ba phương án trở thành ngẫu nhiên
        assertThat(result).extracting(Suggestion::score).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Chưa chọn loại sự kiện vẫn gợi ý được theo số khách")
    void suggestsWithoutEventType() {
        // Trang danh sách không gian gọi API khi khách mới điền số khách, chưa
        // chọn loại sự kiện. Đường đi này phải chạy được chứ không báo lỗi.
        var result = recommendationService.suggest(new RecommendationRequest(
                40, null, null, LocalDate.now().plusDays(30)));

        assertThat(result).hasSize(3);
        assertThat(result).allSatisfy(s ->
                assertThat(s.score()).isBetween(BigDecimal.ZERO, new BigDecimal("100")));
        // Không biết loại tiệc thì chưa dựng được thực đơn, phải trả rỗng chứ
        // không được lỗi
        assertThat(result).allSatisfy(s -> assertThat(s.dishes()).isEmpty());
    }

    @Test
    @DisplayName("Điểm luôn nằm trong thang 0 đến 100 dù có tiêu chí bị bỏ qua")
    void scoreStaysWithinHundred() {
        // Không khai ngân sách nên tiêu chí ngân sách bị bỏ qua, điểm vẫn phải
        // quy về thang 100 chứ không tụt xuống vì thiếu tiêu chí
        for (EventType loai : EventType.values()) {
            var result = recommendationService.suggest(new RecommendationRequest(
                    200, loai, null, LocalDate.now().plusDays(30)));

            assertThat(result).allSatisfy(s -> {
                assertThat(s.score()).isBetween(BigDecimal.ZERO, new BigDecimal("100"));
            });
        }
    }

    @Test
    @DisplayName("Tiệc cưới được gợi ý gói cao cấp, họp mặt gia đình được gói phổ thông")
    void packageTierFollowsEventType() {
        var tiecCuoi = recommendationService.suggest(new RecommendationRequest(
                300, EventType.WEDDING, null, LocalDate.now().plusDays(30)));
        var giaDinh = recommendationService.suggest(new RecommendationRequest(
                40, EventType.FAMILY, null, LocalDate.now().plusDays(30)));

        // Gói Thượng Uyển 6,8 triệu một mâm, Gói Đồng Quê 2,9 triệu
        assertThat(tiecCuoi.get(0).partyPackage().getPricePerTable())
                .as("tiệc cưới nên được gợi ý gói đắt hơn")
                .isGreaterThan(giaDinh.get(0).partyPackage().getPricePerTable());
    }

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
