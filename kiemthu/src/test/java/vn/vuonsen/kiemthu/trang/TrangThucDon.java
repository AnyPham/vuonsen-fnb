package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang thực đơn: /thuc-don. */
public class TrangThucDon extends TrangCoSo {

    public static final String DUONG_DAN = "/thuc-don";

    /** Mọi tab danh mục trừ tab Tất cả. */
    private static final By TAB_DANH_MUC =
            By.cssSelector("[data-test^='category-tab-']:not([data-test='category-tab-all'])");
    private static final By TEN_MON = dt("dish-card-link");
    private static final By KHOI_BAN_CHAY = dt("best-sellers");
    private static final By THE_TRONG_KHOI_BAN_CHAY = By.cssSelector("[data-test='best-sellers'] .card");
    private static final By NUT_LOC_CHAY = dt("filter-vegetarian");
    private static final By THE_MON = dt("dish-card");
    private static final By NHAN_CHAY = dt("dish-vegetarian");

    public TrangThucDon(WebDriver driver) {
        super(driver);
    }

    public TrangThucDon mo() {
        moDuongDan(DUONG_DAN);
        choHien(TEN_MON);
        return this;
    }

    public List<String> tenCacTabDanhMuc() {
        return chuCuaTatCa(TAB_DANH_MUC);
    }

    /** Bấm tab theo mã danh mục, ví dụ trangmieng. */
    public TrangThucDon chonDanhMuc(String maDanhMuc) {
        bam(dt("category-tab-" + maDanhMuc));
        return this;
    }

    /** Tab đang được chọn thì có kiểu nút tô đậm. */
    public boolean tabDangChon(String maDanhMuc) {
        String lop = choHien(dt("category-tab-" + maDanhMuc)).getDomAttribute("class");
        return lop != null && lop.contains("btn-dark");
    }

    /** Tên các món đang hiện trong lưới, đọc khi danh sách đã tải xong. */
    public List<String> tenMonDangHien() {
        return docDanhSachOnDinh(TEN_MON);
    }

    /** Bật hoặc tắt bộ lọc chỉ hiện món chay. */
    public TrangThucDon batTatLocChay() {
        bam(NUT_LOC_CHAY);
        return this;
    }

    public boolean dangLocChay() {
        return "true".equals(choHien(NUT_LOC_CHAY).getDomAttribute("aria-pressed"));
    }

    /**
     * Số thẻ món đang mang nhãn "Món chay". So với tổng số thẻ để biết danh sách có lẫn
     * món mặn hay không, mà không phải tra từng tên món một.
     */
    public int soMonCoNhanChay() {
        tenMonDangHien();
        return driver.findElements(NHAN_CHAY).size();
    }

    public int soMonDangHien() {
        tenMonDangHien();
        return driver.findElements(THE_MON).size();
    }

    public int soMonTrongKhoiBanChay() {
        choHien(KHOI_BAN_CHAY);
        return driver.findElements(THE_TRONG_KHOI_BAN_CHAY).size();
    }

    public TrangChiTietMon moChiTietMon(String tenMon) {
        WebElement lienKet = choDanhSach(TEN_MON).stream()
                .filter(p -> p.getText().trim().equals(tenMon))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không thấy món " + tenMon));
        bam(lienKet);
        return new TrangChiTietMon(driver);
    }
}
