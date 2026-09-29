package vn.vuonsen.fnb.modules.holiday;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// Kiểm thử tra cứu dịp lễ trên dữ liệu mẫu của migration V13
@SpringBootTest
@Transactional
class HolidayDiscountServiceTest {

    @Autowired
    private HolidayDiscountService service;

    @Autowired
    private HolidayDiscountRepository repository;

    private void dipLe(String ten, LocalDate tu, LocalDate den, String tiLe, boolean dangApDung) {
        repository.save(HolidayDiscount.builder()
                .name(ten).startDate(tu).endDate(den)
                .discountRate(new BigDecimal(tiLe)).active(dangApDung)
                .build());
    }

    @Test
    @DisplayName("UT-NL-17 Tết 2027 từ 06/02 đến 08/02: ngày đầu và ngày cuối giảm 20%, ngày 09/02 thì không")
    void tet2027CoDuBien() {
        assertThat(service.find(LocalDate.of(2027, 2, 6))).hasValueSatisfying(d -> {
            assertThat(d.name()).isEqualTo("Tết Nguyên Đán");
            assertThat(d.rate()).isEqualByComparingTo("0.20");
        });
        assertThat(service.find(LocalDate.of(2027, 2, 8))).isPresent();
        assertThat(service.find(LocalDate.of(2027, 2, 9))).isEmpty();
    }

    @Test
    @DisplayName("UT-NL-18 Có dịp lễ mẫu năm 2028: Tết Nguyên Đán 26/01 đến 28/01, Giỗ Tổ 04/04")
    void coDuLieuNam2028() {
        assertThat(service.find(LocalDate.of(2028, 1, 26))).hasValueSatisfying(d -> {
            assertThat(d.name()).isEqualTo("Tết Nguyên Đán");
            assertThat(d.rate()).isEqualByComparingTo("0.20");
        });
        assertThat(service.find(LocalDate.of(2028, 1, 28))).isPresent();
        assertThat(service.find(LocalDate.of(2028, 4, 4))).hasValueSatisfying(d -> {
            assertThat(d.name()).isEqualTo("Giỗ Tổ Hùng Vương");
            assertThat(d.rate()).isEqualByComparingTo("0.10");
        });
    }

    @Test
    @DisplayName("UT-NL-19 Dịp lễ đã tắt thì không được áp dụng")
    void dipLeTatKhongApDung() {
        LocalDate ngay = LocalDate.of(2031, 3, 10);
        dipLe("Lễ thử đã tắt", ngay, ngay, "0.15", false);

        assertThat(service.find(ngay)).isEmpty();
    }

    @Test
    @DisplayName("UT-NL-20 Hai dịp lễ trùng ngày thì lấy dịp giảm nhiều hơn")
    void trungNgayLayMucCaoHon() {
        LocalDate ngay = LocalDate.of(2031, 3, 20);
        dipLe("Lễ giảm ít", ngay, ngay, "0.10", true);
        dipLe("Lễ giảm nhiều", ngay.minusDays(1), ngay.plusDays(1), "0.18", true);

        assertThat(service.find(ngay)).hasValueSatisfying(d -> {
            assertThat(d.name()).isEqualTo("Lễ giảm nhiều");
            assertThat(d.rate()).isEqualByComparingTo("0.18");
        });
    }

    @Test
    @DisplayName("UT-NL-21 Không có ngày thì không tra dịp lễ")
    void khongCoNgay() {
        assertThat(service.find(null)).isEmpty();
    }

    @Test
    @DisplayName("UT-NL-22 Danh sách dịp sắp tới bỏ dịp đã qua và dịp đã tắt, dịp gần nhất đứng đầu")
    void danhSachSapToi() {
        LocalDate homNay = LocalDate.of(2031, 1, 1);
        dipLe("Lễ đã qua", LocalDate.of(2030, 12, 1), LocalDate.of(2030, 12, 2), "0.10", true);
        dipLe("Lễ đã tắt", LocalDate.of(2031, 1, 5), LocalDate.of(2031, 1, 5), "0.10", false);
        dipLe("Lễ tháng ba", LocalDate.of(2031, 3, 1), LocalDate.of(2031, 3, 1), "0.10", true);
        dipLe("Lễ giữa tháng một", LocalDate.of(2031, 1, 15), LocalDate.of(2031, 1, 15), "0.10", true);

        assertThat(service.sapToi(homNay, 10))
                .extracting(HolidayDiscount::getName)
                .containsExactly("Lễ giữa tháng một", "Lễ tháng ba");
    }
}
