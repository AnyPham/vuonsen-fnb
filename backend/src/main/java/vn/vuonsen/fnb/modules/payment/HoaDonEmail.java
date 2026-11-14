package vn.vuonsen.fnb.modules.payment;

import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingDish;
import vn.vuonsen.fnb.modules.dishorder.DishOrder;
import vn.vuonsen.fnb.modules.dishorder.DishOrderItem;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*
 * Dựng thư hóa đơn gửi khách sau khi tiền được ghi nhận: một bản HTML có bảng kê từng khoản,
 * một bản chữ trơn cho hộp thư không hiện được HTML và để ghi vào nhật ký.
 *
 * Tách khỏi PaymentService vì đây thuần là trình bày: nhận phiếu thu, trả về hai chuỗi, không
 * đọc cơ sở dữ liệu, không gửi gì. Nhờ vậy kiểm thử được bằng đối tượng dựng tay.
 *
 * HTML viết theo lối của thư điện tử chứ không theo lối trang web: bố cục bằng bảng, CSS đặt
 * thẳng trên từng thẻ, không dùng lớp hay thẻ style, vì Gmail và Outlook bỏ phần lớn CSS khác.
 */
final class HoaDonEmail {

    record NoiDung(String vanBan, String html) {
    }

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
    private static final String XANH = "#1f3a2e";
    private static final String VANG = "#c9a24d";
    private static final String NEN = "#f6f3ec";

    private HoaDonEmail() {
    }

    static NoiDung dung(Payment phieu, String baseUrl) {
        boolean laTiec = phieu.getOrderType() == OrderType.BOOKING;
        Booking b = phieu.getBooking();
        DishOrder o = phieu.getDishOrder();

        String tenKhach = laTiec ? b.getCustomerName() : o.getCustomerName();
        BigDecimal tongDon = laTiec ? b.getTotalAmount() : o.getTotal();
        BigDecimal daThu = laTiec ? b.getDepositPaid() : o.getPaidAmount();
        BigDecimal conLai = tongDon.subtract(daThu).max(BigDecimal.ZERO);
        String duongTraCuu = laTiec
                ? "%s/tra-cuu?code=%s".formatted(baseUrl, phieu.maDon())
                : "%s/tra-cuu-mon?ma=%s".formatted(baseUrl, phieu.maDon());

        List<String[]> thongTin = laTiec ? thongTinTiec(b) : thongTinMon(o);
        List<String[]> khoanTien = laTiec ? khoanTienTiec(b) : khoanTienMon(o);

        return new NoiDung(
                vanBan(phieu, laTiec, tenKhach, thongTin, khoanTien, tongDon, daThu, conLai, duongTraCuu),
                html(phieu, laTiec, tenKhach, thongTin, khoanTien, tongDon, daThu, conLai, duongTraCuu));
    }

    // ---------------------------------------------------------------- nội dung từng loại đơn

    private static List<String[]> thongTinTiec(Booking b) {
        List<String[]> ds = new ArrayList<>();
        ds.add(new String[] {"Loại tiệc", b.getEventType().getLabel()});
        ds.add(new String[] {"Ngày và buổi", b.getEventDate().format(NGAY) + " · " + b.getTimeSlot().getLabel()});
        ds.add(new String[] {"Không gian", b.getSpace().getName()});
        ds.add(new String[] {"Gói tiệc", b.getPartyPackage() == null ? "Chỉ thuê không gian" : b.getPartyPackage().getName()});
        ds.add(new String[] {"Số khách", b.getTableCount() != null && b.getTableCount() > 0
                ? "%d khách (%d mâm)".formatted(b.getGuestCount(), b.getTableCount())
                : "%d khách".formatted(b.getGuestCount())});
        return ds;
    }

