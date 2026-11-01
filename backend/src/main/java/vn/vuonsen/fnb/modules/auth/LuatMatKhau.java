package vn.vuonsen.fnb.modules.auth;

/*
 * Luật đặt mật khẩu, khai một chỗ cho cả lúc đăng ký lẫn lúc đặt lại.
 *
 * Để mỗi nơi tự viết một biểu thức thì sớm muộn hai nơi lệch nhau: sửa luật ở trang đăng ký
 * mà quên trang quên mật khẩu, thành ra khách đặt được mật khẩu yếu bằng đường vòng.
 *
 * Luật gồm bốn điều kiện: dài tối thiểu 8 ký tự, có chữ thường, có chữ hoa và có số.
 *
 * Vì sao chọn mức này:
 *
 * Mức cũ chỉ đòi 6 ký tự, không đòi gì thêm, nên "123456" vẫn qua. Đó là mật khẩu bị dò
 * đầu tiên trong mọi danh sách tấn công.
 *
 * Vì sao không đòi thêm ký tự đặc biệt: các hướng dẫn hiện nay nghiêng về độ dài hơn là
 * độ phức tạp, vì bắt thêm ký tự đặc biệt thường khiến người dùng đặt kiểu "Matkhau@1"
 * rồi dán vào mọi trang. Bốn điều kiện trên đã chặn được các mật khẩu phổ biến nhất mà
 * vẫn còn gõ được trên điện thoại.
 *
 * Trần 72 ký tự là giới hạn của thuật toán băm BCrypt: quá 72 byte thì phần dư bị bỏ
 * lặng lẽ, khách tưởng mật khẩu dài mà thực tế không phải.
 *
 * Lưu ý: luật này KHÔNG áp khi đăng nhập. Tài khoản lập từ trước vẫn vào được bằng mật
 * khẩu cũ, chỉ khi nào đổi mật khẩu mới phải theo luật mới. Siết ngay ở chỗ đăng nhập sẽ
 * khóa cửa với chính những người đang dùng.
 */
public final class LuatMatKhau {

    private LuatMatKhau() {
    }

    public static final int DAI_TOI_THIEU = 8;
    public static final int DAI_TOI_DA = 72;

    /** Nhìn trước để kiểm từng điều kiện, không tiêu thụ ký tự nào. */
    public static final String BIEU_THUC =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";

    public static final String THONG_BAO =
            "Mật khẩu phải từ 8 ký tự, có chữ hoa, chữ thường và số";
}
