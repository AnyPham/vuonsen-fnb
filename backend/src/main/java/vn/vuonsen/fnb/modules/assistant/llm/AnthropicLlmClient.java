package vn.vuonsen.fnb.modules.assistant.llm;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import vn.vuonsen.fnb.config.props.AssistantProperties;

import java.util.List;
import java.util.Map;

/*
 * Gọi Messages API của Anthropic.
 *
 * Chỉ lo phần truyền nhận: dựng yêu cầu, gửi đi, lấy đoạn chữ trong câu trả lời.
 * Việc quyết định có gọi hay không, và gọi hỏng thì làm gì, để cho LlmAssistant lo.
 *
 * Đặt thời gian chờ ngắn vì khách đang ngồi đợi trong hộp thoại. Chờ quá hạn thì
 * thà chuyển sang câu trả lời dự phòng còn hơn để khách nhìn màn hình trống.
 */
@Component
public class AnthropicLlmClient implements LlmClient {

    // Phiên bản API ghi theo tài liệu của Anthropic, gửi kèm mỗi yêu cầu
    private static final String PHIEN_BAN_API = "2023-06-01";
    private static final int CHO_KET_NOI_MS = 5_000;

    private final AssistantProperties.Llm cauHinh;
    private final RestClient restClient;

    public AnthropicLlmClient(AssistantProperties properties) {
        this.cauHinh = properties.llm();

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CHO_KET_NOI_MS);
        factory.setReadTimeout(Math.max(1, cauHinh.timeoutSeconds()) * 1000);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(cauHinh.baseUrl() == null ? "https://api.anthropic.com" : cauHinh.baseUrl())
                .build();
    }

    @Override
    public String hoi(String chiDanHeThong, String cauHoi) {
        Map<String, Object> yeuCau = Map.of(
                "model", cauHinh.model(),
                "max_tokens", cauHinh.maxTokens(),
                // Chỉ dẫn và dữ liệu nhà hàng nằm ở phần system, câu của khách nằm
                // riêng ở phần messages. Tách hai chỗ để khách không sửa được luật.
                "system", chiDanHeThong,
                "messages", List.of(Map.of("role", "user", "content", cauHoi)));

        KetQua ketQua = restClient.post()
                .uri("/v1/messages")
                .header("x-api-key", cauHinh.apiKey())
                .header("anthropic-version", PHIEN_BAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .body(yeuCau)
                .retrieve()
                .body(KetQua.class);

        if (ketQua == null || ketQua.content() == null) {
            throw new IllegalStateException("Dịch vụ trả về câu trả lời rỗng");
        }

        // Câu trả lời là một danh sách khối, ghép các khối chữ lại với nhau
        return ketQua.content().stream()
                .filter(k -> "text".equals(k.type()) && k.text() != null)
                .map(Khoi::text)
                .reduce((a, b) -> a + "\n" + b)
                .orElseThrow(() -> new IllegalStateException("Câu trả lời không có phần chữ nào"))
                .trim();
    }

    @Override
    public String tenDichVu() {
        return "Anthropic " + cauHinh.model();
    }

    // Chỉ khai báo những trường thực sự đọc tới, phần còn lại của JSON bỏ qua
    private record KetQua(List<Khoi> content) {
    }

    private record Khoi(String type, String text) {
    }
}
