package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;
import java.util.Optional;

/** Trang lịch đặt chỗ của bộ phận quản trị: /quan-tri/lich-dat. */
public class TrangQuanTriLich extends TrangCoSo {

    public static final String DUONG_DAN = "/quan-tri/lich-dat";

    private static final By LUOI = dt("calendar-grid");
    private static final By O_NGAY = dt("calendar-cell");
    private static final By NUT_XEM_THANG = dt("calendar-mode-month");
    private static final By NUT_XEM_NGAY = dt("calendar-mode-day");
    private static final By NUT_THANG_TRUOC = dt("calendar-prev");
    private static final By NUT_THANG_SAU = dt("calendar-next");
    private static final By TIEU_DE = dt("calendar-title");
    private static final By TOM_TAT = dt("calendar-summary");
    private static final By O_CHON_NGAY = dt("calendar-date");
    private static final By BANG_NGAY = dt("calendar-day-table");
    private static final By DONG_NGAY = dt("calendar-day-row");
    private static final By O_TRONG_DONG = By.tagName("td");

    /** Một dòng chi tiết trong chế độ xem ngày, giữ nguyên chữ đang hiển thị. */
    public record DongLich(String loai, String maDon, String khach, String chiTiet,
                           String buoiHoacGio, String trangThai) {
    }

    public TrangQuanTriLich(WebDriver driver) {
        super(driver);
    }

    /** Mở thẳng đường dẫn. Không chờ lưới vì tài khoản thiếu quyền sẽ thấy màn hình khác. */
    public TrangQuanTriLich mo() {
        moDuongDan(DUONG_DAN);
        return this;
    }

    /*
     * Chờ cho lần tải lịch hiện tại xong.
     *
     * Dòng tóm tắt chỉ được vẽ khi đã tải xong, nên nó là dấu hiệu đáng tin hơn là chờ
     * lưới: đổi tháng hay đổi chế độ xem đều gọi lại máy chủ, mà lưới cũ vẫn còn trên màn
     * hình một nhịp trước khi bị thay.
     */
    public TrangQuanTriLich choTaiXong() {
        choHien(TOM_TAT);
        return this;
    }

    /** Kiểm tra nhanh, không chờ. Dùng cho trường hợp đúng ra là không được thấy lưới. */
    public boolean coLuoiThang() {
        return dangCo(LUOI);
    }

    /** Chờ lưới hiện rồi mới trả lời, dùng cho các trường hợp đúng ra phải thấy lưới. */
    public boolean choLuoiThang() {
        choHien(LUOI);
        return true;
    }

    public String tieuDeThang() {
        return choHien(TIEU_DE).getText().trim();
    }

    public String tomTat() {
        return choHien(TOM_TAT).getText().trim();
    }

    public int soO() {
        choHien(LUOI);
        return driver.findElements(O_NGAY).size();
    }

    public TrangQuanTriLich thangTruoc() {
        choTaiXong();
        bam(NUT_THANG_TRUOC);
        return this;
    }

    public TrangQuanTriLich thangSau() {
        choTaiXong();
        bam(NUT_THANG_SAU);
        return this;
    }

    /*
     * Chuyển lưới tới tháng chứa ngày cho trước.
     *
     * Kịch bản tạo đơn vào ngày trống gần nhất, mà ngày đó có thể rơi sang tháng sau, nên
     * không thể giả định lưới đang mở đúng tháng cần xem.
     */
    public TrangQuanTriLich moThangCua(java.time.LocalDate ngay) {
        choTaiXong();
        String can = "Tháng " + ngay.getMonthValue() + " năm " + ngay.getYear();
        for (int lan = 0; lan < 14 && !tieuDeThang().equals(can); lan++) {
            thangSau();
        }
        return this;
    }

    /** Số ô trên lưới đang ghi là còn trống. */
    public long soOTrong() {
        choHien(LUOI);
        return driver.findElements(O_NGAY).stream()
                .filter(o -> o.getText().contains("còn trống"))
                .count();
    }

    public TrangQuanTriLich xemNgay() {
        choTaiXong();
        bam(NUT_XEM_NGAY);
        return this;
    }

    public TrangQuanTriLich xemThang() {
        choTaiXong();
        bam(NUT_XEM_THANG);
        return this;
    }

    /** Đặt ngày cần xem ở chế độ xem ngày. Ô nhập kiểu date nên truyền dạng yyyy-MM-dd. */
    public TrangQuanTriLich chonNgay(String ngayIso) {
        /*
         * Ô nhập kiểu date hiển thị theo locale của trình duyệt, gõ chuỗi ISO vào có thể
         * không ăn. Đặt thẳng giá trị rồi báo cho React biết là cách chắc chắn.
         */
        datGiaTriNgay(O_CHON_NGAY, ngayIso);
        choTaiXong();
        return this;
    }

    /** Bấm vào ô một ngày trên lưới tháng, giao diện chuyển sang xem ngày đó. */
    public TrangQuanTriLich bamO(String ngayIso) {
        choHien(LUOI);
        bam(By.cssSelector("[data-test='calendar-cell'][data-ngay='" + ngayIso + "']"));
        choTaiXong();
        return this;
    }

    /** Chữ hiển thị trong ô một ngày, dùng để kiểm ngày trống có ghi rõ là trống không. */
    public Optional<String> chuTrongO(String ngayIso) {
        List<WebElement> o = driver.findElements(
                By.cssSelector("[data-test='calendar-cell'][data-ngay='" + ngayIso + "']"));
        return o.isEmpty() ? Optional.empty() : Optional.of(o.get(0).getText().trim());
    }

    /** Có bảng chi tiết ngày hay không. Chờ tải xong trước để không đọc nhầm lúc đang tải. */
    /*
     * Đặt giá trị cho ô nhập kiểu date.
     *
     * React theo dõi ô nhập qua bộ đặt giá trị gốc của phần tử, nên gán thẳng value rồi
     * phát sự kiện input mới làm React nhận ra. Gõ phím thì phụ thuộc định dạng ngày của
     * locale trình duyệt, chạy máy khác là hỏng.
     */
    private void datGiaTriNgay(By locator, String ngayIso) {
        WebElement o = choHien(locator);
        chayJs("const o = arguments[0], v = arguments[1];"
                + "const dat = Object.getOwnPropertyDescriptor("
                + "  window.HTMLInputElement.prototype, 'value').set;"
                + "dat.call(o, v);"
                + "o.dispatchEvent(new Event('input', { bubbles: true }));"
                + "o.dispatchEvent(new Event('change', { bubbles: true }));", o, ngayIso);
    }

    public boolean coBangNgay() {
        choTaiXong();
        return dangCo(BANG_NGAY);
    }

    public List<DongLich> cacDongNgay() {
        choHien(BANG_NGAY);
        return driver.findElements(DONG_NGAY).stream().map(dong -> {
            List<WebElement> o = dong.findElements(O_TRONG_DONG);
            return new DongLich(o.get(0).getText().trim(), o.get(1).getText().trim(),
                    o.get(2).getText().trim(), o.get(3).getText().trim(),
                    o.get(4).getText().trim(), o.get(5).getText().trim());
        }).toList();
    }
}
