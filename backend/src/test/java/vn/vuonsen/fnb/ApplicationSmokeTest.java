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

        /*
         * Lọc theo danh mục: kiểm tính chất chứ không kiểm số lượng.
         *
         * Trước đây dòng này đòi đúng sáu món trong danh mục "lau". Thêm một trăm món vào
         * thực đơn là trượt ngay, dù bộ lọc không hỏng chỗ nào. Đòi một con số cố định
         * gắn với dữ liệu mẫu thì cứ mở rộng dữ liệu là phải sửa test, mà sửa mãi thì
         * người ta quen tay sửa cho qua chứ không đọc xem nó báo gì.
         *
         * Điều thật sự cần kiểm: có trả về món, và không lọt món của danh mục khác.
         */
        var monLau = dishRepository.search("lau", null);
        assertThat(monLau).isNotEmpty();
        assertThat(monLau).allSatisfy(mon ->
                assertThat(mon.getCategory().getCode()).isEqualTo("lau"));
    }
}
