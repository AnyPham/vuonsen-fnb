package vn.vuonsen.fnb.modules.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.assistant.dto.LoiMoDauResponse;
import vn.vuonsen.fnb.modules.assistant.hanhvi.LuotXem;
import vn.vuonsen.fnb.modules.assistant.hanhvi.LuotXemRepository;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.payment.PaymentRepository;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Dựng màn mở đầu của hộp thoại trợ lý.
 *
 * Hai nhánh, chia theo việc khách đã từng trả tiền hay chưa:
 *
 *   Khách mới hoặc chưa đăng nhập — chưa có gì để cá nhân hóa, nên đưa thứ đông người
 *   chọn nhất: sảnh và gói tiệc có nhiều đơn đã thanh toán nhất, kèm vài món mới.
 *
 *   Khách quen — nhắc lại đúng sảnh họ từng đặt và mời đặt lại, vì đặt lại chỗ cũ là
 *   việc nhanh nhất họ có thể làm. Món mới xen vào để khách thấy có cái chưa thử.
 *
 * Mọi con số đọc tươi từ cơ sở dữ liệu mỗi lần gọi. Không gõ cứng giá hay tên gói ở bất
 * kỳ đâu trong lớp này: nhà hàng đổi giá một gói mà trợ lý còn đọc số cũ thì sai nặng hơn
 * là trợ lý không biết trả lời.
 */
@Service
@RequiredArgsConstructor
public class LoiMoDauService {

    private static final int SO_MUC_TOI_DA = 3;
    private static final int SO_MON_MOI = 3;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /*
     * Chỉ tính lượt xem trong 60 ngày gần đây.
     *
     * Mối quan tâm có hạn dùng. Khách xem một sảnh từ tám tháng trước, giờ trợ lý lôi ra
     * nhắc thì vừa sai vừa làm khách thấy bị theo dõi.
     */
    private static final int SO_NGAY_CON_NONG = 60;

    /*
     * Phải xem ít nhất hai lần mới tính là quan tâm.
     *
     * Một lần xem có thể chỉ là bấm nhầm hoặc lướt qua. Hai lần trở lên thì khách đã chủ
     * động quay lại, đó mới là tín hiệu.
     */
    private static final long SO_LAN_XEM_TOI_THIEU = 2;

    private final HoSoKhachService hoSoKhachService;
    private final PaymentRepository paymentRepository;
    private final SpaceRepository spaceRepository;
    private final PartyPackageRepository partyPackageRepository;
    private final DishRepository dishRepository;
    private final LuotXemRepository luotXemRepository;

    @Transactional(readOnly = true)
    public LoiMoDauResponse dung(Long userId) {
        HoSoKhach hoSo = hoSoKhachService.layHoSo(userId);
        List<LoiMoDauResponse.Muc> quanTam = dangQuanTam(userId);
        return hoSo.laKhachQuen() ? choKhachQuen(hoSo, quanTam) : choKhachMoi(hoSo, quanTam);
    }

    /* Món khách đã gọi, để trợ lý mời gọi lại thứ họ đã thích. */
    private List<LoiMoDauResponse.Muc> monDaGoi(HoSoKhach hoSo) {
        return hoSo.cacMonDaGoi().stream()
                .map(m -> new LoiMoDauResponse.Muc(
                        m.dishId(),
                        m.tenMon(),
                        m.soPhan() == 1 ? "Bạn đã gọi 1 phần" : "Bạn đã gọi %d phần".formatted(m.soPhan()),
                        "/dat-mon",
                        "Gọi lại"))
                .toList();
    }

