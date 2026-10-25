package vn.vuonsen.fnb.modules.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.booking.dto.BookingResponse;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * Kiểm thử cơ chế giữ chỗ tạm khi khách vừa gửi đơn.
 *
 * Bài toán gốc: ba khách cùng chọn một sảnh, một ngày, một buổi thì trước đây cả ba đều
 * gửi đơn được rồi cùng chờ, và hai người thua cuộc chờ vô ích. Nhưng khóa chỗ ngay lúc
 * khách chọn cũng không được, vì khi đó ai cũng khóa chùa được mà không tốn đồng nào.
 *
 * Lời giải là giữ chỗ TẠM có hạn giờ: giữ đủ lâu để khách kịp trả cọc, hết giờ chưa trả
 * thì tự thả. Bốn bài dưới đây canh đúng bốn nhánh của luật đó.
 *
 * Không đánh dấu @Transactional vì cần dữ liệu lưu hẳn giữa các lời gọi, nên phải tự dọn.
 */
@SpringBootTest
class GiuChoTamTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingStatusHistoryRepository historyRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private PartyPackageRepository packageRepository;

    @Autowired
    private BookingProperties properties;

    private final List<Long> donDaTao = new ArrayList<>();

    @AfterEach
    void donDep() {
        for (Long id : donDaTao) {
            historyRepository.deleteAll(historyRepository.findByBookingIdOrderByCreatedAtAsc(id));
            bookingRepository.deleteById(id);
        }
        donDaTao.clear();
    }

    @Test
    @DisplayName("Đơn vừa gửi được giữ chỗ, mốc hết hạn đúng theo cấu hình")
    void donMoiGuiDuocGiuCho() {
        LocalDate ngay = LocalDate.now().plusDays(70);
        LocalDateTime truocKhiTao = LocalDateTime.now();

        BookingResponse don = taoDon(ngay, TimeSlot.MORNING);
        Booking trongKho = bookingRepository.findById(don.id()).orElseThrow();

        assertThat(trongKho.getGiuChoDen())
                .as("đơn mới gửi phải có mốc giữ chỗ")
                .isNotNull()
                .isAfter(truocKhiTao.plusMinutes(properties.holdMinutes() - 1))
                .isBefore(truocKhiTao.plusMinutes(properties.holdMinutes() + 1));
        assertThat(trongKho.dangChiemCho()).isTrue();
    }

    @Test
    @DisplayName("Khách thứ hai không đặt được buổi đang có người giữ chỗ")
    void nguoiThuHaiBiChan() {
        LocalDate ngay = LocalDate.now().plusDays(71);
        taoDon(ngay, TimeSlot.EVENING);

        /*
         * Đây là nhánh chính của cả tính năng. Trước khi có giữ chỗ, lời gọi này chạy
         * trót lọt và sinh ra đơn thứ hai cho cùng một buổi.
         */
        assertThatThrownBy(() -> taoDon(ngay, TimeSlot.EVENING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã có tiệc");
    }

    @Test
    @DisplayName("Hết hạn giữ chỗ mà chưa trả cọc thì buổi đó trống lại")
    void hetHanThiThaCho() {
        LocalDate ngay = LocalDate.now().plusDays(72);
        BookingResponse donDau = taoDon(ngay, TimeSlot.NOON);

        /*
         * Đẩy mốc giữ chỗ về quá khứ thay vì chờ thật. Chờ 15 phút trong kiểm thử thì
         * không ai chạy nổi bộ kiểm thử nữa.
         */
        choHetHanGiuCho(donDau.id());

        BookingResponse donSau = taoDon(ngay, TimeSlot.NOON);

        assertThat(donSau.id())
                .as("hết hạn giữ chỗ thì khách sau phải đặt được")
                .isNotNull();
        assertThat(bookingRepository.findById(donDau.id()).orElseThrow().dangChiemCho())
                .as("đơn hết hạn giữ chỗ không còn chiếm buổi")
                .isFalse();
    }

    @Test
    @DisplayName("Đã trả cọc thì giữ chỗ vĩnh viễn, không phụ thuộc hạn giờ")
    void traCocThiGiuChoHan() {
        LocalDate ngay = LocalDate.now().plusDays(73);
        BookingResponse don = taoDon(ngay, TimeSlot.MORNING);

        /* Ghi nhận cọc rồi cho hạn giữ chỗ trôi qua. */
        Booking trongKho = bookingRepository.findById(don.id()).orElseThrow();
        trongKho.setDepositPaid(new BigDecimal("1000000"));
        trongKho.setGiuChoDen(LocalDateTime.now().minusHours(1));
        bookingRepository.save(trongKho);

        assertThat(bookingRepository.findById(don.id()).orElseThrow().dangChiemCho())
                .as("khách đã trả tiền thì không được để người khác giành mất chỗ")
                .isTrue();

        assertThatThrownBy(() -> taoDon(ngay, TimeSlot.MORNING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã có tiệc");
    }

    @Test
    @DisplayName("Giữ chỗ chỉ chặn đúng buổi đó, buổi khác cùng ngày vẫn đặt được")
    void chiChanDungBuoiDo() {
        LocalDate ngay = LocalDate.now().plusDays(74);
        taoDon(ngay, TimeSlot.MORNING);

        BookingResponse buoiKhac = taoDon(ngay, TimeSlot.EVENING);

        assertThat(buoiKhac.id())
                .as("giữ chỗ buổi sáng không được chặn buổi tối")
                .isNotNull();
    }

    // ---------- Tiện ích ----------

    private BookingResponse taoDon(LocalDate ngay, TimeSlot buoi) {
        Long khongGian = spaceRepository.findBySlug("sanh-sen-vang").orElseThrow().getId();
        Long goi = packageRepository.findAll().stream()
                .filter(g -> g.getHoursIncluded() == null
                        || g.getHoursIncluded() < properties.fullDayPackageHours())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dữ liệu mẫu không có gói theo buổi"))
                .getId();

        BookingResponse don = bookingService.create(new BookingRequest(
                EventType.OTHER, ngay, buoi, 200, khongGian, goi, null,
                "Khách thử giữ chỗ", "0901234567", null, null), null);
        donDaTao.add(don.id());
        return don;
    }

    private void choHetHanGiuCho(Long bookingId) {
        Booking don = bookingRepository.findById(bookingId).orElseThrow();
        don.setGiuChoDen(LocalDateTime.now().minusMinutes(1));
        bookingRepository.save(don);
    }
}
