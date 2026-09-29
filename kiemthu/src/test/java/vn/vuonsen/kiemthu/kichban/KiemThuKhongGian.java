package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.trang.TrangChiTietKhongGian;
import vn.vuonsen.kiemthu.trang.TrangKhongGian;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 2 — Xem và lọc không gian, ứng với TC-SPACE-01 đến TC-SPACE-08.
 *
 * Dữ liệu mẫu có đúng 6 không gian:
 *   Sảnh Ven Sông 300–800 khách, 15 triệu, ngoài trời
 *   Sảnh Sen Vàng 200–500 khách, 12 triệu, trong nhà
 *   Nhà Rường Gỗ 20–60 khách, 3 triệu
 *   Phòng Hội Nghị Lúa 40–150 khách, 6 triệu
 *   Cụm Chòi Sen 8–144 khách, 500 nghìn mỗi chòi
 *   Vườn Cau 60–150 khách, 7 triệu
 */
@DisplayName("Luồng 2 — Xem và lọc không gian")
class KiemThuKhongGian extends KiemThuCoSo {

    private static final String VEN_SONG = "Sảnh Ven Sông";
    private static final String SEN_VANG = "Sảnh Sen Vàng";
    private static final String RUONG_GO = "Nhà Rường Gỗ";
    private static final String HOI_NGHI = "Phòng Hội Nghị Lúa";
    private static final String CHOI_SEN = "Cụm Chòi Sen";
    private static final String VUON_CAU = "Vườn Cau";

    @Test
    @DisplayName("TC-SPACE-01 Danh sách hiện đủ sáu không gian")
    void tcSpace01_hienDuSauKhongGian() {
        TrangKhongGian trang = new TrangKhongGian(driver()).mo();

        assertThat(trang.tenKhongGianDangHien())
                .containsExactlyInAnyOrder(VEN_SONG, SEN_VANG, RUONG_GO, HOI_NGHI, CHOI_SEN, VUON_CAU);
    }

    /*
     * Đặc tả gốc ghi lọc 50 khách chỉ còn các không gian có sức chứa tối thiểu không vượt 50.
     * Điều đó mâu thuẫn quy tắc nghiệp vụ đã chốt ngày 18/08: bộ lọc chỉ chặn khi vượt sức chứa
     * tối đa, khách ít hơn mức tối thiểu vẫn đặt được và tính theo số mâm tối thiểu. Kịch bản
     * theo quy tắc nghiệp vụ, và dùng 70 khách để kiểm chứng đúng hai vế của quy tắc: loại Nhà
     * Rường Gỗ vì tối đa 60, nhưng vẫn giữ Sảnh Ven Sông dù tối thiểu 300.
     */
    @Test
    @DisplayName("TC-SPACE-02 Lọc theo số khách chỉ loại không gian vượt sức chứa tối đa")
    void tcSpace02_locTheoSoKhach() {
        TrangKhongGian trang = new TrangKhongGian(driver()).mo();

        trang.locSoKhach("70");

        assertThat(trang.tenKhongGianDangHien())
                .containsExactlyInAnyOrder(VEN_SONG, SEN_VANG, HOI_NGHI, CHOI_SEN, VUON_CAU)
                .doesNotContain(RUONG_GO);
    }

    @Test
    @DisplayName("TC-SPACE-03 Lọc theo loại không gian trong nhà")
    void tcSpace03_locTheoLoai() {
        TrangKhongGian trang = new TrangKhongGian(driver()).mo();

        trang.locLoai("INDOOR");

        assertThat(trang.tenKhongGianDangHien()).containsExactly(SEN_VANG);
    }

    @Test
    @DisplayName("TC-SPACE-04 Lọc theo giá thuê tối đa 5.000.000đ")
    void tcSpace04_locTheoGiaToiDa() {
        TrangKhongGian trang = new TrangKhongGian(driver()).mo();

        trang.locGiaToiDa("5000000");

        assertThat(trang.tenKhongGianDangHien()).containsExactlyInAnyOrder(RUONG_GO, CHOI_SEN);
    }

    @Test
    @DisplayName("TC-SPACE-05 Kết hợp hai tiêu chí lọc")
    void tcSpace05_ketHopHaiTieuChi() {
        TrangKhongGian trang = new TrangKhongGian(driver()).mo();

        trang.locSoKhach("500").locLoai("OUTDOOR");

        assertThat(trang.tenKhongGianDangHien()).containsExactly(VEN_SONG);
    }

    @Test
    @DisplayName("TC-SPACE-06 Lọc không ra kết quả thì hiện thông báo rỗng")
    void tcSpace06_locKhongRaKetQua() {
        TrangKhongGian trang = new TrangKhongGian(driver()).mo();

        trang.locSoKhach("900");

        assertThat(trang.tenKhongGianDangHien()).isEmpty();
        assertThat(trang.dangHienKhoiRong()).as("Phải hiện khối thông báo không có kết quả").isTrue();
    }

    @Test
    @DisplayName("TC-SPACE-07 Bỏ bộ lọc thì danh sách quay lại đủ sáu không gian")
    void tcSpace07_xoaBoLoc() {
        TrangKhongGian trang = new TrangKhongGian(driver()).mo();
        trang.locLoai("INDOOR");
        assertThat(trang.tenKhongGianDangHien()).as("Bước chuẩn bị: lọc còn một kết quả").hasSize(1);

        trang.xoaBoLoc();

        assertThat(trang.tenKhongGianDangHien()).hasSize(6);
    }

    @Test
    @DisplayName("TC-SPACE-08 Bấm thẻ mở trang chi tiết")
    void tcSpace08_bamTheMoChiTiet() {
        TrangChiTietKhongGian chiTiet = new TrangKhongGian(driver()).mo().moChiTiet(SEN_VANG);

        assertThat(chiTiet.coChuyenToi("/khong-gian/sanh-sen-vang")).isTrue();
        assertThat(chiTiet.ten()).isEqualTo(SEN_VANG);
    }
}
