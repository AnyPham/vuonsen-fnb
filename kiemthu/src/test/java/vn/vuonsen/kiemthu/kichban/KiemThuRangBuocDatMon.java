package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.trang.TrangChiTietMon;
import vn.vuonsen.kiemthu.trang.TrangDatMon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 9 — Ràng buộc khi đặt món, ứng với TC-DISHV-01 đến TC-DISHV-09.
 *
 * Ô địa chỉ và ô số khách có required và max=40, nên trình duyệt tự chặn gửi form ngay tại
 * chỗ, sớm hơn đặc tả mô tả (đặc tả ghi máy chủ báo lỗi). Các kịch bản đó đọc câu nhắc của
 * trình duyệt thay cho câu báo của máy chủ.
 */
@DisplayName("Luồng 9 — Ràng buộc khi đặt món")
class KiemThuRangBuocDatMon extends KiemThuCoSo {

    private static final String COM_CHAY = "Cơm cháy chà bông kho quẹt";
    private static final String COMBO_NUONG = "Combo nướng than hoa";

    private static LocalDateTime traMaiLucTrua() {
        return LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
    }

    private TrangDatMon gio(Map<String, Object> dong) {
        TrangDatMon trang = new TrangDatMon(driver()).moVoiGio(List.of(dong));
        trang.docTamTinh();
        return trang;
    }

    @Test
    @DisplayName("TC-DISHV-01 Đơn giao dưới mức tối thiểu bị từ chối")
    void tcDishv01_duoiMucToiThieu() {
        TrangDatMon trang = gio(GoiApi.dongGioMon("Bánh da lợn hấp lá dứa", 3)).chonGiaoTanNha();

        trang.dienThongTinGiao("Khách Kiểm Thử", "0901234567", "12 Đường Kiểm Thử", traMaiLucTrua()).guiDon();

        assertThat(trang.thongBaoLoiChung()).contains("tối thiểu 150.000đ");
    }

    @Test
    @DisplayName("TC-DISHV-02 Giao tận nhà bỏ trống địa chỉ")
    void tcDishv02_boTrongDiaChi() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 2)).chonGiaoTanNha();

        trang.dienThongTinGiao("Khách Kiểm Thử", "0901234567", "", traMaiLucTrua()).guiDon();

        assertThat(trang.loiTrinhDuyetODiaChi()).isNotBlank();
        assertThat(trang.duongDanHienTai()).isEqualTo(TrangDatMon.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-DISHV-03 Ăn tại chỗ bỏ trống số khách")
    void tcDishv03_boTrongSoKhach() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 2)).chonAnTaiCho();

        trang.dienThongTinAnTaiCho("Khách Kiểm Thử", "0901234567", "", traMaiLucTrua()).guiDon();

        assertThat(trang.loiTrinhDuyetOSoKhach()).isNotBlank();
        assertThat(trang.duongDanHienTai()).isEqualTo(TrangDatMon.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-DISHV-04 Ăn tại chỗ vượt 40 khách")
    void tcDishv04_vuotSoKhach() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COM_CHAY, 2)).chonAnTaiCho();

        trang.dienThongTinAnTaiCho("Khách Kiểm Thử", "0901234567", "50", traMaiLucTrua()).guiDon();

        assertThat(trang.loiTrinhDuyetOSoKhach()).isNotBlank();
        assertThat(trang.duongDanHienTai()).isEqualTo(TrangDatMon.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-DISHV-05 Món tính giá theo cân không thêm vào giỏ được")
    void tcDishv05_monTheoCan() {
        String slug = (String) GoiApi.monTheoTen("Heo quay giòn bì").get("slug");

        TrangChiTietMon chiTiet = new TrangChiTietMon(driver()).mo(slug);

        assertThat(chiTiet.coNutThemVaoGio()).isFalse();
        assertThat(chiTiet.coNutDatTiecThayThe()).isTrue();
    }

    @Test
    @DisplayName("TC-DISHV-06 Đặt sớm hơn thời gian chuẩn bị bị từ chối")
    void tcDishv06_datSatGio() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COMBO_NUONG, 1)).chonGiaoTanNha();

        trang.dienThongTinGiao("Khách Kiểm Thử", "0901234567", "12 Đường Kiểm Thử",
                LocalDateTime.now().plusMinutes(30)).guiDon();

        assertThat(trang.thongBaoLoiChung()).contains("sớm nhất nhận được lúc");
    }

    @Test
    @DisplayName("TC-DISHV-07 Đặt quá 30 ngày bị từ chối")
    void tcDishv07_quaXaNgay() {
        TrangDatMon trang = gio(GoiApi.dongGioMon(COMBO_NUONG, 1)).chonGiaoTanNha();

        trang.dienThongTinGiao("Khách Kiểm Thử", "0901234567", "12 Đường Kiểm Thử",
                LocalDateTime.now().plusDays(40).withHour(12).withMinute(0)).guiDon();

        assertThat(trang.thongBaoLoiChung()).contains("30 ngày");
    }

    @Test
    @DisplayName("TC-DISHV-08 Giỏ rỗng thì không gửi đơn được")
    void tcDishv08_gioRong() {
        TrangDatMon trang = new TrangDatMon(driver()).mo();

        assertThat(trang.dangHienGioTrong()).isTrue();
        assertThat(trang.coNutGuiDon()).isFalse();
    }

    @Test
    @DisplayName("TC-DISHV-09 Món đã ngừng phục vụ thì bị từ chối")
    void tcDishv09_monNgungPhucVu() {
        Map<String, Object> mon = GoiApi.taoMon("Món thử ngừng bán " + System.nanoTime(), "chinh", 200_000L);
        GoiApi.ngungBanMon(GoiApi.so(mon, "id"));

        TrangDatMon trang = new TrangDatMon(driver()).moVoiGio(List.of(GoiApi.dongGioMon(mon, 1)));

        assertThat(trang.thongBaoLoiChung()).contains("ngừng phục vụ");
    }
}
