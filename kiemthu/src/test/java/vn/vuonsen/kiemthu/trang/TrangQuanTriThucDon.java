package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang quản trị thực đơn: /quan-tri/thuc-don. */
public class TrangQuanTriThucDon extends TrangCoSo {

    public static final String DUONG_DAN = "/quan-tri/thuc-don";

    private static final By NUT_THEM_MOI = dt("dish-new");
    private static final By O_DANH_MUC = dt("dish-category");
    private static final By O_TEN = dt("dish-name");
    private static final By O_GIA = dt("dish-price");
    private static final By O_MO_TA = dt("dish-description");
    private static final By NUT_LUU = dt("dish-save");
    private static final By TEN_TRONG_BANG = By.cssSelector("[data-test='admin-dish-row'] td:first-child");

    public TrangQuanTriThucDon(WebDriver driver) {
        super(driver);
    }

    public TrangQuanTriThucDon mo() {
        moDuongDan(DUONG_DAN);
        choHien(NUT_THEM_MOI);
        return this;
    }

    /*
     * Thêm món mới qua form rồi chờ món xuất hiện trong bảng. Danh mục chọn theo tên hiển thị,
     * ví dụ "Tráng miệng". Bỏ qua ô ảnh vì ô đó chỉ nhận tải tệp lên Cloudinary.
     */
    public TrangQuanTriThucDon themMon(String tenDanhMuc, String tenMon, long gia, String moTa) {
        bam(NUT_THEM_MOI);
        new Select(choHien(O_DANH_MUC)).selectByVisibleText(tenDanhMuc);
        nhap(O_TEN, tenMon);
        nhap(O_GIA, String.valueOf(gia));
        nhap(O_MO_TA, moTa);
        bam(NUT_LUU);
        // Lưu xong bảng tải lại, dòng vừa tìm thấy có thể bị vẽ lại trước khi kịp đọc chữ
        cho.ignoring(StaleElementReferenceException.class)
                .until(d -> d.findElements(TEN_TRONG_BANG).stream().anyMatch(o -> o.getText().trim().equals(tenMon)));
        return this;
    }

    public List<String> tenCacMonTrongBang() {
        return chuCuaTatCa(TEN_TRONG_BANG);
    }
}
