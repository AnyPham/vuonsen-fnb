package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.trang.TrangDanhGia;
import vn.vuonsen.kiemthu.trang.TrangQuanTriDanhGia;
import vn.vuonsen.kiemthu.trang.TrangQuanTriDon;
import vn.vuonsen.kiemthu.trang.TrangQuanTriThucDon;
import vn.vuonsen.kiemthu.trang.TrangThucDon;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 13 — Quản trị đơn, ứng với TC-ADMIN-01 đến TC-ADMIN-08.
 *
 * Chuỗi trạng thái: Chờ xác nhận sang Đã xác nhận hoặc Đã hủy; Đã xác nhận sang Đã hoàn thành
 * hoặc Đã hủy; hai trạng thái cuối không chuyển đi đâu nữa. Đơn cần thao tác được dựng sẵn qua
 * API, mỗi đơn một ngày riêng để không đè lịch nhau.
 */
@DisplayName("Luồng 13 — Quản trị đơn")
class KiemThuQuanTri extends KiemThuCoSo {

    private static Map<String, Object> donMoi(LocalDate ngay) {
        return GoiApi.taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê", ngay, "EVENING", 200);
    }

    private TrangQuanTriDon moQuanTriDon(String maDon) {
        LuongDangNhap.quanTri(driver());
        return new TrangQuanTriDon(driver()).mo().timKiem(maDon);
    }

    @Test
    @DisplayName("TC-ADMIN-01 Xem danh sách đơn đặt tiệc")
    void tcAdmin01_xemDanhSach() {
        String maDon = (String) donMoi(GoiApi.ngayChuaDung()).get("code");

        TrangQuanTriDon trang = moQuanTriDon(maDon);

        assertThat(trang.maCacDonDangHien()).contains(maDon);
    }

    @Test
    @DisplayName("TC-ADMIN-02 Lọc đơn theo trạng thái chờ xác nhận")
    void tcAdmin02_locTheoTrangThai() {
        String maDon = (String) donMoi(GoiApi.ngayChuaDung()).get("code");
        LuongDangNhap.quanTri(driver());

        TrangQuanTriDon trang = new TrangQuanTriDon(driver()).mo().locTrangThai("PENDING");

        assertThat(trang.maCacDonDangHien()).contains(maDon);
        assertThat(trang.trangThaiCacDonDangHien()).isNotEmpty().allMatch("Chờ xác nhận"::equals);
    }

    @Test
    @DisplayName("TC-ADMIN-03 Xác nhận đơn")
    void tcAdmin03_xacNhanDon() {
        String maDon = (String) donMoi(GoiApi.ngayChuaDung()).get("code");
        TrangQuanTriDon trang = moQuanTriDon(maDon);

        trang.chuyenTrangThai(maDon, "CONFIRMED");

        assertThat(trang.trangThaiCuaDon(maDon)).isEqualTo("Đã xác nhận");
    }

    @Test
    @DisplayName("TC-ADMIN-04 Hoàn thành đơn đã xác nhận")
    void tcAdmin04_hoanThanhDon() {
        Map<String, Object> don = donMoi(GoiApi.ngayChuaDung());
        GoiApi.doiTrangThaiDonTiec(GoiApi.so(don, "id"), "CONFIRMED");
        String maDon = (String) don.get("code");
        TrangQuanTriDon trang = moQuanTriDon(maDon);

        trang.chuyenTrangThai(maDon, "COMPLETED");

        assertThat(trang.trangThaiCuaDon(maDon)).isEqualTo("Đã hoàn thành");
    }

    @Test
    @DisplayName("TC-ADMIN-05 Đơn đã hủy không còn thao tác chuyển trạng thái nào")
    void tcAdmin05_donDaHuyKhongChuyenDuoc() {
        Map<String, Object> don = donMoi(GoiApi.ngayChuaDung());
        GoiApi.doiTrangThaiDonTiec(GoiApi.so(don, "id"), "CANCELLED");
        String maDon = (String) don.get("code");

        TrangQuanTriDon trang = moQuanTriDon(maDon);

        assertThat(trang.trangThaiCuaDon(maDon)).isEqualTo("Đã hủy");
        assertThat(trang.thaoTacCuaDon(maDon)).isEmpty();
    }

    @Test
    @DisplayName("TC-ADMIN-06 Đơn đã xác nhận thì chiếm chỗ, đơn chờ xác nhận thì không")
    void tcAdmin06_donXacNhanChiemCho() {
        LocalDate ngay = GoiApi.ngayChuaDung();
        String maDon = (String) donMoi(ngay).get("code");
        assertThat(GoiApi.thuTaoDonTiec("sanh-sen-vang", "Gói Đồng Quê", ngay, "EVENING", 200).ma())
                .as("Đơn chờ xác nhận chưa chiếm chỗ nên vẫn đặt trùng buổi được").isEqualTo(201);

        moQuanTriDon(maDon).chuyenTrangThai(maDon, "CONFIRMED");

        GoiApi.KetQua datTrung = GoiApi.thuTaoDonTiec("sanh-sen-vang", "Gói Đồng Quê", ngay, "EVENING", 200);
        assertThat(datTrung.ma()).as("Đã xác nhận thì buổi đó bị chiếm").isEqualTo(400);
        assertThat(datTrung.than()).contains("đã có tiệc");
    }

    @Test
    @DisplayName("TC-ADMIN-07 Duyệt đánh giá thì đánh giá hiện công khai")
    void tcAdmin07_duyetDanhGia() {
        String maDon = (String) GoiApi.taoDonTiecDaHoanThanh().get("code");
        String noiDung = "Đánh giá chờ duyệt " + System.nanoTime();
        GoiApi.guiDanhGia(maDon, "Khách Được Duyệt", 5, noiDung);
        LuongDangNhap.quanTri(driver());

        new TrangQuanTriDanhGia(driver()).mo().xemChoDuyet().duyet(noiDung);

        assertThat(new TrangDanhGia(driver()).mo().noiDungDanhGiaCongKhai()).contains(noiDung);
    }

    @Test
    @DisplayName("TC-ADMIN-08 Quản trị thêm món mới thì món hiện ở thực đơn công khai")
    void tcAdmin08_themMonMoi() {
        String tenMon = "Chè kiểm thử " + System.nanoTime();
        LuongDangNhap.quanTri(driver());

        new TrangQuanTriThucDon(driver()).mo().themMon("Tráng miệng", tenMon, 45_000L, "Món thêm từ kiểm thử");

        TrangThucDon thucDon = new TrangThucDon(driver()).mo().chonDanhMuc("trangmieng");
        assertThat(thucDon.tenMonDangHien()).contains(tenMon);
    }
}
