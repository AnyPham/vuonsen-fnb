package vn.vuonsen.fnb.modules.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Câu hỏi khách gửi lên
public record AskRequest(
        @NotBlank(message = "Vui lòng nhập câu hỏi")
        @Size(max = 500, message = "Câu hỏi quá dài") String question
) {
}
