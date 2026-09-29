package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.vuonsen.kiemthu.coso.TrangCoSo;
import vn.vuonsen.kiemthu.thanhphan.BangTamTinh;

import java.time.Duration;
import java.time.LocalDate;
import java.util.function.Supplier;

/*
 * Trang đặt tiệc ba bước: /dat-tiec.
 *
 * Bước 1 chọn loại sự kiện, ngày, buổi và số khách. Bước 2 chọn không gian và gói tiệc.
 * Bước 3 nhập thông tin liên hệ. Bảng tạm tính bên phải là một Component Object riêng.
 */
public class TrangDatTiec extends TrangCoSo {

    public static final String DUONG_DAN = "/dat-tiec";

    // ---------- Bước 1 ----------
    private static final By LOAI_SU_KIEN = dt("event-type");
    private static final By NGAY_TO_CHUC = dt("event-date");
    private static final By BUOI = dt("time-slot");
    private static final By SO_KHACH = dt("guest-count");
    private static final By LOI_LOAI_SU_KIEN = dt("error-event-type");
    private static final By LOI_NGAY = dt("error-event-date");
    private static final By LOI_SO_KHACH = dt("error-guest-count");

    // ---------- Bước 2 ----------
    private static final By NUT_KHONG_GIAN = dt("space-pick");
    private static final By NUT_GOI = dt("package-pick");
    private static final By NUT_KHONG_CAN_GOI = dt("no-package-pick");
    private static final By TEN_TRONG_NUT = By.cssSelector(".t");
    private static final By LOI_KHONG_GIAN = dt("error-space");
    private static final By LOI_GOI = dt("error-package");

    // ---------- Bước 3 ----------
    private static final By HO_TEN = dt("customer-name");
    private static final By SO_DIEN_THOAI = dt("customer-phone");
    private static final By EMAIL = dt("customer-email");
    private static final By LOI_HO_TEN = dt("error-customer-name");
    private static final By LOI_SO_DIEN_THOAI = dt("error-customer-phone");

    // ---------- Điều hướng và kết quả ----------
    private static final By NUT_TIEP_TUC = dt("next-step");
    private static final By NUT_QUAY_LAI = dt("prev-step");
    private static final By NUT_GUI = dt("submit-booking");
    private static final By MA_DON = dt("booking-code");

    public TrangDatTiec(WebDriver driver) {
        super(driver);
    }

    public TrangDatTiec mo() {
        moDuongDan(DUONG_DAN);
        choHien(LOAI_SU_KIEN);
        return this;
    }

    // ================= Bước 1 =================

    /** Điền bước 1. Loại sự kiện và buổi dùng mã, ví dụ WEDDING và EVENING. */
    public TrangDatTiec dienBuoc1(String loaiSuKien, LocalDate ngay, String buoi, int soKhach) {
        chon(LOAI_SU_KIEN, loaiSuKien);
        datGiaTri(NGAY_TO_CHUC, ngay.toString());
        chon(BUOI, buoi);
        nhap(SO_KHACH, String.valueOf(soKhach));
        return this;
    }

    public TrangDatTiec doiSoKhach(int soKhach) {
        nhap(SO_KHACH, String.valueOf(soKhach));
        return this;
    }

    public String soKhachDangNhap() {
        return choHien(SO_KHACH).getDomProperty("value");
    }

    public String loaiSuKienDangChon() {
        return new Select(choHien(LOAI_SU_KIEN)).getFirstSelectedOption().getDomAttribute("value");
    }

    public String ngayDangChon() {
        return choHien(NGAY_TO_CHUC).getDomProperty("value");
    }

    public String loiLoaiSuKien() {
        return chuCua(LOI_LOAI_SU_KIEN);
    }

    public String loiNgayToChuc() {
        return chuCua(LOI_NGAY);
    }

    public String loiSoKhach() {
        return chuCua(LOI_SO_KHACH);
    }

    // ================= Điều hướng =================

    public TrangDatTiec tiepTuc() {
        bam(NUT_TIEP_TUC);
        return this;
    }

    public TrangDatTiec quayLai() {
        bam(NUT_QUAY_LAI);
        return this;
    }

    /** Đang ở bước 2: nút chọn không cần gói tiệc chỉ có ở bước này. */
    public boolean dangOBuoc2() {
        choHien(NUT_KHONG_CAN_GOI);
        return true;
    }

    /** Đang ở bước 3: ô họ tên liên hệ chỉ có ở bước này. */
    public boolean dangOBuoc3() {
        choHien(HO_TEN);
        return true;
    }

    // ================= Bước 2 =================

