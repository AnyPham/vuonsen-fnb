package vn.vuonsen.fnb.modules.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.config.props.RecommendationProperties;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.booking.PricingService;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.recommendation.dto.RecommendationRequest;
import vn.vuonsen.fnb.modules.recommendation.dto.Suggestion;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Gợi ý không gian và gói tiệc phù hợp.
 *
 * Điểm mỗi tổ hợp gồm năm tiêu chí, trọng số đọc từ mục app.recommendation
 * trong application.yml:
 *   1. Không gian hợp với loại sự kiện đến đâu   (bảng space_event_types)
 *   2. Số khách lấp đầy sức chứa đến đâu
 *   3. Tầm giá gói tiệc có hợp với loại tiệc không
 *   4. Tổng chi phí có nằm trong ngân sách khách khai không
 *   5. Tổ hợp này đã được bao nhiêu khách trước chọn
 *
 * Cách cộng điểm: mỗi tiêu chí cho một tỉ lệ từ 0 đến 1, nhân với trọng số của
 * nó, rồi chia cho tổng trọng số của những tiêu chí đang dùng được. Nhờ vậy
 * điểm luôn nằm trong thang 100 dù có tiêu chí bị bỏ qua.
 *
 * Vì sao phải bỏ qua tiêu chí: khách không khai ngân sách thì tiêu chí 4 không
 * so được gì, hệ thống chưa có đơn nào thì tiêu chí 5 cũng vậy. Bản trước cho
 * mọi tổ hợp cùng một số điểm ở hai tiêu chí đó, thành ra chúng không phân biệt
 * được gì mà vẫn chiếm chỗ trong thang điểm.
 *
 * Không dùng học máy, đúng phạm vi đề cương đã giới hạn.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final SpaceRepository spaceRepository;
    private final PartyPackageRepository packageRepository;
    private final SpaceEventTypeRepository spaceEventTypeRepository;
    private final RecommendationLogRepository logRepository;
    private final PricingService pricingService;
    private final MenuSuggestionService menuSuggestionService;
    private final RecommendationProperties props;

    public List<Suggestion> suggest(RecommendationRequest request) {
        List<Space> spaces = spaceRepository.findByActiveTrueOrderBySortOrderAsc();
        List<PartyPackage> packages = packageRepository.findByActiveTrueOrderBySortOrderAsc();

        Map<Long, Integer> suitabilityBySpace = suitabilityMap(request.eventType());
        Map<String, Long> popularity = popularityMap(request.eventType());
        long maxPopularity = popularity.values().stream().mapToLong(Long::longValue).max().orElse(0);
        Map<Long, Integer> hangGia = hangGiaCuaGoi(packages);

        List<Suggestion> all = new ArrayList<>();
        for (Space space : spaces) {
            // Không gợi ý không gian không chứa nổi số khách
            if (request.guestCount() > space.getCapacityMax()) {
                continue;
            }
            for (PartyPackage pkg : packages) {
                all.add(score(space, pkg, request, suitabilityBySpace,
                        popularity, maxPopularity, hangGia, packages.size()));
            }
        }

        all.sort(Comparator.comparing(Suggestion::score).reversed());

        // Chỉ dựng thực đơn cho các phương án được chọn, không dựng cho toàn bộ
        // tổ hợp vì phần lớn sẽ bị loại ngay sau khi xếp hạng
        return chonDaDangKhongGian(all).stream()
                .map(s -> s.withDishes(
                        menuSuggestionService.suggestMenu(request.eventType(), s.partyPackage())))
                .toList();
    }

    /*
     * Lấy các phương án điểm cao nhất nhưng giới hạn mỗi không gian xuất hiện
     * tối đa mấy lần. Không có bước này thì cả ba phương án hay rơi vào cùng một
     * sảnh, chỉ khác gói tiệc, khách không có gì để so sánh.
     */
    private List<Suggestion> chonDaDangKhongGian(List<Suggestion> xepTheoDiem) {
        List<Suggestion> ketQua = new ArrayList<>();
        Map<Long, Integer> demTheoKhongGian = new HashMap<>();

        for (Suggestion s : xepTheoDiem) {
            if (ketQua.size() >= props.topN()) {
                break;
            }
            Long maKhongGian = s.space().getId();
            if (demTheoKhongGian.getOrDefault(maKhongGian, 0) >= props.maxPerSpace()) {
                continue;
            }
            ketQua.add(s);
            demTheoKhongGian.merge(maKhongGian, 1, Integer::sum);
        }

        // Nếu ít không gian quá mà chưa đủ số phương án thì nới giới hạn
        for (Suggestion s : xepTheoDiem) {
            if (ketQua.size() >= props.topN()) {
                break;
            }
            if (!ketQua.contains(s)) {
                ketQua.add(s);
            }
        }
        return ketQua;
    }

    /*
     * Xếp các gói tiệc theo giá một mâm từ thấp lên cao, trả về thứ hạng 0, 1, 2...
     * Dùng để biết gói nào là hạng phổ thông, hạng giữa, hạng cao cấp mà không
     * phải viết cứng mã gói vào trong mã nguồn.
     */
    private Map<Long, Integer> hangGiaCuaGoi(List<PartyPackage> packages) {
        List<PartyPackage> theoGia = new ArrayList<>(packages);
        theoGia.sort(Comparator.comparing(PartyPackage::getPricePerTable));

        Map<Long, Integer> hang = new HashMap<>();
        for (int i = 0; i < theoGia.size(); i++) {
            hang.put(theoGia.get(i).getId(), i);
        }
        return hang;
    }

    /*
     * Hạng giá mà từng loại tiệc thường chọn, tính theo thang 0 đến 1.
     * Tiệc cưới và hội nghị hay chọn gói cao cấp, họp mặt gia đình thì chuộng
     * gói phổ thông, sinh nhật nằm ở giữa.
     */
    private double hangGiaMongDoi(EventType eventType) {
        if (eventType == null) {
            return 0.5;
        }
        return switch (eventType) {
            case WEDDING, CORPORATE -> 1.0;
            case FAMILY -> 0.0;
            case BIRTHDAY, OTHER -> 0.5;
        };
    }

    private Suggestion score(Space space, PartyPackage pkg, RecommendationRequest request,
                             Map<Long, Integer> suitabilityBySpace,
                             Map<String, Long> popularity, long maxPopularity,
                             Map<Long, Integer> hangGia, int soGoi) {

        List<String> reasons = new ArrayList<>();
        double tongDiem = 0;
        double tongTrongSo = 0;

        // 1. Không gian hợp với loại sự kiện
        int suitability = suitabilityBySpace.getOrDefault(space.getId(), 3);
        tongDiem += suitability / 5.0 * props.weightEventType();
        tongTrongSo += props.weightEventType();
        if (suitability >= 4 && request.eventType() != null) {
            reasons.add("%s rất hợp với %s".formatted(space.getName(), request.eventType().getLabel()));
        }

        // 2. Sức chứa vừa vặn: khách lấp đầy sảnh thì điểm cao, sảnh quá rộng thì kém
        double fill = (double) request.guestCount() / space.getCapacityMax();
        double tiLeSucChua = Math.min(1.0, fill / props.capacityFitThreshold());
        tongDiem += tiLeSucChua * props.weightCapacityFit();
        tongTrongSo += props.weightCapacityFit();
        if (fill >= props.capacityFitThreshold()) {
            reasons.add("Sức chứa vừa vặn với %d khách".formatted(request.guestCount()));
        } else if (request.guestCount() < space.getCapacityMin()) {
            reasons.add("Số khách dưới mức tối thiểu, sẽ tính tiền theo %d mâm"
                    .formatted(pricingService.minimumTablesFor(space)));
        }

        // 3. Tầm giá gói tiệc hợp với loại tiệc.
        //    Tiêu chí này là thứ duy nhất phân biệt được các gói khi khách chưa
        //    khai ngân sách, bản trước thiếu nên ba gói luôn bằng điểm nhau.
        if (soGoi > 1) {
            double hangCuaGoi = hangGia.getOrDefault(pkg.getId(), 0) / (double) (soGoi - 1);
            double lech = Math.abs(hangCuaGoi - hangGiaMongDoi(request.eventType()));
            double tiLeTamGia = 1 - lech;
            tongDiem += tiLeTamGia * props.weightPackageTier();
            tongTrongSo += props.weightPackageTier();
            if (tiLeTamGia >= 0.99 && request.eventType() != null) {
                reasons.add("Tầm giá %s hợp với %s"
                        .formatted(pkg.getName(), request.eventType().getLabel()));
            }
        }

        PricingService.Quote quote =
                pricingService.calculate(space, pkg, request.guestCount(), request.eventDate());
        BigDecimal totalAmount = quote.totalAmount();

        // 4. Khớp ngân sách. Khách không khai thì bỏ qua tiêu chí này chứ không
        //    cho điểm giống nhau, vì cho giống nhau chỉ làm loãng thang điểm.
        if (request.budget() != null && request.budget().signum() > 0) {
            double ratio = totalAmount.doubleValue() / request.budget().doubleValue();
            double tiLeNganSach;
            if (ratio <= 1) {
                tiLeNganSach = 1;
                reasons.add("Nằm trong ngân sách");
            } else if (ratio <= 1 + props.budgetTolerance()) {
                tiLeNganSach = 0.5;
                reasons.add("Vượt ngân sách dưới %d%%"
                        .formatted(Math.round(props.budgetTolerance() * 100)));
            } else {
                tiLeNganSach = 0;
            }
            tongDiem += tiLeNganSach * props.weightBudget();
            tongTrongSo += props.weightBudget();
        }

        // 5. Phổ biến theo lịch sử. Chưa có đơn nào thì cũng bỏ qua.
        if (maxPopularity > 0) {
            long count = popularity.getOrDefault(key(space.getId(), pkg.getId()), 0L);
            tongDiem += (double) count / maxPopularity * props.weightPopularity();
            tongTrongSo += props.weightPopularity();
            if (count > 0) {
                reasons.add("Đã có %d tiệc chọn tổ hợp này".formatted(count));
            }
        }

        // Quy về thang 100 theo những tiêu chí thật sự dùng được
        double diem = tongTrongSo > 0 ? tongDiem / tongTrongSo * 100 : 0;
        BigDecimal rounded = BigDecimal.valueOf(diem).setScale(2, RoundingMode.HALF_UP);

        // Thực đơn để trống ở bước này, dựng sau cho các phương án được chọn
        return new Suggestion(space, pkg, rounded, totalAmount, quote.tableCount(), reasons, List.of());
    }

    private Map<Long, Integer> suitabilityMap(EventType eventType) {
        if (eventType == null) {
            return Map.of();
        }
        Map<Long, Integer> map = new HashMap<>();
        for (SpaceEventType set : spaceEventTypeRepository.findByEventType(eventType)) {
            map.put(set.getSpace().getId(), set.getSuitability());
        }
        return map;
    }

    private Map<String, Long> popularityMap(EventType eventType) {
        if (eventType == null) {
            return Map.of();
        }
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : logRepository.popularCombinations(eventType)) {
            map.put(key((Long) row[0], (Long) row[1]), (Long) row[2]);
        }
        return map;
    }

    private String key(Long spaceId, Long packageId) {
        return spaceId + ":" + packageId;
    }

    // Ghi nhận gợi ý đã đưa ra, để sau này đối chiếu với đơn khách thật sự đặt
    @Transactional
    public void log(RecommendationRequest request, Suggestion top) {
        logRepository.save(RecommendationLog.builder()
                .guestCount(request.guestCount())
                .eventType(request.eventType())
                .budget(request.budget())
                .eventDate(request.eventDate())
                .suggestedSpace(top.space())
                .suggestedPackage(top.partyPackage())
                .score(top.score())
                .accepted(false)
                .build());
    }
}
