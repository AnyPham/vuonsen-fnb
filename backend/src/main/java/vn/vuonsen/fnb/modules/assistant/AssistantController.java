package vn.vuonsen.fnb.modules.assistant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;
import vn.vuonsen.fnb.modules.assistant.dto.AskRequest;
import vn.vuonsen.fnb.modules.assistant.dto.LoiMoDauResponse;
import vn.vuonsen.fnb.security.AppUserDetails;

// API trợ lý tư vấn, khách chưa đăng nhập cũng hỏi được
@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
@Tag(name = "15. Trợ lý tư vấn")
public class AssistantController {

    private final AssistantFacade assistantFacade;
    private final LoiMoDauService loiMoDauService;

    @PostMapping("/ask")
    @Operation(summary = "Hỏi trợ lý về dịch vụ, trả lời dựa trên dữ liệu của hệ thống")
    public ResponseEntity<AnswerResponse> ask(@Valid @RequestBody AskRequest request) {
        return ResponseEntity.ok(assistantFacade.answer(request.question(), request.history()));
    }

    /*
     * Màn mở đầu, gọi lúc khách bấm mở hộp thoại chứ không phải lúc khách hỏi.
     *
     * Danh tính lấy từ thẻ truy cập qua @AuthenticationPrincipal, không nhận mã người dùng
     * từ trình duyệt gửi lên. Đây là điểm mấu chốt về bảo mật của màn này: nó đọc lịch sử
     * đặt tiệc của một người cụ thể, nên nếu tin mã do trình duyệt gửi thì sửa một con số
     * là xem được lịch sử của người khác.
     *
     * Khách chưa đăng nhập vẫn gọi được, principal khi đó là null và nhận bản dành cho
     * khách mới.
     */
    @GetMapping("/loi-mo-dau")
    @Operation(summary = "Màn mở đầu hộp thoại, cá nhân hóa theo lịch sử đặt tiệc của khách")
    public ResponseEntity<LoiMoDauResponse> loiMoDau(@AuthenticationPrincipal AppUserDetails principal) {
        Long userId = principal == null ? null : principal.getUserId();
        return ResponseEntity.ok(loiMoDauService.dung(userId));
    }
}