    /*
     * Món cùng loại với thứ khách đã gọi nhưng chưa thử.
     *
     * Phép đo giống nhau ở đây là cùng danh mục: khách gọi lẩu thì mời lẩu khác. Đơn giản
     * nhưng đúng với nghiệp vụ nhà hàng, và quan trọng hơn là giải thích được vì sao món
     * đó hiện ra, thứ mà một mô hình gợi ý không nói được.
     *
     * Rỗng khi khách chưa gọi món nào, vì lúc đó không có căn cứ nào để suy khẩu vị.
     */
    private List<LoiMoDauResponse.Muc> monTuongTu(HoSoKhach hoSo) {
        if (hoSo.cacMonDaGoi().isEmpty()) {
            return List.of();
        }
        Set<Long> danhMuc = hoSo.cacMonDaGoi().stream()
                .map(HoSoKhach.MonDaGoi::danhMucId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> daGoi = hoSo.cacMonDaGoi().stream()
                .map(HoSoKhach.MonDaGoi::dishId)
                .collect(java.util.stream.Collectors.toSet());
        if (danhMuc.isEmpty()) {
            return List.of();
        }

        return dishRepository.monCungLoaiChuaThu(danhMuc, daGoi).stream()
                .limit(SO_MUC_TOI_DA)
                .map(m -> new LoiMoDauResponse.Muc(
                        m.getId(),
                        m.getName(),
                        "Cùng loại %s, bạn chưa thử".formatted(
                                m.getCategory() == null ? "món" : m.getCategory().getName().toLowerCase()),
                        "/thuc-don/" + (m.getSlug() == null ? m.getId().toString() : m.getSlug()),
                        "Xem món"))
                .toList();
    }

    /*
     * Phần dựng từ kênh thu thập ngầm: thứ khách xem đi xem lại mà chưa đặt bao giờ.
     *
     * Khác với ba danh sách kia ở chỗ nó nói được điều mà đơn hàng không nói: khách đang
     * phân vân cái gì. Đơn hàng chỉ ghi lại những quyết định đã xong.
     */
    private List<LoiMoDauResponse.Muc> dangQuanTam(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<Object[]> dong = luotXemRepository.khongGianQuanTamChuaDat(
                userId, LuotXem.LoaiDoiTuong.KHONG_GIAN,
                java.time.LocalDateTime.now().minusDays(SO_NGAY_CON_NONG));

        Map<Long, Long> soLanXem = new LinkedHashMap<>();
        for (Object[] d : dong) {
            long lan = ((Number) d[1]).longValue();
            if (lan >= SO_LAN_XEM_TOI_THIEU && soLanXem.size() < SO_MUC_TOI_DA) {
                soLanXem.put(((Number) d[0]).longValue(), lan);
            }
        }
        if (soLanXem.isEmpty()) {
            return List.of();
        }

        List<Space> cacSanh = spaceRepository.findAllById(soLanXem.keySet());
        return theoThuTuXepHang(cacSanh, Space::getId, soLanXem).stream()
                .map(s -> new LoiMoDauResponse.Muc(
                        s.getId(),
                        s.getName(),
                        "Bạn đã xem %d lần nhưng chưa đặt".formatted(soLanXem.get(s.getId())),
                        "/dat-tiec?khongGian=" + s.getId(),
                        "Đặt thử"))
                .toList();
    }

    // ---------- Khách đã từng trả tiền ----------

    private LoiMoDauResponse choKhachQuen(HoSoKhach hoSo, java.util.List<LoiMoDauResponse.Muc> quanTam) {
        String ten = hoSo.tenKhach() == null ? "bạn" : hoSo.tenKhach();
        String loiChao = "Chào mừng %s quay lại Vườn Sen. %s"
                .formatted(ten, nhacSoLan(hoSo.soDonDaThanhToan()));

        List<LoiMoDauResponse.Muc> sanh = new ArrayList<>();
        for (HoSoKhach.SanhDaDat s : hoSo.cacSanhDaDat()) {
            sanh.add(new LoiMoDauResponse.Muc(
                    s.spaceId(),
                    s.tenSanh(),
                    moTaLanDat(s),
                    "/dat-tiec?khongGian=" + s.spaceId(),
                    "Đặt lại"));
        }

        return new LoiMoDauResponse(loiChao, true, sanh, List.of(), monMoi(), quanTam, monDaGoi(hoSo), monTuongTu(hoSo), List.of(
                "Lần trước tôi đặt hết bao nhiêu?",
                "Có ưu đãi nào đang áp dụng không?",
                "Tôi muốn đổi sang sảnh khác"));
    }

    private String nhacSoLan(int soDon) {
        return soDon == 1
                ? "Bạn đã tổ chức một tiệc ở đây. Muốn đặt lại chỗ cũ không?"
                : "Bạn đã tổ chức %d tiệc ở đây. Muốn đặt lại chỗ cũ không?".formatted(soDon);
    }

    private String moTaLanDat(HoSoKhach.SanhDaDat s) {
        String soLan = s.soLan() == 1 ? "Đã đặt 1 lần" : "Đã đặt %d lần".formatted(s.soLan());
        return s.lanGanNhat() == null
                ? soLan
                : "%s, gần nhất %s".formatted(soLan, s.lanGanNhat().format(NGAY));
    }

    // ---------- Khách mới hoặc chưa đăng nhập ----------

    private LoiMoDauResponse choKhachMoi(HoSoKhach hoSo, java.util.List<LoiMoDauResponse.Muc> quanTam) {
        String loiChao = hoSo.daDangNhap()
                ? "Chào %s. Đây là những lựa chọn khách đặt nhiều nhất ở Vườn Sen."
                        .formatted(hoSo.tenKhach() == null ? "bạn" : hoSo.tenKhach())
                : "Chào bạn. Đây là những lựa chọn khách đặt nhiều nhất ở Vườn Sen.";

        return new LoiMoDauResponse(loiChao, false, sanhDatNhieuNhat(), goiDatNhieuNhat(), monMoi(), quanTam, monDaGoi(hoSo), monTuongTu(hoSo),
                List.of("Đặt tiệc như thế nào?", "Có gói tiệc nào?", "Giữ chỗ cần cọc bao nhiêu?"));
    }

    private List<LoiMoDauResponse.Muc> sanhDatNhieuNhat() {
        Map<Long, Long> xepHang = docXepHang(paymentRepository.xepHangKhongGianTheoDonDaThanhToan());
        List<Space> cacSanh = spaceRepository.findAllById(xepHang.keySet());
        return theoThuTuXepHang(cacSanh, Space::getId, xepHang).stream()
                .map(s -> new LoiMoDauResponse.Muc(
                        s.getId(),
                        s.getName(),
                        moTaSanh(s, xepHang.get(s.getId())),
                        "/khong-gian/" + (s.getSlug() == null ? s.getId().toString() : s.getSlug()),
                        "Xem sảnh"))
                .toList();
    }

    private String moTaSanh(Space s, Long soDon) {
        StringBuilder sb = new StringBuilder();
        if (s.getCapacityMin() != null && s.getCapacityMax() != null) {
            sb.append("Chứa %d đến %d khách".formatted(s.getCapacityMin(), s.getCapacityMax()));
        }
        if (soDon != null && soDon > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("đã có %d tiệc".formatted(soDon));
        }
        return sb.toString();
    }

    private List<LoiMoDauResponse.Muc> goiDatNhieuNhat() {
        Map<Long, Long> xepHang = docXepHang(paymentRepository.xepHangGoiTiecTheoDonDaThanhToan());
        List<PartyPackage> cacGoi = partyPackageRepository.findAllById(xepHang.keySet());
        return theoThuTuXepHang(cacGoi, PartyPackage::getId, xepHang).stream()
                .map(g -> new LoiMoDauResponse.Muc(
                        g.getId(),
                        g.getName(),
                        moTaGoi(g),
                        "/goi-tiec",
                        "Xem gói"))
                .toList();
    }

    private String moTaGoi(PartyPackage g) {
        StringBuilder sb = new StringBuilder(TienTe.dinhDang(g.getPricePerTable())).append(" một mâm");
        if (g.getDishCount() != null && g.getDishCount() > 0) {
            sb.append(", %d món".formatted(g.getDishCount()));
        }
        return sb.toString();
    }

    /*
     * Món mới nhất đang phục vụ.
     *
     * Lấy theo ngày tạo bản ghi chứ không theo thứ tự hiển thị, vì thứ tự hiển thị do nhà
     * hàng sắp cho đẹp trang thực đơn, không nói lên món nào mới.
     */
    private List<LoiMoDauResponse.Muc> monMoi() {
        List<Dish> cacMon = dishRepository.findTop3ByAvailableTrueOrderByCreatedAtDesc();
        return cacMon.stream()
                .limit(SO_MON_MOI)
                .map(m -> new LoiMoDauResponse.Muc(
                        m.getId(),
                        m.getName(),
                        TienTe.dinhDang(m.getPrice()),
                        "/thuc-don/" + (m.getSlug() == null ? m.getId().toString() : m.getSlug()),
                        "Xem món"))
                .toList();
    }

    // ---------- Dùng chung ----------

    /* Đổi kết quả [id, số đơn] của truy vấn gộp thành bản đồ, giữ nguyên thứ tự xếp hạng. */
    private Map<Long, Long> docXepHang(List<Object[]> dong) {
        Map<Long, Long> ketQua = new LinkedHashMap<>();
        for (Object[] d : dong) {
            if (d.length >= 2 && d[0] != null) {
                ketQua.put(((Number) d[0]).longValue(), ((Number) d[1]).longValue());
            }
            if (ketQua.size() >= SO_MUC_TOI_DA) {
                break;
            }
        }
        return ketQua;
    }

    /*
     * findAllById không hứa giữ thứ tự, nên phải xếp lại theo số đơn.
     *
     * Bỏ qua chi tiết này thì danh sách "đặt nhiều nhất" ra thứ tự ngẫu nhiên của cơ sở
     * dữ liệu, mà nhìn vẫn giống như đúng, rất khó phát hiện.
     */
    private <T> List<T> theoThuTuXepHang(List<T> cacMuc, java.util.function.Function<T, Long> layId,
                                         Map<Long, Long> xepHang) {
        List<T> sapXep = new ArrayList<>(cacMuc);
        sapXep.sort(Comparator.comparing(
                (T m) -> xepHang.getOrDefault(layId.apply(m), 0L)).reversed());
        return sapXep;
    }
}