    private static List<String[]> khoanTienTiec(Booking b) {
        List<String[]> ds = new ArrayList<>();
        if (b.getMonTuChon() != null && !b.getMonTuChon().isEmpty()) {
            for (BookingDish m : b.getMonTuChon()) {
                BigDecimal thanhTien = m.getDonGia().multiply(BigDecimal.valueOf(m.getSoLuong()));
                ds.add(new String[] {"%s × %d".formatted(m.getTenMon(), m.getSoLuong()), tien(thanhTien)});
            }
        } else if (b.getTableCount() != null && b.getTableCount() > 0 && b.getUnitPrice() != null) {
            ds.add(new String[] {"Tiền ăn: %d mâm × %s".formatted(b.getTableCount(), tien(b.getUnitPrice())), tien(b.getFoodAmount())});
        } else if (b.getFoodAmount() != null && b.getFoodAmount().signum() > 0) {
            ds.add(new String[] {"Tiền ăn", tien(b.getFoodAmount())});
        }
        ds.add(new String[] {"Phí thuê không gian", tien(b.getSpaceFee())});
        if (b.getDiscountAmount() != null && b.getDiscountAmount().signum() > 0) {
            ds.add(new String[] {"Giảm giá", "− " + tien(b.getDiscountAmount())});
        }
        if (b.getVatAmount() != null && b.getVatAmount().signum() > 0) {
            String tiLe = b.getVatRate() == null ? "" : " " + b.getVatRate().movePointRight(2).stripTrailingZeros().toPlainString() + "%";
            ds.add(new String[] {"Thuế VAT" + tiLe, tien(b.getVatAmount())});
        }
        return ds;
    }

    private static List<String[]> thongTinMon(DishOrder o) {
        List<String[]> ds = new ArrayList<>();
        ds.add(new String[] {"Hình thức nhận", o.getFulfillmentType().getLabel()});
        if (o.getServeAt() != null) {
            ds.add(new String[] {"Thời điểm nhận", o.getServeAt().format(GIO)});
        }
        if (o.getDeliveryAddress() != null && !o.getDeliveryAddress().isBlank()) {
            ds.add(new String[] {"Địa chỉ giao", o.getDeliveryAddress()});
        }
        return ds;
    }

    private static List<String[]> khoanTienMon(DishOrder o) {
        List<String[]> ds = new ArrayList<>();
        for (DishOrderItem m : o.getItems()) {
            ds.add(new String[] {"%s × %d".formatted(m.getDishName(), m.getQuantity()), tien(m.getLineTotal())});
        }
        if (o.getDiscountAmount() != null && o.getDiscountAmount().signum() > 0) {
            ds.add(new String[] {"Giảm giá", "− " + tien(o.getDiscountAmount())});
        }
        if (o.getDeliveryFee() != null && o.getDeliveryFee().signum() > 0) {
            ds.add(new String[] {"Phí giao hàng", tien(o.getDeliveryFee())});
        }
        if (o.getVatAmount() != null && o.getVatAmount().signum() > 0) {
            ds.add(new String[] {"Thuế VAT", tien(o.getVatAmount())});
        }
        return ds;
    }

    // ---------------------------------------------------------------- hai bản thư

    private static String vanBan(Payment phieu, boolean laTiec, String tenKhach, List<String[]> thongTin,
                                 List<String[]> khoanTien, BigDecimal tongDon, BigDecimal daThu,
                                 BigDecimal conLai, String duongTraCuu) {
        StringBuilder s = new StringBuilder();
        s.append("Chào ").append(tenKhach).append(",\n\n")
                .append("Vườn Sen đã nhận được thanh toán của bạn. Thư này là xác nhận đã thu tiền,\n")
                .append("không phải hóa đơn giá trị gia tăng.\n\n")
                .append("Mã đơn: ").append(phieu.maDon()).append(" (").append(laTiec ? "đơn đặt tiệc" : "đơn đặt món lẻ").append(")\n")
                .append("Mã phiếu thu: ").append(phieu.getCode()).append('\n')
                .append("Hình thức: ").append(phieu.getMethod().getLabel()).append('\n')
                .append("Số tiền lần này: ").append(tien(phieu.getAmount())).append('\n')
                .append("Thời điểm: ").append(phieu.getConfirmedAt().format(GIO)).append("\n\n");
        thongTin.forEach(d -> s.append(d[0]).append(": ").append(d[1]).append('\n'));
        s.append('\n');
        khoanTien.forEach(d -> s.append(d[0]).append(": ").append(d[1]).append('\n'));
        s.append("\nTổng tiền đơn: ").append(tien(tongDon)).append('\n')
                .append("Đã thanh toán: ").append(tien(daThu)).append('\n')
                .append("Còn phải trả: ").append(tien(conLai)).append("\n\n")
                .append("Tra cứu đơn bất cứ lúc nào tại: ").append(duongTraCuu).append('\n')
                .append("Đã đăng nhập thì xem ở mục Đơn của tôi.\n\n")
                .append("Vườn Sen\n");
        return s.toString();
    }

