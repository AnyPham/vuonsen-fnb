package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongBaoGiaTiec;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.thanhphan.BangTamTinh;
import vn.vuonsen.kiemthu.trang.TrangDangNhap;
import vn.vuonsen.kiemthu.trang.TrangQuanTriNgayLe;
import vn.vuonsen.kiemthu.trang.TrangTraCuuMon;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 15 — Quản trị ngày lễ, ứng với TC-HADM-01 đến TC-HADM-12.
 *
 * Trang /quan-tri/ngay-le chỉ dành cho quản trị: thêm, sửa mức giảm, bật tắt, xóa dịp lễ. Mức
 * giảm chỉ nhận từ 10% đến 20%, ngày kết thúc không được trước ngày bắt đầu. Đơn đã đặt giữ
 * nguyên số tiền đã chốt dù dịp lễ bị sửa hay xóa.
 *
 * Ngày trong bảng hiển thị theo định dạng của trình duyệt, có thể có hoặc không có số 0 đứng
 * đầu, nên kịch bản bỏ số 0 đầu trước khi so.
 */
@DisplayName("Luồng 15 — Quản trị ngày lễ")
class KiemThuQuanTriNgayLe extends KiemThuCoSo {

    @BeforeEach
    void donDipLeTruoc() {
        GoiApi.donDipLeKiemThu();
    }

    @AfterEach
    void donDipLeSau() {
        GoiApi.donDipLeKiemThu();
    }

    private static String ten(String moTa) {
        return GoiApi.TIEN_TO_DIP_LE + moTa + " " + Math.floorMod(System.nanoTime(), 1_000_000L);
    }

    private static LocalDate sau(int soNgay) {
        return LocalDate.now().plusDays(soNgay);
    }

    // "26/01/2028 – 28/01/2028" thành "26/1/2028 – 28/1/2028"
    private static String boSoKhongDau(String chu) {
        return chu.replaceAll("\\b0(\\d)", "$1");
    }

    private static String ngay(LocalDate d) {
        return d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear();
    }

    private TrangQuanTriNgayLe moTrangQuanTri() {
        LuongDangNhap.quanTri(driver());
        return new TrangQuanTriNgayLe(driver()).mo();
    }

    private static boolean coDipLeTen(String tenDip) {
        return GoiApi.danhSachDipLe().stream().anyMatch(h -> tenDip.equals(h.get("name")));
    }

    @Test
    @DisplayName("TC-HADM-01 Quản trị xem danh sách dịp lễ, có đủ dịp lễ mẫu năm 2028")
    void tcHadm01_xemDanhSach() {
        var ds = moTrangQuanTri().danhSach();

        assertThat(ds).anySatisfy(d -> {
            assertThat(d.ten()).isEqualTo("Tết Nguyên Đán");
            assertThat(boSoKhongDau(d.thoiGian())).isEqualTo("26/1/2028 – 28/1/2028");
            assertThat(d.mucGiam()).isEqualTo("20%");
        });
        assertThat(ds).anySatisfy(d -> {
            assertThat(d.ten()).isEqualTo("Giỗ Tổ Hùng Vương");
            assertThat(boSoKhongDau(d.thoiGian())).isEqualTo("4/4/2028");
            assertThat(d.mucGiam()).isEqualTo("10%");
        });
    }

    @Test
    @DisplayName("TC-HADM-02 Thêm dịp lễ hợp lệ thì dịp hiện trong bảng với mức giảm và trạng thái Sắp tới")
    void tcHadm02_themDipLe() {
        LocalDate tu = GoiApi.dauKhoangNgayThuong(sau(40), 2);
        String tenDip = ten("Lễ thêm mới");

        TrangQuanTriNgayLe trang = moTrangQuanTri().themDipLe(tenDip, tu, tu.plusDays(1), 12);

        assertThat(trang.dongCua(tenDip)).hasValueSatisfying(d -> {
            assertThat(boSoKhongDau(d.thoiGian())).isEqualTo(ngay(tu) + " – " + ngay(tu.plusDays(1)));
            assertThat(d.mucGiam()).isEqualTo("12%");
            assertThat(d.trangThai()).isEqualTo("Sắp tới");
        });
    }

    @Test
    @DisplayName("TC-HADM-03 Dịp lễ vừa thêm được áp dụng ngay khi khách báo giá tiệc")
    void tcHadm03_apDungNgay() {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(sau(20), 1);
        String tenDip = ten("Lễ áp dụng ngay");
        moTrangQuanTri().themDipLe(tenDip, ngayLe, ngayLe, 15);

        BangTamTinh.SoLieu s = LuongBaoGiaTiec.tamTinhTiecChuan(driver(), ngayLe);

        assertThat(s.giamGia()).as("15% của 93.000.000").isEqualTo(13_950_000L);
        assertThat(s.quyTac()).contains("Giảm 15% dịp " + tenDip);
    }

    @Test
    @DisplayName("TC-HADM-04 Mức giảm trên 20% bị chặn, dịp lễ không được lưu")
    void tcHadm04_mucTren20() {
        LocalDate tu = sau(40);
        String tenDip = ten("Lễ quá mức");

        TrangQuanTriNgayLe trang = moTrangQuanTri().moFormThem().dienForm(tenDip, tu, tu, "25").bamLuu();

        assertThat(trang.loiTrinhDuyetOMucGiam()).isNotBlank();
        assertThat(trang.dangMoForm()).isTrue();
        assertThat(coDipLeTen(tenDip)).isFalse();
    }

