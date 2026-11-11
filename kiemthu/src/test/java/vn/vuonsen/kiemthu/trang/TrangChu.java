package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang chủ: /. */
public class TrangChu extends TrangCoSo {

    public static final String DUONG_DAN = "/";

    private static final By TIEU_DE = By.cssSelector(".hero h1");

    // Khối gợi ý thông minh ngay dưới ảnh bìa
    private static final By TIEU_DE_GOI_Y = dt("goi-y-tieu-de");
    private static final By CAN_CU = dt("goi-y-can-cu");
    private static final By SANH_GOI_Y = dt("goi-y-sanh");
    private static final By MON_GOI_Y = dt("goi-y-mon");
    private static final By TIEU_DE_MON = dt("goi-y-mon-tieu-de");
    private static final By LOI_MOI_KHAI = dt("goi-y-moi-khai");

    public TrangChu(WebDriver driver) {
        super(driver);
    }

    public TrangChu mo() {
        moDuongDan(DUONG_DAN);
        choHien(TIEU_DE);
        return this;
    }

    public String tieuDe() {
        return choHien(TIEU_DE).getText().trim();
    }

    // ---------------- Gợi ý thông minh ----------------

    /*
     * Chờ phần sảnh mang đúng kiểu mong đợi, CA_NHAN hoặc NOI_BAT.
     *
     * Khối gợi ý tải sau trang và tải lại khi trạng thái đăng nhập đổi, nên đọc ngay lúc mở
     * trang có thể gặp bản của lần tải trước. Hết thời gian chờ mà vẫn khác thì trả false để
     * kịch bản báo trượt kèm kiểu đang hiện, thay vì ném lỗi khó đọc.
     */
    public boolean choKieuSanh(String kieu) {
        return choThuocTinh(TIEU_DE_GOI_Y, "data-kieu", kieu);
    }

    public boolean choKieuMon(String kieu) {
        return choThuocTinh(TIEU_DE_MON, "data-kieu", kieu);
    }

    public String kieuSanhDangHien() {
        return choHien(TIEU_DE_GOI_Y).getAttribute("data-kieu");
    }

    public String tieuDeGoiY() {
        return chuCua(TIEU_DE_GOI_Y);
    }

    public String canCu() {
        return chuCua(CAN_CU);
    }

    /** Slug của các sảnh được gợi ý, theo thứ tự hiện trên trang. */
    public List<String> slugSanhGoiY() {
        return choDanhSach(SANH_GOI_Y).stream().map(e -> e.getAttribute("data-slug")).toList();
    }

    public List<String> lyDoCuaSanh(String slug) {
        WebElement the = sanh(slug);
        trinhDienKetQua(the, true);
        return the.findElements(dt("goi-y-ly-do")).stream().map(e -> e.getText().trim()).toList();
    }

    /** Nhãn "Bạn đã đặt n lần" của một sảnh, rỗng nếu không có nhãn. */
    public String nhanDaDat(String slug) {
        List<WebElement> nhan = sanh(slug).findElements(dt("goi-y-da-dat"));
        return nhan.isEmpty() ? "" : nhan.get(0).getText().trim();
    }

    public int soMonGoiY() {
        return choDanhSach(MON_GOI_Y).size();
    }

    /** Số món gợi ý mang nhãn món chay. */
    public long soMonChay() {
        return choDanhSach(MON_GOI_Y).stream()
                .filter(e -> !e.findElements(dt("goi-y-mon-chay")).isEmpty())
                .count();
    }

    public String loiMoiKhai() {
        return chuCua(LOI_MOI_KHAI);
    }

    private WebElement sanh(String slug) {
        return choHien(By.cssSelector("[data-test='goi-y-sanh'][data-slug='" + slug + "']"));
    }

    private boolean choThuocTinh(By locator, String thuocTinh, String giaTri) {
        try {
            new WebDriverWait(driver, CauHinh.CHO_TOI_DA)
                    .ignoring(StaleElementReferenceException.class)
                    .until(d -> {
                        List<WebElement> ds = d.findElements(locator);
                        return !ds.isEmpty() && giaTri.equals(ds.get(0).getAttribute(thuocTinh));
                    });
            trinhDienKetQua(locator, true);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
