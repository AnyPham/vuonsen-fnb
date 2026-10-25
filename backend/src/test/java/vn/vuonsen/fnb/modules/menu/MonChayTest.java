package vn.vuonsen.fnb.modules.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử cột đánh dấu món chay và bộ lọc theo cột đó.
 *
 * Lý do có phần này: trước khi có cột vegetarian, trợ lý tư vấn phải tự đoán món nào chay
 * theo tên món, và nó đoán sai thật. Chạy thử với khóa Gemini thật, trợ lý
 * kể Nộm hoa chuối là món chay, trong khi nguyên liệu ghi rõ có nước mắm chua ngọt. Khách
 * ăn chay trường gọi phải món đó là lỗi của nhà hàng.
 *
 * Nên các phép kiểm dưới đây soi vào đúng chỗ đó: không món nào có nguyên liệu mặn mà lại
 * bị đánh dấu chay.
 */
@SpringBootTest
class MonChayTest {

    @Autowired
    private DishRepository dishRepository;

    /* Nguyên liệu không bao giờ được xuất hiện trong một món đánh dấu chay. */
    private static final List<String> NGUYEN_LIEU_MAN = List.of(
            "thịt", "ba chỉ", "sườn", "xương", "gà", "vịt", "bò", "heo", "ếch",
            "tôm", "cá", "mực", "nghêu", "sò", "ốc", "cua", "ghẹ", "lươn",
            "dầu hào", "trứng");

    /*
     * Những cụm có tên con vật nhưng là nguyên liệu thực vật. Nấm đùi gà là tên một loại
     * nấm chứ không phải thịt gà; bỏ qua bước này thì phép kiểm báo sai ở ba món nấm.
     */
    private static final List<String> CUM_THUC_VAT = List.of(
            "nấm đùi gà", "nấm bào ngư", "bơ sáp", "trái bơ", "sữa đậu nành",
            "sa tế chay", "nước mắm chay");

    private static String boCumThucVat(String nguyenLieu) {
        String con = nguyenLieu.toLowerCase();
        for (String cum : CUM_THUC_VAT) {
            con = con.replace(cum, " ");
        }
        return con;
    }

    /*
     * Dò nguyên liệu theo nguyên từ, không dò theo chuỗi con.
     *
     * Bản đầu của phép kiểm này dùng contains và báo sai ba món: "Trái cây theo mùa" bị
     * cho là có "heo" vì chữ "theo", "xoài cát" bị cho là có "cá" vì chữ "cát". Đúng cái
     * bẫy đã vấp một lần ở script phân loại, lần đó là do \b của JavaScript không hiểu
     * chữ có dấu. Ở đây phải tự chặn hai đầu bằng lớp chữ cái Unicode.
     */
    private static boolean coNguyenTu(String vanBan, String tu) {
        return Pattern.compile("(?<!\\p{L})" + Pattern.quote(tu) + "(?!\\p{L})",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(vanBan).find();
    }

    @Test
    @DisplayName("Thực đơn có món chay để bán")
    void coMonChay() {
        assertThat(dishRepository.search(null, null, true))
                .as("dữ liệu mẫu phải có món chay, nếu không thì bộ lọc vô nghĩa")
                .isNotEmpty();
    }

    @Test
    @DisplayName("Mọi món trả về khi lọc chay đều thật sự được đánh dấu chay")
    void locChayChiTraVeMonChay() {
        assertThat(dishRepository.search(null, null, true))
                .allSatisfy(mon -> assertThat(mon.isVegetarian()).isTrue());
    }

    /*
     * Phép kiểm quan trọng nhất của lớp này: soi thẳng vào nguyên liệu chứ không tin vào
     * cờ. Đánh dấu sai một món là khách ăn chay gọi phải món có nước mắm hoặc thịt.
     */
    @Test
    @DisplayName("Không món chay nào có nguyên liệu mặn")
    void monChayKhongCoNguyenLieuMan() {
        assertThat(dishRepository.search(null, null, true)).allSatisfy(mon -> {
            String nl = boCumThucVat(mon.getIngredients() == null ? "" : mon.getIngredients());
            for (String man : NGUYEN_LIEU_MAN) {
                assertThat(coNguyenTu(nl, man))
                        .as("món %s bị đánh dấu chay nhưng nguyên liệu có \"%s\": %s",
                                mon.getName(), man, mon.getIngredients())
                        .isFalse();
            }
        });
    }

    /*
     * Nước mắm chay và nước mắm thường chỉ khác nhau một chữ, mà khác nhau hoàn toàn về
     * bản chất. Đây đúng là chỗ đã làm trợ lý đoán sai, nên canh riêng.
     */
    @Test
    @DisplayName("Món chay chỉ được dùng nước mắm chay, không dùng nước mắm thường")
    void monChayKhongDungNuocMamThuong() {
        assertThat(dishRepository.search(null, null, true)).allSatisfy(mon -> {
            String nl = (mon.getIngredients() == null ? "" : mon.getIngredients()).toLowerCase();
            assertThat(nl.replace("nước mắm chay", " "))
                    .as("món %s bị đánh dấu chay nhưng có nước mắm thường", mon.getName())
                    .doesNotContain("nước mắm");
        });
    }

    @Test
    @DisplayName("Nộm hoa chuối không phải món chay vì có nước mắm chua ngọt")
    void nomHoaChuoiKhongPhaiMonChay() {
        var mon = dishRepository.findBySlugForDetail("nom-hoa-chuoi");
        assertThat(mon).isPresent();
        assertThat(mon.get().isVegetarian()).isFalse();
    }

    @Test
    @DisplayName("Đồ uống có cồn không tính là món chay dù không có thịt")
    void doUongCoConKhongTinhLaChay() {
        assertThat(dishRepository.search(null, null, true))
                .extracting(Dish::getName)
                .noneMatch(ten -> ten.toLowerCase().matches(".*(bia|rượu|vang).*"));
    }

    @Test
    @DisplayName("Không khai điều kiện chay thì trả về cả món chay lẫn món mặn")
    void khongLocThiTraVeCaHai() {
        var tatCa = dishRepository.search(null, null, null);
        assertThat(tatCa).anyMatch(Dish::isVegetarian);
        assertThat(tatCa).anyMatch(mon -> !mon.isVegetarian());
    }

    @Test
    @DisplayName("Lọc chay kết hợp được với lọc danh mục")
    void ketHopVoiLocDanhMuc() {
        var ketQua = dishRepository.search("trangmieng", null, true);

        assertThat(ketQua).isNotEmpty();
        assertThat(ketQua).allSatisfy(mon -> {
            assertThat(mon.isVegetarian()).isTrue();
            assertThat(mon.getCategory().getCode()).isEqualTo("trangmieng");
        });
    }
}
