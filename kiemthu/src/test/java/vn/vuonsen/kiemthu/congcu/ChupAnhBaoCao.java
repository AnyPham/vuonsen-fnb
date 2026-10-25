package vn.vuonsen.kiemthu.congcu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/*
 * Chụp ảnh toàn bộ màn hình của hệ thống để chèn vào tiểu luận.
 *
 * Khác ChupAnhMinhChung ở mục đích: lớp kia chụp bằng chứng cho từng việc đã làm, mỗi ảnh
 * nhắm vào một chi tiết cụ thể. Lớp này quét hết các trang theo danh sách đường dẫn và chụp
 * trọn trang, dùng cho mục "Các màn hình trong hệ thống" và phụ lục hướng dẫn người dùng.
 *
 * Tên lớp cố ý không khớp mẫu của Surefire nên không chạy chung với bộ kiểm thử. Gọi đích danh:
 *
 *   mvn -f kiemthu/pom.xml test "-Dtest=ChupAnhBaoCao"
 *
 * Cần bật sẵn backend và giao diện. Ảnh lưu vào target/anh-bao-cao.
 */
@DisplayName("Công cụ — Chụp ảnh màn hình cho báo cáo")
class ChupAnhBaoCao extends KiemThuCoSo {

    private static final Path THU_MUC = Path.of("target", "anh-bao-cao");

    /*
     * Chiều cao tối đa một lát, tính bằng điểm ảnh CSS của trang web.
     *
     * Chụp ở tỉ lệ 0,65 rồi Word thu tiếp về bề ngang 600 điểm, nên 2990 điểm CSS ra
     * đúng khoảng 950 điểm chiều cao trong tài liệu — vừa một trang A4 mà còn chỗ cho
     * dòng chú thích bên dưới.
     */
    private static final double CAO_MOI_LAT = 2990;

    /* Trang công khai: mã ảnh, đường dẫn, phần tử chờ hiện trước khi chụp. */
    private static final List<String[]> TRANG_CONG_KHAI = List.of(
            new String[] { "01-trang-chu", "/", "header" },
            new String[] { "02-khong-gian", "/khong-gian", "[data-test='space-card-link']" },
            new String[] { "03-chi-tiet-khong-gian", "/khong-gian/sanh-sen-vang", "[data-test='space-name']" },
            new String[] { "04-thuc-don", "/thuc-don", "[data-test='dish-card-link']" },
            new String[] { "05-chi-tiet-mon", "/thuc-don/lau-nam-chay", "[data-test='dish-name']" },
            new String[] { "06-goi-tiec", "/goi-tiec", "[data-test='package-card']" },
            new String[] { "07-dat-tiec", "/dat-tiec", "form, .card" },
            new String[] { "08-dat-mon", "/dat-mon", "[data-test='cart-empty'], [data-test='cart-row']" },
            new String[] { "09-uu-dai", "/uu-dai", ".wrap" },
            new String[] { "10-thu-vien", "/thu-vien", ".wrap" },
            new String[] { "11-danh-gia", "/danh-gia", ".wrap" },
            new String[] { "12-tra-cuu-tiec", "/tra-cuu", ".wrap" },
            new String[] { "13-tra-cuu-mon", "/tra-cuu-mon", ".wrap" },
            new String[] { "14-dang-nhap", "/dang-nhap", "form, .card" },
            new String[] { "15-dang-ky", "/dang-ky", "form, .card" },
            new String[] { "16-quen-mat-khau", "/quen-mat-khau", "form, .card" },
            new String[] { "17-dat-lai-mat-khau", "/dat-lai-mat-khau", ".wrap, form, .card" },
            new String[] { "18-thanh-toan", "/thanh-toan", ".wrap, form, .card" });

