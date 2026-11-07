package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.config.props.GoiYCaNhanProperties;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian.CauHinh;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian.DoDo;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian.NguoiKhac;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Đánh giá ngoại tuyến gợi ý sảnh: trả lời câu "đã giải quyết được bài toán chưa" bằng số.
 *
 * Phương pháp giữ lại đơn cuối (leave-last-out), cách đánh giá chuẩn của hệ gợi ý (Herlocker
 * và cộng sự, 2004):
 *   - Mỗi khách có từ 2 đến 5 đơn. Giấu đơn cuối, dựng hồ sơ từ các đơn trước.
 *   - Hệ thống xếp hạng 6 sảnh, xem sảnh khách thật sự đặt ở đơn bị giấu đứng thứ mấy.
 *   - HR@1: tỉ lệ khách mà sảnh đó đứng đầu. HR@3: nằm trong 3 sảnh hiện trên trang chủ.
 *     MRR: trung bình của 1 / thứ hạng. Cả ba càng cao càng tốt.
 *
 * Dữ liệu sinh theo đúng bảng KIEU của bộ sinh dữ liệu lớn (cong-cu/du-lieu-mau), dùng 6 sảnh
 * và mức phù hợp thật lấy từ cơ sở dữ liệu. Hai kiểu dữ liệu (ba điều kiện, xem DIEU_KIEN):
 *   Có khẩu vị:     mỗi khách có một loại tiệc chính và một sảnh ưa thích; 80% số đơn theo
 *                   loại tiệc chính, trong đó 70% chọn đúng sảnh ưa thích.
 *   Không khẩu vị:  mỗi đơn chọn loại tiệc ngẫu nhiên, đúng như bộ sinh dữ liệu lớn hiện tại.
 * Điều kiện thứ hai là nhóm đối chứng: dữ liệu không chứa khẩu vị thì gợi ý cá nhân không có gì
 * để học, không được phép thắng gợi ý nổi bật. Thắng được ở đó nghĩa là phép đo có lỗi.
 *
 * Kết quả ghi ra target/danh-gia-goi-y.md để đưa vào tiểu luận mục 3.3.
 */
@SpringBootTest
class DanhGiaGoiYKhongGianTest {

    private static final long HAT_GIONG = 20261106L;

    /* Bảng KIEU của tao-du-lieu-lon.js: loại tiệc và các sảnh hay dùng cho loại đó. */
    private static final Map<EventType, List<String>> KIEU = new LinkedHashMap<>();

    static {
        KIEU.put(EventType.WEDDING, List.of("sanh-ven-song", "sanh-sen-vang", "vuon-cau"));
        KIEU.put(EventType.CORPORATE, List.of("phong-hoi-nghi", "sanh-sen-vang"));
        KIEU.put(EventType.BIRTHDAY, List.of("nha-ruong-go", "vuon-cau"));
        KIEU.put(EventType.FAMILY, List.of("nha-ruong-go", "cum-choi-sen"));
        KIEU.put(EventType.OTHER, List.of("cum-choi-sen", "vuon-cau"));
    }

    @Autowired
    private GoiYCaNhanService goiYService;
    @Autowired
    private GoiYCaNhanProperties props;

    private record Don(EventType loai, int soKhach, Long sanhId) {
    }

    private record ChiSo(double hr1, double hr3, double mrr) {
    }

    private record TrungBinh(ChiSo tb, ChiSo dolech) {
    }

    /*
     * Ba điều kiện dữ liệu:
     *   A. Có khẩu vị, 800 khách      — hệ thống đã chạy lâu, nhiều khách có lịch sử.
     *   B. Có khẩu vị, 40 khách       — website mới mở, ít láng giềng để so.
     *   C. Không khẩu vị, 800 khách   — nhóm đối chứng, giống bộ dữ liệu lớn hiện tại.
     * Điều kiện B ít khách nên kết quả dao động mạnh hơn, chạy nhiều lần hơn để trung bình ổn định.
     */
    private record DieuKien(String ten, double doTrungThanh, int soKhach, int soLanChay) {
    }

    private static final List<DieuKien> DIEU_KIEN = List.of(
            new DieuKien("A. Có khẩu vị, 800 khách", 0.8, 800, 5),
            new DieuKien("B. Có khẩu vị, 40 khách", 0.8, 40, 40),
            new DieuKien("C. Không khẩu vị, 800 khách (đối chứng)", 0.0, 800, 5));

