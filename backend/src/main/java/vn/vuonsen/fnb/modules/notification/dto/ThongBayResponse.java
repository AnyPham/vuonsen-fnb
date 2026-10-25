package vn.vuonsen.fnb.modules.notification.dto;

/* Kết quả một đợt gửi thông báo, để màn hình quản trị biết đã tới được bao nhiêu máy. */
public record ThongBayResponse(int daGui, int thatBai, int daDon) {
}
