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
 * Gọi Google Gemini qua giao thức REST.
 *
 * Vì sao có lớp này: Gemini có hạn mức dùng miễn phí thật, đăng ký khóa ở Google AI Studio
 * không cần thẻ ngân hàng. Với một trợ lý nhà hàng hỏi vài trăm câu mỗi ngày thì hạn mức
 * miễn phí là đủ, nên đây là đường rẻ nhất mà vẫn cho chất lượng tiếng Việt tốt.
 *
 * Không dùng SDK chính thức của Google mà gọi thẳng REST bằng HttpClient có sẵn trong JDK.
 * Lý do: SDK kéo theo cả chùm thư viện Google Cloud nặng vài chục MB, trong khi ở đây chỉ
 * cần đúng một lời gọi POST. Thêm một phụ thuộc lớn cho một lời gọi là không đáng.
 *
 * Ba điểm khác Anthropic phải chú ý khi đọc mã:
 *
 * 1. Chỉ dẫn hệ thống nằm ở trường systemInstruction riêng, không nhét vào danh sách
 *    tin nhắn như một số dịch vụ khác.
 * 2. Vai của mô hình gọi là "model" chứ không phải "assistant".
 * 3. Khóa API gửi bằng đầu đề x-goog-api-key chứ không nối vào địa chỉ. Tài liệu có nêu
 *    cách nối vào địa chỉ, nhưng làm vậy thì khóa lọt vào nhật ký máy chủ và lịch sử
 *    trình duyệt; để ở đầu đề thì không.
 */
@Component
@ConditionalOnProperty(name = "app.assistant.llm.provider", havingValue = AssistantProperties.Llm.GEMINI,
        matchIfMissing = true)
public class GeminiLlmClient implements LlmClient {

    private static final String DIA_CHI_MAC_DINH = "https://generativelanguage.googleapis.com";

    /*
     * Chọn bản Flash chứ không phải Pro: Flash nằm trong hạn mức miễn phí, trả lời nhanh,
     * và câu tư vấn nhà hàng không cần tới sức suy luận của bản Pro.
     *
     * Trước đây để gemini-2.5-flash. Khóa mới tạo gọi bản đó thì bị trả mã 404 kèm
     * câu "no longer available to new users": Google đã ngừng cấp bản 2.5 cho tài khoản mới
     * và chỉ sang 3.8. Đáng lưu ý là đường dẫn liệt kê mô hình vẫn còn kê tên 2.5-flash, nên
     * chỉ nhìn danh sách đó thì tưởng vẫn dùng được; phải gọi thật mới biết.
     *
     * Rồi cũng không lấy 3.8: đo thử năm bản thì 3.8-flash trả 429 vì mới ra
     * nên đông người dùng và hạn mức miễn phí bị siết, 3.5-flash mất 45 giây một câu, còn
     * 3.5-flash-lite trả lời trong 0,87 giây. Việc của trợ lý chỉ là diễn đạt lại dữ liệu
     * hệ thống đưa sang thành câu tiếng Việt tự nhiên, không phải suy luận gì khó, nên bản
     * lite vừa đủ mà khách không phải ngồi đợi.
     */
    private static final String MO_HINH_MAC_DINH = "gemini-3.5-flash-lite";

    private static final int SO_CHU_TOI_DA_MAC_DINH = 800;

    /*
     * Khoảng nghỉ trước mỗi lần gọi lại, tính bằng mili giây. Hai lần là đủ: quá tải bên
     * Google thường qua trong khoảng một giây, mà khách đang ngồi đợi câu trả lời nên
     * không thể kéo dài mãi. Hỏng tới lần thứ ba thì chuyển sang cơ chế dự phòng, khách
     * vẫn có câu trả lời đúng dữ liệu, chỉ là lời văn cứng hơn.
     */
    private static final long[] CHO_GIUA_HAI_LAN_MS = {400, 1000};

    private final AssistantProperties.Llm cauHinh;
    private final ObjectMapper json;
    private final HttpClient http;

