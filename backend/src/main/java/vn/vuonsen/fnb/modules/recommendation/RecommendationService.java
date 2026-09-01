package vn.vuonsen.fnb.modules.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * Điểm của mỗi tổ hợp gồm bốn thành phần, mỗi thành phần cho tối đa 25 điểm:
 *   1. Mức phù hợp với loại sự kiện  (lấy từ bảng space_event_types)
 *   2. Mức vừa vặn của sức chứa      (khách lấp đầy sảnh thì điểm cao)
 *   3. Mức khớp ngân sách            (tổng tiền càng sát ngân sách càng tốt)
 *   4. Mức phổ biến theo lịch sử     (tổ hợp nhiều khách chọn thì điểm cao)
 *
 * Không dùng học máy, đúng phạm vi đề cương đã giới hạn.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int MAX_PART = 25;
    private static final int TOP_N = 3;

    private final SpaceRepository spaceRepository;
    private final PartyPackageRepository packageRepository;
    private final SpaceEventTypeRepository spaceEventTypeRepository;
    private final RecommendationLogRepository logRepository;
    private final PricingService pricingService;
    private final MenuSuggestionService menuSuggestionService;

    public List<Suggestion> suggest(RecommendationRequest request) {
        List<Space> spaces = spaceRepository.findByActiveTrueOrderBySortOrderAsc();
        List<PartyPackage> packages = packageRepository.findByActiveTrueOrderBySortOrderAsc();

        Map<Long, Integer> suitabilityBySpace = suitabilityMap(request.eventType());
        Map<String, Long> popularity = popularityMap(request.eventType());
        long maxPopularity = popularity.values().stream().mapToLong(Long::longValue).max().orElse(0);

        List<Suggestion> all = new ArrayList<>();

        for (Space space : spaces) {
            // Không gợi ý không gian không chứa nổi số khách
            if (request.guestCount() > space.getCapacityMax()) {
                continue;
            }
            for (PartyPackage pkg : packages) {
                all.add(score(space, pkg, request, suitabilityBySpace, popularity, maxPopularity));
            }
        }

        // Chỉ dựng thực đơn cho các phương án được chọn, không dựng cho toàn bộ
        // tổ hợp vì phần lớn sẽ bị loại ngay sau khi xếp hạng
        return all.stream()
                .sorted(Comparator.comparing(Suggestion::score).reversed())
                .limit(TOP_N)
                .map(s -> s.withDishes(
                        menuSuggestionService.suggestMenu(request.eventType(), s.partyPackage())))
                .toList();
    }

    private Suggestion score(Space space, PartyPackage pkg, RecommendationRequest request,
                             Map<Long, Integer> suitabilityBySpace,
                             Map<String, Long> popularity, long maxPopularity) {

        List<String> reasons = new ArrayList<>();
        double total = 0;

        // 1. Phù hợp với loại sự kiện
        int suitability = suitabilityBySpace.getOrDefault(space.getId(), 3);
        total += suitability / 5.0 * MAX_PART;
        if (suitability >= 4 && request.eventType() != null) {
            reasons.add("%s rất hợp với %s".formatted(space.getName(), request.eventType().getLabel()));
        }

        // 2. Sức chứa vừa vặn: khách lấp đầy sảnh thì điểm cao, sảnh quá rộng thì trừ điểm
        double fill = (double) request.guestCount() / space.getCapacityMax();
        double fillScore = fill >= 0.6 ? MAX_PART : fill / 0.6 * MAX_PART;
        total += fillScore;
        if (fill >= 0.6) {
            reasons.add("Sức chứa vừa vặn với %d khách".formatted(request.guestCount()));
        } else if (request.guestCount() < space.getCapacityMin()) {
            reasons.add("Số khách dưới mức tối thiểu, sẽ tính tiền theo %d mâm"
                    .formatted(pricingService.minimumTablesFor(space)));
        }

        // 3. Khớp ngân sách
        PricingService.Quote quote =
                pricingService.calculate(space, pkg, request.guestCount(), request.eventDate());
        BigDecimal totalAmount = quote.totalAmount();

        if (request.budget() != null && request.budget().signum() > 0) {
            double ratio = totalAmount.doubleValue() / request.budget().doubleValue();
            if (ratio <= 1) {
                total += MAX_PART;
                reasons.add("Nằm trong ngân sách");
            } else if (ratio <= 1.15) {
                total += MAX_PART * 0.5;
                reasons.add("Vượt ngân sách dưới 15%");
            }
        } else {
            total += MAX_PART * 0.5;   // không khai ngân sách thì cho điểm trung bình
        }

        // 4. Phổ biến theo lịch sử đặt tiệc
        long count = popularity.getOrDefault(key(space.getId(), pkg.getId()), 0L);
        if (maxPopularity > 0) {
            total += (double) count / maxPopularity * MAX_PART;
            if (count > 0) {
                reasons.add("Đã có %d tiệc chọn tổ hợp này".formatted(count));
            }
        }

        BigDecimal rounded = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
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
