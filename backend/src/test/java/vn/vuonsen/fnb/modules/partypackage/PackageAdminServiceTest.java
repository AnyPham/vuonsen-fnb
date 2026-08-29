package vn.vuonsen.fnb.modules.partypackage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.partypackage.dto.PackageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Kiểm thử quản trị gói tiệc
@SpringBootTest
@Transactional
class PackageAdminServiceTest {

    @Autowired
    private PackageAdminService packageAdminService;

    @Autowired
    private PartyPackageRepository packageRepository;

    private PackageRequest request(String code, String name) {
        return new PackageRequest(code, name, null, new BigDecimal("3500000"),
                8, 4, false, true, 0, List.of("Trang trí sân khấu", "MC dẫn chương trình"));
    }

    @Test
    @DisplayName("Thêm gói tiệc mới lưu được cả danh sách dịch vụ đi kèm")
    void createsPackageWithFeatures() {
        var created = packageAdminService.create(request("GOI-TEST", "Gói thử nghiệm"));

        assertThat(created.id()).isNotNull();
        // Bảng package_features để sort_order mặc định bằng 0 cho gói thêm từ trang quản trị,
        // nên chỉ kiểm tra đủ dịch vụ chứ không kiểm tra thứ tự
        assertThat(created.features())
                .containsExactlyInAnyOrder("Trang trí sân khấu", "MC dẫn chương trình");
    }

    @Test
    @DisplayName("Trùng mã gói thì bị từ chối")
    void rejectsDuplicateCode() {
        String existingCode = packageRepository.findAllByOrderBySortOrderAsc().get(0).getCode();

        assertThatThrownBy(() -> packageAdminService.create(request(existingCode, "Gói trùng mã")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    @DisplayName("Sửa gói mà giữ nguyên mã của chính nó thì không báo trùng")
    void keepingOwnCodeIsNotDuplicate() {
        var created = packageAdminService.create(request("GOI-SUA", "Gói sẽ sửa"));

        var updated = packageAdminService.update(created.id(),
                request("GOI-SUA", "Gói đã đổi tên"));

        assertThat(updated.name()).isEqualTo("Gói đã đổi tên");
    }

    @Test
    @DisplayName("Ngừng bán thì gói biến mất khỏi danh sách của khách")
    void deactivatedPackageDisappearsFromPublicList() {
        var created = packageAdminService.create(request("GOI-NGUNG", "Gói sắp ngừng"));

        packageAdminService.deactivate(created.id());

        assertThat(packageRepository.findByActiveTrueOrderBySortOrderAsc())
                .noneMatch(p -> p.getId().equals(created.id()));
        // Trang quản trị vẫn thấy để bật bán lại khi cần
        assertThat(packageAdminService.listAll())
                .anyMatch(p -> p.id().equals(created.id()) && !p.active());
    }

    @Test
    @DisplayName("Dòng dịch vụ để trống bị bỏ qua khi lưu")
    void blankFeatureLinesAreDropped() {
        var created = packageAdminService.create(new PackageRequest(
                "GOI-TRONG", "Gói có dòng trống", null, new BigDecimal("2000000"),
                6, 4, false, true, 0, List.of("Có nội dung", "   ", "")));

        assertThat(created.features()).containsExactly("Có nội dung");
    }
}
