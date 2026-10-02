package vn.vuonsen.fnb.common.i18n;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Từ điển tra cứu cho các cụm từ ngắn đã chuẩn hóa: tiện ích của không gian, hạng mục
 * của gói tiệc.
 *
 * Quy tắc phân chia trong dự án: nội dung tự do theo từng dòng (tên, mô tả) thì thêm cột
 * tiếng Anh trong cơ sở dữ liệu; còn cụm từ ngắn lấy đi lấy lại từ một danh sách cố định
 * thì tra ở đây. Lý do: cả hệ thống chỉ có 18 tiện ích khác nhau dùng chung cho 6 không
 * gian, thêm cột thì phải lưu lại cùng một chữ "Máy lạnh" ở nhiều dòng rồi sửa một chỗ
 * lại quên chỗ kia.
 *
 * Tra không thấy thì trả lại nguyên chữ tiếng Việt. Quản trị viên thêm một tiện ích mới
 * ngoài danh sách này thì khách xem bản tiếng Anh vẫn thấy chữ tiếng Việt — xấu, nhưng
 * còn hơn là hiện ra ô trống hoặc báo lỗi.
 */
public final class NoiDungSongNgu {

    private static final Map<String, String> TIEN_ICH = new LinkedHashMap<>();
    private static final Map<String, String> HANG_MUC_GOI = new LinkedHashMap<>();
    private static final Map<String, String> NGAY_LE = new LinkedHashMap<>();

