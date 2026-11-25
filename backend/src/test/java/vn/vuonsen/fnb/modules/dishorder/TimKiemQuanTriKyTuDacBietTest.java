package vn.vuonsen.fnb.modules.dishorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderResponse;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.payment.PaymentService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Ô tìm kiếm của khu quản trị dùng chung cách thoát ký tự LIKE với thực đơn (KAN-65).
 * Có dữ liệu thật trong bảng rồi mới tìm, để chứng minh % và _ không còn khớp mọi dòng.
 */
@SpringBootTest
@Transactional
class TimKiemQuanTriKyTuDacBietTest {

    @Autowired
    private DishOrderService donMon;

    @Autowired
    private PaymentService thanhToan;

    @Autowired
    private DishRepository dishRepository;

    private DishOrderResponse taoDon(String ten) {
        Long monId = dishRepository.search(null, null).get(0).getId();
        return donMon.create(new DishOrderRequest(FulfillmentType.DELIVERY, ten, "0901234567", null,
                "5 Lê Lợi, Quận 1", null, LocalDate.now().plusDays(8).atTime(11, 30), null,
                List.of(new DishOrderRequest.ItemRequest(monId, 1))), null);
    }

    @Test
    @DisplayName("Danh sách đơn món: từ khóa % và _ không còn trả về mọi đơn")
    void donMonKyTuDaiDien() {
        taoDon("Khách tìm kiếm A");
        taoDon("Khách tìm kiếm B");
        assertThat(donMon.search(null, null, "%", PageRequest.of(0, 50)).getTotalElements()).isZero();
        assertThat(donMon.search(null, null, "_", PageRequest.of(0, 50)).getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Danh sách đơn món: từ khóa thường và mã đơn vẫn tìm ra đúng đơn")
    void donMonTuKhoaThuong() {
        DishOrderResponse don = taoDon("Khách tìm kiếm C");
        assertThat(donMon.search(null, null, "tìm kiếm c", PageRequest.of(0, 50)).getContent())
                .extracting(DishOrderResponse::code).contains(don.code());
        assertThat(donMon.search(null, null, don.code(), PageRequest.of(0, 50)).getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sổ thanh toán: từ khóa % không khớp mọi phiếu, mã đơn vẫn tìm ra phiếu")
    void soThanhToan() {
        DishOrderResponse don = taoDon("Khách thanh toán");
        thanhToan.ghiThuTaiQuay(don.code(), new java.math.BigDecimal("10000"), null, null, null, "admin@vuonsen.vn");
        assertThat(thanhToan.danhSach(null, "%", PageRequest.of(0, 50)).getTotalElements()).isZero();
        assertThat(thanhToan.danhSach(null, don.code(), PageRequest.of(0, 50)).getTotalElements()).isEqualTo(1);
    }
}
