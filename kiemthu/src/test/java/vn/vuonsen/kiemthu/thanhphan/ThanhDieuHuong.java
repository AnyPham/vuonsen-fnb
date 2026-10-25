package vn.vuonsen.kiemthu.thanhphan;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

/*
 * Thanh điều hướng ở đầu mọi trang.
 *
 * Là Component Object chứ không phải Page Object: nó xuất hiện trên cả 23 trang, nên viết
 * một lần ở đây rồi dùng chung, thay vì chép locator nút đăng nhập, đăng xuất sang từng trang.
 */
public class ThanhDieuHuong extends TrangCoSo {

    private static final By NUT_DANG_NHAP = dt("nav-login");
    private static final By NUT_DANG_XUAT = dt("nav-logout");
    private static final By NUT_QUAN_TRI = dt("nav-admin");
    private static final By MUC_DON_CUA_TOI = dt("nav-my-orders");
    private static final By NUT_NGON_NGU = dt("lang-switch");

    public ThanhDieuHuong(WebDriver driver) {
        super(driver);
    }

    /*
     * Đổi ngôn ngữ hiển thị, mã là "vi" hoặc "en".
     *
     * Ô chọn ngôn ngữ nằm trong thanh điều hướng nên đặt ở đây, dùng chung cho mọi trang.
     * Phải bấm nút mở danh sách trước rồi mới bấm được mục bên trong: danh sách chỉ được
     * dựng ra khi mở, lúc đóng thì không có trong trang chứ không phải bị ẩn.
     */
    public ThanhDieuHuong doiNgonNgu(String ma) {
        bam(NUT_NGON_NGU);
        bam(dt("lang-option-" + ma));
        // Thuộc tính lang của thẻ html đổi theo ngôn ngữ, chờ nó đổi là chắc đã áp dụng xong
        cho.until(d -> ma.equals(d.findElement(By.tagName("html")).getAttribute("lang")));
        return this;
    }

    /*
     * Thanh điều hướng đang ở trạng thái đã đăng nhập hay chưa, dùng khi trạng thái đã ổn định.
     *
     * Chờ tới khi một trong hai nút xuất hiện rồi mới trả lời: lúc trang vừa tải, ứng dụng còn
     * đang dựng giao diện nên chưa có nút nào. Không dùng ngay sau khi tải lại trang, vì lúc
     * đó ứng dụng hiện nút Đăng nhập trước rồi mới đổi sang Đăng xuất khi khôi phục xong phiên.
     */
    public boolean daDangNhap() {
        cho.until(d -> !d.findElements(NUT_DANG_XUAT).isEmpty() || !d.findElements(NUT_DANG_NHAP).isEmpty());
        return dangCo(NUT_DANG_XUAT);
    }

    /**
     * Chờ thanh điều hướng chuyển sang trạng thái đã đăng nhập. Trả về false nếu hết thời
     * gian chờ mà vẫn chưa chuyển, để kịch bản nhận câu báo lỗi rõ ràng.
     */
    public boolean coChuyenSangDaDangNhap() {
        try {
            choHien(NUT_DANG_XUAT);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Có menu Quản trị hay không. Chỉ trả lời sau khi thanh điều hướng đã ổn định trạng thái. */
    public boolean coMenuQuanTri() {
        daDangNhap();
        return dangCo(NUT_QUAN_TRI);
    }

    /** Có mục Đơn của tôi hay không. Mục này chỉ dành cho khách hàng, quản trị không có. */
    public boolean coMucDonCuaToi() {
        daDangNhap();
        return dangCo(MUC_DON_CUA_TOI);
    }

    public ThanhDieuHuong dangXuat() {
        bam(NUT_DANG_XUAT);
        choHien(NUT_DANG_NHAP);
        return this;
    }
}
