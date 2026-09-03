package vn.vuonsen.fnb.modules.assistant.dto;

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
        String linkLabel
) {
}
