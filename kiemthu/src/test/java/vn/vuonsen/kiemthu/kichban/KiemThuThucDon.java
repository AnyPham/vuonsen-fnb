package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.thanhphan.DaiAnh;
import vn.vuonsen.kiemthu.trang.TrangChiTietMon;
import vn.vuonsen.kiemthu.trang.TrangThucDon;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 4 — Thực đơn và chi tiết món, ứng với TC-MENU-01 đến TC-MENU-08.
 * Thực đơn mẫu có 26 món chia 5 danh mục; mỗi món có 5 ảnh và đủ phần mô tả chi tiết.
 */
@DisplayName("Luồng 4 — Thực đơn và chi tiết món")
class KiemThuThucDon extends KiemThuCoSo {

    private static final String MON_CHINH = "Cơm cháy chà bông kho quẹt";
    private static final String MON_TRANG_MIENG = "Bánh da lợn hấp lá dứa";
    private static final String MON_THEO_CAN = "Heo quay giòn bì";

    private static String slugCua(String tenMon) {
        return (String) GoiApi.monTheoTen(tenMon).get("slug");
    }

    @Test
    @DisplayName("TC-MENU-01 Trang thực đơn hiện đủ năm tab danh mục")
    void tcMenu01_duNamTabDanhMuc() {
        TrangThucDon trang = new TrangThucDon(driver()).mo();

        assertThat(trang.tenCacTabDanhMuc())
                .containsExactly("Khai vị", "Món chính", "Lẩu & nướng", "Tráng miệng", "Đồ uống");
    }

    @Test
    @DisplayName("TC-MENU-02 Chuyển tab thì danh sách món đổi theo")
    void tcMenu02_chuyenTabDoiDanhSach() {
        TrangThucDon trang = new TrangThucDon(driver()).mo();
        List<String> truoc = trang.tenMonDangHien();

        trang.chonDanhMuc("trangmieng");
        List<String> sau = trang.tenMonDangHien();

        assertThat(trang.tabDangChon("trangmieng")).as("Tab vừa bấm phải ở trạng thái đang chọn").isTrue();
        assertThat(sau).isNotEqualTo(truoc).contains(MON_TRANG_MIENG).doesNotContain(MON_CHINH);
    }

    @Test
    @DisplayName("TC-MENU-03 Khối món bán chạy hiển thị")
    void tcMenu03_khoiMonBanChay() {
        TrangThucDon trang = new TrangThucDon(driver()).mo();

        assertThat(trang.soMonTrongKhoiBanChay()).isPositive();
    }

    @Test
    @DisplayName("TC-MENU-04 Mở trang chi tiết món hiện đủ bốn khối thông tin")
    void tcMenu04_moChiTietMon() {
        TrangChiTietMon chiTiet = new TrangThucDon(driver()).mo().moChiTietMon(MON_CHINH);

        assertThat(chiTiet.coChuyenToi("/thuc-don/" + slugCua(MON_CHINH))).isTrue();
        assertThat(chiTiet.ten()).isEqualTo(MON_CHINH);
        assertThat(chiTiet.nguyenLieu()).as("Nguyên liệu").isNotBlank();
        assertThat(chiTiet.cachCheBien()).as("Cách chế biến").isNotBlank();
        assertThat(chiTiet.khauPhan()).as("Khẩu phần").isNotBlank();
        assertThat(chiTiet.luuYKhiDat()).as("Lưu ý khi đặt").isNotBlank();
    }

    @Test
    @DisplayName("TC-MENU-05 Thư viện ảnh của món có 5 ảnh, bấm ảnh nhỏ đổi ảnh lớn")
    void tcMenu05_thuVienAnhCuaMon() {
        DaiAnh daiAnh = new TrangChiTietMon(driver()).mo(slugCua(MON_CHINH)).daiAnh();
        assertThat(daiAnh.soAnhNho()).isEqualTo(5);
        String truoc = daiAnh.duongDanAnhLon();

        daiAnh.bamAnhNho(1);

        assertThat(daiAnh.duongDanAnhLon()).isNotEqualTo(truoc);
    }

    @Test
    @DisplayName("TC-MENU-06 Món tính giá theo cân hiển thị đúng")
    void tcMenu06_monTheoCan() {
        TrangChiTietMon chiTiet = new TrangChiTietMon(driver()).mo(slugCua(MON_THEO_CAN));

        assertThat(chiTiet.gia()).isEqualTo("Theo cân");
    }