    /* Trang quản trị, chụp sau khi đăng nhập bằng tài khoản quản trị. */
    private static final List<String[]> TRANG_QUAN_TRI = List.of(
            new String[] { "20-qt-thong-ke", "/quan-tri/thong-ke", ".wrap" },
            new String[] { "21-qt-don-dat-tiec", "/quan-tri/don-dat-tiec", ".wrap" },
            new String[] { "32-qt-lich-dat", "/quan-tri/lich-dat", "[data-test='calendar-grid']" },
            new String[] { "22-qt-don-dat-mon", "/quan-tri/don-dat-mon", ".wrap" },
            new String[] { "23-qt-thuc-don", "/quan-tri/thuc-don", ".wrap" },
            new String[] { "24-qt-goi-tiec", "/quan-tri/goi-tiec", ".wrap" },
            new String[] { "25-qt-khong-gian", "/quan-tri/khong-gian", ".wrap" },
            new String[] { "26-qt-danh-gia", "/quan-tri/danh-gia", ".wrap" },
            new String[] { "27-qt-ngay-le", "/quan-tri/ngay-le", ".wrap" },
            new String[] { "28-qt-thanh-toan", "/quan-tri/thanh-toan", ".wrap" },
            new String[] { "29-qt-tai-khoan", "/quan-tri/tai-khoan", ".wrap" },
            new String[] { "30-ho-so", "/ho-so", ".wrap" },
            new String[] { "31-don-cua-toi", "/don-cua-toi", ".wrap" });

    // ---------------- Hàm dùng chung ----------------

    private Object js(String ma, Object... thamSo) {
        return ((JavascriptExecutor) driver()).executeScript(ma, thamSo);
    }

    /*
     * Chờ hiệu ứng vẽ xong rồi mới chụp. Biểu đồ recharts vẽ dần trong khoảng một giây và
     * ảnh tải chậm chỉ nạp khi lọt vào khung nhìn, nên phải cuộn hết trang một lượt trước.
     */
    private void chuanBiTrang() {
        js("window.scrollTo(0, document.body.scrollHeight);");
        nghi(700);
        js("window.scrollTo(0, 0);");
        nghi(900);
    }

