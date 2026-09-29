package vn.vuonsen.fnb.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.vuonsen.fnb.common.dto.ErrorResponse;

import java.util.LinkedHashMap;
import java.util.Map;

// Bắt lỗi cho toàn bộ API, trả về JSON theo cùng một định dạng
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        /*
         * Ghép luôn câu báo của từng ô vào thông điệp chung.
         *
         * Giao diện chỉ hiện thông điệp chung ở khối lỗi, nên nếu chỉ trả "Dữ liệu gửi lên không
         * hợp lệ" thì khách không biết ô nào sai, còn phần fieldErrors thì không chỗ nào đọc.
         */
        String chiTiet = String.join("; ", fields.values());
        String thongDiep = chiTiet.isBlank() ? "Dữ liệu gửi lên không hợp lệ"
                : "Dữ liệu gửi lên không hợp lệ: " + chiTiet;
        return ResponseEntity.badRequest()
                .body(ErrorResponse.validation(thongDiep, request.getRequestURI(), fields));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, "Unauthorized", "Email hoặc mật khẩu không đúng", request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "Forbidden", "Bạn không có quyền thực hiện thao tác này",
                        request.getRequestURI()));
    }

    /*
     * Đường dẫn không có trong hệ thống.
     *
     * Spring ném NoResourceFoundException cho đường dẫn không khớp controller nào, trước đây
     * rơi vào lưới bắt Exception cuối tệp nên khách gõ nhầm địa chỉ lại nhận 500 "hệ thống
     * đang bận", nghe như website hỏng. Trả đúng 404 để phân biệt gõ sai với lỗi thật.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                          HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", "Không tìm thấy đường dẫn này",
                        request.getRequestURI()));
    }

    /*
     * Thân yêu cầu không đọc được: JSON thiếu dấu ngoặc, sai kiểu dữ liệu, hoặc sai bảng mã.
     *
     * Lỗi này do bên gửi, không phải do hệ thống, nên phải là 400 chứ không phải 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        log.warn("Không đọc được dữ liệu gửi lên tại {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request",
                        "Dữ liệu gửi lên không đọc được, vui lòng kiểm tra lại định dạng JSON",
                        request.getRequestURI()));
    }

    /*
     * Hai người ghi cùng lúc, database từ chối người sau vì trùng giá trị UNIQUE.
     *
     * Các chỗ đã biết (trùng email, trùng mã đơn) đều được xử lý riêng trước khi tới đây.
     * Handler này là lưới an toàn cuối cùng: lỡ còn chỗ lọt thì khách vẫn nhận câu báo
     * đúng bản chất là xung đột, kèm mã 409, thay vì lỗi 500 khó hiểu.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        log.warn("Xung đột dữ liệu tại {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict",
                        "Dữ liệu vừa được thay đổi bởi một thao tác khác, vui lòng thử lại",
                        request.getRequestURI()));
    }

    // Chờ khóa quá lâu, hoặc bị database hủy vì khóa chết khi nhiều người thao tác cùng lúc
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErrorResponse> handleLock(ConcurrencyFailureException ex, HttpServletRequest request) {
        log.warn("Tranh chấp khóa tại {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict",
                        "Đơn đang được người khác xử lý cùng lúc, vui lòng tải lại trang và thử lại",
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không mong đợi tại {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error",
                        "Hệ thống đang bận, vui lòng thử lại sau", request.getRequestURI()));
    }
}