    @Test
    @DisplayName("TC-MENU-07 Thời gian chuẩn bị khớp dữ liệu")
    void tcMenu07_thoiGianChuanBi() {
        String slug = slugCua(MON_CHINH);
        Map<String, Object> duLieu = GoiApi.chiTietMon(slug);
        long phut = GoiApi.so(duLieu, "prepMinutes");
        // Giao diện làm tròn số giờ tới một chữ số lẻ và bỏ phần ,0: 2 tiếng chứ không phải 2.0 tiếng
        double gio = Math.round(phut / 60.0 * 10) / 10.0;
        String soGio = gio == Math.floor(gio) ? String.valueOf((long) gio) : String.valueOf(gio);
        String kyVong = phut >= 60 ? "khoảng " + soGio + " tiếng" : "khoảng " + phut + " phút";

        TrangChiTietMon chiTiet = new TrangChiTietMon(driver()).mo(slug);

        assertThat(chiTiet.thoiGianChuanBi()).isEqualTo(kyVong);
    }

    @Test
    @DisplayName("TC-MENU-08 Đường dẫn món không tồn tại thì báo không tìm thấy")
    void tcMenu08_duongDanMonKhongTonTai() {
        TrangChiTietMon chiTiet = new TrangChiTietMon(driver()).mo("mon-khong-co");

        assertThat(chiTiet.thongBaoLoiChung()).containsIgnoringCase("không tìm thấy");
    }

    /*
     * Bốn kịch bản dưới đây canh bộ lọc món chay, thêm cùng đợt với cột
     * vegetarian trong cơ sở dữ liệu.
     *
     * Lý do phần này tồn tại: trước khi có cột đó, trợ lý tư vấn phải tự đoán món nào chay
     * theo tên và đã đoán sai, xếp Nộm hoa chuối vào danh sách chay trong khi nguyên liệu
     * có nước mắm chua ngọt.
     */
    @Test
    @DisplayName("TC-MENU-09 Bật lọc món chay thì mọi món còn lại đều mang nhãn Món chay")
    void tcMenu09_locChay() {
        TrangThucDon trang = new TrangThucDon(driver()).mo();
        int tongTruoc = trang.soMonDangHien();

        trang.batTatLocChay();

        assertThat(trang.dangLocChay()).as("Nút lọc phải ở trạng thái đang bật").isTrue();
        int con = trang.soMonDangHien();
        assertThat(con).as("Lọc xong phải còn ít món hơn").isLessThan(tongTruoc);
        assertThat(trang.soMonCoNhanChay())
                .as("Mọi món còn lại đều phải mang nhãn Món chay, không lẫn món mặn")
                .isEqualTo(con);
    }

    @Test
    @DisplayName("TC-MENU-10 Nộm hoa chuối không nằm trong danh sách món chay")
    void tcMenu10_nomHoaChuoiKhongPhaiMonChay() {
        TrangThucDon trang = new TrangThucDon(driver()).mo();

        trang.batTatLocChay();

        // Món có nước mắm chua ngọt, đúng món mà trợ lý từng xếp nhầm là chay
        assertThat(trang.tenMonDangHien()).doesNotContain("Nộm hoa chuối");
    }

    @Test
    @DisplayName("TC-MENU-11 Lọc chay ghép được với tab danh mục")
    void tcMenu11_locChayGhepVoiDanhMuc() {
        TrangThucDon trang = new TrangThucDon(driver()).mo();

        trang.batTatLocChay().chonDanhMuc("trangmieng");

        assertThat(trang.tabDangChon("trangmieng")).isTrue();
        assertThat(trang.dangLocChay()).as("Chọn danh mục không được làm mất điều kiện lọc chay").isTrue();
        int con = trang.soMonDangHien();
        assertThat(con).isPositive();
        assertThat(trang.soMonCoNhanChay()).isEqualTo(con);
    }

    @Test
    @DisplayName("TC-MENU-12 Tắt lọc chay thì danh sách quay lại đủ món")
    void tcMenu12_tatLocChay() {
        TrangThucDon trang = new TrangThucDon(driver()).mo();
        int tongBanDau = trang.soMonDangHien();
        trang.batTatLocChay();
        assertThat(trang.soMonDangHien()).as("Bước chuẩn bị: lọc xong phải ít món hơn").isLessThan(tongBanDau);

        trang.batTatLocChay();

        assertThat(trang.dangLocChay()).isFalse();
        assertThat(trang.soMonDangHien()).isEqualTo(tongBanDau);
    }
}
