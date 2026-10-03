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
 * Kiểm thử nhánh Ollama, tức mô hình mã nguồn mở chạy ngay trên máy.
 *
 * Không gọi Ollama thật: máy chạy kiểm thử không chắc đã cài Ollama, mà kiểm thử phụ thuộc
 * vào một chương trình phải cài thêm thì sớm muộn cũng có người bỏ qua. Hai việc lớp này
 * làm là dựng thân yêu cầu và đọc phản hồi, cả hai kiểm được bằng chuỗi JSON dựng sẵn.
 */
class OllamaLlmClientTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static OllamaLlmClient dungClient(String moHinh) {
        var cauHinh = new AssistantProperties(new AssistantProperties.Llm(
                true, AssistantProperties.Llm.OLLAMA, null, null, moHinh, "low", 0, 20));
        return new OllamaLlmClient(cauHinh, JSON);
    }

    // ---------------- Điều kiện bật ----------------

    /*
     * Điểm khác biệt quan trọng nhất so với hai nhà cung cấp kia: Ollama chạy trên máy nên
     * không có khóa API nào để kiểm tra. Trước khi sửa, điều kiện bật nằm ở cấu hình và bắt
     * buộc phải có khóa, nghĩa là đường này không bao giờ bật lên được.
     */
    @Test
    @DisplayName("Không cần khóa API vẫn sẵn sàng, vì mô hình chạy ngay trên máy")
    void khongCanKhoaVanSanSang() {
        assertThat(dungClient(null).sanSang()).isTrue();
    }

    @Test
    @DisplayName("Bỏ trống tên mô hình thì dùng bản nhỏ chạy vừa card đồ họa 4GB")
    void moHinhMacDinhChayVuaMayHoc() {
        assertThat(dungClient(null).tenDichVu()).isEqualTo("Ollama qwen2.5:3b");
    }

    @Test
    @DisplayName("Khai tên mô hình khác thì dùng đúng tên đó")
    void doiDuocMoHinh() {
        assertThat(dungClient("qwen2.5:7b").tenDichVu()).isEqualTo("Ollama qwen2.5:7b");
    }

    // ---------------- Dựng yêu cầu ----------------

    @Test
    @DisplayName("Chỉ dẫn hệ thống là tin nhắn vai system đứng đầu danh sách")
    void chiDanHeThongDungDau() throws Exception {
        String than = dungClient(null).dungThanYeuCau("LUẬT: chỉ dùng dữ liệu", List.of(), "Sảnh nào lớn nhất?");
        JsonNode tinNhan = JSON.readTree(than).path("messages");

        assertThat(tinNhan).hasSize(2);
        assertThat(tinNhan.path(0).path("role").asText()).isEqualTo("system");
        assertThat(tinNhan.path(0).path("content").asText()).isEqualTo("LUẬT: chỉ dùng dữ liệu");
        assertThat(tinNhan.path(1).path("role").asText()).isEqualTo("user");
    }

    @Test
    @DisplayName("Lượt của mô hình mang vai assistant")
    void luotCuaMoHinhMangVaiAssistant() throws Exception {
        var lichSu = List.of(
                new LuotHoiThoai(true, "Có gói tiệc nào?"),
                new LuotHoiThoai(false, "Bên mình có ba gói."));

        JsonNode tinNhan = JSON.readTree(
                dungClient(null).dungThanYeuCau("chỉ dẫn", lichSu, "Gói rẻ nhất bao nhiêu?")).path("messages");

        assertThat(tinNhan).hasSize(4);
        assertThat(tinNhan.path(1).path("role").asText()).isEqualTo("user");
        assertThat(tinNhan.path(2).path("role").asText()).isEqualTo("assistant");
        assertThat(tinNhan.path(3).path("content").asText()).isEqualTo("Gói rẻ nhất bao nhiêu?");
    }

    /*
     * Chế độ truyền dòng trả về nhiều dòng JSON nối nhau. Bật nhầm thì phần đọc phản hồi
     * chỉ lấy được mảnh đầu tiên, mà lỗi đó không lộ ra ngay: câu trả lời vẫn có chữ, chỉ
     * là cụt mất gần hết.
     */
    @Test
    @DisplayName("Tắt truyền dòng để nhận trọn câu trả lời trong một lần")
    void tatTruyenDong() throws Exception {
        String than = dungClient(null).dungThanYeuCau("chỉ dẫn", List.of(), "Giá bao nhiêu?");
        assertThat(JSON.readTree(than).path("stream").asBoolean(true)).isFalse();
    }

    @Test
    @DisplayName("Nhiệt độ để thấp vì đây là trợ lý tra cứu")
    void nhietDoThap() throws Exception {
        String than = dungClient(null).dungThanYeuCau("chỉ dẫn", List.of(), "Giá bao nhiêu?");
        assertThat(JSON.readTree(than).path("options").path("temperature").asDouble())
                .isLessThanOrEqualTo(0.3);
    }

    // ---------------- Đọc phản hồi ----------------

    @Test
    @DisplayName("Đọc được câu trả lời từ trường message")
    void docDuocCauTraLoi() {
        String than = """
                {"model":"qwen2.5:3b","message":{"role":"assistant",
                "content":"Sảnh Ven Sông chứa 300 đến 800 khách."},"done":true}""";

        assertThat(dungClient(null).docLoiVan(than))
                .isEqualTo("Sảnh Ven Sông chứa 300 đến 800 khách.");
    }

    @Test
    @DisplayName("Câu trả lời rỗng thì coi như hỏng, để trợ lý chuyển sang cơ chế dự phòng")
    void cauTraLoiRongThiCoiNhuHong() {
        String than = """
                {"message":{"role":"assistant","content":"   "},"done":true}""";

        assertThatThrownBy(() -> dungClient(null).docLoiVan(than))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Trả về dữ liệu hỏng thì báo lỗi rõ ràng")
    void duLieuHongThiBaoLoi() {
        assertThatThrownBy(() -> dungClient(null).docLoiVan("khong phai JSON"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("không đọc được");
    }

    /*
     * Ollama chưa chạy là tình huống thường gặp nhất với đường này, nên câu báo lỗi phải
     * nói thẳng phải làm gì chứ không chỉ ném ra một ngoại lệ kết nối trơ trọi.
     */
    @Test
    @DisplayName("Ollama chưa chạy thì câu báo lỗi chỉ rõ phải kiểm tra gì")
    void ollamaChuaChayThiBaoRo() {
        var cauHinh = new AssistantProperties(new AssistantProperties.Llm(
                true, AssistantProperties.Llm.OLLAMA, null, "http://localhost:1", null, "low", 0, 1));
        var client = new OllamaLlmClient(cauHinh, JSON);

        assertThatThrownBy(() -> client.hoi("chỉ dẫn", List.of(), "Giá bao nhiêu?"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ollama đã chạy chưa");
    }
}
