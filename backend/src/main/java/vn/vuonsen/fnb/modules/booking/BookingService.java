package vn.vuonsen.fnb.modules.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.booking.dto.BookingResponse;
import vn.vuonsen.fnb.modules.booking.dto.QuoteRequest;
import vn.vuonsen.fnb.modules.booking.dto.QuoteResponse;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Xử lý đặt tiệc: báo giá, tạo đơn, tra cứu, đổi trạng thái
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final SpaceRepository spaceRepository;
    private final PartyPackageRepository packageRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;
    private final BookingProperties properties;

    // Chỉ tính tiền, không lưu vào database
    public QuoteResponse quote(QuoteRequest request) {
        Space space = findSpace(request.spaceId());
        PartyPackage pkg = findPackage(request.packageId());
        validateGuestCount(request.guestCount(), space);
        return QuoteResponse.from(
                pricingService.calculate(space, pkg, request.guestCount(), request.eventDate()));
    }

    @Transactional
    public BookingResponse create(BookingRequest request, Long userId) {
        Space space = findSpace(request.spaceId());
        PartyPackage pkg = findPackage(request.packageId());

        validateGuestCount(request.guestCount(), space);
        validateSlotAvailable(space, request.eventDate(), request.timeSlot());

        PricingService.Quote quote =
                pricingService.calculate(space, pkg, request.guestCount(), request.eventDate());

        User user = userId == null ? null : userRepository.findById(userId).orElse(null);

        Booking booking = Booking.builder()
                .code(nextBookingCode())
                .user(user)
                .space(space)
                .partyPackage(pkg)
                .eventType(request.eventType())
                .eventDate(request.eventDate())
                .timeSlot(request.timeSlot())
                .guestCount(request.guestCount())
                .tableCount(quote.tableCount())
                .unitPrice(quote.unitPrice())
                .foodAmount(quote.foodAmount())
                .spaceFee(quote.spaceFee())
                .discountAmount(quote.discountAmount())
                .vatRate(quote.vatRate())
                .vatAmount(quote.vatAmount())
                .totalAmount(quote.totalAmount())
                .customerName(request.customerName().trim())
                .customerPhone(request.customerPhone().trim())
                .customerEmail(request.customerEmail())
                .note(request.note())
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        recordHistory(saved, null, BookingStatus.PENDING, "Khách gửi yêu cầu từ website",
                user == null ? "guest" : user.getEmail());

        log.info("Da tao don dat tiec {} - {} khach - tong {}", saved.getCode(), saved.getGuestCount(),
                saved.getTotalAmount());
        // TODO: gửi email xác nhận cho khách
        return BookingResponse.from(saved);
    }

    // Tra cứu bằng mã đơn, dành cho khách không có tài khoản
    public BookingResponse getByCode(String code) {
        return bookingRepository.findByCode(code)
                .map(BookingResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("đơn đặt tiệc", code));
    }

    public Page<BookingResponse> listOfUser(Long userId, Pageable pageable) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(BookingResponse::from);
    }

    public Page<BookingResponse> search(BookingStatus status, LocalDate from, LocalDate to,
                                        String keyword, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return bookingRepository.search(status, from, to, kw, pageable).map(BookingResponse::from);
    }

    @Transactional
    public BookingResponse changeStatus(Long bookingId, BookingStatus target, String note, String actor) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("đơn đặt tiệc", bookingId));

        BookingStatus current = booking.getStatus();
        if (current == target) {
            return BookingResponse.from(booking);
        }
        if (!current.canTransitionTo(target)) {
            throw new BusinessException("Không thể chuyển đơn từ '%s' sang '%s'"
                    .formatted(current.getLabel(), target.getLabel()));
        }
        // Trước khi xác nhận phải kiểm tra buổi đó còn trống
        if (target.occupiesSlot()) {
            validateSlotAvailable(booking.getSpace(), booking.getEventDate(), booking.getTimeSlot());
        }

        booking.setStatus(target);
        Booking saved = bookingRepository.save(booking);
        recordHistory(saved, current, target, note, actor);
        return BookingResponse.from(saved);
    }

    private Space findSpace(Long spaceId) {
        return spaceRepository.findById(spaceId)
                .orElseThrow(() -> ResourceNotFoundException.of("không gian", spaceId));
    }

    private PartyPackage findPackage(Long packageId) {
        return packageRepository.findById(packageId)
                .orElseThrow(() -> ResourceNotFoundException.of("gói tiệc", packageId));
    }

    private void validateGuestCount(int guestCount, Space space) {
        if (guestCount < properties.minGuests() || guestCount > properties.maxGuests()) {
            throw new BusinessException("Số khách phải trong khoảng %d - %d"
                    .formatted(properties.minGuests(), properties.maxGuests()));
        }
        if (!space.fitsGuests(guestCount)) {
            throw new BusinessException("%s chỉ phục vụ %d - %d khách, không phù hợp với %d khách"
                    .formatted(space.getName(), space.getCapacityMin(), space.getCapacityMax(), guestCount));
        }
    }

    private void validateSlotAvailable(Space space, LocalDate date, TimeSlot slot) {
        boolean taken = bookingRepository.existsBySpaceIdAndEventDateAndTimeSlotAndStatus(
                space.getId(), date, slot, BookingStatus.CONFIRMED);
        if (taken) {
            throw new BusinessException("%s đã có tiệc vào %s %s, vui lòng chọn buổi hoặc ngày khác"
                    .formatted(space.getName(), slot.getLabel(), date));
        }
    }

    // Sinh mã đơn dạng VS-20260815-0001
    private String nextBookingCode() {
        LocalDate today = LocalDate.now();
        long todayCount = bookingRepository.countCreatedBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        return "VS-%s-%04d".formatted(today.format(CODE_DATE), todayCount + 1);
    }

    private void recordHistory(Booking booking, BookingStatus from, BookingStatus to,
                               String note, String actor) {
        historyRepository.save(BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(actor)
                .note(note)
                .build());
    }

    public long countByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }
}
