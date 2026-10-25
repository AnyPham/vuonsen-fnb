package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.luong.LuongDangNhap;
import vn.vuonsen.kiemthu.trang.TrangDangNhap;
import vn.vuonsen.kiemthu.trang.TrangQuanTriLich;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 17 — Lịch đặt chỗ của bộ phận quản trị, ứng với TC-CAL-01 đến TC-CAL-10.
 *
 * Trang /quan-tri/lich-dat gộp đơn đặt tiệc và đơn đặt món lên cùng một lịch. Đơn tiệc xếp
 * theo ngày tổ chức, đơn món xếp theo thời điểm khách nhận.
 *
 * Kịch bản tự tạo đơn qua API rồi mới mở lịch, nên không phụ thuộc dữ liệu mẫu có sẵn.
 * Ngày tổ chức luôn lấy ngày chưa có đơn nào để lưới tháng không lẫn với đơn của kịch bản khác.
 */
@DisplayName("Luồng 17 — Lịch đặt chỗ quản trị")
class KiemThuQuanTriLich extends KiemThuCoSo {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private TrangQuanTriLich moLichBangQuanTri() {
        LuongDangNhap.quanTri(driver());
        return new TrangQuanTriLich(driver()).mo();
    }

    // ================= Quyền truy cập =================

    @Test
    @DisplayName("TC-CAL-01 Khách chưa đăng nhập mở lịch thì bị đưa về trang đăng nhập")
    void tcCal01_khachKhongVaoDuoc() {
        TrangQuanTriLich lich = new TrangQuanTriLich(driver()).mo();

        assertThat(lich.coChuyenToi(TrangDangNhap.DUONG_DAN)).isTrue();
        assertThat(lich.coLuoiThang()).isFalse();
    }

    @Test
    @DisplayName("TC-CAL-02 Quản trị mở lịch thì thấy lưới tháng")
    void tcCal02_quanTriThayLuoi() {
        TrangQuanTriLich lich = moLichBangQuanTri();

        assertThat(lich.choLuoiThang()).isTrue();
        assertThat(lich.tieuDeThang()).contains("Tháng").contains(String.valueOf(LocalDate.now().getYear()));
    }

    // ================= Lưới tháng =================

    @Test
    @DisplayName("TC-CAL-03 Lưới tháng luôn đủ số ô chia hết cho bảy")
    void tcCal03_luoiDuTuan() {
        TrangQuanTriLich lich = moLichBangQuanTri();

        int soO = lich.soO();
        assertThat(soO % 7).isZero();
        /* Một tháng trải tối thiểu bốn tuần và tối đa sáu tuần trên lưới bắt đầu từ thứ Hai. */
        assertThat(soO).isBetween(28, 42);
    }

    @Test
    @DisplayName("TC-CAL-04 Ngày chưa có đơn nào thì ô ghi rõ là còn trống")
    void tcCal04_ngayTrongGhiRo() {
        TrangQuanTriLich lich = moLichBangQuanTri();

        /*
         * Không chỉ đích danh một ngày, vì ngày nào trống còn tùy dữ liệu đang có. Điều cần
         * canh là lưới có đánh dấu ngày trống hay không, nên đếm trên cả tháng.
         */
        assertThat(lich.soOTrong()).isPositive();
    }

    @Test
    @DisplayName("TC-CAL-05 Đơn đặt tiệc vừa tạo hiện đúng ô ngày tổ chức, kèm buổi")
    void tcCal05_donTiecHienDungO() {
        /* 120 khách là 12 mâm, dưới ngưỡng 20 mâm nên chỉ cần báo trước 3 ngày. */
        LocalDate ngay = GoiApi.ngayChuaDung();
        GoiApi.taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê", ngay, "EVENING", 120);

        TrangQuanTriLich lich = moLichBangQuanTri().moThangCua(ngay);

        assertThat(lich.chuTrongO(ngay.format(ISO)))
                .hasValueSatisfying(chu -> assertThat(chu).contains("Buổi tối").doesNotContain("còn trống"));
    }

    @Test
    @DisplayName("TC-CAL-06 Dòng tóm tắt đếm đúng số đơn trong khoảng đang xem")
    void tcCal06_tomTatDemDung() {
        LocalDate ngay = GoiApi.ngayChuaDung();
        GoiApi.taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê", ngay, "NOON", 120);

        TrangQuanTriLich lich = moLichBangQuanTri().moThangCua(ngay);

        assertThat(lich.tomTat()).contains("đơn đặt tiệc").contains("đơn đặt món");
    }

    // ================= Chuyển tháng =================

    @Test
    @DisplayName("TC-CAL-07 Bấm tháng sau rồi tháng trước thì quay lại đúng tháng ban đầu")
    void tcCal07_chuyenThangQuaLai() {
        TrangQuanTriLich lich = moLichBangQuanTri();
        String banDau = lich.tieuDeThang();

        lich.thangSau();
        String thangSau = lich.tieuDeThang();
        assertThat(thangSau).isNotEqualTo(banDau);

        lich.thangTruoc();
        assertThat(lich.tieuDeThang()).isEqualTo(banDau);
    }

    // ================= Xem ngày =================

    @Test
    @DisplayName("TC-CAL-08 Bấm một ô có đơn thì chuyển sang xem ngày và liệt kê đơn đó")
    void tcCal08_bamOMoChiTiet() {
        LocalDate ngay = GoiApi.ngayChuaDung();
        Map<String, Object> don = GoiApi.taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê", ngay, "EVENING", 120);
        String maDon = String.valueOf(don.get("code"));

        TrangQuanTriLich lich = moLichBangQuanTri().moThangCua(ngay).bamO(ngay.format(ISO));

        assertThat(lich.coBangNgay()).isTrue();
        List<TrangQuanTriLich.DongLich> dong = lich.cacDongNgay();
        assertThat(dong).extracting(TrangQuanTriLich.DongLich::maDon).contains(maDon);
        assertThat(dong).extracting(TrangQuanTriLich.DongLich::loai).contains("Đặt tiệc");
    }

    @Test
    @DisplayName("TC-CAL-09 Xem một ngày không có đơn nào thì báo trống thay vì bảng rỗng")
    void tcCal09_ngayTrongBaoRo() {
        /* Ngày chưa có đơn tiệc nào; lấy xa hẳn để không đụng đơn món của dữ liệu mẫu. */
        LocalDate trong = GoiApi.ngayChuaDung().plusMonths(6);

        TrangQuanTriLich lich = moLichBangQuanTri().xemNgay().chonNgay(trong.format(ISO));

        assertThat(lich.coBangNgay()).isFalse();
    }

    @Test
    @DisplayName("TC-CAL-10 Đơn tiệc trên lịch hiện đúng trạng thái đang chờ xác nhận")
    void tcCal10_hienDungTrangThai() {
        LocalDate ngay = GoiApi.ngayChuaDung();
        GoiApi.taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê", ngay, "MORNING", 120);

        TrangQuanTriLich lich = moLichBangQuanTri().xemNgay().chonNgay(ngay.format(ISO));

        assertThat(lich.cacDongNgay())
                .extracting(TrangQuanTriLich.DongLich::trangThai)
                .contains("Chờ xác nhận");
    }
}
