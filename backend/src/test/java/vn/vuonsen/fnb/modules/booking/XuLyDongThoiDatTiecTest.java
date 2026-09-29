package vn.vuonsen.fnb.modules.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.common.concurrency.ThuLaiKhiTrungMa;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.booking.dto.BookingResponse;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử nhiều người thao tác cùng lúc, chạy trên database thật (H2).
 *
 * Các luồng được giữ lại ở vạch xuất phát rồi thả cùng một lúc, để chúng chạy chồng lên
 * nhau thật sự chứ không lần lượt. Vì phụ thuộc thời điểm, một lần chạy đạt chưa chứng
 * minh tuyệt đối là không có lỗi, nên các kịch bản xác nhận lặp lại nhiều vòng.
 *
 * Lớp kiểm thử cố ý không đánh dấu @Transactional: mỗi luồng phải tự mở giao dịch riêng
 * như hai yêu cầu thật từ hai người, và dữ liệu phải được lưu hẳn thì luồng kia mới
 * thấy. Đổi lại phải tự dọn dữ liệu sau mỗi bài để không làm lệch các kiểm thử khác.
 */
@SpringBootTest
class XuLyDongThoiDatTiecTest {

    private static final int SO_VONG = 20;

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

    @Autowired
    private ThuLaiKhiTrungMa thuLai;

    private final ConcurrentLinkedQueue<Long> donDaTao = new ConcurrentLinkedQueue<>();

    @AfterEach
    void donDep() {
        for (Long id : donDaTao) {
            historyRepository.deleteAll(historyRepository.findByBookingIdOrderByCreatedAtAsc(id));
            bookingRepository.deleteById(id);
        }
        donDaTao.clear();
    }

    /*
     * Đây là kịch bản quyết định, nơi trùng lịch thật sự xảy ra nếu không khóa.
     *
     * Hai đơn khác buổi nên khi xác nhận, không đơn nào tự hủy đơn nào: hai giao dịch không
     * ghi chung một dòng dữ liệu nào, database không có cớ gì để tự chặn. Nhưng đơn thuê
     * trọn ngày chiếm cả ngày, nên đúng luật chỉ được xác nhận một trong hai. Không khóa
     * thì cả hai cùng đọc thấy ngày còn trống và cùng được xác nhận.
     */
    @Test
    @DisplayName("Xác nhận cùng lúc đơn thuê trọn ngày và đơn buổi tối cùng ngày: chỉ một đơn giữ được chỗ")
    void xacNhanDongThoiDonTronNgay() throws Exception {
        for (int vong = 0; vong < SO_VONG; vong++) {
            LocalDate ngay = LocalDate.now().plusDays(40 + vong);
            BookingResponse donTronNgay = taoDon(ngay, TimeSlot.MORNING, goiTronNgay());
            BookingResponse donBuoiToi = taoDon(ngay, TimeSlot.EVENING, goiTheoBuoi());

            chayCungLuc(List.of(
                    () -> xacNhan(donTronNgay.id(), "quan-tri-1"),
                    () -> xacNhan(donBuoiToi.id(), "quan-tri-2")));

            assertThat(demDonGiuCho(donTronNgay, donBuoiToi))
                    .as("Vòng %d: số đơn được xác nhận trong ngày có đơn thuê trọn ngày", vong + 1)
                    .isEqualTo(1);
        }
    }

