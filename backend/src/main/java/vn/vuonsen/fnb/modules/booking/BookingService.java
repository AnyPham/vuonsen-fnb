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
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        validateLeadTime(request.eventDate(), request.guestCount());
        validatePackageFitsSlot(pkg, request.timeSlot());
        validateSlotAvailable(space, pkg, request.eventDate(), request.timeSlot());

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

        log.info("Đã tạo đơn đặt tiệc {} - {} khách - tổng {}", saved.getCode(), saved.getGuestCount(),
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
            validateSlotAvailable(booking.getSpace(), booking.getPartyPackage(),
                    booking.getEventDate(), booking.getTimeSlot());
        }
        if (target == BookingStatus.CANCELLED) {
            note = withRefundPolicy(booking, note);
        }

        booking.setStatus(target);
        Booking saved = bookingRepository.save(booking);
        recordHistory(saved, current, target, note, actor);

        // Xác nhận một đơn thì các đơn khác cùng chỗ cùng buổi không còn cơ hội, hủy luôn
        // để nhân viên không phải nhớ và khách không bị treo chờ vô hạn.
        if (target.occupiesSlot()) {
            cancelCompetingRequests(saved, actor);
        }
        return BookingResponse.from(saved);
    }

    private void cancelCompetingRequests(Booking confirmed, String actor) {
        List<Booking> waiting = bookingRepository.findBySpaceIdAndEventDateAndTimeSlotAndStatus(
                confirmed.getSpace().getId(), confirmed.getEventDate(),
                confirmed.getTimeSlot(), BookingStatus.PENDING);

        for (Booking other : waiting) {
            if (other.getId().equals(confirmed.getId())) {
                continue;
            }
            other.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(other);
            recordHistory(other, BookingStatus.PENDING, BookingStatus.CANCELLED,
                    "Buổi này đã nhận đơn %s".formatted(confirmed.getCode()), actor);
            log.info("Đã hủy đơn {} do trùng lịch với đơn {}", other.getCode(), confirmed.getCode());
        }
    }

    // Ghi rõ mức hoàn cọc theo thời điểm hủy để hai bên khỏi tranh cãi về sau
    private String withRefundPolicy(Booking booking, String note) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), booking.getEventDate());
        String policy;
        if (daysLeft >= 30) {
            policy = "Hủy trước %d ngày, hoàn 100%% tiền cọc".formatted(daysLeft);
        } else if (daysLeft >= 15) {
            policy = "Hủy trước %d ngày, hoàn 50%% tiền cọc".formatted(daysLeft);
        } else if (daysLeft >= 0) {
            policy = "Hủy trước %d ngày, không hoàn tiền cọc".formatted(daysLeft);
        } else {
            policy = "Hủy sau ngày tổ chức";
        }
        return (note == null || note.isBlank()) ? policy : note + " - " + policy;
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
        // Chỉ chặn khi vượt sức chứa. Khách ít hơn mức tối thiểu vẫn nhận, tính tiền
        // theo số mâm tối thiểu của không gian đó.
        if (guestCount > space.getCapacityMax()) {
            throw new BusinessException("%s chỉ chứa tối đa %d khách, không phục vụ được %d khách"
                    .formatted(space.getName(), space.getCapacityMax(), guestCount));
        }
    }

    // Khách phải báo trước để nhà hàng kịp chuẩn bị nguyên liệu, nhân sự và trang trí.
    // Tiệc càng lớn càng cần nhiều thời gian.
    private void validateLeadTime(LocalDate eventDate, int guestCount) {
        long daysAhead = ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        int tableCount = pricingService.tableCountFor(guestCount);

        int required = tableCount >= properties.largePartyTables()
                ? properties.largePartyMinDays()
                : properties.minDaysAhead();

        if (daysAhead < required) {
            throw new BusinessException(
                    "Tiệc %d mâm cần đặt trước ít nhất %d ngày, ngày bạn chọn chỉ còn %d ngày"
                            .formatted(tableCount, required, Math.max(daysAhead, 0)));
        }
    }

    // Gói tiệc dài hơn thời lượng của buổi thì không phục vụ được
    private void validatePackageFitsSlot(PartyPackage pkg, TimeSlot slot) {
        Integer hours = pkg.getHoursIncluded();
        if (hours == null || isFullDay(pkg)) {
            return;
        }
        if (hours > slot.getDurationHours()) {
            throw new BusinessException(
                    "%s cần %d tiếng, %s chỉ có %d tiếng. Vui lòng chọn buổi khác."
                            .formatted(pkg.getName(), hours, slot.getLabel(), slot.getDurationHours()));
        }
    }

    // Gói thuê trọn ngày chiếm cả ngày, không thể xếp thêm tiệc nào khác vào cùng không gian
    private boolean isFullDay(PartyPackage pkg) {
        Integer hours = pkg.getHoursIncluded();
        return hours != null && hours >= properties.fullDayPackageHours();
    }

    private void validateSlotAvailable(Space space, PartyPackage pkg, LocalDate date, TimeSlot slot) {
        List<Booking> confirmed = bookingRepository.findBySpaceIdAndEventDateAndStatus(
                space.getId(), date, BookingStatus.CONFIRMED);

        for (Booking other : confirmed) {
            if (isFullDay(other.getPartyPackage())) {
                throw new BusinessException("%s đã cho thuê trọn ngày %s theo đơn %s"
                        .formatted(space.getName(), date, other.getCode()));
            }
            if (isFullDay(pkg)) {
                throw new BusinessException("%s đã có tiệc ngày %s nên không thể thuê trọn ngày"
                        .formatted(space.getName(), date));
            }
            if (other.getTimeSlot() == slot) {
                throw new BusinessException("%s đã có tiệc vào %s ngày %s, vui lòng chọn buổi hoặc ngày khác"
                        .formatted(space.getName(), slot.getLabel(), date));
            }
        }
    }

    // Sinh mã đơn dạng VS-20260815-0001.
    // Hai khách bấm gửi cùng lúc có thể ra cùng một số nên phải dò tới khi gặp mã chưa dùng.
    private String nextBookingCode() {
        LocalDate today = LocalDate.now();
        long sequence = bookingRepository.countCreatedBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()) + 1;

        String code = formatCode(today, sequence);
        while (bookingRepository.existsByCode(code)) {
            sequence++;
            code = formatCode(today, sequence);
        }
        return code;
    }

    private String formatCode(LocalDate date, long sequence) {
        return "VS-%s-%04d".formatted(date.format(CODE_DATE), sequence);
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
