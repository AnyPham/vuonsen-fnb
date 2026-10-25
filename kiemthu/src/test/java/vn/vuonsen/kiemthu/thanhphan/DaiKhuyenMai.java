package vn.vuonsen.kiemthu.thanhphan;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

/** Dải thông báo khuyến mãi ở đầu trang chủ. */
public class DaiKhuyenMai extends TrangCoSo {

    private static final By DAI = By.cssSelector(".km-dai");
    private static final By MUC_GIAM = By.cssSelector(".km-muc");
    private static final By TEN_DIP = By.cssSelector(".km-ten");
    private static final By CHU_PHU = By.cssSelector(".km-phu");
    private static final By NUT_XEM = By.cssSelector(".km-nut");
    private static final By NUT_CHUONG = By.cssSelector(".km-chuong");
    private static final By NUT_DONG = By.cssSelector(".km-dong");

    public DaiKhuyenMai(WebDriver driver) {
        super(driver);
    }

    public boolean dangHien() {
        return dangCo(DAI);
    }

    public DaiKhuyenMai choHien() {
        choHien(DAI);
        return this;
    }

    public String mucGiam() {
        return choHien(MUC_GIAM).getText().trim();
    }

    public String tenDip() {
        return choHien(TEN_DIP).getText().trim();
    }

    public String chuPhu() {
        return choHien(CHU_PHU).getText().trim();
    }

    public boolean coNutChuong() {
        return dangCo(NUT_CHUONG);
    }

    public DaiKhuyenMai bamXemUuDai() {
        bam(NUT_XEM);
        return this;
    }

    public DaiKhuyenMai dong() {
        bam(NUT_DONG);
        return this;
    }

    /** Thuộc tính trợ năng của dải, để máy đọc màn hình biết đây là thông báo. */
    public String vaiTro() {
        return choHien(DAI).getDomAttribute("role");
    }

    public String thongBaoSong() {
        return choHien(DAI).getDomAttribute("aria-live");
    }

    /** Xóa dấu đã đóng trong localStorage, để chạy lại kịch bản từ trạng thái sạch. */
    public void quenDaDong() {
        chayJs("localStorage.removeItem('vuonsen.khuyenmai.daDong');");
    }
}
