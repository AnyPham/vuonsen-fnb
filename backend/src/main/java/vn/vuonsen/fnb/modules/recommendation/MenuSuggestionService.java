package vn.vuonsen.fnb.modules.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Gợi ý thực đơn cho một gói tiệc.
 *
 * Gói tiệc chỉ ghi số món, ví dụ Gói Sen Vàng 8 món, chứ không ghi cụ thể là
 * món nào. Trước đây khách phải tự chọn, lớp này chọn giúp một thực đơn mẫu.
 *
 * Điểm mỗi món gồm ba phần, tổng 100:
 *   1. Mức phù hợp với loại sự kiện (bảng dish_event_types)   tối đa 50
 *   2. Món được gọi nhiều nhất                                 tối đa 30
 *   3. Giá món sát với tiền ăn trung bình của gói              tối đa 20
 *
 * Cách chọn món gồm ba bước:
 *   1. Mỗi danh mục bắt buộc lấy món điểm cao nhất, để thực đơn nào cũng có
 *      đủ khai vị, món chính và tráng miệng.
 *   2. Số món còn lại lấy theo điểm từ cao xuống, nhưng mỗi danh mục không
 *      quá một phần ba số món, tránh dồn hết vào một nhóm.
 *   3. Nếu trần làm chưa đủ số món thì nới trần cho đủ.
 *
 * Nhờ bước 2 lấy theo điểm chứ không chia đều mỗi danh mục một món, danh mục
 * ít hợp với loại tiệc sẽ bị đẩy xuống. Ví dụ lẩu chấm 2 điểm cho tiệc cưới
 * nên khó lọt vào thực đơn cưới, còn chấm 5 điểm cho họp mặt gia đình thì
 * gần như luôn có mặt.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuSuggestionService {

    private static final int DIEM_SU_KIEN = 50;
    private static final int DIEM_BAN_CHAY = 30;
    private static final int DIEM_GIA = 20;

    // Tiền món chiếm khoảng 60% giá một mâm, phần còn lại là phục vụ,
    // trang trí và dụng cụ. Dùng để ước lượng tầm giá món hợp với từng gói.
    private static final BigDecimal TY_LE_TIEN_MON = new BigDecimal("0.6");

    // Một mâm tiệc Việt bao giờ cũng phải có khai vị, món chính và tráng miệng.
    // Lẩu nướng và đồ uống là phần thêm, có hay không tùy loại tiệc.
    private static final List<String> DANH_MUC_BAT_BUOC = List.of("khaivi", "chinh", "trangmieng");

    private final DishEventTypeRepository dishEventTypeRepository;

    public List<Dish> suggestMenu(EventType eventType, PartyPackage pkg) {
        int soMonCanChon = pkg.getDishCount() == null ? 0 : pkg.getDishCount();
        if (soMonCanChon <= 0 || eventType == null) {
            return List.of();
        }

        BigDecimal tienMoiMon = tienAnTrungBinhMoiMon(pkg, soMonCanChon);

        List<Dish> xepTheoDiem = dishEventTypeRepository.findForEvent(eventType).stream()
                .sorted(Comparator.comparingDouble((DishEventType det) -> diem(det, tienMoiMon)).reversed())
                .map(DishEventType::getDish)
                .toList();

        return chonMon(xepTheoDiem, soMonCanChon);
    }

    private BigDecimal tienAnTrungBinhMoiMon(PartyPackage pkg, int soMon) {
        return pkg.getPricePerTable()
                .multiply(TY_LE_TIEN_MON)
                .divide(BigDecimal.valueOf(soMon), 0, RoundingMode.HALF_UP);
    }

    private double diem(DishEventType det, BigDecimal tienMoiMon) {
        Dish mon = det.getDish();
        double tong = 0;

        // 1. Hợp với loại sự kiện
        tong += det.getSuitability() / 5.0 * DIEM_SU_KIEN;

        // 2. Món được gọi nhiều nhất
        if (mon.isBestSeller()) {
            tong += DIEM_BAN_CHAY;
        }

        // 3. Giá sát với tiền ăn trung bình của gói. Món tính giá theo cân thì
        //    không so được, cho điểm trung bình để không bị loại oan.
        if (mon.getPrice() == null || tienMoiMon.signum() == 0) {
            tong += DIEM_GIA / 2.0;
        } else {
            double lech = Math.abs(mon.getPrice().doubleValue() - tienMoiMon.doubleValue())
                    / tienMoiMon.doubleValue();
            tong += Math.max(0, 1 - lech) * DIEM_GIA;
        }

        return tong;
    }

    private List<Dish> chonMon(List<Dish> xepTheoDiem, int soMonCanChon) {
        int tranMoiDanhMuc = Math.max(1, (int) Math.ceil(soMonCanChon / 3.0));

        List<Dish> ketQua = new ArrayList<>();
        Set<Long> daChon = new HashSet<>();
        Map<String, Integer> demTheoDanhMuc = new HashMap<>();

        // Bước 1: đảm bảo thực đơn nào cũng có khai vị, món chính, tráng miệng
        for (String maDanhMuc : DANH_MUC_BAT_BUOC) {
            if (ketQua.size() >= soMonCanChon) {
                break;
            }
            xepTheoDiem.stream()
                    .filter(mon -> mon.getCategory().getCode().equals(maDanhMuc))
                    .findFirst()
                    .ifPresent(mon -> them(mon, ketQua, daChon, demTheoDanhMuc));
        }

        // Bước 2: lấy theo điểm, tôn trọng trần mỗi danh mục
        for (Dish mon : xepTheoDiem) {
            if (ketQua.size() >= soMonCanChon) {
                break;
            }
            String maDanhMuc = mon.getCategory().getCode();
            if (daChon.contains(mon.getId())
                    || demTheoDanhMuc.getOrDefault(maDanhMuc, 0) >= tranMoiDanhMuc) {
                continue;
            }
            them(mon, ketQua, daChon, demTheoDanhMuc);
        }

        // Bước 3: trần làm chưa đủ món thì nới ra cho đủ số món của gói
        for (Dish mon : xepTheoDiem) {
            if (ketQua.size() >= soMonCanChon) {
                break;
            }
            if (!daChon.contains(mon.getId())) {
                them(mon, ketQua, daChon, demTheoDanhMuc);
            }
        }

        return ketQua;
    }

    private void them(Dish mon, List<Dish> ketQua, Set<Long> daChon, Map<String, Integer> dem) {
        ketQua.add(mon);
        daChon.add(mon.getId());
        dem.merge(mon.getCategory().getCode(), 1, Integer::sum);
    }
}
