package vn.vuonsen.fnb.modules.recommendation.canhan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.config.props.GoiYCaNhanProperties;
import vn.vuonsen.fnb.modules.assistant.hanhvi.LuotXem;
import vn.vuonsen.fnb.modules.booking.BookingStatus;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.recommendation.SpaceEventType;
import vn.vuonsen.fnb.modules.recommendation.SpaceEventTypeRepository;
import vn.vuonsen.fnb.modules.recommendation.canhan.GoiYCaNhanResponse.CanCu;
import vn.vuonsen.fnb.modules.recommendation.canhan.GoiYCaNhanResponse.LyDo;
import vn.vuonsen.fnb.modules.recommendation.canhan.GoiYCaNhanResponse.MonGoiY;
import vn.vuonsen.fnb.modules.recommendation.canhan.GoiYCaNhanResponse.Phan;
import vn.vuonsen.fnb.modules.recommendation.canhan.GoiYCaNhanResponse.SanhGoiY;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Gợi ý thông minh trên trang chủ: sảnh và món hợp với hồ sơ của khách.
 *
 * Hai nhánh, theo đúng yêu cầu của cô hướng dẫn:
 *
 *   Khách chưa có thông tin (chưa đăng nhập, hoặc đã đăng nhập nhưng chưa đặt gì, chưa xem
 *   sảnh nào nhiều lần, chưa khai sở thích): hiện những sảnh và món được đặt nhiều nhất.
 *   Đây là cách xử lý "khởi đầu lạnh" phổ biến nhất (Schein và cộng sự, 2002): chưa biết gì
 *   về người này thì đưa thứ đông người chọn nhất.
 *
 *   Khách đã có thông tin: dựng hồ sơ từ lịch sử đặt, lượt xem và sở thích tự khai, rồi xếp
 *   hạng sảnh bằng BoGoiYKhongGian và món bằng BoGoiYMon.
 *
 * Bản đồ để trình bày, theo câu hỏi của hội đồng:
 *   Hồ sơ người dùng dựng ở đâu        hoSoSanh() và hoSoMon() bên dưới, DacTrungKhongGian.HoSo
 *   Độ tương đồng tính ở đâu           DoTuongDong
 *   So khớp với lịch sử ở đâu          BoGoiYKhongGian.xepHang, BoGoiYMon.xepHang
 *   Khách mới xử lý ở đâu              sanhNoiBat() và monNoiBat()
 *
 * Dữ liệu của mọi khách (để tìm láng giềng) được giữ trong bộ nhớ giayLamMoi giây, vì trang
 * chủ là trang đông người vào nhất, đọc lại toàn bộ lịch sử mỗi lượt là phí. Riêng hồ sơ của
 * chính khách đang xem luôn đọc mới, nên khách vừa đặt xong quay lại trang chủ là thấy ngay.
 */
@Service
@RequiredArgsConstructor
public class GoiYCaNhanService {

    private final GoiYCaNhanRepository goiYRepository;
    private final SoThichKhachRepository soThichRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceEventTypeRepository spaceEventTypeRepository;
    private final GoiYCaNhanProperties props;

    private volatile DuLieuChung boNho;

    /* Dữ liệu dùng chung cho mọi lượt gợi ý. */
    public record DuLieuChung(
            Map<Long, DacTrungKhongGian.Sanh> sanh,
            Map<Long, double[]> vectorSanh,
            List<BoGoiYKhongGian.NguoiKhac> nguoiKhac,
            Map<Long, Long> phoBienSanh,
            Map<Long, DacTrungMon.Mon> mon,
            Map<Long, double[]> vectorMon,
            Map<Long, Set<Long>> khachTheoMon,
            Map<Long, Long> phoBienMon,
            long taoLuc
    ) {
    }

