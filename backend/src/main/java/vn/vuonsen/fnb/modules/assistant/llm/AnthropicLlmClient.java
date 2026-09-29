package vn.vuonsen.fnb.modules.assistant.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import vn.vuonsen.fnb.config.props.AssistantProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Gọi Claude qua SDK Java chính thức của Anthropic.
 *
 * Chỉ lo phần truyền nhận: dựng yêu cầu, gửi đi, lấy đoạn chữ trong câu trả lời. Việc quyết
 * định có gọi hay không, và gọi hỏng thì làm gì, để cho LlmAssistant lo.
 *
 * Mấy lựa chọn đáng chú ý:
 * - Chỉ dẫn hệ thống, gồm luật và toàn bộ dữ liệu nhà hàng, được đánh dấu lưu đệm. Phần này
 *   dài và giống nhau giữa các câu hỏi trong ngày, nên các câu sau đọc lại rẻ và nhanh hơn.
 * - Mức công sức suy luận để thấp: câu tư vấn ngắn không cần suy luận sâu, khách lại đang đợi.
 * - Không tự thử lại khi lỗi. Hỏng là trả về ngay để LlmAssistant chuyển sang câu trả lời dự
 *   phòng, còn hơn để khách chờ thêm vài lượt thử lại.
 * - Với Claude Opus 5, bật dự phòng phía máy chủ khi mô hình từ chối trả lời: yêu cầu bị từ
 *   chối được chạy lại trên mô hình khác do Anthropic chọn theo loại từ chối.
 */
@Component
public class AnthropicLlmClient implements LlmClient, DisposableBean {

    static final String BETA_DU_PHONG_TU_CHOI = "server-side-fallback-2026-07-01";
    private static final String DIA_CHI_MAC_DINH = "https://api.anthropic.com";

    private final AssistantProperties.Llm cauHinh;

    // Chỉ tạo khi thật sự gọi tới, máy không có khóa API thì không bao giờ tạo
    private volatile AnthropicClient client;

    public AnthropicLlmClient(AssistantProperties properties) {
        this.cauHinh = properties.llm();
    }

    @Override
    public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
        Message phanHoi = client().messages().create(dungYeuCau(cauHinh, chiDanHeThong, lichSu, cauHoi));

        // Dừng vì hết trần độ dài hay vì bị từ chối thì câu chữ không trọn vẹn, coi như hỏng
        StopReason lyDo = phanHoi.stopReason().orElse(null);
        if (!StopReason.END_TURN.equals(lyDo)) {
            throw new IllegalStateException("Mô hình dừng giữa chừng: " + lyDo);
        }

        String loiVan = phanHoi.content().stream()
                .flatMap(khoi -> khoi.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining("\n"))
                .trim();
        if (loiVan.isEmpty()) {
            throw new IllegalStateException("Câu trả lời không có phần chữ nào");
        }
        return loiVan;
    }

    @Override
    public String tenDichVu() {
        return "Anthropic " + cauHinh.model();
    }

    @Override
    public void destroy() {
        AnthropicClient hienCo = client;
        if (hienCo != null) {
            hienCo.close();
        }
    }

    private AnthropicClient client() {
        AnthropicClient hienCo = client;
        if (hienCo == null) {
            synchronized (this) {
                if (client == null) {
                    String diaChi = cauHinh.baseUrl() == null || cauHinh.baseUrl().isBlank()
                            ? DIA_CHI_MAC_DINH : cauHinh.baseUrl();
                    client = AnthropicOkHttpClient.builder()
                            .apiKey(cauHinh.apiKey())
                            .baseUrl(diaChi)
                            .timeout(Duration.ofSeconds(Math.max(1, cauHinh.timeoutSeconds())))
                            .maxRetries(0)
                            .build();
                }
                hienCo = client;
            }
        }
        return hienCo;
    }

    /*
     * Dựng yêu cầu gửi Claude. Tách thành hàm tĩnh, không gọi mạng, để kiểm thử được hình dạng
     * yêu cầu mà không cần khóa API.
     */
    static MessageCreateParams dungYeuCau(AssistantProperties.Llm cauHinh, String chiDanHeThong,
                                          List<LuotHoiThoai> lichSu, String cauHoi) {
        MessageCreateParams.Builder yeuCau = MessageCreateParams.builder()
                .model(cauHinh.model())
                .maxTokens(cauHinh.maxTokens())
                // Luật và dữ liệu nằm ở phần system, lời của khách nằm riêng ở messages,
                // để khách không sửa được luật bằng cách viết khéo câu hỏi
                .systemOfTextBlockParams(List.of(TextBlockParam.builder()
                        .text(chiDanHeThong)
                        .cacheControl(CacheControlEphemeral.builder().build())
                        .build()))
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.of(cauHinh.effort() == null ? "low" : cauHinh.effort()))
                        .build());

        for (LuotHoiThoai luot : xepLuot(lichSu)) {
            if (luot.cuaKhach()) {
                yeuCau.addUserMessage(luot.noiDung());
            } else {
                yeuCau.addAssistantMessage(luot.noiDung());
            }
        }
        yeuCau.addUserMessage(cauHoi);

        if (hoTroDuPhongTuChoi(cauHinh.model())) {
            yeuCau.putAdditionalHeader("anthropic-beta", BETA_DU_PHONG_TU_CHOI);
            yeuCau.putAdditionalBodyProperty("fallbacks", JsonValue.from("default"));
        }
        return yeuCau.build();
    }

    /*
     * Xếp lịch sử thành chuỗi lượt xen kẽ khách và trợ lý, mở đầu bằng lượt khách và kết thúc
     * bằng lượt trợ lý, vì ngay sau đó là câu hỏi mới của khách.
     *
     * Lịch sử từ trình duyệt có thể lệch: câu hỏi trước lỗi mạng nên không có câu trả lời, hay
     * đoạn đầu bị cắt mất lượt khách. Hai lượt cùng vai liền nhau thì ghép làm một.
     */
    static List<LuotHoiThoai> xepLuot(List<LuotHoiThoai> lichSu) {
        List<LuotHoiThoai> ra = new ArrayList<>();
        if (lichSu == null) {
            return ra;
        }
        for (LuotHoiThoai luot : lichSu) {
            if (luot == null || luot.noiDung() == null || luot.noiDung().isBlank()) {
                continue;
            }
            if (ra.isEmpty() && !luot.cuaKhach()) {
                continue;
            }
            LuotHoiThoai cuoi = ra.isEmpty() ? null : ra.get(ra.size() - 1);
            if (cuoi != null && cuoi.cuaKhach() == luot.cuaKhach()) {
                ra.set(ra.size() - 1, new LuotHoiThoai(cuoi.cuaKhach(), cuoi.noiDung() + "\n\n" + luot.noiDung()));
            } else {
                ra.add(luot);
            }
        }
        if (!ra.isEmpty() && ra.get(ra.size() - 1).cuaKhach()) {
            ra.remove(ra.size() - 1);
        }
        return ra;
    }

    // Dự phòng phía máy chủ bằng "default" dành cho Claude Opus 5 và Claude Fable 5.1
    static boolean hoTroDuPhongTuChoi(String model) {
        return model != null && (model.startsWith("claude-opus-5") || model.startsWith("claude-fable-5-1"));
    }
}