    private static String html(Payment phieu, boolean laTiec, String tenKhach, List<String[]> thongTin,
                               List<String[]> khoanTien, BigDecimal tongDon, BigDecimal daThu,
                               BigDecimal conLai, String duongTraCuu) {
        StringBuilder h = new StringBuilder();
        h.append("<!doctype html><html lang=\"vi\"><head><meta charset=\"utf-8\"></head>")
                .append("<body style=\"margin:0;padding:0;background:").append(NEN).append(";\">")
                .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:").append(NEN).append(";padding:24px 0;\"><tr><td align=\"center\">")
                .append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background:#ffffff;border-radius:10px;overflow:hidden;font-family:Arial,Helvetica,sans-serif;color:#1f2937;\">");

        // Đầu thư
        h.append("<tr><td style=\"background:").append(XANH).append(";padding:22px 28px;\">")
                .append("<div style=\"font-size:22px;font-weight:bold;color:").append(VANG).append(";letter-spacing:1px;\">VƯỜN SEN</div>")
                .append("<div style=\"font-size:13px;color:#d1d5db;margin-top:4px;\">Khu ẩm thực và tiệc sân vườn</div></td></tr>");

        // Lời chào và số tiền
        h.append("<tr><td style=\"padding:26px 28px 8px;\">")
                .append("<div style=\"font-size:20px;font-weight:bold;color:").append(XANH).append(";\">Đã nhận thanh toán</div>")
                .append("<p style=\"font-size:15px;line-height:1.6;margin:10px 0 0;\">Chào ").append(e(tenKhach))
                .append(", Vườn Sen đã nhận được thanh toán của bạn cho ").append(laTiec ? "đơn đặt tiệc" : "đơn đặt món lẻ")
                .append(" <b>").append(e(phieu.maDon())).append("</b>.</p></td></tr>");

        h.append("<tr><td style=\"padding:14px 28px;\"><table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:").append(NEN).append(";border-radius:8px;\">")
                .append("<tr><td style=\"padding:16px 18px;font-size:13px;color:#6b7280;\">Số tiền lần này</td>")
                .append("<td align=\"right\" style=\"padding:16px 18px;font-size:22px;font-weight:bold;color:").append(XANH).append(";\">").append(tien(phieu.getAmount())).append("</td></tr>")
                .append(dongNho("Mã phiếu thu", phieu.getCode()))
                .append(dongNho("Hình thức", phieu.getMethod().getLabel()))
                .append(dongNho("Thời điểm", phieu.getConfirmedAt().format(GIO)))
                .append("<tr><td colspan=\"2\" style=\"height:10px;\"></td></tr></table></td></tr>");

        // Thông tin đơn
        h.append(tieuDeMuc("Thông tin đơn"));
        h.append("<tr><td style=\"padding:0 28px;\"><table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:14px;\">");
        for (String[] d : thongTin) {
            h.append("<tr><td style=\"padding:6px 0;color:#6b7280;width:40%;\">").append(e(d[0]))
                    .append("</td><td style=\"padding:6px 0;\">").append(e(d[1])).append("</td></tr>");
        }
        h.append("</table></td></tr>");

