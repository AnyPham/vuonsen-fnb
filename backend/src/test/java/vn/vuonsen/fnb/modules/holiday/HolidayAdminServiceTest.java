package vn.vuonsen.fnb.modules.holiday;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.modules.holiday.dto.HolidayDiscountRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Kiểm thử quản trị dịp lễ giảm giá
@SpringBootTest
@Transactional
class HolidayAdminServiceTest {

    private static final LocalDate NGAY = LocalDate.of(2031, 5, 10);

    @Autowired
    private HolidayAdminService service;

    @Autowired
    private HolidayDiscountRepository repository;

    private static HolidayDiscountRequest yeuCau(String ten, LocalDate tu, LocalDate den, String tiLe, Boolean dangApDung) {
        return new HolidayDiscountRequest(ten, tu, den, new BigDecimal(tiLe), dangApDung);
    }

    @Test
    @DisplayName("UT-NL-23 Thêm dịp lễ hợp lệ: tên được cắt khoảng trắng, bỏ trống trạng thái thì mặc định đang áp dụng")
    void themDipLe() {
        var tao = service.create(yeuCau("  Lễ thử thêm  ", NGAY, NGAY.plusDays(1), "0.15", null));

        assertThat(tao.id()).isNotNull();
        assertThat(tao.name()).isEqualTo("Lễ thử thêm");
        assertThat(tao.active()).isTrue();
        assertThat(repository.findById(tao.id())).isPresent();
    }

    @Test
    @DisplayName("UT-NL-24 Ngày kết thúc trước ngày bắt đầu thì bị từ chối")
    void tuChoiNgayNguoc() {
        assertThatThrownBy(() -> service.create(yeuCau("Lễ ngày ngược", NGAY, NGAY.minusDays(1), "0.15", true)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ngày kết thúc không được trước ngày bắt đầu");
    }

    @Test
    @DisplayName("UT-NL-25 Sửa dịp lễ thì cập nhật mức giảm và trạng thái")
    void suaDipLe() {
        var tao = service.create(yeuCau("Lễ thử sửa", NGAY, NGAY, "0.10", true));

        var sua = service.update(tao.id(), yeuCau("Lễ thử sửa", NGAY, NGAY, "0.18", false));

        assertThat(sua.discountRate()).isEqualByComparingTo("0.18");
        assertThat(sua.active()).isFalse();
    }

    @Test
    @DisplayName("UT-NL-26 Xóa dịp lễ; xóa dịp không tồn tại thì báo không tìm thấy")
    void xoaDipLe() {
        var tao = service.create(yeuCau("Lễ thử xóa", NGAY, NGAY, "0.10", true));

        service.delete(tao.id());

        assertThat(repository.findById(tao.id())).isEmpty();
        assertThatThrownBy(() -> service.delete(tao.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("UT-NL-27 Danh sách có đủ 15 dịp lễ mẫu 2026 đến 2028, dịp mới nhất đứng đầu")
    void danhSachDuLieuMau() {
        var ds = service.listAll();

        assertThat(ds).hasSize(15);
        assertThat(ds.get(0).name()).isEqualTo("Quốc khánh");
        assertThat(ds.get(0).startDate()).isEqualTo(LocalDate.of(2028, 9, 2));
    }
}
