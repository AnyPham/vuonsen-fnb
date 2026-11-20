package vn.vuonsen.fnb.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Lỗi KAN-65: từ khóa % và _ khớp toàn bộ thực đơn vì đi thẳng vào mệnh đề LIKE.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TuKhoaTimKiemTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Thoát ký tự đại diện %, _ và chính ký tự thoát")
    void thoatKyTuDaiDien() {
        assertThat(TuKhoaTimKiem.choLike("%")).isEqualTo("!%");
        assertThat(TuKhoaTimKiem.choLike("_")).isEqualTo("!_");
        assertThat(TuKhoaTimKiem.choLike("50%_off!")).isEqualTo("50!%!_off!!");
        assertThat(TuKhoaTimKiem.choLike("  gỏi  ")).isEqualTo("gỏi");
    }

    @Test
    @DisplayName("Từ khóa rỗng hoặc chỉ có dấu cách thì không lọc")
    void tuKhoaRong() {
        assertThat(TuKhoaTimKiem.choLike(null)).isNull();
        assertThat(TuKhoaTimKiem.choLike("   ")).isNull();
    }

    @Test
    @DisplayName("Tìm món với từ khóa % hoặc _ trả danh sách rỗng")
    void timMonKyTuDaiDien() throws Exception {
        mockMvc.perform(get("/api/v1/menu/dishes").param("keyword", "%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/v1/menu/dishes").param("keyword", "_"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Từ khóa thường vẫn tìm ra món như trước")
    void timMonTuKhoaThuong() throws Exception {
        mockMvc.perform(get("/api/v1/menu/dishes").param("keyword", "gỏi"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()", greaterThan(0)));
    }
}
