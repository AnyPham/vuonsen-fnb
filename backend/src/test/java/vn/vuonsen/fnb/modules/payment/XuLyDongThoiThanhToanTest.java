package vn.vuonsen.fnb.modules.payment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.modules.booking.BookingRepository;
import vn.vuonsen.fnb.modules.booking.BookingService;
import vn.vuonsen.fnb.modules.booking.BookingStatusHistoryRepository;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.booking.TimeSlot;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.booking.dto.BookingResponse;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import org.springframework.http.ResponseEntity;
import vn.vuonsen.fnb.modules.payment.dto.TaoThanhToanRequest;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Nhiều khách cùng tạo phiếu thanh toán một lúc, chạy trên database thật (H2).
 *
 * Bài này sinh ra từ một lỗi mà đo tải bằng JMeter phát hiện: ở mức 25 người dùng đồng thời,
 * khoảng một phần tư số lượt đóng cọc nhận lỗi 409. Mã phiếu thu dạng TT-B-ngày-số thứ tự,
 * trước đây sinh bằng cách dò từ số 1 tới số đầu tiên chưa dùng. Hai người cùng dò cùng lúc
 * thì cùng thấy một số còn trống vì chưa ai kịp lưu, cùng ghi, và database chặn người sau
 * vì trùng ràng buộc UNIQUE.
 *
 * Gọi thẳng vào controller chứ không gọi service, để đi đúng đường một yêu cầu HTTP đi qua:
 * cơ chế thử lại nằm ở controller, nên gọi service trực tiếp sẽ bỏ sót chuyện controller có
 * thử lại hay không. Bản đầu của bài này gọi service rồi tự bọc thử lại, và đạt ngay cả khi
 * lỗi còn nguyên, vì nó tự làm hộ đúng phần controller thiếu.
 *
 * Cũng như lớp kiểm thử đồng thời của đặt tiệc, các luồng được giữ ở vạch xuất phát rồi thả
 * cùng một lúc, và lớp này không đánh dấu @Transactional để mỗi luồng tự mở giao dịch riêng
 * như yêu cầu thật. Đổi lại phải tự dọn dữ liệu.
 */
@SpringBootTest
class XuLyDongThoiThanhToanTest {

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private PaymentRepository paymentRepository;

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
            paymentRepository.deleteAll(paymentRepository.findByBookingIdOrderByCreatedAtAsc(id));
            historyRepository.deleteAll(historyRepository.findByBookingIdOrderByCreatedAtAsc(id));
            bookingRepository.deleteById(id);
        }
        donDaTao.clear();
    }

    @Test
    @DisplayName("Mười lăm khách cùng tạo phiếu đóng cọc: phiếu nào cũng tạo được, không trùng mã")
    void taoPhieuDongThoiKhongTrungMa() throws Exception {
        int soKhach = 15;
        List<String> maDon = new ArrayList<>();
        for (int i = 0; i < soKhach; i++) {
            maDon.add(taoDon(LocalDate.now().plusDays(500 + i)));
        }

        List<Callable<Object>> dsViec = new ArrayList<>();
        for (String ma : maDon) {
            dsViec.add(() -> paymentController.taoYeuCau(new TaoThanhToanRequest(ma, null)));
        }

        List<Object> ketQua = chayCungLuc(dsViec);

        for (Object kq : ketQua) {
            assertThat(kq).as("Tạo phiếu phải thành công, nhưng nhận được: %s", kq)
                    .isInstanceOf(ResponseEntity.class);
        }
        Set<String> maPhieu = new HashSet<>();
        for (Long id : donDaTao) {
            paymentRepository.findByBookingIdOrderByCreatedAtAsc(id).forEach(p -> maPhieu.add(p.getCode()));
        }
        assertThat(maPhieu).as("Mỗi đơn một phiếu, mã phiếu không được trùng").hasSize(soKhach);
    }

    // ---------------- Dựng dữ liệu và chạy song song ----------------

    /** Tạo tuần tự một đơn đặt tiệc theo buổi để có cái mà thanh toán. */
    private String taoDon(LocalDate ngay) {
        Long khongGian = spaceRepository.findBySlug("sanh-sen-vang").orElseThrow().getId();
        Long goi = packageRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(g -> g.getHoursIncluded() != null)
                .filter(g -> g.getHoursIncluded() <= TimeSlot.EVENING.getDurationHours()
                        && g.getHoursIncluded() < properties.fullDayPackageHours())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dữ liệu mẫu không có gói theo buổi"))
                .getId();

        BookingResponse don = bookingService.create(new BookingRequest(
                EventType.OTHER, ngay, TimeSlot.EVENING, 200, khongGian, goi, null,
                "Khách thử thanh toán đồng thời", "0901234567", null, null), null);
        donDaTao.add(don.id());
        return don.code();
    }

    /* Giữ mọi luồng ở vạch xuất phát rồi thả cùng lúc; lỗi của từng luồng được trả về thay vì ném ra. */
    private List<Object> chayCungLuc(List<Callable<Object>> dsViec) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(dsViec.size());
        CountDownLatch vachXuatPhat = new CountDownLatch(1);
        try {
            List<Future<Object>> dangChay = new ArrayList<>();
            for (Callable<Object> viec : dsViec) {
                dangChay.add(pool.submit(() -> {
                    vachXuatPhat.await();
                    try {
                        return viec.call();
                    } catch (Exception loi) {
                        return loi;
                    }
                }));
            }
            vachXuatPhat.countDown();

            List<Object> ketQua = new ArrayList<>();
            for (Future<Object> f : dangChay) {
                ketQua.add(f.get(30, TimeUnit.SECONDS));
            }
            return ketQua;
        } finally {
            pool.shutdownNow();
        }
    }
}
