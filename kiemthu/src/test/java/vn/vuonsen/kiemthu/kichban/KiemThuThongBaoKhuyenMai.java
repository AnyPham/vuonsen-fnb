package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.thanhphan.DaiKhuyenMai;
import vn.vuonsen.kiemthu.trang.TrangChu;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 18 — Thông báo khuyến mãi trên trang chủ, ứng với TC-PROMO-01 đến TC-PROMO-08.
 *
 * Dải ở đầu trang chủ báo dịp lễ đáng chú ý nhất: dịp đang diễn ra được ưu tiên, sau đó
 * tới dịp sắp tới gần nhất. Nhiều dịp chồng nhau thì lấy mức giảm cao nhất, đúng như cách
 * hệ thống tính tiền.
 *
 * Kịch bản tự tạo dịp lễ qua API rồi xóa sau mỗi ca, nên không phụ thuộc dữ liệu mẫu.
 * Mỗi ca đều xóa dấu đã đóng trong localStorage trước khi mở trang.
 */
@DisplayName("Luồng 18 — Thông báo khuyến mãi trang chủ")
class KiemThuThongBaoKhuyenMai extends KiemThuCoSo {

    @BeforeEach
    void donTruoc() {
        GoiApi.donDipLeKiemThu();
    }

    @AfterEach
    void donSau() {
        GoiApi.donDipLeKiemThu();
    }

    private static String ten(String moTa) {
        return GoiApi.TIEN_TO_DIP_LE + moTa + " " + Math.floorMod(System.nanoTime(), 1_000_000L);
    }

    /* Mở trang chủ ở trạng thái sạch: chưa đóng thông báo nào. */
    private DaiKhuyenMai moTrangChu() {
        new TrangChu(driver()).mo();
        DaiKhuyenMai dai = new DaiKhuyenMai(driver());
        dai.quenDaDong();
        new TrangChu(driver()).mo();
        return dai;
    }

    @Test
    @DisplayName("TC-PROMO-01 Có dịp lễ sắp tới thì trang chủ hiện dải khuyến mãi")
    void tcPromo01_coDipThiHien() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(LocalDate.now().plusDays(10), 2);
        String tenDip = ten("Lễ báo tin");
        GoiApi.taoDipLe(tenDip, ngay, ngay.plusDays(1), 0.18, true);

        DaiKhuyenMai dai = moTrangChu().choHien();

        assertThat(dai.dangHien()).isTrue();
        assertThat(dai.tenDip()).isEqualTo(tenDip);
        assertThat(dai.mucGiam()).contains("18");
    }

    @Test
    @DisplayName("TC-PROMO-02 Dịp đang diễn ra được báo là đang áp dụng")
    void tcPromo02_dipDangChay() {
        LocalDate homNay = LocalDate.now();
        String tenDip = ten("Lễ đang chạy");
        GoiApi.taoDipLe(tenDip, homNay, homNay.plusDays(2), 0.15, true);

        DaiKhuyenMai dai = moTrangChu().choHien();

        assertThat(dai.tenDip()).isEqualTo(tenDip);
        assertThat(dai.chuPhu()).contains("Đang áp dụng");
    }

    @Test
    @DisplayName("TC-PROMO-03 Dịp sắp tới hiện số ngày còn lại")
    void tcPromo03_demNgayConLai() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(LocalDate.now().plusDays(12), 2);
        GoiApi.taoDipLe(ten("Lễ đếm ngày"), ngay, ngay.plusDays(1), 0.12, true);

        DaiKhuyenMai dai = moTrangChu().choHien();

        assertThat(dai.chuPhu()).contains("Còn").contains("ngày");
    }

    @Test
    @DisplayName("TC-PROMO-04 Hai dịp cùng diễn ra thì báo dịp có mức giảm cao hơn")
    void tcPromo04_layMucCaoNhat() {
        LocalDate homNay = LocalDate.now();
        GoiApi.taoDipLe(ten("Lễ giảm ít"), homNay, homNay.plusDays(3), 0.10, true);
        String tenCao = ten("Lễ giảm nhiều");
        GoiApi.taoDipLe(tenCao, homNay, homNay.plusDays(3), 0.20, true);

        DaiKhuyenMai dai = moTrangChu().choHien();

        assertThat(dai.tenDip()).isEqualTo(tenCao);
        assertThat(dai.mucGiam()).contains("20");
    }

    @Test
    @DisplayName("TC-PROMO-05 Dịp đã tắt thì không báo")
    void tcPromo05_dipTatThiKhongBao() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(LocalDate.now().plusDays(14), 2);
        String tenDip = ten("Lễ đã tắt");
        GoiApi.taoDipLe(tenDip, ngay, ngay.plusDays(1), 0.20, false);

        DaiKhuyenMai dai = moTrangChu();

        /* Dữ liệu mẫu có thể còn dịp khác, nên chỉ cần chắc dịp đã tắt không được nêu tên. */
        if (dai.dangHien()) {
            assertThat(dai.tenDip()).isNotEqualTo(tenDip);
        }
    }

    @Test
    @DisplayName("TC-PROMO-06 Bấm Xem ưu đãi thì sang trang ưu đãi")
    void tcPromo06_bamSangTrangUuDai() {
        LocalDate homNay = LocalDate.now();
        GoiApi.taoDipLe(ten("Lễ xem ưu đãi"), homNay, homNay.plusDays(2), 0.15, true);

        DaiKhuyenMai dai = moTrangChu().choHien();
        dai.bamXemUuDai();

        assertThat(dai.coChuyenToi("/uu-dai")).isTrue();
    }

    @Test
    @DisplayName("TC-PROMO-07 Đóng rồi thì mở lại trang chủ không hiện lại nữa")
    void tcPromo07_dongThiNhoLai() {
        LocalDate homNay = LocalDate.now();
        GoiApi.taoDipLe(ten("Lễ đóng thử"), homNay, homNay.plusDays(2), 0.15, true);

        DaiKhuyenMai dai = moTrangChu().choHien();
        dai.dong();
        assertThat(dai.dangHien()).isFalse();

        new TrangChu(driver()).mo();
        assertThat(new DaiKhuyenMai(driver()).dangHien()).isFalse();
    }

    @Test
    @DisplayName("TC-PROMO-08 Dải có thuộc tính trợ năng để máy đọc màn hình nhận ra")
    void tcPromo08_troNang() {
        LocalDate homNay = LocalDate.now();
        GoiApi.taoDipLe(ten("Lễ trợ năng"), homNay, homNay.plusDays(2), 0.15, true);

        DaiKhuyenMai dai = moTrangChu().choHien();

        assertThat(dai.vaiTro()).isEqualTo("status");
        assertThat(dai.thongBaoSong()).isEqualTo("polite");
    }
}
