package vn.vuonsen.fnb.common.exception;

// Không tìm thấy dữ liệu, trả về HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("Khong tim thay %s voi dinh danh '%s'".formatted(resource, id));
    }
}
