package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.GoiApi;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.thanhphan.BangTamTinh;
import vn.vuonsen.kiemthu.trang.TrangDatTiec;
import vn.vuonsen.kiemthu.trang.TrangTraCuuTiec;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 5 — Đặt tiệc ba bước, ứng với TC-BOOK-01 đến TC-BOOK-13.
 *
 * Tham số nghiệp vụ: 1 mâm 10 khách, VAT 8%, cọc 30%, giảm 5% khi đặt trước từ 60 ngày,
 * miễn phí thuê khi tiền ăn đạt 10 lần phí thuê, chưa đạt thì giảm phí thuê theo tỉ lệ.
 *
 * Mọi con số mong đợi tính tay từ công thức trong PricingService, ví dụ trường hợp chuẩn:
 *   200 khách, Sảnh Sen Vàng (thuê 12 triệu), Gói Sen Vàng (4,5 triệu một mâm), đặt trước 30 ngày
 *   20 mâm x 4.500.000 = 90.000.000 tiền ăn
 *   mức miễn phí thuê = 12.000.000 x 10 = 120.000.000; mới đạt 75% nên trả 25% phí thuê = 3.000.000
 *   VAT 8% của 93.000.000 = 7.440.000; tổng 100.440.000; cọc 30% = 30.132.000
 */
@DisplayName("Luồng 5 — Đặt tiệc ba bước")
class KiemThuDatTiec extends KiemThuCoSo {

    private static final String SEN_VANG = "Sảnh Sen Vàng";
    private static final String VEN_SONG = "Sảnh Ven Sông";
    private static final String CHOI_SEN = "Cụm Chòi Sen";
    private static final String GOI_SEN_VANG = "Gói Sen Vàng";
    private static final String GOI_DONG_QUE = "Gói Đồng Quê";
    private static final String GOI_THUONG_UYEN = "Gói Thượng Uyển";

    private static LocalDate sau(int soNgay) {
        return LocalDate.now().plusDays(soNgay);
    }

    /*
     * Ngày không trùng dịp lễ nào, dùng cho các kịch bản so số tiền cố định. Chạy đúng dịp lễ thì
     * ngày cách 30 hay 70 ngày có thể rơi vào dịp giảm giá, con số mong đợi sẽ lệch.
     * buoc 1 là tìm về sau, -1 là tìm về trước.
     */
    private static LocalDate ngayThuong(LocalDate ngay, int buoc) {
        return GoiApi.ngayThuong(ngay, buoc);
    }

    /** Mở trang, điền bước 1 hợp lệ rồi sang bước 2. */
    private TrangDatTiec denBuoc2(int soKhach, LocalDate ngay) {
        TrangDatTiec trang = new TrangDatTiec(driver()).mo()
                .dienBuoc1("WEDDING", ngay, "EVENING", soKhach)
                .tiepTuc();
        trang.dangOBuoc2();
        return trang;
    }

    private BangTamTinh.SoLieu tamTinh(String khongGian, String goi, int soKhach, LocalDate ngay) {
        TrangDatTiec trang = denBuoc2(soKhach, ngay).chonKhongGian(khongGian).chonGoi(goi);
        return trang.bangTamTinh().docKhiOnDinh();
    }

    @Test
    @DisplayName("TC-BOOK-01 Đi trọn ba bước và gửi đơn, đơn ở trạng thái chờ xác nhận")
    void tcBook01_diTronBaBuoc() {
        TrangDatTiec trang = denBuoc2(200, sau(30)).chonKhongGian(SEN_VANG).chonGoi(GOI_SEN_VANG).tiepTuc();
        trang.dangOBuoc3();

        String maDon = trang.dienLienHe("Khách Kiểm Thử", "0901234567", "kiemthu@vuonsen.vn")
                .guiYeuCau()
                .maDonVuaTao();

        assertThat(maDon).matches("VS-\\d{8}-\\d{4}");
        TrangTraCuuTiec traCuu = new TrangTraCuuTiec(driver()).mo().traCuu(maDon);
        assertThat(traCuu.trangThai()).isEqualTo("Chờ xác nhận");
    }

