package vn.vuonsen.fnb.modules.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.booking.TimeSlot;

import java.time.LocalDate;

// Dữ liệu form đặt tiệc 3 bước
public record BookingRequest(
        // Bước 1: sự kiện và số khách
        @NotNull(message = "Vui lòng chọn loại hình sự kiện") EventType eventType,
        @NotNull(message = "Vui lòng chọn ngày tổ chức")
        @Future(message = "Ngày tổ chức phải sau ngày hôm nay") LocalDate eventDate,
        @NotNull(message = "Vui lòng chọn buổi") TimeSlot timeSlot,
        @NotNull(message = "Vui lòng nhập số khách")
        @Min(value = 1, message = "Số khách phải lớn hơn 0") Integer guestCount,

        // Bước 2: chọn không gian và gói tiệc
        @NotNull(message = "Vui lòng chọn không gian") Long spaceId,
        @NotNull(message = "Vui lòng chọn gói tiệc") Long packageId,

        // Bước 3: thông tin liên hệ
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(min = 2, max = 120, message = "Họ tên từ 2 đến 120 ký tự") String customerName,
        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "^[0-9\\s.+()-]{9,15}$", message = "Số điện thoại không hợp lệ") String customerPhone,
        @Email(message = "Email không hợp lệ") @Size(max = 160) String customerEmail,
        @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự") String note
) {
}
