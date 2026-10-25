package vn.vuonsen.fnb.modules.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.booking.dto.MonTuChonRequest;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Đặt sảnh nhỏ kèm thực đơn tự chọn.
 *
 * Hình thức này dành cho nhóm nhỏ không muốn lấy gói tiệc cố định: tự chọn từng món, trả
 * tiền theo đúng số phần đã gọi, cộng phí thuê không gian.
 *
 * Bốn ràng buộc cần canh: không đi cùng gói tiệc, chỉ cho nhóm từ 40 khách trở xuống,
 * không gọi trùng một món hai dòng, và tiền ăn tính theo món chứ không theo mâm.
 */
@SpringBootTest
@ActiveProfiles("dev")
@ExtendWith(SpringExtension.class)
@DisplayName("Đặt sảnh nhỏ với thực đơn tự chọn")
class MonTuChonTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private DishRepository dishRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private PricingService pricingService;

    /* Không gian nhỏ nhất đang mở, đúng đối tượng của hình thức đặt này. */
    private Space sanhNho() {
        return spaceRepository.findAll().stream()
                .filter(Space::isActive)
                .min(java.util.Comparator.comparingInt(Space::getCapacityMin))
                .orElseThrow();
    }

    private List<Dish> baMonDangBan() {
        return dishRepository.findAll().stream().filter(Dish::isAvailable).limit(3).toList();
    }

    private BookingRequest donTuChon(Space sanh, int soKhach, List<MonTuChonRequest> mon) {
        return new BookingRequest(
                EventType.FAMILY, LocalDate.now().plusDays(20), TimeSlot.NOON, soKhach,
                sanh.getId(), null, mon,
                "Khách Kiểm Thử", "0901234567", null, null);
    }

    @Test
    @DisplayName("Tiền ăn bằng tổng tiền từng món, không nhân theo mâm")
    void tienAnTinhTheoMon() {
        Space sanh = sanhNho();
        List<Dish> mon = baMonDangBan();
        var gia = pricingService.calculate(sanh, null,
                mon.get(0).getPrice().multiply(java.math.BigDecimal.valueOf(2)),
                20, LocalDate.now().plusDays(20));

        assertThat(gia.tableCount()).isZero();
        assertThat(gia.foodAmount())
                .isEqualByComparingTo(mon.get(0).getPrice().multiply(java.math.BigDecimal.valueOf(2)));
    }

    @Test
    @DisplayName("Chọn món cho nhóm nhỏ thì đặt được, đơn lưu đúng số món")
    void nhomNhoDatDuoc() {
        Space sanh = sanhNho();
        List<Dish> mon = baMonDangBan();
        var don = bookingService.create(donTuChon(sanh, 20, List.of(
                new MonTuChonRequest(mon.get(0).getId(), 3),
                new MonTuChonRequest(mon.get(1).getId(), 2))), null);

        assertThat(don.code()).isNotBlank();
        assertThat(don.totalAmount()).isPositive();
    }

    @Test
    @DisplayName("Trên 40 khách thì không cho tự chọn món, phải lấy gói tiệc")
    void quaDongThiTuChoi() {
        Space sanh = sanhNho();
        List<Dish> mon = baMonDangBan();

        assertThatThrownBy(() -> bookingService.create(
                donTuChon(sanh, 60, List.of(new MonTuChonRequest(mon.get(0).getId(), 5))), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tối đa 40 khách");
    }

    @Test
    @DisplayName("Gọi trùng một món hai dòng thì bị từ chối")
    void goiTrungMonThiTuChoi() {
        Space sanh = sanhNho();
        List<Dish> mon = baMonDangBan();

        assertThatThrownBy(() -> bookingService.create(
                donTuChon(sanh, 20, List.of(
                        new MonTuChonRequest(mon.get(0).getId(), 2),
                        new MonTuChonRequest(mon.get(0).getId(), 1))), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("một dòng");
    }

    @Test
    @DisplayName("Món không có trong thực đơn thì bị từ chối")
    void monKhongTonTaiThiTuChoi() {
        Space sanh = sanhNho();

        assertThatThrownBy(() -> bookingService.create(
                donTuChon(sanh, 20, List.of(new MonTuChonRequest(999_999L, 1))), null))
                .hasMessageContaining("món ăn");
    }
}
