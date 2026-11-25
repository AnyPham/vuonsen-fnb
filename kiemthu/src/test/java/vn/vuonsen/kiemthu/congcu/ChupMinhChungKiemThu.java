package vn.vuonsen.kiemthu.congcu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.thanhphan.HopThoaiTroLy;
import vn.vuonsen.kiemthu.trang.TrangDatMon;
import vn.vuonsen.kiemthu.trang.TrangHoSo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/*
 * Chụp ảnh minh chứng cho báo cáo đồ án Kiểm thử phần mềm (Nhóm 20), lưu vào
 * target/anh-minh-chung-kiem-thu.
 *
 * Là công cụ, không phải kịch bản kiểm thử: tên lớp không khớp mẫu KiemThu* nên không chạy chung
 * với bộ kiểm thử. Gọi đích danh khi backend và giao diện đang chạy:
 *   mvn -f kiemthu/pom.xml test "-Dtest=ChupMinhChungKiemThu"
 * Các ảnh trợ lý ở chế độ AI cần bật mô hình ngôn ngữ ở backend (ASSISTANT_LLM_ENABLED=true).
 */
@DisplayName("Công cụ — Chụp ảnh minh chứng đồ án kiểm thử")
class ChupMinhChungKiemThu extends KiemThuCoSo {

    private static final Path THU_MUC = Path.of("target", "anh-minh-chung-kiem-thu");

    @BeforeEach
    void khungNhin() {
        driver().manage().window().setSize(new Dimension(1440, 900));
    }

    private void chup(String ten) {
        try {
            Thread.sleep(800);
            Files.createDirectories(THU_MUC);
            Path tep = THU_MUC.resolve(ten + ".png");
            Files.write(tep, ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BYTES));
            System.out.println("[minh-chung] " + tep.toAbsolutePath());
        } catch (Exception loi) {
            throw new IllegalStateException("Không chụp được " + ten, loi);
        }
    }

    private WebElement choHien(By phanTu) {
        return new WebDriverWait(driver(), CauHinh.CHO_TOI_DA).until(ExpectedConditions.visibilityOfElementLocated(phanTu));
    }

    private static By dt(String ten) {
        return By.cssSelector("[data-test='" + ten + "']");
    }

    /** Thu nhỏ trang còn 2/3 rồi cuộn tiêu đề lên đầu, để một ảnh thấy trọn tiêu đề, căn cứ và thẻ sảnh kèm lý do. */
    private void cuonLenDau(WebElement phanTu) {
        ((JavascriptExecutor) driver()).executeScript("document.body.style.zoom = '0.67';"
                + "arguments[0].scrollIntoView({block: 'start', behavior: 'instant'}); window.scrollBy(0, -60);", phanTu);
    }

    private void cuonToi(WebElement phanTu) {
        ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", phanTu);
    }

    @Test
    @DisplayName("Gợi ý theo độ tương đồng trên trang chủ")
    void goiYTrangChu() {
        driver().get(CauHinh.BASE_URL + "/");
        cuonLenDau(choHien(dt("goi-y-tieu-de")));
        chup("mc-goi-y-noi-bat");

        LuongDangNhap.TaiKhoan khach = LuongDangNhap.taoKhachMoi();
        LuongDangNhap.dangNhap(driver(), khach.email(), khach.matKhau());
        new TrangHoSo(driver()).mo().khaiSoThich(Set.of("FAMILY"), 30, null, "TIET_KIEM", false);
        driver().get(CauHinh.BASE_URL + "/");
        cuonLenDau(choHien(dt("goi-y-tieu-de")));
        chup("mc-goi-y-ca-nhan");
    }

    @Test
    @DisplayName("KAN-64: giỏ món dừng ở 200 phần, nút tăng bị khóa")
    void gioMonGioiHan() {
        new TrangDatMon(driver()).moVoiGio(List.of(GoiApi.dongGioMon("Cơm cháy chà bông kho quẹt", 200)));
        cuonToi(choHien(dt("cart-plus")));
        chup("mc-gio-mon-gioi-han-200");
    }

    @Test
    @DisplayName("KAN-65: tìm món với ký tự % không còn ra toàn bộ thực đơn")
    void timMonKyTuDacBiet() {
        driver().get(CauHinh.BASE_URL + "/thuc-don");
        WebElement o = choHien(dt("dish-search"));
        o.sendKeys("%");
        chup("mc-tim-mon-ky-tu-phan-tram");
    }

    @Test
    @DisplayName("KAN-67, KAN-70: trợ lý trả lời câu có lời chào và câu hỏi gộp")
    void troLyNganHang() {
        driver().get(CauHinh.BASE_URL + "/");
        HopThoaiTroLy troLy = new HopThoaiTroLy(driver()).mo();
        troLy.hoi("Chào shop, sảnh nào chứa được 300 khách?");
        chup("mc-tro-ly-chao-kem-cau-hoi");
        troLy.hoi("Cho mình hỏi về không gian và món ăn");
        chup("mc-tro-ly-cau-hoi-gop");
    }

    @Test
    @DisplayName("Trợ lý ở chế độ AI: câu ngoài ngân hàng câu hỏi do mô hình ngôn ngữ trả lời")
    void troLyCheDoAi() {
        driver().get(CauHinh.BASE_URL + "/");
        HopThoaiTroLy troLy = new HopThoaiTroLy(driver()).mo();
        troLy.hoi("Nhà hàng có chỗ đậu xe ô tô cho khách dự tiệc không?");
        chup("mc-tro-ly-ai-ngoai-ngan-hang");
        troLy.hoi("Giá ở đây có rẻ hơn nhà hàng khác không?");
        chup("mc-tro-ly-ai-so-sanh");
    }
}