    private WebElement nutTheoTen(By loaiNut, String ten) {
        return choDanhSach(loaiNut).stream()
                .filter(nut -> nut.findElement(TEN_TRONG_NUT).getText().trim().equals(ten))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không thấy lựa chọn " + ten));
    }

    /** Tên không gian đang được chọn ở bước 2, null nếu chưa chọn không gian nào. */
    public String khongGianDangChon() {
        return choDanhSach(NUT_KHONG_GIAN).stream()
                .filter(nut -> {
                    String lop = nut.getDomAttribute("class");
                    return lop != null && lop.contains("sel");
                })
                .map(nut -> nut.findElement(TEN_TRONG_NUT).getText().trim())
                .findFirst()
                .orElse(null);
    }

    /*
     * Bấm một thẻ lựa chọn ở bước 2 rồi chờ tới khi thẻ thật sự mang trạng thái đang chọn.
     *
     * Khối gợi ý ở đầu bước 2 tải xong sau và đẩy bố cục xuống. Bấm đúng lúc đó thì tọa độ vừa
     * tính không còn trúng thẻ: lệnh bấm vẫn chạy trót lọt nhưng không có gì được chọn, mãi tới
     * bước đọc bảng tạm tính kịch bản mới trượt, nên lỗi lúc có lúc không và báo sai chỗ. Kiểm
     * ngay sau khi bấm thì lỗi lộ ra đúng chỗ, và bấm lại là qua được.
     */
    private void bamChonThe(String ten, Runnable bamThe, Supplier<Boolean> daChon) {
        for (int lan = 1; lan <= 3; lan++) {
            bamThe.run();
            try {
                new WebDriverWait(driver, Duration.ofSeconds(2))
                        .ignoring(StaleElementReferenceException.class)
                        .until(d -> daChon.get());
                return;
            } catch (TimeoutException e) {
                // Bố cục vừa đổi nên cú bấm không trúng thẻ, thử lại
            }
        }
        throw new IllegalStateException("Bấm ba lần mà lựa chọn '" + ten + "' vẫn chưa được chọn");
    }

    /** Thẻ lựa chọn đang được chọn thì mang thêm lớp sel. */
    private static boolean daChonThe(WebElement the) {
        String lop = the.getDomAttribute("class");
        return lop != null && lop.contains("sel");
    }

    public TrangDatTiec chonKhongGian(String tenKhongGian) {
        bamChonThe(tenKhongGian,
                () -> bam(nutTheoTen(NUT_KHONG_GIAN, tenKhongGian)),
                () -> tenKhongGian.equals(khongGianDangChon()));
        return this;
    }

    /** Nút chọn không gian đang bị khóa, ví dụ vì số khách vượt sức chứa. */
    public boolean khongGianBiKhoa(String tenKhongGian) {
        return !nutTheoTen(NUT_KHONG_GIAN, tenKhongGian).isEnabled();
    }

    public TrangDatTiec chonGoi(String tenGoi) {
        bamChonThe(tenGoi,
                () -> bam(nutTheoTen(NUT_GOI, tenGoi)),
                () -> daChonThe(nutTheoTen(NUT_GOI, tenGoi)));
        return this;
    }

    /** Nút chọn gói đang bị khóa, ví dụ vì gói dài hơn buổi đã chọn. */
    public boolean goiBiKhoa(String tenGoi) {
        return !nutTheoTen(NUT_GOI, tenGoi).isEnabled();
    }

    public TrangDatTiec chonKhongCanGoi() {
        bamChonThe("Không cần gói tiệc",
                () -> bam(NUT_KHONG_CAN_GOI),
                () -> daChonThe(driver.findElement(NUT_KHONG_CAN_GOI)));
        return this;
    }

    public String loiKhongGian() {
        return chuCua(LOI_KHONG_GIAN);
    }

    public String loiGoiTiec() {
        return chuCua(LOI_GOI);
    }

    // ================= Bước 3 =================

    public TrangDatTiec dienLienHe(String hoTen, String soDienThoai, String email) {
        nhap(HO_TEN, hoTen);
        nhap(SO_DIEN_THOAI, soDienThoai);
        if (email != null) {
            nhap(EMAIL, email);
        }
        return this;
    }

    public TrangDatTiec guiYeuCau() {
        bam(NUT_GUI);
        return this;
    }

    public String loiHoTen() {
        return chuCua(LOI_HO_TEN);
    }

    public String loiSoDienThoai() {
        return chuCua(LOI_SO_DIEN_THOAI);
    }

    /** Mã đơn trên màn hình gửi thành công. */
    public String maDonVuaTao() {
        return chuCua(MA_DON);
    }

    public BangTamTinh bangTamTinh() {
        return new BangTamTinh(driver);
    }
}
