package vn.vuonsen.fnb.modules.statistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingStatus;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.booking.TimeSlot;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;
import vn.vuonsen.fnb.modules.dishorder.DishOrderStatus;
import vn.vuonsen.fnb.modules.dishorder.FulfillmentType;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceType;
import vn.vuonsen.fnb.modules.statistic.dto.ThongKeResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử cách gộp số liệu thống kê, không cần cơ sở dữ liệu.
 *
 * Phần này dễ sai lặng lẽ: sai quy ước đếm thì biểu đồ vẫn vẽ ra đẹp, chỉ có con số là
 * sai, mà nhìn biểu đồ thì không cách nào phát hiện. Nên các bài dưới đây chốt lại từng
 * quy ước một.
 */
class ThongKeAggregatorTest {

    private static final LocalDate TU = LocalDate.of(2026, 1, 1);
    private static final LocalDate DEN = LocalDate.of(2026, 3, 31);

    private ThongKeAggregator aggregator;
    private Space sanhSenVang;
    private Space nhaRuongGo;
    private PartyPackage senVang;
    private PartyPackage dongQue;

    @BeforeEach
    void setUp() {
        aggregator = new ThongKeAggregator();

        sanhSenVang = Space.builder()
                .name("Sảnh Sen Vàng").spaceType(SpaceType.INDOOR)
                .capacityMin(200).capacityMax(500)
                .rentalFee(new BigDecimal("12000000")).feeUnit("SESSION")
                .build();
        sanhSenVang.setId(1L);

        nhaRuongGo = Space.builder()
                .name("Nhà Rường Gỗ").spaceType(SpaceType.PRIVATE)
                .capacityMin(20).capacityMax(60)
                .rentalFee(new BigDecimal("3000000")).feeUnit("SESSION")
                .build();
        nhaRuongGo.setId(2L);

        senVang = PartyPackage.builder()
                .name("Gói Sen Vàng").pricePerTable(new BigDecimal("4500000")).build();
        dongQue = PartyPackage.builder()
                .name("Gói Đồng Quê").pricePerTable(new BigDecimal("2900000")).build();
    }

    // ---------------- Quy ước ghi nhận doanh thu ----------------

