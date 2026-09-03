package vn.vuonsen.fnb.modules.assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Kiểm thử trợ lý tư vấn trên dữ liệu mẫu
@SpringBootTest
class AssistantServiceTest {

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private IntentDetector detector;

    @Test
    @DisplayName("Nhận ra được từng loại câu hỏi")
    void detectsEachIntent() {
        assertThat(detector.detect("Xin chào")).isEqualTo(Intent.CHAO_HOI);
        assertThat(detector.detect("Sảnh nào chứa được 300 khách?")).isEqualTo(Intent.KHONG_GIAN);
        assertThat(detector.detect("Có gói tiệc nào?")).isEqualTo(Intent.GOI_TIEC);
        assertThat(detector.detect("Thực đơn có món gì?")).isEqualTo(Intent.THUC_DON);
        assertThat(detector.detect("Đặt cọc bao nhiêu?")).isEqualTo(Intent.DAT_COC);
        assertThat(detector.detect("Có khuyến mãi gì không?")).isEqualTo(Intent.KHUYEN_MAI);
        assertThat(detector.detect("Đặt tiệc như thế nào?")).isEqualTo(Intent.QUY_TRINH_DAT);
        assertThat(detector.detect("Địa chỉ ở đâu?")).isEqualTo(Intent.LIEN_HE);
    }

    @Test
    @DisplayName("Câu hỏi chi phí không được nhận nhầm thành lời chào")
    void costQuestionIsNotMistakenForGreeting() {
        // Lỗi từng gặp: từ khóa chào hỏi "hi" khớp vào giữa chữ "chi phí" vì so
        // theo kiểu chứa chuỗi. Nay so theo từ trọn vẹn nên không còn.
        assertThat(detector.detect("Chi phí một tiệc bao nhiêu?")).isEqualTo(Intent.CHI_PHI);
        assertThat(detector.detect("Chi phí thế nào?")).isEqualTo(Intent.CHI_PHI);
    }

    @Test
    @DisplayName("Câu hỏi thời gian báo trước không bị nhận nhầm thành hỏi món lẩu")
    void leadTimeQuestionIsNotMistakenForHotpot() {
        // Chữ "lâu" trong "bao lâu" bỏ dấu thành "lau", trùng với món lẩu
        assertThat(detector.detect("Đặt trước bao lâu?")).isEqualTo(Intent.QUY_TRINH_DAT);
    }

    @Test
    @DisplayName("Gõ không dấu vẫn hiểu được")
    void understandsTextWithoutDiacritics() {
        assertThat(detector.detect("sanh nao chua duoc 50 khach")).isEqualTo(Intent.KHONG_GIAN);
        assertThat(detector.detect("co goi tiec nao khong")).isEqualTo(Intent.GOI_TIEC);
        assertThat(detector.detect("dat coc bao nhieu")).isEqualTo(Intent.DAT_COC);
    }

    @Test
    @DisplayName("Lấy đúng số khách trong câu hỏi")
    void readsGuestCountFromQuestion() {
        assertThat(detector.soTrongCau("Sảnh nào chứa được 300 khách?")).isEqualTo(300);
        assertThat(detector.soTrongCau("Có gói tiệc nào?")).isZero();
    }

    @Test
    @DisplayName("Hỏi sảnh kèm số khách thì chỉ liệt kê sảnh chứa nổi")
    void listsOnlySpacesThatFitTheGuestCount() {
        var kq = assistantService.answer("Sảnh nào chứa được 300 khách?");

        assertThat(kq.intent()).isEqualTo(Intent.KHONG_GIAN);
        // Chỉ Sảnh Ven Sông và Sảnh Sen Vàng chứa nổi 300 khách
        assertThat(kq.answer()).contains("Sảnh Ven Sông", "Sảnh Sen Vàng");
        assertThat(kq.answer()).doesNotContain("Nhà Rường Gỗ");
        assertThat(kq.link()).isEqualTo("/khong-gian");
    }

    @Test
    @DisplayName("Số khách vượt sức chứa lớn nhất thì báo rõ chứ không gợi ý bừa")
    void saysSoWhenGuestCountExceedsEverySpace() {
        var kq = assistantService.answer("Chỗ bạn có chứa nổi 2000 khách không?");

        assertThat(kq.intent()).isEqualTo(Intent.KHONG_GIAN);
        assertThat(kq.answer()).contains("2000");
        assertThat(kq.answer()).doesNotContain("Sảnh Ven Sông");
    }

    @Test
    @DisplayName("Câu trả lời lấy số liệu từ cấu hình chứ không viết cứng")
    void answersUseConfiguredBusinessRules() {
        assertThat(assistantService.answer("Đặt cọc bao nhiêu?").answer()).contains("30%");
        assertThat(assistantService.answer("Có khuyến mãi gì không?").answer()).contains("60", "5%");
        assertThat(assistantService.answer("Đặt tiệc như thế nào?").answer()).contains("3", "20", "7");
    }

    @Test
    @DisplayName("Câu trả lời lấy dữ liệu thật từ cơ sở dữ liệu")
    void answersUseRealData() {
        var goi = assistantService.answer("Có gói tiệc nào?");
        assertThat(goi.answer()).contains("Gói Đồng Quê", "Gói Sen Vàng", "Gói Thượng Uyển");

        var mon = assistantService.answer("Thực đơn có món gì?");
        assertThat(mon.answer()).contains("Khai vị".toLowerCase(), "Gỏi củ hũ dừa tôm thịt");
    }

    @Test
    @DisplayName("Câu ngoài phạm vi thì nhận là không hiểu và gợi ý câu hỏi khác")
    void offersSuggestionsWhenQuestionIsOutOfScope() {
        var kq = assistantService.answer("Hôm nay trời đẹp nhỉ");

        assertThat(kq.intent()).isEqualTo(Intent.KHONG_HIEU);
        assertThat(kq.suggestions()).isNotEmpty();
        // Không bịa câu trả lời, mà chỉ ra số điện thoại để gặp người thật
        assertThat(kq.answer()).contains("chưa hiểu");
    }

    @Test
    @DisplayName("Mọi câu trả lời đều kèm câu hỏi gợi ý tiếp theo")
    void everyAnswerHasSuggestions() {
        for (String cau : new String[] {
                "Xin chào", "Có gói tiệc nào?", "Thực đơn có món gì?",
                "Chi phí bao nhiêu?", "Đặt cọc bao nhiêu?", "Địa chỉ ở đâu?" }) {
            assertThat(assistantService.answer(cau).suggestions())
                    .as("câu hỏi: %s", cau)
                    .isNotEmpty();
        }
    }
}