    /* Hồ sơ sảnh của một khách kèm những gì đã dùng để dựng nó. */
    public record HoSoSanh(DacTrungKhongGian.HoSo hoSo, Map<Long, Integer> daDat,
                           Map<Long, Long> luotXem, int soTiec, boolean coSoThich) {
    }

    public record HoSoMon(DacTrungMon.HoSo hoSo, Map<Long, Long> daGoi, boolean coSoThich) {
    }

    @Transactional(readOnly = true)
    public GoiYCaNhanResponse goiY(Long userId) {
        DuLieuChung chung = duLieuChung();
        SoThichGoiY soThich = userId == null ? null
                : soThichRepository.findByUserId(userId).map(SoThichKhach::choGoiY).orElse(null);

        HoSoSanh hsSanh = userId == null ? null : hoSoSanh(userId, soThich, chung);
        Phan<SanhGoiY> phanSanh = hsSanh == null || hsSanh.hoSo().rong()
                ? sanhNoiBat(chung)
                : sanhCaNhan(userId, hsSanh, chung);

        HoSoMon hsMon = userId == null ? null : hoSoMon(userId, soThich, chung);
        Phan<MonGoiY> phanMon = hsMon == null || hsMon.hoSo().rong()
                ? monNoiBat(chung)
                : monCaNhan(hsMon, chung);

        return new GoiYCaNhanResponse(phanSanh, phanMon);
    }

    /* Thứ hạng đầy đủ kèm điểm từng thành phần, cho bài kiểm thử và phần minh họa trong tiểu luận. */
    @Transactional(readOnly = true)
    public List<BoGoiYKhongGian.KetQua> xepHangSanh(Long userId) {
        DuLieuChung chung = duLieuChung();
        SoThichGoiY soThich = soThichRepository.findByUserId(userId).map(SoThichKhach::choGoiY).orElse(null);
        HoSoSanh hs = hoSoSanh(userId, soThich, chung);
        if (hs.hoSo().rong()) {
            return List.of();
        }
        return BoGoiYKhongGian.xepHang(hs.hoSo().vector(props.trongSoNhomSanh()), hs.daDat().keySet(),
                chung.vectorSanh(), nguoiKhacTru(chung, userId), props.cauHinhSanh(), chung.phoBienSanh());
    }

    // ---------------------------------------------------------------- hồ sơ của khách đang xem

    /*
     * Hồ sơ sảnh: cộng dồn tiệc đã đặt, sảnh đã xem nhiều lần và sở thích tự khai.
     * Đơn đang chờ xác nhận chỉ tính một nửa vì khách có thể chưa chốt.
     */
    public HoSoSanh hoSoSanh(Long userId, SoThichGoiY soThich, DuLieuChung chung) {
        DacTrungKhongGian.HoSo hoSo = new DacTrungKhongGian.HoSo();
        Map<Long, Integer> daDat = new HashMap<>();
        int soTiec = 0;
        for (Object[] d : goiYRepository.lichSuDatTiecCua(userId)) {
            Long sanhId = (Long) d[1];
            hoSo.themTiec((EventType) d[2], (Integer) d[3], chung.sanh().get(sanhId), trongSoDon((BookingStatus) d[4]));
            daDat.merge(sanhId, 1, Integer::sum);
            soTiec++;
        }

        Map<Long, Long> luotXem = new HashMap<>();
        LocalDateTime tuLuc = LocalDateTime.now().minusDays(props.soNgayLuotXem());
        for (Object[] d : goiYRepository.luotXemCua(userId, LuotXem.LoaiDoiTuong.KHONG_GIAN, tuLuc)) {
            Long sanhId = ((Number) d[0]).longValue();
            long lan = ((Number) d[1]).longValue();
            if (lan >= props.soLanXemToiThieu() && chung.sanh().containsKey(sanhId)) {
                hoSo.themLuotXem(chung.sanh().get(sanhId), props.trongSoLuotXem());
                luotXem.put(sanhId, lan);
            }
        }

        hoSo.themSoThich(soThich, props.trongSoSoThich());
        boolean coSoThich = soThich != null && !soThich.trong();
        return new HoSoSanh(hoSo, daDat, luotXem, soTiec, coSoThich);
    }

