package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang danh sách không gian: /khong-gian. */
public class TrangKhongGian extends TrangCoSo {

    public static final String DUONG_DAN = "/khong-gian";

    private static final By O_SO_KHACH = dt("filter-guests");
    private static final By O_LOAI = dt("filter-type");
    private static final By O_GIA_TOI_DA = dt("filter-max-price");
    private static final By TEN_KHONG_GIAN = dt("space-card-link");
    private static final By KHOI_RONG = dt("empty");

    public TrangKhongGian(WebDriver driver) {
        super(driver);
    }

    public TrangKhongGian mo() {
        moDuongDan(DUONG_DAN);
        choHien(TEN_KHONG_GIAN);
        return this;
    }

    public TrangKhongGian locSoKhach(String soKhach) {
        nhap(O_SO_KHACH, soKhach);
        return this;
    }

    /** Lọc theo mã loại không gian, ví dụ INDOOR, OUTDOOR. Truyền chuỗi rỗng để bỏ lọc. */
    public TrangKhongGian locLoai(String maLoai) {
        chon(O_LOAI, maLoai);
        return this;
    }

    public TrangKhongGian locGiaToiDa(String gia) {
        nhap(O_GIA_TOI_DA, gia);
        return this;
    }

    /*
     * Bỏ hết điều kiện lọc.
     *
     * Đặc tả TC-SPACE-07 ghi có nút xóa bộ lọc, nhưng giao diện không có nút này. Phương thức
     * làm đúng việc người dùng buộc phải làm thay: xóa tay từng ô.
     */
    public TrangKhongGian xoaBoLoc() {
        nhap(O_SO_KHACH, "");
        chon(O_LOAI, "");
        nhap(O_GIA_TOI_DA, "");
        return this;
    }

    /** Tên các không gian đang hiện, đọc khi danh sách đã tải xong kết quả lọc. */
    public List<String> tenKhongGianDangHien() {
        return docDanhSachOnDinh(TEN_KHONG_GIAN);
    }

    public boolean dangHienKhoiRong() {
        tenKhongGianDangHien();
        return dangCo(KHOI_RONG);
    }

    public TrangChiTietKhongGian moChiTiet(String tenKhongGian) {
        WebElement lienKet = choDanhSach(TEN_KHONG_GIAN).stream()
                .filter(p -> p.getText().trim().equals(tenKhongGian))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không thấy thẻ " + tenKhongGian));
        bam(lienKet);
        return new TrangChiTietKhongGian(driver);
    }
}
