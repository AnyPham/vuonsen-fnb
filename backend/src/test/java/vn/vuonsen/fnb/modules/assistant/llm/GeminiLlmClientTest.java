package vn.vuonsen.fnb.modules.assistant.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.config.props.AssistantProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Kiểm thử phần dựng yêu cầu và đọc phản hồi của nhánh Gemini.
 *
 * Không gọi mạng thật. Hai việc duy nhất lớp này làm là đổi dữ liệu sang đúng định dạng
 * của Gemini rồi đọc ngược lại, mà cả hai đều kiểm được bằng chuỗi JSON dựng sẵn. Bắt
 * phải có khóa API thật mới chạy được kiểm thử thì sẽ không ai chạy.
 *
 * Định dạng của Gemini khác Anthropic ở mấy chỗ dễ viết nhầm, nên kiểm từng chỗ một:
 * chỉ dẫn hệ thống nằm ở trường riêng, vai của mô hình gọi là "model" chứ không phải
 * "assistant", và câu chữ nằm trong candidates[0].content.parts.
 */
class GeminiLlmClientTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static GeminiLlmClient dungClient(String khoa) {
        var cauHinh = new AssistantProperties(new AssistantProperties.Llm(
                true, AssistantProperties.Llm.GEMINI, khoa, null, null, "low", 0, 20));
        return new GeminiLlmClient(cauHinh, JSON);
    }

    // ---------------- Điều kiện bật ----------------

    @Test
    @DisplayName("Không có khóa thì báo chưa sẵn sàng, kèm chỗ lấy khóa miễn phí")
    void chuaCoKhoaThiChuaSanSang() {
        assertThat(dungClient(null).sanSang()).isFalse();
        assertThat(dungClient("  ").sanSang()).isFalse();
        assertThat(dungClient(null).huongDanBat()).contains("aistudio.google.com");
    }

    @Test
    @DisplayName("Có khóa thì sẵn sàng")
    void coKhoaThiSanSang() {
        assertThat(dungClient("khoa-thu").sanSang()).isTrue();
    }

    @Test
    @DisplayName("Bỏ trống tên mô hình thì dùng bản Flash nằm trong hạn mức miễn phí")
    void moHinhMacDinhLaBanMienPhi() {
        assertThat(dungClient("khoa-thu").tenDichVu()).isEqualTo("Google gemini-3.5-flash-lite");
    }

    // ---------------- Thử lại khi gặp lỗi nhất thời ----------------

    /*
     * Ngày 03/10/2026 chạy thử với khóa thật, nhiều câu hỏi liên tiếp bị Gemini trả 503.
     * Lỗi nhất thời bên Google chứ không phải hệ thống hỏng, nên gọi lại là được.
     */
    @Test
    @DisplayName("Máy chủ quá tải thì gọi lại")
    void thuLaiKhiQuaTai() {
        assertThat(GeminiLlmClient.nenThuLai(503)).isTrue();
    }

    /*
     * Phép kiểm quan trọng nhất của nhóm này, vì nó canh một chỗ đã làm sai một lần.
     *
     * Bản đầu xếp 429 vào nhóm đáng gọi lại, lý do nghe xuôi: 429 cũng là lỗi nhất thời.
     * Nhưng 429 của hạn mức miễn phí tính theo số lượt mỗi phút, nên gọi lại sau vài trăm
     * mili giây chắc chắn trượt tiếp mà vẫn bị trừ lượt. Đo thật mới thấy: hỏi tám câu
     * liên tiếp thì năm câu trả 429, và chính phần thử lại đã góp phần đốt hạn mức.
     */
    @Test
    @DisplayName("Chạm trần số lượt thì không gọi lại, gọi lại chỉ tốn thêm lượt")
    void khongThuLaiKhiChamTranSoLuot() {
        assertThat(GeminiLlmClient.nenThuLai(429)).isFalse();
    }

    /*
     * Bốn mã này là bốn tình huống đã gặp thật khi dựng phần Gemini: thân yêu cầu sai,
     * khóa hỏng, khóa không đủ quyền, và mô hình bị gỡ khỏi tài khoản mới.
     */
    @Test
    @DisplayName("Lỗi cố định thì không gọi lại, gọi lại bao nhiêu lần cũng vậy")
    void khongThuLaiKhiLoiCoDinh() {
        assertThat(GeminiLlmClient.nenThuLai(400)).as("400 thân yêu cầu sai").isFalse();
        assertThat(GeminiLlmClient.nenThuLai(401)).as("401 khóa hỏng").isFalse();
        assertThat(GeminiLlmClient.nenThuLai(403)).as("403 khóa không đủ quyền").isFalse();
        assertThat(GeminiLlmClient.nenThuLai(404)).as("404 mô hình không còn").isFalse();
    }

    @Test
    @DisplayName("Gọi thành công thì không gọi lại")
    void khongThuLaiKhiThanhCong() {
        assertThat(GeminiLlmClient.nenThuLai(200)).isFalse();
    }

    // ---------------- Dựng yêu cầu ----------------

    @Test
    @DisplayName("Chỉ dẫn hệ thống nằm ở trường riêng, không lẫn vào các lượt trò chuyện")
    void chiDanHeThongNamRieng() throws Exception {
        String than = dungClient("khoa-thu").dungThanYeuCau("LUẬT: chỉ dùng dữ liệu", List.of(), "Sảnh nào lớn nhất?");
        JsonNode goc = JSON.readTree(than);

        assertThat(goc.path("systemInstruction").path("parts").path(0).path("text").asText())
                .isEqualTo("LUẬT: chỉ dùng dữ liệu");
        // Chỉ có đúng một lượt là câu hỏi hiện tại, chỉ dẫn không bị nhét vào đây
        assertThat(goc.path("contents")).hasSize(1);
    }

    @Test
    @DisplayName("Lượt của mô hình mang vai model, không phải assistant")
    void luotCuaMoHinhMangVaiModel() throws Exception {
        var lichSu = List.of(
                new LuotHoiThoai(true, "Có gói tiệc nào?"),
                new LuotHoiThoai(false, "Bên mình có ba gói."));

        String than = dungClient("khoa-thu").dungThanYeuCau("chỉ dẫn", lichSu, "Gói rẻ nhất bao nhiêu?");
        JsonNode cacLuot = JSON.readTree(than).path("contents");

        assertThat(cacLuot).hasSize(3);
        assertThat(cacLuot.path(0).path("role").asText()).isEqualTo("user");
        assertThat(cacLuot.path(1).path("role").asText()).isEqualTo("model");
        assertThat(cacLuot.path(2).path("role").asText()).isEqualTo("user");
        assertThat(cacLuot.path(2).path("parts").path(0).path("text").asText())
                .isEqualTo("Gói rẻ nhất bao nhiêu?");
    }

    @Test
    @DisplayName("Nhiệt độ để thấp vì đây là trợ lý tra cứu, cùng câu hỏi phải ra cùng con số")
    void nhietDoThap() throws Exception {
        String than = dungClient("khoa-thu").dungThanYeuCau("chỉ dẫn", List.of(), "Giá bao nhiêu?");
        assertThat(JSON.readTree(than).path("generationConfig").path("temperature").asDouble())
                .isLessThanOrEqualTo(0.3);
    }

    // ---------------- Đọc phản hồi ----------------

    @Test
    @DisplayName("Đọc được câu trả lời từ candidates")
    void docDuocCauTraLoi() {
        String than = """
                {"candidates":[{"content":{"parts":[{"text":"Sảnh Ven Sông chứa 300 đến 800 khách."}],
                "role":"model"},"finishReason":"STOP"}]}""";

        assertThat(dungClient("khoa-thu").docLoiVan(than))
                .isEqualTo("Sảnh Ven Sông chứa 300 đến 800 khách.");
    }

    /*
     * Dừng vì hết trần độ dài thì câu chữ cụt giữa chừng. Coi như hỏng để trợ lý chuyển
     * sang cơ chế dự phòng, còn hơn đưa cho khách một câu chưa nói hết ý.
     */
    @Test
    @DisplayName("Dừng giữa chừng thì coi như hỏng, không đưa câu cụt cho khách")
    void dungGiuaChungThiCoiNhuHong() {
        String than = """
                {"candidates":[{"content":{"parts":[{"text":"Sảnh Ven Sông chứa 300 đến"}]},
                "finishReason":"MAX_TOKENS"}]}""";

        assertThatThrownBy(() -> dungClient("khoa-thu").docLoiVan(than))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MAX_TOKENS");
    }

    @Test
    @DisplayName("Bị bộ lọc an toàn chặn thì cũng coi như hỏng")
    void biChanThiCoiNhuHong() {
        String than = """
                {"candidates":[{"finishReason":"SAFETY"}]}""";

        assertThatThrownBy(() -> dungClient("khoa-thu").docLoiVan(than))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SAFETY");
    }

    @Test
    @DisplayName("Không có câu trả lời nào thì báo lỗi rõ ràng")
    void khongCoCauTraLoi() {
        assertThatThrownBy(() -> dungClient("khoa-thu").docLoiVan("{\"candidates\":[]}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Trả về dữ liệu hỏng thì báo lỗi chứ không ném lỗi phân tích JSON ra ngoài")
    void duLieuHongThiBaoLoi() {
        assertThatThrownBy(() -> dungClient("khoa-thu").docLoiVan("khong phai JSON"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("không đọc được");
    }
}
