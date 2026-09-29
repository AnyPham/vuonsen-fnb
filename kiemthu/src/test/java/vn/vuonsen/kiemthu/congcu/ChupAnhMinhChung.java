package vn.vuonsen.kiemthu.congcu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongBaoGiaTiec;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.thanhphan.HopThoaiTroLy;
import vn.vuonsen.kiemthu.trang.TrangDanhGia;
import vn.vuonsen.kiemthu.trang.TrangDatMon;
import vn.vuonsen.kiemthu.trang.TrangDatTiec;
import vn.vuonsen.kiemthu.trang.TrangQuanTriDon;
import vn.vuonsen.kiemthu.trang.TrangQuanTriNgayLe;
import vn.vuonsen.kiemthu.trang.TrangQuanTriThucDon;
import vn.vuonsen.kiemthu.trang.TrangThucDon;
import vn.vuonsen.kiemthu.trang.TrangTraCuuTiec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/*
 * Chụp ảnh minh chứng cho các việc đã làm, lưu vào target/anh-minh-chung.
 *
 * Đây là công cụ, không phải kịch bản kiểm thử: không có câu khẳng định nào, chỉ mở đúng màn
 * hình rồi chụp lại. Tên lớp cố ý không khớp mẫu của Surefire (KiemThu... hoặc ...Test) nên nó
 * không chạy chung với bộ kiểm thử; muốn chạy thì gọi đích danh:
 *
 *   mvn -f kiemthu/pom.xml test "-Dtest=ChupAnhMinhChung"
 *   mvn -f kiemthu/pom.xml test "-Dtest=ChupAnhMinhChung#viec26*"     (chỉ một việc)
 *
 * Cần bật sẵn backend và giao diện. Ảnh chụp ở kích thước cửa sổ 1920x1080 như khi chạy kiểm thử.
 *
 * Dữ liệu để lại: dịp lễ minh họa có tiền tố "KT " bị xóa ngay sau khi chụp. Riêng ảnh Việc 29
 * tạo thật hai đơn thuê riêng Sảnh Sen Vàng tên "KT Demo", một đơn được xác nhận và một đơn tự
 * hủy; hai đơn này ở lại trong database, mỗi lần chạy lại sinh thêm hai đơn nữa.
 */
@DisplayName("Công cụ — Chụp ảnh minh chứng")
class ChupAnhMinhChung extends KiemThuCoSo {

    private static final Path THU_MUC = Path.of("target", "anh-minh-chung");
    private static final String COM_CHAY = "Cơm cháy chà bông kho quẹt";
    private static final String SEN_VANG = "Sảnh Sen Vàng";

    @BeforeEach
    @AfterEach
    void donDipLeThu() {
        GoiApi.donDipLeKiemThu();
    }

    // ---------------- Công cụ chụp ----------------

