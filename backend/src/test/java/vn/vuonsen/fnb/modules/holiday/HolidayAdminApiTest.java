package vn.vuonsen.fnb.modules.holiday;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Kiểm thử API quản trị dịp lễ qua tầng web: kiểm tra dữ liệu nhập và phân quyền.
 *
 * Câu báo lỗi tiếng Việt được đọc theo UTF-8, vì phản hồi giả của MockMvc mặc định đọc theo
 * ISO-8859-1 và làm vỡ dấu.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HolidayAdminApiTest {

    private static final String URL = "/api/v1/admin/holidays";

    @Autowired
    private MockMvc mockMvc;

    private static String than(String ten, String tu, String den, String tiLe) {
        return """
                {"name": %s, "startDate": %s, "endDate": %s, "discountRate": %s, "active": true}
                """.formatted(ten, tu, den, tiLe);
    }

    private static ResultMatcher coChu(String chu) {
        return ketQua -> assertThat(ketQua.getResponse().getContentAsString(StandardCharsets.UTF_8)).contains(chu);
    }

    @Test
    @DisplayName("UT-NL-28 Mức giảm 25% bị từ chối với câu báo tiếng Việt")
    @WithMockUser(roles = "ADMIN")
    void tuChoiMucTren20() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(than("\"Lễ thử\"", "\"2031-05-10\"", "\"2031-05-10\"", "0.25")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.discountRate").exists())
                .andExpect(coChu("Mức giảm tối đa là 20%"));
    }

    @Test
    @DisplayName("UT-NL-29 Mức giảm 5% bị từ chối với câu báo tiếng Việt")
    @WithMockUser(roles = "ADMIN")
    void tuChoiMucDuoi10() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(than("\"Lễ thử\"", "\"2031-05-10\"", "\"2031-05-10\"", "0.05")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.discountRate").exists())
                .andExpect(coChu("Mức giảm tối thiểu là 10%"));
    }

    @Test
    @DisplayName("UT-NL-30 Bỏ trống tên và ngày bắt đầu thì bị từ chối")
    @WithMockUser(roles = "ADMIN")
    void tuChoiThieuDuLieu() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(than("\"\"", "null", "\"2031-05-10\"", "0.15")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.startDate").exists());
    }

    @Test
    @DisplayName("UT-NL-31 Quản trị thêm dịp lễ hợp lệ thì nhận mã 201")
    @WithMockUser(roles = "ADMIN")
    void quanTriThemDuoc() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(than("\"Lễ thử hợp lệ\"", "\"2031-05-10\"", "\"2031-05-11\"", "0.15")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("UT-NL-32 Nhân viên không quản lý được dịp lễ")
    @WithMockUser(roles = "STAFF")
    void nhanVienBiChan() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UT-NL-33 Khách chưa đăng nhập bị chặn khỏi API quản trị dịp lễ")
    void khachBiChan() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().is4xxClientError());
    }
}
