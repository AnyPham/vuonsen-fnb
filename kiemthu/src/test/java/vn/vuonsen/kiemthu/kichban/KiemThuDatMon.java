package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.coso.TrangCoSo;
import vn.vuonsen.kiemthu.trang.TrangChiTietMon;
import vn.vuonsen.kiemthu.trang.TrangDatMon;
import vn.vuonsen.kiemthu.trang.TrangTraCuuMon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 8 — Đặt món lẻ, ứng với TC-DISH-01 đến TC-DISH-12.
 *
 * Tham số: phí giao 30.000đ, miễn phí giao từ 500.000đ, đơn giao tối thiểu 150.000đ,
 * VAT 8% chỉ tính trên tiền món. Món mẫu: Cơm cháy chà bông kho quẹt 135.000đ,
 * Combo nướng than hoa 690.000đ, Bánh da lợn hấp lá dứa 40.000đ.
 */
@DisplayName("Luồng 8 — Đặt món lẻ")
class KiemThuDatMon extends KiemThuCoSo {

    private static final String COM_CHAY = "Cơm cháy chà bông kho quẹt";
    private static final String COMBO_NUONG = "Combo nướng than hoa";
    private static final String BANH_DA_LON = "Bánh da lợn hấp lá dứa";

    /*
     * Trưa của ngày thường gần nhất kể từ ngày mai. Tránh dịp lễ vì đơn nhận đúng dịp lễ được
     * giảm giá, còn các con số mong đợi trong luồng này tính theo giá ngày thường.
     */
    private static LocalDateTime traMaiLucTrua() {
        return GoiApi.ngayThuong(LocalDate.now().plusDays(1), 1).atTime(12, 0);
    }

    private TrangDatMon gio(Map<String, Object>... cacDong) {
        return new TrangDatMon(driver()).moVoiGio(List.of(cacDong));
    }

    @Test
    @DisplayName("TC-DISH-01 Thêm món vào giỏ từ trang chi tiết món")
    void tcDish01_themMonVaoGio() {
        String slug = (String) GoiApi.monTheoTen(COM_CHAY).get("slug");
        new TrangChiTietMon(driver()).mo(slug).themVaoGio();

        TrangDatMon trang = new TrangDatMon(driver()).mo();

        assertThat(trang.tenMonTrongGio()).containsExactly(COM_CHAY);
        assertThat(trang.soLuong(COM_CHAY)).isEqualTo(1);
        assertThat(trang.docTamTinh().tienMon()).isEqualTo(135_000L);
    }

