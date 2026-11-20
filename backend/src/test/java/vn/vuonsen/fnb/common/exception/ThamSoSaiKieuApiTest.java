package vn.vuonsen.fnb.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Tham số sai kiểu phải trả 400 kèm tên tham số, không được rơi xuống 500.
 * Bốn đường dẫn dưới đây là đúng bốn chỗ trả 500 khi kiểm lại lỗi KAN-60 ngày 11/11/2026.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ThamSoSaiKieuApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Lịch quản trị: ngày không có thật trả 400, nêu tên tham số tuNgay")
    void lichNgayKhongCoThat() throws Exception {
        mockMvc.perform(get("/api/v1/admin/calendar")
                        .param("tuNgay", "2026-02-30").param("denNgay", "2026-03-01")
                        .with(user("admin@vuonsen.vn").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("tuNgay")));
    }

    @Test
    @DisplayName("Lịch quản trị: 29/02 năm không nhuận và chuỗi không phải ngày đều trả 400")
    void lichNamKhongNhuanVaChuoiSai() throws Exception {
        mockMvc.perform(get("/api/v1/admin/calendar")
                        .param("tuNgay", "2026-03-01").param("denNgay", "2027-02-29")
                        .with(user("admin@vuonsen.vn").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("denNgay")));
        mockMvc.perform(get("/api/v1/admin/calendar")
                        .param("tuNgay", "abc").param("denNgay", "2026-03-01")
                        .with(user("admin@vuonsen.vn").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Kiểm tra buổi trống (công khai): ngày không có thật trả 400")
    void buoiTrongNgayKhongCoThat() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/availability").param("date", "2026-02-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("date")));
    }

    @Test
    @DisplayName("Danh sách đơn quản trị: ngày sai và trạng thái không tồn tại đều trả 400")
    void danhSachDonThamSoSai() throws Exception {
        mockMvc.perform(get("/api/v1/admin/bookings").param("from", "2026-02-30")
                        .with(user("admin@vuonsen.vn").roles("ADMIN")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/bookings").param("status", "ABC")
                        .with(user("admin@vuonsen.vn").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CONFIRMED")));
    }

    @Test
    @DisplayName("Ngày hợp lệ vẫn trả 200, kể cả 29/02 năm nhuận")
    void ngayHopLeVanChay() throws Exception {
        mockMvc.perform(get("/api/v1/admin/calendar")
                        .param("tuNgay", "2028-02-29").param("denNgay", "2028-03-01")
                        .with(user("admin@vuonsen.vn").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
