package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongBaoGiaTiec;
import vn.vuonsen.kiemthu.thanhphan.BangTamTinh;
import vn.vuonsen.kiemthu.thanhphan.HopThoaiTroLy;
import vn.vuonsen.kiemthu.trang.TrangDatMon;
import vn.vuonsen.kiemthu.trang.TrangThucDon;
import vn.vuonsen.kiemthu.trang.TrangTraCuuMon;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 14 — Giảm giá ngày lễ phía khách, ứng với TC-HOL-01 đến TC-HOL-10.
 *
 * Quy tắc: ngày tổ chức tiệc hoặc ngày nhận món rơi vào dịp lễ đang bật thì giảm theo mức của
 * dịp đó. Tiệc giảm trên tiền ăn cộng phí thuê, đơn món giảm trên tiền món, VAT tính sau khi
 * giảm. Không cộng dồn với giảm đặt sớm 5%, lấy mức cao hơn. Mức miễn phí giao hàng xét trên
 * tiền món trước khi giảm.
 *
 * Dịp lễ trong kịch bản được tạo qua API vào những ngày không trùng dịp lễ mẫu nào, tên bắt đầu
 * bằng "KT " và bị xóa trước lẫn sau mỗi kịch bản, nên kết quả không phụ thuộc ngày chạy.
 *
 * Tiệc chuẩn chưa giảm: 93.000.000 trước thuế, tổng 100.440.000.
 * Món mẫu: Cơm cháy chà bông kho quẹt 135.000đ.
 */
@DisplayName("Luồng 14 — Giảm giá ngày lễ")
class KiemThuGiamGiaNgayLe extends KiemThuCoSo {

    private static final String COM_CHAY = "Cơm cháy chà bông kho quẹt";

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

    /*
     * Mở giỏ món, chọn hình thức nhận, đặt giờ nhận vào một ngày thường và chờ bảng về giá gốc.
     * Làm bước này trước rồi mới đổi sang dịp lễ, để lần đọc sau chắc chắn là số liệu mới.
     */
    @SafeVarargs
    private TrangDatMon gioNhanNgayThuong(LocalDate ngayThuong, boolean giaoTanNha, Map<String, Object>... cacDong) {
        TrangDatMon trang = new TrangDatMon(driver()).moVoiGio(List.of(cacDong));
        if (giaoTanNha) {
            trang.chonGiaoTanNha();
        } else {
            trang.chonAnTaiCho();
        }
        trang.chonThoiDiemNhan(ngayThuong.atTime(12, 0)).docTamTinhKhongGiamGia();
        return trang;
    }

    // ================= Đặt tiệc =================

    @Test
    @DisplayName("TC-HOL-01 Tiệc rơi vào dịp lễ khi chưa đủ ngày đặt sớm thì giảm theo mức dịp lễ")
    void tcHol01_tiecGiamTheoDipLe() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(sau(20), 3);
        String tenDip = ten("Lễ tiệc");
        GoiApi.taoDipLe(tenDip, ngay, ngay.plusDays(1), 0.15, true);

        BangTamTinh.SoLieu s = LuongBaoGiaTiec.tamTinhTiecChuan(driver(), ngay);

