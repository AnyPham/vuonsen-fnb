package vn.vuonsen.fnb.modules.statistic;

import org.springframework.stereotype.Component;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingStatus;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;
import vn.vuonsen.fnb.modules.dishorder.DishOrderStatus;
import vn.vuonsen.fnb.modules.dishorder.FulfillmentType;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.statistic.dto.ThongKeResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Gộp số liệu thống kê từ danh sách đơn.
 *
 * Không đụng tới cơ sở dữ liệu, chỉ nhận vào các danh sách đã nạp sẵn. Nhờ vậy kiểm thử
 * được toàn bộ cách tính mà không cần dựng ứng dụng, giống cách DishOrderPricing đang làm
 * với công thức tính tiền. Đây là phần dễ sai lặng lẽ: sai một quy ước đếm thì biểu đồ
 * vẫn vẽ ra đẹp, chỉ có con số là sai, nên càng cần kiểm thử chặt.
 */
@Component
public class ThongKeAggregator {

    /*
     * Đơn nào được tính là doanh thu.
     *
     * Chỉ đơn đã xác nhận và đã hoàn thành. Đơn chờ xác nhận chưa chắc thành tiền, đơn
     * đã hủy thì chắc chắn không. Gộp cả bốn trạng thái vào sẽ ra con số đẹp nhưng sai,
     * và sai theo hướng có lợi nên càng khó phát hiện.
     */
    private static final Set<BookingStatus> GHI_NHAN_TIEC =
            EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED);

    private static final Set<DishOrderStatus> GHI_NHAN_MON =
            EnumSet.of(DishOrderStatus.CONFIRMED, DishOrderStatus.COMPLETED);

    /** Một ngày bán được ba buổi: sáng, trưa, tối. Dùng để tính tỉ lệ lấp đầy. */
    private static final int SO_BUOI_MOI_NGAY = 3;

    private static final DateTimeFormatter NHAN_THANG = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ThongKeResponse gop(LocalDate tuNgay, LocalDate denNgay,
                               List<Booking> donTiec, List<DishOrder> donMon, List<Space> khongGian) {

        List<Booking> ghiNhan = donTiec.stream().filter(b -> GHI_NHAN_TIEC.contains(b.getStatus())).toList();

        return new ThongKeResponse(
                new ThongKeResponse.KhoangThoiGian(tuNgay.format(NGAY), denNgay.format(NGAY)),
                tongQuan(donTiec, ghiNhan),
                doanhThuTheoThang(tuNgay, denNgay, ghiNhan),
                donTheoTrangThai(donTiec),
                doanhThuTheoKhongGian(ghiNhan),
                tiLeChonGoi(ghiNhan),
                theoLoaiSuKien(ghiNhan),
                lapDayKhongGian(tuNgay, denNgay, ghiNhan, khongGian),
                donDatMon(tuNgay, denNgay, donMon)
        );
    }

    // ---------------- Bốn ô số liệu đầu trang ----------------
    private ThongKeResponse.TongQuan tongQuan(List<Booking> tatCa, List<Booking> ghiNhan) {
        BigDecimal doanhThu = ghiNhan.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long soKhach = ghiNhan.stream().mapToLong(Booking::getGuestCount).sum();

        BigDecimal trungBinh = ghiNhan.isEmpty()
                ? BigDecimal.ZERO
                : doanhThu.divide(BigDecimal.valueOf(ghiNhan.size()), 0, RoundingMode.HALF_UP);

        long soHuy = tatCa.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        double tiLeHuy = tatCa.isEmpty() ? 0 : lamTronTiLe((double) soHuy / tatCa.size());

        return new ThongKeResponse.TongQuan(
                doanhThu, ghiNhan.size(), tatCa.size(), soKhach, trungBinh, tiLeHuy);
    }

    // ---------------- Biểu đồ 1: doanh thu theo tháng ----------------
    private List<ThongKeResponse.DiemThang> doanhThuTheoThang(
            LocalDate tuNgay, LocalDate denNgay, List<Booking> ghiNhan) {

        // Dựng sẵn đủ tháng trong khoảng, kể cả tháng không có đơn. Thiếu tháng thì
        // đường biểu đồ nhảy cóc, nhìn vào tưởng doanh thu liền mạch.
        Map<YearMonth, List<Booking>> theoThang = khungThang(tuNgay, denNgay);
        for (Booking b : ghiNhan) {
            YearMonth thang = YearMonth.from(b.getEventDate());
            List<Booking> o = theoThang.get(thang);
            if (o != null) {
                o.add(b);
            }
        }

        List<ThongKeResponse.DiemThang> ra = new ArrayList<>();
        theoThang.forEach((thang, dsDon) -> ra.add(new ThongKeResponse.DiemThang(
                thang.atDay(1).format(NHAN_THANG),
                dsDon.stream().map(Booking::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                dsDon.size())));
        return ra;
    }

    // ---------------- Biểu đồ 2: đơn theo trạng thái ----------------
    private List<ThongKeResponse.MucDem> donTheoTrangThai(List<Booking> tatCa) {
        // Đếm cả bốn trạng thái, kể cả trạng thái không có đơn nào, để biểu đồ tròn
        // luôn có đủ chú giải. Đây là biểu đồ duy nhất tính cả đơn hủy.
        List<ThongKeResponse.MucDem> ra = new ArrayList<>();
        for (BookingStatus tt : BookingStatus.values()) {
            long dem = tatCa.stream().filter(b -> b.getStatus() == tt).count();
            ra.add(new ThongKeResponse.MucDem(tt.getLabel(), dem));
        }
        return ra;
    }

    // ---------------- Biểu đồ 3: doanh thu theo không gian ----------------
    private List<ThongKeResponse.MucDoanhThu> doanhThuTheoKhongGian(List<Booking> ghiNhan) {
        Map<String, List<Booking>> theoKhongGian = new LinkedHashMap<>();
        for (Booking b : ghiNhan) {
            theoKhongGian.computeIfAbsent(b.getSpace().getName(), k -> new ArrayList<>()).add(b);
        }

        List<ThongKeResponse.MucDoanhThu> ra = new ArrayList<>();
        theoKhongGian.forEach((ten, dsDon) -> ra.add(new ThongKeResponse.MucDoanhThu(
                ten,
                dsDon.stream().map(Booking::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                dsDon.size())));

        // Xếp giảm dần để nhìn ra ngay không gian nào đang gánh doanh thu
        ra.sort(Comparator.comparing(ThongKeResponse.MucDoanhThu::doanhThu).reversed());
        return ra;
    }

    // ---------------- Biểu đồ 4: tỉ lệ chọn gói tiệc ----------------
    private List<ThongKeResponse.MucDem> tiLeChonGoi(List<Booking> ghiNhan) {
        // Đơn chỉ thuê không gian gom vào một mục riêng thay vì làm vỡ biểu đồ
        return demTheoNhan(ghiNhan, Booking::tenGoiHienThi);
    }

    // ---------------- Biểu đồ 5: loại sự kiện ----------------
    private List<ThongKeResponse.MucDem> theoLoaiSuKien(List<Booking> ghiNhan) {
        return demTheoNhan(ghiNhan, b -> b.getEventType().getLabel());
    }

    // ---------------- Biểu đồ 6: tỉ lệ lấp đầy không gian ----------------
    private List<ThongKeResponse.LapDay> lapDayKhongGian(
            LocalDate tuNgay, LocalDate denNgay, List<Booking> ghiNhan, List<Space> khongGian) {

        // Cộng 1 vì khoảng thống kê tính cả ngày đầu lẫn ngày cuối
        long soNgay = ChronoUnit.DAYS.between(tuNgay, denNgay) + 1;
        long tongSoBuoi = soNgay * SO_BUOI_MOI_NGAY;

        /*
         * Một không gian trong một buổi chỉ bán được một lần, nên phải đếm số cặp
         * (ngày, buổi) khác nhau chứ không đếm số đơn. Nếu đếm số đơn thì dữ liệu lỗi
         * có hai đơn trùng buổi sẽ đẩy tỉ lệ vượt quá 100%.
         */
        Map<Long, Set<String>> buoiDaDat = new LinkedHashMap<>();
        for (Booking b : ghiNhan) {
            buoiDaDat.computeIfAbsent(b.getSpace().getId(), k -> new HashSet<>())
                    .add(b.getEventDate() + "|" + b.getTimeSlot().name());
        }

        List<ThongKeResponse.LapDay> ra = new ArrayList<>();
        for (Space kg : khongGian) {
            long daDat = buoiDaDat.getOrDefault(kg.getId(), Set.of()).size();
            double tiLe = tongSoBuoi == 0 ? 0 : lamTronTiLe((double) daDat / tongSoBuoi);
            ra.add(new ThongKeResponse.LapDay(kg.getName(), daDat, tongSoBuoi, tiLe));
        }
        ra.sort(Comparator.comparingDouble(ThongKeResponse.LapDay::tiLe).reversed());
        return ra;
    }

    // ---------------- Biểu đồ 7: đơn đặt món lẻ ----------------
    private List<ThongKeResponse.DiemThangMon> donDatMon(
            LocalDate tuNgay, LocalDate denNgay, List<DishOrder> donMon) {

        List<DishOrder> ghiNhan = donMon.stream()
                .filter(d -> GHI_NHAN_MON.contains(d.getStatus()))
                .toList();

        Map<YearMonth, List<DishOrder>> theoThang = khungThang(tuNgay, denNgay);
        for (DishOrder d : ghiNhan) {
            YearMonth thang = YearMonth.from(d.getServeAt().toLocalDate());
            List<DishOrder> o = theoThang.get(thang);
            if (o != null) {
                o.add(d);
            }
        }

        List<ThongKeResponse.DiemThangMon> ra = new ArrayList<>();
        theoThang.forEach((thang, ds) -> ra.add(new ThongKeResponse.DiemThangMon(
                thang.atDay(1).format(NHAN_THANG),
                ds.stream().map(DishOrder::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add),
                ds.size(),
                ds.stream().filter(d -> d.getFulfillmentType() == FulfillmentType.DELIVERY).count(),
                ds.stream().filter(d -> d.getFulfillmentType() == FulfillmentType.DINE_IN).count())));
        return ra;
    }

    // ---------------- Dùng chung ----------------

    /** Dựng sẵn các ô tháng trống từ ngày đầu tới ngày cuối, giữ đúng thứ tự thời gian. */
    private <T> Map<YearMonth, List<T>> khungThang(LocalDate tuNgay, LocalDate denNgay) {
        Map<YearMonth, List<T>> khung = new LinkedHashMap<>();
        YearMonth chay = YearMonth.from(tuNgay);
        YearMonth het = YearMonth.from(denNgay);
        while (!chay.isAfter(het)) {
            khung.put(chay, new ArrayList<>());
            chay = chay.plusMonths(1);
        }
        return khung;
    }

    private List<ThongKeResponse.MucDem> demTheoNhan(
            List<Booking> ds, java.util.function.Function<Booking, String> layNhan) {

        Map<String, Long> dem = new LinkedHashMap<>();
        for (Booking b : ds) {
            dem.merge(layNhan.apply(b), 1L, Long::sum);
        }

        List<ThongKeResponse.MucDem> ra = new ArrayList<>();
        dem.forEach((ten, so) -> ra.add(new ThongKeResponse.MucDem(ten, so)));
        ra.sort(Comparator.comparingLong(ThongKeResponse.MucDem::soDon).reversed());
        return ra;
    }

    /** Trả tỉ lệ dạng phần trăm, làm tròn một chữ số thập phân cho dễ đọc trên biểu đồ. */
    private double lamTronTiLe(double tiLe) {
        return Math.round(tiLe * 1000.0) / 10.0;
    }
}
