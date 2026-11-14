package vn.vuonsen.kiemthu.congcu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.trang.TrangDatTiec;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Quay video demo phần Gmail: một khách thao tác thật trên web bằng Gmail và mật khẩu, đi qua
 * đăng ký có mã OTP, đăng nhập, quên mật khẩu, đặt tiệc, đóng cọc và nhận hóa đơn.
 *
 * Máy quay chưa bật Gmail thì thư không đi ra ngoài; lá thư được đọc lại từ bảng email_logs
 * (cột html_body, migration V32) và mở thành một lớp phủ ngay trên trang để người xem thấy đúng
 * nội dung khách nhận. Mã OTP và đường dẫn đặt lại mật khẩu cũng lấy từ cơ sở dữ liệu qua
 * mysql.exe của XAMPP, vì bộ kiểm thử không có trình điều khiển JDBC.
 *
 * Cần backend, giao diện và MySQL đang chạy; trang code dựng trước bằng
 *   node cong-cu/video-demo/tao-trang-code.js gmail
 * Chạy:
 *   mvn -f kiemthu/pom.xml test "-Dtest=QuayVideoGmail" -Dheadless=true
 * Kết quả: target/video-gmail/khung/*.jpg và target/video-gmail/danh-sach.txt cho ffmpeg.
 */
@DisplayName("Công cụ — Quay video demo phần Gmail")
class QuayVideoGmail extends KiemThuCoSo {

    private static final Path THU_MUC = Path.of("target", "video-gmail");
    private static final Path KHUNG = THU_MUC.resolve("khung");
    private static final Path TRANG = Path.of("target", "video-demo", "trang");
    private static final String MYSQL = System.getProperty("mysql.cli", "C:/xampp/mysql/bin/mysql.exe");
    private static final String NGUOI_GUI = "Vuon Sen <khong-tra-loi@vuonsen.vn>";

    private final List<String> danhSach = new ArrayList<>();
    private int soKhung = 0;

    // ---------------------------------------------------------------- tiện ích quay

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

    private void phuDe(String chu) {
        js("let d = document.getElementById('phu-de-demo');"
                + "if (!d) { d = document.createElement('div'); d.id = 'phu-de-demo';"
                + "d.style.cssText = 'position:fixed;left:0;right:0;bottom:0;z-index:2147483647;"
                + "background:rgba(15,23,42,.92);color:#fff;font:600 19px/1.45 Segoe UI,Arial,sans-serif;"
                + "padding:12px 28px;text-align:center';"
                + "document.body.appendChild(d); }"
                + "d.textContent = arguments[0]; d.style.display = arguments[0] ? 'block' : 'none';", chu);
    }

    private void danhDau(String css) {
        js("const e = document.querySelector(arguments[0]);"
                + "if (e) { e.scrollIntoView({block: 'center'}); e.style.outline = '4px solid #f59e0b'; e.style.outlineOffset = '3px'; }", css);
    }

    /* Gõ vào ô như người dùng: đánh dấu ô, xóa nội dung cũ, gõ, chụp một khung. */
    private void go(String css, String chu, double giay) {
        danhDau(css);
        WebElement o = cho(css);
        o.clear();
        o.sendKeys(chu);
        nghi(150);
        khung(giay);
        js("const e = document.querySelector(arguments[0]); if (e) e.style.outline = '';", css);
    }

    private void bam(String css, String chuPhuDe) {
        phuDe(chuPhuDe);
        danhDau(css);
        khung(1.6);
        js("document.querySelector(arguments[0]).click();", css);
    }

    /*
     * Lớp phủ toàn màn hình chứa một trang HTML (lá thư hoặc trang code) trong iframe srcdoc.
     * Không chuyển trang, nên trạng thái đang dở của ứng dụng (ô nhập mã OTP) vẫn còn nguyên.
     */
    private void moLop(String dauLop, String html) {
        js("let d = document.getElementById('lop-demo'); if (d) d.remove();"
                + "d = document.createElement('div'); d.id = 'lop-demo';"
                + "d.style.cssText = 'position:fixed;inset:0;z-index:2147483646;background:#e5e7eb;display:flex;flex-direction:column;font-family:Arial,sans-serif';"
                + "if (arguments[0]) { const h = document.createElement('div');"
                + " h.style.cssText = 'background:#fff;border-bottom:1px solid #d1d5db;padding:12px 22px;font-size:14px;line-height:1.7;color:#111827;white-space:pre-line';"
                + " h.textContent = arguments[0]; d.appendChild(h); }"
                + "const f = document.createElement('iframe'); f.srcdoc = arguments[1];"
                + "f.style.cssText = 'flex:1;border:0;background:#fff;width:100%';"
                + "d.appendChild(f); document.body.appendChild(d);", dauLop, html);
        nghi(900);
    }