    private static final String NOI_BAT = "Sảnh nổi bật (đặt nhiều nhất)";

    @Test
    @DisplayName("Đánh giá ngoại tuyến: có khẩu vị thì gợi ý cá nhân thắng nổi bật, không khẩu vị thì không")
    void danhGia() throws IOException {
        Map<Long, DacTrungKhongGian.Sanh> sanh = goiYService.duLieuChung().sanh();
        Map<String, Long> theoSlug = new HashMap<>();
        sanh.values().forEach(s -> theoSlug.put(s.slug(), s.id()));

        StringBuilder bao = new StringBuilder();
        bao.append("# Đánh giá ngoại tuyến gợi ý sảnh\n\n");
        bao.append("Mỗi khách 2–5 đơn, giữ lại đơn cuối để kiểm. Mỗi ô là trung bình ± độ lệch chuẩn qua nhiều lần chạy "
                + "với hạt giống khác nhau, bắt đầu từ %d.%n".formatted(HAT_GIONG));

        Map<String, Map<String, TrungBinh>> ketQua = new LinkedHashMap<>();
        long batDau = System.nanoTime();
        long soLuot = 0;
        for (DieuKien dk : DIEU_KIEN) {
            List<Map<String, ChiSo>> cacLan = new ArrayList<>();
            for (int lan = 0; lan < dk.soLanChay(); lan++) {
                cacLan.add(chay(sinhDuLieu(sanh, theoSlug, dk.doTrungThanh(), dk.soKhach(), HAT_GIONG + lan), sanh));
                soLuot += 8L * dk.soKhach();
            }
            Map<String, TrungBinh> tb = trungBinh(cacLan);
            ketQua.put(dk.ten(), tb);

            bao.append("%n## %s, %d lần chạy%n%n".formatted(dk.ten(), dk.soLanChay()));
            bao.append("| Cách gợi ý | HR@1 | HR@3 | MRR |%n|---|---|---|---|%n".formatted());
            tb.forEach((ten, v) -> bao.append(String.format(Locale.ROOT, "| %s | %.3f ± %.3f | %.3f ± %.3f | %.3f ± %.3f |%n",
                    ten, v.tb().hr1(), v.dolech().hr1(), v.tb().hr3(), v.dolech().hr3(), v.tb().mrr(), v.dolech().mrr())));
        }
        double giay = (System.nanoTime() - batDau) / 1e9;
        bao.append(String.format(Locale.ROOT, "%nThời gian: %d lượt xếp hạng bằng độ tương đồng cùng các mốc so sánh hết %.1f giây, "
                + "trung bình %.2f ms một lượt.%n", soLuot, giay, giay * 1000 / soLuot));

        Path ra = Path.of("target", "danh-gia-goi-y.md");
        Files.writeString(ra, bao.toString(), StandardCharsets.UTF_8);
        System.out.println(bao);

        String cauHinh = String.format(Locale.ROOT, "Kết hợp α = %.1f, cosine", props.alpha());

        Map<String, TrungBinh> a = ketQua.get(DIEU_KIEN.get(0).ten());
        assertThat(a.get(cauHinh).tb().mrr()).as("có khẩu vị: gợi ý cá nhân phải thắng gợi ý nổi bật")
                .isGreaterThan(a.get(NOI_BAT).tb().mrr() + 0.1);
        assertThat(a.get(cauHinh).tb().hr3()).isGreaterThan(a.get(NOI_BAT).tb().hr3() + 0.1);

        Map<String, TrungBinh> c = ketQua.get(DIEU_KIEN.get(2).ten());
        assertThat(c.get(cauHinh).tb().mrr()).as("không khẩu vị: không có gì để học, không được thắng rõ rệt")
                .isLessThan(c.get(NOI_BAT).tb().mrr() + 0.03);
    }

