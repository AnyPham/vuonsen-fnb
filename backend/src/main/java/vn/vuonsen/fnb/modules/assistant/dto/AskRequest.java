package vn.vuonsen.fnb.modules.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// Câu hỏi khách gửi lên
public record AskRequest(
        @NotBlank(message = "Vui lòng nhập câu hỏi")
        @Size(max = 500, message = "Câu hỏi quá dài") String question,

        // Các lượt hỏi đáp trước trong cuộc trò chuyện, cũ trước mới sau. Không bắt buộc.
        @Size(max = 50, message = "Lịch sử trò chuyện quá dài") List<LuotHoi> history
) {

    // role là "user" cho lượt của khách, "assistant" cho lượt của trợ lý
    public record LuotHoi(String role, String content) {
    }
}
