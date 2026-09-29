package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.trang.TrangDanhGia;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 10 — Gửi đánh giá, ứng với TC-REV-01 đến TC-REV-07.
 *
 * Đánh giá bắt buộc kèm mã một đơn đã hoàn thành, và phải được quản trị duyệt mới hiện công
 * khai. Đơn đã hoàn thành được dựng sẵn qua API. Kịch bản gửi với tư cách khách vãng lai,
 * đúng như trang đánh giá mô tả: chỉ cần mã đơn, không yêu cầu đăng nhập.
 */
@DisplayName("Luồng 10 — Gửi đánh giá")
class KiemThuDanhGia extends KiemThuCoSo {

    private static String maDonDaHoanThanh() {
        return (String) GoiApi.taoDonTiecDaHoanThanh().get("code");
    }

    @Test
    @DisplayName("TC-REV-01 Gửi đánh giá với mã đơn hợp lệ")
    void tcRev01_guiVoiMaDonHopLe() {
        TrangDanhGia trang = new TrangDanhGia(driver()).mo();

        trang.guiDanhGia(maDonDaHoanThanh(), "Khách Hài Lòng", 5, "Tiệc chu đáo " + System.nanoTime());

        assertThat(trang.thongBaoDaGui()).contains("duyệt");
    }

    @Test
    @DisplayName("TC-REV-02 Đánh giá mới chưa hiện công khai khi chưa được duyệt")
    void tcRev02_chuaHienCongKhai() {
        String noiDung = "Chưa duyệt thì chưa hiện " + System.nanoTime();
        TrangDanhGia trang = new TrangDanhGia(driver()).mo();
        trang.guiDanhGia(maDonDaHoanThanh(), "Khách Chờ Duyệt", 4, noiDung);
        trang.thongBaoDaGui();

        trang.taiLaiTrang();

        assertThat(trang.noiDungDanhGiaCongKhai()).doesNotContain(noiDung);
    }

    @Test
    @DisplayName("TC-REV-03 Mã đơn không tồn tại thì báo không tìm thấy")
    void tcRev03_maDonKhongTonTai() {
        TrangDanhGia trang = new TrangDanhGia(driver()).mo();

        trang.guiDanhGia("VS-00000000-9999", "Khách Lạ", 5, "Không có đơn này");

        assertThat(trang.thongBaoLoiChung()).containsIgnoringCase("không tìm thấy");
    }

    /* Ô mã đơn có required nên trình duyệt chặn ngay, câu nhắc là của trình duyệt. */
    @Test
    @DisplayName("TC-REV-04 Bỏ trống mã đơn")
    void tcRev04_boTrongMaDon() {
        TrangDanhGia trang = new TrangDanhGia(driver()).mo();

        trang.guiDanhGia("", "Khách Quên Mã", 5, "Có nội dung");

        assertThat(trang.loiTrinhDuyetOMaDon()).isNotBlank();
    }

    @Test
    @DisplayName("TC-REV-05 Bỏ trống nội dung đánh giá")
    void tcRev05_boTrongNoiDung() {
        TrangDanhGia trang = new TrangDanhGia(driver()).mo();

        trang.guiDanhGia("VS-00000000-0001", "Khách Im Lặng", 5, "");

        assertThat(trang.loiTrinhDuyetONoiDung()).isNotBlank();
    }

    /* Đặc tả mô tả bấm ngôi sao; giao diện thực tế là ô chọn số sao. */
    @Test
    @DisplayName("TC-REV-06 Chọn số sao")
    void tcRev06_chonSoSao() {
        TrangDanhGia trang = new TrangDanhGia(driver()).mo();

        trang.chonSoSao(4);

        assertThat(trang.soSaoDangChon()).isEqualTo("4");
    }

    @Test
    @DisplayName("TC-REV-07 Danh sách đánh giá công khai hiển thị")
    void tcRev07_danhSachCongKhai() {
        TrangDanhGia trang = new TrangDanhGia(driver()).mo();

        assertThat(trang.noiDungDanhGiaCongKhai()).isNotEmpty();
    }
}
