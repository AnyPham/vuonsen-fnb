package vn.vuonsen.fnb.modules.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm luật đặt mật khẩu.
 *
 * Kiểm thẳng biểu thức thay vì dựng cả tầng web, vì đây là một quy tắc thuần túy. Dựng
 * cả ứng dụng lên chỉ để thử một chuỗi thì chậm mà không kiểm được gì thêm.
 */
@DisplayName("Luật đặt mật khẩu")
class LuatMatKhauTest {

    private boolean dat(String matKhau) {
        return matKhau != null && matKhau.matches(LuatMatKhau.BIEU_THUC);
    }

    @ParameterizedTest(name = "từ chối: {0}")
    @DisplayName("Từ chối các mật khẩu yếu thường gặp")
    @ValueSource(strings = {
            "123456",        // mật khẩu bị dò đầu tiên trong mọi danh sách tấn công
            "1234567",       // vẫn chỉ có số
            "12345678",      // đủ dài nhưng không có chữ
            "abcdefgh",      // không có chữ hoa, không có số
            "ABCDEFGH",      // không có chữ thường, không có số
            "Abcdefgh",      // thiếu số
            "Abc1234",       // đủ loại ký tự nhưng mới 7 ký tự
            "matkhau1",      // thiếu chữ hoa
            "MATKHAU1",      // thiếu chữ thường
    })
    void tuChoiMatKhauYeu(String matKhau) {
        assertThat(dat(matKhau)).isFalse();
    }

    @ParameterizedTest(name = "nhận: {0}")
    @DisplayName("Nhận mật khẩu đủ bốn điều kiện")
    @ValueSource(strings = {
            "Abcd1234",            // đúng mức tối thiểu 8 ký tự
            "Admin@123",           // tài khoản quản trị mặc định phải còn hợp lệ
            "VuonSen2026",
            "Mk1Chuoi@Dai#Hon",    // có ký tự đặc biệt thì vẫn nhận, chỉ là không bắt buộc
            "Tiệc2026Vui",         // chữ có dấu vẫn tính là chữ thường
    })
    void nhanMatKhauDuDieuKien(String matKhau) {
        assertThat(dat(matKhau)).isTrue();
    }

    @Test
    @DisplayName("Chuỗi dài quá trần của BCrypt thì bị từ chối")
    void chanChuoiQuaDai() {
        // BCrypt bỏ lặng phần sau byte thứ 72, khách tưởng mật khẩu dài mà thực ra không
        String quaDai = "Abcd1234" + "x".repeat(LuatMatKhau.DAI_TOI_DA);
        assertThat(dat(quaDai)).isFalse();
    }

    @Test
    @DisplayName("Thông báo lỗi nói rõ cả bốn điều kiện")
    void thongBaoNoiRoDieuKien() {
        assertThat(LuatMatKhau.THONG_BAO)
                .contains("8 ký tự")
                .contains("chữ hoa")
                .contains("chữ thường")
                .contains("số");
    }
}
