package vn.vuonsen.kiemthu.coso;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.util.Map;

/*
 * Lớp cơ sở của mọi lớp kịch bản kiểm thử.
 *
 * Mỗi kịch bản dùng một trình duyệt mới hoàn toàn. Trình duyệt mới có localStorage sạch,
 * nên không kịch bản nào thừa hưởng phiên đăng nhập hay giỏ hàng của kịch bản chạy trước.
 * Cách này chậm hơn dùng chung một trình duyệt, đổi lại đáp ứng yêu cầu độc lập giữa các
 * kịch bản: đổi thứ tự chạy thì kết quả không đổi.
 */
@ExtendWith(ChupManHinhKhiTruot.class)
public abstract class KiemThuCoSo {

    private WebDriver driver;
    private QuayManHinh quay;

    /*
     * Dọn dữ liệu kịch bản tự sinh ra, chạy một lần sau mỗi lớp kịch bản.
     *
     * Để ở lớp cơ sở nên mọi lớp kịch bản đều được dọn, kể cả lớp viết sau này. Một kịch
     * bản tạo ra dữ liệu rồi bỏ đó là kịch bản chưa xong: lần chạy sau nhìn thấy đúng cơ
     * sở dữ liệu như lần chạy trước thì kết quả mới lặp lại được.
     */
    @AfterAll
    static void donDuLieuKichBan() {
        GoiApi.donMonKiemThu();
        GoiApi.donDanhGiaKiemThu();
    }

    @BeforeEach
    void moTrinhDuyet(TestInfo kichBan) {
        driver = taoTrinhDuyet();
        if (CauHinh.QUAY_VIDEO) {
            quay = QuayManHinh.batDau(driver, kichBan);
        }
    }

    @AfterEach
    void dongTrinhDuyet() {
        if (driver != null) {
            // Chế độ trình diễn: giữ màn hình kết quả thêm một lúc cho người xem video kịp nhìn
            TrangCoSo.dungTrinhDien(CauHinh.DEMO_DUNG_MS * 3);
            if (quay != null) {
                quay.dung();
                quay = null;
            }
            // quit chứ không phải close: close chỉ đóng cửa sổ, để lại tiến trình driver chạy ngầm
            driver.quit();
            driver = null;
        }
    }

    public WebDriver driver() {
        return driver;
    }

    private static WebDriver taoTrinhDuyet() {
        if ("edge".equalsIgnoreCase(CauHinh.TRINH_DUYET)) {
            return new EdgeDriver(tuyChonChung(new EdgeOptions()));
        }
        return new ChromeDriver(tuyChonChung(new ChromeOptions()));
    }

    private static <T extends ChromiumOptions<T>> T tuyChonChung(T tuyChon) {
        if (CauHinh.CHAY_AN) {
            // Chạy không cửa sổ, bắt buộc trên máy chủ GitHub Actions vì ở đó không có màn hình
            tuyChon.addArguments("--headless=new");
        }
        tuyChon.addArguments(
                // Cố định kích thước để giao diện không chuyển sang bố cục điện thoại
                "--window-size=1920,1080",
                // Hai tùy chọn cần khi chạy trong container của máy chủ tích hợp liên tục
                "--no-sandbox",
                "--disable-dev-shm-usage");

        /*
         * Tắt trình quản lý mật khẩu. Đăng nhập bằng mật khẩu mẫu như Admin@123 thì trình
         * duyệt bật hộp thoại cảnh báo mật khẩu bị lộ, có thể che mất nút cần bấm.
         */
        tuyChon.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return tuyChon;
    }
}