    @Test
    @DisplayName("Doanh thu chỉ tính đơn đã xác nhận và đã hoàn thành")
    void doanhThuBoQuaDonChoVaDonHuy() {
        List<Booking> don = List.of(
                donTiec(LocalDate.of(2026, 1, 10), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200),
                donTiec(LocalDate.of(2026, 1, 20), TimeSlot.NOON, sanhSenVang, senVang,
                        BookingStatus.COMPLETED, "50000000", 100),
                // Hai đơn dưới đây không được tính vào doanh thu
                donTiec(LocalDate.of(2026, 2, 5), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.PENDING, "999000000", 300),
                donTiec(LocalDate.of(2026, 2, 6), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CANCELLED, "888000000", 400));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang));

        assertThat(kq.tongQuan().doanhThu()).isEqualByComparingTo("150000000");
        assertThat(kq.tongQuan().soDonGhiNhan()).isEqualTo(2);
        assertThat(kq.tongQuan().tongSoDon()).isEqualTo(4);
        assertThat(kq.tongQuan().soKhach()).isEqualTo(300);
    }

    @Test
    @DisplayName("Giá trị đơn trung bình chia theo số đơn ghi nhận, không chia tổng số đơn")
    void giaTriTrungBinhChiaTheoDonGhiNhan() {
        List<Booking> don = List.of(
                donTiec(LocalDate.of(2026, 1, 10), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200),
                donTiec(LocalDate.of(2026, 1, 11), TimeSlot.NOON, sanhSenVang, senVang,
                        BookingStatus.CANCELLED, "100000000", 200));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang));

        // 100 triệu / 1 đơn ghi nhận, chứ không phải 100 triệu / 2 đơn
        assertThat(kq.tongQuan().giaTriDonTrungBinh()).isEqualByComparingTo("100000000");
        assertThat(kq.tongQuan().tiLeHuy()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Danh sách rỗng không gây lỗi chia cho không")
    void danhSachRongTraVeSoKhong() {
        var kq = aggregator.gop(TU, DEN, List.of(), List.of(), List.of(sanhSenVang));

        assertThat(kq.tongQuan().doanhThu()).isEqualByComparingTo("0");
        assertThat(kq.tongQuan().giaTriDonTrungBinh()).isEqualByComparingTo("0");
        assertThat(kq.tongQuan().tiLeHuy()).isZero();
        assertThat(kq.lapDayKhongGian()).hasSize(1);
        assertThat(kq.lapDayKhongGian().get(0).tiLe()).isZero();
    }

    // ---------------- Biểu đồ theo tháng ----------------

    @Test
    @DisplayName("Tháng không có đơn vẫn xuất hiện trên biểu đồ với giá trị 0")
    void thangRongVanCoMatDeDuongBieuDoLienMach() {
        List<Booking> don = List.of(
                donTiec(LocalDate.of(2026, 1, 10), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200),
                donTiec(LocalDate.of(2026, 3, 10), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "60000000", 150));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang));

        assertThat(kq.doanhThuTheoThang()).hasSize(3);
        assertThat(kq.doanhThuTheoThang()).extracting(ThongKeResponse.DiemThang::thang)
                .containsExactly("01/2026", "02/2026", "03/2026");
        assertThat(kq.doanhThuTheoThang().get(0).doanhThu()).isEqualByComparingTo("100000000");
        // Tháng 2 không có đơn nhưng vẫn phải có mặt
        assertThat(kq.doanhThuTheoThang().get(1).doanhThu()).isEqualByComparingTo("0");
        assertThat(kq.doanhThuTheoThang().get(1).soDon()).isZero();
        assertThat(kq.doanhThuTheoThang().get(2).doanhThu()).isEqualByComparingTo("60000000");
    }

    // ---------------- Đơn theo trạng thái ----------------

    @Test
    @DisplayName("Biểu đồ trạng thái liệt kê đủ bốn trạng thái, kể cả trạng thái không có đơn")
    void trangThaiLietKeDuBonMuc() {
        List<Booking> don = List.of(
                donTiec(LocalDate.of(2026, 1, 10), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang));

        assertThat(kq.donTheoTrangThai()).hasSize(4);
        assertThat(kq.donTheoTrangThai()).extracting(ThongKeResponse.MucDem::ten)
                .containsExactly("Chờ xác nhận", "Đã xác nhận", "Đã hoàn thành", "Đã hủy");
        assertThat(kq.donTheoTrangThai().get(1).soDon()).isEqualTo(1);
        assertThat(kq.donTheoTrangThai().get(0).soDon()).isZero();
    }

    // ---------------- Doanh thu theo không gian ----------------

    @Test
    @DisplayName("Doanh thu theo không gian xếp giảm dần")
    void doanhThuTheoKhongGianXepGiamDan() {
        List<Booking> don = List.of(
                donTiec(LocalDate.of(2026, 1, 10), TimeSlot.EVENING, nhaRuongGo, dongQue,
                        BookingStatus.CONFIRMED, "20000000", 50),
                donTiec(LocalDate.of(2026, 1, 12), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang, nhaRuongGo));

        assertThat(kq.doanhThuTheoKhongGian()).extracting(ThongKeResponse.MucDoanhThu::ten)
                .containsExactly("Sảnh Sen Vàng", "Nhà Rường Gỗ");
    }

    // ---------------- Tỉ lệ lấp đầy ----------------

    @Test
    @DisplayName("Lấp đầy đếm số buổi khác nhau, hai đơn trùng buổi chỉ tính một")
    void lapDayDemBuoiKhongDemDon() {
        LocalDate ngay = LocalDate.of(2026, 1, 10);
        List<Booking> don = List.of(
                donTiec(ngay, TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200),
                // Dữ liệu lỗi: hai đơn cùng không gian, cùng ngày, cùng buổi
                donTiec(ngay, TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.COMPLETED, "90000000", 180),
                // Buổi khác trong cùng ngày thì tính thêm
                donTiec(ngay, TimeSlot.NOON, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "80000000", 160));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang));

        var lapDay = kq.lapDayKhongGian().get(0);
        // 90 ngày x 3 buổi = 270 buổi có thể bán
        assertThat(lapDay.tongSoBuoi()).isEqualTo(270);
        // Ba đơn nhưng chỉ chiếm hai buổi khác nhau
        assertThat(lapDay.soBuoiDaDat()).isEqualTo(2);
        assertThat(lapDay.tiLe()).isEqualTo(0.7);
    }

    @Test
    @DisplayName("Không gian chưa có đơn nào vẫn hiện trên biểu đồ với tỉ lệ 0")
    void khongGianChuaCoDonVanHien() {
        List<Booking> don = List.of(
                donTiec(LocalDate.of(2026, 1, 10), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang, nhaRuongGo));

        assertThat(kq.lapDayKhongGian()).hasSize(2);
        assertThat(kq.lapDayKhongGian()).extracting(ThongKeResponse.LapDay::ten)
                .contains("Nhà Rường Gỗ");
    }

    // ---------------- Đặt món lẻ ----------------

    @Test
    @DisplayName("Đơn đặt món tách riêng giao tận nhà và ăn tại chỗ, bỏ đơn hủy")
    void donDatMonTachTheoHinhThuc() {
        List<DishOrder> don = List.of(
                donMon(LocalDateTime.of(2026, 1, 5, 18, 0), FulfillmentType.DELIVERY,
                        DishOrderStatus.COMPLETED, "321600"),
                donMon(LocalDateTime.of(2026, 1, 6, 12, 0), FulfillmentType.DELIVERY,
                        DishOrderStatus.CONFIRMED, "745200"),
                donMon(LocalDateTime.of(2026, 1, 7, 19, 0), FulfillmentType.DINE_IN,
                        DishOrderStatus.COMPLETED, "291600"),
                // Đơn hủy không tính
                donMon(LocalDateTime.of(2026, 1, 8, 19, 0), FulfillmentType.DINE_IN,
                        DishOrderStatus.CANCELLED, "999000"));

        var kq = aggregator.gop(TU, DEN, List.of(), don, List.of(sanhSenVang));

        var thang1 = kq.donDatMon().get(0);
        assertThat(thang1.thang()).isEqualTo("01/2026");
        assertThat(thang1.soDon()).isEqualTo(3);
        assertThat(thang1.giaoTanNha()).isEqualTo(2);
        assertThat(thang1.anTaiCho()).isEqualTo(1);
        assertThat(thang1.doanhThu()).isEqualByComparingTo("1358400");
    }

    // ---------------- Khoảng thời gian ----------------

    @Test
    @DisplayName("Khoảng ngày được trả về để giao diện hiển thị lại")
    void traVeKhoangDaThongKe() {
        var kq = aggregator.gop(TU, DEN, List.of(), List.of(), List.of());

        assertThat(kq.khoang().tuNgay()).isEqualTo("01/01/2026");
        assertThat(kq.khoang().denNgay()).isEqualTo("31/03/2026");
    }

    // ---------------- Dựng dữ liệu mẫu ----------------

    private Booking donTiec(LocalDate ngay, TimeSlot buoi, Space khongGian, PartyPackage goi,
                            BookingStatus trangThai, String tong, int soKhach) {
        return Booking.builder()
                .code("VS-TEST-" + ngay + "-" + buoi)
                .space(khongGian)
                .partyPackage(goi)
                .eventType(EventType.WEDDING)
                .eventDate(ngay)
                .timeSlot(buoi)
                .guestCount(soKhach)
                .tableCount(soKhach / 10)
                .totalAmount(new BigDecimal(tong))
                .status(trangThai)
                .build();
    }

    private DishOrder donMon(LocalDateTime luc, FulfillmentType hinhThuc,
                            DishOrderStatus trangThai, String tong) {
        return DishOrder.builder()
                .code("DM-TEST-" + luc)
                .fulfillmentType(hinhThuc)
                .serveAt(luc)
                .total(new BigDecimal(tong))
                .status(trangThai)
                .build();
    }
    @Test
    @DisplayName("Đơn chỉ thuê không gian được đếm riêng trong biểu đồ gói tiệc")
    void donKhongGoiDemRieng() {
        List<Booking> don = List.of(
                donTiec(LocalDate.of(2026, 1, 10), TimeSlot.EVENING, sanhSenVang, senVang,
                        BookingStatus.CONFIRMED, "100000000", 200),
                donTiec(LocalDate.of(2026, 1, 11), TimeSlot.NOON, sanhSenVang, null,
                        BookingStatus.CONFIRMED, "12960000", 150));

        var kq = aggregator.gop(TU, DEN, don, List.of(), List.of(sanhSenVang));

        assertThat(kq.tiLeChonGoi()).extracting(ThongKeResponse.MucDem::ten)
                .containsExactlyInAnyOrder("Gói Sen Vàng", "Chỉ thuê không gian");
        assertThat(kq.tongQuan().doanhThu()).isEqualByComparingTo("112960000");
    }
}
