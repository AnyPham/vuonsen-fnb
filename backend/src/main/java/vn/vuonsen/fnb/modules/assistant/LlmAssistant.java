package vn.vuonsen.fnb.modules.assistant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.vuonsen.fnb.config.props.AssistantProperties;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;
import vn.vuonsen.fnb.modules.assistant.llm.LlmClient;

import java.util.Optional;

/*
 * Nhánh trả lời bằng mô hình ngôn ngữ ngoài.
 *
 * Nhiệm vụ ở đây là quyết định chính sách, còn việc gọi mạng để cho LlmClient lo.
 * Chính sách gồm ba điều:
 *
 * 1. Chỉ gọi khi đã bật và có khóa API.
 * 2. Gọi hỏng kiểu gì cũng không được ném lỗi ra ngoài. Trả về rỗng để nơi gọi
 *    dùng câu trả lời dự phòng. Khách đang đợi trong hộp thoại, thà đưa câu trả
 *    lời dựng sẵn còn hơn hiện thông báo lỗi.
 * 3. Câu mô hình viết ra chỉ thay phần lời văn. Đường dẫn trang và câu hỏi gợi ý
 *    vẫn lấy của bản dự phòng, nên mô hình không đẩy khách sang trang không có
 *    thật được.
 */
@Component
@Slf4j
public class LlmAssistant {

    private final AssistantProperties.Llm cauHinh;
    private final LlmClient client;
    private final SystemContextBuilder contextBuilder;

    public LlmAssistant(AssistantProperties properties, LlmClient client,
            SystemContextBuilder contextBuilder) {
        this.cauHinh = properties.llm();
        this.client = client;
        this.contextBuilder = contextBuilder;
    }

    public boolean batDuoc() {
        return cauHinh.dungDuoc();
    }

    public Optional<AnswerResponse> traLoi(String cauHoi, AnswerResponse duPhong) {
        if (!batDuoc()) {
            return Optional.empty();
        }

        try {
            String loiVan = client.hoi(contextBuilder.dungChiDan(), cauHoi);
            if (loiVan == null || loiVan.isBlank()) {
                log.warn("Trợ lý: {} trả về câu rỗng, chuyển sang cơ chế dự phòng", client.tenDichVu());
                return Optional.empty();
            }
            return Optional.of(duPhong.thayLoiVanBangMoHinh(loiVan.trim()));

        } catch (Exception e) {
            /*
             * Bắt hết mọi loại lỗi, kể cả hết hạn chờ, sai khóa, hết hạn mức hay dịch
             * vụ ngoài đang hỏng. Ghi nhật ký để còn biết đường sửa, nhưng khách vẫn
             * nhận được câu trả lời bình thường.
             *
             * Không ghi nội dung ngoại lệ ở mức nghiêm trọng vì thông điệp lỗi của
             * dịch vụ ngoài có thể chứa mảnh khóa API.
             */
            log.warn("Trợ lý: gọi {} không thành ({}), chuyển sang cơ chế dự phòng",
                    client.tenDichVu(), e.getClass().getSimpleName());
            log.debug("Chi tiết lỗi gọi mô hình ngôn ngữ", e);
            return Optional.empty();
        }
    }
}
