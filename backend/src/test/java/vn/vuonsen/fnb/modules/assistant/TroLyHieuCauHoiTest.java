package vn.vuonsen.fnb.modules.assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Bốn lỗi trợ lý hiểu sai câu hỏi, phát hiện bằng kiểm thử khám phá ngày 17/11/2026.
 */
@SpringBootTest
class TroLyHieuCauHoiTest {

    @Autowired
    private IntentDetector detector;

    @Autowired
    private AssistantService assistantService;

    @Test
    @DisplayName("KAN-67: lời chào kèm câu hỏi được trả lời theo câu hỏi")
    void loiChaoKemCauHoi() {
        assertThat(detector.detect("Chào shop, sảnh nào chứa được 300 khách?")).isEqualTo(Intent.KHONG_GIAN);
        assertThat(detector.detect("Xin chào, cho mình hỏi đặt cọc bao nhiêu")).isEqualTo(Intent.DAT_COC);
        assertThat(assistantService.answer("Chào shop, sảnh nào chứa được 300 khách?").answer())
                .contains("Sảnh Ven Sông");
    }

    @Test
    @DisplayName("KAN-67: câu chào suông vẫn được chào lại")
    void chaoSuong() {
        assertThat(detector.detect("Xin chào")).isEqualTo(Intent.CHAO_HOI);
        assertThat(detector.detect("alo shop ơi")).isEqualTo(Intent.CHAO_HOI);
    }

    @Test
    @DisplayName("KAN-68: hỏi giá gói tiệc được xếp vào chủ đề chi phí")
    void giaGoiTiec() {
        assertThat(detector.detect("Giá gói tiệc bao nhiêu?")).isEqualTo(Intent.CHI_PHI);
        assertThat(detector.detect("Có gói tiệc nào?")).isEqualTo(Intent.GOI_TIEC);
    }

    @Test
    @DisplayName("KAN-69: hỏi hủy thì có hoàn tiền cọc không được trả lời theo chính sách hoàn cọc")
    void huyThiHoanLaiCoc() {
        assertThat(detector.detect("Hủy thì hoàn lại tiền đặt cọc không?")).isEqualTo(Intent.HOAN_COC);
        assertThat(detector.detect("Nếu hủy có lấy lại cọc được không")).isEqualTo(Intent.HOAN_COC);
        assertThat(detector.detect("Đặt cọc bao nhiêu?")).isEqualTo(Intent.DAT_COC);
    }

    @Test
    @DisplayName("KAN-70: câu hỏi gộp không gian và món ăn được trả lời đủ hai chủ đề")
    void cauHoiGop() {
        assertThat(detector.chuDeGop("Cho mình hỏi về không gian và món ăn"))
                .containsExactly(Intent.KHONG_GIAN, Intent.THUC_DON);
        var kq = assistantService.answer("Cho mình hỏi về không gian và món ăn");
        assertThat(kq.answer()).contains("Sảnh Sen Vàng", "Gỏi củ hũ dừa tôm thịt");
    }

    @Test
    @DisplayName("KAN-70: câu một chủ đề không bị gộp, sảnh nhỏ không bị tách đôi")
    void khongGopNham() {
        assertThat(detector.chuDeGop("Sảnh nào chứa được 300 khách?")).isEmpty();
        assertThat(detector.chuDeGop("Nhóm nhỏ tự chọn món với sảnh nhỏ được không?")).isEmpty();
    }
}