    @Test
    @DisplayName("TC-DISH-02 Tăng số lượng thì thành tiền cập nhật")
    void tcDish02_tangSoLuong() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 1));
        long tongTruoc = trang.docTamTinh().tong();

        trang.tangSoLuong(COM_CHAY);

        assertThat(trang.soLuong(COM_CHAY)).isEqualTo(2);
        assertThat(trang.docTamTinhSauKhiDoi(tongTruoc).tienMon()).isEqualTo(270_000L);
    }

    @Test
    @DisplayName("TC-DISH-03 Xóa món khỏi giỏ")
    void tcDish03_xoaMon() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 1), GoiApi.dongGioMon(BANH_DA_LON, 1));
        long tongTruoc = trang.docTamTinh().tong();

        trang.boMon(BANH_DA_LON).choGioCon(1);

        assertThat(trang.tenMonTrongGio()).containsExactly(COM_CHAY);
        assertThat(trang.docTamTinhSauKhiDoi(tongTruoc).tienMon()).isEqualTo(135_000L);
    }

    @Test
    @DisplayName("TC-DISH-04 Giỏ hàng còn nguyên sau khi tải lại trang")
    void tcDish04_gioConSauKhiTaiLai() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 1), GoiApi.dongGioMon(BANH_DA_LON, 1));

        trang.taiLaiTrang();

        assertThat(trang.tenMonTrongGio()).containsExactlyInAnyOrder(COM_CHAY, BANH_DA_LON);
    }

    @Test
    @DisplayName("TC-DISH-05 Đơn giao dưới mức miễn phí thì tính phí giao, VAT không tính trên phí giao")
    void tcDish05_phiGiaoDuoiMucMienPhi() {
        TrangDatMon.TamTinh t = gio(GoiApi.dongGioMon(COM_CHAY, 2)).chonThoiDiemNhan(traMaiLucTrua())
                .chonGiaoTanNha().docTamTinhKhongGiamGia();

        assertThat(TrangCoSo.soTien(t.phiGiao())).isEqualTo(30_000L);
        assertThat(t.vat()).as("8% của 270.000").isEqualTo(21_600L);
        assertThat(t.tong()).isEqualTo(321_600L);
    }

    @Test
    @DisplayName("TC-DISH-06 Đơn đạt mức thì miễn phí giao")
    void tcDish06_mienPhiGiao() {
        TrangDatMon.TamTinh t = gio(GoiApi.dongGioMon(COMBO_NUONG, 1)).chonThoiDiemNhan(traMaiLucTrua())
                .chonGiaoTanNha().docTamTinhKhongGiamGia();

        assertThat(t.phiGiao()).isEqualTo("Miễn phí");
        assertThat(t.vat()).isEqualTo(55_200L);
        assertThat(t.tong()).isEqualTo(745_200L);
        assertThat(t.ghiChuGiao()).containsIgnoringCase("miễn phí");
    }

    @Test
    @DisplayName("TC-DISH-07 Gợi ý đặt thêm bao nhiêu để được miễn phí giao")
    void tcDish07_goiYDatThem() {
        TrangDatMon.TamTinh t = gio(GoiApi.dongGioMon(COM_CHAY, 2)).chonGiaoTanNha().docTamTinh();

        assertThat(t.ghiChuGiao()).as("500.000 - 270.000").contains("230.000");
    }

    @Test
    @DisplayName("TC-DISH-08 Chuyển sang ăn tại chỗ thì mất phí giao")
    void tcDish08_chuyenAnTaiCho() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 2)).chonGiaoTanNha();
        long tongKhiGiao = trang.docTamTinh().tong();

        trang.chonAnTaiCho();
        TrangDatMon.TamTinh t = trang.docTamTinhSauKhiDoi(tongKhiGiao);

        assertThat(TrangCoSo.soTien(t.phiGiao())).isZero();
        assertThat(t.tong()).isEqualTo(tongKhiGiao - 30_000L);
        assertThat(trang.dangCoOSoKhach()).isTrue();
        assertThat(trang.dangCoODiaChi()).isFalse();
    }

    @Test
    @DisplayName("TC-DISH-09 Gửi đơn giao tận nhà thành công")
    void tcDish09_guiDonGiaoTanNha() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COMBO_NUONG, 1)).chonGiaoTanNha();
        trang.docTamTinh();

        trang.dienThongTinGiao("Khách Kiểm Thử", "0901234567", "12 Đường Kiểm Thử", traMaiLucTrua()).guiDon();

        assertThat(trang.coChuyenToi(TrangTraCuuMon.DUONG_DAN)).isTrue();
        assertThat(new TrangTraCuuMon(driver()).maDonKetQua()).matches("DM-\\d{8}-\\d{4}");
    }

    @Test
    @DisplayName("TC-DISH-10 Gửi đơn ăn tại chỗ thành công, không có phí giao")
    void tcDish10_guiDonAnTaiCho() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 2)).chonAnTaiCho();
        trang.docTamTinh();

        trang.dienThongTinAnTaiCho("Khách Kiểm Thử", "0901234567", "4", traMaiLucTrua()).guiDon();

        assertThat(trang.coChuyenToi(TrangTraCuuMon.DUONG_DAN)).isTrue();
        assertThat(new TrangTraCuuMon(driver()).tongCong()).isEqualTo(291_600L);
    }

    @Test
    @DisplayName("TC-DISH-11 Tra cứu đơn đặt món bằng mã")
    void tcDish11_traCuuDonMon() {
        long idCombo = GoiApi.so(GoiApi.monTheoTen(COMBO_NUONG), "id");
        String maDon = (String) GoiApi.taoDonMonGiao(idCombo, 1).get("code");

        TrangTraCuuMon trang = new TrangTraCuuMon(driver()).mo().traCuu(maDon);

        assertThat(trang.maDonKetQua()).isEqualTo(maDon);
        assertThat(trang.cacDongMon()).containsExactly(COMBO_NUONG + " × 1");
        assertThat(trang.tongCong()).isEqualTo(745_200L);
    }

    @Test
    @DisplayName("TC-DISH-12 Giá trong đơn cũ không đổi khi giá món thay đổi")
    void tcDish12_giaDonCuKhongDoi() {
        String tenMon = "Món thử giá " + System.nanoTime();
        Map<String, Object> mon = GoiApi.taoMon(tenMon, "chinh", 200_000L);
        long idMon = GoiApi.so(mon, "id");
        String maDon = (String) GoiApi.taoDonMonGiao(idMon, 1).get("code");

        GoiApi.doiGiaMon(idMon, tenMon, "chinh", 250_000L);

        TrangTraCuuMon trang = new TrangTraCuuMon(driver()).mo().traCuu(maDon);
        assertThat(trang.thanhTienCuaMon(tenMon)).as("Giữ đúng giá lúc đặt").isEqualTo(200_000L);
    }
}
