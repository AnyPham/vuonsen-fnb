package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.thanhphan.ThanhDieuHuong;
import vn.vuonsen.kiemthu.trang.TrangGoiTiec;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 16 — Xem và lọc gói tiệc, ứng với TC-PKG-01 đến TC-PKG-10.
 *
 * Luồng thêm sau ngày 01/10/2026, cùng đợt với bộ lọc gói tiệc. Trước đó cả bộ kiểm thử
 * không có kịch bản nào đi qua trang bảng giá, dù đây là một trong năm trang khách xem
 * nhiều nhất trước khi quyết định đặt tiệc.
 *
 * Dữ liệu mẫu sau migration V23 có tám gói, xếp theo giá tăng dần:
 *   HOP-MAT        1,8 triệu   5 món    2 giờ
 *   DONG-QUE       2,9 triệu   7 món    3 giờ
 *   SEN-TRANG      3,6 triệu   7 món    4 giờ
 *   TAT-NIEN       3,9 triệu   9 món    4 giờ
 *   SEN-VANG       4,5 triệu   8 món    5 giờ   (được chọn nhiều nhất)
 *   HOI-NGHI       5,2 triệu   8 món    8 giờ
 *   THUONG-UYEN    6,8 triệu  10 món   12 giờ
 *   CUOI-TRON-GOI  8,5 triệu  12 món   10 giờ
 *
 * Kịch bản đối chiếu bằng mã gói chứ không bằng tên, trừ hai trường hợp cố ý kiểm tên.
 * Mã nằm ở thuộc tính data-code nên không đổi khi chuyển ngôn ngữ, còn tên thì đổi.
 */
@DisplayName("Luồng 16 — Xem và lọc gói tiệc")
class KiemThuGoiTiec extends KiemThuCoSo {

    private static final String HOP_MAT = "HOP-MAT";
    private static final String DONG_QUE = "DONG-QUE";
    private static final String SEN_TRANG = "SEN-TRANG";
    private static final String TAT_NIEN = "TAT-NIEN";
    private static final String SEN_VANG = "SEN-VANG";
    private static final String HOI_NGHI = "HOI-NGHI";
    private static final String THUONG_UYEN = "THUONG-UYEN";
    private static final String CUOI_TRON_GOI = "CUOI-TRON-GOI";

    @Test
    @DisplayName("TC-PKG-01 Danh sách hiện đủ tám gói, xếp theo giá tăng dần")
    void tcPkg01_hienDuTamGoi() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();

