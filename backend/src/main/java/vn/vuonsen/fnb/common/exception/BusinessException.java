package vn.vuonsen.fnb.common.exception;

// Lỗi nghiệp vụ: số khách vượt sức chứa, trùng lịch... trả về HTTP 400
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
