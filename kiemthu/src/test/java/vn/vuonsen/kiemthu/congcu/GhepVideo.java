package vn.vuonsen.kiemthu.congcu;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.json.Json;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/*
 * Ghép khung hình đã quay bằng -Dquay=true thành một video trình diễn.
 *
 *   mvn -f kiemthu/pom.xml test "-Dtest=GhepVideo" "-Dvideo.lan=target/video/<lần chạy>"
 *        ["-Dvideo.thuTu=TC-BOOK-01,TC-HOL-02"] ["-Dvideo.ten=demo-kiem-thu-selenium"]
 *
 * Không cần cài phần mềm dựng video. Chrome mở một trang cục bộ, phát lại từng khung đúng nhịp đã
 * quay lên một canvas, thêm phụ đề tên test case, nhãn Đạt hoặc Không đạt và màn tổng kết, rồi
 * dùng MediaRecorder ghi canvas thành tệp MP4, hoặc WebM nếu Chrome không ghi được MP4.
 *
 * Kết quả Đạt hay Không đạt đọc từ báo cáo Surefire của chính lần chạy đã quay, đặt ở thư mục
 * bao-cao của lần chạy. Không có báo cáo thì video ghi rõ là không tìm thấy kết quả, không tự
 * suy ra. Khoảng chờ dài hơn 2 giây giữa hai khung được rút còn 2 giây, và điều này được ghi
 * ngay ở màn mở đầu của video.
 *
 * Tên lớp cố ý không khớp mẫu Surefire nên không chạy chung với bộ kiểm thử.
 */
@DisplayName("Công cụ — Ghép video trình diễn")
class GhepVideo {

    private static final Json JSON = new Json();
    private static final long TOI_DA_CHO_MS = 2000;
    private static final Pattern TEST_CASE = Pattern.compile(
            "<testcase\\s+name=\"([^\"]*)\"[^>]*?time=\"([\\d.]+)\"[^>]*?(/>|>([\\s\\S]*?)</testcase>)");
    private static final Pattern THONG_DIEP = Pattern.compile("<(?:failure|error)[^>]*message=\"([^\"]*)\"");

    @Test
    void ghep() throws Exception {
        Path lan = thuMucLanChay();
        List<Map<String, Object>> doan = docCacDoan(lan);
        Map<String, Map<String, Object>> ketQua = docKetQua(lan.resolve("bao-cao"));
        doan.forEach(d -> d.put("ketQua", ketQua.get((String) d.remove("tenHienThi"))));

        Map<String, Object> video = new LinkedHashMap<>();
        int[] boKiemThu = demBoKiemThu();
        video.put("tieuDe", "Demo kiểm thử tự động website Vườn Sen bằng Selenium WebDriver");
        video.put("moTa", "Quay trực tiếp khung trình duyệt Chrome trong lúc bộ kiểm thử chạy thật."
                + " Selenium tự mở trang, điền form, bấm nút và kiểm tra kết quả, không có thao tác tay.");
        video.put("ghiChu", "Chạy ở chế độ trình diễn: viền đỏ là phần tử sắp thao tác, viền xanh là kết quả đang được"
                + " kiểm tra, mỗi bước dừng khoảng 0,8 giây. Khoảng chờ dài hơn 2 giây giữa hai khung hình được rút còn 2 giây.");
        video.put("tongTestCase", boKiemThu[0]);
        video.put("tongLuong", boKiemThu[1]);
        video.put("toiDaCho", TOI_DA_CHO_MS);
        video.put("doan", doan);

        String ten = System.getProperty("video.ten", "demo-kiem-thu-selenium");
        long uocTinhGiay = uocTinhThoiLuong(doan) / 1000;
        System.out.printf("[ghep] %d test case, video dài khoảng %d giây, ghi theo thời gian thực%n", doan.size(), uocTinhGiay);

        CompletableFuture<Path> ketThuc = new CompletableFuture<>();
        HttpServer may = moMayChu(lan, JSON.toJson(video), ten, ketThuc);
        ChromeOptions tuyChon = new ChromeOptions().addArguments("--headless=new", "--window-size=1400,900");
        WebDriver trinhDuyet = new ChromeDriver(tuyChon);
        try {
            trinhDuyet.get("http://127.0.0.1:" + may.getAddress().getPort() + "/");
            Path tep = ketThuc.get(uocTinhGiay + 180, TimeUnit.SECONDS);
            System.out.printf("[ghep] Đã ghi %s (%d KB)%n", tep.toAbsolutePath(), Files.size(tep) / 1024);
        } finally {
            trinhDuyet.quit();
            may.stop(0);
        }
    }

