package vn.vuonsen.fnb.common.exception;

// Không tìm thấy dữ liệu, trả về HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("Không tìm thấy %s với định danh '%s'".formatted(resource, id));
    }
}