    @Test
    @DisplayName("TC-HADM-05 Mức giảm dưới 10% bị chặn, dịp lễ không được lưu")
    void tcHadm05_mucDuoi10() {
        LocalDate tu = sau(40);
        String tenDip = ten("Lễ dưới mức");

        TrangQuanTriNgayLe trang = moTrangQuanTri().moFormThem().dienForm(tenDip, tu, tu, "5").bamLuu();

        assertThat(trang.loiTrinhDuyetOMucGiam()).isNotBlank();
        assertThat(coDipLeTen(tenDip)).isFalse();
    }

    @Test
    @DisplayName("TC-HADM-06 Ngày kết thúc trước ngày bắt đầu bị chặn, dịp lễ không được lưu")
    void tcHadm06_ngayNguoc() {
        LocalDate tu = sau(42);
        String tenDip = ten("Lễ ngày ngược");

        TrangQuanTriNgayLe trang = moTrangQuanTri().moFormThem()
                .dienForm(tenDip, tu, tu.minusDays(2), "15").bamLuu();

        assertThat(trang.loiTrinhDuyetONgayKetThuc()).isNotBlank();
        assertThat(coDipLeTen(tenDip)).isFalse();
    }

    @Test
    @DisplayName("TC-HADM-07 Sửa mức giảm thì bảng và dữ liệu cập nhật mức mới")
    void tcHadm07_suaMucGiam() {
        LocalDate tu = sau(40);
        String tenDip = ten("Lễ sửa mức");
        GoiApi.taoDipLe(tenDip, tu, tu, 0.10, true);

        TrangQuanTriNgayLe trang = moTrangQuanTri().suaDipLe(tenDip, 18, true);

        assertThat(trang.dongCua(tenDip)).hasValueSatisfying(d -> assertThat(d.mucGiam()).isEqualTo("18%"));
        Map<String, Object> dip = GoiApi.danhSachDipLe().stream()
                .filter(h -> tenDip.equals(h.get("name"))).findFirst().orElseThrow();
        assertThat(((Number) dip.get("discountRate")).doubleValue()).isEqualTo(0.18);
    }

    @Test
    @DisplayName("TC-HADM-08 Tắt Đang áp dụng thì dịp chuyển sang Đã tắt và khách không còn được giảm")
    void tcHadm08_tatDipLe() {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(sau(20), 1);
        String tenDip = ten("Lễ tắt");
        GoiApi.taoDipLe(tenDip, ngayLe, ngayLe, 0.15, true);

        TrangQuanTriNgayLe trang = moTrangQuanTri().suaDipLe(tenDip, 15, false);
        assertThat(trang.dongCua(tenDip)).hasValueSatisfying(d -> assertThat(d.trangThai()).isEqualTo("Đã tắt"));

        BangTamTinh.SoLieu s = LuongBaoGiaTiec.tamTinhTiecChuan(driver(), ngayLe);
        assertThat(s.giamGia()).isNull();
        assertThat(s.tong()).isEqualTo(100_440_000L);
    }

    @Test
    @DisplayName("TC-HADM-09 Xóa dịp lễ thì dịp biến mất khỏi bảng và dữ liệu")
    void tcHadm09_xoaDipLe() {
        LocalDate tu = sau(40);
        String tenDip = ten("Lễ xóa");
        GoiApi.taoDipLe(tenDip, tu, tu, 0.10, true);

        TrangQuanTriNgayLe trang = moTrangQuanTri().xoaDipLe(tenDip);

        assertThat(trang.dongCua(tenDip)).isEmpty();
        assertThat(coDipLeTen(tenDip)).isFalse();
    }

    @Test
    @DisplayName("TC-HADM-10 Đơn đặt món đã gửi giữ nguyên tiền giảm sau khi xóa dịp lễ")
    void tcHadm10_donCuGiuTienGiam() {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(sau(1), 1);
        String tenDip = ten("Lễ đơn cũ");
        GoiApi.taoDipLe(tenDip, ngayLe, ngayLe, 0.10, true);
        long idCombo = GoiApi.so(GoiApi.monTheoTen("Combo nướng than hoa"), "id");
        // Combo 690.000, giảm 69.000 còn 621.000, miễn phí giao, VAT 49.680, tổng 670.680
        String maDon = (String) GoiApi.taoDonMonGiao(idCombo, 1, ngayLe.atTime(12, 0)).get("code");

        moTrangQuanTri().xoaDipLe(tenDip);
        TrangTraCuuMon traCuu = new TrangTraCuuMon(driver()).mo().traCuu(maDon);

        assertThat(traCuu.giamGia()).isEqualTo(69_000L);
        assertThat(traCuu.tongCong()).isEqualTo(670_680L);
    }

    @Test
    @DisplayName("TC-HADM-11 Tài khoản khách hàng không vào được trang quản trị ngày lễ")
    void tcHadm11_khachHangBiChan() {
        LuongDangNhap.khachMoiDaDangNhap(driver());

        TrangQuanTriNgayLe trang = new TrangQuanTriNgayLe(driver()).mo();

        assertThat(trang.dangHienKhongDuQuyen()).isTrue();
    }

    @Test
    @DisplayName("TC-HADM-12 Khách chưa đăng nhập bị chuyển sang trang đăng nhập")
    void tcHadm12_khachVangLaiBiChan() {
        TrangQuanTriNgayLe trang = new TrangQuanTriNgayLe(driver()).mo();

        assertThat(trang.coChuyenToi(TrangDangNhap.DUONG_DAN)).isTrue();
    }
}