    // ---------------- Dữ liệu ----------------

    private static Path thuMucLanChay() throws IOException {
        String chiDinh = System.getProperty("video.lan");
        if (chiDinh != null && !chiDinh.isBlank()) {
            return Path.of(chiDinh).toAbsolutePath().normalize();
        }
        try (Stream<Path> ds = Files.list(Path.of("target", "video"))) {
            return ds.filter(Files::isDirectory)
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .orElseThrow(() -> new IllegalStateException("Chưa có lần quay nào trong target/video"))
                    .toAbsolutePath().normalize();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> docCacDoan(Path lan) throws IOException {
        List<Map<String, Object>> doan = new ArrayList<>();
        try (Stream<Path> ds = Files.list(lan.resolve("khung"))) {
            for (Path thuMuc : ds.filter(Files::isDirectory).toList()) {
                Path tepThongTin = thuMuc.resolve("thong-tin.json");
                if (!Files.exists(tepThongTin)) {
                    continue;
                }
                Map<String, Object> tt = JSON.toType(Files.readString(tepThongTin, StandardCharsets.UTF_8), Map.class);
                String tenHienThi = (String) tt.get("tenHienThi");
                int cach = tenHienThi.indexOf(' ');

                List<String[]> dong = Files.readAllLines(thuMuc.resolve("khung.txt"), StandardCharsets.UTF_8).stream()
                        .filter(s -> !s.isBlank()).map(s -> s.trim().split(" ")).toList();
                if (dong.isEmpty()) {
                    continue;
                }
                long dau = Long.parseLong(dong.get(0)[1]);
                List<Object[]> khung = dong.stream()
                        .map(s -> new Object[] {"khung/" + thuMuc.getFileName() + "/" + s[0], Long.parseLong(s[1]) - dau})
                        .toList();

                Map<String, Object> d = new LinkedHashMap<>();
                d.put("ma", cach > 0 ? tenHienThi.substring(0, cach) : tenHienThi);
                d.put("ten", cach > 0 ? tenHienThi.substring(cach + 1) : "");
                d.put("tenHienThi", tenHienThi);
                d.put("luong", tt.get("luong"));
                d.put("batDau", tt.get("batDau"));
                d.put("khung", khung);
                doan.add(d);
            }
        }
        String thuTu = System.getProperty("video.thuTu", "");
        List<String> dsThuTu = Arrays.stream(thuTu.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        doan.sort(Comparator.<Map<String, Object>>comparingInt(d -> {
            int i = dsThuTu.indexOf((String) d.get("ma"));
            return i < 0 ? Integer.MAX_VALUE : i;
        }).thenComparingLong(d -> ((Number) d.get("batDau")).longValue()));
        if (doan.isEmpty()) {
            throw new IllegalStateException("Không có đoạn quay nào trong " + lan);
        }
        return doan;
    }

    private static Map<String, Map<String, Object>> docKetQua(Path thuMuc) throws IOException {
        Map<String, Map<String, Object>> ketQua = new HashMap<>();
        if (!Files.isDirectory(thuMuc)) {
            System.out.println("[ghep] Không có thư mục báo cáo " + thuMuc + ", video sẽ ghi không tìm thấy kết quả");
            return ketQua;
        }
        try (Stream<Path> ds = Files.list(thuMuc)) {
            for (Path tep : ds.filter(p -> p.toString().endsWith(".xml")).toList()) {
                Matcher m = TEST_CASE.matcher(Files.readString(tep, StandardCharsets.UTF_8));
                while (m.find()) {
                    String than = m.group(4) == null ? "" : m.group(4);
                    Matcher loi = THONG_DIEP.matcher(than);
                    Map<String, Object> kq = new LinkedHashMap<>();
                    kq.put("dat", !than.contains("<failure") && !than.contains("<error"));
                    kq.put("giay", Double.parseDouble(m.group(2)));
                    kq.put("loi", loi.find() ? giaiMaXml(loi.group(1)) : null);
                    ketQua.put(giaiMaXml(m.group(1)), kq);
                }
            }
        }
        return ketQua;
    }

    private static String giaiMaXml(String s) {
        return s.replace("&quot;", "\"").replace("&apos;", "'").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&#10;", " ").replace("&amp;", "&");
    }

    // Đếm từ mã nguồn kịch bản để màn tổng kết không ghi số liệu gõ tay
    private static int[] demBoKiemThu() throws IOException {
        Path kichBan = Path.of("src", "test", "java", "vn", "vuonsen", "kiemthu", "kichban");
        int tc = 0;
        int luong = 0;
        try (Stream<Path> ds = Files.list(kichBan)) {
            for (Path tep : ds.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = Pattern.compile("@DisplayName\\(\"TC-").matcher(Files.readString(tep, StandardCharsets.UTF_8));
                int dem = 0;
                while (m.find()) {
                    dem++;
                }
                tc += dem;
                luong += dem > 0 ? 1 : 0;
            }
        }
        return new int[] {tc, luong};
    }

    @SuppressWarnings("unchecked")
    private static long uocTinhThoiLuong(List<Map<String, Object>> doan) {
        long tong = 6000 + 8000;
        for (Map<String, Object> d : doan) {
            List<Object[]> khung = (List<Object[]>) d.get("khung");
            tong += 2500 + 3000 + 1500;
            for (int i = 0; i + 1 < khung.size(); i++) {
                tong += Math.min((long) khung.get(i + 1)[1] - (long) khung.get(i)[1], TOI_DA_CHO_MS);
            }
        }
        return tong;
    }

    // ---------------- Máy chủ cục bộ ----------------

    private static HttpServer moMayChu(Path lan, String videoJson, String ten, CompletableFuture<Path> ketThuc)
            throws IOException {
        HttpServer may = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        // Luồng mặc định của máy chủ: xử lý tuần tự là đủ vì trang tải từng khung một, và stop() dọn luôn luồng
        may.setExecutor(null);
        may.createContext("/", trao -> {
            try {
                String duong = trao.getRequestURI().getPath();
                if ("POST".equals(trao.getRequestMethod()) && "/nhat-ky".equals(duong)) {
                    System.out.println("[ghep] " + new String(trao.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    traVe(trao, 200, "text/plain", new byte[0]);
                } else if ("POST".equals(trao.getRequestMethod()) && "/luu".equals(duong)) {
                    byte[] than = trao.getRequestBody().readAllBytes();
                    if (trao.getRequestHeaders().containsKey("X-Loi")) {
                        ketThuc.completeExceptionally(new IllegalStateException(new String(than, StandardCharsets.UTF_8)));
                    } else {
                        String loai = trao.getRequestHeaders().getFirst("X-Loai");
                        Path tep = lan.resolve(ten + (loai != null && loai.contains("mp4") ? ".mp4" : ".webm"));
                        Files.write(tep, than);
                        ketThuc.complete(tep);
                    }
                    traVe(trao, 200, "text/plain", new byte[0]);
                } else if ("/".equals(duong)) {
                    traVe(trao, 200, "text/html; charset=utf-8", TRANG.getBytes(StandardCharsets.UTF_8));
                } else if ("/video.json".equals(duong)) {
                    traVe(trao, 200, "application/json; charset=utf-8", videoJson.getBytes(StandardCharsets.UTF_8));
                } else {
                    Path tep = lan.resolve(duong.substring(1)).normalize();
                    if (!tep.startsWith(lan) || !Files.isRegularFile(tep)) {
                        traVe(trao, 404, "text/plain", new byte[0]);
                    } else {
                        traVe(trao, 200, tep.toString().endsWith(".jpg") ? "image/jpeg" : "application/octet-stream",
                                Files.readAllBytes(tep));
                    }
                }
            } catch (Exception loi) {
                ketThuc.completeExceptionally(loi);
                traVe(trao, 500, "text/plain", new byte[0]);
            }
        });
        may.start();
        return may;
    }

    private static void traVe(HttpExchange trao, int ma, String loai, byte[] than) throws IOException {
        trao.getResponseHeaders().set("Content-Type", loai);
        trao.sendResponseHeaders(ma, than.length == 0 ? -1 : than.length);
        if (than.length > 0) {
            trao.getResponseBody().write(than);
        }
        trao.close();
    }

    // ---------------- Trang dựng video ----------------

    private static final String TRANG = """
            <!doctype html>
            <html lang="vi"><head><meta charset="utf-8"><title>Ghép video</title></head>
            <body style="margin:0;background:#000"><canvas id="c" width="1280" height="720"></canvas>
            <script>
            const W = 1280, H = 720, THANH = 95;
            const g = document.getElementById('c').getContext('2d');
            const MAU = { nen: '#f7f3ea', xanh: '#1b3a2b', vang: '#c6a052', chu: '#2a2723', dat: '#2e7d32', truot: '#c62828' };
            const PHONG = 'Segoe UI, Arial, sans-serif';
            const nk = (m) => fetch('/nhat-ky', { method: 'POST', body: String(m) });
            const doi = (ms) => new Promise((r) => setTimeout(r, ms));
            const nap = async (src) => { const a = new Image(); a.src = src; await a.decode(); return a; };

            // Vẽ lại cảnh hiện tại đều đặn để luồng video có khung liên tục cả khi hình đứng yên
            let canh = () => {};
            setInterval(() => canh(), 100);

            function chuoi(font, mau) { g.font = font + ' ' + PHONG; g.fillStyle = mau; }
            function cat(chu, rong) {
              if (g.measureText(chu).width <= rong) return chu;
              while (chu.length > 1 && g.measureText(chu + '…').width > rong) chu = chu.slice(0, -1);
              return chu + '…';
            }
            function viet(chu, x, y, rong, cao) {
              let dong = '';
              for (const t of chu.split(' ')) {
                const thu = dong ? dong + ' ' + t : t;
                if (g.measureText(thu).width > rong && dong) { g.fillText(dong, x, y); y += cao; dong = t; } else { dong = thu; }
              }
              if (dong) { g.fillText(dong, x, y); y += cao; }
              return y;
            }
            function hop(x, y, w, h, r, mau) { g.fillStyle = mau; g.beginPath(); g.roundRect(x, y, w, h, r); g.fill(); }

            function manMoDau(d) {
              return () => {
                g.fillStyle = MAU.xanh; g.fillRect(0, 0, W, H);
                chuoi('600 19px', MAU.vang); g.fillText('TIỂU LUẬN TỐT NGHIỆP — PHẠM TRẦN TUẤN ANH — MSSV 21130004', 80, 90);
                chuoi('700 42px', '#ffffff'); let y = viet(d.tieuDe, 80, 150, W - 160, 52);
                chuoi('22px', '#e8e2d4'); y = viet(d.moTa, 80, y + 14, W - 160, 32);
                chuoi('600 21px', MAU.vang); g.fillText('Các test case trong video:', 80, y + 22); y += 58;
                chuoi('20px', '#ffffff');
                d.doan.forEach((dn, i) => { g.fillText(cat((i + 1) + '.  ' + dn.ma + '   ' + dn.ten, W - 200), 100, y); y += 30; });
                chuoi('17px', '#bfb8a8'); viet(d.ghiChu, 80, H - 70, W - 160, 25);
              };
            }
            function manDoan(dn, i, n) {
              return () => {
                g.fillStyle = MAU.nen; g.fillRect(0, 0, W, H);
                chuoi('600 22px', MAU.vang); g.fillText('TEST CASE ' + (i + 1) + '/' + n + '  —  ' + dn.luong.toUpperCase(), 80, 250);
                chuoi('700 58px', MAU.xanh); g.fillText(dn.ma, 80, 330);
                chuoi('32px', MAU.chu); viet(dn.ten, 80, 392, W - 160, 44);
              };
            }
            function nhan(kq) {
              const dat = kq && kq.dat;
              const chu = !kq ? 'KHÔNG TÌM THẤY KẾT QUẢ TRONG BÁO CÁO' : dat ? '✓  ĐẠT' : '✗  KHÔNG ĐẠT';
              chuoi('700 40px', '#fff'); const w = g.measureText(chu).width + 80; const x = (W - w) / 2;
              hop(x, 36, w, 84, 16, !kq ? '#555555' : dat ? MAU.dat : MAU.truot);
              chuoi('700 40px', '#fff'); g.fillText(chu, x + 40, 92);
              if (kq) {
                chuoi('18px', '#fff');
                let phu = 'Kết quả lấy từ báo cáo Surefire của lần chạy; cả kịch bản, tính từ lúc mở trình duyệt, hết ' + kq.giay.toFixed(1).replace('.', ',') + ' giây';
                if (!dat && kq.loi) phu = cat('Lỗi: ' + kq.loi, 1000);
                const w2 = g.measureText(phu).width + 32;
                hop((W - w2) / 2, 130, w2, 34, 8, 'rgba(0,0,0,0.75)');
                chuoi('18px', '#fff'); g.fillText(phu, (W - w2) / 2 + 16, 153);
              }
            }
            function manKhung(anh, dn, i, n, coNhan) {
              return () => {
                const cao = H - THANH;
                g.fillStyle = '#000'; g.fillRect(0, 0, W, cao);
                const tl = Math.min(W / anh.naturalWidth, cao / anh.naturalHeight);
                const w = anh.naturalWidth * tl, h = anh.naturalHeight * tl;
                g.drawImage(anh, (W - w) / 2, (cao - h) / 2, w, h);
                g.fillStyle = MAU.xanh; g.fillRect(0, cao, W, THANH);
                chuoi('700 24px', MAU.vang); g.fillText(dn.ma, 24, cao + 38);
                const rMa = g.measureText(dn.ma).width;
                chuoi('22px', '#ffffff'); g.fillText(cat(dn.ten, W - rMa - 80), 24 + rMa + 16, cao + 38);
                chuoi('17px', '#cfc8b8'); g.fillText(dn.luong + '   ·   Selenium WebDriver tự điều khiển Chrome, không có thao tác tay', 24, cao + 73);
                chuoi('600 17px', MAU.vang); const so = (i + 1) + '/' + n; g.fillText(so, W - 24 - g.measureText(so).width, cao + 73);
                if (coNhan) nhan(dn.ketQua);
              };
            }
            function manTongKet(d) {
              return () => {
                g.fillStyle = MAU.xanh; g.fillRect(0, 0, W, H);
                chuoi('700 40px', '#ffffff'); g.fillText('Kết quả lần chạy', 80, 120);
                let y = 190, dat = 0;
                d.doan.forEach((dn) => {
                  const kq = dn.ketQua; if (kq && kq.dat) dat++;
                  chuoi('700 26px', !kq ? '#9e9e9e' : kq.dat ? '#81c784' : '#ef9a9a'); g.fillText(!kq ? '?' : kq.dat ? '✓' : '✗', 80, y);
                  chuoi('600 22px', MAU.vang); g.fillText(dn.ma, 120, y);
                  chuoi('22px', '#ffffff'); g.fillText(cat(dn.ten, 800), 270, y);
                  if (kq) { chuoi('20px', '#cfc8b8'); const s = kq.giay.toFixed(1).replace('.', ',') + ' giây'; g.fillText(s, W - 80 - g.measureText(s).width, y); }
                  y += 44;
                });
                chuoi('600 24px', '#ffffff'); g.fillText('Đạt ' + dat + '/' + d.doan.length + ' test case trong video — số liệu lấy từ báo cáo Surefire của chính lần chạy này', 80, y + 30);
                chuoi('20px', '#e8e2d4');
                viet('Toàn bộ bộ kiểm thử có ' + d.tongTestCase + ' test case trên ' + d.tongLuong + ' luồng nghiệp vụ, tổ chức theo mô hình Page Object Model, chạy bằng JUnit 5 và Maven.', 80, y + 80, W - 160, 30);
              };
            }

            (async () => {
              try {
                const d = await (await fetch('/video.json')).json();
                const loai = ['video/mp4;codecs=avc1.640028', 'video/mp4;codecs=avc1.42E01F', 'video/mp4',
                  'video/webm;codecs=vp9', 'video/webm;codecs=vp8', 'video/webm'].find((t) => MediaRecorder.isTypeSupported(t));
                nk('Dinh dang video: ' + loai);
                const ghi = new MediaRecorder(document.getElementById('c').captureStream(30), { mimeType: loai, videoBitsPerSecond: 5000000 });
                const manh = [];
                ghi.ondataavailable = (e) => { if (e.data.size) manh.push(e.data); };
                const dung = new Promise((r) => { ghi.onstop = r; });

                canh = manMoDau(d); canh();
                ghi.start(1000);
                await doi(6000);
                const n = d.doan.length;
                for (let i = 0; i < n; i++) {
                  const dn = d.doan[i];
                  nk('Doan ' + (i + 1) + '/' + n + ' ' + dn.ma + ': ' + dn.khung.length + ' khung');
                  canh = manDoan(dn, i, n); canh(); await doi(2500);
                  let anh = null;
                  for (let k = 0; k < dn.khung.length; k++) {
                    anh = await nap(dn.khung[k][0]);
                    canh = manKhung(anh, dn, i, n, false); canh();
                    const tiep = k + 1 < dn.khung.length ? dn.khung[k + 1][1] : dn.khung[k][1] + 1500;
                    await doi(Math.max(0, Math.min(tiep - dn.khung[k][1], d.toiDaCho)));
                  }
                  if (anh) { canh = manKhung(anh, dn, i, n, true); canh(); await doi(3000); }
                }
                canh = manTongKet(d); canh(); await doi(8000);
                ghi.stop(); await dung;
                await fetch('/luu', { method: 'POST', headers: { 'X-Loai': loai }, body: new Blob(manh, { type: loai }) });
              } catch (e) {
                await fetch('/luu', { method: 'POST', headers: { 'X-Loi': '1' }, body: String(e && e.stack || e) });
              }
            })();
            </script></body></html>
            """;
}
