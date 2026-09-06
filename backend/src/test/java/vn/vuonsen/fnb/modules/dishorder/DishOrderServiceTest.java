package vn.vuonsen.fnb.modules.dishorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderQuoteRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderRequest;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Kiểm thử đơn đặt món lẻ.
 *
 * Tập trung vào hai chỗ dễ sai: công thức tính tiền, và các quy tắc từ chối đơn.
 * Từ chối sai thì mất khách, nhận sai thì bếp không kịp làm hoặc thu thiếu tiền.
 */
@SpringBootTest
class DishOrderServiceTest {

    @Autowired
    private DishOrderService service;

    @Autowired
    private DishRepository dishRepository;

    private Dish mon(String ten) {
        return dishRepository.findAllWithCategory().stream()
                .filter(d -> d.getName().equals(ten))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dữ liệu mẫu không có món: " + ten));
    }

    private DishOrderRequest.ItemRequest dong(String ten, int soLuong) {
        return new DishOrderRequest.ItemRequest(mon(ten).getId(), soLuong);
    }

    private DishOrderRequest donGiao(List<DishOrderRequest.ItemRequest> mon, LocalDateTime luc) {
        return new DishOrderRequest(FulfillmentType.DELIVERY, "Nguyễn Văn A", "0901234567",
                null, "12 Bình Quới, Bình Thạnh", null, luc, null, mon);
    }

    private DishOrderRequest donTaiCho(List<DishOrderRequest.ItemRequest> mon, int soKhach) {
        return new DishOrderRequest(FulfillmentType.DINE_IN, "Trần Thị B", "0912345678",
                null, null, soKhach, LocalDateTime.now().plusDays(2), null, mon);
    }

    // ---------------- Tính tiền ----------------

    @Test
    @DisplayName("Tạm tính cộng đúng tiền món và thuế 8%")
    void quoteAddsUpSubtotalAndVat() {
        // Gỏi củ hũ dừa 185.000 x 2 = 370.000
        var kq = service.quote(new DishOrderQuoteRequest(FulfillmentType.DINE_IN,
                List.of(dong("Gỏi củ hũ dừa tôm thịt", 2))));

        assertThat(kq.subtotal()).isEqualByComparingTo("370000");
        assertThat(kq.vatAmount()).isEqualByComparingTo("29600");   // 8% của 370.000
        assertThat(kq.total()).isEqualByComparingTo("399600");
        assertThat(kq.lines()).hasSize(1);
        assertThat(kq.lines().get(0).lineTotal()).isEqualByComparingTo("370000");
    }

    @Test
    @DisplayName("Đơn ăn tại chỗ không tính phí giao hàng")
    void dineInHasNoDeliveryFee() {
        var kq = service.quote(new DishOrderQuoteRequest(FulfillmentType.DINE_IN,
                List.of(dong("Chè bưởi Cần Thơ", 1))));

        assertThat(kq.deliveryFee()).isEqualByComparingTo("0");
        assertThat(kq.deliveryNote()).contains("không tính phí giao");
    }

    @Test
    @DisplayName("Đơn giao nhỏ thì tính phí giao, đơn lớn thì được miễn")
    void deliveryFeeDependsOnOrderValue() {
        // Chè bưởi 45.000 x 4 = 180.000, chưa đạt mức miễn phí 500.000
        var nho = service.quote(new DishOrderQuoteRequest(FulfillmentType.DELIVERY,
                List.of(dong("Chè bưởi Cần Thơ", 4))));
        assertThat(nho.deliveryFee()).isEqualByComparingTo("30000");
        assertThat(nho.deliveryNote()).contains("Đặt thêm");

        // Gà ta 450.000 x 2 = 900.000, vượt mức miễn phí
        var lon = service.quote(new DishOrderQuoteRequest(FulfillmentType.DELIVERY,
                List.of(dong("Gà ta hấp lá chanh", 2))));
        assertThat(lon.deliveryFee()).isEqualByComparingTo("0");
        assertThat(lon.deliveryNote()).contains("miễn phí giao");
    }

    @Test
    @DisplayName("Thuế tính trên tiền món, không tính trên phí giao")
    void vatIsNotChargedOnDeliveryFee() {
        var kq = service.quote(new DishOrderQuoteRequest(FulfillmentType.DELIVERY,
                List.of(dong("Chè bưởi Cần Thơ", 4))));

        // 180.000 tiền món, thuế 8% là 14.400 chứ không phải 8% của 210.000
        assertThat(kq.subtotal()).isEqualByComparingTo("180000");
        assertThat(kq.vatAmount()).isEqualByComparingTo("14400");
        assertThat(kq.total()).isEqualByComparingTo("224400");      // 180.000 + 30.000 + 14.400
    }

    // ---------------- Quy tắc từ chối đơn ----------------

