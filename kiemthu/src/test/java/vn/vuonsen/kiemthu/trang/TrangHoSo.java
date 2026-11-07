package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.Set;

/** Trang hồ sơ cá nhân: /ho-so, chỉ vào được khi đã đăng nhập. */
public class TrangHoSo extends TrangCoSo {

    public static final String DUONG_DAN = "/ho-so";

    // Phần sở thích tự khai, dùng cho gợi ý thông minh trên trang chủ
    private static final By KHOI_SO_THICH = dt("so-thich");
    private static final By SO_KHACH = dt("so-thich-so-khach");
    private static final By TAM_GIA = dt("so-thich-tam-gia");
    private static final By AN_CHAY = dt("so-thich-an-chay");
    private static final By LUU = dt("so-thich-luu");
    private static final By DA_LUU = dt("so-thich-da-luu");

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

    /*
     * Khai sở thích rồi bấm lưu, chờ tới khi trang báo đã lưu.
     *
     * loaiTiec là mã loại tiệc (WEDDING, FAMILY...), kieuKhongGian là TRONG_NHA hoặc NGOAI_TROI,
     * tamGia là TIET_KIEM, VUA_PHAI hoặc CAO_CAP; để null là bỏ qua ô đó.
     */
    public TrangHoSo khaiSoThich(Set<String> loaiTiec, Integer soKhach, String kieuKhongGian,
                                 String tamGia, boolean anChay) {
        choHien(KHOI_SO_THICH);
        for (String loai : loaiTiec) {
            WebElement o = choHien(dt("so-thich-loai-" + loai));
            if (!o.isSelected()) {
                bam(o);
            }
        }
        if (soKhach != null) {
            nhap(SO_KHACH, String.valueOf(soKhach));
        }
        if (kieuKhongGian != null) {
            bam(dt("so-thich-kieu-" + kieuKhongGian));
        }
        if (tamGia != null) {
            chon(TAM_GIA, tamGia);
        }
        WebElement chay = choHien(AN_CHAY);
        if (chay.isSelected() != anChay) {
            bam(chay);
        }
        bam(LUU);
        choHien(DA_LUU);
        trinhDienKetQua(DA_LUU, false);
        return this;
    }
}
