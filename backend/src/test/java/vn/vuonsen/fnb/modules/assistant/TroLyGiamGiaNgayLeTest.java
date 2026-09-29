package vn.vuonsen.fnb.modules.assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscount;
import vn.vuonsen.fnb.modules.holiday.HolidayDiscountRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử trợ lý tư vấn biết về giảm giá dịp lễ, cả nhánh dự phòng lẫn chỉ dẫn gửi mô hình.
 *
 * Dịp lễ thử được thêm trong từng test và hoàn tác sau test, đặt tương đối theo ngày chạy để
 * không phụ thuộc dữ liệu mẫu còn hay đã qua.
 */
@SpringBootTest
@Transactional
class TroLyGiamGiaNgayLeTest {

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private AssistantService assistantService;

    @Autowired
    private IntentDetector detector;

    @Autowired
    private SystemContextBuilder contextBuilder;

    @Autowired
    private HolidayDiscountRepository repository;

    private void dipLe(String ten, LocalDate tu, LocalDate den, String tiLe, boolean dangApDung) {
        repository.save(HolidayDiscount.builder()
                .name(ten).startDate(tu).endDate(den)
                .discountRate(new BigDecimal(tiLe)).active(dangApDung)
                .build());
    }

    @Test
    @DisplayName("UT-NL-34 Hỏi về ngày lễ, dịp lễ, ưu đãi ngày Tết thì hiểu là hỏi khuyến mãi")
    void hieuCauHoiVeDipLe() {
        assertThat(detector.detect("Dịp lễ có giảm giá không?")).isEqualTo(Intent.KHUYEN_MAI);
        assertThat(detector.detect("Ngày lễ thì sao?")).isEqualTo(Intent.KHUYEN_MAI);
        assertThat(detector.detect("Tết có ưu đãi gì không")).isEqualTo(Intent.KHUYEN_MAI);
    }

    @Test
    @DisplayName("UT-NL-35 Trả lời khuyến mãi có ưu đãi dịp lễ và liệt kê dịp sắp tới lấy từ trang quản trị")
    void traLoiCoDipLeSapToi() {
        LocalDate ngayMai = LocalDate.now().plusDays(1);
        dipLe("Lễ thử trợ lý", ngayMai, ngayMai.plusDays(1), "0.18", true);

        var kq = assistantService.answer("Có khuyến mãi gì không?");

        assertThat(kq.intent()).isEqualTo(Intent.KHUYEN_MAI);
        assertThat(kq.answer())
                .contains("Dịp lễ", "không cộng dồn")
                .contains("Lễ thử trợ lý (" + ngayMai.format(NGAY) + " - " + ngayMai.plusDays(1).format(NGAY) + "): giảm 18%");
    }

    @Test
    @DisplayName("UT-NL-36 Dịp lễ đã qua hoặc đang tắt thì trợ lý không nhắc tới")
    void khongNhacDipDaQuaHoacDaTat() {
        LocalDate homNay = LocalDate.now();
        dipLe("Lễ thử đã qua", homNay.minusDays(10), homNay.minusDays(9), "0.15", true);
        dipLe("Lễ thử đã tắt", homNay.plusDays(2), homNay.plusDays(2), "0.15", false);

        String traLoi = assistantService.answer("Có khuyến mãi gì không?").answer();
        String chiDan = contextBuilder.dungChiDan();

        assertThat(traLoi).doesNotContain("Lễ thử đã qua", "Lễ thử đã tắt");
        assertThat(chiDan).doesNotContain("Lễ thử đã qua", "Lễ thử đã tắt");
    }

    @Test
    @DisplayName("UT-NL-37 Chỉ dẫn gửi mô hình ngôn ngữ có khối ưu đãi dịp lễ kèm dịp sắp tới")
    void chiDanCoKhoiDipLe() {
        LocalDate ngay = LocalDate.now().plusDays(3);
        dipLe("Lễ thử chỉ dẫn", ngay, ngay, "0.12", true);

        String chiDan = contextBuilder.dungChiDan();

        assertThat(chiDan)
                .contains("[Ưu đãi dịp lễ]", "không cộng dồn")
                .contains("Lễ thử chỉ dẫn, " + ngay.format(NGAY) + ": giảm 12%");
    }
}