        assertThat(trang.maGoiDangHien())
                .containsExactly(HOP_MAT, DONG_QUE, SEN_TRANG, TAT_NIEN,
                        SEN_VANG, HOI_NGHI, THUONG_UYEN, CUOI_TRON_GOI);
    }

    @Test
    @DisplayName("TC-PKG-02 Lọc theo ngân sách tối đa 4.000.000đ mỗi mâm")
    void tcPkg02_locTheoNganSach() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();

        trang.locGiaToiDa("4000000");

        assertThat(trang.maGoiDangHien())
                .containsExactly(HOP_MAT, DONG_QUE, SEN_TRANG, TAT_NIEN);
    }

    @Test
    @DisplayName("TC-PKG-03 Lọc theo số món tối thiểu là 9 món")
    void tcPkg03_locTheoSoMon() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();

        trang.locSoMonToiThieu("9");

        assertThat(trang.maGoiDangHien())
                .containsExactly(TAT_NIEN, THUONG_UYEN, CUOI_TRON_GOI)
                .doesNotContain(SEN_VANG);   // 8 món, thiếu đúng một món
    }

    /*
     * Mốc trọn ngày là 8 giờ, lấy theo app.booking.full-day-package-hours của backend. Gói
     * Cưới Trọn Gói dùng 10 giờ nên vẫn thuộc nhóm trọn ngày dù không phải 12 giờ như
     * Thượng Uyển: điều kiện là "từ 8 giờ trở lên" chứ không phải bằng đúng một con số.
     */
    @Test
    @DisplayName("TC-PKG-04 Lọc trọn ngày chỉ giữ gói dùng không gian từ 8 giờ")
    void tcPkg04_locTronNgay() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();

        trang.locThoiGian(TrangGoiTiec.TRON_NGAY);

        assertThat(trang.maGoiDangHien())
                .containsExactly(HOI_NGHI, THUONG_UYEN, CUOI_TRON_GOI);
    }

    @Test
    @DisplayName("TC-PKG-05 Lọc nửa ngày giữ gói từ 4 giờ trở lên")
    void tcPkg05_locNuaNgay() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();

        trang.locThoiGian(TrangGoiTiec.NUA_NGAY);

        assertThat(trang.maGoiDangHien())
                .containsExactly(SEN_TRANG, TAT_NIEN, SEN_VANG, HOI_NGHI, THUONG_UYEN, CUOI_TRON_GOI)
                .doesNotContain(HOP_MAT, DONG_QUE);   // 2 giờ và 3 giờ
    }

    @Test
    @DisplayName("TC-PKG-06 Kết hợp ba tiêu chí lọc cùng lúc")
    void tcPkg06_ketHopBaTieuChi() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();

        trang.locGiaToiDa("4000000")
                .locSoMonToiThieu("9")
                .locThoiGian(TrangGoiTiec.NUA_NGAY);

        // Chỉ Tất Niên thỏa cả ba: 3,9 triệu, 9 món, 4 giờ
        assertThat(trang.maGoiDangHien()).containsExactly(TAT_NIEN);
    }

    @Test
    @DisplayName("TC-PKG-07 Lọc không ra kết quả thì hiện khối thông báo rỗng")
    void tcPkg07_locKhongRaKetQua() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();

        trang.locSoMonToiThieu("20");

        assertThat(trang.maGoiDangHien()).isEmpty();
        assertThat(trang.dangHienKhoiRong())
                .as("Phải hiện khối thông báo không có gói nào khớp").isTrue();
    }

    @Test
    @DisplayName("TC-PKG-08 Bấm Bỏ lọc thì danh sách quay lại đủ tám gói")
    void tcPkg08_boLoc() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();
        trang.locGiaToiDa("2000000");
        assertThat(trang.maGoiDangHien()).as("Bước chuẩn bị: lọc còn một gói").hasSize(1);

        trang.boLoc();

        assertThat(trang.maGoiDangHien()).hasSize(8);
        assertThat(trang.dangCoNutBoLoc())
                .as("Bỏ lọc xong thì nút cũng biến mất").isFalse();
    }

    /*
     * Dòng đếm chỉ hiện khi khách đang lọc. Lúc chưa lọc, câu "8 gói phù hợp" không nói
     * thêm được gì ngoài thứ đã bày ngay bên dưới, nên giao diện cố ý giấu đi.
     */
    @Test
    @DisplayName("TC-PKG-09 Dòng đếm chỉ hiện khi đang lọc và đếm đúng số gói")
    void tcPkg09_dongDemKetQua() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();
        assertThat(trang.chuDemKetQua()).as("Chưa lọc thì không hiện dòng đếm").isEmpty();

        trang.locGiaToiDa("4000000");

        assertThat(trang.chuDemKetQua()).contains("4");
    }

    /*
     * Trang này là nơi gọn nhất để canh chức năng song ngữ bằng kiểm thử giao diện: nó có
     * cả chữ cố định lấy từ tệp ngôn ngữ (nhãn ô lọc) lẫn chữ lấy từ cơ sở dữ liệu (tên
     * gói), nên một kịch bản kiểm được cả hai đường dịch.
     */
    @Test
    @DisplayName("TC-PKG-10 Chuyển sang tiếng Anh thì đổi cả nhãn bộ lọc lẫn tên gói")
    void tcPkg10_chuyenSangTiengAnh() {
        TrangGoiTiec trang = new TrangGoiTiec(driver()).mo();
        assertThat(trang.tenGoiDangHien()).as("Mặc định phải là tiếng Việt").contains("Gói Họp Mặt");

        new ThanhDieuHuong(driver()).doiNgonNgu("en");

        assertThat(trang.nhanOLocGia()).isEqualTo("Maximum price per table");
        assertThat(trang.tenGoiDangHien()).contains("Get-together Package", "Full Wedding Package");
        assertThat(trang.maGoiDangHien())
                .as("Đổi ngôn ngữ không được làm đổi danh sách gói").hasSize(8);
    }
}
