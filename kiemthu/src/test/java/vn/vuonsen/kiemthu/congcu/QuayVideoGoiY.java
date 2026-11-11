package vn.vuonsen.kiemthu.congcu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/*
 * Quay video demo gợi ý sảnh theo lượt xem, kèm các trang code xen giữa.
 *
 * Không quay màn hình thật: chụp từng khung hình của trình duyệt qua CDP, ghi kèm thời
 * gian giữ của mỗi khung, rồi ffmpeg ghép lại. Trang code dựng trước bằng
 * cong-cu/video-demo/tao-trang-code.js. Cần backend, giao diện và tài khoản quản trị.
 *
 *   mvn -f kiemthu/pom.xml test "-Dtest=QuayVideoGoiY" -Dheadless=true -Dbase.url=...
 *
 * Kết quả: target/video-demo/khung/*.jpg và target/video-demo/danh-sach.txt.
 */
@DisplayName("Công cụ — Quay video demo gợi ý theo lượt xem")
class QuayVideoGoiY extends KiemThuCoSo {

    private static final Path THU_MUC = Path.of("target", "video-demo");
    private static final Path KHUNG = THU_MUC.resolve("khung");
    private static final String SANH = "phong-hoi-nghi";

    private final List<String> danhSach = new ArrayList<>();
    private int soKhung = 0;

    private Object js(String ma, Object... thamSo) {
        return ((JavascriptExecutor) driver()).executeScript(ma, thamSo);
    }

