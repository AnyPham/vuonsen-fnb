package vn.vuonsen.kiemthu.congcu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.trang.TrangDatMon;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm chứng tạm thời cho hai lỗi sửa ngày 17/09, chạy trước và sau khi sửa:
 *
 *   mvn -f kiemthu/pom.xml test "-Dtest=KiemChungSuaLoi" "-Dheadless=true"
 *
 * Lỗi 1 là lỗi tranh chấp mạng, bấm tay chỉ thỉnh thoảng mới gặp. Để tái hiện chắc chắn, chèn
 * vào trang một đoạn JavaScript giữ yêu cầu tạm tính kế tiếp lại 2 giây trước khi gửi, nên
 * phản hồi của yêu cầu cũ về sau phản hồi của yêu cầu mới.
 */
@DisplayName("Kiểm chứng sửa lỗi 17/09")
class KiemChungSuaLoi extends KiemThuCoSo {

    private static final Path THU_MUC = Path.of("target", "anh-minh-chung");

    @BeforeEach
    @AfterEach
    void donDipLeThu() {
        GoiApi.donDipLeKiemThu();
    }

    private void chup(String ten) throws Exception {
        Files.createDirectories(THU_MUC);
        Files.write(THU_MUC.resolve(ten + ".png"), ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BYTES));
    }

    private Object js(String ma, Object... thamSo) {
        return ((JavascriptExecutor) driver()).executeScript(ma, thamSo);
    }

    @Test
    @DisplayName("Lỗi 1 — Phản hồi tạm tính cũ về muộn không được đè kết quả mới")
    void loi1_tamTinhCuVeMuon() throws Exception {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(LocalDate.now().plusDays(3), 1);
        GoiApi.taoDipLe(GoiApi.TIEN_TO_DIP_LE + "Kiểm chứng tạm tính", ngayLe, ngayLe, 0.15, true);

        TrangDatMon trang = new TrangDatMon(driver()).moVoiGio(List.of(
                GoiApi.dongGioMon("Cơm cháy chà bông kho quẹt", 4)));
        trang.docTamTinh();

        js("if (!window.__daChen) {"
                + "  const moGoc = XMLHttpRequest.prototype.open, guiGoc = XMLHttpRequest.prototype.send;"
                + "  XMLHttpRequest.prototype.open = function (m, url) { this.__url = url; return moGoc.apply(this, arguments); };"
                + "  XMLHttpRequest.prototype.send = function (than) {"
                + "    if (window.__giuLanToi && String(this.__url).includes('/dish-orders/quote')) {"
                + "      window.__giuLanToi = false;"
                + "      setTimeout(() => guiGoc.call(this, than), 2000);"
                + "      return;"
                + "    }"
                + "    return guiGoc.call(this, than);"
                + "  };"
                + "  window.__daChen = true;"
                + "}"
                + "window.__giuLanToi = true;");

        // Yêu cầu cũ: ăn tại chỗ, giờ mặc định không phải dịp lễ, bị giữ 2 giây
        trang.chonAnTaiCho();
        // Yêu cầu mới: giờ nhận rơi vào dịp lễ, gửi ngay nên về trước
        trang.chonThoiDiemNhan(ngayLe.atTime(12, 0));
        trang.docTamTinhCoGiamGia();

        // Chờ quá thời điểm phản hồi cũ về rồi mới xem bảng còn đúng không
        Thread.sleep(3500);
        chup("viec-38-tam-tinh-sau-khi-phan-hoi-cu-ve-muon");

        assertThat(driver().findElements(By.cssSelector("[data-test='order-discount']")))
                .as("Bảng tạm tính phải giữ dòng giảm giá dịp lễ của lựa chọn mới nhất")
                .isNotEmpty();
    }

    @Test
    @DisplayName("Lỗi 2 — Trang thống kê hiện đúng câu báo lỗi của máy chủ")
    void loi2_thongKeHienDungCauBaoLoi() throws Exception {
        LuongDangNhap.quanTri(driver());
        driver().get(CauHinh.BASE_URL + "/quan-tri/thong-ke");

        WebDriverWait cho = new WebDriverWait(driver(), CauHinh.CHO_TOI_DA);
        cho.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='stats-filter']")));
        // Khoảng gần 7 năm, vượt giới hạn 3 năm của máy chủ
        js("arguments[0].value = '2020-01-01'; arguments[1].value = '2026-12-31';",
                driver().findElement(By.cssSelector("[data-test='from']")),
                driver().findElement(By.cssSelector("[data-test='to']")));
        driver().findElement(By.cssSelector("[data-test='apply-range']")).click();

        String cauBao = cho.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")))
                .getText().trim();
        chup("viec-38-thong-ke-bao-loi-khoang-ngay");

        assertThat(cauBao).contains("Khoảng thống kê tối đa 3 năm");
    }
}
