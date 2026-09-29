package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

/** Trang tra cứu đơn đặt tiệc bằng mã: /tra-cuu. */
public class TrangTraCuuTiec extends TrangCoSo {

    public static final String DUONG_DAN = "/tra-cuu";

    private static final By O_MA_DON = dt("track-code");
    private static final By NUT_TRA_CUU = dt("track-submit");
    private static final By MA_KET_QUA = dt("result-code");
    private static final By TRANG_THAI = dt("result-status");
    private static final By KHONG_GIAN = dt("result-space");

    public TrangTraCuuTiec(WebDriver driver) {
        super(driver);
    }

    public TrangTraCuuTiec mo() {
        moDuongDan(DUONG_DAN);
        choHien(O_MA_DON);
        return this;
    }

    public TrangTraCuuTiec traCuu(String maDon) {
        nhap(O_MA_DON, maDon);
        bam(NUT_TRA_CUU);
        return this;
    }

    public String maDonKetQua() {
        return chuCua(MA_KET_QUA);
    }

    public String trangThai() {
        return chuCua(TRANG_THAI);
    }

    public String khongGian() {
        return chuCua(KHONG_GIAN);
    }
}
