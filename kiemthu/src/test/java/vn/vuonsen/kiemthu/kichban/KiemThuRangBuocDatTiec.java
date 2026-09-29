package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.trang.TrangDatTiec;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 6 — Ràng buộc khi đặt tiệc, ứng với TC-BOOKV-01 đến TC-BOOKV-11.
 *
 * Tham số: số khách 10–800, tiệc thường báo trước 3 ngày, tiệc từ 20 mâm báo trước 7 ngày.
 *
 * Một số ràng buộc giao diện chặn ngay từ bước nhập, sớm hơn đặc tả mô tả (đặc tả ghi máy
 * chủ báo lỗi khi gửi đơn). Kịch bản kiểm tra điều cốt lõi là đơn vi phạm bị từ chối, ở tầng
 * nào chặn thì ghi rõ trong từng kịch bản.
 */
@DisplayName("Luồng 6 — Ràng buộc khi đặt tiệc")
class KiemThuRangBuocDatTiec extends KiemThuCoSo {

    private static final String SEN_VANG = "Sảnh Sen Vàng";

    private static LocalDate sau(int soNgay) {
        return LocalDate.now().plusDays(soNgay);
    }

    private TrangDatTiec buoc1(int soKhach, LocalDate ngay, String buoi) {
        return new TrangDatTiec(driver()).mo().dienBuoc1("WEDDING", ngay, buoi, soKhach).tiepTuc();
    }

    private TrangDatTiec denBuoc3(int soKhach, LocalDate ngay, String goi) {
        TrangDatTiec trang = buoc1(soKhach, ngay, "EVENING");
        trang.dangOBuoc2();
        trang.chonKhongGian(SEN_VANG).chonGoi(goi).tiepTuc();
        trang.dangOBuoc3();
        return trang;
    }

    /* Giao diện khóa luôn nút chọn không gian khi số khách vượt sức chứa tối đa. */
    @Test
    @DisplayName("TC-BOOKV-01 Số khách vượt sức chứa thì không chọn được không gian đó")
    void tcBookv01_vuotSucChua() {
        TrangDatTiec trang = buoc1(300, sau(30), "EVENING");
        trang.dangOBuoc2();

        assertThat(trang.khongGianBiKhoa("Nhà Rường Gỗ")).as("Nhà Rường Gỗ tối đa 60 khách").isTrue();
        assertThat(trang.khongGianBiKhoa(SEN_VANG)).as("Sảnh Sen Vàng chứa được 300 khách").isFalse();
    }

    @Test
    @DisplayName("TC-BOOKV-02 Số khách dưới mức tối thiểu")
    void tcBookv02_duoiToiThieu() {
        TrangDatTiec trang = buoc1(5, sau(30), "EVENING");

        assertThat(trang.loiSoKhach()).contains("10").contains("800");
    }

    @Test
    @DisplayName("TC-BOOKV-03 Số khách vượt mức tối đa")
    void tcBookv03_vuotToiDa() {
        TrangDatTiec trang = buoc1(900, sau(30), "EVENING");

        assertThat(trang.loiSoKhach()).contains("10").contains("800");
    }

    @Test
    @DisplayName("TC-BOOKV-04 Tiệc nhỏ đặt sát ngày bị từ chối")
    void tcBookv04_tiecNhoSatNgay() {
        TrangDatTiec trang = buoc1(50, sau(1), "EVENING");

        assertThat(trang.loiNgayToChuc()).contains("ít nhất 3 ngày");
    }

    @Test
    @DisplayName("TC-BOOKV-05 Tiệc từ 20 mâm đặt chưa đủ bảy ngày bị từ chối")
    void tcBookv05_tiecLonChuaDuBayNgay() {
        TrangDatTiec trang = buoc1(200, sau(5), "EVENING");

        assertThat(trang.loiNgayToChuc()).contains("ít nhất 7 ngày");
    }

    /*
     * Ranh giới quan trọng: 19 mâm chưa phải tiệc lớn nên chỉ cần báo trước 3 ngày. Kịch bản gửi
     * hẳn đơn để chắc cả máy chủ cũng chấp nhận, không chỉ giao diện cho qua.
     */
    @Test
    @DisplayName("TC-BOOKV-06 Ranh giới tiệc lớn: 19 mâm đặt trước 5 ngày vẫn được nhận")
    void tcBookv06_ranhGioi19Mam() {
        TrangDatTiec trang = denBuoc3(190, sau(5), "Gói Đồng Quê");

        String maDon = trang.dienLienHe("Khách Ranh Giới", "0901234567", null).guiYeuCau().maDonVuaTao();

        assertThat(maDon).matches("VS-\\d{8}-\\d{4}");
    }

    /* Giao diện khóa luôn nút chọn gói khi gói dài hơn buổi đã chọn. */
    @Test
    @DisplayName("TC-BOOKV-07 Gói tiệc dài hơn thời lượng buổi thì không chọn được")
    void tcBookv07_goiDaiHonBuoi() {
        TrangDatTiec trang = buoc1(200, sau(30), "MORNING");
        trang.dangOBuoc2();

        assertThat(trang.goiBiKhoa("Gói Sen Vàng")).as("Gói 5 tiếng, buổi sáng chỉ 4 tiếng").isTrue();
        assertThat(trang.goiBiKhoa("Gói Đồng Quê")).as("Gói 3 tiếng vừa buổi sáng").isFalse();
    }

    @Test
    @DisplayName("TC-BOOKV-08 Trùng lịch với đơn đã xác nhận thì bị từ chối")
    void tcBookv08_trungLich() {
        LocalDate ngay = GoiApi.ngayChuaDung();
        Map<String, Object> donCo = GoiApi.taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê", ngay, "EVENING", 200);
        GoiApi.doiTrangThaiDonTiec(GoiApi.so(donCo, "id"), "CONFIRMED");

        TrangDatTiec trang = denBuoc3(200, ngay, "Gói Sen Vàng");
        trang.dienLienHe("Khách Trùng Lịch", "0901234567", null).guiYeuCau();

        assertThat(trang.thongBaoLoiChung()).contains("đã có tiệc");
    }

    @Test
    @DisplayName("TC-BOOKV-09 Bỏ trống họ tên ở bước ba")
    void tcBookv09_boTrongHoTen() {
        TrangDatTiec trang = denBuoc3(200, sau(30), "Gói Sen Vàng");

        trang.dienLienHe("", "0901234567", null).guiYeuCau();

        assertThat(trang.loiHoTen()).isEqualTo("Vui lòng nhập họ tên");
    }

    @Test
    @DisplayName("TC-BOOKV-10 Bỏ trống số điện thoại ở bước ba")
    void tcBookv10_boTrongSoDienThoai() {
        TrangDatTiec trang = denBuoc3(200, sau(30), "Gói Sen Vàng");

        trang.dienLienHe("Khách Kiểm Thử", "", null).guiYeuCau();

        assertThat(trang.loiSoDienThoai()).isEqualTo("Số điện thoại không hợp lệ");
    }

    @Test
    @DisplayName("TC-BOOKV-11 Ngày tổ chức trong quá khứ bị từ chối")
    void tcBookv11_ngayTrongQuaKhu() {
        TrangDatTiec trang = buoc1(50, sau(-1), "EVENING");

        assertThat(trang.loiNgayToChuc()).isNotBlank();
    }
}
