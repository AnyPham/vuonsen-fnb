package vn.vuonsen.fnb.modules.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.config.props.MailProperties;
import vn.vuonsen.fnb.modules.booking.dto.MonTuChonRequest;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.booking.dto.BookingResponse;
import vn.vuonsen.fnb.modules.booking.dto.QuoteRequest;
import vn.vuonsen.fnb.modules.booking.dto.QuoteResponse;
import vn.vuonsen.fnb.modules.booking.dto.TinhTrangTrongResponse;
import vn.vuonsen.fnb.modules.notification.EmailService;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final DishRepository dishRepository;
    private final PricingService pricingService;
    private final EmailService emailService;
    private final BookingProperties properties;
    private final MailProperties mailProperties;

    // Chỉ tính tiền, không lưu vào database
    public QuoteResponse quote(QuoteRequest request) {
        Space space = findSpace(request.spaceId());
        PartyPackage pkg = findPackage(request.packageId());
        validateGuestCount(request.guestCount(), space);

        /*
         * Báo giá phải đi qua đúng những ràng buộc của lúc đặt thật, không thì khách thấy
         * một con số trên bảng tạm tính rồi bấm gửi lại bị từ chối.
         */
        List<MonDaChon> monDaChon = docMonTuChon(
                new BookingRequest(null, null, null, request.guestCount(), request.spaceId(),
                        request.packageId(), request.monTuChon(), null, null, null, null),
                pkg);
        BigDecimal tienMon = monDaChon.isEmpty() ? null : tongTienMon(monDaChon);

        return QuoteResponse.from(pricingService.calculate(
                space, pkg, tienMon, request.guestCount(), request.eventDate()));
    }

    /*
     * Tiệc tự chọn món chỉ mở cho nhóm nhỏ.
     *
     * Trên ngưỡng này thì bếp không kham nổi việc nấu lẻ từng món cho cả trăm khách, và
     * gói tiệc cố định mới là thứ hợp lý. Con số lấy đúng bằng mức đã thống nhất với
     * nhà hàng cho hình thức đặt sảnh nhỏ.
     *
     * Để công khai vì trợ lý tư vấn cũng phải nói đúng con số này khi khách hỏi. Gõ lại
     * số 40 ở chỗ thứ hai thì sớm muộn hai nơi cũng lệch nhau.
     */
    public static final int SO_KHACH_TOI_DA_TU_CHON = 40;

    @Transactional
    public BookingResponse create(BookingRequest request, Long userId) {
        Space space = findSpace(request.spaceId());
        PartyPackage pkg = findPackage(request.packageId());

        List<MonDaChon> monDaChon = docMonTuChon(request, pkg);

        validateGuestCount(request.guestCount(), space);
        validateLeadTime(request.eventDate(), request.guestCount());
        validatePackageFitsSlot(pkg, request.timeSlot());
        validateSlotAvailable(space, pkg, request.eventDate(), request.timeSlot());

        BigDecimal tienMon = monDaChon.isEmpty() ? null : tongTienMon(monDaChon);
        PricingService.Quote quote = pricingService.calculate(
                space, pkg, tienMon, request.guestCount(), request.eventDate());

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
                // Khoản phải cọc chốt luôn ở đây, đổi tỉ lệ cọc sau này không đụng tới đơn cũ
                .depositAmount(quote.depositAmount())
                .monTuChon(new java.util.ArrayList<>())
                .customerName(request.customerName().trim())
                .customerPhone(request.customerPhone().trim())
                .customerEmail(request.customerEmail())
                .note(request.note())
                .status(BookingStatus.PENDING)
                /*
                 * Giữ buổi này lại cho khách trong lúc họ đi trả cọc.
                 *
                 * Từ lúc này tới khi hết hạn, không ai khác đặt được buổi đó. Trả cọc kịp
                 * thì chỗ được giữ hẳn; không kịp thì mốc này trôi qua và buổi đó tự trống
                 * lại, không cần ai can thiệp.
                 */
                .giuChoDen(LocalDateTime.now().plusMinutes(properties.holdMinutes()))
                .build();

        /*
         * Chép tên và giá món tại thời điểm đặt. Nhà hàng đổi giá về sau thì đơn đã chốt
         * vẫn giữ nguyên con số khách đã đồng ý.
         */
        for (MonDaChon m : monDaChon) {
            booking.getMonTuChon().add(BookingDish.builder()
                    .booking(booking)
                    .dish(m.mon())
                    .tenMon(m.mon().getName())
                    .soLuong(m.soLuong())
                    .donGia(m.mon().getPrice())
                    .build());
        }

        Booking saved = bookingRepository.save(booking);
        recordHistory(saved, null, BookingStatus.PENDING, "Khách gửi yêu cầu từ website",
                user == null ? "guest" : user.getEmail());

        log.info("Đã tạo đơn đặt tiệc {} - {} khách - tổng {}", saved.getCode(), saved.getGuestCount(),
                saved.getTotalAmount());
        guiThuXacNhan(saved);
        return BookingResponse.from(saved);
    }

    /*
     * Thư xác nhận gửi ngay sau khi đơn đã lưu.
     *
     * Khách không để lại email thì bỏ qua, vì email là trường không bắt buộc khi đặt tiệc.
     * Thư ghi rõ mã đơn và số tiền cọc, hai thứ khách cần ngay sau khi đặt.
     */
    private void guiThuXacNhan(Booking b) {
        if (b.getCustomerEmail() == null || b.getCustomerEmail().isBlank()) {
            return;
        }
        emailService.gui(b.getCustomerEmail(),
                "Vườn Sen đã nhận yêu cầu đặt tiệc " + b.getCode(),
                """
                Chào %s,

                Vườn Sen đã nhận yêu cầu đặt tiệc của bạn. Bộ phận kinh doanh sẽ liên hệ xác
                nhận trong vòng 24 giờ.

                Mã đơn: %s
                Ngày tổ chức: %s, %s
                Số khách: %d
                Không gian: %s
                Gói tiệc: %s
                Tổng tạm tính: %s đồng
                Cần đặt cọc giữ ngày: %s đồng

                Tra cứu đơn bất cứ lúc nào tại: %s/tra-cuu?code=%s
                Thanh toán cọc tại: %s/thanh-toan?ma=%s

                Vườn Sen
                """.formatted(
                        b.getCustomerName(), b.getCode(), b.getEventDate(), b.getTimeSlot().getLabel(),
                        b.getGuestCount(), b.getSpace().getName(), b.tenGoiHienThi(),
                        b.getTotalAmount().toPlainString(), b.getDepositAmount().toPlainString(),
                        mailProperties.baseUrl(), b.getCode(),
                        mailProperties.baseUrl(), b.getCode()),
                "XAC_NHAN_DON");
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

    /*
     * Tình trạng còn trống của tất cả không gian trong một ngày.
     *
     * Chỉ đơn đã xác nhận mới coi là chiếm chỗ, giống đúng quy tắc mà bước gửi đơn đang dùng;
     * đơn còn chờ duyệt thì chưa chắc thành tiệc nên không khóa chỗ của người khác.
     */
    public TinhTrangTrongResponse tinhTrangTrong(LocalDate ngay) {
        Map<Long, List<Booking>> theoKhongGian = bookingRepository
                .findTrongNgay(ngay, BookingStatus.CONFIRMED).stream()
                .collect(Collectors.groupingBy(b -> b.getSpace().getId()));

        List<TinhTrangTrongResponse.KhongGian> ra = spaceRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(space -> {
                    List<Booking> daDat = theoKhongGian.getOrDefault(space.getId(), List.of());
                    boolean caNgay = daDat.stream().anyMatch(b -> isFullDay(b.getPartyPackage()));
                    List<String> buoiDaKin = daDat.stream()
                            .map(b -> b.getTimeSlot().name())
                            .distinct()
                            .toList();
                    return new TinhTrangTrongResponse.KhongGian(
                            space.getId(), space.getName(), buoiDaKin, caNgay);
                })
                .toList();

        return new TinhTrangTrongResponse(ngay, ra);
    }

    /*
     * Ghi nhận tiền cọc khách đã đóng.
     *
     * Website chưa nối cổng thanh toán nên tiền vào bằng chuyển khoản hoặc tiền mặt tại quầy;
     * hàm này chỉ ghi lại việc đó, không tự thu tiền. Mỗi lần gọi là một lần thu, cộng dồn vào
     * khoản đã đóng để khách đóng cọc làm hai lần vẫn ghi được.
     */
    @Transactional
    public BookingResponse ghiNhanCoc(Long bookingId, BigDecimal soTien, String hinhThuc, String actor) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("đơn đặt tiệc", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Đơn đã hủy thì không ghi nhận cọc được");
        }
        if (soTien == null || soTien.signum() <= 0) {
            throw new BusinessException("Số tiền cọc phải lớn hơn 0");
        }

        BigDecimal daDong = booking.getDepositPaid().add(soTien);
        if (daDong.compareTo(booking.getTotalAmount()) > 0) {
            throw new BusinessException("Tổng tiền đã thu (%s) vượt quá tổng tiền của đơn"
                    .formatted(daDong.toPlainString()));
        }

        booking.setDepositPaid(daDong);
        booking.setDepositPaidAt(LocalDateTime.now());
        booking.setDepositMethod("CASH".equals(hinhThuc) ? "CASH" : "TRANSFER");

        Booking saved = bookingRepository.save(booking);
        log.info("Đơn {} ghi nhận cọc {} bởi {}", saved.getCode(), soTien, actor);
        return BookingResponse.from(saved);
    }

    /*
     * Đổi trạng thái đơn, trong đó bước xác nhận phải chống hai người làm cùng lúc.
     *
     * Mức cô lập READ_COMMITTED là bắt buộc chứ không phải tùy chọn. MariaDB mặc định dùng
     * REPEATABLE_READ: mọi câu đọc trong một giao dịch nhìn thấy ảnh chụp dữ liệu từ lần
     * đọc đầu tiên. Người đến sau dù đã chờ lấy được khóa vẫn đọc theo ảnh chụp cũ, không
     * thấy đơn người trước vừa xác nhận, và lại xác nhận trùng. READ_COMMITTED cho mỗi
     * câu đọc thấy dữ liệu mới nhất đã lưu, nên vừa lấy được khóa là thấy ngay.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
            // Khóa không gian trước rồi mới kiểm tra lịch. Không khóa thì hai người xác
            // nhận cùng lúc sẽ cùng thấy buổi còn trống và cùng xác nhận.
            spaceRepository.khoaDeXacNhanDon(booking.getSpace().getId());
            validateSlotAvailable(booking.getSpace(), booking.getPartyPackage(),
                    booking.getEventDate(), booking.getTimeSlot());
        }
        if (target == BookingStatus.CANCELLED) {
            note = chotTienHoan(booking, note);
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

    /*
     * Chốt số tiền hoàn lại khi khách hủy đơn.
     *
     * Trước đây chỗ này chỉ ghi một câu mô tả chính sách vào lịch sử, ví dụ "hoàn 50% tiền
     * cọc", mà không tính ra con số. Nhân viên phải tự nhẩm, còn khách tra đơn thì không
     * thấy mình được nhận lại bao nhiêu.
     *
     * Tính trên số tiền khách đã đóng thật, không phải số phải đóng: khách mới đóng một
     * nửa cọc thì chỉ hoàn theo phần đã đóng đó.
     *
     * Chép cả tỉ lệ lẫn số tiền vào đơn. Nhà hàng đổi chính sách hoàn tiền về sau thì đơn
     * đã hủy vẫn giữ nguyên con số hai bên đã thống nhất lúc hủy.
     */
    private String chotTienHoan(Booking booking, String note) {
        long conLai = ChronoUnit.DAYS.between(LocalDate.now(), booking.getEventDate());
        BigDecimal daDong = booking.getDepositPaid() == null ? BigDecimal.ZERO : booking.getDepositPaid();

        BigDecimal tiLe;
        String moTa;
        if (conLai < 0) {
            tiLe = BigDecimal.ZERO;
            moTa = "Hủy sau ngày tổ chức, không hoàn tiền cọc";
        } else if (conLai >= properties.refundFullDays()) {
            tiLe = BigDecimal.ONE;
            moTa = "Hủy trước %d ngày, hoàn 100%% tiền cọc".formatted(conLai);
        } else if (conLai >= properties.refundHalfDays()) {
            tiLe = new BigDecimal("0.50");
            moTa = "Hủy trước %d ngày, hoàn 50%% tiền cọc".formatted(conLai);
        } else {
            tiLe = BigDecimal.ZERO;
            moTa = "Hủy trước %d ngày, không hoàn tiền cọc".formatted(conLai);
        }

        BigDecimal tienHoan = daDong.multiply(tiLe).setScale(2, java.math.RoundingMode.HALF_UP);
        booking.setRefundRate(tiLe);
        booking.setRefundAmount(tienHoan);
        booking.setRefundNote(moTa);

        /*
         * Khách chưa đóng đồng nào thì không nói chuyện hoàn tiền, chỉ ghi lý do hủy.
         * Ghi "hoàn 0 đồng" cho một đơn chưa thu tiền chỉ làm người đọc tưởng có tranh chấp.
         */
        String cau = daDong.signum() == 0
                ? moTa + " (chưa thu tiền cọc nên không phát sinh hoàn trả)"
                : moTa + ", hoàn %,.0f trên %,.0f đã đóng".formatted(tienHoan, daDong);

        log.info("Hủy đơn {}: còn {} ngày, đã đóng {}, hoàn {}",
                booking.getCode(), conLai, daDong, tienHoan);

        return (note == null || note.isBlank()) ? cau : note + " - " + cau;
    }

    private Space findSpace(Long spaceId) {
        return spaceRepository.findById(spaceId)
                .orElseThrow(() -> ResourceNotFoundException.of("không gian", spaceId));
    }

    // Không chọn gói nghĩa là chỉ thuê không gian, trả về null thay vì báo lỗi
    /* Một món đã đối chiếu với cơ sở dữ liệu, kèm số phần khách gọi. */
    private record MonDaChon(Dish mon, int soLuong) {
    }

    /*
     * Đọc danh sách món khách tự chọn và kiểm tra ngay tại đây.
     *
     * Bốn điều kiện: không đi cùng gói tiệc, chỉ cho nhóm nhỏ, món phải đang phục vụ, và
     * không được gọi trùng một món hai dòng. Kiểm ở máy chủ chứ không tin giao diện, vì
     * đường dẫn API gọi thẳng được.
     */
    private List<MonDaChon> docMonTuChon(BookingRequest request, PartyPackage pkg) {
        List<MonTuChonRequest> gui = request.monTuChon();
        if (gui == null || gui.isEmpty()) {
            return List.of();
        }
        if (pkg != null) {
            throw new IllegalArgumentException(
                    "Đã chọn gói tiệc thì không chọn thêm món lẻ. Bỏ gói tiệc nếu muốn tự chọn thực đơn.");
        }
        if (request.guestCount() > SO_KHACH_TOI_DA_TU_CHON) {
            throw new IllegalArgumentException(
                    "Thực đơn tự chọn chỉ nhận tối đa " + SO_KHACH_TOI_DA_TU_CHON
                            + " khách. Từ " + (SO_KHACH_TOI_DA_TU_CHON + 1) + " khách trở lên, vui lòng chọn một gói tiệc.");
        }

        java.util.Set<Long> daGap = new java.util.HashSet<>();
        List<MonDaChon> ketQua = new java.util.ArrayList<>();
        for (MonTuChonRequest m : gui) {
            if (!daGap.add(m.dishId())) {
                throw new IllegalArgumentException("Một món chỉ được chọn một dòng, hãy cộng số phần lại");
            }
            Dish mon = dishRepository.findById(m.dishId())
                    .orElseThrow(() -> ResourceNotFoundException.of("món ăn", m.dishId()));
            if (!mon.isAvailable()) {
                throw new IllegalArgumentException("Món " + mon.getName() + " hiện không phục vụ");
            }
            ketQua.add(new MonDaChon(mon, m.soLuong()));
        }
        return ketQua;
    }

    private BigDecimal tongTienMon(List<MonDaChon> cacMon) {
        return cacMon.stream()
                .map(m -> m.mon().getPrice().multiply(BigDecimal.valueOf(m.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PartyPackage findPackage(Long packageId) {
        if (packageId == null) {
            return null;
        }
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
        if (pkg == null) {
            return; // chỉ thuê không gian thì không có thời lượng gói để so với buổi
        }
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
        // Đơn chỉ thuê không gian tính theo một buổi, không chiếm trọn ngày
        if (pkg == null) {
            return false;
        }
        Integer hours = pkg.getHoursIncluded();
        return hours != null && hours >= properties.fullDayPackageHours();
    }

    /*
     * Buổi đó còn nhận được đơn nữa không.
     *
     * Trước đây chỉ xét đơn đã xác nhận, nên ba khách cùng chọn một buổi đều gửi đơn
     * được rồi cùng chờ, và hai người thua cuộc chờ vô ích. Nay xét thêm đơn đang giữ
     * chỗ tạm và đơn đã trả cọc, để người thứ hai biết ngay buổi đó đang có người giữ.
     *
     * Lọc trong bộ nhớ thay vì thêm điều kiện vào câu truy vấn, vì luật chiếm chỗ nằm
     * gọn trong Booking.dangChiemCho và còn được dùng ở chỗ khác. Mỗi không gian một
     * ngày chỉ có vài đơn nên lọc ở đây không tốn gì.
     */
    private void validateSlotAvailable(Space space, PartyPackage pkg, LocalDate date, TimeSlot slot) {
        List<Booking> dangGiu = bookingRepository
                .findBySpaceIdAndEventDate(space.getId(), date).stream()
                .filter(Booking::dangChiemCho)
                .toList();

        for (Booking other : dangGiu) {
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
