package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.trang.TrangChu;
import vn.vuonsen.kiemthu.trang.TrangHoSo;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 20 — Gợi ý thông minh trên trang chủ, ứng với TC-GOIY-01 đến TC-GOIY-05.
 *
 * Kiểm đúng hai nhánh cô hướng dẫn yêu cầu: khách chưa có thông tin thấy sảnh nổi bật,
 * khách đã có hồ sơ (sở thích tự khai hoặc lịch sử đặt tiệc) thấy sảnh hợp với mình.
 *
 * Mỗi ca dùng một tài khoản mới tinh để biết chắc hồ sơ gồm những gì. Dùng chung tài khoản
 * thì sở thích khai ở ca trước sẽ lọt sang ca sau, kết quả phụ thuộc thứ tự chạy.
 */
@DisplayName("Luồng 20 — Gợi ý thông minh trên trang chủ")
class KiemThuGoiYThongMinh extends KiemThuCoSo {

    private static final String CA_NHAN = "CA_NHAN";
    private static final String NOI_BAT = "NOI_BAT";

    private LuongDangNhap.TaiKhoan dangNhapKhachMoi() {
        LuongDangNhap.TaiKhoan khach = LuongDangNhap.taoKhachMoi();
        LuongDangNhap.dangNhap(driver(), khach.email(), khach.matKhau());
        return khach;
    }

    @Test
    @DisplayName("TC-GOIY-01 Khách chưa đăng nhập thấy ba sảnh nổi bật, mỗi sảnh ghi số tiệc đã đặt")
    void tcGoiY01_khachLaThaySanhNoiBat() {
        TrangChu trang = new TrangChu(driver()).mo();

        assertThat(trang.choKieuSanh(NOI_BAT)).as("kiểu đang hiện: " + trang.kieuSanhDangHien()).isTrue();
        assertThat(trang.tieuDeGoiY()).isEqualTo("Sảnh nổi bật");
        assertThat(trang.slugSanhGoiY()).hasSize(3);
        String dauTien = trang.slugSanhGoiY().get(0);
        assertThat(trang.lyDoCuaSanh(dauTien)).anyMatch(l -> l.startsWith("Đã có ") && l.endsWith("tiệc đặt sảnh này"));
        assertThat(trang.loiMoiKhai()).contains("Đăng nhập để nhận gợi ý");
    }

    @Test
    @DisplayName("TC-GOIY-02 Tài khoản mới chưa đặt, chưa khai gì vẫn là khách mới, được mời khai sở thích")
    void tcGoiY02_taiKhoanMoiVanNoiBat() {
        dangNhapKhachMoi();
        TrangChu trang = new TrangChu(driver()).mo();

        assertThat(trang.choKieuSanh(NOI_BAT)).as("kiểu đang hiện: " + trang.kieuSanhDangHien()).isTrue();
        assertThat(trang.loiMoiKhai()).contains("Khai sở thích trong hồ sơ");
    }

    @Test
    @DisplayName("TC-GOIY-03 Khai họp mặt gia đình 30 khách, tiết kiệm: trang chủ đổi sang sảnh hợp nhu cầu")
    void tcGoiY03_khaiSoThichThiGoiYRieng() {
        dangNhapKhachMoi();
        new TrangHoSo(driver()).mo().khaiSoThich(Set.of("FAMILY"), 30, null, "TIET_KIEM", false);

        TrangChu trang = new TrangChu(driver()).mo();

        assertThat(trang.choKieuSanh(CA_NHAN)).as("kiểu đang hiện: " + trang.kieuSanhDangHien()).isTrue();
        assertThat(trang.tieuDeGoiY()).isEqualTo("Những sảnh phù hợp với nhu cầu của bạn");
        assertThat(trang.canCu()).contains("sở thích bạn khai");
        assertThat(trang.slugSanhGoiY().subList(0, 2)).containsExactlyInAnyOrder("nha-ruong-go", "cum-choi-sen");
        assertThat(trang.lyDoCuaSanh("nha-ruong-go")).contains("Hợp với họp mặt gia đình bạn hay tổ chức");
    }

    @Test
    @DisplayName("TC-GOIY-04 Khai ưu tiên món chay: phần món gợi ý riêng và toàn món chay")
    void tcGoiY04_anChayThiGoiYMonChay() {
        dangNhapKhachMoi();
        new TrangHoSo(driver()).mo().khaiSoThich(Set.of(), null, null, null, true);

        TrangChu trang = new TrangChu(driver()).mo();

        assertThat(trang.choKieuMon(CA_NHAN)).isTrue();
        assertThat(trang.soMonGoiY()).isEqualTo(3);
        assertThat(trang.soMonChay()).isEqualTo(3);
    }

    @Test
    @DisplayName("TC-GOIY-05 Đặt một tiệc ở Nhà Rường Gỗ rồi về trang chủ: sảnh đó có nhãn đã đặt một lần")
    void tcGoiY05_lichSuDatTiec() {
        LuongDangNhap.TaiKhoan khach = dangNhapKhachMoi();
        Map<String, Object> don = GoiApi.taoDonTiec(khach.token(), "nha-ruong-go", "Gói Đồng Quê",
                GoiApi.ngayChuaDung(), "NOON", 40);
        try {
            TrangChu trang = new TrangChu(driver()).mo();

            assertThat(trang.choKieuSanh(CA_NHAN)).as("kiểu đang hiện: " + trang.kieuSanhDangHien()).isTrue();
            assertThat(trang.canCu()).contains("1 tiệc bạn đã đặt");
            assertThat(trang.slugSanhGoiY()).contains("nha-ruong-go");
            assertThat(trang.nhanDaDat("nha-ruong-go")).isEqualTo("Bạn đã đặt 1 lần");
        } finally {
            // Hủy để đơn thử không làm lệch số tiệc của sảnh nổi bật ở lần chạy sau
            GoiApi.doiTrangThaiDonTiec(GoiApi.so(don, "id"), "CANCELLED");
        }
    }
}
