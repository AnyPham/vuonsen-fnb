package vn.vuonsen.fnb.modules.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Tính tiền hoàn khi khách hủy đơn đã đóng cọc.
 *
 * Quy định: hủy trước 30 ngày hoàn đủ, trước 15 ngày hoàn một nửa, sát ngày thì không
 * hoàn. Tính trên số tiền khách đã đóng thật chứ không phải số phải đóng, vì khách có
 * thể mới đóng một phần.
 *
 * Trước khi có phần này, hệ thống chỉ ghi một câu mô tả chính sách vào lịch sử trạng
 * thái mà không tính ra con số, nên nhân viên phải tự nhẩm và khách không tra được.
 */
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Tính tiền hoàn cọc khi hủy đơn")
class HoanCocTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private SpaceRepository spaceRepository;

    private Space sanh() {
        return spaceRepository.findBySlug("sanh-sen-vang").orElseThrow();
    }

    /* Tạo một đơn tổ chức sau soNgay ngày, rồi ghi nhận đã đóng đủ cọc. */
    private Booking donDaDongCoc(int soNgay) {
        Space s = sanh();
        var don = bookingService.create(new BookingRequest(
                EventType.OTHER, LocalDate.now().plusDays(soNgay), TimeSlot.NOON, 200,
                s.getId(), null, null,
                "Khách hoàn cọc", "0901234567", null, null), null);

        Booking b = bookingRepository.findById(don.id()).orElseThrow();
        b.setDepositPaid(b.getDepositAmount());
        return bookingRepository.save(b);
    }

    private Booking huy(Booking b) {
        bookingService.changeStatus(b.getId(), BookingStatus.CANCELLED, "Khách đổi kế hoạch", "kiemthu");
        return bookingRepository.findById(b.getId()).orElseThrow();
    }

    @Test
    @DisplayName("Hủy trước 30 ngày thì hoàn đủ số đã đóng")
    void truoc30NgayHoanDu() {
        Booking b = donDaDongCoc(45);
        BigDecimal daDong = b.getDepositPaid();

        Booking sau = huy(b);

        assertThat(sau.getRefundRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(sau.getRefundAmount()).isEqualByComparingTo(daDong);
        assertThat(sau.getRefundNote()).contains("100");
    }

    @Test
    @DisplayName("Hủy trong khoảng 15 đến 29 ngày thì hoàn một nửa")
    void trong15Den29NgayHoanNua() {
        Booking b = donDaDongCoc(20);
        BigDecimal daDong = b.getDepositPaid();

        Booking sau = huy(b);

        assertThat(sau.getRefundRate()).isEqualByComparingTo(new BigDecimal("0.50"));
        assertThat(sau.getRefundAmount())
                .isEqualByComparingTo(daDong.multiply(new BigDecimal("0.50")).setScale(2, java.math.RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Hủy sát ngày thì không hoàn đồng nào")
    void satNgayKhongHoan() {
        /* 10 ngày: đã qua ngưỡng báo trước 7 ngày của tiệc 20 mâm, nhưng dưới mốc hoàn 15 ngày. */
        Booking b = donDaDongCoc(10);

        Booking sau = huy(b);

        assertThat(sau.getRefundRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sau.getRefundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sau.getRefundNote()).contains("không hoàn");
    }

    @Test
    @DisplayName("Chưa đóng đồng cọc nào thì không nói chuyện hoàn tiền")
    void chuaDongThiKhongHoan() {
        Space s = sanh();
        var don = bookingService.create(new BookingRequest(
                EventType.OTHER, LocalDate.now().plusDays(45), TimeSlot.NOON, 200,
                s.getId(), null, null,
                "Khách chưa cọc", "0901234567", null, null), null);

        bookingService.changeStatus(don.id(), BookingStatus.CANCELLED, null, "kiemthu");
        Booking sau = bookingRepository.findById(don.id()).orElseThrow();

        assertThat(sau.getRefundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        /* Câu ghi lại phải nói rõ là chưa thu tiền, không để người đọc tưởng có tranh chấp. */
        assertThat(sau.getRefundNote()).isNotBlank();
    }

    @Test
    @DisplayName("Tiền hoàn tính trên số đã đóng thật, không phải số phải đóng")
    void tinhTrenSoDaDong() {
        Space s = sanh();
        var don = bookingService.create(new BookingRequest(
                EventType.OTHER, LocalDate.now().plusDays(45), TimeSlot.NOON, 200,
                s.getId(), null, null,
                "Khách đóng một nửa", "0901234567", null, null), null);

        Booking b = bookingRepository.findById(don.id()).orElseThrow();
        BigDecimal motNua = b.getDepositAmount().divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
        b.setDepositPaid(motNua);
        bookingRepository.save(b);

        Booking sau = huy(b);

        assertThat(sau.getRefundAmount()).isEqualByComparingTo(motNua);
        assertThat(sau.getRefundAmount()).isLessThan(b.getDepositAmount());
    }
}
