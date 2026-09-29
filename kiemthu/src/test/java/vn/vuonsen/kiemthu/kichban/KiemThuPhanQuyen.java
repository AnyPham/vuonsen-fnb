package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.thanhphan.ThanhDieuHuong;
import vn.vuonsen.kiemthu.trang.TrangDangNhap;
import vn.vuonsen.kiemthu.trang.TrangDonCuaToi;
import vn.vuonsen.kiemthu.trang.TrangHoSo;
import vn.vuonsen.kiemthu.trang.TrangQuanTriDon;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 12 — Phân quyền, ứng với TC-PERM-01 đến TC-PERM-07.
 * Ba vai trò: khách vãng lai, khách có tài khoản, quản trị. Đây là luồng kiểm thử truy cập bị từ chối.
 */
@DisplayName("Luồng 12 — Phân quyền")
class KiemThuPhanQuyen extends KiemThuCoSo {

    @Test
    @DisplayName("TC-PERM-01 Khách chưa đăng nhập bị chặn khỏi trang hồ sơ")
    void tcPerm01_chanTrangHoSo() {
        TrangHoSo trang = new TrangHoSo(driver()).mo();

        assertThat(trang.coChuyenToi(TrangDangNhap.DUONG_DAN)).isTrue();
    }

    @Test
    @DisplayName("TC-PERM-02 Khách chưa đăng nhập bị chặn khỏi khu quản trị")
    void tcPerm02_chanKhuQuanTri() {
        TrangQuanTriDon trang = new TrangQuanTriDon(driver()).mo();

        assertThat(trang.coChuyenToi(TrangDangNhap.DUONG_DAN)).isTrue();
    }

    @Test
    @DisplayName("TC-PERM-03 Tài khoản khách hàng bị chặn khỏi khu quản trị")
    void tcPerm03_khachHangBiChan() {
        LuongDangNhap.khachMoiDaDangNhap(driver());

        TrangQuanTriDon trang = new TrangQuanTriDon(driver()).mo();

        assertThat(trang.dangHienKhongDuQuyen()).isTrue();
    }

    @Test
    @DisplayName("TC-PERM-04 Tài khoản quản trị vào được khu quản trị")
    void tcPerm04_quanTriVaoDuoc() {
        LuongDangNhap.quanTri(driver());

        TrangQuanTriDon trang = new TrangQuanTriDon(driver()).mo();
        trang.maCacDonDangHien();

        assertThat(trang.duongDanHienTai()).isEqualTo(TrangQuanTriDon.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-PERM-05 Khách hàng không thấy menu quản trị")
    void tcPerm05_khongThayMenuQuanTri() {
        ThanhDieuHuong thanhDieuHuong = LuongDangNhap.dangNhap(driver(),
                LuongDangNhap.taoKhachMoi().email(), "matkhau123");

        assertThat(thanhDieuHuong.coMenuQuanTri()).isFalse();
        assertThat(thanhDieuHuong.coMucDonCuaToi()).isTrue();
    }

    @Test
    @DisplayName("TC-PERM-06 Khách chỉ xem được đơn của chính mình")
    void tcPerm06_chiXemDonCuaMinh() {
        LuongDangNhap.TaiKhoan khachA = LuongDangNhap.taoKhachMoi();
        LuongDangNhap.TaiKhoan khachB = LuongDangNhap.taoKhachMoi();
        String donCuaA = (String) GoiApi.taoDonTiec(khachA.token(), "sanh-sen-vang", "Gói Đồng Quê",
                GoiApi.ngayChuaDung(), "EVENING", 200).get("code");
        String donCuaB = (String) GoiApi.taoDonTiec(khachB.token(), "sanh-sen-vang", "Gói Đồng Quê",
                GoiApi.ngayChuaDung(), "EVENING", 200).get("code");

        LuongDangNhap.dangNhap(driver(), khachA.email(), khachA.matKhau());
        TrangDonCuaToi trang = new TrangDonCuaToi(driver()).mo();

        assertThat(trang.maCacDon()).containsExactly(donCuaA).doesNotContain(donCuaB);
    }

    @Test
    @DisplayName("TC-PERM-07 Mất phiên đăng nhập thì bị đưa về trang đăng nhập")
    void tcPerm07_matPhien() {
        LuongDangNhap.khachMoiDaDangNhap(driver());
        TrangHoSo trang = new TrangHoSo(driver());

        trang.xoaLocalStorage();
        trang.mo();

        assertThat(trang.coChuyenToi(TrangDangNhap.DUONG_DAN)).isTrue();
    }
}
