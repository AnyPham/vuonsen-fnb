package vn.vuonsen.kiemthu.coso;

import org.openqa.selenium.json.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Chuẩn bị dữ liệu thử bằng cách gọi thẳng API của backend.
 *
 * Nhiều kịch bản cần dữ liệu có sẵn trước khi bắt đầu: gửi đánh giá cần một đơn đã hoàn
 * thành, thử trùng lịch cần một đơn đã xác nhận. Tạo những thứ đó bằng cách bấm qua giao
 * diện thì kịch bản dài gấp mấy lần, chậm, và hỏng ở bước chuẩn bị sẽ bị hiểu nhầm là hỏng
 * ở chức năng đang kiểm thử. Gọi API thì nhanh và tách bạch: kịch bản chỉ dùng giao diện
 * cho đúng phần mình kiểm thử.
 *
 * Lỗi ở bước chuẩn bị ném IllegalStateException kèm nội dung phản hồi, để phân biệt rõ với
 * lỗi khẳng định của kịch bản.
 */
public final class GoiApi {

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final Json JSON = new Json();
    private static final DateTimeFormatter GIO_PHUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /*
     * Mỗi lần xin một ngày mới thì tăng một, để các đơn tạo qua API trong cùng một lần chạy
     * không đè lịch nhau. Bắt đầu từ 100 ngày tới cho xa mọi quy định báo trước.
     */
    private static final AtomicInteger NGAY_TIEP_THEO = new AtomicInteger(100);

    private static String tokenQuanTri;

    private GoiApi() {
    }

    /** Kết quả gọi API mà không ném lỗi, dùng khi chính việc bị từ chối là điều cần kiểm tra. */
    public record KetQua(int ma, String than) {
    }

    // ---------------- Gọi chung ----------------

