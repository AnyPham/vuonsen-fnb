package vn.vuonsen.fnb.modules.assistant.llm;

import com.anthropic.models.messages.MessageCreateParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.config.props.AssistantProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Kiểm thử hình dạng yêu cầu gửi Claude, không gọi mạng và không cần khóa API
class AnthropicLlmClientTest {

    private static AssistantProperties.Llm cauHinh(String model) {
        return new AssistantProperties.Llm(true, "khoa-thu", null, model, "low", 4000, 20);
    }

    @Test
    @DisplayName("UT-AI-04 Yêu cầu gửi Claude có chỉ dẫn hệ thống, lịch sử đúng thứ tự và câu hỏi mới ở cuối")
    void dungYeuCauDayDu() {
        MessageCreateParams yeuCau = AnthropicLlmClient.dungYeuCau(cauHinh("claude-opus-5"), "Luật và dữ liệu",
                List.of(new LuotHoiThoai(true, "Có gói tiệc nào?"), new LuotHoiThoai(false, "Dạ có ba gói ạ.")),
                "Gói nào rẻ nhất?");

        assertThat(yeuCau.model().asString()).isEqualTo("claude-opus-5");
        assertThat(yeuCau.maxTokens()).isEqualTo(4000L);
        assertThat(yeuCau.system()).isPresent();
        assertThat(yeuCau.messages()).extracting(m -> m.role().asString())
                .containsExactly("user", "assistant", "user");
        assertThat(yeuCau.messages().get(2).content().asString()).isEqualTo("Gói nào rẻ nhất?");
    }

    @Test
    @DisplayName("UT-AI-05 Lịch sử lệch vai được xếp lại: bỏ lượt trợ lý mở đầu, ghép lượt trùng vai, bỏ câu khách chưa được trả lời")
    void xepLaiLichSuLechVai() {
        List<LuotHoiThoai> ra = AnthropicLlmClient.xepLuot(List.of(
                new LuotHoiThoai(false, "Chào bạn"),
                new LuotHoiThoai(true, "Có gói tiệc nào?"),
                new LuotHoiThoai(true, "Gói nào rẻ nhất?"),
                new LuotHoiThoai(false, "Dạ Gói Đồng Quê ạ."),
                new LuotHoiThoai(true, "Còn sảnh thì sao?")));

        assertThat(ra).containsExactly(
                new LuotHoiThoai(true, "Có gói tiệc nào?\n\nGói nào rẻ nhất?"),
                new LuotHoiThoai(false, "Dạ Gói Đồng Quê ạ."));
    }

    @Test
    @DisplayName("UT-AI-06 Claude Opus 5 bật dự phòng phía máy chủ khi bị từ chối, mô hình khác thì không")
    void batDuPhongTuChoiTheoMoHinh() {
        MessageCreateParams opus = AnthropicLlmClient.dungYeuCau(cauHinh("claude-opus-5"), "x", List.of(), "hỏi");
        MessageCreateParams sonnet = AnthropicLlmClient.dungYeuCau(cauHinh("claude-sonnet-5"), "x", List.of(), "hỏi");

        assertThat(opus._additionalBodyProperties()).containsKey("fallbacks");
        assertThat(opus._additionalHeaders().values("anthropic-beta"))
                .contains(AnthropicLlmClient.BETA_DU_PHONG_TU_CHOI);
        assertThat(sonnet._additionalBodyProperties()).doesNotContainKey("fallbacks");
    }
}