    /*
     * Kịch bản cùng buổi. Không khóa thì hai giao dịch hủy chéo đơn của nhau, ghi đè cùng
     * hai dòng dữ liệu nên database tự chặn một bên, nhưng bên bị chặn nhận lỗi khóa thay
     * vì câu báo trùng lịch. Có khóa thì người sau chờ lượt và nhận câu báo tử tế.
     */
    @Test
    @DisplayName("Hai người cùng xác nhận hai đơn trùng buổi: chỉ đúng một đơn giữ được chỗ")
    void xacNhanDongThoiCungBuoi() throws Exception {
        for (int vong = 0; vong < SO_VONG; vong++) {
            LocalDate ngay = LocalDate.now().plusDays(100 + vong);
            BookingResponse donA = taoDon(ngay, TimeSlot.EVENING, goiTheoBuoi());
            BookingResponse donB = taoDon(ngay, TimeSlot.EVENING, goiTheoBuoi());

            chayCungLuc(List.of(
                    () -> xacNhan(donA.id(), "quan-tri-1"),
                    () -> xacNhan(donB.id(), "quan-tri-2")));

            assertThat(demDonGiuCho(donA, donB))
                    .as("Vòng %d: số đơn được xác nhận cho cùng một buổi", vong + 1)
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Nhiều khách gửi đơn cùng lúc: đơn nào cũng thành công, không trùng mã")
    void guiDonDongThoiKhongTrungMa() throws Exception {
        int soKhach = 5;
        List<Callable<Object>> dsViec = new ArrayList<>();
        for (int i = 0; i < soKhach; i++) {
            LocalDate ngay = LocalDate.now().plusDays(200 + i);
            // Gọi qua đúng cơ chế thử lại mà controller đang dùng
            dsViec.add(() -> thuLai.chay("uk_booking_code",
                    () -> taoDon(ngay, TimeSlot.EVENING, goiTheoBuoi())));
        }

        List<Object> ketQua = chayCungLuc(dsViec);

        Set<String> maDon = new HashSet<>();
        for (Object kq : ketQua) {
            assertThat(kq).as("Đơn gửi lên phải thành công, nhưng nhận được: %s", kq)
                    .isInstanceOf(BookingResponse.class);
            maDon.add(((BookingResponse) kq).code());
        }
        assertThat(maDon).as("Mã đơn không được trùng").hasSize(soKhach);
    }

    // ---------------- Dựng dữ liệu và chạy song song ----------------

    /** Gói có thời lượng từ mức thuê trọn ngày trở lên (Gói Thượng Uyển 12 tiếng). */
    private Long goiTronNgay() {
        return timGoi(g -> g.getHoursIncluded() >= properties.fullDayPackageHours());
    }

    /** Gói vừa với buổi tối 5 tiếng và không phải thuê trọn ngày (Gói Đồng Quê 3 tiếng). */
    private Long goiTheoBuoi() {
        return timGoi(g -> g.getHoursIncluded() <= TimeSlot.EVENING.getDurationHours()
                && g.getHoursIncluded() < properties.fullDayPackageHours());
    }

    private Long timGoi(Predicate<PartyPackage> dieuKien) {
        return packageRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(g -> g.getHoursIncluded() != null)
                .filter(dieuKien)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Dữ liệu mẫu không có gói phù hợp"))
                .getId();
    }

    private BookingResponse taoDon(LocalDate ngay, TimeSlot buoi, Long goi) {
        Long khongGian = spaceRepository.findBySlug("sanh-sen-vang").orElseThrow().getId();

        BookingResponse don = bookingService.create(new BookingRequest(
                EventType.OTHER, ngay, buoi, 200, khongGian, goi,
                "Khách thử đồng thời", "0901234567", null, null), null);
        donDaTao.add(don.id());
        return don;
    }

    private Object xacNhan(Long id, String nguoiDuyet) {
        return bookingService.changeStatus(id, BookingStatus.CONFIRMED, null, nguoiDuyet);
    }

    private long demDonGiuCho(BookingResponse... dsDon) {
        return java.util.Arrays.stream(dsDon)
                .map(don -> bookingRepository.findById(don.id()).orElseThrow().getStatus())
                .filter(trangThai -> trangThai == BookingStatus.CONFIRMED)
                .count();
    }

    /*
     * Chạy các việc trên nhiều luồng: giữ tất cả ở vạch xuất phát rồi thả cùng lúc.
     * Trả về kết quả của từng việc; việc nào ném lỗi thì trả về chính lỗi đó để bài
     * kiểm thử tự quyết định lỗi đó có chấp nhận được hay không.
     */
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