    /* Hồ sơ khẩu vị: các món đã gọi (trọng số là số đơn có món đó) và sở thích tự khai. */
    public HoSoMon hoSoMon(Long userId, SoThichGoiY soThich, DuLieuChung chung) {
        Map<Long, Long> daGoi = new HashMap<>();
        for (List<Object[]> nguon : List.of(goiYRepository.monDaGoiTrongDonMonCua(userId),
                goiYRepository.monDaGoiTrongDonTiecCua(userId))) {
            for (Object[] d : nguon) {
                daGoi.merge((Long) d[1], ((Number) d[2]).longValue(), Long::sum);
            }
        }

        DacTrungMon.HoSo hoSo = new DacTrungMon.HoSo();
        daGoi.forEach((monId, soDon) -> hoSo.themMon(chung.mon().get(monId), soDon));
        hoSo.themSoThich(soThich, props.trongSoSoThich());
        boolean coSoThich = soThich != null && (soThich.anChay() || soThich.tamGia() != null);
        return new HoSoMon(hoSo, daGoi, coSoThich);
    }

    private double trongSoDon(BookingStatus trangThai) {
        return trangThai == BookingStatus.PENDING ? props.trongSoDonCho() : 1.0;
    }

    // ---------------------------------------------------------------- khách đã có thông tin

    private Phan<SanhGoiY> sanhCaNhan(Long userId, HoSoSanh hs, DuLieuChung chung) {
        double[] vector = hs.hoSo().vector(props.trongSoNhomSanh());
        List<BoGoiYKhongGian.KetQua> xepHang = BoGoiYKhongGian.xepHang(vector, hs.daDat().keySet(),
                chung.vectorSanh(), nguoiKhacTru(chung, userId), props.cauHinhSanh(), chung.phoBienSanh());

        List<SanhGoiY> items = new ArrayList<>();
        for (BoGoiYKhongGian.KetQua kq : xepHang.subList(0, Math.min(props.soSanh(), xepHang.size()))) {
            DacTrungKhongGian.Sanh s = chung.sanh().get(kq.sanhId());
            items.add(sanhGoiY(s, hs.daDat().getOrDefault(s.id(), 0),
                    lyDoSanh(s, kq, hs, vector, chung)));
        }
        CanCu canCu = new CanCu(hs.soTiec(), hs.luotXem().size(), 0, hs.coSoThich());
        return new Phan<>(GoiYCaNhanResponse.CA_NHAN, canCu, items);
    }

