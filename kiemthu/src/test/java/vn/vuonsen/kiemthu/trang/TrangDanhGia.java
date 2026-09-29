package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang đánh giá: xem đánh giá đã duyệt và gửi đánh giá mới, /danh-gia. */
public class TrangDanhGia extends TrangCoSo {

    public static final String DUONG_DAN = "/danh-gia";

    private static final By O_MA_DON = dt("review-booking-code");
    private static final By O_TEN = dt("review-name");
    private static final By O_SO_SAO = dt("review-rating");
    private static final By O_NOI_DUNG = dt("review-content");
    private static final By NUT_GUI = dt("submit-review");
    private static final By DA_GUI = dt("review-sent");
    private static final By NOI_DUNG_CONG_KHAI = dt("review-text");

    public TrangDanhGia(WebDriver driver) {
        super(driver);
    }

    public TrangDanhGia mo() {
        moDuongDan(DUONG_DAN);
        choHien(O_MA_DON);
        return this;
    }

    /** Điền rồi gửi đánh giá. Truyền chuỗi rỗng để thử trường hợp bỏ trống. */
    public TrangDanhGia guiDanhGia(String maDon, String tenHienThi, int soSao, String noiDung) {
        nhap(O_MA_DON, maDon);
        nhap(O_TEN, tenHienThi);
        chonSoSao(soSao);
        nhap(O_NOI_DUNG, noiDung);
        bam(NUT_GUI);
        return this;
    }

    /*
     * Chọn số sao.
     *
     * Đặc tả TC-REV-06 mô tả bấm vào ngôi sao, nhưng giao diện dùng ô chọn số sao. Phương thức
     * làm đúng thao tác giao diện thực tế cho phép.
     */
    public TrangDanhGia chonSoSao(int soSao) {
        chon(O_SO_SAO, String.valueOf(soSao));
        return this;
    }

    public String soSaoDangChon() {
        return new Select(choHien(O_SO_SAO)).getFirstSelectedOption().getDomAttribute("value");
    }

    public String thongBaoDaGui() {
        return chuCua(DA_GUI);
    }

    /** Nội dung các đánh giá đang hiện công khai, đọc khi danh sách đã tải xong. */
    public List<String> noiDungDanhGiaCongKhai() {
        return docDanhSachOnDinh(NOI_DUNG_CONG_KHAI);
    }

    /** Câu nhắc lỗi của chính trình duyệt ở ô mã đơn, rỗng nếu ô hợp lệ. */
    public String loiTrinhDuyetOMaDon() {
        return choHien(O_MA_DON).getDomProperty("validationMessage");
    }

    /** Câu nhắc lỗi của chính trình duyệt ở ô nội dung, rỗng nếu ô hợp lệ. */
    public String loiTrinhDuyetONoiDung() {
        return choHien(O_NOI_DUNG).getDomProperty("validationMessage");
    }
}
