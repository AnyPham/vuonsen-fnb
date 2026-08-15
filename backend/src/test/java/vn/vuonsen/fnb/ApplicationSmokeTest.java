package vn.vuonsen.fnb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import static org.assertj.core.api.Assertions.assertThat;

// Kiểm tra ứng dụng khởi động được và dữ liệu mẫu đã nạp đủ
@SpringBootTest
class ApplicationSmokeTest {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private PartyPackageRepository packageRepository;

    @Test
    @DisplayName("Ứng dụng khởi động được và nạp đủ dữ liệu mẫu")
    void contextLoadsWithSeedData() {
        assertThat(spaceRepository.findByActiveTrueOrderBySortOrderAsc()).hasSize(6);
        assertThat(packageRepository.findByActiveTrueOrderBySortOrderAsc()).hasSize(3);
        assertThat(dishRepository.search(null, null)).hasSizeGreaterThan(20);
        assertThat(dishRepository.search("lau", null)).hasSize(6);
    }
}
