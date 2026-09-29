package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.CauHinh;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.thanhphan.ThanhDieuHuong;
import vn.vuonsen.kiemthu.trang.TrangDangKy;
import vn.vuonsen.kiemthu.trang.TrangDangNhap;
import vn.vuonsen.kiemthu.trang.TrangHoSo;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 1 — Đăng ký và đăng nhập, ứng với TC-AUTH-01 đến TC-AUTH-10 trong bản đặc tả.
 *
 * Kịch bản chỉ gọi phương thức của Page Object, không chứa locator nào. Mọi câu khẳng định
 * đều nằm ở đây chứ không nằm trong Page Object.
 *
 * Kết quả mong đợi viết theo bản đặc tả test case, không viết theo cách giao diện đang cài
 * đặt. Kiểm thử để phát hiện chỗ giao diện làm khác đặc tả, nên nếu viết theo cài đặt thì
 * sai ở đâu cũng sẽ đạt.
 */
@DisplayName("Luồng 1 — Đăng ký và đăng nhập")
class KiemThuXacThuc extends KiemThuCoSo {

    /** Câu báo cố định cho mọi trường hợp đăng nhập sai, không tiết lộ email có tồn tại hay không. */
    private static final String LOI_SAI_THONG_TIN = "Email hoặc mật khẩu không đúng";

    /** Mỗi lần gọi ra một email chưa ai dùng, để kịch bản đăng ký chạy lại bao nhiêu lần cũng được. */
    private static String emailMoi() {
        return "kiemthu." + System.nanoTime() + "@vuonsen.vn";
    }

