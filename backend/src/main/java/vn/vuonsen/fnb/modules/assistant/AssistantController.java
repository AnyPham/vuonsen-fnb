package vn.vuonsen.fnb.modules.assistant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;
import vn.vuonsen.fnb.modules.assistant.dto.AskRequest;

// API trợ lý tư vấn, khách chưa đăng nhập cũng hỏi được
@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@Tag(name = "15. Trợ lý tư vấn")
public class AssistantController {

    private final AssistantFacade assistantFacade;

    @PostMapping("/ask")
    @Operation(summary = "Hỏi trợ lý về dịch vụ, trả lời dựa trên dữ liệu của hệ thống")
    public ResponseEntity<AnswerResponse> ask(@Valid @RequestBody AskRequest request) {
        return ResponseEntity.ok(assistantFacade.answer(request.question()));
    }
}
