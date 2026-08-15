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
        LocalDate eventDate,
        String timeSlot,
        String timeSlotLabel,
        Integer guestCount,
        Integer tableCount,
        Long spaceId,
        String spaceName,
        Long packageId,
        String packageName,
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
        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(), b.getCode(),
                b.getEventType().name(), b.getEventType().getLabel(),
                b.getEventDate(),
                b.getTimeSlot().name(), b.getTimeSlot().getLabel(),
                b.getGuestCount(), b.getTableCount(),
                b.getSpace().getId(), b.getSpace().getName(),
                b.getPartyPackage().getId(), b.getPartyPackage().getName(),
                b.getUnitPrice(), b.getFoodAmount(), b.getSpaceFee(), b.getDiscountAmount(),
                b.getVatRate(), b.getVatAmount(), b.getTotalAmount(),
                b.getCustomerName(), b.getCustomerPhone(), b.getCustomerEmail(), b.getNote(),
                b.getStatus().name(), b.getStatus().getLabel(),
                b.getCreatedAt());
    }
}