    private void lenDau() {
        js("window.scrollTo(0, 0);");
        nghi(300);
    }

    private void dongLop() {
        js("const d = document.getElementById('lop-demo'); if (d) d.remove();");
    }

    private void moThu(String nguoiNhan, String loai, String chuPhuDe, double giay) {
        String tieuDe = sql("SELECT subject FROM email_logs WHERE recipient='" + nguoiNhan + "' AND kind='" + loai + "' ORDER BY id DESC LIMIT 1");
        String html = sql("SELECT IFNULL(html_body, '') FROM email_logs WHERE recipient='" + nguoiNhan + "' AND kind='" + loai + "' ORDER BY id DESC LIMIT 1");
        if (html.isBlank()) {
            String vanBan = sql("SELECT body FROM email_logs WHERE recipient='" + nguoiNhan + "' AND kind='" + loai + "' ORDER BY id DESC LIMIT 1");
            html = "<pre style=\"font:16px/1.65 Arial,sans-serif;white-space:pre-wrap;padding:26px 34px;margin:0;color:#111827\">"
                    + vanBan.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</pre>";
        }
        moLop("Hộp thư của khách — bản xem trước\nTừ: " + NGUOI_GUI + "\nTới: " + nguoiNhan + "\nTiêu đề: " + tieuDe, html);
        phuDe(chuPhuDe);
        khung(giay * 0.5);
        // Thư dài hơn màn hình (hóa đơn có bảng kê) thì cuộn dần xuống cuối cho người xem thấy hết
        Number du = (Number) js("const f = document.querySelector('#lop-demo iframe');"
                + "return Math.max(0, f.contentDocument.documentElement.scrollHeight - f.clientHeight);");
        if (du.intValue() > 40) {
            for (int i = 1; i <= 12; i++) {
                js("document.querySelector('#lop-demo iframe').contentWindow.scrollTo(0, arguments[0]);", du.intValue() * i / 12);
                khung(0.08);
            }
        }
        khung(giay * 0.5);
        dongLop();
    }

    private void trangCode(String ten, double giay) {
        try {
            moLop(null, Files.readString(TRANG.resolve(ten + ".html")));
        } catch (Exception e) {
            throw new IllegalStateException("Thiếu trang " + ten + ", chạy tao-trang-code.js gmail trước", e);
        }
        phuDe("");
        khung(giay);
        dongLop();
    }

    /* Một giá trị từ cơ sở dữ liệu, qua mysql.exe; --raw để không bị đổi ký tự xuống dòng thành \n. */
    private static String sql(String cau) {
        try {
            Process p = new ProcessBuilder(MYSQL, "-u", "root", "-N", "-B", "--raw",
                    "--default-character-set=utf8mb4", "vuonsen_fnb", "-e", cau).redirectErrorStream(true).start();
            String ra = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            p.waitFor();
            return ra.endsWith("\n") ? ra.substring(0, ra.length() - 1) : ra;
        } catch (Exception e) {
            throw new IllegalStateException("Không chạy được mysql: " + cau, e);
        }
    }

    private void dangXuat() {
        js("const n = document.querySelector(\"[data-test='nav-logout']\"); if (n) n.click();");
        nghi(1200);
    }

    private void dangNhap(String email, String matKhau, String chuPhuDe) {
        driver().get(CauHinh.BASE_URL + "/dang-nhap");
        cho("[data-test='login-form']");
        nghi(600);
        phuDe(chuPhuDe);
        go("[data-test='login-form'] [data-test='email']", email, 0.9);
        go("[data-test='login-form'] [data-test='password']", matKhau, 1.2);
        bam("[data-test='submit-login']", "Bấm Đăng nhập");
        new WebDriverWait(driver(), CauHinh.CHO_TOI_DA).until(d -> !d.getCurrentUrl().contains("/dang-nhap"));
        nghi(1500);
        lenDau();
    }

    // ---------------------------------------------------------------- kịch bản

    @Test
    @DisplayName("Quay video demo đăng ký, đăng nhập, quên mật khẩu và hóa đơn qua Gmail")
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

        String email = System.getProperty("email.demo", "minhanh.vuonsen" + (System.currentTimeMillis() % 10000) + "@gmail.com");
        String matKhau = "VuonSen2026";
        String matKhauMoi = "SenVang2027";

