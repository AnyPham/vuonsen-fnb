package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;
import vn.vuonsen.kiemthu.thanhphan.DaiAnh;

import java.util.List;

/** Trang chi tiết một không gian: /khong-gian/{slug}. */
public class TrangChiTietKhongGian extends TrangCoSo {

    private static final By TEN = dt("space-name");
    private static final By SUC_CHUA = dt("capacity");
    private static final By GIA_THUE = dt("rental-fee");
    private static final By TIEN_ICH = dt("amenity");
    private static final By BAN_DO = dt("map");
    private static final By NUT_DAT_TIEC = dt("book-space");

    public TrangChiTietKhongGian(WebDriver driver) {
        super(driver);
    }

    /** Mở theo slug. Không chờ nội dung, vì slug sai thì trang hiện khối lỗi thay vì nội dung. */
    public TrangChiTietKhongGian mo(String slug) {
        moDuongDan(TrangKhongGian.DUONG_DAN + "/" + slug);
        return this;
    }

    public String ten() {
        return chuCua(TEN);
    }

    public String sucChua() {
        return chuCua(SUC_CHUA);
    }

    public long giaThue() {
        return soTien(chuCua(GIA_THUE));
    }

    public List<String> tienIch() {
        return chuCuaTatCa(TIEN_ICH);
    }

    public DaiAnh daiAnh() {
        return new DaiAnh(driver);
    }

    public boolean banDoDangHien() {
        return choHien(BAN_DO).isDisplayed();
    }

    public String nguonBanDo() {
        return choHien(BAN_DO).getDomAttribute("src");
    }

    public void bamDatTiec() {
        bam(NUT_DAT_TIEC);
    }
}