    @Test
    @DisplayName("Món tính giá theo cân không đặt lẻ được")
    void dishWithoutFixedPriceCannotBeOrdered() {
        assertThatThrownBy(() -> service.quote(new DishOrderQuoteRequest(FulfillmentType.DELIVERY,
                List.of(dong("Heo quay giòn bì", 1)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chưa đặt lẻ được");
    }

    @Test
    @DisplayName("Đơn giao tận nhà dưới mức tối thiểu thì từ chối")
    void deliveryOrderBelowMinimumIsRejected() {
        // Chè bưởi 45.000, chưa đạt mức tối thiểu 150.000
        assertThatThrownBy(() -> service.create(
                donGiao(List.of(dong("Chè bưởi Cần Thơ", 1)), LocalDateTime.now().plusDays(1)), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tối thiểu");
    }

    @Test
    @DisplayName("Đơn ăn tại chỗ đông hơn mức nhận thì chỉ sang đặt tiệc")
    void dineInOverCapacityIsRedirectedToParty() {
        assertThatThrownBy(() -> service.create(
                donTaiCho(List.of(dong("Gà ta hấp lá chanh", 5)), 60), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đặt tiệc theo gói");
    }

    @Test
    @DisplayName("Đặt sát giờ thì từ chối và nói rõ món nào cần chuẩn bị lâu")
    void orderTooSoonIsRejectedWithReason() {
        // Gà ta cần 45 phút, đặt trước 10 phút thì bếp không kịp
        assertThatThrownBy(() -> service.create(
                donGiao(List.of(dong("Gà ta hấp lá chanh", 1)), LocalDateTime.now().plusMinutes(10)), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sớm nhất nhận được");
    }

    @Test
    @DisplayName("Không nhận đơn quá xa ngày")
    void orderTooFarAheadIsRejected() {
        assertThatThrownBy(() -> service.create(
                donGiao(List.of(dong("Gà ta hấp lá chanh", 1)), LocalDateTime.now().plusDays(60)), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("trong vòng");
    }

    // ---------------- Tạo đơn ----------------

    @Test
    @DisplayName("Tạo đơn thành công thì sinh mã riêng và chốt giá tại thời điểm đặt")
    void createOrderSnapshotsPriceAndGeneratesCode() {
        var kq = service.create(
                donGiao(List.of(dong("Gà ta hấp lá chanh", 1)), LocalDateTime.now().plusDays(1)), null);

        // Mã đơn đặt món có tiền tố riêng để phân biệt với đơn đặt tiệc
        assertThat(kq.code()).startsWith("DM-");
        assertThat(kq.status()).isEqualTo("PENDING");
        assertThat(kq.items()).hasSize(1);

        // Giá được chép vào đơn, không đọc lại từ bảng món
        assertThat(kq.items().get(0).dishName()).isEqualTo("Gà ta hấp lá chanh");
        assertThat(kq.items().get(0).unitPrice()).isEqualByComparingTo("450000");
        assertThat(kq.subtotal()).isEqualByComparingTo("450000");
        assertThat(kq.deliveryFee()).isEqualByComparingTo("30000");  // chưa đạt 500.000
    }

    @Test
    @DisplayName("Đơn ăn tại chỗ lưu số khách, đơn giao lưu địa chỉ")
    void eachTypeKeepsItsOwnFields() {
        var giao = service.create(
                donGiao(List.of(dong("Gà ta hấp lá chanh", 1)), LocalDateTime.now().plusDays(1)), null);
        assertThat(giao.deliveryAddress()).isNotBlank();
        assertThat(giao.guestCount()).isNull();

        var taiCho = service.create(donTaiCho(List.of(dong("Gà ta hấp lá chanh", 1)), 8), null);
        assertThat(taiCho.guestCount()).isEqualTo(8);
        assertThat(taiCho.deliveryAddress()).isNull();
    }

    @Test
    @DisplayName("Tra cứu được đơn bằng mã")
    void orderCanBeLookedUpByCode() {
        var moi = service.create(
                donGiao(List.of(dong("Gà ta hấp lá chanh", 1)), LocalDateTime.now().plusDays(1)), null);

        assertThat(service.getByCode(moi.code()).id()).isEqualTo(moi.id());
    }

    // ---------------- Chuyển trạng thái ----------------

    @Test
    @DisplayName("Chuyển trạng thái theo đúng thứ tự, không nhảy cóc")
    void statusTransitionsFollowTheAllowedOrder() {
        var moi = service.create(
                donGiao(List.of(dong("Gà ta hấp lá chanh", 1)), LocalDateTime.now().plusDays(1)), null);

        var daXacNhan = service.updateStatus(moi.id(), DishOrderStatus.CONFIRMED);
        assertThat(daXacNhan.status()).isEqualTo("CONFIRMED");

        var xong = service.updateStatus(moi.id(), DishOrderStatus.COMPLETED);
        assertThat(xong.status()).isEqualTo("COMPLETED");

        // Đơn đã hoàn thành thì không quay lại được nữa
        assertThatThrownBy(() -> service.updateStatus(moi.id(), DishOrderStatus.CONFIRMED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không thể chuyển đơn");
    }
}