    /*
     * Lý do cho một sảnh, lấy từ chính phép tính chứ không viết sẵn.
     *
     * Nhóm thuộc tính nào đóng góp nhiều vào điểm nội dung thì nêu trước, và chỉ nêu khi điều
     * đó đúng với sảnh: khách hay tổ chức tiệc cưới và sảnh chấm từ 4 điểm cho tiệc cưới thì
     * mới nói "hợp tiệc cưới". Thêm lý do từ lọc cộng tác (bao nhiêu khách giống bạn đã đặt)
     * và từ lượt xem.
     */
    private List<LyDo> lyDoSanh(DacTrungKhongGian.Sanh s, BoGoiYKhongGian.KetQua kq, HoSoSanh hs,
                                double[] vector, DuLieuChung chung) {
        DacTrungKhongGian.HoSo hoSo = hs.hoSo();
        double[] vSanh = chung.vectorSanh().get(s.id());
        double[] trongSo = props.trongSoNhomSanh();
        List<KhungVector.Nhom> cacNhom = DacTrungKhongGian.KHUNG.cacNhom();

        List<Integer> thuTu = new ArrayList<>(List.of(0, 1, 2, 3));
        thuTu.sort(Comparator.comparingDouble((Integer i) ->
                trongSo[i] * DacTrungKhongGian.KHUNG.cosineNhom(vector, vSanh, cacNhom.get(i))).reversed());

        // Lý do trực tiếp về chính sảnh này (đã xem) đứng trước, lý do cộng tác đứng sau,
        // phần nội dung lấp chỗ còn lại, tổng không quá ba
        Long xem = hs.luotXem().get(s.id());
        boolean coCongTac = kq.soLangGiengDaDat() > 0;
        int choNoiDung = Math.max(1, 3 - (xem == null ? 0 : 1) - (coCongTac ? 1 : 0));

        List<LyDo> ds = new ArrayList<>();
        if (xem != null) {
            ds.add(LyDo.of("DA_XEM", xem));
        }
        int soNoiDung = 0;
        for (int i : thuTu) {
            if (soNoiDung >= Math.min(2, choNoiDung)) {
                break;
            }
            int truoc = ds.size();
            KhungVector.Nhom n = cacNhom.get(i);
            if (n == DacTrungKhongGian.LOAI_TIEC) {
                EventType e = hoSo.loaiTiecChinh();
                if (e != null && s.mucPhuHop(e) >= 4) {
                    ds.add(LyDo.of("LOAI_TIEC", e.name()));
                }
            } else if (n == DacTrungKhongGian.SO_KHACH) {
                int d = hoSo.daiSoKhachChinh();
                if (d >= 0 && s.chuaDuocDai(d)) {
                    ds.add(LyDo.of("SO_KHACH", DacTrungKhongGian.NHAN_DAI_SO_KHACH[d]));
                }
            } else if (n == DacTrungKhongGian.KIEU_KHONG_GIAN) {
                if (hoSo.kieuChinh() != null && hoSo.kieuChinh() == s.kieu()) {
                    ds.add(LyDo.of("KIEU_KHONG_GIAN", s.kieu().name()));
                }
            } else if (hoSo.tamGiaChinh() != null && hoSo.tamGiaChinh() == s.tamGia()) {
                ds.add(LyDo.of("TAM_GIA", s.tamGia().name()));
            }
            soNoiDung += ds.size() - truoc;
        }
        if (coCongTac) {
            ds.add(LyDo.of("KHACH_GIONG", kq.soLangGiengDaDat()));
        }
        return ds.size() > 3 ? ds.subList(0, 3) : ds;
    }

    private Phan<MonGoiY> monCaNhan(HoSoMon hs, DuLieuChung chung) {
        double[] vector = hs.hoSo().vector(props.trongSoNhomMon());
        List<BoGoiYMon.KetQua> xepHang = BoGoiYMon.xepHang(vector, hs.daGoi().keySet(), chung.vectorMon(),
                chung.khachTheoMon(), props.alphaMon(), chung.phoBienMon());

        List<MonGoiY> items = new ArrayList<>();
        for (BoGoiYMon.KetQua kq : xepHang.subList(0, Math.min(props.soMon(), xepHang.size()))) {
            DacTrungMon.Mon m = chung.mon().get(kq.monId());
            items.add(monGoiY(m, lyDoMon(m, kq, hs.hoSo(), chung)));
        }
        CanCu canCu = new CanCu(0, 0, hs.daGoi().size(), hs.coSoThich());
        return new Phan<>(GoiYCaNhanResponse.CA_NHAN, canCu, items);
    }