    private void chup(String ten) {
        try {
            Files.createDirectories(THU_MUC);
            Path tep = THU_MUC.resolve(ten + ".png");
            Files.write(tep, ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BYTES));
            System.out.println("[anh-minh-chung] " + tep.toAbsolutePath());
        } catch (Exception loi) {
            throw new IllegalStateException("Không chụp được màn hình " + ten, loi);
        }
    }

    /*
     * Chụp trọn khối bố cục chứa phần tử cho trước, kể cả phần nằm ngoài màn hình.
     *
     * Trang đặt tiệc và đặt món chia hai cột: form bên trái, bảng tạm tính bên phải nằm ở đầu
     * cột và không bám theo khi cuộn. Bước 2 cao hơn một màn hình nên chụp khung nhìn thì luôn
     * mất một trong hai. Lệnh Page.captureScreenshot của Chrome DevTools chụp được vùng ngoài
     * khung nhìn mà không phải đổi kích thước cửa sổ. Cuộn về đầu trang trước khi đo, để thanh
     * menu dính không đè lên vùng chụp.
     */
    private void chupKhoi(By phanTuTrongKhoi, String ten) {
        // Chừa phía trên rộng hơn để lấy luôn tiêu đề trang nằm ngay trên khối
        chupKhoi(phanTuTrongKhoi, ten, 90);
    }

    /** leTren nhỏ khi phía trên khối là tiêu đề lớn, chừa rộng sẽ cắt ngang giữa dòng tiêu đề. */
    private void chupKhoi(By phanTuTrongKhoi, String ten, double leTren) {
        WebElement khoi = (WebElement) js(
                "return arguments[0].closest('.grid') || arguments[0].closest('.card') || arguments[0];",
                choHien(phanTuTrongKhoi));
        chupTuDen(khoi, khoi, ten, leTren);
    }

    /*
     * Chụp vùng từ mép trên phần tử đầu tới mép dưới phần tử cuối, bề ngang phủ cả hai.
     * Dùng cho trang dài như thống kê: chia thành vài ảnh, mỗi ảnh cắt đúng ranh giới một hàng
     * biểu đồ thay vì cắt ngang giữa biểu đồ như khi chụp theo khung nhìn.
     */
    @SuppressWarnings("unchecked")
    private void chupTuDen(WebElement dau, WebElement cuoi, String ten, double leTren) {
        js("window.scrollTo(0, 0);");
        Map<String, Object> khung = (Map<String, Object>) js(
                "const a = arguments[0].getBoundingClientRect(), b = arguments[1].getBoundingClientRect();"
                        + "const trai = Math.min(a.left, b.left), phai = Math.max(a.right, b.right);"
                        + "return {x: trai + window.scrollX, y: a.top + window.scrollY, w: phai - trai, h: b.bottom - a.top};",
                dau, cuoi);
        double le = 28;
        double x = Math.max(0, ((Number) khung.get("x")).doubleValue() - le);
        double y = Math.max(0, ((Number) khung.get("y")).doubleValue() - leTren);
        Map<String, Object> vung = Map.of(
                "x", x, "y", y,
                "width", ((Number) khung.get("w")).doubleValue() + 2 * le,
                "height", ((Number) khung.get("y")).doubleValue() - y + ((Number) khung.get("h")).doubleValue() + le,
                "scale", 1);
        // Thanh menu dính theo khung nhìn nên bị vẽ chèn vào giữa ảnh; ẩn tạm lúc chụp rồi trả lại ngay
        js("const h = document.querySelector('header'); if (h) h.style.visibility = 'hidden';");
        Map<String, Object> ketQua;
        try {
            ketQua = ((HasCdp) driver()).executeCdpCommand("Page.captureScreenshot",
                    Map.of("format", "png", "captureBeyondViewport", true, "clip", vung));
        } finally {
            js("const h = document.querySelector('header'); if (h) h.style.visibility = '';");
        }
        try {
            Files.createDirectories(THU_MUC);
            Path tep = THU_MUC.resolve(ten + ".png");
            Files.write(tep, Base64.getDecoder().decode((String) ketQua.get("data")));
            System.out.println("[anh-minh-chung] " + tep.toAbsolutePath());
        } catch (Exception loi) {
            throw new IllegalStateException("Không chụp được khối " + ten, loi);
        }
    }

    private WebDriverWait cho() {
        return new WebDriverWait(driver(), CauHinh.CHO_TOI_DA);
    }

    private WebElement choHien(By phanTu) {
        return cho().until(ExpectedConditions.visibilityOfElementLocated(phanTu));
    }

    private static By dt(String ten) {
        return By.cssSelector("[data-test='" + ten + "']");
    }

    private Object js(String ma, Object... thamSo) {
        return ((JavascriptExecutor) driver()).executeScript(ma, thamSo);
    }

    private void cuonRaGiua(WebElement phanTu) {
        js("arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", phanTu);
    }

    /*
     * Chờ hiệu ứng vẽ xong rồi mới chụp.
     *
     * Biểu đồ recharts vẽ dần các cột trong khoảng một giây, khung phóng to ảnh cũng có hiệu
     * ứng hiện dần. Không có tín hiệu nào báo hiệu ứng đã xong nên chỉ còn cách chờ một nhịp.
     * Chỗ này chấp nhận được vì đây là công cụ chụp ảnh, không phải kịch bản kiểm thử.
     */
    private static void choHetHieuUng() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------------- Việc 22 ----------------

    @Test
    @DisplayName("Việc 22 — Đặt món lẻ")
    void viec22_datMonLe() {
        TrangDatMon trang = new TrangDatMon(driver()).moVoiGio(List.of(
                GoiApi.dongGioMon(COM_CHAY, 2),
                GoiApi.dongGioMon("Chè bưởi Cần Thơ", 2)));
        trang.chonGiaoTanNha();
        trang.docTamTinh();

        chup("viec-22-dat-mon-le");
    }

    // ---------------- Việc 26: thống kê bằng biểu đồ ----------------

    @Test
    @DisplayName("Việc 26 — Trang thống kê bằng biểu đồ")
    void viec26_thongKe() {
        LuongDangNhap.quanTri(driver());
        driver().get(CauHinh.BASE_URL + "/quan-tri/thong-ke");

        // Lấy cả năm 2026 để thấy đủ các tháng có đơn, kể cả tiệc tổ chức cuối năm
        choHien(dt("stats-filter"));
        js("arguments[0].value = '2026-01-01'; arguments[1].value = '2026-12-31';",
                driver().findElement(dt("from")), driver().findElement(dt("to")));
        driver().findElement(dt("apply-range")).click();
        cho().until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "31/12/2026"));
        choHien(By.cssSelector(".recharts-surface"));
        choHetHieuUng();

        // Nửa trên: bộ lọc, bốn ô số liệu, doanh thu theo tháng, trạng thái đơn và tỉ lệ chọn gói
        chupTuDen(choHien(dt("stats-filter")), choHien(dt("chart-goi-tiec")), "viec-26-thong-ke-1", 28);
        // Nửa dưới: doanh thu theo không gian, loại sự kiện, lấp đầy không gian, đơn đặt món
        chupTuDen(choHien(dt("chart-khong-gian")), choHien(dt("chart-dat-mon")), "viec-26-thong-ke-2", 28);
    }

    // ---------------- Việc 27: ẩn đường dẫn ảnh ----------------

    @Test
    @DisplayName("Việc 27 — Form sửa món chỉ còn ảnh xem trước, không hiện đường dẫn")
    void viec27_formSuaMon() {
        LuongDangNhap.quanTri(driver());
        new TrangQuanTriThucDon(driver()).mo();

        // Nút Sửa không có data-test; công cụ chụp ảnh tìm theo chữ là đủ
        WebElement nutSua = choHien(By.xpath("//tr[@data-test='admin-dish-row'][td[1][normalize-space()='"
                + COM_CHAY + "']]//button[normalize-space()='Sửa']"));
        cuonRaGiua(nutSua);
        nutSua.click();

        cuonRaGiua(choHien(By.xpath("//label[contains(normalize-space(), 'Đổi ảnh')]")));
        choHetHieuUng();
        chup("viec-27-form-sua-mon");
    }

    @Test
    @DisplayName("Việc 27 — Bấm ảnh đánh giá thì phóng to ngay trong trang")
    void viec27_anhDanhGiaPhongTo() {
        new TrangDanhGia(driver()).mo();

        WebElement anh = choHien(By.cssSelector(".anh-danh-gia button"));
        cuonRaGiua(anh);
        anh.click();

        choHien(By.cssSelector("[role='dialog']"));
        choHetHieuUng();
        chup("viec-27-anh-danh-gia-phong-to");
    }

    // ---------------- Việc 28: thuê riêng không gian ----------------

    @Test
    @DisplayName("Việc 28 — Thuê riêng không gian, không kèm gói tiệc")
    void viec28_thueRiengKhongGian() {
        TrangDatTiec trang = new TrangDatTiec(driver()).mo()
                .dienBuoc1("OTHER", GoiApi.ngayThuong(LocalDate.now().plusDays(30), 1), "EVENING", 150)
                .tiepTuc();
        trang.dangOBuoc2();
        trang.chonKhongGian(SEN_VANG).chonKhongCanGoi();
        trang.bangTamTinh().docKhiOnDinh();

        chupKhoi(dt("no-package-pick"), "viec-28-buoc-2-khong-can-goi", 28);
    }

    // ---------------- Việc 29: hai người cùng đặt một sảnh ----------------

    @Test
    @DisplayName("Việc 29 — Hai khách cùng đặt một sảnh, quản trị xác nhận một đơn thì đơn kia tự hủy")
    void viec29_haiKhachCungDatMotSanh() throws Exception {
        LocalDate ngay = GoiApi.ngayChuaDung();
        String[] ma = haiKhachGuiDonCungLuc(ngay);

        LuongDangNhap.quanTri(driver());
        TrangQuanTriDon quanTri = new TrangQuanTriDon(driver()).mo();
        quanTri.timKiem("KT Demo");
        cho().until(d -> quanTri.maCacDonDangHien().containsAll(List.of(ma[0], ma[1])));
        choHetHieuUng();
        chup("viec-29-hai-don-cung-sanh-cung-buoi");

        quanTri.chuyenTrangThai(ma[0], "CONFIRMED");
        cho().until(d -> quanTri.trangThaiCuaDon(ma[1]).toLowerCase().contains("hủy"));
        choHetHieuUng();
        chup("viec-29-xac-nhan-mot-don-don-kia-tu-huy");

        // Đơn vừa xác nhận là đơn thuê riêng, nên dùng luôn làm ảnh tra cứu cho Việc 28
        new TrangTraCuuTiec(driver()).mo().traCuu(ma[0]);
        choHien(dt("result-space"));
        choHetHieuUng();
        chup("viec-28-tra-cuu-chi-thue-khong-gian");
    }

    /*
     * Hai khách gửi đơn thuê riêng cùng sảnh, cùng ngày, cùng buổi, gửi đồng thời bằng hai luồng.
     * Hai luồng cùng chờ ở một vạch xuất phát rồi mới gửi, để hai yêu cầu tới máy chủ gần như
     * cùng lúc. Trả về mã hai đơn theo thứ tự khách A, khách B.
     */
    private static String[] haiKhachGuiDonCungLuc(LocalDate ngay) throws Exception {
        long idSanh = GoiApi.idKhongGian("sanh-sen-vang");
        CountDownLatch vachXuatPhat = new CountDownLatch(1);

        CompletableFuture<String> khachA = CompletableFuture.supplyAsync(
                () -> guiDonThueRieng(vachXuatPhat, idSanh, ngay, "KT Demo khách A", "0901111111"));
        CompletableFuture<String> khachB = CompletableFuture.supplyAsync(
                () -> guiDonThueRieng(vachXuatPhat, idSanh, ngay, "KT Demo khách B", "0902222222"));
        vachXuatPhat.countDown();

        return new String[] {khachA.get(), khachB.get()};
    }

    private static String guiDonThueRieng(CountDownLatch vachXuatPhat, long idSanh, LocalDate ngay,
                                          String hoTen, String soDienThoai) {
        try {
            vachXuatPhat.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        Map<String, Object> than = new HashMap<>();
        than.put("eventType", "OTHER");
        than.put("eventDate", ngay.toString());
        than.put("timeSlot", "EVENING");
        than.put("guestCount", 150);
        than.put("spaceId", idSanh);
        than.put("packageId", null);
        than.put("customerName", hoTen);
        than.put("customerPhone", soDienThoai);

        GoiApi.KetQua kq = GoiApi.goiThu("POST", "/api/v1/bookings", than, null);
        if (kq.ma() >= 400) {
            throw new IllegalStateException("Gửi đơn của " + hoTen + " thất bại: HTTP " + kq.ma() + " " + kq.than());
        }
        Map<?, ?> don = new Json().toType(kq.than(), Map.class);
        return (String) don.get("code");
    }

    // ---------------- Việc 32, 33: giảm giá dịp lễ ----------------

    @Test
    @DisplayName("Việc 32 — Tiệc vào dịp Tết: giảm 20%, không cộng dồn với ưu đãi đặt sớm")
    void viec32_tiecDipTet() {
        // Tết Nguyên Đán 2027 có sẵn trong dữ liệu mẫu, và cách hôm nay hơn 60 ngày nên cũng đủ
        // điều kiện ưu đãi đặt sớm: bảng tạm tính phải chọn mức cao hơn
        LuongBaoGiaTiec.tamTinhTiecChuan(driver(), LocalDate.of(2027, 2, 6));

        chupKhoi(dt("discount"), "viec-32-tiec-dip-tet", 28);
    }

    @Test
    @DisplayName("Việc 32 — Trang quản trị ngày lễ")
    void viec32_quanTriNgayLe() {
        LuongDangNhap.quanTri(driver());
        TrangQuanTriNgayLe trang = new TrangQuanTriNgayLe(driver()).mo();
        trang.danhSach();

        chup("viec-32-quan-tri-ngay-le");
    }

    @Test
    @DisplayName("Việc 32 — Đặt món nhận vào dịp lễ thì có dòng giảm giá")
    void viec32_datMonDipLe() {
        // Đơn món chỉ nhận trước tối đa 30 ngày, mà dịp lễ thật gần nhất còn xa hơn thế,
        // nên tạo tạm một dịp lễ minh họa rồi xóa ngay sau khi chụp
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(LocalDate.now().plusDays(3), 1);
        GoiApi.taoDipLe(GoiApi.TIEN_TO_DIP_LE + "Dịp lễ minh họa", ngayLe, ngayLe, 0.15, true);

        TrangDatMon trang = new TrangDatMon(driver()).moVoiGio(List.of(
                GoiApi.dongGioMon(COM_CHAY, 4),
                GoiApi.dongGioMon("Chè bưởi Cần Thơ", 4)));
        trang.chonAnTaiCho().chonThoiDiemNhan(ngayLe.atTime(12, 0));
        trang.docTamTinhCoGiamGia();

        chupKhoi(dt("order-discount"), "viec-32-dat-mon-dip-le");
    }

    @Test
    @DisplayName("Việc 33 — Trợ lý tư vấn trả lời về ưu đãi dịp lễ")
    void viec33_troLy() {
        new TrangThucDon(driver()).mo();

        new HopThoaiTroLy(driver()).mo().hoi("Dịp lễ sắp tới có giảm giá không?");

        choHetHieuUng();
        chup("viec-33-tro-ly-uu-dai-dip-le");
    }
}