    /* Sinh lịch sử đặt tiệc cho soKhach khách. doTrungThanh = tỉ lệ đơn theo loại tiệc chính. */
    private List<List<Don>> sinhDuLieu(Map<Long, DacTrungKhongGian.Sanh> sanh, Map<String, Long> theoSlug,
                                       double doTrungThanh, int soKhach, long hatGiong) {
        Random r = new Random(hatGiong * 31 + (long) (doTrungThanh * 1000) + soKhach);
        List<EventType> cacLoai = new ArrayList<>(KIEU.keySet());
        List<List<Don>> kq = new ArrayList<>();
        for (int k = 0; k < soKhach; k++) {
            EventType loaiChinh = cacLoai.get(r.nextInt(cacLoai.size()));
            List<String> sanhCuaLoai = KIEU.get(loaiChinh);
            String sanhUaThich = sanhCuaLoai.get(r.nextInt(sanhCuaLoai.size()));

            int soDon = 2 + r.nextInt(4);
            List<Don> ds = new ArrayList<>();
            for (int j = 0; j < soDon; j++) {
                EventType loai;
                String slug;
                if (r.nextDouble() < doTrungThanh) {
                    loai = loaiChinh;
                    slug = r.nextDouble() < 0.7 ? sanhUaThich : sanhCuaLoai.get(r.nextInt(sanhCuaLoai.size()));
                } else {
                    loai = cacLoai.get(r.nextInt(cacLoai.size()));
                    List<String> ung = KIEU.get(loai);
                    slug = ung.get(r.nextInt(ung.size()));
                }
                DacTrungKhongGian.Sanh s = sanh.get(theoSlug.get(slug));
                // Số khách như bộ sinh dữ liệu lớn: từ sức chứa tối thiểu tới 2,5 lần mức đó
                int tran = Math.min(s.sucChuaMax(), (int) Math.round(s.sucChuaMin() * 2.5));
                int soNguoi = s.sucChuaMin() + r.nextInt(Math.max(1, tran - s.sucChuaMin() + 1));
                ds.add(new Don(loai, soNguoi, s.id()));
            }
            kq.add(ds);
        }
        return kq;
    }

    private Map<String, ChiSo> chay(List<List<Don>> duLieu, Map<Long, DacTrungKhongGian.Sanh> sanh) {
        double[] trongSo = props.trongSoNhomSanh();
        Map<Long, double[]> vectorSanh = new LinkedHashMap<>();
        sanh.forEach((id, s) -> vectorSanh.put(id, DacTrungKhongGian.vector(s, trongSo)));

        // Tập huấn luyện: mọi đơn trừ đơn cuối của mỗi khách
        List<double[]> hoSo = new ArrayList<>();
        List<Set<Long>> daDat = new ArrayList<>();
        List<Map<Long, Integer>> soLanDat = new ArrayList<>();
        Map<Long, Long> phoBien = new HashMap<>();
        for (List<Don> ds : duLieu) {
            DacTrungKhongGian.HoSo hs = new DacTrungKhongGian.HoSo();
            Set<Long> dat = new HashSet<>();
            Map<Long, Integer> lan = new HashMap<>();
            for (Don d : ds.subList(0, ds.size() - 1)) {
                hs.themTiec(d.loai(), d.soKhach(), sanh.get(d.sanhId()), 1.0);
                dat.add(d.sanhId());
                lan.merge(d.sanhId(), 1, Integer::sum);
                phoBien.merge(d.sanhId(), 1L, Long::sum);
            }
            hoSo.add(hs.vector(trongSo));
            daDat.add(dat);
            soLanDat.add(lan);
        }
        List<NguoiKhac> tatCa = new ArrayList<>();
        for (int i = 0; i < duLieu.size(); i++) {
            tatCa.add(new NguoiKhac((long) i, hoSo.get(i), daDat.get(i)));
        }

        Map<String, ChiSo> kq = new LinkedHashMap<>();
        kq.put("Ngẫu nhiên (tính theo lý thuyết)", ngauNhien(vectorSanh.size()));
        kq.put("Sảnh nổi bật (đặt nhiều nhất)", danhGia(duLieu, i -> theoPhoBien(vectorSanh.keySet(), phoBien)));
        kq.put("Đặt lại sảnh đã đặt nhiều nhất", danhGia(duLieu, i -> {
            Map<Long, Integer> lan = soLanDat.get(i);
            List<Long> ds = new ArrayList<>(theoPhoBien(vectorSanh.keySet(), phoBien));
            ds.sort(Comparator.comparingInt((Long s) -> -lan.getOrDefault(s, 0)));
            return ds;
        }));

        record PhuongAn(String ten, double alpha, DoDo doDo) {
        }
        List<PhuongAn> phuongAn = List.of(
                new PhuongAn("Chỉ lọc theo nội dung (α = 1)", 1.0, DoDo.COSINE),
                new PhuongAn("Chỉ lọc cộng tác, cosine (α = 0)", 0.0, DoDo.COSINE),
                new PhuongAn("Chỉ lọc cộng tác, Pearson (α = 0)", 0.0, DoDo.PEARSON),
                new PhuongAn("Chỉ lọc cộng tác, Jaccard (α = 0)", 0.0, DoDo.JACCARD),
                new PhuongAn("Kết hợp α = 0.2, cosine", 0.2, DoDo.COSINE),
                new PhuongAn("Kết hợp α = 0.4, cosine", 0.4, DoDo.COSINE),
                new PhuongAn("Kết hợp α = 0.6, cosine", 0.6, DoDo.COSINE),
                new PhuongAn("Kết hợp α = 0.8, cosine", 0.8, DoDo.COSINE));

        for (PhuongAn pa : phuongAn) {
            CauHinh ch = new CauHinh(pa.alpha(), props.soLangGieng(), pa.doDo());
            kq.put(pa.ten(), danhGia(duLieu, i -> {
                List<NguoiKhac> khac = new ArrayList<>(tatCa);
                khac.remove(i);
                return BoGoiYKhongGian.xepHang(hoSo.get(i), daDat.get(i), vectorSanh, khac, ch, phoBien)
                        .stream().map(BoGoiYKhongGian.KetQua::sanhId).toList();
            }));
        }
        return kq;
    }

