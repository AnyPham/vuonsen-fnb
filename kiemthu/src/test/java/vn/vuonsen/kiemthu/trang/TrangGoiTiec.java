package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang bảng giá gói tiệc: /goi-tiec. */
public class TrangGoiTiec extends TrangCoSo {

    public static final String DUONG_DAN = "/goi-tiec";

    /** Giá trị của ô thả xuống thời gian, khớp hằng số bên giao diện. */
    public static final String NUA_NGAY = "4";
    public static final String TRON_NGAY = "8";
    public static final String MOI_THOI_GIAN = "";

    private static final By O_GIA_TOI_DA = dt("filter-package-max-price");
    private static final By O_SO_MON = dt("filter-package-min-dishes");
    private static final By O_THOI_GIAN = dt("filter-package-hours");
    private static final By NUT_BO_LOC = dt("filter-package-clear");
    private static final By TEN_GOI = dt("package-name");
    private static final By THE_GOI = dt("package-card");
    private static final By NHAN_NOI_BAT = dt("package-featured");
    private static final By DEM_KET_QUA = dt("package-count");
    private static final By KHOI_RONG = dt("empty");

    public TrangGoiTiec(WebDriver driver) {
        super(driver);
    }

    public TrangGoiTiec mo() {
        moDuongDan(DUONG_DAN);
        choHien(TEN_GOI);
        return this;
    }

    public TrangGoiTiec locGiaToiDa(String gia) {
        nhap(O_GIA_TOI_DA, gia);
        return this;
    }

    public TrangGoiTiec locSoMonToiThieu(String soMon) {
        nhap(O_SO_MON, soMon);
        return this;
    }

    /** Lọc theo thời gian dùng không gian: NUA_NGAY, TRON_NGAY hoặc MOI_THOI_GIAN để bỏ lọc. */
    public TrangGoiTiec locThoiGian(String soGio) {
        chon(O_THOI_GIAN, soGio);
        return this;
    }

    /*
     * Bấm nút Bỏ lọc.
     *
     * Khác trang không gian ở chỗ trang này có nút thật, không phải xóa tay từng ô. Nút chỉ
     * hiện khi đang có điều kiện lọc, nên gọi lúc chưa lọc gì thì sẽ hết thời gian chờ.
     */
    public TrangGoiTiec boLoc() {
        bam(NUT_BO_LOC);
        return this;
    }

    public boolean dangCoNutBoLoc() {
        return dangCo(NUT_BO_LOC);
    }

    /** Tên các gói đang hiện, đọc khi danh sách đã tải xong kết quả lọc. */
    public List<String> tenGoiDangHien() {
        return docDanhSachOnDinh(TEN_GOI);
    }

    /*
     * Mã các gói đang hiện, đọc từ thuộc tính data-code trên thẻ.
     *
     * Dùng mã thay cho tên khi kịch bản không quan tâm ngôn ngữ đang hiển thị: tên gói đổi
     * theo tiếng Việt hay tiếng Anh, còn mã thì không. Chờ danh sách ổn định trước đã, rồi
     * mới đọc thuộc tính, nếu không sẽ đọc trúng lúc danh sách đang vẽ lại.
     */
    public List<String> maGoiDangHien() {
        tenGoiDangHien();
        return driver.findElements(THE_GOI).stream()
                .map(the -> the.getAttribute("data-code"))
                .toList();
    }

    public boolean dangHienKhoiRong() {
        tenGoiDangHien();
        return dangCo(KHOI_RONG);
    }

    /** Chữ của dòng đếm kết quả, chuỗi rỗng khi dòng này không hiện. */
    public String chuDemKetQua() {
        tenGoiDangHien();
        return dangCo(DEM_KET_QUA) ? chuCua(DEM_KET_QUA) : "";
    }

    /** Số gói đang mang nhãn "Được chọn nhiều nhất". */
    public int soNhanNoiBat() {
        tenGoiDangHien();
        return driver.findElements(NHAN_NOI_BAT).size();
    }

    /** Nhãn của ô lọc giá, dùng để kiểm giao diện đã đổi sang ngôn ngữ khác. */
    public String nhanOLocGia() {
        return chuCua(By.cssSelector("label[for='f-pkg-price']"));
    }
}