    public GeminiLlmClient(AssistantProperties properties, ObjectMapper json) {
        this.cauHinh = properties.llm();
        this.json = json;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(cauHinh.timeoutAnToan()))
                .build();
    }

    @Override
    public boolean sanSang() {
        return cauHinh.coKhoa();
    }

    @Override
    public String huongDanBat() {
        return "chưa có khóa Gemini. Lấy khóa miễn phí ở https://aistudio.google.com/apikey "
                + "rồi dán vào backend/tro-ly-ai.properties";
    }

    @Override
    public String tenDichVu() {
        return "Google " + cauHinh.modelHoac(MO_HINH_MAC_DINH);
    }

    @Override
    public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
        String than = dungThanYeuCau(chiDanHeThong, lichSu, cauHoi);
        HttpResponse<String> phanHoi = guiCoThuLai(than);

        if (phanHoi.statusCode() != 200) {
            /*
             * Không ghi nguyên văn phần thân lỗi ra ngoại lệ: thông điệp lỗi của Google có
             * thể chép lại một phần yêu cầu, trong đó có khóa API nếu ai đó lỡ cấu hình
             * theo kiểu nối khóa vào địa chỉ.
             */
            throw new IllegalStateException("Gemini trả mã " + phanHoi.statusCode());
        }
        return docLoiVan(phanHoi.body());
    }

    /*
     * Chỉ thử lại khi gặp 503, tức máy chủ Google đang quá tải nhất thời. Google trả kèm
     * câu "spikes in demand are usually temporary", gọi lại một nhịp là thường được.
     *
     * Cố ý KHÔNG thử lại 429. Nhìn qua thì 429 cũng là lỗi nhất thời nên đáng gọi lại,
     * nhưng 429 của hạn mức miễn phí tính theo số lượt mỗi phút: gọi lại sau vài trăm
     * mili giây thì chắc chắn trượt tiếp, mà mỗi lần trượt vẫn trừ vào hạn mức. Thử lại
     * ở đây làm tình hình xấu đi chứ không đỡ được gì. Bản đầu có để 429 trong danh sách
     * này, đo thật mới thấy sai.
     *
     * Các mã còn lại là lỗi cố định, gọi lại bao nhiêu lần cũng vậy: 400 thân yêu cầu
     * sai, 401 và 403 khóa hỏng, 404 tên mô hình không còn.
     */
    static boolean nenThuLai(int maHttp) {
        return maHttp == 503;
    }

    /*
     * Gọi Gemini, gặp lỗi nhất thời thì thử lại tối đa hai lần nữa.
     *
     * Chỉ thử lại theo mã trả về, không thử lại khi ném ngoại lệ. Phân biệt này quan
     * trọng: 503 về rất nhanh nên thử lại gần như không làm khách chờ thêm, còn hết hạn
     * chờ thì mỗi lần đã ngốn trọn số giây cấu hình, thử ba lần là khách đợi gấp ba.
     *
     * Đợi giãn dần giữa các lần để không dội thêm vào lúc máy chủ bên kia đang nghẽn.
     */
    private HttpResponse<String> guiCoThuLai(String than) {
        HttpResponse<String> phanHoi = gui(than);

        for (int lan = 0; lan < CHO_GIUA_HAI_LAN_MS.length && nenThuLai(phanHoi.statusCode()); lan++) {
            try {
                Thread.sleep(CHO_GIUA_HAI_LAN_MS[lan]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return phanHoi;
            }
            phanHoi = gui(than);
        }
        return phanHoi;
    }

    // ---------------- Dựng yêu cầu ----------------

    String dungThanYeuCau(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
        ObjectNode goc = json.createObjectNode();

        // Chỉ dẫn hệ thống đi riêng, không lẫn vào các lượt trò chuyện
        goc.putObject("systemInstruction")
                .putArray("parts")
                .addObject()
                .put("text", chiDanHeThong);

        ArrayNode cacLuot = goc.putArray("contents");
        for (LuotHoiThoai luot : lichSu) {
            ObjectNode o = cacLuot.addObject();
            o.put("role", luot.cuaKhach() ? "user" : "model");
            o.putArray("parts").addObject().put("text", luot.noiDung());
        }
        ObjectNode luotMoi = cacLuot.addObject();
        luotMoi.put("role", "user");
        luotMoi.putArray("parts").addObject().put("text", cauHoi);

        ObjectNode caiDat = goc.putObject("generationConfig");
        caiDat.put("maxOutputTokens", cauHinh.maxTokensHoac(SO_CHU_TOI_DA_MAC_DINH));
        /*
         * Nhiệt độ thấp vì đây là trợ lý tra cứu chứ không phải viết quảng cáo. Cùng một câu
         * hỏi về giá gói tiệc thì lần nào cũng nên ra cùng một con số.
         */
        caiDat.put("temperature", 0.3);

        return goc.toString();
    }

    private HttpResponse<String> gui(String than) {
        String diaChi = "%s/v1beta/models/%s:generateContent".formatted(
                cauHinh.baseUrlHoac(DIA_CHI_MAC_DINH).replaceAll("/+$", ""),
                cauHinh.modelHoac(MO_HINH_MAC_DINH));

        HttpRequest yeuCau = HttpRequest.newBuilder(URI.create(diaChi))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", cauHinh.apiKey())
                .timeout(Duration.ofSeconds(cauHinh.timeoutAnToan()))
                .POST(HttpRequest.BodyPublishers.ofString(than, StandardCharsets.UTF_8))
                .build();

        try {
            return http.send(yeuCau, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bị ngắt khi đang gọi Gemini", e);
        } catch (Exception e) {
            throw new IllegalStateException("Không gọi được Gemini", e);
        }
    }

    // ---------------- Đọc câu trả lời ----------------

    String docLoiVan(String than) {
        JsonNode goc;
        try {
            goc = json.readTree(than);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini trả về dữ liệu không đọc được", e);
        }

        JsonNode ungVien = goc.path("candidates").path(0);
        if (ungVien.isMissingNode()) {
            throw new IllegalStateException("Gemini không trả về câu trả lời nào");
        }

        /*
         * Dừng vì lý do khác STOP nghĩa là câu chữ chưa trọn vẹn: hết trần độ dài, hoặc bị
         * bộ lọc an toàn chặn. Coi như hỏng để trợ lý chuyển sang cơ chế dự phòng, còn hơn
         * đưa cho khách một câu cụt giữa chừng.
         */
        String lyDoDung = ungVien.path("finishReason").asText("");
        if (!lyDoDung.isEmpty() && !"STOP".equals(lyDoDung)) {
            throw new IllegalStateException("Gemini dừng giữa chừng: " + lyDoDung);
        }

        /*
         * Bỏ qua phần mang cờ thought. Bản 3.x là mô hình có suy luận: nó trả về cả phần
         * nghĩ nháp lẫn câu trả lời, hai thứ nằm chung trong mảng parts và chỉ khác nhau ở
         * cờ này. Gộp hết thì khách đọc được cả đoạn mô hình đang tự lẩm bẩm.
         */
        StringBuilder sb = new StringBuilder();
        for (JsonNode phan : ungVien.path("content").path("parts")) {
            if (phan.path("thought").asBoolean(false)) {
                continue;
            }
            String chu = phan.path("text").asText("");
            if (!chu.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(chu);
            }
        }

        String loiVan = sb.toString().trim();
        if (loiVan.isEmpty()) {
            throw new IllegalStateException("Câu trả lời của Gemini không có phần chữ nào");
        }
        return loiVan;
    }
}
