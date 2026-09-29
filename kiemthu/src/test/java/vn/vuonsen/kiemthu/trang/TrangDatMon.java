package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.json.Json;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Trang giỏ món và gửi đơn đặt món lẻ: /dat-mon. */
public class TrangDatMon extends TrangCoSo {

    public static final String DUONG_DAN = "/dat-mon";

    /** Khóa localStorage mà giao diện dùng để giữ giỏ món. */
    private static final String KHOA_GIO = "vuonsen-gio-mon";
    private static final DateTimeFormatter GIO_PHUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final By GIO_TRONG = dt("cart-empty");
    private static final By DONG_GIO = dt("cart-row");
    private static final By TEN_TRONG_DONG = By.cssSelector("strong a");
    private static final By SO_LUONG = dt("cart-qty");
    private static final By NUT_BOT = dt("cart-minus");
    private static final By NUT_THEM = dt("cart-plus");
    private static final By NUT_BO_MON = dt("cart-remove");

    private static final By GIAO_TAN_NHA = dt("fulfillment-DELIVERY");
    private static final By AN_TAI_CHO = dt("fulfillment-DINE_IN");
    private static final By HO_TEN = dt("order-name");
    private static final By SO_DIEN_THOAI = dt("order-phone");
    private static final By DIA_CHI = dt("order-address");
    private static final By SO_KHACH = dt("order-guests");
    private static final By THOI_DIEM = dt("order-serve-at");
    private static final By NUT_GUI = dt("submit-order");

    private static final By TIEN_MON = dt("order-subtotal");
    private static final By PHI_GIAO = dt("order-delivery-fee");
    private static final By VAT = dt("order-vat");
    private static final By TONG = dt("order-total");
    private static final By GHI_CHU_GIAO = dt("delivery-note");
    private static final By GIAM_GIA = dt("order-discount");
    private static final By GHI_CHU_GIAM = dt("discount-note");

    /**
     * Số liệu bảng tạm tính đơn món. phiGiao giữ nguyên chữ vì có thể là "Miễn phí". giamGia là
     * null khi không có dòng giảm giá dịp lễ, ghiChuGiam là chuỗi rỗng khi không có câu giải thích.
     */
    public record TamTinh(long tienMon, String phiGiao, long vat, long tong, String ghiChuGiao,
                          Long giamGia, String ghiChuGiam) {
    }

    public TrangDatMon(WebDriver driver) {
        super(driver);
    }

    public TrangDatMon mo() {
        moDuongDan(DUONG_DAN);
        return this;
    }

    /*
     * Bỏ sẵn món vào giỏ rồi mở trang giỏ.
     *
     * Giỏ được giữ trong localStorage và ứng dụng chỉ đọc lúc khởi động. localStorage lại gắn
     * với địa chỉ trang, nên phải mở trang chủ một lần để có địa chỉ, ghi giỏ, rồi mới mở trang
     * giỏ. Dùng cho các kịch bản không kiểm tra thao tác thêm món, để khỏi phải bấm qua từng
     * trang chi tiết món. Mỗi phần tử cần dishId, name, price, slug, quantity.
     */
    public TrangDatMon moVoiGio(List<Map<String, Object>> cacMon) {
        String gio = new Json().toJson(cacMon);
        moDuongDan("/");
        datGio(gio);
        moDuongDan(DUONG_DAN);
        try {
            choHien(DONG_GIO);
        } catch (TimeoutException e) {
            /*
             * Thỉnh thoảng trang mở ra với giỏ rỗng: lần ghi localStorage trước đó chưa kịp có
             * hiệu lực cho lần tải sau. Ghi lại rồi tải lại đúng một lần; vẫn rỗng thì để lỗi
             * nổi lên như thường, vì lúc đó là lỗi thật chứ không phải chậm nhịp.
             */
            datGio(gio);
            taiLaiTrang();
            choHien(DONG_GIO);
        }
        return this;
    }

    private void datGio(String gio) {
        chayJs("window.localStorage.setItem(arguments[0], arguments[1]);", KHOA_GIO, gio);
    }

    // ================= Giỏ món =================

    private WebElement dongCuaMon(String tenMon) {
        return choDanhSach(DONG_GIO).stream()
                .filter(dong -> dong.findElement(TEN_TRONG_DONG).getText().trim().equals(tenMon))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Giỏ không có món " + tenMon));
    }

    public List<String> tenMonTrongGio() {
        return choDanhSach(DONG_GIO).stream().map(dong -> dong.findElement(TEN_TRONG_DONG).getText().trim()).toList();
    }

    public int soLuong(String tenMon) {
        return Integer.parseInt(dongCuaMon(tenMon).findElement(SO_LUONG).getText().trim());
    }

    public TrangDatMon tangSoLuong(String tenMon) {
        bam(dongCuaMon(tenMon).findElement(NUT_THEM));
        return this;
    }

    public TrangDatMon giamSoLuong(String tenMon) {
        bam(dongCuaMon(tenMon).findElement(NUT_BOT));
        return this;
    }

    public TrangDatMon boMon(String tenMon) {
        bam(dongCuaMon(tenMon).findElement(NUT_BO_MON));
        return this;
    }

    /** Chờ giỏ còn đúng số dòng cho trước, dùng sau khi bỏ bớt món. */
    public TrangDatMon choGioCon(int soDong) {
        cho.until(d -> d.findElements(DONG_GIO).size() == soDong);
        return this;
    }