        driver().get(CauHinh.BASE_URL + "/");
        cho("header");
        trangCode("g00-mo-dau", 7);
        trangCode("g01-cac-buoc", 9);

        // Bước 1: đăng ký
        driver().get(CauHinh.BASE_URL + "/dang-ky");
        cho("[data-test='register-form']");
        nghi(800);
        phuDe("Bước 1. Khách mở trang Đăng ký");
        khung(2.5);
        phuDe("Điền họ tên, địa chỉ Gmail và số điện thoại");
        go("[data-test='full-name']", "Nguyễn Minh Anh", 0.9);
        go("[data-test='register-form'] [data-test='email']", email, 1.2);
        go("[data-test='phone']", "0912345678", 0.9);
        phuDe("Mật khẩu từ 8 ký tự, có chữ hoa, chữ thường và số");
        go("[data-test='register-form'] [data-test='password']", matKhau, 2.2);
        bam("[data-test='submit-register']", "Bấm Đăng ký");

        // Bước 2: mã OTP
        cho("[data-test='verify-form']");
        lenDau();
        nghi(900);
        phuDe("Bước 2. Tài khoản được tạo ở trạng thái chưa xác thực; hệ thống gửi mã 6 số về Gmail");
        khung(4);
        String ma = sql("SELECT verification_code FROM users WHERE email='" + email + "'");
        moThu(email, "XAC_THUC_EMAIL", "Thư khách nhận trong Gmail: mã nằm ở tiêu đề và trong ô chữ to, hạn 15 phút", 7);

        // Bước 3: nhập sai rồi nhập đúng
        String maSai = "000000".equals(ma) ? "111111" : "000000";
        phuDe("Thử nhập sai mã");
        go("[data-test='verify-code']", maSai, 1.2);
        bam("[data-test='submit-verify']", "Bấm Xác nhận với mã sai");
        cho("[data-test='error']");
        nghi(500);
        danhDau("[data-test='error']");
        phuDe("Nhập sai: hệ thống báo còn 4 lần thử; sai đủ 5 lần thì mã bị hủy, phải bấm gửi lại mã");
        khung(4.5);
        phuDe("Bước 3. Nhập đúng mã vừa nhận trong Gmail");
        go("[data-test='verify-code']", ma, 1.5);
        bam("[data-test='submit-verify']", "Bấm Xác nhận");
        new WebDriverWait(driver(), CauHinh.CHO_TOI_DA).until(d -> !d.getCurrentUrl().contains("/dang-ky"));
        nghi(1800);
        lenDau();
        phuDe("Mã đúng: tài khoản được kích hoạt và đăng nhập luôn");
        khung(4);
        trangCode("g02-ma-otp", 11);
        trangCode("g03-kiem-ma", 10);

        // Bước 4: đăng xuất, đăng nhập lại bằng mật khẩu
        phuDe("Bước 4. Đăng xuất");
        khung(1.5);
        dangXuat();
        dangNhap(email, matKhau, "Đăng nhập lại bằng Gmail và mật khẩu đã đăng ký");
        phuDe("Đăng nhập thành công bằng mật khẩu");
        khung(3.5);

        // Bước 5: quên mật khẩu
        dangXuat();
        driver().get(CauHinh.BASE_URL + "/dang-nhap");
        cho("[data-test='login-form']");
        nghi(600);
        bam("[data-test='link-forgot']", "Bước 5. Quên mật khẩu: bấm Quên mật khẩu?");
        cho("[data-test='forgot-email']");
        nghi(600);
        phuDe("Nhập Gmail của tài khoản");
        go("[data-test='forgot-email']", email, 1.4);
        bam("[data-test='forgot-submit']", "Bấm gửi");
        cho("[data-test='forgot-sent']");
        lenDau();
        nghi(700);
        phuDe("Hệ thống gửi thư có đường dẫn đặt lại mật khẩu, dùng được một lần");
        khung(3.5);
        moThu(email, "DAT_LAI_MAT_KHAU", "Thư đặt lại mật khẩu khách nhận trong Gmail", 7);