    private List<LyDo> lyDoMon(DacTrungMon.Mon m, BoGoiYMon.KetQua kq, DacTrungMon.HoSo hoSo, DuLieuChung chung) {
        List<LyDo> ds = new ArrayList<>();
        DacTrungMon.Mon giong = kq.monGiongNhat() == null ? null : chung.mon().get(kq.monGiongNhat());
        if (giong != null) {
            ds.add(new LyDo("HAY_GOI_CUNG", giong.ten(), giong.tenEn() == null ? giong.ten() : giong.tenEn()));
        }
        if (m.maDanhMuc() != null && m.maDanhMuc().equals(hoSo.danhMucChinh())) {
            ds.add(new LyDo("CUNG_LOAI", m.tenDanhMuc(), m.tenDanhMucEn() == null ? m.tenDanhMuc() : m.tenDanhMucEn()));
        }
        if (m.chay() && hoSo.thichChay()) {
            ds.add(LyDo.of("MON_CHAY", null));
        }
        if (m.tamGia() != null && m.tamGia() == hoSo.tamGiaChinh() && ds.size() < 3) {
            ds.add(LyDo.of("TAM_GIA", m.tamGia().name()));
        }
        return ds.size() > 3 ? ds.subList(0, 3) : ds;
    }

    // ---------------------------------------------------------------- khách chưa có thông tin

    /* Sảnh được đặt nhiều nhất; bằng nhau thì theo thứ tự nhà hàng sắp. */
    private Phan<SanhGoiY> sanhNoiBat(DuLieuChung chung) {
        List<SanhGoiY> items = chung.sanh().values().stream()
                .sorted(Comparator.comparingLong((DacTrungKhongGian.Sanh s) ->
                                -chung.phoBienSanh().getOrDefault(s.id(), 0L))
                        .thenComparingInt(DacTrungKhongGian.Sanh::thuTu))
                .limit(props.soSanh())
                .map(s -> {
                    long n = chung.phoBienSanh().getOrDefault(s.id(), 0L);
                    return sanhGoiY(s, 0, n > 0 ? List.of(LyDo.of("SO_TIEC", n)) : List.of());
                })
                .toList();
        return new Phan<>(GoiYCaNhanResponse.NOI_BAT, CanCu.TRONG, items);
    }

    /* Món được gọi nhiều nhất; chưa có đơn nào thì theo món nhà hàng đánh dấu bán chạy. */
    private Phan<MonGoiY> monNoiBat(DuLieuChung chung) {
        List<MonGoiY> items = chung.mon().values().stream()
                .sorted(Comparator.comparingLong((DacTrungMon.Mon m) ->
                                -chung.phoBienMon().getOrDefault(m.id(), 0L))
                        .thenComparing(m -> !m.banChay())
                        .thenComparingInt(DacTrungMon.Mon::thuTu))
                .limit(props.soMon())
                .map(m -> {
                    long n = chung.phoBienMon().getOrDefault(m.id(), 0L);
                    return monGoiY(m, n > 0 ? List.of(LyDo.of("SO_LAN_GOI", n)) : List.of());
                })
                .toList();
        return new Phan<>(GoiYCaNhanResponse.NOI_BAT, CanCu.TRONG, items);
    }

    // ---------------------------------------------------------------- dữ liệu dùng chung

    public DuLieuChung duLieuChung() {
        DuLieuChung hienTai = boNho;
        long bayGio = System.currentTimeMillis();
        if (hienTai != null && bayGio - hienTai.taoLuc() < props.giayLamMoi() * 1000L) {
            return hienTai;
        }
        synchronized (this) {
            if (boNho == null || bayGio - boNho.taoLuc() >= props.giayLamMoi() * 1000L) {
                boNho = docDuLieuChung();
            }
            return boNho;
        }
    }

    /* Bỏ bộ nhớ đệm, lượt sau đọc lại từ cơ sở dữ liệu. Dùng trong bài kiểm thử. */
    public void lamMoi() {
        boNho = null;
    }

