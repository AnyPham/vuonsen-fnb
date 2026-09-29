package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;
import java.util.Optional;

/** Trang quản trị đơn đặt tiệc: /quan-tri/don-dat-tiec. */
public class TrangQuanTriDon extends TrangCoSo {

    public static final String DUONG_DAN = "/quan-tri/don-dat-tiec";

    private static final By LOC_TRANG_THAI = dt("admin-filter-status");
    private static final By O_TIM_KIEM = dt("admin-filter-keyword");
    private static final By DONG_DON = dt("admin-booking-row");
    private static final By MA_DON = dt("admin-booking-code");
    private static final By TRANG_THAI = dt("admin-booking-status");
    private static final By NUT_THAO_TAC = By.cssSelector("[data-test^='action-']");

    public TrangQuanTriDon(WebDriver driver) {
        super(driver);
    }

    /** Mở thẳng đường dẫn. Không chờ bảng vì tài khoản không đủ quyền sẽ thấy màn hình khác. */
    public TrangQuanTriDon mo() {
        moDuongDan(DUONG_DAN);
        return this;
    }

    /** Lọc theo mã trạng thái, ví dụ PENDING. Truyền chuỗi rỗng để xem tất cả. */
    public TrangQuanTriDon locTrangThai(String maTrangThai) {
        chon(LOC_TRANG_THAI, maTrangThai);
        return this;
    }

    /** Tìm theo mã đơn, tên hoặc số điện thoại. */
    public TrangQuanTriDon timKiem(String tuKhoa) {
        nhap(O_TIM_KIEM, tuKhoa);
        return this;
    }

    public List<String> maCacDonDangHien() {
        return docDanhSachOnDinh(MA_DON);
    }

    /** Trạng thái của các đơn đang hiện, đọc khi bảng đã tải xong. */
    public List<String> trangThaiCacDonDangHien() {
        maCacDonDangHien();
        return driver.findElements(TRANG_THAI).stream().map(p -> p.getText().trim()).toList();
    }

    private Optional<WebElement> dongCuaDon(WebDriver d, String maDon) {
        return d.findElements(DONG_DON).stream()
                .filter(dong -> dong.findElement(MA_DON).getText().trim().equals(maDon))
                .findFirst();
    }

    private WebElement choDongCuaDon(String maDon) {
        return cho.until(d -> {
            try {
                return dongCuaDon(d, maDon).orElse(null);
            } catch (StaleElementReferenceException e) {
                return null;
            }
        });
    }

    public String trangThaiCuaDon(String maDon) {
        return choDongCuaDon(maDon).findElement(TRANG_THAI).getText().trim();
    }

    /** Các thao tác đang có cho một đơn, ví dụ CONFIRMED, CANCELLED. Đơn đã kết thúc thì rỗng. */
    public List<String> thaoTacCuaDon(String maDon) {
        return choDongCuaDon(maDon).findElements(NUT_THAO_TAC).stream()
                .map(nut -> nut.getDomAttribute("data-test").replace("action-", ""))
                .toList();
    }

    /*
     * Chuyển trạng thái một đơn: bấm nút thao tác, đồng ý hộp xác nhận của trình duyệt, rồi
     * chờ ô trạng thái của đơn đó đổi khác trước khi trả về. Trang tải lại bảng sau khi đổi,
     * nên phải tìm lại dòng mỗi lần đọc.
     */
    public TrangQuanTriDon chuyenTrangThai(String maDon, String trangThaiDich) {
        String truoc = trangThaiCuaDon(maDon);
        bam(choDongCuaDon(maDon).findElement(dt("action-" + trangThaiDich)));
        dongYHopThoai();
        cho.until(d -> {
            try {
                return dongCuaDon(d, maDon)
                        .map(dong -> !dong.findElement(TRANG_THAI).getText().trim().equals(truoc))
                        .orElse(false);
            } catch (StaleElementReferenceException e) {
                return false;
            }
        });
        return this;
    }
}
