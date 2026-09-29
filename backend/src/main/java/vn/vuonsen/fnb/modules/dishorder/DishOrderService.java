package vn.vuonsen.fnb.modules.dishorder;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.config.props.DishOrderProperties;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderQuoteRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderQuoteResponse;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderRequest;
import vn.vuonsen.fnb.modules.dishorder.dto.DishOrderResponse;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.config.props.MailProperties;
import vn.vuonsen.fnb.modules.notification.EmailService;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*
 * Nghiệp vụ đơn đặt món lẻ.
 *
 * Khác đặt tiệc ở chỗ không giữ chỗ không gian nào, nên không phải kiểm tra trùng
 * lịch. Bù lại phải kiểm hai thứ mà đặt tiệc không cần: món có bán lẻ được không, và
 * bếp có kịp làm trước giờ khách muốn nhận không.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishOrderService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter GIO_PHUT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final DishOrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final DishOrderPricing pricing;
    private final DishOrderProperties props;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final MailProperties mailProperties;

    // ---------------- Tạm tính ----------------

    public DishOrderQuoteResponse quote(DishOrderQuoteRequest request) {
        List<DongMon> dong = dungDongMon(request.items());
        return dungBangTamTinh(dong, request.fulfillmentType(), request.serveAt());
    }

    // ---------------- Tạo đơn ----------------

    @Transactional
    public DishOrderResponse create(DishOrderRequest request, Long userId) {
        // Khách chưa đăng nhập vẫn đặt được nên userId có thể null
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        List<DongMon> dong = dungDongMon(request.items());
        BigDecimal tienMon = tongTienMon(dong);

        kiemTraHinhThuc(request, tienMon);
        kiemTraThoiGian(request.serveAt(), dong);

        BigDecimal phiGiao = pricing.phiGiao(request.fulfillmentType(), tienMon);
        DishOrderPricing.GiamGia giamGia = pricing.giamGiaNgayLe(tienMon, request.serveAt());
        BigDecimal thue = pricing.thue(tienMon.subtract(giamGia.soTien()));

        DishOrder order = DishOrder.builder()
                .code(nextOrderCode())
                .fulfillmentType(request.fulfillmentType())
                .customerName(request.customerName().trim())
                .customerPhone(request.customerPhone().trim())
                .customerEmail(request.customerEmail() == null || request.customerEmail().isBlank()
                        ? null : request.customerEmail().trim())
                .deliveryAddress(request.fulfillmentType() == FulfillmentType.DELIVERY
                        ? request.deliveryAddress().trim() : null)
                .guestCount(request.fulfillmentType() == FulfillmentType.DINE_IN
                        ? request.guestCount() : null)
                .serveAt(request.serveAt())
                .note(request.note() == null || request.note().isBlank() ? null : request.note().trim())
                .subtotal(tienMon)
                .discountAmount(giamGia.soTien())
                .deliveryFee(phiGiao)
                .vatAmount(thue)
                .total(pricing.tongCong(tienMon, giamGia.soTien(), phiGiao, thue))
                .status(DishOrderStatus.PENDING)
                .user(user)
                .build();

        for (DongMon d : dong) {
            order.themMon(DishOrderItem.builder()
                    .dish(d.mon())
                    .dishName(d.mon().getName())
                    .unitPrice(d.donGia())
                    .quantity(d.soLuong())
                    .lineTotal(d.thanhTien())
                    .build());
        }

        DishOrder daLuu = orderRepository.save(order);
        guiThuXacNhan(daLuu);
        return DishOrderResponse.from(daLuu);
    }

    /*
     * Thư xác nhận đơn đặt món.
     *
     * Cũng như đơn đặt tiệc, khách không để lại email thì bỏ qua. Thư nhắc lại giờ nhận món
     * vì đó là thứ khách hay quên nhất.
     */
    private void guiThuXacNhan(DishOrder o) {
        if (o.getCustomerEmail() == null || o.getCustomerEmail().isBlank()) {
            return;
        }
        emailService.gui(o.getCustomerEmail(),
                "Vườn Sen đã nhận đơn đặt món " + o.getCode(),
                """
                Chào %s,

                Vườn Sen đã nhận đơn đặt món của bạn.

                Mã đơn: %s
                Hình thức: %s
                Thời điểm nhận: %s
                Tổng tiền: %s đồng

                Tra cứu đơn tại: %s/tra-cuu-mon?ma=%s

                Vườn Sen
                """.formatted(
                        o.getCustomerName(), o.getCode(), o.getFulfillmentType().getLabel(),
                        o.getServeAt(), o.getTotal().toPlainString(),
                        mailProperties.baseUrl(), o.getCode()),
                "XAC_NHAN_DON");
    }

    // ---------------- Tra cứu ----------------

    public DishOrderResponse getByCode(String code) {
        return orderRepository.findByCode(code.trim())
                .map(DishOrderResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("đơn đặt món", code));
    }

    public Page<DishOrderResponse> listOfUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(DishOrderResponse::from);
    }

    public Page<DishOrderResponse> search(DishOrderStatus status, FulfillmentType type,
            String keyword, Pageable pageable) {
        String tuKhoa = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return orderRepository.search(status, type, tuKhoa, pageable).map(DishOrderResponse::from);
    }

    @Transactional
    public DishOrderResponse updateStatus(Long id, DishOrderStatus target) {
        DishOrder order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("đơn đặt món", id));

        if (!order.getStatus().canTransitionTo(target)) {
            throw new BusinessException("Không thể chuyển đơn từ %s sang %s"
                    .formatted(order.getStatus().getLabel(), target.getLabel()));
        }
        order.setStatus(target);
        return DishOrderResponse.from(orderRepository.save(order));
    }

    // ---------------- Dựng dòng món ----------------

    private record DongMon(Dish mon, BigDecimal donGia, int soLuong, BigDecimal thanhTien) {
    }

    private List<DongMon> dungDongMon(List<DishOrderRequest.ItemRequest> yeuCau) {
        List<DongMon> ra = new ArrayList<>();

        for (var it : yeuCau) {
            Dish mon = dishRepository.findById(it.dishId())
                    .orElseThrow(() -> ResourceNotFoundException.of("món ăn", it.dishId()));

            if (!mon.isAvailable()) {
                throw new BusinessException("Món %s hiện đã ngừng phục vụ".formatted(mon.getName()));
            }

            /*
             * Món tính giá linh hoạt không đặt lẻ được.
             *
             * Ví dụ heo quay tính theo cân, phải quay xong cân lên mới biết tiền. Đơn
             * đặt món chốt tiền ngay lúc gửi nên không xử lý được kiểu giá này. Nói
             * thẳng và chỉ đường sang đặt tiệc, hơn là để khách đặt rồi mới báo lại.
             */
            if (mon.getPrice() == null) {
                throw new BusinessException(
                        "Món %s tính giá theo thực tế (%s) nên chưa đặt lẻ được. Bạn liên hệ trực tiếp hoặc đặt qua gói tiệc giúp mình nhé."
                                .formatted(mon.getName(),
                                        mon.getPriceNote() == null ? "liên hệ" : mon.getPriceNote()));
            }

            BigDecimal thanhTien = pricing.lamTron(
                    mon.getPrice().multiply(BigDecimal.valueOf(it.quantity())));
            ra.add(new DongMon(mon, mon.getPrice(), it.quantity(), thanhTien));
        }
        return ra;
    }

    private BigDecimal tongTienMon(List<DongMon> dong) {
        return pricing.lamTron(dong.stream()
                .map(DongMon::thanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private DishOrderQuoteResponse dungBangTamTinh(List<DongMon> dong, FulfillmentType hinhThuc,
                                                   LocalDateTime nhanLuc) {
        BigDecimal tienMon = tongTienMon(dong);
        BigDecimal phiGiao = pricing.phiGiao(hinhThuc, tienMon);
        DishOrderPricing.GiamGia giamGia = pricing.giamGiaNgayLe(tienMon, nhanLuc);
        BigDecimal thue = pricing.thue(tienMon.subtract(giamGia.soTien()));

        return new DishOrderQuoteResponse(
                dong.stream()
                        .map(d -> new DishOrderQuoteResponse.LineResponse(
                                d.mon().getId(), d.mon().getName(), d.donGia(),
                                d.soLuong(), d.thanhTien()))
                        .toList(),
                tienMon,
                giamGia.soTien(),
                giamGia.ghiChu(),
                giamGia.ghiChuEn(),
                phiGiao,
                pricing.giaiThichPhiGiao(hinhThuc, tienMon),
                pricing.giaiThichPhiGiaoEn(hinhThuc, tienMon),
                thue,
                pricing.tongCong(tienMon, giamGia.soTien(), phiGiao, thue),
                ghiChuThoiGian(dong),
                ghiChuThoiGianEn(dong));
    }

    // ---------------- Kiểm tra nghiệp vụ ----------------

    private void kiemTraHinhThuc(DishOrderRequest r, BigDecimal tienMon) {
        if (r.fulfillmentType() == FulfillmentType.DELIVERY) {
            if (r.deliveryAddress() == null || r.deliveryAddress().isBlank()) {
                throw new BusinessException("Đơn giao tận nhà cần địa chỉ nhận hàng");
            }
            if (tienMon.compareTo(props.minDeliveryAmount()) < 0) {
                throw new BusinessException(
                        "Đơn giao tận nhà tối thiểu %s. Đơn hiện tại mới %s."
                                .formatted(tienVND(props.minDeliveryAmount()), tienVND(tienMon)));
            }
        } else {
            if (r.guestCount() == null) {
                throw new BusinessException("Đơn ăn tại chỗ cần cho biết số khách");
            }
            if (r.guestCount() > props.maxGuestsDineIn()) {
                throw new BusinessException(
                        "Đơn ăn tại chỗ nhận tối đa %d khách. Đông hơn thì bạn đặt tiệc theo gói giúp mình nhé."
                                .formatted(props.maxGuestsDineIn()));
            }
        }
    }

    /*
     * Kiểm tra thời điểm nhận món.
     *
     * Mức báo trước không cố định mà lấy theo món lâu nhất trong đơn. Đơn toàn món
     * nhanh thì 2 tiếng là đủ, nhưng đơn có heo quay cần 3 tiếng thì phải báo sớm hơn.
     * Dùng lại cột prep_minutes đã nhập cho từng món ở trang chi tiết.
     */
    private void kiemTraThoiGian(LocalDateTime serveAt, List<DongMon> dong) {
        LocalDateTime bayGio = LocalDateTime.now();
        int phutCanNhat = phutLauNhat(dong);
        LocalDateTime somNhat = bayGio.plusMinutes(phutCanNhat);

        if (serveAt.isBefore(somNhat)) {
            String monLau = dong.stream()
                    .filter(d -> d.mon().getPrepMinutes() != null
                            && d.mon().getPrepMinutes() >= phutCanNhat)
                    .map(d -> d.mon().getName())
                    .findFirst().orElse(null);

            String vi = monLau == null ? ""
                    : " Món %s cần khoảng %d phút chuẩn bị.".formatted(monLau, phutCanNhat);
            throw new BusinessException(
                    "Đơn này sớm nhất nhận được lúc %s.%s"
                            .formatted(somNhat.format(GIO_PHUT), vi));
        }

        if (serveAt.isAfter(bayGio.plusDays(props.maxDaysAhead()))) {
            throw new BusinessException("Chỉ nhận đơn trong vòng %d ngày tới"
                    .formatted(props.maxDaysAhead()));
        }
    }

    private int phutLauNhat(List<DongMon> dong) {
        int theoMon = dong.stream()
                .map(d -> d.mon().getPrepMinutes())
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        return Math.max(props.minHoursAhead() * 60, theoMon);
    }

    private String ghiChuThoiGian(List<DongMon> dong) {
        int phut = phutLauNhat(dong);
        LocalDateTime somNhat = LocalDateTime.now().plusMinutes(phut);
        return "Sớm nhất nhận được lúc %s.".formatted(somNhat.format(GIO_PHUT));
    }

    private String ghiChuThoiGianEn(List<DongMon> dong) {
        int phut = phutLauNhat(dong);
        LocalDateTime somNhat = LocalDateTime.now().plusMinutes(phut);
        return "The earliest we can have this ready is %s.".formatted(somNhat.format(GIO_PHUT));
    }

    // ---------------- Tiện ích ----------------

    /*
     * Sinh mã đơn dạng DM-20260905-0001, cố ý khác tiền tố VS của đơn đặt tiệc để
     * nhân viên nhìn mã là biết ngay loại đơn nào.
     */
    private String nextOrderCode() {
        LocalDate today = LocalDate.now();
        long stt = orderRepository.countCreatedBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()) + 1;

        String code = "DM-%s-%04d".formatted(today.format(CODE_DATE), stt);
        while (orderRepository.existsByCode(code)) {
            stt++;
            code = "DM-%s-%04d".formatted(today.format(CODE_DATE), stt);
        }
        return code;
    }

    private String tienVND(BigDecimal so) {
        return "%,d".formatted(so.longValue()).replace(',', '.') + "đ";
    }
}
