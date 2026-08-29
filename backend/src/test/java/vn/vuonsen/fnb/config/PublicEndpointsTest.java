package vn.vuonsen.fnb.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Kiểm tra phân quyền: khách chưa đăng nhập phải xem được phần công khai
// và phải bị chặn ở khu quản trị.
@SpringBootTest
@AutoConfigureMockMvc
class PublicEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Khách chưa đăng nhập xem được toàn bộ nội dung công khai")
    void guestCanReachPublicEndpoints() throws Exception {
        List<String> publicUrls = List.of(
                "/api/v1/spaces",
                "/api/v1/spaces/types",
                "/api/v1/menu/categories",
                "/api/v1/menu/dishes",
                "/api/v1/packages",
                "/api/v1/gallery",
                "/api/v1/reviews",
                // Thiếu dòng này thì form đặt tiệc không nạp được loại sự kiện và buổi
                "/api/v1/bookings/options");

        for (String url : publicUrls) {
            mockMvc.perform(get(url))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Khách chưa đăng nhập bị chặn ở khu quản trị")
    void guestCannotReachAdminEndpoints() throws Exception {
        List<String> adminUrls = List.of(
                "/api/v1/admin/bookings",
                "/api/v1/admin/reviews",
                "/api/v1/admin/dishes",
                "/api/v1/admin/packages",
                "/api/v1/admin/spaces");

        for (String url : adminUrls) {
            mockMvc.perform(get(url))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Test
    @DisplayName("Khách chưa đăng nhập bị chặn ở trang hồ sơ cá nhân")
    void guestCannotReachProfile() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().is4xxClientError());
    }
}
