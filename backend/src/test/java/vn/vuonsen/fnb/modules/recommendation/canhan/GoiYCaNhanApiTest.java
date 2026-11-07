package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.vuonsen.fnb.modules.user.Role;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;
import vn.vuonsen.fnb.security.AppUserDetails;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* Kiểm hai đường dẫn của gợi ý thông minh qua đúng tầng HTTP và phân quyền thật. */
@SpringBootTest
@AutoConfigureMockMvc
class GoiYCaNhanApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GoiYCaNhanService goiYService;

    @BeforeEach
    void lamMoi() {
        goiYService.lamMoi();
    }

    @Test
    @DisplayName("Khách chưa đăng nhập gọi được gợi ý trang chủ và nhận bản nổi bật")
    void khachLaGoiDuoc() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/ca-nhan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.khongGian.kieu").value("NOI_BAT"))
                .andExpect(jsonPath("$.khongGian.items.length()").value(3))
                .andExpect(jsonPath("$.monAn.kieu").value("NOI_BAT"));
    }

    @Test
    @DisplayName("Sở thích là dữ liệu riêng: chưa đăng nhập thì không xem, không sửa được")
    void soThichCanDangNhap() throws Exception {
        mockMvc.perform(get("/api/v1/me/so-thich")).andExpect(status().is4xxClientError());
        mockMvc.perform(put("/api/v1/me/so-thich").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Lưu sở thích xong thì đọc lại đúng, và trang chủ chuyển sang gợi ý riêng")
    void luuSoThichRoiGoiYRieng() throws Exception {
        AppUserDetails khach = new AppUserDetails(userRepository.save(User.builder()
                .fullName("Khách thử sở thích")
                .email("sothich." + System.nanoTime() + "@example.com")
                .passwordHash("khong-dang-nhap-duoc")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build()));

        mockMvc.perform(get("/api/v1/recommendations/ca-nhan").with(user(khach)))
                .andExpect(jsonPath("$.khongGian.kieu").value("NOI_BAT"));

        mockMvc.perform(put("/api/v1/me/so-thich").with(user(khach))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loaiTiec":["WEDDING","BIRTHDAY"],"soKhach":250,
                                 "kieuKhongGian":"NGOAI_TROI","tamGia":"CAO_CAP","anChay":false}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/me/so-thich").with(user(khach)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loaiTiec.length()").value(2))
                .andExpect(jsonPath("$.soKhach").value(250))
                .andExpect(jsonPath("$.kieuKhongGian").value("NGOAI_TROI"))
                .andExpect(jsonPath("$.tamGia").value("CAO_CAP"));

        mockMvc.perform(get("/api/v1/recommendations/ca-nhan").with(user(khach)))
                .andExpect(jsonPath("$.khongGian.kieu").value("CA_NHAN"))
                .andExpect(jsonPath("$.khongGian.canCu.coSoThich").value(true));
    }

    @Test
    @DisplayName("Số khách khai bằng 0 bị từ chối")
    void soKhachKhongHopLe() throws Exception {
        AppUserDetails khach = new AppUserDetails(userRepository.save(User.builder()
                .fullName("Khách thử sai số")
                .email("sothich.sai." + System.nanoTime() + "@example.com")
                .passwordHash("khong-dang-nhap-duoc")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build()));

        mockMvc.perform(put("/api/v1/me/so-thich").with(user(khach))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"soKhach\":0}"))
                .andExpect(status().isBadRequest());
    }
}
