package vn.vuonsen.fnb.modules.booking.dto;

import vn.vuonsen.fnb.modules.booking.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String code,
        String eventType,
        String eventTypeLabel,
        String eventTypeLabelEn,
        LocalDate eventDate,
        String timeSlot,
        String timeSlotLabel,
        String timeSlotLabelEn,
        Integer guestCount,
        Integer tableCount,
        Long spaceId,
        String spaceName,
        String spaceNameEn,
        Long packageId,
        String packageName,
        String packageNameEn,
        BigDecimal unitPrice,
        BigDecimal foodAmount,
        BigDecimal spaceFee,
        BigDecimal discountAmount,
        BigDecimal vatRate,
        BigDecimal vatAmount,
        BigDecimal totalAmount,
        String customerName,
        String customerPhone,
        String customerEmail,
        String note,
        String status,
        String statusLabel,
        String statusLabelEn,

        // Tiền cọc: khoản phải đóng, khoản đã thu và lần thu gần nhất
        BigDecimal depositAmount,
        BigDecimal depositPaid,
        LocalDateTime depositPaidAt,
        String depositMethod,

        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(), b.getCode(),
                b.getEventType().name(), b.getEventType().getLabel(), b.getEventType().getLabelEn(),
                b.getEventDate(),
                b.getTimeSlot().name(), b.getTimeSlot().getLabel(), b.getTimeSlot().getLabelEn(),
                b.getGuestCount(), b.getTableCount(),
                b.getSpace().getId(), b.getSpace().getName(), b.getSpace().getNameEn(),
                b.getPartyPackage() == null ? null : b.getPartyPackage().getId(),
                b.tenGoiHienThi(),
                b.getPartyPackage() == null ? null : b.getPartyPackage().getNameEn(),
                b.getUnitPrice(), b.getFoodAmount(), b.getSpaceFee(), b.getDiscountAmount(),
                b.getVatRate(), b.getVatAmount(), b.getTotalAmount(),
                b.getCustomerName(), b.getCustomerPhone(), b.getCustomerEmail(), b.getNote(),
                b.getStatus().name(), b.getStatus().getLabel(), b.getStatus().getLabelEn(),
                b.getDepositAmount(), b.getDepositPaid(), b.getDepositPaidAt(), b.getDepositMethod(),
                b.getCreatedAt());
    }
}
