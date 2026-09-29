package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.thanhphan.DaiAnh;
import vn.vuonsen.kiemthu.trang.TrangChiTietKhongGian;
import vn.vuonsen.kiemthu.trang.TrangDatTiec;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 3 — Trang chi tiết không gian, ứng với TC-SPDT-01 đến TC-SPDT-06.
 * Không gian mẫu: Sảnh Sen Vàng, 200–500 khách, 12.000.000đ mỗi buổi.
 */
@DisplayName("Luồng 3 — Trang chi tiết không gian")
class KiemThuChiTietKhongGian extends KiemThuCoSo {

    private static final String SLUG = "sanh-sen-vang";
    private static final String TEN = "Sảnh Sen Vàng";

    @Test
    @DisplayName("TC-SPDT-01 Hiện đúng sức chứa và giá thuê")
    void tcSpdt01_hienDungSucChuaVaGiaThue() {
        TrangChiTietKhongGian trang = new TrangChiTietKhongGian(driver()).mo(SLUG);

        assertThat(trang.ten()).isEqualTo(TEN);
        assertThat(trang.sucChua()).contains("200").contains("500");
        assertThat(trang.giaThue()).isEqualTo(12_000_000L);
    }

    @Test
    @DisplayName("TC-SPDT-02 Danh sách tiện ích hiện đầy đủ")
    void tcSpdt02_tienIchHienDayDu() {
        TrangChiTietKhongGian trang = new TrangChiTietKhongGian(driver()).mo(SLUG);

        assertThat(trang.tienIch()).isNotEmpty().contains("200-500 khách");
    }

    @Test
    @DisplayName("TC-SPDT-03 Bấm ảnh nhỏ thì ảnh lớn đổi theo")
    void tcSpdt03_bamAnhNhoDoiAnhLon() {
        DaiAnh daiAnh = new TrangChiTietKhongGian(driver()).mo(SLUG).daiAnh();
        int soAnh = daiAnh.soAnhNho();
        assertThat(soAnh).as("Không gian mẫu phải có ảnh nhỏ").isPositive();
        String truoc = daiAnh.duongDanAnhLon();

        daiAnh.bamAnhNho(Math.min(2, soAnh - 1));

        assertThat(daiAnh.duongDanAnhLon()).isNotEqualTo(truoc);
    }

    @Test
    @DisplayName("TC-SPDT-04 Bản đồ hiển thị")
    void tcSpdt04_banDoHienThi() {
        TrangChiTietKhongGian trang = new TrangChiTietKhongGian(driver()).mo(SLUG);

        assertThat(trang.banDoDangHien()).isTrue();
        assertThat(trang.nguonBanDo()).contains("google.com/maps");
    }

    /*
     * Đặc tả yêu cầu không gian vừa xem được chọn sẵn ở trang đặt tiệc. Kịch bản bấm nút, đi
     * qua bước 1 để tới bước chọn không gian, rồi kiểm tra không gian nào đang được chọn.
     */
    @Test
    @DisplayName("TC-SPDT-05 Nút đặt tiệc dẫn sang trang đặt tiệc, chọn sẵn không gian vừa xem")
    void tcSpdt05_nutDatTiecChonSanKhongGian() {
        TrangChiTietKhongGian trang = new TrangChiTietKhongGian(driver()).mo(SLUG);
        trang.ten();

        trang.bamDatTiec();

        TrangDatTiec datTiec = new TrangDatTiec(driver());
        assertThat(datTiec.coChuyenToi(TrangDatTiec.DUONG_DAN)).isTrue();
        datTiec.dienBuoc1("WEDDING", LocalDate.now().plusDays(30), "EVENING", 200).tiepTuc();
        assertThat(datTiec.khongGianDangChon())
                .as("Không gian vừa xem phải được chọn sẵn ở bước 2").isEqualTo(TEN);
    }

    @Test
    @DisplayName("TC-SPDT-06 Đường dẫn không tồn tại thì báo không tìm thấy")
    void tcSpdt06_duongDanKhongTonTai() {
        TrangChiTietKhongGian trang = new TrangChiTietKhongGian(driver()).mo("khong-co-that");

        assertThat(trang.thongBaoLoiChung()).containsIgnoringCase("không tìm thấy");
    }
}