        String thu = sql("SELECT body FROM email_logs WHERE recipient='" + email + "' AND kind='DAT_LAI_MAT_KHAU' ORDER BY id DESC LIMIT 1");
        Matcher m = Pattern.compile("(https?://\\S+/dat-lai-mat-khau\\?ma=\\S+)").matcher(thu);
        if (!m.find()) {
            throw new IllegalStateException("Không thấy đường dẫn đặt lại mật khẩu trong thư");
        }
        driver().get(m.group(1));
        cho("[data-test='reset-password']");
        nghi(800);
        phuDe("Mở đường dẫn trong thư, đặt mật khẩu mới");
        khung(2.5);
        go("[data-test='reset-password']", matKhauMoi, 0.9);
        go("[data-test='reset-password-again']", matKhauMoi, 1.2);
        bam("[data-test='reset-submit']", "Bấm Đặt lại mật khẩu");
        cho("[data-test='reset-done']");
        lenDau();
        nghi(700);
        phuDe("Đổi mật khẩu thành công; hệ thống gửi thêm thư báo mật khẩu vừa đổi");
        khung(3.5);
        dangNhap(email, matKhauMoi, "Đăng nhập bằng mật khẩu mới");
        phuDe("Đăng nhập thành công bằng mật khẩu mới");
        khung(3);

        // Bước 6: đặt tiệc, đóng cọc, hóa đơn
        String maDon = null;
        try {
            TrangDatTiec trang = new TrangDatTiec(driver()).mo()
                    .dienBuoc1("WEDDING", GoiApi.ngayChuaDung(), "EVENING", 200);
            nghi(600);
            phuDe("Bước 6. Đặt tiệc: chọn loại tiệc, ngày, buổi và số khách");
            khung(3.5);
            trang.tiepTuc();
            trang.dangOBuoc2();
            trang.chonKhongGian("Sảnh Sen Vàng").chonGoi("Gói Sen Vàng").bangTamTinh().docKhiOnDinh();
            nghi(600);
            phuDe("Chọn sảnh và gói tiệc; bảng chi phí tạm tính tự cập nhật");
            khung(3.5);
            trang.tiepTuc();
            trang.dangOBuoc3();
            nghi(600);
            phuDe("Đã đăng nhập nên họ tên, số điện thoại và Gmail được điền sẵn từ tài khoản");
            khung(3.5);
            maDon = trang.guiYeuCau().maDonVuaTao();
            lenDau();
            nghi(900);
            phuDe("Gửi đơn: nhận mã đơn " + maDon + "; hệ thống gửi thư xác nhận đơn về Gmail");
            khung(3.5);
            moThu(email, "XAC_NHAN_DON", "Thư xác nhận đơn khách nhận trong Gmail", 6);

            bam("[data-test='go-pay']", "Bấm Đóng cọc giữ ngày");
            cho("[data-test='pay-deposit']");
            lenDau();
            nghi(1500);
            phuDe("Trang thanh toán: chọn Đóng cọc (30%) hoặc Trả hết. Lúc quay tạm tắt VNPay để dùng VietQR chạy thử; trả qua VNPay thì hóa đơn gửi y hệt");
            khung(3.5);
            bam("[data-test='pay-deposit']", "Chọn Đóng cọc");
            cho("[data-test='payment-qr-block']");
            nghi(1500);
            danhDau("[data-test='sandbox-confirm']");
            phuDe("Khách quét mã VietQR để chuyển khoản; máy đang ở chế độ chạy thử nên có nút giả lập ngân hàng báo có");
            khung(5);
            bam("[data-test='sandbox-confirm']", "Giả lập ngân hàng báo đã nhận tiền");
            nghi(3000);
            js("window.scrollTo(0, 0);");
            nghi(400);
            phuDe("Tiền được ghi nhận vào đơn; hệ thống gửi hóa đơn về Gmail ngay lúc đó");
            khung(4);
            moThu(email, "HOA_DON", "Hóa đơn khách nhận trong Gmail: bảng kê từng khoản, đã trả, còn phải trả", 9);
        } finally {
            if (maDon != null) {
                try {
                    GoiApi.doiTrangThaiDonTiec(GoiApi.idDonTiecTheoMa(maDon), "CANCELLED");
                } catch (RuntimeException e) {
                    System.out.println("[video-gmail] khong huy duoc don " + maDon + ": " + e.getMessage());
                }
            }
        }

        trangCode("g04-gui-thu", 11);
        trangCode("g05-hoa-don", 10);
        trangCode("g06-bat-gmail", 12);
        trangCode("g99-ket", 6);

        danhSach.add(danhSach.get(danhSach.size() - 2));
        Files.write(THU_MUC.resolve("danh-sach.txt"), danhSach);
        System.out.println("[video-gmail] " + soKhung + " khung, tai khoan " + email + ", don " + maDon);
    }
}
