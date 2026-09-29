package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;
import vn.vuonsen.kiemthu.thanhphan.DaiAnh;

/** Trang chi tiết một món: /thuc-don/{slug}. */
public class TrangChiTietMon extends TrangCoSo {

    private static final By TEN = dt("dish-name");
    private static final By GIA = dt("dish-price");
    private static final By NGUYEN_LIEU = dt("ingredients");
    private static final By CACH_CHE_BIEN = dt("preparation");
    private static final By KHAU_PHAN = dt("portion");
    private static final By THOI_GIAN_CHUAN_BI = dt("prep-time");
    private static final By LUU_Y = dt("order-note");
    private static final By NUT_THEM_VAO_GIO = dt("add-to-cart");
    private static final By NUT_DAT_TIEC_THAY_THE = dt("book-with-dish");

    public TrangChiTietMon(WebDriver driver) {
        super(driver);
    }

    /** Mở theo slug. Không chờ nội dung, vì slug sai thì trang hiện khối lỗi thay vì nội dung. */
    public TrangChiTietMon mo(String slug) {
        moDuongDan(TrangThucDon.DUONG_DAN + "/" + slug);
        return this;
    }

    public String ten() {
        return chuCua(TEN);
    }

    /** Chữ ở ô giá: một số tiền, hoặc ghi chú giá như "Theo cân". */
    public String gia() {
        return chuCua(GIA);
    }

    public String nguyenLieu() {
        return chuCua(NGUYEN_LIEU);
    }

    public String cachCheBien() {
        return chuCua(CACH_CHE_BIEN);
    }

    public String khauPhan() {
        return chuCua(KHAU_PHAN);
    }

    public String thoiGianChuanBi() {
        return chuCua(THOI_GIAN_CHUAN_BI);
    }

    public String luuYKhiDat() {
        return chuCua(LUU_Y);
    }

    public DaiAnh daiAnh() {
        return new DaiAnh(driver);
    }

    /** Món này có nút thêm vào giỏ hay không. Chờ trang tải xong tên món rồi mới trả lời. */
    public boolean coNutThemVaoGio() {
        choHien(TEN);
        return dangCo(NUT_THEM_VAO_GIO);
    }

    /** Món không đặt lẻ được thì hiện nút chuyển sang đặt tiệc thay cho nút thêm vào giỏ. */
    public boolean coNutDatTiecThayThe() {
        choHien(TEN);
        return dangCo(NUT_DAT_TIEC_THAY_THE);
    }

    public TrangChiTietMon themVaoGio() {
        bam(NUT_THEM_VAO_GIO);
        return this;
    }
}
