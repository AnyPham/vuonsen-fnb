package vn.vuonsen.fnb.modules.statistic.dto;

import java.math.BigDecimal;
import java.util.List;

/*
 * Toàn bộ số liệu cho trang thống kê, gói trong một lần gọi.
 *
 * Gộp làm một thay vì tách bảy đường dẫn riêng vì trang thống kê luôn hiển thị cả bảy
 * biểu đồ cùng lúc. Tách ra thì giao diện phải gọi bảy lần, mỗi lần một vòng mạng, mà
 * vẫn phải chờ đủ cả bảy mới vẽ xong. Dữ liệu cũng lấy từ cùng một tập đơn nên gộp lại
 * còn tiết kiệm được việc đọc cơ sở dữ liệu nhiều lần.
 */
public record ThongKeResponse(
        KhoangThoiGian khoang,
        TongQuan tongQuan,
        List<DiemThang> doanhThuTheoThang,
        List<MucDem> donTheoTrangThai,
        List<MucDoanhThu> doanhThuTheoKhongGian,
        List<MucDem> tiLeChonGoi,
        List<MucDem> theoLoaiSuKien,
        List<LapDay> lapDayKhongGian,
        List<DiemThangMon> donDatMon
) {

    /** Khoảng ngày đang thống kê, trả về để giao diện hiển thị lại cho khớp. */
    public record KhoangThoiGian(String tuNgay, String denNgay) {
    }

    /**
     * Bốn ô số liệu ở đầu trang.
     *
     * Doanh thu và số khách chỉ tính đơn đã xác nhận hoặc đã hoàn thành. Đơn chờ xác
     * nhận chưa chắc thành tiền, đơn đã hủy thì không thành tiền, gộp vào sẽ ra con số
     * đẹp nhưng sai.
     */
    public record TongQuan(
            BigDecimal doanhThu,
            long soDonGhiNhan,
            long tongSoDon,
            long soKhach,
            BigDecimal giaTriDonTrungBinh,
            double tiLeHuy
    ) {
    }

    /** Một cột trong biểu đồ doanh thu theo tháng. */
    public record DiemThang(String thang, BigDecimal doanhThu, long soDon) {
    }

    /** Một lát trong biểu đồ tròn: tên nhãn và số đơn. */
    public record MucDem(String ten, long soDon) {
    }

    /** Một dòng trong biểu đồ doanh thu theo không gian. */
    public record MucDoanhThu(String ten, BigDecimal doanhThu, long soDon) {
    }

    /**
     * Tỉ lệ lấp đầy của một không gian.
     *
     * Một ngày có ba buổi nên tổng số buổi có thể bán bằng số ngày nhân ba. Đếm số buổi
     * đã có đơn được xác nhận rồi chia cho tổng đó.
     */
    public record LapDay(String ten, long soBuoiDaDat, long tongSoBuoi, double tiLe) {
    }

    /** Một cột trong biểu đồ đặt món lẻ, tách riêng giao tận nhà và ăn tại chỗ. */
    public record DiemThangMon(
            String thang,
            BigDecimal doanhThu,
            long soDon,
            long giaoTanNha,
            long anTaiCho
    ) {
    }
}