        assertThat(s.giamGia()).as("15% của 93.000.000").isEqualTo(13_950_000L);
        assertThat(s.vat()).as("8% của 79.050.000").isEqualTo(6_324_000L);
        assertThat(s.tong()).isEqualTo(85_374_000L);
        assertThat(s.coc()).as("Cọc 30% trên tổng đã giảm").isEqualTo(25_612_200L);
        assertThat(s.quyTac()).contains("Giảm 15% dịp " + tenDip).noneMatch(q -> q.contains("cộng dồn"));
    }

    @Test
    @DisplayName("TC-HOL-02 Tiệc vừa đặt sớm vừa trùng dịp lễ thì chỉ lấy mức cao hơn, không cộng dồn")
    void tcHol02_khongCongDonVoiDatSom() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(sau(70), 1);
        String tenDip = ten("Lễ đặt sớm");
        GoiApi.taoDipLe(tenDip, ngay, ngay, 0.20, true);

        BangTamTinh.SoLieu s = LuongBaoGiaTiec.tamTinhTiecChuan(driver(), ngay);

        assertThat(s.giamGia()).as("Chỉ 20%, không phải 20% cộng 5%").isEqualTo(18_600_000L);
        assertThat(s.vat()).isEqualTo(5_952_000L);
        assertThat(s.tong()).isEqualTo(80_352_000L);
        assertThat(s.quyTac())
                .contains("Giảm 20% dịp " + tenDip)
                .anyMatch(q -> q.contains("không cộng dồn"))
                .noneMatch(q -> q.contains("do đặt trước"));
    }

    @Test
    @DisplayName("TC-HOL-03 Ngày cuối của dịp lễ vẫn được giảm")
    void tcHol03_ngayCuoiVanGiam() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(sau(20), 3);
        GoiApi.taoDipLe(ten("Lễ hai ngày"), ngay, ngay.plusDays(1), 0.15, true);

        BangTamTinh.SoLieu s = LuongBaoGiaTiec.tamTinhTiecChuan(driver(), ngay.plusDays(1));

        assertThat(s.giamGia()).isEqualTo(13_950_000L);
    }

    @Test
    @DisplayName("TC-HOL-04 Ngày ngay sau dịp lễ không được giảm")
    void tcHol04_ngaySauDipLeKhongGiam() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(sau(20), 3);
        GoiApi.taoDipLe(ten("Lễ hai ngày"), ngay, ngay.plusDays(1), 0.15, true);

        BangTamTinh.SoLieu s = LuongBaoGiaTiec.tamTinhTiecChuan(driver(), ngay.plusDays(2));

        assertThat(s.giamGia()).as("Không có dòng giảm giá").isNull();
        assertThat(s.tong()).isEqualTo(100_440_000L);
    }

    @Test
    @DisplayName("TC-HOL-05 Dịp lễ đang tắt thì tiệc không được giảm")
    void tcHol05_dipLeTatKhongGiam() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(sau(20), 1);
        GoiApi.taoDipLe(ten("Lễ đã tắt"), ngay, ngay, 0.15, false);

        BangTamTinh.SoLieu s = LuongBaoGiaTiec.tamTinhTiecChuan(driver(), ngay);

        assertThat(s.giamGia()).isNull();
        assertThat(s.tong()).isEqualTo(100_440_000L);
    }

    // ================= Đặt món =================

    @Test
    @DisplayName("TC-HOL-06 Đặt món nhận vào dịp lễ thì bảng tạm tính có dòng giảm giá, VAT tính sau giảm")
    void tcHol06_monCoDongGiamGia() {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(sau(1), 2);
        String tenDip = ten("Lễ đặt món");
        GoiApi.taoDipLe(tenDip, ngayLe, ngayLe, 0.10, true);
        TrangDatMon trang = gioNhanNgayThuong(ngayLe.plusDays(1), false, GoiApi.dongGioMon(COM_CHAY, 2));

        TrangDatMon.TamTinh t = trang.chonThoiDiemNhan(ngayLe.atTime(12, 0)).docTamTinhCoGiamGia();

        assertThat(t.tienMon()).isEqualTo(270_000L);
        assertThat(t.giamGia()).as("10% của 270.000").isEqualTo(27_000L);
        assertThat(t.vat()).as("8% của 243.000").isEqualTo(19_440L);
        assertThat(t.tong()).isEqualTo(262_440L);
        assertThat(t.ghiChuGiam()).isEqualTo("Giảm 10% dịp " + tenDip + ".");
    }

    @Test
    @DisplayName("TC-HOL-07 Đơn giao đạt mức miễn phí giao trước khi giảm thì vẫn được miễn phí giao")
    void tcHol07_vanMienPhiGiao() {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(sau(1), 2);
        GoiApi.taoDipLe(ten("Lễ giao hàng"), ngayLe, ngayLe, 0.10, true);
        TrangDatMon trang = gioNhanNgayThuong(ngayLe.plusDays(1), true, GoiApi.dongGioMon(COM_CHAY, 4));

        TrangDatMon.TamTinh t = trang.chonThoiDiemNhan(ngayLe.atTime(12, 0)).docTamTinhCoGiamGia();

        assertThat(t.tienMon()).isEqualTo(540_000L);
        assertThat(t.giamGia()).as("Sau giảm còn 486.000, dưới mức 500.000").isEqualTo(54_000L);
        assertThat(t.phiGiao()).isEqualTo("Miễn phí");
        assertThat(t.vat()).isEqualTo(38_880L);
        assertThat(t.tong()).isEqualTo(524_880L);
    }

    @Test
    @DisplayName("TC-HOL-08 Đổi giờ nhận từ dịp lễ sang ngày thường thì mất dòng giảm giá")
    void tcHol08_doiSangNgayThuong() {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(sau(1), 2);
        GoiApi.taoDipLe(ten("Lễ đổi giờ"), ngayLe, ngayLe, 0.10, true);
        TrangDatMon trang = gioNhanNgayThuong(ngayLe.plusDays(1), false, GoiApi.dongGioMon(COM_CHAY, 2));
        assertThat(trang.chonThoiDiemNhan(ngayLe.atTime(12, 0)).docTamTinhCoGiamGia().giamGia()).isEqualTo(27_000L);

        TrangDatMon.TamTinh t = trang.chonThoiDiemNhan(ngayLe.plusDays(1).atTime(12, 0)).docTamTinhKhongGiamGia();

        assertThat(t.giamGia()).isNull();
        assertThat(t.ghiChuGiam()).isEmpty();
        assertThat(t.tong()).as("270.000 cộng VAT 21.600").isEqualTo(291_600L);
    }

    @Test
    @DisplayName("TC-HOL-09 Gửi đơn món nhận vào dịp lễ thì trang tra cứu hiện dòng giảm giá và tổng đã giảm")
    void tcHol09_guiDonVaTraCuu() {
        LocalDate ngayLe = GoiApi.dauKhoangNgayThuong(sau(1), 2);
        GoiApi.taoDipLe(ten("Lễ gửi đơn"), ngayLe, ngayLe, 0.10, true);
        TrangDatMon trang = gioNhanNgayThuong(ngayLe.plusDays(1), false, GoiApi.dongGioMon(COM_CHAY, 2));

        trang.dienThongTinAnTaiCho("Khách Kiểm Thử", "0901234567", "4", ngayLe.atTime(12, 0));
        trang.docTamTinhCoGiamGia();
        trang.guiDon();

        assertThat(trang.coChuyenToi(TrangTraCuuMon.DUONG_DAN)).isTrue();
        TrangTraCuuMon traCuu = new TrangTraCuuMon(driver());
        assertThat(traCuu.giamGia()).isEqualTo(27_000L);
        assertThat(traCuu.tongCong()).isEqualTo(262_440L);
    }

    // ================= Trợ lý tư vấn =================

    @Test
    @DisplayName("TC-HOL-10 Trợ lý tư vấn báo được ưu đãi dịp lễ sắp tới lấy từ trang quản trị")
    void tcHol10_troLyBaoDipLe() {
        LocalDate ngay = GoiApi.dauKhoangNgayThuong(sau(1), 1);
        String tenDip = ten("Lễ trợ lý");
        GoiApi.taoDipLe(tenDip, ngay, ngay, 0.18, true);
        new TrangThucDon(driver()).mo();

        String traLoi = new HopThoaiTroLy(driver()).mo().hoi("Dịp lễ có giảm giá không?");

        assertThat(traLoi).contains("không cộng dồn").contains(tenDip).contains("18%");
    }
}
