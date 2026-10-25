package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

/** Trang chủ: /. */
public class TrangChu extends TrangCoSo {

    public static final String DUONG_DAN = "/";

    private static final By TIEU_DE = By.cssSelector(".hero h1");

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
}
