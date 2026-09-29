package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.trang.TrangTraCuuTiec;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 7 — Tra cứu đơn đặt tiệc, ứng với TC-TRACK-01 đến TC-TRACK-04.
 * Đơn cần tra cứu được tạo sẵn qua API, để kịch bản chỉ kiểm tra đúng chức năng tra cứu.
 */
@DisplayName("Luồng 7 — Tra cứu đơn đặt tiệc")
class KiemThuTraCuuTiec extends KiemThuCoSo {

    private static String taoDonMoi() {
        return (String) GoiApi.taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê",
                GoiApi.ngayChuaDung(), "EVENING", 200).get("code");
    }

    @Test
    @DisplayName("TC-TRACK-01 Tra cứu bằng mã đơn hợp lệ")
    void tcTrack01_maDonHopLe() {
        String maDon = taoDonMoi();

        TrangTraCuuTiec trang = new TrangTraCuuTiec(driver()).mo().traCuu(maDon);

        assertThat(trang.maDonKetQua()).isEqualTo(maDon);
        assertThat(trang.trangThai()).isEqualTo("Chờ xác nhận");
        assertThat(trang.khongGian()).isEqualTo("Sảnh Sen Vàng");
    }

    @Test
    @DisplayName("TC-TRACK-02 Mã đơn không tồn tại thì báo không tìm thấy")
    void tcTrack02_maDonKhongTonTai() {
        TrangTraCuuTiec trang = new TrangTraCuuTiec(driver()).mo().traCuu("VS-00000000-9999");

        assertThat(trang.thongBaoLoiChung()).containsIgnoringCase("không tìm thấy");
    }

    @Test
    @DisplayName("TC-TRACK-03 Bỏ trống mã đơn thì hiện nhắc nhập mã")
    void tcTrack03_boTrongMaDon() {
        TrangTraCuuTiec trang = new TrangTraCuuTiec(driver()).mo().traCuu("");

        assertThat(trang.thongBaoLoiNeuCo()).as("Phải có câu nhắc nhập mã đơn").isNotBlank();
    }

    @Test
    @DisplayName("TC-TRACK-04 Mã đơn có khoảng trắng thừa vẫn tìm ra đơn")
    void tcTrack04_khoangTrangThua() {
        String maDon = taoDonMoi();

        TrangTraCuuTiec trang = new TrangTraCuuTiec(driver()).mo().traCuu("   " + maDon + "   ");

        assertThat(trang.maDonKetQua()).isEqualTo(maDon);
    }
}