    public static KetQua goiThu(String phuongThuc, String duongDan, Object than, String token) {
        HttpRequest.Builder yeuCau = HttpRequest.newBuilder(URI.create(CauHinh.API_URL + duongDan))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json");
        if (token != null) {
            yeuCau.header("Authorization", "Bearer " + token);
        }
        yeuCau.method(phuongThuc, than == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(JSON.toJson(than)));
        try {
            HttpResponse<String> phanHoi = HTTP.send(yeuCau.build(), HttpResponse.BodyHandlers.ofString());
            return new KetQua(phanHoi.statusCode(), phanHoi.body());
        } catch (IOException e) {
            throw new IllegalStateException("Không gọi được " + duongDan + ": "
                    + e.getClass().getSimpleName() + " " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bị ngắt khi gọi " + duongDan, e);
        }
    }

    private static Object goi(String phuongThuc, String duongDan, Object than, String token) {
        KetQua kq = goiThu(phuongThuc, duongDan, than, token);
        if (kq.ma() >= 400) {
            throw new IllegalStateException("Chuẩn bị dữ liệu thất bại: %s %s -> HTTP %d %s"
                    .formatted(phuongThuc, duongDan, kq.ma(), kq.than()));
        }
        return kq.than() == null || kq.than().isBlank() ? null : JSON.toType(kq.than(), Object.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> doiTuong(Object ketQua) {
        return (Map<String, Object>) ketQua;
    }

    /** Danh sách trả về có thể là mảng, hoặc trang có trường content; xử lý được cả hai. */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> danhSach(Object ketQua) {
        if (ketQua instanceof Map<?, ?> m && m.get("content") instanceof List<?> ds) {
            return (List<Map<String, Object>>) ds;
        }
        return (List<Map<String, Object>>) ketQua;
    }

    public static long so(Map<String, Object> doiTuong, String khoa) {
        return ((Number) doiTuong.get(khoa)).longValue();
    }

    /*
     * Một ngày tổ chức chưa có đơn nào, cách hôm nay hơn 100 ngày.
     *
     * Bộ đếm bắt đầu lại từ đầu mỗi lần chạy, còn dữ liệu trên backend vẫn giữ nguyên nếu chưa
     * khởi động lại. Chạy lại lần hai sẽ trúng đúng những ngày lần trước đã xác nhận đơn và bị
     * từ chối vì trùng lịch. Nên ngày nào cũng hỏi lại backend, ngày đã có đơn thì bỏ qua.
     */
    public static synchronized LocalDate ngayChuaDung() {
        for (int lan = 0; lan < 1000; lan++) {
            LocalDate ngay = LocalDate.now().plusDays(NGAY_TIEP_THEO.incrementAndGet());
            Map<String, Object> trang = doiTuong(goi("GET",
                    "/api/v1/admin/bookings?from=" + ngay + "&to=" + ngay + "&size=1", null, tokenQuanTri()));
            if (so(trang, "totalElements") == 0) {
                return ngay;
            }
        }
        throw new IllegalStateException("Chuẩn bị dữ liệu thất bại: không tìm được ngày trống trong 1000 ngày");
    }

    // ---------------- Tài khoản ----------------

    public static String dangNhap(String email, String matKhau) {
        return (String) doiTuong(goi("POST", "/api/auth/login",
                Map.of("email", email, "password", matKhau), null)).get("accessToken");
    }

    public static synchronized String tokenQuanTri() {
        if (tokenQuanTri == null) {
            tokenQuanTri = dangNhap(CauHinh.EMAIL_QUAN_TRI, CauHinh.MAT_KHAU_QUAN_TRI);
        }
        return tokenQuanTri;
    }

    /** Đăng ký tài khoản khách mới, trả về access token của tài khoản đó. */
    public static String dangKyKhach(String hoTen, String email, String matKhau) {
        return (String) doiTuong(goi("POST", "/api/auth/register",
                Map.of("fullName", hoTen, "email", email, "password", matKhau), null)).get("accessToken");
    }

    // ---------------- Không gian và gói tiệc ----------------

    public static long idKhongGian(String slug) {
        return so(doiTuong(goi("GET", "/api/v1/spaces/" + slug, null, null)), "id");
    }

    public static long idGoi(String tenGoi) {
        return danhSach(goi("GET", "/api/v1/packages", null, null)).stream()
                .filter(g -> tenGoi.equals(g.get("name")))
                .findFirst()
                .map(g -> so(g, "id"))
                .orElseThrow(() -> new IllegalStateException("Dữ liệu mẫu không có " + tenGoi));
    }

    // ---------------- Đơn đặt tiệc ----------------

    private static Map<String, Object> thanDonTiec(String slug, String tenGoi, LocalDate ngay, String buoi, int soKhach) {
        Map<String, Object> than = new HashMap<>();
        than.put("eventType", "OTHER");
        than.put("eventDate", ngay.toString());
        than.put("timeSlot", buoi);
        than.put("guestCount", soKhach);
        than.put("spaceId", idKhongGian(slug));
        than.put("packageId", tenGoi == null ? null : idGoi(tenGoi));
        than.put("customerName", "Khách dữ liệu thử");
        than.put("customerPhone", "0901234567");
        return than;
    }

    /** Tạo đơn đặt tiệc. token để null nghĩa là khách không đăng nhập. */
    public static Map<String, Object> taoDonTiec(String token, String slug, String tenGoi,
                                                 LocalDate ngay, String buoi, int soKhach) {
        return doiTuong(goi("POST", "/api/v1/bookings", thanDonTiec(slug, tenGoi, ngay, buoi, soKhach), token));
    }

    /** Thử tạo đơn đặt tiệc mà không ném lỗi, để kiểm tra trường hợp bị từ chối. */
    public static KetQua thuTaoDonTiec(String slug, String tenGoi, LocalDate ngay, String buoi, int soKhach) {
        return goiThu("POST", "/api/v1/bookings", thanDonTiec(slug, tenGoi, ngay, buoi, soKhach), null);
    }

    public static void doiTrangThaiDonTiec(long id, String trangThai) {
        goi("PATCH", "/api/v1/admin/bookings/" + id + "/status",
                Map.of("status", trangThai, "note", "Dữ liệu thử"), tokenQuanTri());
    }

    /** Một đơn đã tổ chức xong, đủ điều kiện để khách gửi đánh giá. */
    public static Map<String, Object> taoDonTiecDaHoanThanh() {
        Map<String, Object> don = taoDonTiec(null, "sanh-sen-vang", "Gói Đồng Quê", ngayChuaDung(), "EVENING", 200);
        doiTrangThaiDonTiec(so(don, "id"), "CONFIRMED");
        doiTrangThaiDonTiec(so(don, "id"), "COMPLETED");
        return don;
    }

    // ---------------- Thực đơn ----------------

    public static Map<String, Object> monTheoTen(String ten) {
        return danhSach(goi("GET", "/api/v1/menu/dishes", null, null)).stream()
                .filter(m -> ten.equals(m.get("name")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Thực đơn không có món " + ten));
    }

    /** Chi tiết một món theo slug, có đủ nguyên liệu, khẩu phần và thời gian chuẩn bị. */
    public static Map<String, Object> chiTietMon(String slug) {
        return doiTuong(goi("GET", "/api/v1/menu/dishes/" + slug, null, null));
    }

    public static long idDanhMuc(String maDanhMuc) {
        return danhSach(goi("GET", "/api/v1/menu/categories", null, null)).stream()
                .filter(d -> maDanhMuc.equals(d.get("code")))
                .findFirst()
                .map(d -> so(d, "id"))
                .orElseThrow(() -> new IllegalStateException("Không có danh mục " + maDanhMuc));
    }

    private static Map<String, Object> thanMon(String ten, String maDanhMuc, long gia) {
        Map<String, Object> than = new HashMap<>();
        than.put("categoryId", idDanhMuc(maDanhMuc));
        than.put("name", ten);
        than.put("description", "Món tạo cho kiểm thử tự động");
        than.put("price", gia);
        than.put("available", true);
        than.put("bestSeller", false);
        than.put("sortOrder", 99);
        return than;
    }

    /** Tạo một món riêng cho kịch bản, để sửa hay ngừng bán cũng không đụng tới món mẫu. */
    public static Map<String, Object> taoMon(String ten, String maDanhMuc, long gia) {
        return doiTuong(goi("POST", "/api/v1/admin/dishes", thanMon(ten, maDanhMuc, gia), tokenQuanTri()));
    }

    public static void doiGiaMon(long id, String ten, String maDanhMuc, long giaMoi) {
        goi("PUT", "/api/v1/admin/dishes/" + id, thanMon(ten, maDanhMuc, giaMoi), tokenQuanTri());
    }

    public static void ngungBanMon(long id) {
        goi("DELETE", "/api/v1/admin/dishes/" + id, null, tokenQuanTri());
    }

    /*
     * Tiền tố tên của những món do kiểm thử sinh ra.
     *
     * Dọn theo tên chứ không theo mã món đã ghi lại lúc tạo, vì TC-ADMIN-08 tạo món qua
     * giao diện quản trị chứ không qua API nên không có mã để ghi. Dọn theo tên còn được
     * thêm một cái lợi: quét luôn những món sót lại của các lần chạy trước đó.
     */
    private static final List<String> TIEN_TO_MON_KIEM_THU =
            List.of("Món thử giá ", "Món thử ngừng bán ", "Chè kiểm thử ");

    /*
     * Xóa hẳn những món do kiểm thử sinh ra.
     *
     * Không có bước này thì mỗi lần chạy bộ kiểm thử lại để lại vài món tên kiểu
     * "Món thử giá 317610324460300" nằm lẫn giữa thực đơn thật, khách vào web đọc thấy.
     * Bỏ qua mọi lỗi: dọn dẹp mà ném lỗi thì kịch bản vừa chạy đúng lại bị báo trượt oan.
     */
    /*
     * Xóa những đánh giá do kiểm thử gửi lên.
     *
     * TC-ADMIN-07 gửi một đánh giá rồi duyệt nó, nghĩa là sau mỗi lần chạy lại có thêm
     * một dòng "Đánh giá chờ duyệt 7900874587700" hiện công khai ở trang đánh giá, và nó
     * còn kéo điểm trung bình lệch đi vì luôn cho 5 sao.
     */
    public static void donDanhGiaKiemThu() {
        /*
         * Quét cả hai danh sách: chờ duyệt và đã duyệt.
         *
         * Endpoint danh sách đánh giá mặc định chỉ trả về đánh giá chờ duyệt. Lần đầu viết
         * hàm này chỉ gọi mặc định nên dọn hụt đúng cái cần dọn: TC-ADMIN-07 duyệt đánh giá
         * rồi mới xong, nên lúc dọn nó đã nằm ở danh sách đã duyệt và hiện công khai.
         */
        for (boolean daDuyet : new boolean[] { false, true }) {
            donDanhGiaTheoTrangThai(daDuyet);
        }
    }

    private static void donDanhGiaTheoTrangThai(boolean daDuyet) {
        try {
            String duongDan = "/api/v1/admin/reviews?approved=" + daDuyet + "&page=0&size=200";
            Object ketQua = goi("GET", duongDan, null, tokenQuanTri());
            Map<String, Object> trang = doiTuong(ketQua);
            Object noiDung = trang.get("content");
            if (!(noiDung instanceof List<?> danhSach)) {
                return;
            }
            for (Object phanTu : danhSach) {
                if (!(phanTu instanceof Map<?, ?> danhGia)) {
                    continue;
                }
                String noi = String.valueOf(danhGia.get("content"));
                // Nhận ra đánh giá của kiểm thử nhờ dãy số nanoTime nối sau nội dung
                if (!coDaySoDai(noi)) {
                    continue;
                }
                try {
                    goi("DELETE", "/api/v1/admin/reviews/" + so(castMap(danhGia), "id"), null, tokenQuanTri());
                } catch (RuntimeException boQua) {
                    // Xóa không được thì bỏ qua, dọn dẹp không được phép làm trượt kịch bản
                }
            }
        } catch (RuntimeException boQua) {
            // Backend chưa chạy thì thôi
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> nguon) {
        return (Map<String, Object>) nguon;
    }

    /*
     * Chuỗi có chứa một dãy từ 10 chữ số liền nhau trở lên không.
     *
     * Đây là dấu vết của System.nanoTime() mà các kịch bản nối vào tên và nội dung để mỗi
     * lần chạy ra một giá trị khác nhau. Dữ liệu thật không bao giờ có dãy số dài như vậy,
     * nên đây là cách nhận diện đáng tin. Viết bằng vòng lặp thay vì biểu thức chính quy
     * cho dễ đọc, và khỏi phải nhớ quy tắc thoát ký tự trong chuỗi Java.
     */
    private static boolean coDaySoDai(String chuoi) {
        int lienTiep = 0;
        for (int i = 0; i < chuoi.length(); i++) {
            lienTiep = Character.isDigit(chuoi.charAt(i)) ? lienTiep + 1 : 0;
            if (lienTiep >= 10) {
                return true;
            }
        }
        return false;
    }

    public static void donMonKiemThu() {
        try {
            for (Map<String, Object> mon : danhSach(goi("GET", "/api/v1/admin/dishes", null, tokenQuanTri()))) {
                String ten = String.valueOf(mon.get("name"));
                if (TIEN_TO_MON_KIEM_THU.stream().noneMatch(ten::startsWith)) {
                    continue;
                }
                long id = so(mon, "id");
                try {
                    goi("DELETE", "/api/v1/admin/dishes/" + id + "/vinh-vien", null, tokenQuanTri());
                } catch (RuntimeException chuaXoaHanDuoc) {
                    /*
                     * Món đã nằm trong đơn thì backend từ chối xóa hẳn, đúng như thiết kế: hóa đơn
                     * cũ còn phải tra ra được tên món. Kịch bản TC-DATMON-09 rơi đúng vào trường
                     * hợp này vì nó tạo món rồi đặt luôn món đó.
                     *
                     * Lùi về ngừng bán: món biến khỏi thực đơn công khai nên khách không nhìn thấy
                     * nữa, mà dòng dữ liệu vẫn còn cho đơn cũ tra cứu.
                     */
                    ngungBanMon(id);
                }
            }
        } catch (RuntimeException boQua) {
            // Backend không chạy hoặc không đăng nhập được: đó là việc của kịch bản, không phải của bước dọn
        }
    }

    /** Một dòng giỏ món theo đúng dạng giao diện lưu trong localStorage, lấy theo tên món mẫu. */
    public static Map<String, Object> dongGioMon(String tenMon, int soLuong) {
        return dongGioMon(monTheoTen(tenMon), soLuong);
    }

    /** Một dòng giỏ món từ dữ liệu món, dùng được cho cả món vừa tạo trong kịch bản. */
    public static Map<String, Object> dongGioMon(Map<String, Object> mon, int soLuong) {
        Map<String, Object> dong = new HashMap<>();
        dong.put("dishId", mon.get("id"));
        dong.put("name", mon.get("name"));
        dong.put("price", mon.get("price"));
        dong.put("slug", mon.get("slug"));
        dong.put("imageUrl", mon.get("imageUrl"));
        dong.put("quantity", soLuong);
        return dong;
    }

    // ---------------- Đánh giá ----------------

    /** Gửi đánh giá cho một đơn đã hoàn thành. Dùng tài khoản quản trị vì API cần đăng nhập. */
    public static void guiDanhGia(String maDon, String tenHienThi, int soSao, String noiDung) {
        goi("POST", "/api/v1/reviews", Map.of("bookingCode", maDon, "customerName", tenHienThi,
                "rating", soSao, "content", noiDung), tokenQuanTri());
    }

    // ---------------- Đơn đặt món ----------------

    /*
     * Tạo đơn giao tận nhà một món, nhận vào trưa ngày thường gần nhất kể từ ngày mai.
     * Người gọi tự bảo đảm đơn đạt mức giao tối thiểu. Tránh dịp lễ để số tiền không bị giảm.
     */
    public static Map<String, Object> taoDonMonGiao(long idMon, int soLuong) {
        return taoDonMonGiao(idMon, soLuong, ngayThuong(LocalDate.now().plusDays(1), 1).atTime(12, 0));
    }

    /** Tạo đơn giao tận nhà một món, nhận vào thời điểm cho trước. */
    public static Map<String, Object> taoDonMonGiao(long idMon, int soLuong, LocalDateTime nhanLuc) {
        Map<String, Object> than = new HashMap<>();
        than.put("fulfillmentType", "DELIVERY");
        than.put("customerName", "Khách dữ liệu thử");
        than.put("customerPhone", "0901234567");
        than.put("deliveryAddress", "1 Đường Kiểm Thử, Bình Thạnh");
        than.put("serveAt", nhanLuc.format(GIO_PHUT));
        than.put("items", List.of(Map.of("dishId", idMon, "quantity", soLuong)));
        return doiTuong(goi("POST", "/api/v1/dish-orders", than, null));
    }

    // ---------------- Giảm giá ngày lễ ----------------

    /** Tên các dịp lễ do kịch bản tạo đều bắt đầu bằng chuỗi này, để dọn được sau kịch bản. */
    public static final String TIEN_TO_DIP_LE = "KT ";

    public static List<Map<String, Object>> danhSachDipLe() {
        return danhSach(goi("GET", "/api/v1/admin/holidays", null, tokenQuanTri()));
    }

    /** Tạo dịp lễ. tiLe dạng thập phân: 0.15 là giảm 15%. */
    public static Map<String, Object> taoDipLe(String ten, LocalDate tuNgay, LocalDate denNgay,
                                               double tiLe, boolean dangApDung) {
        Map<String, Object> than = new HashMap<>();
        than.put("name", ten);
        than.put("startDate", tuNgay.toString());
        than.put("endDate", denNgay.toString());
        than.put("discountRate", tiLe);
        than.put("active", dangApDung);
        return doiTuong(goi("POST", "/api/v1/admin/holidays", than, tokenQuanTri()));
    }

    /*
     * Xóa mọi dịp lễ do kịch bản tạo.
     *
     * Dịp lễ làm đổi giá của mọi kịch bản khác, nên một dịp sót lại sau lần chạy bị ngắt giữa
     * chừng sẽ làm lệch số tiền ở lần chạy sau. Gọi trước và sau mỗi kịch bản có dùng dịp lễ.
     */
    public static void donDipLeKiemThu() {
        danhSachDipLe().stream()
                .filter(h -> String.valueOf(h.get("name")).startsWith(TIEN_TO_DIP_LE))
                .forEach(h -> goi("DELETE", "/api/v1/admin/holidays/" + so(h, "id"), null, tokenQuanTri()));
    }

    private static boolean coDipLeVao(List<Map<String, Object>> cacDip, LocalDate ngay) {
        return cacDip.stream().anyMatch(h -> Boolean.TRUE.equals(h.get("active"))
                && !ngay.isBefore(LocalDate.parse((String) h.get("startDate")))
                && !ngay.isAfter(LocalDate.parse((String) h.get("endDate"))));
    }

    /*
     * Ngày thường gần nhất tính từ ngày cho trước: buoc 1 là tìm về sau, -1 là tìm về trước.
     *
     * Các kịch bản so số tiền cố định phải tránh dịp lễ. Chạy kiểm thử đúng dịp lễ thì tiệc
     * cách 30 ngày có thể rơi vào dịp giảm giá, con số mong đợi sẽ không còn đúng.
     */
    public static LocalDate ngayThuong(LocalDate tuNgay, int buoc) {
        List<Map<String, Object>> cacDip = danhSachDipLe();
        LocalDate ngay = tuNgay;
        while (coDipLeVao(cacDip, ngay)) {
            ngay = ngay.plusDays(buoc);
        }
        return ngay;
    }

    /** Ngày đầu của soNgay ngày liên tiếp không trùng dịp lễ nào, tìm từ ngày cho trước trở đi. */
    public static LocalDate dauKhoangNgayThuong(LocalDate tuNgay, int soNgay) {
        List<Map<String, Object>> cacDip = danhSachDipLe();
        LocalDate dau = tuNgay;
        while (true) {
            int k = 0;
            while (k < soNgay && !coDipLeVao(cacDip, dau.plusDays(k))) {
                k++;
            }
            if (k == soNgay) {
                return dau;
            }
            dau = dau.plusDays(k + 1L);
        }
    }
}
