package vn.vuonsen.fnb.modules.payment.dto;

import vn.vuonsen.fnb.modules.payment.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Một phiếu thu, dùng cho cả sổ thanh toán của quản trị lẫn lịch sử hiện cho khách
public record PaymentResponse(
        Long id,
        String code,
        String orderType,
        String orderTypeLabel,
        String orderTypeLabelEn,
        String orderCode,
        String purpose,
        String purposeLabel,
        String purposeLabelEn,
        BigDecimal amount,
        String method,
        String methodLabel,
        String methodLabelEn,
        String status,
        String statusLabel,
        String statusLabelEn,
        String reference,
        String note,
        String confirmedBy,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getCode(),
                p.getOrderType().name(), p.getOrderType().getLabel(), p.getOrderType().getLabelEn(), p.maDon(),
                p.getPurpose().name(), p.getPurpose().getLabel(), p.getPurpose().getLabelEn(),
                p.getAmount(),
                p.getMethod().name(), p.getMethod().getLabel(), p.getMethod().getLabelEn(),
                p.getStatus().name(), p.getStatus().getLabel(), p.getStatus().getLabelEn(),
                p.getReference(), p.getNote(),
                p.getConfirmedBy(), p.getConfirmedAt(), p.getCreatedAt());
    }
}