    @Test
    @DisplayName("TC-BOOK-02 Số mâm tính đúng từ số khách")
    void tcBook02_soMamTuSoKhach() {
        assertThat(tamTinh(SEN_VANG, GOI_SEN_VANG, 200, sau(30)).soMam()).isEqualTo(20L);
    }

    @Test
    @DisplayName("TC-BOOK-03 Số khách lẻ làm tròn lên một mâm")
    void tcBook03_lamTronLenMotMam() {
        assertThat(tamTinh(SEN_VANG, GOI_SEN_VANG, 205, sau(30)).soMam()).isEqualTo(21L);
    }

    @Test
    @DisplayName("TC-BOOK-04 Bảng tạm tính đầy đủ, đúng công thức")
    void tcBook04_bangTamTinhDungCongThuc() {
        BangTamTinh.SoLieu s = tamTinh(SEN_VANG, GOI_SEN_VANG, 200, ngayThuong(sau(30), 1));

        assertThat(s.tienAn()).as("Tiền ăn").isEqualTo(90_000_000L);
        assertThat(soTien(s.phiThue())).as("Phí thuê giảm theo tỉ lệ").isEqualTo(3_000_000L);
        assertThat(s.giamGia()).as("Đặt trước 30 ngày thì không có dòng giảm giá").isNull();
        assertThat(s.vat()).as("VAT").isEqualTo(7_440_000L);
        assertThat(s.tong()).as("Tổng").isEqualTo(100_440_000L);
        assertThat(s.coc()).as("Cọc").isEqualTo(30_132_000L);
    }

