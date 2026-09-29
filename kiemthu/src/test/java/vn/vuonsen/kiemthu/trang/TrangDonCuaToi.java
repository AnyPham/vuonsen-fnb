package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang đơn đặt tiệc của tài khoản đang đăng nhập: /don-cua-toi. */
public class TrangDonCuaToi extends TrangCoSo {

    public static final String DUONG_DAN = "/don-cua-toi";

    private static final By MA_DON = dt("my-booking-code");

    public TrangDonCuaToi(WebDriver driver) {
        super(driver);
    }

    /** Mở thẳng đường dẫn. Không chờ nội dung vì khách chưa đăng nhập sẽ bị chuyển đi. */
    public TrangDonCuaToi mo() {
        moDuongDan(DUONG_DAN);
        return this;
    }

    /** Mã các đơn đang hiện, đọc khi danh sách đã tải xong. Chưa có đơn nào thì trả danh sách rỗng. */
    public List<String> maCacDon() {
        return docDanhSachOnDinh(MA_DON);
    }
}