    private DuLieuChung docDuLieuChung() {
        Map<Long, DacTrungKhongGian.Sanh> sanh = docSanh();
        Map<Long, double[]> vectorSanh = new LinkedHashMap<>();
        sanh.forEach((id, s) -> vectorSanh.put(id, DacTrungKhongGian.vector(s, props.trongSoNhomSanh())));

        Map<Long, SoThichGoiY> soThich = new HashMap<>();
        for (SoThichKhach st : soThichRepository.findAll()) {
            soThich.put(st.getUserId(), st.choGoiY());
        }

        // Hồ sơ của mọi khách đã từng đặt, dựng bằng đúng cách dựng hồ sơ khách đang xem
        // (không tính lượt xem, vì lượt xem chỉ dùng cho chính người đang xem)
        Map<Long, DacTrungKhongGian.HoSo> hoSo = new HashMap<>();
        Map<Long, Set<Long>> daDat = new HashMap<>();
        for (Object[] d : goiYRepository.lichSuDatTiec()) {
            Long userId = (Long) d[0];
            Long sanhId = (Long) d[1];
            hoSo.computeIfAbsent(userId, k -> new DacTrungKhongGian.HoSo())
                    .themTiec((EventType) d[2], (Integer) d[3], sanh.get(sanhId), trongSoDon((BookingStatus) d[4]));
            daDat.computeIfAbsent(userId, k -> new HashSet<>()).add(sanhId);
        }
        List<BoGoiYKhongGian.NguoiKhac> nguoiKhac = new ArrayList<>();
        hoSo.forEach((userId, hs) -> {
            hs.themSoThich(soThich.get(userId), props.trongSoSoThich());
            nguoiKhac.add(new BoGoiYKhongGian.NguoiKhac(userId, hs.vector(props.trongSoNhomSanh()),
                    daDat.get(userId)));
        });

        Map<Long, Long> phoBienSanh = demTheoKhoang(goiYRepository::soDonTheoSanh);

        Map<Long, DacTrungMon.Mon> mon = docMon();
        Map<Long, double[]> vectorMon = new LinkedHashMap<>();
        mon.forEach((id, m) -> vectorMon.put(id, DacTrungMon.vector(m, props.trongSoNhomMon())));

        Map<Long, Set<Long>> khachTheoMon = new HashMap<>();
        for (List<Object[]> nguon : List.of(goiYRepository.monDaGoiTrongDonMon(),
                goiYRepository.monDaGoiTrongDonTiec())) {
            for (Object[] d : nguon) {
                khachTheoMon.computeIfAbsent((Long) d[1], k -> new HashSet<>()).add((Long) d[0]);
            }
        }

        Map<Long, Long> phoBienMon = demTheoKhoang(tu -> {
            List<Object[]> gop = new ArrayList<>(goiYRepository.soDonTheoMonTrongDonMon(tu));
            gop.addAll(goiYRepository.soDonTheoMonTrongDonTiec(tu));
            return gop;
        });

        return new DuLieuChung(sanh, vectorSanh, nguoiKhac, phoBienSanh, mon, vectorMon, khachTheoMon,
                phoBienMon, System.currentTimeMillis());
    }

    /*
     * Đếm số đơn trong soNgayNoiBat ngày gần đây. Trong khoảng đó chưa có đơn nào (ví dụ cơ sở
     * dữ liệu mới dựng) thì đếm toàn bộ, để khách mới vẫn thấy được thứ tự có ý nghĩa.
     */
    private Map<Long, Long> demTheoKhoang(java.util.function.Function<LocalDateTime, List<Object[]>> truyVan) {
        Map<Long, Long> dem = cong(truyVan.apply(LocalDateTime.now().minusDays(props.soNgayNoiBat())));
        return dem.isEmpty() ? cong(truyVan.apply(LocalDateTime.of(2000, 1, 1, 0, 0))) : dem;
    }

    private static Map<Long, Long> cong(List<Object[]> dong) {
        Map<Long, Long> kq = new HashMap<>();
        for (Object[] d : dong) {
            kq.merge(((Number) d[0]).longValue(), ((Number) d[1]).longValue(), Long::sum);
        }
        return kq;
    }