        // Bảng kê
        h.append(tieuDeMuc("Chi tiết chi phí"));
        h.append("<tr><td style=\"padding:0 28px;\"><table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"font-size:14px;border-collapse:collapse;\">");
        for (String[] d : khoanTien) {
            h.append("<tr><td style=\"padding:8px 0;border-bottom:1px solid #eee;\">").append(e(d[0]))
                    .append("</td><td align=\"right\" style=\"padding:8px 0;border-bottom:1px solid #eee;white-space:nowrap;\">").append(e(d[1])).append("</td></tr>");
        }
        h.append("<tr><td style=\"padding:12px 0 6px;font-weight:bold;\">Tổng tiền đơn</td><td align=\"right\" style=\"padding:12px 0 6px;font-weight:bold;\">").append(tien(tongDon)).append("</td></tr>")
                .append("<tr><td style=\"padding:6px 0;color:#15803d;\">Đã thanh toán</td><td align=\"right\" style=\"padding:6px 0;color:#15803d;\">").append(tien(daThu)).append("</td></tr>")
                .append("<tr><td style=\"padding:6px 0;font-weight:bold;color:").append(conLai.signum() > 0 ? "#b45309" : XANH).append(";\">Còn phải trả</td>")
                .append("<td align=\"right\" style=\"padding:6px 0;font-weight:bold;color:").append(conLai.signum() > 0 ? "#b45309" : XANH).append(";\">").append(tien(conLai)).append("</td></tr>")
                .append("</table></td></tr>");

        // Nút tra cứu và chân thư
        h.append("<tr><td align=\"center\" style=\"padding:26px 28px 8px;\">")
                .append("<a href=\"").append(e(duongTraCuu)).append("\" style=\"display:inline-block;background:").append(XANH)
                .append(";color:#ffffff;text-decoration:none;padding:12px 26px;border-radius:22px;font-size:14px;font-weight:bold;\">Tra cứu đơn</a>")
                .append("<div style=\"font-size:12px;color:#6b7280;margin-top:10px;\">Đã đăng nhập thì xem ở mục Đơn của tôi.</div></td></tr>")
                .append("<tr><td style=\"padding:18px 28px 24px;font-size:12px;line-height:1.6;color:#9ca3af;border-top:1px solid #eee;\">")
                .append("Thư này xác nhận Vườn Sen đã thu tiền, không phải hóa đơn giá trị gia tăng. ")
                .append("Thư được gửi tự động, vui lòng không trả lời thư này.</td></tr>");

        h.append("</table></td></tr></table></body></html>");
        return h.toString();
    }

    private static String tieuDeMuc(String chu) {
        return "<tr><td style=\"padding:20px 28px 6px;font-size:13px;font-weight:bold;letter-spacing:.5px;color:" + VANG
                + ";text-transform:uppercase;\">" + e(chu) + "</td></tr>";
    }

    private static String dongNho(String nhan, String giaTri) {
        return "<tr><td style=\"padding:3px 18px;font-size:13px;color:#6b7280;\">" + e(nhan)
                + "</td><td align=\"right\" style=\"padding:3px 18px;font-size:13px;\">" + e(giaTri) + "</td></tr>";
    }

    // ---------------------------------------------------------------- tiện ích

    /* 1250000 thành "1.250.000 đ", dấu chấm ngăn hàng nghìn như cách viết tiền ở Việt Nam. */
    static String tien(BigDecimal so) {
        DecimalFormatSymbols ky = new DecimalFormatSymbols();
        ky.setGroupingSeparator('.');
        ky.setDecimalSeparator(',');
        return new DecimalFormat("#,##0", ky).format(so == null ? BigDecimal.ZERO : so) + " đ";
    }

    /* Tên khách, tên món là chữ người dùng nhập, phải thoát ký tự HTML trước khi đặt vào thư. */
    static String e(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
