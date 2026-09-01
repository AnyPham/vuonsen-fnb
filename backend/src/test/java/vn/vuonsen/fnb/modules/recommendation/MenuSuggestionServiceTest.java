package vn.vuonsen.fnb.modules.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Kiểm thử phần gợi ý thực đơn cho gói tiệc
@SpringBootTest
@Transactional
class MenuSuggestionServiceTest {

    @Autowired
    private MenuSuggestionService menuSuggestionService;

    @Autowired
    private PartyPackageRepository packageRepository;

    @Autowired
    private DishRepository dishRepository;

    private PartyPackage goi(String code) {
        return packageRepository.findByCode(code).orElseThrow();
    }

    private BigDecimal tongGia(List<Dish> monAn) {
        return monAn.stream()
                .map(Dish::getPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    @DisplayName("Số món gợi ý đúng bằng số món ghi trong gói tiệc")
    void suggestsExactlyThePackageDishCount() {
        for (String code : List.of("DONG-QUE", "SEN-VANG", "THUONG-UYEN")) {
            PartyPackage pkg = goi(code);
            var thucDon = menuSuggestionService.suggestMenu(EventType.WEDDING, pkg);

            assertThat(thucDon)
                    .as("gói %s ghi %d món", code, pkg.getDishCount())
                    .hasSize(pkg.getDishCount());
        }
    }

    @Test
    @DisplayName("Thực đơn trải đều các danh mục, không dồn hết vào một nhóm")
    void spreadsAcrossCategories() {
        var thucDon = menuSuggestionService.suggestMenu(EventType.WEDDING, goi("SEN-VANG"));

        long soDanhMuc = thucDon.stream()
                .map(d -> d.getCategory().getId())
                .distinct()
                .count();

        // Gói 8 món mà chỉ nằm trong 1-2 danh mục thì thực đơn vô lý
        assertThat(soDanhMuc).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Họp mặt gia đình có lẩu nướng, tiệc cưới thì không")
    void hotpotFitsFamilyGatheringNotWedding() {
        var giaDinh = menuSuggestionService.suggestMenu(EventType.FAMILY, goi("DONG-QUE"));
        var tiecCuoi = menuSuggestionService.suggestMenu(EventType.WEDDING, goi("DONG-QUE"));

        // Bảng dish_event_types chấm lẩu 5 điểm cho họp mặt, 2 điểm cho tiệc cưới.
        // Lẩu khó phục vụ đồng loạt nhiều bàn nên nhà hàng ít xếp vào tiệc cưới.
        assertThat(demLau(giaDinh))
                .as("Thực đơn họp mặt gia đình phải có lẩu hoặc món nướng")
                .isGreaterThan(0);
        assertThat(demLau(tiecCuoi))
                .as("Thực đơn tiệc cưới không nên có lẩu")
                .isZero();
    }

    @Test
    @DisplayName("Thực đơn nào cũng có đủ khai vị, món chính và tráng miệng")
    void alwaysCoversTheThreeCoreCategories() {
        for (EventType loai : EventType.values()) {
            var thucDon = menuSuggestionService.suggestMenu(loai, goi("DONG-QUE"));
            var cacDanhMuc = thucDon.stream().map(d -> d.getCategory().getCode()).toList();

            assertThat(cacDanhMuc)
                    .as("thực đơn cho %s", loai.getLabel())
                    .contains("khaivi", "chinh", "trangmieng");
        }
    }

    private long demLau(List<Dish> thucDon) {
        return thucDon.stream().filter(d -> d.getCategory().getCode().equals("lau")).count();
    }

    @Test
    @DisplayName("Thực đơn của gói đắt tiền có giá trị cao hơn gói rẻ")
    void expensivePackageGetsMoreValuableMenu() {
        var goiRe = menuSuggestionService.suggestMenu(EventType.WEDDING, goi("DONG-QUE"));
        var goiCaoCap = menuSuggestionService.suggestMenu(EventType.WEDDING, goi("THUONG-UYEN"));

        // So tổng chứ không so trung bình. Gói nhiều món phải lấy thêm tráng miệng
        // và đồ uống, hai nhóm này vốn rẻ nên kéo giá trung bình xuống, trong khi
        // giá trị cả mâm vẫn cao hơn.
        assertThat(tongGia(goiCaoCap))
                .as("Gói Thượng Uyển 6,8 triệu một mâm phải có thực đơn giá trị hơn Gói Đồng Quê 2,9 triệu")
                .isGreaterThan(tongGia(goiRe));
    }

    @Test
    @DisplayName("Trong cùng danh mục, gói đắt tiền chọn món có giá cao hơn")
    void expensivePackagePicksPricierWithinSameCategory() {
        var goiRe = menuSuggestionService.suggestMenu(EventType.WEDDING, goi("DONG-QUE"));
        var goiCaoCap = menuSuggestionService.suggestMenu(EventType.WEDDING, goi("THUONG-UYEN"));

        assertThat(tongGia(monChinh(goiCaoCap)))
                .as("Món chính của gói cao cấp phải đắt hơn")
                .isGreaterThan(tongGia(monChinh(goiRe)));
    }

    private List<Dish> monChinh(List<Dish> thucDon) {
        return thucDon.stream().filter(d -> d.getCategory().getCode().equals("chinh")).toList();
    }

    @Test
    @DisplayName("Món đã ngừng bán không được đưa vào thực đơn gợi ý")
    void skipsUnavailableDishes() {
        // Ngừng bán món bán chạy nhất của danh mục món chính
        Dish caLoc = dishRepository.search(null, "Cá lóc nướng trui").get(0);
        caLoc.setAvailable(false);
        dishRepository.saveAndFlush(caLoc);

        var thucDon = menuSuggestionService.suggestMenu(EventType.WEDDING, goi("THUONG-UYEN"));

        assertThat(thucDon).noneMatch(d -> d.getId().equals(caLoc.getId()));
    }

    @Test
    @DisplayName("Gói không ghi số món thì trả về thực đơn rỗng chứ không báo lỗi")
    void returnsEmptyWhenPackageHasNoDishCount() {
        PartyPackage pkg = goi("SEN-VANG");
        pkg.setDishCount(null);

        assertThat(menuSuggestionService.suggestMenu(EventType.WEDDING, pkg)).isEmpty();
    }
}
