package vn.vuonsen.kiemthu.coso;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Cấu hình của bộ kiểm thử, đọc từ tham số dòng lệnh hoặc biến môi trường.
 *
 * Không ghi cứng địa chỉ hay tài khoản vào kịch bản. Chạy trên máy cá nhân thì dùng giá
 * trị mặc định; chạy trên GitHub Actions thì truyền giá trị khác vào mà không phải sửa mã.
 * Thứ tự ưu tiên: tham số -D trên dòng lệnh, rồi biến môi trường, rồi giá trị mặc định.
 */
public final class CauHinh {

    /** Địa chỉ giao diện cần kiểm thử, không có dấu gạch chéo ở cuối. */
    public static final String BASE_URL =
            doc("base.url", "KIEMTHU_BASE_URL", "http://localhost:5173").replaceAll("/+$", "");

    /*
     * Địa chỉ backend để chuẩn bị dữ liệu thử qua API.
     *
     * Gọi thẳng backend chứ không đi vòng qua proxy của máy chủ giao diện. Máy chủ Vite chỉ
     * lắng nghe trên địa chỉ IPv6 ::1; trình duyệt tự thử cả hai họ địa chỉ nên vẫn vào được,
     * còn HttpClient của Java ưu tiên 127.0.0.1 nên bị từ chối kết nối. Backend lắng nghe trên
     * mọi địa chỉ nên không vướng chuyện này.
     */
    public static final String API_URL =
            doc("api.url", "KIEMTHU_API_URL", "http://localhost:8080").replaceAll("/+$", "");

    /** Chạy ẩn trình duyệt. Trên máy chủ tích hợp liên tục (có biến CI) thì mặc định bật. */
    public static final boolean CHAY_AN = Boolean.parseBoolean(
            doc("headless", "KIEMTHU_HEADLESS", System.getenv("CI") != null ? "true" : "false"));

    /** Trình duyệt dùng để kiểm thử: chrome hoặc edge. */
    public static final String TRINH_DUYET = doc("browser", "KIEMTHU_BROWSER", "chrome");

    /**
     * Thời gian chờ tối đa cho một điều kiện. Chỉ là mức trần: điều kiện thỏa sớm thì đi
     * tiếp ngay, nên đặt rộng tay cũng không làm bộ kiểm thử chậm đi.
     */
    public static final Duration CHO_TOI_DA =
            Duration.ofSeconds(Long.parseLong(doc("cho.giay", "KIEMTHU_CHO_GIAY", "10")));

    /** Tài khoản quản trị do backend tự tạo khi khởi động lần đầu. */
    public static final String EMAIL_QUAN_TRI = doc("admin.email", "KIEMTHU_ADMIN_EMAIL", "admin@vuonsen.vn");
    public static final String MAT_KHAU_QUAN_TRI = doc("admin.password", "KIEMTHU_ADMIN_PASSWORD", "Admin@123");

    /*
     * Chế độ trình diễn, bật khi quay video minh họa: -Ddemo=true.
     *
     * Bình thường kịch bản bấm và gõ nhanh hơn mắt người theo kịp. Bật chế độ này thì trước
     * mỗi thao tác, phần tử sắp bị tác động được tô viền đỏ và dừng một nhịp. Mặc định tắt;
     * chạy kiểm thử thật, nhất là trên GitHub Actions, không bao giờ bật.
     */
    public static final boolean CHE_DO_DEMO = Boolean.parseBoolean(doc("demo", "KIEMTHU_DEMO", "false"));

    /** Thời gian dừng trước mỗi thao tác ở chế độ trình diễn, tính bằng mili giây: -Ddemo.dung=800. */
    public static final long DEMO_DUNG_MS = Long.parseLong(doc("demo.dung", "KIEMTHU_DEMO_DUNG", "800"));

    /*
     * Quay khung trình duyệt trong lúc chạy để làm video trình diễn: -Dquay=true.
     *
     * Khung hình của mỗi lần chạy lưu ở target/video/<tên lần chạy>/khung, sau đó ghép thành video
     * bằng công cụ GhepVideo. Tên lần chạy mặc định là thời điểm khởi động, đặt tay bằng -Dquay.lan.
     */
    public static final boolean QUAY_VIDEO = Boolean.parseBoolean(doc("quay", "KIEMTHU_QUAY", "false"));

    public static final Path THU_MUC_VIDEO = Path.of("target", "video", doc("quay.lan", "KIEMTHU_QUAY_LAN",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))));

    /** Nơi lưu ảnh chụp màn hình khi kịch bản trượt. Nằm trong target nên không lọt vào git. */
    public static final Path THU_MUC_ANH_LOI = Path.of("target", "anh-loi");

    private CauHinh() {
    }

    private static String doc(String thamSo, String bienMoiTruong, String macDinh) {
        String giaTri = System.getProperty(thamSo);
        if (giaTri == null || giaTri.isBlank()) {
            giaTri = System.getenv(bienMoiTruong);
        }
        return (giaTri == null || giaTri.isBlank()) ? macDinh : giaTri.trim();
    }
}