    @Test
    @DisplayName("TC-BOOK-05 Bảng tạm tính cập nhật ngay khi đổi số khách")
    void tcBook05_capNhatKhiDoiSoKhach() {
        TrangDatTiec trang = denBuoc2(200, sau(30)).chonKhongGian(SEN_VANG).chonGoi(GOI_SEN_VANG);
        BangTamTinh bang = trang.bangTamTinh();
        long tongTruoc = bang.docKhiOnDinh().tong();

        trang.quayLai().doiSoKhach(300);
        BangTamTinh.SoLieu sau = bang.docSauKhiTinhLai(tongTruoc);

        assertThat(sau.soMam()).isEqualTo(30L);
        assertThat(sau.tong()).isGreaterThan(tongTruoc);
        assertThat(trang.duongDanHienTai()).as("Không được tải lại sang trang khác").isEqualTo(TrangDatTiec.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-BOOK-06 Bảng tạm tính cập nhật khi đổi gói tiệc")
    void tcBook06_capNhatKhiDoiGoi() {
        TrangDatTiec trang = denBuoc2(200, sau(30)).chonKhongGian(SEN_VANG).chonGoi(GOI_SEN_VANG);
        BangTamTinh bang = trang.bangTamTinh();
        BangTamTinh.SoLieu truoc = bang.docKhiOnDinh();
        assertThat(truoc.donGia()).isEqualTo(4_500_000L);

        trang.chonGoi(GOI_THUONG_UYEN);
        BangTamTinh.SoLieu sau = bang.docSauKhiTinhLai(truoc.tong());

        assertThat(sau.donGia()).isEqualTo(6_800_000L);
        assertThat(sau.tienAn()).isEqualTo(136_000_000L);
    }

    @Test
    @DisplayName("TC-BOOK-07 Miễn phí thuê không gian khi tiền ăn đạt mức")
    void tcBook07_mienPhiThueKhiDatMuc() {
        BangTamTinh.SoLieu s = tamTinh(SEN_VANG, GOI_SEN_VANG, 270, sau(30));

        assertThat(s.soMam()).isEqualTo(27L);
        assertThat(s.tienAn()).isEqualTo(121_500_000L);
        assertThat(s.phiThue()).isEqualTo("Miễn phí");
        assertThat(s.quyTac()).anyMatch(q -> q.contains("Miễn phí thuê không gian"));
    }

    @Test
    @DisplayName("TC-BOOK-08 Giảm 5% khi đặt trước từ 60 ngày")
    void tcBook08_giamKhiDatSom() {
        BangTamTinh.SoLieu s = tamTinh(SEN_VANG, GOI_SEN_VANG, 200, ngayThuong(sau(70), 1));

        assertThat(s.giamGia()).as("Giảm 5% của 93.000.000").isEqualTo(4_650_000L);
        assertThat(s.vat()).as("VAT tính trên phần còn lại").isEqualTo(7_068_000L);
        assertThat(s.tong()).isEqualTo(95_418_000L);
    }

    @Test
    @DisplayName("TC-BOOK-09 Không giảm khi đặt trước dưới 60 ngày")
    void tcBook09_khongGiamDuoi60Ngay() {
        BangTamTinh.SoLieu s = tamTinh(SEN_VANG, GOI_SEN_VANG, 200, ngayThuong(sau(59), -1));

        assertThat(s.giamGia()).as("Không có dòng giảm giá").isNull();
        assertThat(s.tong()).isEqualTo(100_440_000L);
    }

    @Test
    @DisplayName("TC-BOOK-10 Áp mức mâm tối thiểu của không gian")
    void tcBook10_mucMamToiThieu() {
        BangTamTinh.SoLieu s = tamTinh(VEN_SONG, GOI_SEN_VANG, 200, sau(30));

        assertThat(s.soMam()).as("Sảnh Ven Sông nhận tối thiểu 30 mâm").isEqualTo(30L);
        assertThat(s.quyTac()).anyMatch(q -> q.contains("tối thiểu 30 mâm"));
    }

    @Test
    @DisplayName("TC-BOOK-11 Không gian tính phí theo chòi")
    void tcBook11_tinhPhiTheoChoi() {
        BangTamTinh.SoLieu s = tamTinh(CHOI_SEN, GOI_DONG_QUE, 50, sau(30));

        assertThat(soTien(s.phiThue())).as("5 chòi x 500.000").isEqualTo(2_500_000L);
        assertThat(s.quyTac()).anyMatch(q -> q.contains("Thuê 5 chòi"));
    }

    @Test
    @DisplayName("TC-BOOK-12 Quay lại bước trước vẫn giữ nguyên dữ liệu đã nhập")
    void tcBook12_quayLaiGiuDuLieu() {
        LocalDate ngay = sau(30);
        TrangDatTiec trang = denBuoc2(200, ngay).chonKhongGian(SEN_VANG).chonGoi(GOI_SEN_VANG);

        trang.quayLai();

        assertThat(trang.loaiSuKienDangChon()).isEqualTo("WEDDING");
        assertThat(trang.soKhachDangNhap()).isEqualTo("200");
        assertThat(trang.ngayDangChon()).isEqualTo(ngay.toString());
        trang.tiepTuc();
        assertThat(trang.khongGianDangChon()).isEqualTo(SEN_VANG);
    }

    @Test
    @DisplayName("TC-BOOK-13 Chưa chọn không gian và gói thì không sang bước 3 được")
    void tcBook13_chuaChonKhongSangBuoc3() {
        TrangDatTiec trang = denBuoc2(200, sau(30));

        trang.tiepTuc();

        assertThat(trang.loiKhongGian()).isNotBlank();
        assertThat(trang.loiGoiTiec()).isNotBlank();
        assertThat(trang.dangOBuoc2()).isTrue();
    }

    private static long soTien(String chu) {
        return chu == null ? 0 : vn.vuonsen.kiemthu.coso.TrangCoSo.soTien(chu);
    }
}
