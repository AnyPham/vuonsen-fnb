package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;
import vn.vuonsen.kiemthu.thanhphan.ThanhDieuHuong;

/** Trang đăng ký tài khoản: /dang-ky. */
public class TrangDangKy extends TrangCoSo {

    public static final String DUONG_DAN = "/dang-ky";

    private static final By BIEU_MAU = dt("register-form");
    private static final By O_HO_TEN = dt("full-name");
    private static final By O_EMAIL = dt("email");
    private static final By O_SO_DIEN_THOAI = dt("phone");
    private static final By O_MAT_KHAU = dt("password");
    private static final By NUT_GUI = dt("submit-register");
    private static final By THONG_BAO_LOI = dt("error");

    public TrangDangKy(WebDriver driver) {
        super(driver);
    }

    public TrangDangKy mo() {
        moDuongDan(DUONG_DAN);
        choHien(BIEU_MAU);
        return this;
    }

    /** Điền form rồi gửi. Số điện thoại không bắt buộc, truyền null thì bỏ trống ô đó. */
    public ThanhDieuHuong dangKy(String hoTen, String email, String soDienThoai, String matKhau) {
        nhap(O_HO_TEN, hoTen);
        nhap(O_EMAIL, email);
        if (soDienThoai != null) {
            nhap(O_SO_DIEN_THOAI, soDienThoai);
        }
        nhap(O_MAT_KHAU, matKhau);
        bam(NUT_GUI);
        return new ThanhDieuHuong(driver);
    }

    /** Chờ khối thông báo lỗi do máy chủ trả về hiện ra rồi trả về nội dung. */
    public String thongBaoLoi() {
        return chuCua(THONG_BAO_LOI);
    }

    /*
     * Câu nhắc lỗi của chính trình duyệt ở ô mật khẩu, rỗng nếu ô hợp lệ.
     *
     * Ô có minLength, required hay type=email được trình duyệt tự kiểm tra và chặn gửi form
     * ngay tại chỗ. Câu nhắc hiện dạng bong bóng của trình duyệt, không nằm trong cây DOM của
     * trang nên không tìm được bằng locator, phải đọc thuộc tính validationMessage của ô.
     */
    public String loiTrinhDuyetOMatKhau() {
        return choHien(O_MAT_KHAU).getDomProperty("validationMessage");
    }

    /** Câu nhắc lỗi của chính trình duyệt ở ô email, rỗng nếu ô hợp lệ. */
    public String loiTrinhDuyetOEmail() {
        return choHien(O_EMAIL).getDomProperty("validationMessage");
    }
}
