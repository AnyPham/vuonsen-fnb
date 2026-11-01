package vn.vuonsen.fnb.modules.auth.dto;

/*
 * Kết quả của bước đăng ký.
 *
 * Tách khỏi AuthResponse vì hai thứ khác hẳn nhau: đăng ký xong thì CHƯA có token, tài
 * khoản còn phải qua bước nhập mã. Nếu dùng chung AuthResponse rồi để token rỗng thì phía
 * giao diện phải tự đoán xem lúc nào rỗng là bình thường, lúc nào là lỗi.
 *
 * Trường email trả về để màn nhập mã biết đang xác thực cho địa chỉ nào mà không phải giữ
 * lại giá trị người dùng vừa gõ.
 */
public record DangKyResponse(
        String email,
        boolean canXacThuc,
        String message
) {
    public static DangKyResponse choXacThuc(String email) {
        return new DangKyResponse(email, true,
                "Mình đã gửi mã xác nhận tới " + email
                        + ". Nhập mã để kích hoạt tài khoản, mã có hiệu lực 15 phút.");
    }
}
