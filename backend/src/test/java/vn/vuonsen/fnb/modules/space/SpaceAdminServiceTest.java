package vn.vuonsen.fnb.modules.space;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.space.dto.SpaceRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Kiểm thử quản trị không gian, mỗi test tự hoàn tác nên không làm bẩn dữ liệu mẫu
@SpringBootTest
@Transactional
class SpaceAdminServiceTest {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SpaceRepository spaceRepository;

    private SpaceRequest request(String code, String slug, int min, int max) {
        return new SpaceRequest(code, "Khong gian thu nghiem", slug, SpaceType.OUTDOOR,
                "Mo ta ngan", "Mo ta chi tiet", min, max, new BigDecimal("5000000"),
                "SESSION", null, null, null, null, true, 99, List.of("Bãi đỗ xe", "Máy lạnh"));
    }

    @Test
    @DisplayName("Thêm không gian mới lưu được cả danh sách tiện ích")
    void createsSpaceWithAmenities() {
        var created = spaceService.create(request("KG-TEST", "kg-test", 50, 200));

        assertThat(created.id()).isNotNull();
        assertThat(created.amenities()).containsExactlyInAnyOrder("Bãi đỗ xe", "Máy lạnh");
    }

    @Test
    @DisplayName("Trùng mã không gian thì bị từ chối")
    void rejectsDuplicateCode() {
        String existingCode = spaceRepository.findAllByOrderBySortOrderAsc().get(0).getCode();

        assertThatThrownBy(() -> spaceService.create(request(existingCode, "kg-trung-ma", 50, 200)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    @DisplayName("Sức chứa tối thiểu lớn hơn tối đa thì bị từ chối")
    void rejectsMinGreaterThanMax() {
        assertThatThrownBy(() -> spaceService.create(request("KG-SAI-SUC-CHUA", "kg-sai", 300, 100)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sức chứa tối đa");
    }

    @Test
    @DisplayName("Tính phí theo chòi mà quên khai số khách mỗi chòi thì bị từ chối")
    void rejectsHutPricingWithoutUnitCapacity() {
        SpaceRequest thieuSoKhach = new SpaceRequest(
                "KG-CHOI", "Cum choi thu nghiem", "kg-choi", SpaceType.HUT,
                null, null, 10, 60, new BigDecimal("800000"),
                "HUT", null, null, null, null, true, 99, List.of());

        assertThatThrownBy(() -> spaceService.create(thieuSoKhach))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("số khách mỗi chòi");
    }

    @Test
    @DisplayName("Ngừng kinh doanh thì khách không thấy nữa nhưng trang quản trị vẫn thấy")
    void deactivatedSpaceStaysVisibleToAdminOnly() {
        var created = spaceService.create(request("KG-NGUNG", "kg-ngung", 20, 80));

        spaceService.deactivate(created.id());

        // Danh sách của khách chỉ lấy không gian đang cho thuê
        assertThat(spaceService.search(null, null, null))
                .noneMatch(s -> s.id().equals(created.id()));
        // Trang quản trị vẫn thấy để bật lại khi cần
        assertThat(spaceService.listForAdmin())
                .anyMatch(s -> s.id().equals(created.id()) && !s.active());
    }
}
