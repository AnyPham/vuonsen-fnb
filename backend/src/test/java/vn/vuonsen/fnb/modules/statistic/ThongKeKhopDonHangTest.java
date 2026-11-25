package vn.vuonsen.fnb.modules.statistic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.dishorder.DishOrderService;
import vn.vuonsen.fnb.modules.dishorder.DishOrderStatus;
import vn.vuonsen.fnb.modules.dishorder.FulfillmentType;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderResponse;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.statistic.dto.ThongKeResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * PCN-05: số liệu thống kê phải khớp với số tiền khách thực trả theo công thức tính giá.
 *
 * Đi trọn đường thật: đặt đơn qua DishOrderService (giá do máy chủ tính), đổi trạng thái,
 * rồi đọc trang thống kê. So phần chênh trước và sau, nên không phụ thuộc dữ liệu mẫu
 * có sẵn trong cùng khoảng ngày.
 */
@SpringBootTest
@Transactional
class ThongKeKhopDonHangTest {

    @Autowired
    private DishOrderService donMon;

    @Autowired
    private ThongKeService thongKe;

    @Autowired
    private DishRepository dishRepository;

    private DishOrderResponse datDon(LocalDateTime luc, int soLuong) {
        Long monId = dishRepository.search(null, null).get(0).getId();
        return donMon.create(new DishOrderRequest(FulfillmentType.DELIVERY, "Khách thống kê", "0901234567",
                null, "12 Nguyễn Huệ, Quận 1", null, luc, null,
                List.of(new DishOrderRequest.ItemRequest(monId, soLuong))), null);
    }

    private BigDecimal doanhThuMon(LocalDate ngay) {
        return thongKe.thongKe(ngay, ngay).donDatMon().stream()
                .map(ThongKeResponse.DiemThangMon::doanhThu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long soDonMon(LocalDate ngay) {
        return thongKe.thongKe(ngay, ngay).donDatMon().stream().mapToLong(ThongKeResponse.DiemThangMon::soDon).sum();
    }

    @Test
    @DisplayName("Đơn món đã xác nhận được cộng đúng tổng tiền đơn vào doanh thu thống kê")
    void donXacNhanKhopDoanhThu() {
        LocalDateTime luc = LocalDate.now().plusDays(9).atTime(11, 0);
        BigDecimal truoc = doanhThuMon(luc.toLocalDate());
        long soTruoc = soDonMon(luc.toLocalDate());

        DishOrderResponse don = datDon(luc, 3);
        donMon.updateStatus(don.id(), DishOrderStatus.CONFIRMED);

        assertThat(doanhThuMon(luc.toLocalDate()).subtract(truoc)).isEqualByComparingTo(don.total());
        assertThat(soDonMon(luc.toLocalDate()) - soTruoc).isEqualTo(1);
    }

    @Test
    @DisplayName("Đơn chờ xác nhận và đơn đã hủy không được tính vào doanh thu")
    void donChuaGhiNhanKhongTinh() {
        LocalDateTime luc = LocalDate.now().plusDays(10).atTime(18, 0);
        BigDecimal truoc = doanhThuMon(luc.toLocalDate());

        datDon(luc, 2);                                   // để nguyên PENDING
        DishOrderResponse huy = datDon(luc, 4);
        donMon.updateStatus(huy.id(), DishOrderStatus.CANCELLED);

        assertThat(doanhThuMon(luc.toLocalDate())).isEqualByComparingTo(truoc);
    }

    @Test
    @DisplayName("Tổng tiền đơn bằng tạm tính trừ giảm giá cộng phí giao và VAT")
    void tongTienDungCongThuc() {
        DishOrderResponse don = datDon(LocalDate.now().plusDays(11).atTime(12, 0), 2);
        assertThat(don.total()).isEqualByComparingTo(
                don.subtotal().subtract(don.discountAmount()).add(don.deliveryFee()).add(don.vatAmount()));
    }
}