    private static void nghi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private WebElement cho(String css) {
        return new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(css)));
    }

    /* Chụp khung nhìn hiện tại thành một khung hình, giữ trên video trong số giây cho trước. */
    private void khung(double giay) {
        try {
            Map<String, Object> kq = ((HasCdp) driver()).executeCdpCommand("Page.captureScreenshot",
                    Map.of("format", "jpeg", "quality", 88));
            String ten = String.format("k%05d.jpg", ++soKhung);
            Files.write(KHUNG.resolve(ten), Base64.getDecoder().decode((String) kq.get("data")));
            danhSach.add("file 'khung/" + ten + "'");
            danhSach.add("duration " + giay);
        } catch (Exception e) {
            throw new IllegalStateException("Không chụp được khung hình", e);
        }
    }

    /* Dải phụ đề cố định ở đáy trang, thay cho lời thuyết minh. */
    private void phuDe(String chu) {
        js("let d = document.getElementById('phu-de-demo');"
                + "if (!d) { d = document.createElement('div'); d.id = 'phu-de-demo';"
                + "d.style.cssText = 'position:fixed;left:0;right:0;bottom:0;z-index:2147483647;"
                + "background:rgba(15,23,42,.9);color:#fff;font:600 20px/1.45 Segoe UI,Arial,sans-serif;"
                + "padding:12px 28px;text-align:center';"
                + "document.body.appendChild(d); }"
                + "d.textContent = arguments[0];", chu);
    }

    /* Cuộn mượt tới phần tử, chụp từng nấc để video thấy được chuyển động. */
    private void cuonToi(String css) {
        double dau = ((Number) js("return window.scrollY;")).doubleValue();
        double dich = ((Number) js("const e = document.querySelector(arguments[0]);"
                + "return Math.max(0, e.getBoundingClientRect().top + window.scrollY - 70);", css)).doubleValue();
        int soNac = 16;
        for (int i = 1; i <= soNac; i++) {
            double t = (double) i / soNac;
            double y = dau + (dich - dau) * (1 - Math.pow(1 - t, 3));
            js("window.scrollTo(0, arguments[0]);", y);
            khung(0.05);
        }
    }

    /* Viền cam quanh phần tử sắp bấm hoặc cần chú ý. */
    private void danhDau(String css) {
        js("const e = document.querySelector(arguments[0]);"
                + "e.style.outline = '4px solid #f59e0b'; e.style.outlineOffset = '4px';", css);
    }

    /* Bấm như người dùng: đánh dấu, dừng một nhịp, rồi bấm (điều hướng trong ứng dụng, không tải lại trang). */
    private void bam(String css, String chuPhuDe) {
        phuDe(chuPhuDe);
        danhDau(css);
        khung(1.6);
        js("document.querySelector(arguments[0]).click();", css);
    }

    private void trangCode(String ten, double giay) {
        Path tep = Path.of("target", "video-demo", "trang", ten + ".html").toAbsolutePath();
        driver().get(tep.toUri().toString());
        nghi(400);
        khung(giay);
    }

    private void moChiTietSanh(int lan) {
        bam("a[href='/khong-gian']", "Vào mục Không gian");
        cho("a[href='/khong-gian/" + SANH + "']");
        nghi(1200);
        cuonToi("a[href='/khong-gian/" + SANH + "']");
        bam("a[href='/khong-gian/" + SANH + "']", "Mở trang chi tiết Phòng Hội Nghị Lúa (lần " + lan + ")");
        cho("[data-test='space-name']");
        nghi(1500);
        phuDe("Trang chi tiết Phòng Hội Nghị Lúa, lần xem thứ " + lan + ". Máy chủ ghi một lượt xem gắn với tài khoản");
        khung(3.0);
    }

    private void xemKhoiGoiY(String chuPhuDe, double giay) {
        cho("[data-test='goi-y-tieu-de']");
        nghi(1500);
        cuonToi("[data-test='goi-y-trang-chu']");
        phuDe(chuPhuDe);
        khung(giay);
    }

    /* Cuộn thêm để thấy trọn phần chữ của các thẻ sảnh: tên, lý do gợi ý, giá. */
    private void cuonXemLyDo() {
        double dau = ((Number) js("return window.scrollY;")).doubleValue();
        double dich = ((Number) js("const b = document.querySelector(\"[data-test='goi-y-sanh']\").getBoundingClientRect();"
                + "return b.bottom + window.scrollY - 640;")).doubleValue();
        for (int i = 1; i <= 12; i++) {
            js("window.scrollTo(0, arguments[0]);", dau + (dich - dau) * i / 12.0);
            khung(0.05);
        }
    }

    @Test
    @DisplayName("Quay video demo gợi ý sảnh theo lượt xem")
    void quay() throws Exception {
        if (Files.exists(KHUNG)) {
            try (var ds = Files.list(KHUNG)) {
                for (Path p : ds.toList()) {
                    Files.delete(p);
                }
            }
        }
        Files.createDirectories(KHUNG);
        ((HasCdp) driver()).executeCdpCommand("Emulation.setDeviceMetricsOverride",
                Map.of("width", 1280, "height", 720, "deviceScaleFactor", 1, "mobile", false));

        trangCode("00-mo-dau", 5);

        // Tài khoản khách mới: chưa đặt tiệc, chưa khai sở thích
        String email = "minhanh" + (System.currentTimeMillis() % 100000) + "@vuonsen.vn";
        GoiApi.dangKyKhach("Nguyễn Minh Anh", email, LuongDangNhap.MAT_KHAU_KHACH);

        driver().get(CauHinh.BASE_URL + "/dang-nhap");
        WebElement oEmail = cho("input[type='email']");
        nghi(800);
        phuDe("Đăng nhập bằng một tài khoản khách mới: chưa đặt tiệc, chưa khai sở thích");
        khung(2.0);
        oEmail.sendKeys(email);
        driver().findElement(By.cssSelector("input[type='password']")).sendKeys(LuongDangNhap.MAT_KHAU_KHACH);
        khung(1.5);
        driver().findElement(By.cssSelector("form button[type='submit']")).click();
        new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                .until(d -> !d.getCurrentUrl().contains("/dang-nhap"));
        driver().get(CauHinh.BASE_URL + "/");

        xemKhoiGoiY("Chưa có dữ liệu gì về khách, nên trang chủ gợi ý theo độ phổ biến: \"Sảnh nổi bật\"", 3.0);
        cuonXemLyDo();
        phuDe("Ba sảnh có nhiều tiệc nhất trong 365 ngày: Sảnh Sen Vàng, Vườn Cau, Nhà Rường Gỗ");
        khung(4.5);
        js("window.scrollTo(0, 0);");
        nghi(400);

        moChiTietSanh(1);
        moChiTietSanh(2);

        trangCode("01-ghi-luot-xem", 9);
        trangCode("02-dung-ho-so", 11);

        driver().get(CauHinh.BASE_URL + "/");
        nghi(800);
        phuDe("Thoát ra, vào lại trang chủ");
        khung(1.5);
        xemKhoiGoiY("Trang chủ đổi sang \"Những sảnh phù hợp với nhu cầu của bạn\"", 3.5);
        cuonXemLyDo();
        danhDau("[data-test='goi-y-sanh']");
        phuDe("Sảnh vừa xem đứng đầu, kèm lý do \"Bạn đã xem sảnh này 2 lần\" và số khách có hồ sơ giống đã đặt");
        khung(6.0);
        phuDe("Hai sảnh còn lại cùng cỡ với sảnh đã xem; Sảnh Sen Vàng tuy phổ biến nhưng khác xa hồ sơ nên bị loại");
        khung(5.0);

        trangCode("03-cosine", 9);
        trangCode("04-xep-hang", 12);
        trangCode("99-ket", 5);

        // ffmpeg concat cần lặp lại khung cuối, không thì khung cuối không được giữ
        danhSach.add(danhSach.get(danhSach.size() - 2));
        Files.write(THU_MUC.resolve("danh-sach.txt"), danhSach);
        System.out.println("[video-demo] " + soKhung + " khung, tai khoan " + email);
    }
}
