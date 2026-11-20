package vn.vuonsen.fnb.modules.dishorder;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Lỗi KAN-64: số lượng mỗi món chỉ bị chặn dưới, gửi thẳng API 100.000 phần vẫn tạo đơn.
 * Kiểm theo giá trị biên của miền hợp lệ [1, 200].
 */
@SpringBootTest
@AutoConfigureMockMvc
class GioiHanSoLuongMonTest {

    private static final Validator KIEM_TRA = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0} phần là hợp lệ")
    @ValueSource(ints = {1, 2, 199, 200})
    @DisplayName("Số lượng trong miền [1, 200] được nhận")
    void soLuongHopLe(int soLuong) {
        assertThat(KIEM_TRA.validate(new DishOrderRequest.ItemRequest(1L, soLuong))).isEmpty();
    }

    @ParameterizedTest(name = "{0} phần bị từ chối")
    @ValueSource(ints = {-1, 0, 201, 100000})
    @DisplayName("Số lượng ngoài miền [1, 200] bị từ chối")
    void soLuongNgoaiMien(int soLuong) {
        assertThat(KIEM_TRA.validate(new DishOrderRequest.ItemRequest(1L, soLuong))).isNotEmpty();
    }

    @Test
    @DisplayName("Gửi thẳng API 100.000 phần nhận 400 kèm câu báo giới hạn, không tạo đơn")
    void apiTuChoiSoLuongQuaLon() throws Exception {
        String than = """
                {"fulfillmentType":"DELIVERY","customerName":"Khách kiểm thử","customerPhone":"0901234567",
                 "deliveryAddress":"12 Nguyễn Huệ, Quận 1","serveAt":"%s",
                 "items":[{"dishId":1,"quantity":100000}]}
                """.formatted(LocalDateTime.now().plusDays(5).withNano(0));
        mockMvc.perform(post("/api/v1/dish-orders").contentType(MediaType.APPLICATION_JSON).content(than)
                        .with(user("khach@vuonsen.vn").roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("200")));
    }
}
