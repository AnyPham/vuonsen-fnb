package vn.vuonsen.fnb.modules.assistant.dto;

import vn.vuonsen.fnb.modules.assistant.AnswerSource;
import vn.vuonsen.fnb.modules.assistant.Intent;

import java.util.List;

/*
 * Câu trả lời của trợ lý.
 *
 * Kèm theo intent để giao diện biết trợ lý hiểu câu hỏi theo hướng nào, và kèm
 * danh sách câu hỏi gợi ý để khách bấm tiếp thay vì phải tự nghĩ ra câu hỏi.
 */
public record AnswerResponse(
        String answer,
        Intent intent,
        List<String> suggestions,
        // Đường dẫn trang liên quan, để giao diện hiện nút chuyển trang
        String link,
        String linkLabel,
        // Câu trả lời do mô hình ngôn ngữ hay do cơ chế dự phòng dựng ra
        AnswerSource source
) {

    /*
     * Dạng rút gọn cho những câu trả lời hệ thống tự dựng.
     *
     * Có hàm này thì phần trả lời dự phòng không phải lặp lại DU_PHONG ở mười mấy
     * chỗ, mà cũng không sợ quên mất trường nguồn.
     */
    public AnswerResponse(String answer, Intent intent, List<String> suggestions,
            String link, String linkLabel) {
        this(answer, intent, suggestions, link, linkLabel, AnswerSource.DU_PHONG);
    }

    /*
     * Giữ nguyên hướng hiểu, câu gợi ý và đường dẫn của bản dự phòng, chỉ thay
     * phần lời văn bằng câu mô hình ngôn ngữ viết.
     *
     * Làm vậy vì mô hình chỉ giỏi diễn đạt, còn việc chọn trang nào cho khách bấm
     * tiếp thì hệ thống nắm chắc hơn. Mô hình cũng không bịa được đường dẫn lạ.
     */
    public AnswerResponse thayLoiVanBangMoHinh(String loiVan) {
        return new AnswerResponse(loiVan, intent, suggestions, link, linkLabel,
                AnswerSource.MO_HINH_NGON_NGU);
    }
}