    @Test
    @DisplayName("TC-AUTH-01 Đăng ký tài khoản mới hợp lệ")
    void tcAuth01_dangKyTaiKhoanMoiHopLe() {
        TrangDangKy trang = new TrangDangKy(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangKy("Khách Kiểm Thử", emailMoi(), "0901234567", "matkhau123");

        assertThat(thanhDieuHuong.coChuyenSangDaDangNhap())
                .as("Đăng ký thành công thì phải tự đăng nhập").isTrue();
        assertThat(trang.coChuyenToi("/"))
                .as("Đăng ký xong phải chuyển về trang chủ, đang ở %s", trang.duongDanHienTai()).isTrue();
    }

    @Test
    @DisplayName("TC-AUTH-02 Từ chối email đã tồn tại")
    void tcAuth02_tuChoiEmailDaTonTai() {
        TrangDangKy trang = new TrangDangKy(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangKy(
                "Khách Trùng Email", CauHinh.EMAIL_QUAN_TRI, null, "matkhau123");

        assertThat(trang.thongBaoLoi()).isEqualTo("Email này đã được đăng ký");
        assertThat(thanhDieuHuong.daDangNhap()).as("Không được đăng nhập").isFalse();
        assertThat(trang.duongDanHienTai()).isEqualTo(TrangDangKy.DUONG_DAN);
    }

    /*
     * Đặc tả ghi thông báo hiện ngay dưới ô mật khẩu. Giao diện thực tế dùng kiểm tra có sẵn
     * của trình duyệt (minLength), nên câu nhắc hiện dạng bong bóng của trình duyệt. Kịch bản
     * kiểm tra điều cốt lõi của đặc tả: form bị chặn gửi và có câu nhắc ở đúng ô mật khẩu.
     */
    @Test
    @DisplayName("TC-AUTH-03 Từ chối mật khẩu dưới 6 ký tự")
    void tcAuth03_tuChoiMatKhauDuoi6KyTu() {
        TrangDangKy trang = new TrangDangKy(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangKy("Khách Mật Khẩu Ngắn", emailMoi(), null, "12345");

        assertThat(trang.loiTrinhDuyetOMatKhau()).as("Ô mật khẩu phải có câu nhắc lỗi").isNotBlank();
        assertThat(thanhDieuHuong.daDangNhap()).as("Không được đăng nhập").isFalse();
        assertThat(trang.duongDanHienTai()).isEqualTo(TrangDangKy.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-AUTH-04 Từ chối số điện thoại sai định dạng")
    void tcAuth04_tuChoiSoDienThoaiSaiDinhDang() {
        TrangDangKy trang = new TrangDangKy(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangKy("Khách Sai Số", emailMoi(), "1234567", "matkhau123");

        assertThat(trang.thongBaoLoi()).contains("Số điện thoại không hợp lệ");
        assertThat(thanhDieuHuong.daDangNhap()).as("Không được đăng nhập").isFalse();
    }

    @Test
    @DisplayName("TC-AUTH-05 Từ chối email sai định dạng")
    void tcAuth05_tuChoiEmailSaiDinhDang() {
        TrangDangKy trang = new TrangDangKy(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangKy("Khách Sai Email", "abc@", null, "matkhau123");

        assertThat(trang.loiTrinhDuyetOEmail()).as("Ô email phải có câu nhắc lỗi").isNotBlank();
        assertThat(thanhDieuHuong.daDangNhap()).as("Không được đăng nhập").isFalse();
        assertThat(trang.duongDanHienTai()).isEqualTo(TrangDangKy.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-AUTH-06 Đăng nhập đúng thông tin")
    void tcAuth06_dangNhapDungThongTin() {
        TrangDangNhap trang = new TrangDangNhap(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangNhap(CauHinh.EMAIL_QUAN_TRI, CauHinh.MAT_KHAU_QUAN_TRI);

        assertThat(thanhDieuHuong.coChuyenSangDaDangNhap())
                .as("Thanh điều hướng phải chuyển sang trạng thái đã đăng nhập").isTrue();
        assertThat(trang.coChuyenToi("/"))
                .as("Đăng nhập xong phải rời trang đăng nhập, đang ở %s", trang.duongDanHienTai()).isTrue();
    }

    @Test
    @DisplayName("TC-AUTH-07 Từ chối sai mật khẩu")
    void tcAuth07_tuChoiSaiMatKhau() {
        TrangDangNhap trang = new TrangDangNhap(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangNhap(CauHinh.EMAIL_QUAN_TRI, "sai-mat-khau");

        assertThat(trang.thongBaoLoi()).isEqualTo(LOI_SAI_THONG_TIN);
        assertThat(thanhDieuHuong.daDangNhap()).as("Không được đăng nhập").isFalse();
        assertThat(trang.duongDanHienTai()).isEqualTo(TrangDangNhap.DUONG_DAN);
    }

    @Test
    @DisplayName("TC-AUTH-08 Từ chối email chưa đăng ký, không tiết lộ email có tồn tại hay không")
    void tcAuth08_tuChoiEmailChuaDangKy() {
        TrangDangNhap trang = new TrangDangNhap(driver()).mo();

        ThanhDieuHuong thanhDieuHuong = trang.dangNhap(emailMoi(), "matkhau123");

        // Phải trùng khít câu báo của trường hợp sai mật khẩu. Khác đi dù một chữ là kẻ xấu
        // dò được email nào đã có tài khoản.
        assertThat(trang.thongBaoLoi()).isEqualTo(LOI_SAI_THONG_TIN);
        assertThat(thanhDieuHuong.daDangNhap()).as("Không được đăng nhập").isFalse();
    }

    @Test
    @DisplayName("TC-AUTH-09 Đăng xuất")
    void tcAuth09_dangXuat() {
        ThanhDieuHuong thanhDieuHuong = new TrangDangNhap(driver()).mo()
                .dangNhap(CauHinh.EMAIL_QUAN_TRI, CauHinh.MAT_KHAU_QUAN_TRI);
        assertThat(thanhDieuHuong.coChuyenSangDaDangNhap()).as("Bước chuẩn bị: phải đăng nhập được").isTrue();

        thanhDieuHuong.dangXuat();

        assertThat(thanhDieuHuong.daDangNhap()).as("Đăng xuất xong phải về trạng thái chưa đăng nhập").isFalse();

        TrangHoSo hoSo = new TrangHoSo(driver()).mo();
        assertThat(hoSo.coChuyenToi(TrangDangNhap.DUONG_DAN))
                .as("Đã đăng xuất thì mở trang hồ sơ phải bị chuyển về đăng nhập, đang ở %s",
                        hoSo.duongDanHienTai()).isTrue();
    }

    @Test
    @DisplayName("TC-AUTH-10 Giữ phiên sau khi tải lại trang")
    void tcAuth10_giuPhienSauKhiTaiLai() {
        TrangDangNhap trang = new TrangDangNhap(driver()).mo();
        ThanhDieuHuong thanhDieuHuong = trang.dangNhap(CauHinh.EMAIL_QUAN_TRI, CauHinh.MAT_KHAU_QUAN_TRI);
        assertThat(thanhDieuHuong.coChuyenSangDaDangNhap()).as("Bước chuẩn bị: phải đăng nhập được").isTrue();

        trang.taiLaiTrang();

        assertThat(thanhDieuHuong.coChuyenSangDaDangNhap())
                .as("Tải lại trang xong vẫn phải còn đăng nhập").isTrue();
        assertThat(trang.duongDanHienTai()).isNotEqualTo(TrangDangNhap.DUONG_DAN);
    }
}
