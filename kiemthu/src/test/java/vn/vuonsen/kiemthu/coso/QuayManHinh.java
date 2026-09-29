package vn.vuonsen.kiemthu.coso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.json.Json;

import java.io.BufferedWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Quay khung trình duyệt bằng Chrome DevTools trong lúc kịch bản chạy, dùng để làm video trình diễn.
 *
 * Selenium mở Chrome kèm một cổng gỡ lỗi. Lớp này mở thêm một kết nối DevTools riêng tới đúng tab
 * đó và bật Page.startScreencast: mỗi lần trang vẽ lại, Chrome gửi về một khung hình. Kết nối này
 * tách hẳn khỏi WebDriver nên không chen vào lệnh của kịch bản và không làm đổi kết quả kiểm thử.
 *
 * Mỗi khung lưu thành một tệp JPEG, kèm thời điểm Chrome vẽ khung đó, để lúc ghép video giữ đúng
 * nhịp thật. Chỉ bật khi -Dquay=true. Quay hỏng thì chỉ in cảnh báo, không làm trượt kịch bản.
 */
public final class QuayManHinh {

    private static final Json JSON = new Json();

    private final Path thuMuc;
    private final BufferedWriter danhMuc;
    private final Map<String, Object> thongTin;
    private final AtomicInteger soKhung = new AtomicInteger();
    private final AtomicInteger maLenh = new AtomicInteger();
    private WebSocket ketNoi;

    private QuayManHinh(Path thuMuc, Map<String, Object> thongTin) throws Exception {
        this.thuMuc = thuMuc;
        this.thongTin = thongTin;
        Files.createDirectories(thuMuc);
        this.danhMuc = Files.newBufferedWriter(thuMuc.resolve("khung.txt"), StandardCharsets.UTF_8);
    }

    /** Bắt đầu quay cho một kịch bản. Trả về null nếu không quay được. */
    public static QuayManHinh batDau(WebDriver driver, TestInfo kichBan) {
        try {
            String lop = kichBan.getTestClass().map(Class::getSimpleName).orElse("KhongRo");
            String luong = kichBan.getTestClass().map(c -> c.getAnnotation(DisplayName.class))
                    .map(DisplayName::value).orElse(lop);
            String phuongThuc = kichBan.getTestMethod().map(m -> m.getName()).orElse("khongRo");

            Map<String, Object> thongTin = new LinkedHashMap<>();
            thongTin.put("tenHienThi", kichBan.getDisplayName());
            thongTin.put("luong", luong);
            thongTin.put("lop", lop);
            thongTin.put("phuongThuc", phuongThuc);
            thongTin.put("batDau", System.currentTimeMillis());

            QuayManHinh quay = new QuayManHinh(
                    CauHinh.THU_MUC_VIDEO.resolve("khung").resolve(lop + "_" + phuongThuc), thongTin);
            quay.ketNoi(diaChiGoLoi(driver));
            return quay;
        } catch (Exception loi) {
            System.out.println("[quay] Không bật được quay khung trình duyệt: " + loi);
            return null;
        }
    }

    private static String diaChiGoLoi(WebDriver driver) {
        Capabilities kha = ((HasCapabilities) driver).getCapabilities();
        for (String khoa : List.of("goog:chromeOptions", "ms:edgeOptions")) {
            if (kha.getCapability(khoa) instanceof Map<?, ?> m && m.get("debuggerAddress") != null) {
                return m.get("debuggerAddress").toString();
            }
        }
        throw new IllegalStateException("Trình duyệt không mở cổng gỡ lỗi");
    }

    @SuppressWarnings("unchecked")
    private void ketNoi(String diaChi) throws Exception {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        String json = http.send(HttpRequest.newBuilder(URI.create("http://" + diaChi + "/json")).build(),
                HttpResponse.BodyHandlers.ofString()).body();
        String duongWs = ((List<Map<String, Object>>) JSON.toType(json, List.class)).stream()
                .filter(t -> "page".equals(t.get("type")))
                .map(t -> (String) t.get("webSocketDebuggerUrl"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không thấy tab nào để quay"));

        ketNoi = http.newWebSocketBuilder()
                .buildAsync(URI.create(duongWs), new NguoiNghe())
                .get(10, TimeUnit.SECONDS);
        gui("Page.startScreencast", Map.of("format", "jpeg", "quality", 75, "maxWidth", 1280, "maxHeight", 720));
    }

    // WebSocket của JDK không cho gửi chồng khi lượt gửi trước chưa xong, nên gửi tuần tự
    private synchronized void gui(String lenh, Map<String, Object> thamSo) {
        ketNoi.sendText(JSON.toJson(Map.of("id", maLenh.incrementAndGet(), "method", lenh, "params", thamSo)), true)
                .join();
    }

    @SuppressWarnings("unchecked")
    private void xuLy(String tinNhan) throws Exception {
        if (!tinNhan.contains("\"Page.screencastFrame\"")) {
            return;
        }
        Map<String, Object> thamSo = (Map<String, Object>) ((Map<String, Object>) JSON.toType(tinNhan, Map.class)).get("params");
        Map<String, Object> meta = (Map<String, Object>) thamSo.get("metadata");
        long luc = Math.round(((Number) meta.get("timestamp")).doubleValue() * 1000);
        int so = soKhung.incrementAndGet();

        String ten = String.format("%06d.jpg", so);
        Files.write(thuMuc.resolve(ten), Base64.getDecoder().decode((String) thamSo.get("data")));
        synchronized (danhMuc) {
            danhMuc.write(ten + " " + luc);
            danhMuc.newLine();
        }
        // Không báo đã nhận thì Chrome ngừng gửi khung tiếp theo
        gui("Page.screencastFrameAck", Map.of("sessionId", thamSo.get("sessionId")));
    }

    /** Dừng quay, ghi lại thông tin kịch bản. Gọi trước khi đóng trình duyệt. */
    public void dung() {
        try {
            gui("Page.stopScreencast", Map.of());
            Thread.sleep(300);
            ketNoi.sendClose(WebSocket.NORMAL_CLOSURE, "").get(3, TimeUnit.SECONDS);
        } catch (Exception loi) {
            System.out.println("[quay] Đóng kết nối quay không trọn vẹn: " + loi);
        }
        try {
            synchronized (danhMuc) {
                danhMuc.close();
            }
            thongTin.put("ketThuc", System.currentTimeMillis());
            thongTin.put("soKhung", soKhung.get());
            Files.writeString(thuMuc.resolve("thong-tin.json"), JSON.toJson(thongTin), StandardCharsets.UTF_8);
            System.out.println("[quay] " + soKhung.get() + " khung -> " + thuMuc.toAbsolutePath());
        } catch (Exception loi) {
            System.out.println("[quay] Không ghi được thông tin quay: " + loi);
        }
    }

    // Tin nhắn DevTools dài có thể tới thành nhiều mảnh, gom đủ rồi mới xử lý
    private final class NguoiNghe implements WebSocket.Listener {
        private final StringBuilder dem = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence manh, boolean cuoi) {
            dem.append(manh);
            if (cuoi) {
                String tinNhan = dem.toString();
                dem.setLength(0);
                try {
                    xuLy(tinNhan);
                } catch (Exception loi) {
                    System.out.println("[quay] Bỏ qua một khung lỗi: " + loi);
                }
            }
            ws.request(1);
            return null;
        }
    }
}
