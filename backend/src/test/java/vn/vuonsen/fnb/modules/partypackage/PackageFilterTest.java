package vn.vuonsen.fnb.modules.partypackage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử bộ lọc gói tiệc, phần còn thiếu của yêu cầu tab/filter trong đề cương.
 *
 * Kiểm tính chất chứ không kiểm số lượng cố định, cùng lý do đã ghi ở ApplicationSmokeTest:
 * đòi đúng một con số gắn với dữ liệu mẫu thì cứ thêm một gói tiệc là trượt, dù bộ lọc
 * không hỏng chỗ nào. Thứ cần kiểm là mọi gói trả về đều thỏa điều kiện, và gói đáng lẽ
 * phải có thì không bị loại nhầm.
 */
@SpringBootTest
class PackageFilterTest {

    @Autowired
    private PartyPackageRepository packageRepository;

    private static final BigDecimal BA_TRIEU = new BigDecimal("3000000");

    private static List<String> ma(List<PartyPackage> goi) {
        return goi.stream().map(PartyPackage::getCode).toList();
    }

    /*
     * So sánh theo mã gói chứ không theo thực thể. Hai lần gọi repository nằm ngoài một
     * giao dịch chung nên trả về hai bộ đối tượng khác nhau, mà BaseEntity không định
     * nghĩa equals, thành ra so trực tiếp là trượt dù dữ liệu giống hệt.
     */
    @Test
    @DisplayName("Không khai điều kiện nào thì trả về đúng danh sách gói đang bán, đúng thứ tự")
    void khongLocThiTraVeTatCa() {
        assertThat(ma(packageRepository.search(null, null, null)))
                .containsExactlyElementsOf(ma(packageRepository.findByActiveTrueOrderBySortOrderAsc()));
    }

    @Test
    @DisplayName("Lọc theo ngân sách: mọi gói trả về đều không vượt mức giá khách khai")
    void locTheoNganSach() {
        var ketQua = packageRepository.search(BA_TRIEU, null, null);

        assertThat(ketQua).isNotEmpty();
        assertThat(ketQua).allSatisfy(goi ->
                assertThat(goi.getPricePerTable()).isLessThanOrEqualTo(BA_TRIEU));
        // Gói rẻ nhất phải nằm trong kết quả, nếu không là bộ lọc đang loại nhầm
        assertThat(ma(ketQua)).contains("DONG-QUE");
    }

    @Test
    @DisplayName("Lọc theo số món: mọi gói trả về đều đủ số món tối thiểu")
    void locTheoSoMon() {
        var ketQua = packageRepository.search(null, 8, null);

        assertThat(ketQua).isNotEmpty();
        assertThat(ketQua).allSatisfy(goi ->
                assertThat(goi.getDishCount()).isGreaterThanOrEqualTo(8));
        assertThat(ma(ketQua)).doesNotContain("DONG-QUE");   // gói 7 món
    }

    /*
     * Ngưỡng 8 giờ là mốc trọn ngày, lấy theo app.booking.full-day-package-hours. Kiểm
     * riêng mốc này vì nó là con số dùng chung với phần tính tiền: để hai nơi lệch nhau
     * thì khách lọc ra một gói "trọn ngày" mà lúc tính tiền lại không được tính trọn ngày.
     */
    @Test
    @DisplayName("Lọc trọn ngày chỉ giữ gói dùng không gian từ 8 giờ trở lên")
    void locTronNgay() {
        var ketQua = packageRepository.search(null, null, 8);

        assertThat(ketQua).isNotEmpty();
        assertThat(ketQua).allSatisfy(goi ->
                assertThat(goi.getHoursIncluded()).isGreaterThanOrEqualTo(8));
    }

    @Test
    @DisplayName("Khai nhiều điều kiện thì gói trả về phải thỏa đồng thời tất cả")
    void nhieuDieuKienCungLuc() {
        var ketQua = packageRepository.search(new BigDecimal("5000000"), 8, 4);

        assertThat(ketQua).isNotEmpty();
        assertThat(ketQua).allSatisfy(goi -> {
            assertThat(goi.getPricePerTable()).isLessThanOrEqualTo(new BigDecimal("5000000"));
            assertThat(goi.getDishCount()).isGreaterThanOrEqualTo(8);
            assertThat(goi.getHoursIncluded()).isGreaterThanOrEqualTo(4);
        });
    }

    /*
     * Không có gói nào khớp là tình huống hợp lệ, không phải lỗi. Giao diện dựa vào danh
     * sách rỗng để hiện câu gợi ý khách nới điều kiện, nên ở đây phải là rỗng chứ không
     * phải ném ngoại lệ hay lặng lẽ bỏ qua điều kiện mà trả về cả danh sách.
     */
    @Test
    @DisplayName("Không gói nào khớp thì trả về danh sách rỗng, không phải trả về tất cả")
    void khongKhopThiRong() {
        assertThat(packageRepository.search(new BigDecimal("1000"), null, null)).isEmpty();
    }

    /*
     * Kiểm thẳng tính chất "mọi gói trả về đều đang bán", không đi vòng qua việc liệt kê
     * các gói đã ngừng bán rồi đòi kết quả không chứa chúng. Dữ liệu mẫu hiện không có
     * gói nào ngừng bán, nên cách đi vòng đó so với một danh sách rỗng và không kiểm
     * được gì; tính chất này thì luôn kiểm được, kể cả sau khi quản trị ngừng bán một gói.
     */
    @Test
    @DisplayName("Kết quả lọc chỉ gồm gói đang bán")
    void chiGomGoiDangBan() {
        assertThat(packageRepository.search(null, null, null))
                .isNotEmpty()
                .allSatisfy(goi -> assertThat(goi.isActive()).isTrue());
    }
}
