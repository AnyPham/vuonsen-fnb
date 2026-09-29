package vn.vuonsen.fnb.modules.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.common.exception.ResourceNotFoundException;
import vn.vuonsen.fnb.config.props.PaymentProperties;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingRepository;
import vn.vuonsen.fnb.modules.booking.BookingStatus;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;
import vn.vuonsen.fnb.modules.dishorder.DishOrderRepository;
import vn.vuonsen.fnb.modules.dishorder.DishOrderStatus;
import vn.vuonsen.fnb.modules.payment.dto.PaymentResponse;
import vn.vuonsen.fnb.modules.payment.dto.TinhHinhThanhToanResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Thu tiền của đơn đặt tiệc và đơn đặt món.
 *
 * Hệ thống không nối trực tiếp với ngân hàng nên không tự biết tiền đã về hay chưa. Luồng
 * thực tế là: khách quét mã VietQR chuyển khoản, hệ thống ghi một phiếu ở trạng thái chờ
 * đối soát, nhân viên mở sao kê thấy tiền rồi bấm xác nhận. Chậm hơn cổng thanh toán tự
 * động nhưng không bao giờ ghi nhận nhầm một khoản chưa về tới tài khoản.
 *
 * Số tiền đã thu trên đơn chỉ cộng lên khi phiếu được xác nhận, không cộng lúc tạo phiếu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final DateTimeFormatter NGAY_MA = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final DishOrderRepository dishOrderRepository;
    private final PaymentProperties properties;
    private final VnPayService vnPayService;

    // ---------------------------------------------------------------- tra cứu

    public TinhHinhThanhToanResponse tinhHinh(String maDon) {
        return timDonTiec(maDon)
                .map(this::tinhHinhCuaTiec)
                .or(() -> timDonMon(maDon).map(this::tinhHinhCuaMon))
                .orElseThrow(() -> ResourceNotFoundException.of("đơn hàng", maDon));
    }

    public Page<PaymentResponse> danhSach(PaymentStatus status, String keyword, Pageable pageable) {
        String tuKhoa = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return paymentRepository.search(status, tuKhoa, pageable).map(PaymentResponse::from);
    }

    public long dem(PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }

    // ------------------------------------------------------------- tạo phiếu

    /*
     * Khách bấm thanh toán: tạo phiếu chờ đối soát cho phần còn thiếu.
     *
     * Bấm hai lần thì không sinh hai phiếu. Phiếu cũ còn chờ mà số tiền vẫn đúng thì đưa lại
     * chính nó, vì mã QR gắn với nội dung chuyển khoản của phiếu đó.
     */
    @Transactional
    public TinhHinhThanhToanResponse taoYeuCau(String maDon, PaymentPurpose mucDich) {
        Optional<Booking> tiec = timDonTiec(maDon);
        if (tiec.isPresent()) {
            taoYeuCauChoTiec(tiec.get(), mucDich);
            return tinhHinhCuaTiec(tiec.get());
        }

        DishOrder mon = timDonMon(maDon)
                .orElseThrow(() -> ResourceNotFoundException.of("đơn hàng", maDon));
        taoYeuCauChoMon(mon);
        return tinhHinhCuaMon(mon);
    }

    private void taoYeuCauChoTiec(Booking booking, PaymentPurpose mucDich) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Đơn đã hủy thì không thanh toán được");
        }

        PaymentPurpose purpose = mucDich == null ? PaymentPurpose.DEPOSIT : mucDich;
        BigDecimal soTien = soTienCanTra(booking, purpose);
        if (soTien.signum() <= 0) {
            throw new BusinessException(purpose == PaymentPurpose.DEPOSIT
                    ? "Đơn này đã đóng đủ tiền cọc"
                    : "Đơn này đã thanh toán đủ");
        }

        Optional<Payment> dangCho = paymentRepository
                .findFirstByBookingIdAndPurposeAndStatus(booking.getId(), purpose, PaymentStatus.PENDING);
        if (dangCho.isPresent() && dangCho.get().getAmount().compareTo(soTien) == 0) {
            return;
        }
        dangCho.ifPresent(cu -> cu.setStatus(PaymentStatus.CANCELLED));

        paymentRepository.save(Payment.builder()
                .code(sinhMa("TT-B"))
                .orderType(OrderType.BOOKING)
                .booking(booking)
                .purpose(purpose)
                .amount(soTien)
                .method(PaymentMethod.VIETQR)
                .status(PaymentStatus.PENDING)
                .reference(noiDungChuyenKhoan(booking.getCode()))
                .build());
    }

    private void taoYeuCauChoMon(DishOrder order) {
        if (order.getStatus() == DishOrderStatus.CANCELLED) {
            throw new BusinessException("Đơn đã hủy thì không thanh toán được");
        }

        BigDecimal soTien = order.getTotal().subtract(order.getPaidAmount());
        if (soTien.signum() <= 0) {
            throw new BusinessException("Đơn này đã thanh toán đủ");
        }

        Optional<Payment> dangCho = paymentRepository.findFirstByDishOrderIdAndPurposeAndStatus(
                order.getId(), PaymentPurpose.FULL, PaymentStatus.PENDING);
        if (dangCho.isPresent() && dangCho.get().getAmount().compareTo(soTien) == 0) {
            return;
        }
        dangCho.ifPresent(cu -> cu.setStatus(PaymentStatus.CANCELLED));

        order.setPaymentMethod(PaymentMethod.VIETQR.name());
        paymentRepository.save(Payment.builder()
                .code(sinhMa("TT-M"))
                .orderType(OrderType.DISH_ORDER)
                .dishOrder(order)
                .purpose(PaymentPurpose.FULL)
                .amount(soTien)
                .method(PaymentMethod.VIETQR)
                .status(PaymentStatus.PENDING)
                .reference(noiDungChuyenKhoan(order.getCode()))
                .build());
    }

    /*
     * Nhân viên thu tiền mặt tại quầy: ghi thẳng một phiếu đã xác nhận.
     *
     * Không qua bước chờ đối soát vì tiền đã nằm trong két, không có gì phải soát.
     */
    @Transactional
    public PaymentResponse ghiThuTaiQuay(String maDon, BigDecimal soTien, PaymentPurpose mucDich,
                                         PaymentMethod hinhThuc, String ghiChu, String nguoiThu) {
        if (soTien == null || soTien.signum() <= 0) {
            throw new BusinessException("Số tiền phải lớn hơn 0");
        }

        Payment.PaymentBuilder phieu = Payment.builder()
                .amount(soTien)
                .method(hinhThuc == null ? PaymentMethod.CASH : hinhThuc)
                .status(PaymentStatus.PENDING)
                .note(ghiChu);

        Optional<Booking> tiec = timDonTiec(maDon);
        if (tiec.isPresent()) {
            phieu.code(sinhMa("TT-B"))
                    .orderType(OrderType.BOOKING)
                    .booking(tiec.get())
                    .purpose(mucDich == null ? PaymentPurpose.DEPOSIT : mucDich)
                    .reference(tiec.get().getCode());
        } else {
            DishOrder mon = timDonMon(maDon)
                    .orElseThrow(() -> ResourceNotFoundException.of("đơn hàng", maDon));
            phieu.code(sinhMa("TT-M"))
                    .orderType(OrderType.DISH_ORDER)
                    .dishOrder(mon)
                    .purpose(PaymentPurpose.FULL)
                    .reference(mon.getCode());
        }

        return xacNhan(paymentRepository.save(phieu.build()).getId(), nguoiThu, null);
    }

    // ----------------------------------------------------------- đối soát

    @Transactional
    public PaymentResponse xacNhan(Long id, String nguoiXacNhan, String maGiaoDich) {
        Payment phieu = paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("phiếu thu", id));

        if (phieu.getStatus() == PaymentStatus.CONFIRMED) {
            return PaymentResponse.from(phieu);
        }
        if (phieu.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("Phiếu đã hủy thì không xác nhận lại được");
        }

        congTienVaoDon(phieu);

        phieu.setStatus(PaymentStatus.CONFIRMED);
        phieu.setConfirmedBy(nguoiXacNhan);
        phieu.setConfirmedAt(LocalDateTime.now());
        if (maGiaoDich != null && !maGiaoDich.isBlank()) {
            phieu.setReference(maGiaoDich.trim());
        }

        log.info("Xác nhận phiếu thu {} của đơn {} số tiền {} bởi {}",
                phieu.getCode(), phieu.maDon(), phieu.getAmount(), nguoiXacNhan);
        return PaymentResponse.from(phieu);
    }

    @Transactional
    public PaymentResponse huy(Long id, String lyDo) {
        Payment phieu = paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("phiếu thu", id));

        if (phieu.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessException("Phiếu đã xác nhận nhận tiền thì không hủy được, "
                    + "muốn trả lại tiền cho khách thì ghi một phiếu hoàn tiền");
        }
        phieu.setStatus(PaymentStatus.CANCELLED);
        phieu.setNote(lyDo);
        return PaymentResponse.from(phieu);
    }

    /*
     * Cộng tiền vào đơn khi phiếu được xác nhận.
     *
     * Chặn thu quá tổng tiền đơn: thu dư thì sổ sách lệch và không ai biết phải trả lại bao
     * nhiêu. Muốn ghi nhận khoản dư thì phải sửa đơn trước.
     */
    private void congTienVaoDon(Payment phieu) {
        if (phieu.getOrderType() == OrderType.BOOKING) {
            Booking booking = phieu.getBooking();
            BigDecimal daThu = booking.getDepositPaid().add(phieu.getAmount());
            if (daThu.compareTo(booking.getTotalAmount()) > 0) {
                throw new BusinessException("Tổng tiền đã thu vượt quá tổng tiền của đơn %s"
                        .formatted(booking.getCode()));
            }
            booking.setDepositPaid(daThu);
            booking.setDepositPaidAt(LocalDateTime.now());
            booking.setDepositMethod(phieu.getMethod().name());
            return;
        }

        DishOrder order = phieu.getDishOrder();
        BigDecimal daThu = order.getPaidAmount().add(phieu.getAmount());
        if (daThu.compareTo(order.getTotal()) > 0) {
            throw new BusinessException("Tổng tiền đã thu vượt quá tổng tiền của đơn %s"
                    .formatted(order.getCode()));
        }
        order.setPaidAmount(daThu);
        order.setPaymentMethod(phieu.getMethod().name());
    }

    // ------------------------------------------------------------- dựng dữ liệu trả về

    private TinhHinhThanhToanResponse tinhHinhCuaTiec(Booking b) {
        BigDecimal daThu = b.getDepositPaid();
        BigDecimal conPhaiTra = b.getTotalAmount().subtract(daThu);

        String trangThai;
        String nhan;
        if (conPhaiTra.signum() <= 0) {
            trangThai = "DA_TRA_DU";
            nhan = "Đã thanh toán đủ";
        } else if (daThu.compareTo(b.getDepositAmount()) >= 0 && daThu.signum() > 0) {
            trangThai = "DA_COC";
            nhan = "Đã đóng cọc, còn lại thanh toán sau tiệc";
        } else if (daThu.signum() > 0) {
            trangThai = "COC_THIEU";
            nhan = "Đã thu một phần, chưa đủ tiền cọc";
        } else {
            trangThai = "CHUA_TRA";
            nhan = "Chưa thanh toán";
        }

        List<Payment> phieu = paymentRepository.findByBookingIdOrderByCreatedAtAsc(b.getId());
        Payment dangCho = phieu.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .reduce((dau, cuoi) -> cuoi)
                .orElse(null);

        return new TinhHinhThanhToanResponse(
                b.getCode(), OrderType.BOOKING.getLabel(), OrderType.BOOKING.getLabelEn(),
                b.getCustomerName(),
                b.getTotalAmount(), b.getDepositAmount(), daThu, conPhaiTra,
                trangThai, nhan,
                dangCho == null ? null : PaymentResponse.from(dangCho),
                phieu.stream().map(PaymentResponse::from).toList(),
                chuyenKhoan(dangCho, b.getCode()));
    }

    private TinhHinhThanhToanResponse tinhHinhCuaMon(DishOrder d) {
        BigDecimal daThu = d.getPaidAmount();
        BigDecimal conPhaiTra = d.getTotal().subtract(daThu);
        boolean daDu = conPhaiTra.signum() <= 0;

        List<Payment> phieu = paymentRepository.findByDishOrderIdOrderByCreatedAtAsc(d.getId());
        Payment dangCho = phieu.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .reduce((dau, cuoi) -> cuoi)
                .orElse(null);

        return new TinhHinhThanhToanResponse(
                d.getCode(), OrderType.DISH_ORDER.getLabel(), OrderType.DISH_ORDER.getLabelEn(),
                d.getCustomerName(),
                d.getTotal(), BigDecimal.ZERO, daThu, conPhaiTra,
                daDu ? "DA_TRA_DU" : "CHUA_TRA",
                daDu ? "Đã thanh toán đủ" : "Chưa thanh toán",
                dangCho == null ? null : PaymentResponse.from(dangCho),
                phieu.stream().map(PaymentResponse::from).toList(),
                chuyenKhoan(dangCho, d.getCode()));
    }

    /*
     * Thông tin chuyển khoản kèm chuỗi VietQR.
     *
     * Không có phiếu chờ thì không dựng mã, vì mã QR mang sẵn số tiền của đúng phiếu đó.
     * Chưa cấu hình tài khoản ngân hàng thì cũng trả null, giao diện tự chuyển sang nhắc
     * khách gọi điện thay vì hiện một mã QR không dùng được.
     */
    private TinhHinhThanhToanResponse.ThongTinChuyenKhoan chuyenKhoan(Payment dangCho, String maDon) {
        if (dangCho == null || !properties.daCauHinh()) {
            return null;
        }
        String noiDung = noiDungChuyenKhoan(maDon);
        String soTaiKhoan = properties.soTaiKhoanHieuLuc();
        return new TinhHinhThanhToanResponse.ThongTinChuyenKhoan(
                properties.bankName(),
                soTaiKhoan,
                properties.tenChuTaiKhoanHieuLuc(),
                dangCho.getAmount(),
                noiDung,
                VietQrBuilder.dungChuoi(properties.bankBin(), soTaiKhoan,
                        dangCho.getAmount(), noiDung),
                properties.dangChayThu());
    }

    /*
     * Tạo giao dịch VNPay và trả về địa chỉ chuyển khách sang cổng.
     *
     * Vẫn ghi một phiếu chờ đối soát như khi chuyển khoản, chỉ khác hình thức. Khác biệt thật
     * sự nằm ở bước sau: VNPay gọi ngược về báo kết quả nên phiếu được xác nhận tự động, không
     * cần ai mở sao kê ra soát.
     */
    @Transactional
    public String taoThanhToanVnPay(String maDon, PaymentPurpose mucDich, String ipKhach) {
        if (!vnPayService.daMoCong()) {
            throw new BusinessException("Chưa mở cổng thanh toán VNPay. "
                    + "Vui lòng chọn chuyển khoản quét mã QR hoặc trả tiền mặt tại nhà hàng.");
        }

        TinhHinhThanhToanResponse tinhHinh = taoYeuCau(maDon, mucDich);
        PaymentResponse phieu = tinhHinh.phieuChoDoiSoat();
        if (phieu == null) {
            throw new BusinessException("Không tạo được phiếu thanh toán cho đơn này");
        }

        // Đánh dấu phiếu đi qua cổng, để lúc đối soát biết khoản này không nằm trong sao kê ngân hàng
        paymentRepository.findById(phieu.id()).ifPresent(p -> p.setMethod(PaymentMethod.VNPAY));

        return vnPayService.dungDuongDanThanhToan(phieu.code(), phieu.amount(),
                "Thanh toan don " + maDon, ipKhach);
    }

    /*
     * Nhận kết quả VNPay trả về sau khi khách thanh toán xong.
     *
     * Kiểm chữ ký trước tiên, trước cả khi đọc bất kỳ tham số nào khác. Tin vào số tiền hay
     * mã phiếu trong tham số rồi mới kiểm chữ ký là bỏ luôn tác dụng bảo vệ: ai cũng sửa được
     * tham số trên thanh địa chỉ.
     */
    @Transactional
    public PaymentResponse xacNhanVnPay(Map<String, String> thamSo) {
        if (!vnPayService.chuKyHopLe(thamSo)) {
            throw new BusinessException("Chữ ký của kết quả thanh toán không hợp lệ");
        }

        String maPhieu = thamSo.get("vnp_TxnRef");
        Payment phieu = paymentRepository.findByCode(maPhieu)
                .orElseThrow(() -> ResourceNotFoundException.of("phiếu thu", maPhieu));

        if (!vnPayService.thanhCong(thamSo)) {
            log.info("VNPay báo giao dịch {} không thành công, mã {}",
                    maPhieu, thamSo.get("vnp_ResponseCode"));
            if (phieu.getStatus() == PaymentStatus.PENDING) {
                phieu.setStatus(PaymentStatus.CANCELLED);
                phieu.setNote("VNPay báo không thành công, mã " + thamSo.get("vnp_ResponseCode"));
            }
            return PaymentResponse.from(phieu);
        }

        return xacNhan(phieu.getId(), "vnpay", thamSo.get("vnp_TransactionNo"));
    }

    /*
     * Giả lập ngân hàng báo có, chỉ chạy được ở chế độ thử.
     *
     * Chạy thật thì tiền về tài khoản mới có người xác nhận, không có đường tắt nào. Nhưng
     * khi trình diễn hoặc khi chạy kiểm thử tự động thì không thể chuyển khoản thật, nên cần
     * một cách đi hết luồng. Hàm này làm đúng việc mà nhân viên đối soát sẽ làm, không hơn.
     */
    @Transactional
    public PaymentResponse giaLapNganHangBaoCo(String maDon) {
        if (!properties.sandbox()) {
            throw new BusinessException("Chức năng giả lập chỉ dùng được ở chế độ chạy thử");
        }

        TinhHinhThanhToanResponse tinhHinh = tinhHinh(maDon);
        if (tinhHinh.phieuChoDoiSoat() == null) {
            throw new BusinessException("Đơn này không có phiếu nào đang chờ đối soát");
        }
        return xacNhan(tinhHinh.phieuChoDoiSoat().id(), "sandbox@vuonsen.vn",
                "GIA LAP " + maDon);
    }

    // ------------------------------------------------------------------ phụ trợ

    private Optional<Booking> timDonTiec(String maDon) {
        return maDon == null ? Optional.empty() : bookingRepository.findByCode(maDon.trim());
    }

    private Optional<DishOrder> timDonMon(String maDon) {
        return maDon == null ? Optional.empty() : dishOrderRepository.findByCode(maDon.trim());
    }

    private BigDecimal soTienCanTra(Booking b, PaymentPurpose mucDich) {
        if (mucDich == PaymentPurpose.DEPOSIT) {
            return b.getDepositAmount().subtract(b.getDepositPaid()).max(BigDecimal.ZERO);
        }
        return b.getTotalAmount().subtract(b.getDepositPaid()).max(BigDecimal.ZERO);
    }

    /** Nội dung chuyển khoản chính là mã đơn, để đối soát sao kê chỉ cần nhìn một dòng. */
    private String noiDungChuyenKhoan(String maDon) {
        return maDon;
    }

    private String sinhMa(String tienTo) {
        String ngay = LocalDate.now().format(NGAY_MA);
        for (int stt = 1; stt < 10000; stt++) {
            String ma = "%s-%s-%04d".formatted(tienTo, ngay, stt);
            if (!paymentRepository.existsByCode(ma)) {
                return ma;
            }
        }
        throw new BusinessException("Đã hết số phiếu thu cho hôm nay");
    }
}