    static {
        // Sức chứa
        TIEN_ICH.put("20-60 khách", "20–60 guests");
        TIEN_ICH.put("40-150 khách", "40–150 guests");
        TIEN_ICH.put("60-150 khách", "60–150 guests");
        TIEN_ICH.put("200-500 khách", "200–500 guests");
        TIEN_ICH.put("300-800 khách", "300–800 guests");
        TIEN_ICH.put("8-12 khách/chòi", "8–12 guests per hut");

        // Trang thiết bị
        TIEN_ICH.put("Máy lạnh", "Air conditioning");
        TIEN_ICH.put("Máy chiếu 4K", "4K projector");
        TIEN_ICH.put("LED 6x3m", "6x3m LED wall");
        TIEN_ICH.put("Sân khấu 8m", "8m stage");
        TIEN_ICH.put("Karaoke", "Karaoke");

        // Đặc điểm không gian
        TIEN_ICH.put("Có mái che", "Covered");
        TIEN_ICH.put("Phòng kín", "Enclosed room");
        TIEN_ICH.put("Trên mặt nước", "Over the water");
        TIEN_ICH.put("Góc chụp đẹp", "Photogenic spots");

        // Hình thức phục vụ
        TIEN_ICH.put("Đặt lẻ", "Single-unit booking");
        TIEN_ICH.put("Tiệc đứng", "Standing buffet");
        TIEN_ICH.put("Teabreak", "Tea break");

        // Hạng mục trong gói tiệc, cũng là cụm từ chuẩn hóa nên tra cùng một kiểu
        HANG_MUC_GOI.put("Trang trí bàn cơ bản, hoa tươi", "Basic table setting with fresh flowers");
        HANG_MUC_GOI.put("Nước sâm & trà đá không giới hạn", "Unlimited herbal tea and iced tea");
        HANG_MUC_GOI.put("Sử dụng không gian 3 tiếng", "Three hours of venue use");
        HANG_MUC_GOI.put("Thực đơn 6 món + tráng miệng", "Six-course menu plus dessert");
        HANG_MUC_GOI.put("Nhân viên phục vụ 1 người/2 bàn", "One server for every two tables");
        HANG_MUC_GOI.put("Thực đơn 8 món có hải sản", "Eight-course menu including seafood");
        HANG_MUC_GOI.put("Cổng hoa, backdrop & bàn gallery", "Flower arch, backdrop and gallery table");
        HANG_MUC_GOI.put("MC dẫn chương trình 2 tiếng", "Two hours of professional hosting");
        HANG_MUC_GOI.put("Âm thanh, ánh sáng sân khấu", "Stage sound and lighting");
        HANG_MUC_GOI.put("Bánh kem & tháp ly champagne", "Celebration cake and champagne tower");
        HANG_MUC_GOI.put("Sử dụng không gian 5 tiếng", "Five hours of venue use");
        HANG_MUC_GOI.put("Thực đơn 10 món chọn theo yêu cầu", "Ten-course menu chosen to order");
        HANG_MUC_GOI.put("Trang trí concept riêng, hoa nhập", "Bespoke styling with imported flowers");
        HANG_MUC_GOI.put("MC + ban nhạc acoustic 3 người", "Host plus a three-piece acoustic band");
        HANG_MUC_GOI.put("Quay phim & chụp ảnh phóng sự", "Documentary-style photography and video");
        HANG_MUC_GOI.put("Xe điện đón khách, lễ tân riêng", "Electric shuttle and a dedicated reception desk");
        HANG_MUC_GOI.put("Sử dụng không gian trọn ngày", "Full-day venue use");

        // Hạng mục của năm gói thêm ở V23
        HANG_MUC_GOI.put("Thực đơn 5 món theo mùa", "Five-course seasonal menu");
        HANG_MUC_GOI.put("Trà đá & nước ngọt không giới hạn", "Unlimited iced tea and soft drinks");
        HANG_MUC_GOI.put("Bàn ghế, khăn trải bàn tiêu chuẩn", "Standard tables, chairs and linen");
        HANG_MUC_GOI.put("Sử dụng không gian 2 tiếng", "Two hours of venue use");
        HANG_MUC_GOI.put("Thực đơn 7 món, có món chay tùy chọn", "Seven-course menu with optional vegetarian dishes");
        HANG_MUC_GOI.put("Trang trí bàn tiệc theo tông màu chọn trước", "Table styling in a colour scheme chosen in advance");
        HANG_MUC_GOI.put("Bánh kem và nước ngọt cho khách nhỏ tuổi", "Celebration cake and soft drinks for younger guests");
        HANG_MUC_GOI.put("Sử dụng không gian 4 tiếng", "Four hours of venue use");
        HANG_MUC_GOI.put("Thực đơn 9 món, có lẩu cuối tiệc", "Nine-course menu ending with a hot pot");
        HANG_MUC_GOI.put("Bia và nước ngọt không giới hạn trong 2 tiếng", "Unlimited beer and soft drinks for two hours");
        HANG_MUC_GOI.put("Màn hình LED chiếu video tổng kết năm", "LED screen for the year-in-review video");
        HANG_MUC_GOI.put("Bốc thăm trúng thưởng, quà cho nhân viên", "Lucky draw setup and gifts for staff");
        HANG_MUC_GOI.put("Thực đơn 8 món phục vụ theo suất", "Eight-course menu served as individual plates");
        HANG_MUC_GOI.put("Hai lần teabreak giữa giờ", "Two tea breaks between sessions");
        HANG_MUC_GOI.put("Máy chiếu, màn chiếu và bục phát biểu", "Projector, screen and speaker's lectern");
        HANG_MUC_GOI.put("Micro không dây và kỹ thuật viên trực", "Wireless microphones with a technician on hand");
        HANG_MUC_GOI.put("Bảng tên, bàn đón tiếp và nước suối tại chỗ", "Name cards, a welcome desk and bottled water at each seat");
        HANG_MUC_GOI.put("Thực đơn 12 món, bàn thử món trước ngày cưới", "Twelve-course menu with a tasting before the wedding day");
        HANG_MUC_GOI.put("Xe hoa và phòng chờ riêng cho cô dâu chú rể", "Wedding car and a private suite for the couple");
        HANG_MUC_GOI.put("Điều phối viên theo sát từ đón khách đến tiễn khách", "A coordinator on hand from arrival to farewell");

        // Ngày lễ: chỉ năm cái tên lặp lại qua các năm nên cũng tra bằng từ điển
        NGAY_LE.put("Tết Dương lịch", "New Year's Day");
        NGAY_LE.put("Tết Nguyên Đán", "Lunar New Year");
        NGAY_LE.put("Giỗ Tổ Hùng Vương", "Hung Kings Commemoration Day");
        NGAY_LE.put("Giải phóng miền Nam và Quốc tế Lao động", "Reunification Day and Labour Day");
        NGAY_LE.put("Quốc khánh", "National Day");
    }

    private NoiDungSongNgu() {
    }

    public static String tienIch(String viet) {
        if (viet == null) return null;
        return TIEN_ICH.getOrDefault(viet, viet);
    }

    public static List<String> tienIch(List<String> viet) {
        if (viet == null) return List.of();
        return viet.stream().map(NoiDungSongNgu::tienIch).toList();
    }

    public static String ngayLe(String viet) {
        if (viet == null) return null;
        return NGAY_LE.getOrDefault(viet, viet);
    }

    public static List<String> hangMucGoi(List<String> viet) {
        if (viet == null) return List.of();
        return viet.stream().map(c -> c == null ? null : HANG_MUC_GOI.getOrDefault(c, c)).toList();
    }
}
