package vn.vuonsen.kiemthu.luong;

import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.thanhphan.ThanhDieuHuong;
import vn.vuonsen.kiemthu.trang.TrangDangNhap;

/*
 * Luồng nghiệp vụ đăng nhập, dùng làm bước chuẩn bị cho các kịch bản cần tài khoản.
 *
 * Thuộc tầng luồng trong mô hình Page Object Model: ghép sẵn chuỗi thao tác mà nhiều kịch
 * bản lặp lại, ở đây là mở trang đăng nhập, điền, gửi và chờ vào được. Kịch bản phân quyền,
 * quản trị và đánh giá đều cần bước này nhưng không kiểm thử chính nó.
 *
 * Đăng nhập không được thì ném IllegalStateException, để phân biệt hỏng ở bước chuẩn bị với
 * hỏng ở chức năng đang kiểm thử.
 */
public final class LuongDangNhap {

    /** Tài khoản khách vừa tạo cho kịch bản. */
    public record TaiKhoan(String email, String matKhau, String token) {
    }

    private LuongDangNhap() {
    }

    public static ThanhDieuHuong dangNhap(WebDriver driver, String email, String matKhau) {
        ThanhDieuHuong thanhDieuHuong = new TrangDangNhap(driver).mo().dangNhap(email, matKhau);
        if (!thanhDieuHuong.coChuyenSangDaDangNhap()) {
            throw new IllegalStateException("Bước chuẩn bị: không đăng nhập được bằng " + email);
        }
        return thanhDieuHuong;
    }

    public static ThanhDieuHuong quanTri(WebDriver driver) {
        return dangNhap(driver, CauHinh.EMAIL_QUAN_TRI, CauHinh.MAT_KHAU_QUAN_TRI);
    }

    /** Tạo tài khoản khách mới qua API, chưa đăng nhập trên trình duyệt. */
    public static TaiKhoan taoKhachMoi() {
        String email = "khach." + System.nanoTime() + "@vuonsen.vn";
        String matKhau = "matkhau123";
        return new TaiKhoan(email, matKhau, GoiApi.dangKyKhach("Khách Kiểm Thử", email, matKhau));
    }

    /** Tạo tài khoản khách mới rồi đăng nhập tài khoản đó trên trình duyệt. */
    public static TaiKhoan khachMoiDaDangNhap(WebDriver driver) {
        TaiKhoan taiKhoan = taoKhachMoi();
        dangNhap(driver, taiKhoan.email(), taiKhoan.matKhau());
        return taiKhoan;
    }
}
