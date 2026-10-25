package vn.vuonsen.fnb.modules.assistant.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.vuonsen.fnb.config.props.AssistantProperties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/*
 * Gọi mô hình ngôn ngữ mã nguồn mở chạy ngay trên máy, qua Ollama.
 *
 * Vì sao có lớp này: đây là đường duy nhất không tốn một đồng nào và không cần đăng ký
 * tài khoản ở đâu cả. Ollama tải mô hình về máy rồi phục vụ qua cổng 11434; toàn bộ câu
 * hỏi của khách không rời khỏi máy, nên cũng không có chuyện dữ liệu nhà hàng bị gửi ra
 * ngoài. Chạy được cả khi mất mạng, hợp cho buổi bảo vệ.
 *
 * Đổi lại, chất lượng phụ thuộc vào mô hình mà máy gánh nổi. Máy học thường chỉ chạy được
 * mô hình cỡ ba tỉ tham số, tiếng Việt của nhóm đó kém hơn hẳn dịch vụ đám mây. Vì vậy mặc
 * định để đây là lựa chọn dự phòng chứ không phải lựa chọn đầu tiên.
 *
 * Giao thức đơn giản hơn Gemini: chỉ dẫn hệ thống là một tin nhắn vai "system" đứng đầu
 * danh sách, giống hệt cách hầu hết thư viện trò chuyện làm.
 *
 * Đặt stream về false để nhận trọn câu trả lời trong một lần. Chế độ truyền dòng trả về
 * nhiều dòng JSON nối nhau, xử lý phức tạp hơn mà ở đây không được lợi gì: trợ lý chỉ hiện
 * câu trả lời khi đã có đủ.
 */
@Component
@ConditionalOnProperty(name = "app.assistant.llm.provider", havingValue = AssistantProperties.Llm.OLLAMA)
public class OllamaLlmClient implements LlmClient {

    private static final String DIA_CHI_MAC_DINH = "http://localhost:11434";

    /*
     * Qwen2.5 bản ba tỉ tham số: cỡ này nén lại còn khoảng hai gigabyte nên chạy vừa card
     * đồ họa bốn gigabyte, mà tiếng Việt khá nhất trong nhóm mô hình nhỏ. Máy khỏe hơn thì
     * đổi sang bản bảy tỉ tham số bằng cấu hình, không phải sửa mã.
     */
    private static final String MO_HINH_MAC_DINH = "qwen2.5:3b";

    private static final int SO_CHU_TOI_DA_MAC_DINH = 800;

    private final AssistantProperties.Llm cauHinh;
    private final ObjectMapper json;
    private final HttpClient http;

    public OllamaLlmClient(AssistantProperties properties, ObjectMapper json) {
        this.cauHinh = properties.llm();
        this.json = json;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(cauHinh.timeoutAnToan()))
                .build();
    }

    /*
     * Luôn coi là sẵn sàng vì Ollama không có khóa API để mà kiểm tra.
     *
     * Cố ý không thử kết nối ở đây: hàm này chạy mỗi lần khách hỏi, gọi mạng trong đó thì
     * mỗi câu hỏi phải chờ hai lượt. Ollama chưa chạy thì lời gọi thật sẽ hỏng và trợ lý
     * tự chuyển sang cơ chế dự phòng, khách vẫn có câu trả lời.
     */
    @Override
    public boolean sanSang() {
        return true;
    }

    @Override
    public String tenDichVu() {
        return "Ollama " + cauHinh.modelHoac(MO_HINH_MAC_DINH);
    }

    @Override
    public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
        String than = dungThanYeuCau(chiDanHeThong, lichSu, cauHoi);
        HttpResponse<String> phanHoi = gui(than);

        if (phanHoi.statusCode() != 200) {
            throw new IllegalStateException("Ollama trả mã " + phanHoi.statusCode());
        }
        return docLoiVan(phanHoi.body());
    }

    // ---------------- Dựng yêu cầu ----------------

    String dungThanYeuCau(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
        ObjectNode goc = json.createObjectNode();
        goc.put("model", cauHinh.modelHoac(MO_HINH_MAC_DINH));
        goc.put("stream", false);

        ArrayNode tinNhan = goc.putArray("messages");
        tinNhan.addObject().put("role", "system").put("content", chiDanHeThong);
        for (LuotHoiThoai luot : lichSu) {
            tinNhan.addObject()
                    .put("role", luot.cuaKhach() ? "user" : "assistant")
                    .put("content", luot.noiDung());
        }
        tinNhan.addObject().put("role", "user").put("content", cauHoi);

        ObjectNode tuyChon = goc.putObject("options");
        // Trợ lý tra cứu nên để nhiệt độ thấp, cùng câu hỏi thì cùng con số
        tuyChon.put("temperature", 0.3);
        tuyChon.put("num_predict", cauHinh.maxTokensHoac(SO_CHU_TOI_DA_MAC_DINH));

        return goc.toString();
    }

    private HttpResponse<String> gui(String than) {
        String diaChi = cauHinh.baseUrlHoac(DIA_CHI_MAC_DINH).replaceAll("/+$", "") + "/api/chat";

        HttpRequest yeuCau = HttpRequest.newBuilder(URI.create(diaChi))
                .header("Content-Type", "application/json")
                /*
                 * Cho thêm thời gian so với dịch vụ đám mây. Mô hình chạy trên máy học phải
                 * nạp vào bộ nhớ card đồ họa ở lần gọi đầu, riêng bước đó đã mất vài giây.
                 */
                .timeout(Duration.ofSeconds(cauHinh.timeoutAnToan() * 3L))
                .POST(HttpRequest.BodyPublishers.ofString(than, StandardCharsets.UTF_8))
                .build();

        try {
            return http.send(yeuCau, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bị ngắt khi đang gọi Ollama", e);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Không gọi được Ollama ở " + diaChi + ". Kiểm tra Ollama đã chạy chưa", e);
        }
    }

    // ---------------- Đọc câu trả lời ----------------

    String docLoiVan(String than) {
        JsonNode goc;
        try {
            goc = json.readTree(than);
        } catch (Exception e) {
            throw new IllegalStateException("Ollama trả về dữ liệu không đọc được", e);
        }

        String loiVan = goc.path("message").path("content").asText("").trim();
        if (loiVan.isEmpty()) {
            throw new IllegalStateException("Câu trả lời của Ollama không có phần chữ nào");
        }
        return loiVan;
    }
}
