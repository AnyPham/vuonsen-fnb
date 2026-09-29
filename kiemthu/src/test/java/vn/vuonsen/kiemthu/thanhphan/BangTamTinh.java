package vn.vuonsen.kiemthu.thanhphan;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/*
 * Bảng tạm tính chi phí bên phải form đặt tiệc.
 *
 * Tách thành Component Object vì đây là vùng phức tạp nhất của trang đặt tiệc: bảy dòng tiền,
 * có dòng lúc hiện lúc ẩn, và tự tính lại mỗi khi khách đổi lựa chọn. Để chung vào trang đặt
 * tiệc thì lớp đó phình to và lẫn lộn giữa thao tác nhập form với việc đọc số liệu.
 */
public class BangTamTinh extends TrangCoSo {

    private static final By SO_MAM = dt("table-count");
    private static final By DON_GIA = dt("unit-price");
    private static final By TIEN_AN = dt("food-total");
    private static final By PHI_THUE = dt("rental-fee");
    private static final By GIAM_GIA = dt("discount");
    private static final By VAT = dt("vat");
    private static final By TONG = dt("grand-total");
    private static final By COC = dt("deposit");
    private static final By QUY_TAC = By.cssSelector("[data-test='price-rules'] li");

    /**
     * Toàn bộ số liệu của bảng ở một thời điểm. Dòng nào đang ẩn thì để null, ví dụ đơn chỉ
     * thuê không gian không có dòng số mâm và tiền ăn, đơn đặt gần ngày không có dòng giảm giá.
     */
    public record SoLieu(Long soMam, Long donGia, Long tienAn, String phiThue, Long giamGia,
                         long vat, long tong, long coc, List<String> quyTac) {
    }

    public BangTamTinh(WebDriver driver) {
        super(driver);
    }

    /*
     * Đọc bảng khi số liệu đã ổn định: có dòng tổng, và hai lần đọc liên tiếp cách nhau một
     * nhịp chờ cho cùng một kết quả. Dùng cho lần bảng hiện ra đầu tiên.
     */
    public SoLieu docKhiOnDinh() {
        AtomicReference<SoLieu> lanTruoc = new AtomicReference<>();
        SoLieu ketQua = cho.until(d -> {
            SoLieu hienTai = docMotLan(d);
            if (hienTai == null) {
                lanTruoc.set(null);
                return null;
            }
            if (hienTai.equals(lanTruoc.get())) {
                return hienTai;
            }
            lanTruoc.set(hienTai);
            return null;
        });
        trinhDienKetQua(TONG, true);
        return ketQua;
    }

    /*
     * Chờ bảng tính lại xong rồi mới đọc, dùng sau khi khách đổi số khách, gói hay ngày.
     *
     * Giao diện đợi người dùng gõ xong 350 mili giây rồi mới hỏi lại máy chủ, trong lúc đó
     * bảng vẫn hiện con số cũ. Nếu chỉ chờ ổn định thì dễ bắt trúng con số cũ, nên phải chờ
     * tổng tiền đổi khác giá trị trước khi đổi rồi mới đọc.
     */
    public SoLieu docSauKhiTinhLai(long tongTruocKhiDoi) {
        cho.until(d -> {
            SoLieu hienTai = docMotLan(d);
            return hienTai != null && hienTai.tong() != tongTruocKhiDoi;
        });
        return docKhiOnDinh();
    }

    private SoLieu docMotLan(WebDriver d) {
        try {
            String tong = chuHoacNull(d, TONG);
            if (tong == null) {
                return null;
            }
            return new SoLieu(
                    soHoacNull(chuHoacNull(d, SO_MAM)),
                    soHoacNull(chuHoacNull(d, DON_GIA)),
                    soHoacNull(chuHoacNull(d, TIEN_AN)),
                    chuHoacNull(d, PHI_THUE),
                    soHoacNull(chuHoacNull(d, GIAM_GIA)),
                    soTien(chuHoacNull(d, VAT)),
                    soTien(tong),
                    soTien(chuHoacNull(d, COC)),
                    d.findElements(QUY_TAC).stream().map(p -> p.getText().trim()).toList());
        } catch (StaleElementReferenceException e) {
            return null;
        }
    }

    private static String chuHoacNull(WebDriver d, By locator) {
        List<WebElement> ds = d.findElements(locator);
        return ds.isEmpty() ? null : ds.get(0).getText().trim();
    }

    private static Long soHoacNull(String chu) {
        return chu == null ? null : soTien(chu);
    }
}
