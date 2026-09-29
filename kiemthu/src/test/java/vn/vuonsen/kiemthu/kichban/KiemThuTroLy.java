package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.thanhphan.HopThoaiTroLy;
import vn.vuonsen.kiemthu.trang.TrangThucDon;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 11 — Trợ lý tư vấn, ứng với TC-BOT-01 đến TC-BOT-05.
 *
 * Chỉ kiểm thử nhánh trả lời dự phòng có sẵn trong hệ thống. Nhánh gọi mô hình ngôn ngữ ngoài
 * nằm ngoài phạm vi vì kết quả không lặp lại được. Hộp thoại có mặt trên mọi trang, kịch bản
 * mở nó từ trang thực đơn.
 */
@DisplayName("Luồng 11 — Trợ lý tư vấn")
class KiemThuTroLy extends KiemThuCoSo {

    private HopThoaiTroLy moHopThoai() {
        new TrangThucDon(driver()).mo();
        return new HopThoaiTroLy(driver()).mo();
    }

    @Test
    @DisplayName("TC-BOT-01 Mở hộp thoại thấy lời chào và câu hỏi gợi ý")
    void tcBot01_moHopThoai() {
        HopThoaiTroLy troLy = moHopThoai();

        assertThat(troLy.loiChao()).isNotBlank();
        assertThat(troLy.cauGoiY()).isNotEmpty();
    }

    @Test
    @DisplayName("TC-BOT-02 Hỏi giờ mở cửa thì câu trả lời nhắc đúng giờ")
    void tcBot02_hoiGioMoCua() {
        HopThoaiTroLy troLy = moHopThoai();

        String traLoi = troLy.hoi("Nhà hàng mở cửa mấy giờ");

        assertThat(traLoi).isNotBlank().contains("22:00");
    }

    @Test
    @DisplayName("TC-BOT-03 Bấm câu hỏi gợi ý thì tự gửi và nhận câu trả lời")
    void tcBot03_bamCauGoiY() {
        HopThoaiTroLy troLy = moHopThoai();
        String cauGoiY = troLy.cauGoiY().get(0);

        String traLoi = troLy.bamGoiY(0);

        List<String> daGui = troLy.cauHoiDaGui();
        assertThat(daGui).isNotEmpty();
        assertThat(daGui.get(daGui.size() - 1)).isEqualTo(cauGoiY);
        assertThat(traLoi).isNotBlank();
    }

    @Test
    @DisplayName("TC-BOT-04 Ô nhập trống thì nút gửi bị khóa")
    void tcBot04_oNhapTrong() {
        HopThoaiTroLy troLy = moHopThoai();

        assertThat(troLy.nutGuiDangKhoa()).isTrue();
    }

    @Test
    @DisplayName("TC-BOT-05 Đóng hộp thoại")
    void tcBot05_dongHopThoai() {
        HopThoaiTroLy troLy = moHopThoai();

        troLy.dong();

        assertThat(troLy.dangMo()).isFalse();
        assertThat(troLy.nutMoDangHien()).isTrue();
    }
}
