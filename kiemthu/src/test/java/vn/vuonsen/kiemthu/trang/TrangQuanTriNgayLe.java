package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Trang quản trị giảm giá ngày lễ: /quan-tri/ngay-le. */
public class TrangQuanTriNgayLe extends TrangCoSo {

    public static final String DUONG_DAN = "/quan-tri/ngay-le";

    private static final By NUT_THEM = dt("holiday-new");
    private static final By O_TEN = dt("holiday-name");
    private static final By O_TU_NGAY = dt("holiday-start");
    private static final By O_DEN_NGAY = dt("holiday-end");
    private static final By O_MUC_GIAM = dt("holiday-percent");
    private static final By O_DANG_AP_DUNG = dt("holiday-active");
    private static final By NUT_LUU = dt("holiday-save");
    private static final By DONG = dt("admin-holiday-row");
    private static final By O_TRONG_DONG = By.tagName("td");
    private static final By NUT_SUA = dt("holiday-edit");
    private static final By NUT_XOA = dt("holiday-delete");

    /** Một dòng trong bảng dịp lễ, giữ nguyên chữ đang hiển thị. */
    public record DongDipLe(String ten, String thoiGian, String mucGiam, String trangThai) {
    }

    public TrangQuanTriNgayLe(WebDriver driver) {
        super(driver);
    }

    /** Mở thẳng đường dẫn. Không chờ bảng vì tài khoản không đủ quyền sẽ thấy màn hình khác. */
    public TrangQuanTriNgayLe mo() {
        moDuongDan(DUONG_DAN);
        return this;
    }

    // ================= Đọc bảng =================

    private static DongDipLe docDong(WebElement dong) {
        List<WebElement> o = dong.findElements(O_TRONG_DONG);
        return new DongDipLe(o.get(0).getText().trim(), o.get(1).getText().trim(),
                o.get(2).getText().trim(), o.get(3).getText().trim());
    }

    /** Danh sách dịp lễ khi bảng đã tải xong. */
    public List<DongDipLe> danhSach() {
        choHien(DONG);
        return cho.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(DONG).stream().map(TrangQuanTriNgayLe::docDong).toList());
    }

    public Optional<DongDipLe> dongCua(String ten) {
        Optional<DongDipLe> dong = danhSach().stream().filter(d -> d.ten().equals(ten)).findFirst();
        // Chế độ trình diễn: tô dòng vừa đọc; dòng bị vẽ lại giữa chừng thì bỏ qua phần tô
        if (dong.isPresent()) {
            driver.findElements(DONG).stream()
                    .filter(d -> {
                        try {
                            return d.getText().contains(ten);
                        } catch (StaleElementReferenceException e) {
                            return false;
                        }
                    })
                    .findFirst()
                    .ifPresent(d -> trinhDienKetQua(d, false));
        }
        return dong;
    }

    /*
     * Chờ tới khi bảng có dòng mang tên cho trước và thỏa điều kiện.
     *
     * Lưu xong trang tải lại cả bảng, dòng cũ bị vẽ lại, nên phải chờ theo nội dung chứ không
     * giữ phần tử cũ để đọc lại.
     */
    private DongDipLe choDong(String ten, Predicate<DongDipLe> dieuKien) {
        return cho.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(DONG).stream()
                        .map(TrangQuanTriNgayLe::docDong)
                        .filter(dong -> dong.ten().equals(ten) && dieuKien.test(dong))
                        .findFirst()
                        .orElse(null));
    }

    private WebElement phanTuDong(String ten) {
        return cho.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(DONG).stream()
                        .filter(dong -> dong.findElement(O_TRONG_DONG).getText().trim().equals(ten))
                        .findFirst()
                        .orElse(null));
    }

    // ================= Form thêm và sửa =================

    public TrangQuanTriNgayLe moFormThem() {
        bam(NUT_THEM);
        choHien(O_TEN);
        return this;
    }

    /** Điền form. mucGiam là chuỗi để thử được cả giá trị ngoài khoảng cho phép. */
    public TrangQuanTriNgayLe dienForm(String ten, LocalDate tuNgay, LocalDate denNgay, String mucGiam) {
        nhap(O_TEN, ten);
        datGiaTri(O_TU_NGAY, tuNgay.toString());
        datGiaTri(O_DEN_NGAY, denNgay.toString());
        nhap(O_MUC_GIAM, mucGiam);
        return this;
    }

    public TrangQuanTriNgayLe datDangApDung(boolean batLen) {
        WebElement o = choHien(O_DANG_AP_DUNG);
        if (o.isSelected() != batLen) {
            bam(o);
        }
        return this;
    }

    /** Bấm Lưu mà không chờ gì thêm, dùng cho trường hợp form bị chặn không gửi được. */
    public TrangQuanTriNgayLe bamLuu() {
        bam(NUT_LUU);
        return this;
    }

    /** Thêm dịp lễ qua form rồi chờ dịp đó hiện trong bảng. */
    public TrangQuanTriNgayLe themDipLe(String ten, LocalDate tuNgay, LocalDate denNgay, int mucGiam) {
        moFormThem();
        dienForm(ten, tuNgay, denNgay, String.valueOf(mucGiam));
        bamLuu();
        choDong(ten, dong -> true);
        return this;
    }

    /** Sửa mức giảm và trạng thái của một dịp rồi lưu, chờ bảng hiện đúng giá trị mới. */
    public TrangQuanTriNgayLe suaDipLe(String ten, int mucGiamMoi, boolean dangApDung) {
        bam(phanTuDong(ten).findElement(NUT_SUA));
        nhap(O_MUC_GIAM, String.valueOf(mucGiamMoi));
        datDangApDung(dangApDung);
        bamLuu();
        String mucMoi = mucGiamMoi + "%";
        choDong(ten, dong -> {
            boolean daTat = dong.trangThai().equals("Đã tắt");
            return dong.mucGiam().equals(mucMoi) && daTat == !dangApDung;
        });
        return this;
    }

    /** Bấm Xóa, đồng ý ở hộp xác nhận, chờ dịp biến mất khỏi bảng. */
    public TrangQuanTriNgayLe xoaDipLe(String ten) {
        bam(phanTuDong(ten).findElement(NUT_XOA));
        dongYHopThoai();
        cho.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(DONG).stream()
                        .noneMatch(dong -> dong.findElement(O_TRONG_DONG).getText().trim().equals(ten)));
        return this;
    }

    public boolean dangMoForm() {
        return dangCo(NUT_LUU);
    }

    /** Câu nhắc lỗi của chính trình duyệt ở ô mức giảm, rỗng nếu ô hợp lệ. */
    public String loiTrinhDuyetOMucGiam() {
        return choHien(O_MUC_GIAM).getDomProperty("validationMessage");
    }

    /** Câu nhắc lỗi của chính trình duyệt ở ô đến ngày, rỗng nếu ô hợp lệ. */
    public String loiTrinhDuyetONgayKetThuc() {
        return choHien(O_DEN_NGAY).getDomProperty("validationMessage");
    }
}
