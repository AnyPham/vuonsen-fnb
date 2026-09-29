package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang tra cứu đơn đặt món bằng mã: /tra-cuu-mon. */
public class TrangTraCuuMon extends TrangCoSo {

    public static final String DUONG_DAN = "/tra-cuu-mon";

    private static final By O_MA_DON = dt("track-code");
    private static final By NUT_TRA_CUU = dt("track-submit");
    private static final By MA_KET_QUA = dt("result-code");
    private static final By HINH_THUC = dt("result-fulfillment");
    private static final By DONG_MON = dt("result-item");
    private static final By TEN_TRONG_DONG = By.tagName("th");
    private static final By THANH_TIEN_TRONG_DONG = By.tagName("td");
    private static final By TONG = dt("result-total");
    private static final By GIAM_GIA = dt("result-discount");

    public TrangTraCuuMon(WebDriver driver) {
        super(driver);
    }

    public TrangTraCuuMon mo() {
        moDuongDan(DUONG_DAN);
        choHien(O_MA_DON);
        return this;
    }

    public TrangTraCuuMon traCuu(String maDon) {
        nhap(O_MA_DON, maDon);
        bam(NUT_TRA_CUU);
        return this;
    }

    public String maDonKetQua() {
        return chuCua(MA_KET_QUA);
    }

    public String hinhThucNhan() {
        return chuCua(HINH_THUC);
    }

    /** Mỗi dòng dạng "Tên món × số lượng". */
    public List<String> cacDongMon() {
        return choDanhSach(DONG_MON).stream().map(dong -> dong.findElement(TEN_TRONG_DONG).getText().trim()).toList();
    }

    public long thanhTienCuaMon(String tenMon) {
        WebElement dong = choDanhSach(DONG_MON).stream()
                .filter(d -> d.findElement(TEN_TRONG_DONG).getText().trim().startsWith(tenMon))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Đơn không có món " + tenMon));
        return soTien(dong.findElement(THANH_TIEN_TRONG_DONG).getText());
    }

    public long tongCong() {
        return soTien(chuCua(TONG));
    }

    /** Tiền giảm giá dịp lễ đã chốt trong đơn, null nếu đơn không có dòng giảm giá. */
    public Long giamGia() {
        choHien(TONG);
        List<WebElement> dong = driver.findElements(GIAM_GIA);
        return dong.isEmpty() ? null : soTien(dong.get(0).getText());
    }
}
