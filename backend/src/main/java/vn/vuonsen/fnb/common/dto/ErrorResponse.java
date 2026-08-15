package vn.vuonsen.fnb.common.dto;

import java.time.LocalDateTime;
import java.util.Map;

// Dạng JSON trả về khi API bị lỗi.
// fieldErrors chỉ có khi form nhập sai, key là tên trường.
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse validation(String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), 400, "Bad Request", message, path, fieldErrors);
    }
}