    private interface XepHang {
        List<Long> cho(int khach);
    }

    private ChiSo danhGia(List<List<Don>> duLieu, XepHang xepHang) {
        double hr1 = 0;
        double hr3 = 0;
        double mrr = 0;
        for (int i = 0; i < duLieu.size(); i++) {
            List<Don> ds = duLieu.get(i);
            Long dapAn = ds.get(ds.size() - 1).sanhId();
            int hang = xepHang.cho(i).indexOf(dapAn) + 1;
            if (hang == 1) {
                hr1++;
            }
            if (hang >= 1 && hang <= 3) {
                hr3++;
            }
            if (hang >= 1) {
                mrr += 1.0 / hang;
            }
        }
        int n = duLieu.size();
        return new ChiSo(hr1 / n, hr3 / n, mrr / n);
    }

    private static Map<String, TrungBinh> trungBinh(List<Map<String, ChiSo>> cacLan) {
        Map<String, TrungBinh> kq = new LinkedHashMap<>();
        for (String ten : cacLan.get(0).keySet()) {
            double[][] gt = new double[3][cacLan.size()];
            for (int i = 0; i < cacLan.size(); i++) {
                ChiSo c = cacLan.get(i).get(ten);
                gt[0][i] = c.hr1();
                gt[1][i] = c.hr3();
                gt[2][i] = c.mrr();
            }
            kq.put(ten, new TrungBinh(new ChiSo(tb(gt[0]), tb(gt[1]), tb(gt[2])),
                    new ChiSo(dl(gt[0]), dl(gt[1]), dl(gt[2]))));
        }
        return kq;
    }

    private static double tb(double[] x) {
        double s = 0;
        for (double v : x) {
            s += v;
        }
        return s / x.length;
    }

    /* Độ lệch chuẩn mẫu, chia n − 1. */
    private static double dl(double[] x) {
        double m = tb(x);
        double s = 0;
        for (double v : x) {
            s += (v - m) * (v - m);
        }
        return x.length < 2 ? 0 : Math.sqrt(s / (x.length - 1));
    }

    /* Xếp ngẫu nhiên n sảnh: HR@1 = 1/n, HR@3 = 3/n, MRR = (1/n) · Σ 1/r. */
    private static ChiSo ngauNhien(int n) {
        double mrr = 0;
        for (int r = 1; r <= n; r++) {
            mrr += 1.0 / r;
        }
        return new ChiSo(1.0 / n, Math.min(3, n) / (double) n, mrr / n);
    }

    private static List<Long> theoPhoBien(Set<Long> sanh, Map<Long, Long> phoBien) {
        List<Long> ds = new ArrayList<>(sanh);
        ds.sort(Comparator.comparingLong((Long s) -> -phoBien.getOrDefault(s, 0L)).thenComparing(s -> s));
        return ds;
    }
}
