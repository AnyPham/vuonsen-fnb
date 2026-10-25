package vn.vuonsen.fnb.modules.assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/*
 * Canh phần dựng chỉ dẫn hệ thống gửi cho mô hình ngôn ngữ.
 *
 * Lý do lớp này ra đời là một lỗi đã mắc thật. Chỉ dẫn được dựng bằng
 * String.formatted, nên thêm một câu có dấu phần trăm kiểu "giảm 20% dịp lễ" là cả lệnh
 * dựng ném UnknownFormatConversionException. Trong Java, phải viết %% mới ra một dấu %.
 *
 * Điều khiến lỗi này nguy hiểm hơn mức bình thường: trợ lý bắt mọi ngoại lệ rồi lặng lẽ
 * lùi về cơ chế dự phòng, nên nhìn từ ngoài vào website vẫn trả lời bình thường, chỉ là
 * không bao giờ gọi tới mô hình nữa. Không có phép kiểm nào ở đây thì lỗi nằm im rất lâu.
 */
@SpringBootTest
class SystemContextBuilderTest {

    @Autowired
    private SystemContextBuilder builder;

    @Test
    @DisplayName("Dựng được chỉ dẫn mà không ném lỗi định dạng chuỗi")
    void dungDuocChiDan() {
        assertThatCode(() -> builder.dungChiDan()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Chỉ dẫn mang đủ dữ liệu thật để mô hình trả lời")
    void chiDanMangDuDuLieu() {
        String chiDan = builder.dungChiDan();

        assertThat(chiDan)
                .contains("Sảnh Ven Sông")
                .contains("Gói Sen Vàng")
                .contains("món chay");
    }

    /*
     * Dấu phần trăm trong chỉ dẫn là chỗ dễ sai nhất, vì câu nào nói về giảm giá cũng
     * cần tới nó. Chỉ dẫn dựng xong phải còn dấu % thật chứ không phải %% chưa được thay.
     */
    @Test
    @DisplayName("Dấu phần trăm hiện đúng một dấu, không sót %% chưa thay")
    void dauPhanTramDungDinhDang() {
        String chiDan = builder.dungChiDan();

        assertThat(chiDan).contains("%");
        assertThat(chiDan).doesNotContain("%%");
    }

    /*
     * Luật khách dễ hiểu nhầm nhất: nghe giảm 20% dịp lễ rồi tưởng được cộng thêm 5% đặt
     * sớm. Chỉ dẫn phải mang luật này thì mô hình mới nói ra được.
     */
    @Test
    @DisplayName("Chỉ dẫn nói rõ ưu đãi dịp lễ không cộng dồn với đặt sớm")
    void chiDanNoiRoLuatKhongCongDon() {
        assertThat(builder.dungChiDan()).contains("không cộng dồn");
    }
}
