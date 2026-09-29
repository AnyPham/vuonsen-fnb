package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

/** Trang hồ sơ cá nhân: /ho-so, chỉ vào được khi đã đăng nhập. */
public class TrangHoSo extends TrangCoSo {

    public static final String DUONG_DAN = "/ho-so";

    public TrangHoSo(WebDriver driver) {
        super(driver);
    }

    /**
     * Mở thẳng đường dẫn trang hồ sơ. Cố ý không chờ nội dung trang, vì khách chưa đăng
     * nhập sẽ bị chuyển đi nơi khác; kịch bản tự kiểm tra xem trang chuyển tới đâu.
     */
    public TrangHoSo mo() {
        moDuongDan(DUONG_DAN);
        return this;
    }
}