    private Map<Long, DacTrungKhongGian.Sanh> docSanh() {
        List<Space> dsSanh = spaceRepository.findByActiveTrueOrderBySortOrderAsc();

        Map<Long, Map<EventType, Integer>> mucPhuHop = new HashMap<>();
        for (SpaceEventType set : spaceEventTypeRepository.findAll()) {
            mucPhuHop.computeIfAbsent(set.getSpace().getId(), k -> new EnumMap<>(EventType.class))
                    .put(set.getEventType(), set.getSuitability());
        }

        Map<Long, BigDecimal> gia = new HashMap<>();
        dsSanh.forEach(s -> gia.put(s.getId(), s.getRentalFee()));
        Map<Long, SoThichGoiY.TamGia> bacGia = DacTrungKhongGian.chiaBacGia(gia);

        Map<Long, DacTrungKhongGian.Sanh> kq = new LinkedHashMap<>();
        for (Space s : dsSanh) {
            kq.put(s.getId(), new DacTrungKhongGian.Sanh(
                    s.getId(), s.getSlug(), s.getName(), s.getNameEn(), s.getThumbnailUrl(),
                    s.getCapacityMin() == null ? 1 : s.getCapacityMin(),
                    s.getCapacityMax() == null ? Integer.MAX_VALUE : s.getCapacityMax(),
                    s.getRentalFee(), s.getFeeUnit(),
                    s.getSortOrder() == null ? 0 : s.getSortOrder(),
                    mucPhuHop.getOrDefault(s.getId(), Map.of()),
                    SoThichGoiY.KieuKhongGian.cua(s.getSpaceType()),
                    bacGia.get(s.getId())));
        }
        return kq;
    }

    private Map<Long, DacTrungMon.Mon> docMon() {
        List<Dish> dsMon = goiYRepository.monDangBan();
        dsMon.sort(Comparator.comparing((Dish d) -> d.getSortOrder() == null ? 0 : d.getSortOrder())
                .thenComparing(Dish::getId));

        Map<Long, BigDecimal> gia = new HashMap<>();
        dsMon.forEach(d -> gia.put(d.getId(), d.getPrice()));
        Map<Long, SoThichGoiY.TamGia> bacGia = DacTrungMon.chiaBacGia(gia);

        Map<Long, DacTrungMon.Mon> kq = new LinkedHashMap<>();
        for (Dish d : dsMon) {
            kq.put(d.getId(), new DacTrungMon.Mon(
                    d.getId(), d.getSlug(), d.getName(), d.getNameEn(), d.getImageUrl(),
                    d.getPrice(), d.getPriceNote(), d.getPriceNoteEn(),
                    d.isVegetarian(), d.isBestSeller(),
                    d.getSortOrder() == null ? 0 : d.getSortOrder(),
                    d.getCategory().getCode(), d.getCategory().getName(), d.getCategory().getNameEn(),
                    bacGia.get(d.getId())));
        }
        return kq;
    }

    private static List<BoGoiYKhongGian.NguoiKhac> nguoiKhacTru(DuLieuChung chung, Long userId) {
        return chung.nguoiKhac().stream().filter(v -> !v.userId().equals(userId)).toList();
    }

    private static SanhGoiY sanhGoiY(DacTrungKhongGian.Sanh s, int daDat, List<LyDo> lyDo) {
        return new SanhGoiY(s.id(), s.slug(), s.ten(), s.tenEn(), s.anh(), s.sucChuaMin(),
                s.sucChuaMax() == Integer.MAX_VALUE ? null : s.sucChuaMax(), s.giaThue(), s.donViGia(),
                daDat, lyDo);
    }

    private static MonGoiY monGoiY(DacTrungMon.Mon m, List<LyDo> lyDo) {
        return new MonGoiY(m.id(), m.slug(), m.ten(), m.tenEn(), m.anh(), m.gia(), m.ghiChuGia(),
                m.ghiChuGiaEn(), m.chay(), lyDo);
    }
}
