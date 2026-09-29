package vn.vuonsen.kiemthu.luong;

import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.thanhphan.BangTamTinh;
import vn.vuonsen.kiemthu.trang.TrangDatTiec;

import java.time.LocalDate;

/*
 * Luồng báo giá tiệc chuẩn, dùng lại ở các kịch bản giảm giá ngày lễ.
 *
 * Tiệc chuẩn: 200 khách, buổi tối, Sảnh Sen Vàng, Gói Sen Vàng. Chưa giảm giá thì tiền ăn
 * 90.000.000 cộng phí thuê 3.000.000 là 93.000.000, VAT 7.440.000, tổng 100.440.000.
 */
public final class LuongBaoGiaTiec {

    private LuongBaoGiaTiec() {
    }

    /** Đi hết bước 1 và bước 2 của trang đặt tiệc, đọc bảng tạm tính khi đã ổn định. */
    public static BangTamTinh.SoLieu tamTinhTiecChuan(WebDriver driver, LocalDate ngayToChuc) {
        TrangDatTiec trang = new TrangDatTiec(driver).mo()
                .dienBuoc1("WEDDING", ngayToChuc, "EVENING", 200)
                .tiepTuc();
        trang.dangOBuoc2();
        return trang.chonKhongGian("Sảnh Sen Vàng").chonGoi("Gói Sen Vàng")
                .bangTamTinh().docKhiOnDinh();
    }
}
