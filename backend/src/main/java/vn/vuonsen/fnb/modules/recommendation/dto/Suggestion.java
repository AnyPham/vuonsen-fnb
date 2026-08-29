package vn.vuonsen.fnb.modules.recommendation.dto;

import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.space.Space;

import java.math.BigDecimal;
import java.util.List;

// Kết quả chấm điểm trong nội bộ, chưa phải dạng gửi ra ngoài
public record Suggestion(
        Space space,
        PartyPackage partyPackage,
        BigDecimal score,
        BigDecimal totalAmount,
        int tableCount,
        List<String> reasons
) {
}
