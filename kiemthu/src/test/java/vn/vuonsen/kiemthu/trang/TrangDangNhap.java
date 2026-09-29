package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;
import vn.vuonsen.kiemthu.thanhphan.ThanhDieuHuong;

/** Trang đăng nhập: /dang-nhap. */
public class TrangDangNhap extends TrangCoSo {

    public static final String DUONG_DAN = "/dang-nhap";

    private static final By BIEU_MAU = dt("login-form");
    private static final By O_EMAIL = dt("email");
    private static final By O_MAT_KHAU = dt("password");
    private static final By NUT_GUI = dt("submit-login");
    private static final By THONG_BAO_LOI = dt("error");

    public TrangDangNhap(WebDriver driver) {
        super(driver);
    }

    public TrangDangNhap mo() {
        moDuongDan(DUONG_DAN);
        choHien(BIEU_MAU);
        return this;
    }

    /*
     * Điền email, mật khẩu rồi gửi.
     *
     * Không tự khẳng định đăng nhập thành công hay thất bại, vì cùng một thao tác phục vụ cả
     * kịch bản thuận lẫn kịch bản nghịch. Trả về thanh điều hướng để kịch bản tự kiểm tra.
     */
    public ThanhDieuHuong dangNhap(String email, String matKhau) {
        nhap(O_EMAIL, email);
        nhap(O_MAT_KHAU, matKhau);
        bam(NUT_GUI);
        return new ThanhDieuHuong(driver);
    }

    /** Chờ khối thông báo lỗi hiện ra rồi trả về nội dung. */
    public String thongBaoLoi() {
        return chuCua(THONG_BAO_LOI);
    }
}