    public boolean dangHienGioTrong() {
        choHien(GIO_TRONG);
        return true;
    }

    public boolean coNutGuiDon() {
        return dangCo(NUT_GUI);
    }

    // ================= Hình thức nhận và thông tin =================

    public TrangDatMon chonGiaoTanNha() {
        bam(GIAO_TAN_NHA);
        choHien(DIA_CHI);
        return this;
    }

    public TrangDatMon chonAnTaiCho() {
        bam(AN_TAI_CHO);
        choHien(SO_KHACH);
        return this;
    }

    public boolean dangCoODiaChi() {
        return dangCo(DIA_CHI);
    }

    public boolean dangCoOSoKhach() {
        return dangCo(SO_KHACH);
    }

    public TrangDatMon dienThongTinGiao(String hoTen, String soDienThoai, String diaChi, LocalDateTime thoiDiem) {
        nhap(HO_TEN, hoTen);
        nhap(SO_DIEN_THOAI, soDienThoai);
        nhap(DIA_CHI, diaChi);
        datGiaTri(THOI_DIEM, thoiDiem.format(GIO_PHUT));
        return this;
    }

    /** soKhach là chuỗi để thử được cả trường hợp bỏ trống. */
    public TrangDatMon dienThongTinAnTaiCho(String hoTen, String soDienThoai, String soKhach, LocalDateTime thoiDiem) {
        nhap(HO_TEN, hoTen);
        nhap(SO_DIEN_THOAI, soDienThoai);
        nhap(SO_KHACH, soKhach);
        datGiaTri(THOI_DIEM, thoiDiem.format(GIO_PHUT));
        return this;
    }

    /** Đổi thời điểm nhận món. Bảng tạm tính sẽ tính lại vì ngày nhận quyết định có giảm giá dịp lễ không. */
    public TrangDatMon chonThoiDiemNhan(LocalDateTime thoiDiem) {
        datGiaTri(THOI_DIEM, thoiDiem.format(GIO_PHUT));
        return this;
    }

    /** Bấm gửi. Nút chỉ bấm được khi bảng tạm tính đã có số liệu, nên chờ nút mở rồi mới bấm. */
    public TrangDatMon guiDon() {
        bam(NUT_GUI);
        return this;
    }

    /** Câu nhắc lỗi của chính trình duyệt ở ô địa chỉ, rỗng nếu ô hợp lệ. */
    public String loiTrinhDuyetODiaChi() {
        return choHien(DIA_CHI).getDomProperty("validationMessage");
    }

    /** Câu nhắc lỗi của chính trình duyệt ở ô số khách, rỗng nếu ô hợp lệ. */
    public String loiTrinhDuyetOSoKhach() {
        return choHien(SO_KHACH).getDomProperty("validationMessage");
    }

    // ================= Bảng tạm tính =================

    private TamTinh docMotLan(WebDriver d) {
        try {
            List<WebElement> tong = d.findElements(TONG);
            if (tong.isEmpty()) {
                return null;
            }
            return new TamTinh(
                    soTien(d.findElement(TIEN_MON).getText()),
                    d.findElement(PHI_GIAO).getText().trim(),
                    soTien(d.findElement(VAT).getText()),
                    soTien(tong.get(0).getText()),
                    d.findElements(GHI_CHU_GIAO).isEmpty() ? "" : d.findElement(GHI_CHU_GIAO).getText().trim(),
                    d.findElements(GIAM_GIA).isEmpty() ? null : soTien(d.findElement(GIAM_GIA).getText()),
                    d.findElements(GHI_CHU_GIAM).isEmpty() ? "" : d.findElement(GHI_CHU_GIAM).getText().trim());
        } catch (StaleElementReferenceException e) {
            return null;
        }
    }

    /** Đọc bảng tạm tính khi hai lần đọc liên tiếp cho cùng kết quả. */
    public TamTinh docTamTinh() {
        AtomicReference<TamTinh> lanTruoc = new AtomicReference<>();
        TamTinh ketQua = cho.until(d -> {
            TamTinh hienTai = docMotLan(d);
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
     * Chờ bảng có dòng giảm giá dịp lễ rồi mới đọc.
     *
     * Dùng sau khi đổi giờ nhận sang một dịp lễ. Không chờ tổng tiền đổi được, vì trước đó bảng
     * có thể đang hiện đúng con số ấy nếu ngày mặc định cũng là dịp lễ cùng mức giảm.
     */
    public TamTinh docTamTinhCoGiamGia() {
        choHien(GIAM_GIA);
        return docTamTinh();
    }

    /** Chờ bảng không còn dòng giảm giá dịp lễ rồi mới đọc, dùng sau khi đổi giờ nhận sang ngày thường. */
    public TamTinh docTamTinhKhongGiamGia() {
        cho.until(d -> !d.findElements(TONG).isEmpty() && d.findElements(GIAM_GIA).isEmpty());
        return docTamTinh();
    }

    /** Chờ bảng tính lại tới khi tổng tiền khác giá trị trước khi đổi, rồi mới đọc. */
    public TamTinh docTamTinhSauKhiDoi(long tongTruocKhiDoi) {
        cho.until(d -> {
            TamTinh hienTai = docMotLan(d);
            return hienTai != null && hienTai.tong() != tongTruocKhiDoi;
        });
        return docTamTinh();
    }
}