    private static void nghi(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /*
     * Chụp trọn chiều cao trang bằng lệnh CDP, không chỉ phần đang nhìn thấy.
     *
     * Trang dài hơn một trang giấy thì cắt thành nhiều lát bằng nhau rồi chụp từng lát,
     * thay vì chụp một ảnh thật cao. Lý do: ảnh cao đưa vào Word phải thu nhỏ theo bề
     * ngang trang A4, cao quá thì chữ trong ảnh bé tới mức không đọc nổi. Cắt ở mức CDP
     * nên không cần thư viện xử lý ảnh nào, và mỗi lát vẫn là ảnh chụp thật của trang.
     */
    @SuppressWarnings("unchecked")
    private void chupTronTrang(String ten) {
        chuanBiTrang();
        Map<String, Object> cd = (Map<String, Object>) js(
                "return {w: Math.max(document.body.scrollWidth, document.documentElement.clientWidth),"
                        + " h: Math.max(document.body.scrollHeight, document.documentElement.clientHeight)};");
        double rong = ((Number) cd.get("w")).doubleValue();
        double cao = ((Number) cd.get("h")).doubleValue();

        int soLat = (int) Math.ceil(cao / CAO_MOI_LAT);
        double caoLat = cao / soLat;
        for (int i = 0; i < soLat; i++) {
            String tenLat = soLat == 1 ? ten : ten + "-" + (char) ('a' + i);
            chupMotLat(tenLat, rong, i * caoLat, caoLat);
        }
    }

    /*
     * Chup dang JPEG chu khong PHAI PNG, va thu nho con 0,65 lan.
     *
     * PNG khong nen duoc anh chup that nen trang nhieu anh mon an phinh toi vai megabyte,
     * ca bo anh len toi 20MB, nhung vao Word thi tep bao cao nang khong mo noi. O 0,65
     * lan, anh con khoang 1200 diem ngang, van gap doi be ngang vung chu cua trang A4 nen
     * in ra khong vo hat.
     */
    private void chupMotLat(String ten, double rong, double y, double cao) {
        Map<String, Object> ketQua = ((HasCdp) driver()).executeCdpCommand("Page.captureScreenshot",
                Map.of("format", "jpeg", "quality", 82, "captureBeyondViewport", true,
                        "clip", Map.of("x", 0, "y", y, "width", rong, "height", cao, "scale", 0.65)));
        luu(ten, (String) ketQua.get("data"));
    }

    private void luu(String ten, String duLieuBase64) {
        try {
            Files.createDirectories(THU_MUC);
            Path tep = THU_MUC.resolve(ten + ".jpg");
            Files.write(tep, Base64.getDecoder().decode(duLieuBase64));
            System.out.println("[anh-bao-cao] " + tep.getFileName());
        } catch (Exception loi) {
            throw new IllegalStateException("Không lưu được ảnh " + ten, loi);
        }
    }

    /*
     * Mở một đường dẫn rồi chụp. Trang nào hỏng thì chỉ in cảnh báo chứ không dừng cả lượt:
     * đây là công cụ chụp ảnh, thiếu một ảnh còn hơn mất cả bộ.
     */
    @Test
    @DisplayName("Chụp trang tài liệu API đang chạy trên máy chủ")
    void chupTaiLieuApi() {
        try {
            driver().get(CauHinh.API_URL + "/swagger-ui/index.html");
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".opblock-tag, .opblock")));
            nghi(1200);
            chupTronTrang("19-tai-lieu-api");
        } catch (Exception loi) {
            System.out.println("[anh-bao-cao] BO QUA tai lieu API: " + loi.getClass().getSimpleName());
        }
    }

    /* Be ngang mo phong: dien thoai, may tinh bang. De cuong yeu cau chay duoc tren ca hai. */
    private static final List<int[]> KHO_MAN_HINH = List.of(
            new int[] { 390, 844 }, new int[] { 820, 1180 });

    private static final List<String[]> TRANG_RESPONSIVE = List.of(
            new String[] { "dien-thoai", "/" },
            new String[] { "dien-thoai", "/thuc-don" },
            new String[] { "dien-thoai", "/dat-tiec" },
            new String[] { "may-tinh-bang", "/" },
            new String[] { "may-tinh-bang", "/khong-gian" });

    @Test
    @DisplayName("Chụp giao diện trên khổ điện thoại và máy tính bảng")
    void chupGiaoDienResponsive() {
        int stt = 50;
        for (String[] trang : TRANG_RESPONSIVE) {
            int[] kho = trang[0].equals("dien-thoai") ? KHO_MAN_HINH.get(0) : KHO_MAN_HINH.get(1);
            try {
                datKhoManHinh(kho[0], kho[1]);
                driver().get(CauHinh.BASE_URL + trang[1]);
                new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                        .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("header")));
                /* An thanh cuon: o kho may tinh bang no de lai mot dai xam doc mep phai anh. */
                js("const d = document.createElement('style');"
                        + "d.textContent = '::-webkit-scrollbar{width:0;height:0}';"
                        + "document.head.appendChild(d);");
                nghi(700);
                /* Chi lay dung khung nhin: muc dich la cho thay bo cuc vua man hinh, khong phai ca trang. */
                chupVungNhin(stt + "-" + trang[0] + trang[1].replace("/", "-"));
            } catch (Exception loi) {
                System.out.println("[anh-bao-cao] BO QUA " + trang[0] + trang[1] + ": "
                        + loi.getClass().getSimpleName());
            }
            stt++;
        }
        xoaKhoManHinh();
    }

    @Test
    @DisplayName("Chụp bản đồ Google Maps trên trang chi tiết không gian")
    void chupBanDo() {
        try {
            driver().get(CauHinh.BASE_URL + "/khong-gian/sanh-sen-vang");
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("iframe")));
            js("const f = document.querySelector('iframe');"
                    + "f.scrollIntoView({block: 'center'});");
            nghi(2500);
            chupVungNhin("55-ban-do");
        } catch (Exception loi) {
            System.out.println("[anh-bao-cao] BO QUA ban do: " + loi.getClass().getSimpleName());
        }
    }

    /* Ep be ngang cua so ve dung so diem anh can do, khong phu thuoc cua so that cua may. */
    private void datKhoManHinh(int rong, int cao) {
        ((HasCdp) driver()).executeCdpCommand("Emulation.setDeviceMetricsOverride",
                Map.of("width", rong, "height", cao, "deviceScaleFactor", 2, "mobile", rong < 768));
    }

    private void xoaKhoManHinh() {
        ((HasCdp) driver()).executeCdpCommand("Emulation.clearDeviceMetricsOverride", Map.of());
    }

    /* Chup dung phan dang nhin thay, dung khi chi can mot khoi chu khong can ca trang. */
    private void chupVungNhin(String ten) {
        Map<String, Object> ketQua = ((HasCdp) driver()).executeCdpCommand("Page.captureScreenshot",
                Map.of("format", "jpeg", "quality", 85));
        luu(ten, (String) ketQua.get("data"));
    }

    @Test
    @DisplayName("Chụp dải thông báo khuyến mãi trên trang chủ")
    void chupThongBaoKhuyenMai() {
        /* Dựng tạm một dịp đang diễn ra để ảnh cho thấy đúng trạng thái đáng chú ý nhất. */
        java.time.LocalDate homNay = java.time.LocalDate.now();
        /* Tên không mang tiền tố dữ liệu thử, vì ảnh này đưa vào báo cáo. Xóa theo mã sau khi chụp. */
        Long maDip = null;
        try {
            Map<String, Object> dip = vn.vuonsen.kiemthu.coso.GoiApi.taoDipLe(
                    "Tết Nguyên Đán", homNay, homNay.plusDays(3), 0.20, true);
            maDip = ((Number) dip.get("id")).longValue();

            driver().get(CauHinh.BASE_URL + "/");
            js("localStorage.removeItem('vuonsen.khuyenmai.daDong');");
            driver().get(CauHinh.BASE_URL + "/");
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".km-dai")));
            nghi(600);
            chupVungNhin("56-thong-bao-khuyen-mai");
        } catch (Exception loi) {
            System.out.println("[anh-bao-cao] BO QUA thong bao khuyen mai: " + loi.getClass().getSimpleName());
        } finally {
            if (maDip != null) {
                vn.vuonsen.kiemthu.coso.GoiApi.xoaDipLe(maDip);
            }
        }
    }

    private void moVaChup(String[] trang) {
        String ten = trang[0], duongDan = trang[1], cho = trang[2];
        try {
            driver().get(CauHinh.BASE_URL + duongDan);
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cho)));
            chupTronTrang(ten);
        } catch (Exception loi) {
            System.out.println("[anh-bao-cao] BO QUA " + ten + " (" + duongDan + "): "
                    + loi.getClass().getSimpleName());
        }
    }

    /*
     * Kết xuất các sơ đồ của báo cáo từ tệp HTML chứa SVG thành ảnh.
     *
     * Vẽ sơ đồ bằng SVG rồi nhờ trình duyệt kết xuất, thay vì vẽ tay trong công cụ đồ họa:
     * sơ đồ nằm trong kho mã nguồn dưới dạng văn bản nên sửa được bằng trình soạn thảo, so
     * sánh được giữa các phiên bản, và chạy lại một lệnh là ra bộ ảnh mới.
     *
     * Chụp đúng khung của thẻ svg chứ không chụp cả cửa sổ, nên ảnh không có viền trắng thừa.
     */
    @SuppressWarnings("unchecked")
    private void chupSoDo(String tenTep) {
        Path nguon = Path.of("..", "cong-cu", "tieu-luan", "so-do", tenTep + ".html")
                .toAbsolutePath().normalize();
        try {
            driver().get(nguon.toUri().toString());
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.id("hinh")));
            nghi(500);
            Map<String, Object> k = (Map<String, Object>) js(
                    "const r = document.getElementById('hinh').getBoundingClientRect();"
                            + "return {w: r.width, h: r.height};");
            Map<String, Object> ketQua = ((HasCdp) driver()).executeCdpCommand("Page.captureScreenshot",
                    Map.of("format", "png", "captureBeyondViewport", true,
                            "clip", Map.of("x", 0, "y", 0,
                                    "width", ((Number) k.get("w")).doubleValue(),
                                    "height", ((Number) k.get("h")).doubleValue(), "scale", 1.4)));
            luuPng("so-do-" + tenTep, (String) ketQua.get("data"));
        } catch (Exception loi) {
            System.out.println("[anh-bao-cao] BO QUA so do " + tenTep + ": " + loi.getClass().getSimpleName());
        }
    }

    /* Sơ đồ là nét vẽ nên giữ PNG, JPEG sẽ làm nhòe chữ nhỏ và viền mảnh. */
    private void luuPng(String ten, String duLieuBase64) {
        try {
            Files.createDirectories(THU_MUC);
            Path tep = THU_MUC.resolve(ten + ".png");
            Files.write(tep, Base64.getDecoder().decode(duLieuBase64));
            System.out.println("[anh-bao-cao] " + tep.getFileName());
        } catch (Exception loi) {
            throw new IllegalStateException("Không lưu được ảnh " + ten, loi);
        }
    }

    @Test
    @DisplayName("Kết xuất các sơ đồ của báo cáo")
    void ketXuatSoDo() {
        List.of("kien-truc", "usecase-tong-quat", "usecase-dat-tiec",
                "usecase-quan-tri", "usecase-tro-ly", "erd",
                "activity-dat-tiec", "activity-tro-ly",
                "sequence-xac-nhan-don", "sequence-tro-ly",
                "ket-qua-kiem-thu",
                /* So do ly thuyet cho chuong 2 */
                "lt-kim-tu-thap", "lt-kien-truc", "lt-spring-boot", "lt-dom-ao", "lt-rest",
                "lt-jwt", "lt-selenium", "lt-pom", "lt-github-actions", "lt-goi-y",
                "lt-rang-buoc-llm", "lt-tinh-gia").forEach(this::chupSoDo);
    }

    // ---------------- Các lượt chụp ----------------

    @Test
    @DisplayName("Chụp toàn bộ màn hình công khai")
    void chupTrangCongKhai() {
        TRANG_CONG_KHAI.forEach(this::moVaChup);
    }

    @Test
    @DisplayName("Chụp toàn bộ màn hình quản trị")
    void chupTrangQuanTri() {
        LuongDangNhap.quanTri(driver());
        TRANG_QUAN_TRI.forEach(this::moVaChup);
    }

    /*
     * Ba ảnh minh họa cho các chức năng chỉ hiện sau một thao tác, không mở thẳng bằng
     * đường dẫn được: khối gợi ý, hộp thoại trợ lý và bộ lọc món chay.
     */
    @Test
    @DisplayName("Chụp các chức năng cần thao tác mới hiện")
    void chupChucNangTuongTac() {
        // Khối gợi ý: hiện sau khi khai số khách và loại sự kiện
        try {
            driver().get(CauHinh.BASE_URL + "/khong-gian");
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='filter-guests']")));
            driver().findElement(By.cssSelector("[data-test='filter-guests']")).sendKeys("300");
            nghi(1800);
            chupTronTrang("40-goi-y-phuong-an");
        } catch (Exception e) {
            System.out.println("[anh-bao-cao] BO QUA 40-goi-y-phuong-an: " + e.getClass().getSimpleName());
        }

        // Bộ lọc món chay
        try {
            driver().get(CauHinh.BASE_URL + "/thuc-don");
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='filter-vegetarian']")));
            var nut = driver().findElement(By.cssSelector("[data-test='filter-vegetarian']"));
            // Nut nam giua trang, hop thoai tro ly noi o goc co the che mat; cuon ra giua roi bam bang JS
            js("arguments[0].scrollIntoView({block:'center',behavior:'instant'}); arguments[0].click();", nut);
            nghi(1500);
            chupTronTrang("41-loc-mon-chay");
        } catch (Exception e) {
            System.out.println("[anh-bao-cao] BO QUA 41-loc-mon-chay: " + e.getClass().getSimpleName());
        }

        // Hộp thoại trợ lý sau khi đã hỏi một câu
        try {
            driver().get(CauHinh.BASE_URL + "/");
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='assistant-open']")));
            driver().findElement(By.cssSelector("[data-test='assistant-open']")).click();
            nghi(800);
            var o = driver().findElement(By.cssSelector("[data-test='assistant-input']"));
            o.sendKeys("Sanh nao chua duoc 500 khach?");
            driver().findElement(By.cssSelector("[data-test='assistant-send']")).click();
            nghi(6000);
            chupTronTrang("42-tro-ly-tu-van");
        } catch (Exception e) {
            System.out.println("[anh-bao-cao] BO QUA 42-tro-ly-tu-van: " + e.getClass().getSimpleName());
        }

        // Bộ lọc gói tiệc
        try {
            driver().get(CauHinh.BASE_URL + "/goi-tiec");
            new WebDriverWait(driver(), CauHinh.CHO_TOI_DA)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='filter-package-max-price']")));
            driver().findElement(By.cssSelector("[data-test='filter-package-max-price']")).sendKeys("4000000");
            nghi(1500);
            chupTronTrang("43-loc-goi-tiec");
        } catch (Exception e) {
            System.out.println("[anh-bao-cao] BO QUA 43-loc-goi-tiec: " + e.getClass().getSimpleName());
        }
    }
}
