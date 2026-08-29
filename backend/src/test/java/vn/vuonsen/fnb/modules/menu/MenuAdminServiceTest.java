package vn.vuonsen.fnb.modules.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.menu.dto.DishRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Kiểm thử quản trị thực đơn, mỗi test tự hoàn tác nên không làm bẩn dữ liệu mẫu
@SpringBootTest
@Transactional
class MenuAdminServiceTest {

    @Autowired
    private MenuAdminService menuAdminService;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private DishCategoryRepository categoryRepository;

    private Long anyCategoryId() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc().get(0).getId();
    }

    @Test
    @DisplayName("Món không có giá mà cũng không ghi chú cách tính thì bị từ chối")
    void rejectsDishWithNeitherPriceNorNote() {
        DishRequest request = new DishRequest(
                anyCategoryId(), "Món thiếu giá", null, null, null, null, false, true, 0);

        assertThatThrownBy(() -> menuAdminService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ghi chú cách tính giá");
    }

    @Test
    @DisplayName("Món tính giá linh hoạt chỉ cần ghi chú là thêm được")
    void acceptsDishWithPriceNoteOnly() {
        DishRequest request = new DishRequest(
                anyCategoryId(), "Tôm hùm hấp", null, null, "Theo cân", null, false, true, 0);

        var created = menuAdminService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.price()).isNull();
        assertThat(created.priceNote()).isEqualTo("Theo cân");
    }

    @Test
    @DisplayName("Ngừng bán thì món biến mất khỏi thực đơn của khách nhưng vẫn còn trong database")
    void deactivatedDishDisappearsFromPublicMenu() {
        var created = menuAdminService.create(new DishRequest(
                anyCategoryId(), "Món sắp ngừng bán", null,
                new BigDecimal("150000"), null, null, false, true, 0));

        menuAdminService.deactivate(created.id());

        // Thực đơn của khách chỉ lấy món đang bán
        assertThat(dishRepository.search(null, "Món sắp ngừng bán")).isEmpty();
        // Nhưng bản ghi vẫn còn để các đơn cũ tra cứu được tên món
        assertThat(dishRepository.findById(created.id())).isPresent();
        assertThat(dishRepository.findById(created.id()).get().isAvailable()).isFalse();
    }

    @Test
    @DisplayName("Trang quản trị thấy cả món đã ngừng bán")
    void adminListIncludesUnavailableDishes() {
        var created = menuAdminService.create(new DishRequest(
                anyCategoryId(), "Món chỉ quản trị thấy", null,
                new BigDecimal("99000"), null, null, false, false, 0));

        assertThat(menuAdminService.listAll())
                .anyMatch(d -> d.id().equals(created.id()) && !d.available());
    }
}
