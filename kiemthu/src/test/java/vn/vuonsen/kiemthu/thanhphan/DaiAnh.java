package vn.vuonsen.kiemthu.thanhphan;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

/*
 * Dải ảnh gồm một ảnh lớn và các ảnh nhỏ bên dưới, bấm ảnh nhỏ thì ảnh lớn đổi theo.
 *
 * Xuất hiện ở cả trang chi tiết không gian lẫn trang chi tiết món, nên tách thành Component
 * Object dùng chung thay vì chép cùng một đoạn locator và cơ chế chờ sang hai trang.
 */
public class DaiAnh extends TrangCoSo {

    private static final By ANH_LON = dt("main-image");
    private static final By ANH_NHO = dt("thumb");

    public DaiAnh(WebDriver driver) {
        super(driver);
    }

    public String duongDanAnhLon() {
        return choHien(ANH_LON).getDomAttribute("src");
    }

    public int soAnhNho() {
        return choDanhSach(ANH_NHO).size();
    }

    /** Bấm ảnh nhỏ thứ n (tính từ 0) rồi chờ ảnh lớn đổi theo. */
    public DaiAnh bamAnhNho(int thuTu) {
        String truoc = duongDanAnhLon();
        bam(choDanhSach(ANH_NHO).get(thuTu));
        cho.until(d -> {
            try {
                String sau = d.findElement(ANH_LON).getDomAttribute("src");
                return sau != null && !sau.equals(truoc);
            } catch (StaleElementReferenceException e) {
                return false;
            }
        });
        return this;
    }
}
