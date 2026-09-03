package vn.vuonsen.fnb.modules.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.config.props.BookingProperties;
import vn.vuonsen.fnb.config.props.ContactProperties;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishCategoryRepository;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.partypackage.PartyPackage;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.space.Space;
import vn.vuonsen.fnb.modules.space.SpaceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/*
 * Trợ lý tư vấn dịch vụ.
 *
 * Nguyên tắc: mọi câu trả lời dựng từ dữ liệu thật trong cơ sở dữ liệu và tham số
 * trong tệp cấu hình, không viết cứng nội dung. Thêm một không gian hay đổi tỉ lệ
 * đặt cọc thì câu trả lời tự đổi theo, không phải sửa mã nguồn.
 *
 * Đây cũng là điều đề cương yêu cầu: trợ lý hoạt động trong phạm vi dữ liệu của
 * hệ thống. Trợ lý sẽ không bịa ra thông tin không có trong cơ sở dữ liệu, khác
 * với việc gọi thẳng một mô hình ngôn ngữ mà không giới hạn ngữ cảnh.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssistantService {

    private static final int SO_MON_GOI_Y = 3;

    private final IntentDetector detector;
    private final SpaceRepository spaceRepository;
    private final PartyPackageRepository packageRepository;
    private final DishRepository dishRepository;
    private final DishCategoryRepository categoryRepository;
    private final BookingProperties booking;
    private final ContactProperties contact;

    public AnswerResponse answer(String cauHoi) {
        Intent intent = detector.detect(cauHoi);
        return switch (intent) {
            case CHAO_HOI -> chaoHoi();
            case KHONG_GIAN -> traLoiKhongGian(detector.soTrongCau(cauHoi));
            case GOI_TIEC -> traLoiGoiTiec();
            case THUC_DON -> traLoiThucDon();
            case CHI_PHI -> traLoiChiPhi();
            case DAT_COC -> traLoiDatCoc();
            case KHUYEN_MAI -> traLoiKhuyenMai();
            case QUY_TRINH_DAT -> traLoiQuyTrinh();
            case TRA_CUU_DON -> traLoiTraCuu();
            case LIEN_HE -> traLoiLienHe();
            case KHONG_HIEU -> khongHieu();
        };
    }

    // ---------------- Từng loại câu hỏi ----------------

    private AnswerResponse chaoHoi() {
        return new AnswerResponse(
                "Chào bạn, mình là trợ lý của Vườn Sen. Mình giúp được về không gian, "
                        + "thực đơn, gói tiệc, chi phí và cách đặt tiệc. Bạn cần hỏi gì ạ?",
                Intent.CHAO_HOI, goiYMacDinh(), null, null);
    }

    private AnswerResponse traLoiKhongGian(int soKhach) {
        List<Space> danhSach = spaceRepository.findByActiveTrueOrderBySortOrderAsc();

        // Khách có nói số khách thì lọc luôn cho đúng nhu cầu
        if (soKhach > 0) {
            List<Space> vua = danhSach.stream()
                    .filter(s -> soKhach <= s.getCapacityMax())
                    .toList();

            if (vua.isEmpty()) {
                int lonNhat = danhSach.stream().mapToInt(Space::getCapacityMax).max().orElse(0);
                return new AnswerResponse(
                        "Hiện không gian lớn nhất của Vườn Sen chứa được %d khách nên chưa nhận được tiệc %d khách. "
                                .formatted(lonNhat, soKhach)
                                + "Bạn gọi %s để bên mình tư vấn phương án khác nhé.".formatted(contact.phone()),
                        Intent.KHONG_GIAN, goiYMacDinh(), null, null);
            }

            StringBuilder sb = new StringBuilder(
                    "Với %d khách, Vườn Sen có %d không gian phù hợp:\n".formatted(soKhach, vua.size()));
            for (Space s : vua) {
                sb.append("\n• %s: chứa %d đến %d khách, thuê %s một %s"
                        .formatted(s.getName(), s.getCapacityMin(), s.getCapacityMax(),
                                tien(s.getRentalFee()), donViThue(s)));
            }
            sb.append("\n\nTiền ăn đạt mức tối thiểu của sảnh thì được miễn phí thuê không gian.");
            return new AnswerResponse(sb.toString(), Intent.KHONG_GIAN,
                    List.of("Gói tiệc có những gói nào?", "Đặt tiệc cần báo trước bao lâu?", "Đặt cọc bao nhiêu?"),
                    "/khong-gian", "Xem tất cả không gian");
        }

        StringBuilder sb = new StringBuilder("Vườn Sen có %d không gian:\n".formatted(danhSach.size()));
        for (Space s : danhSach) {
            sb.append("\n• %s (%s): %d đến %d khách"
                    .formatted(s.getName(), s.getSpaceType().getLabel(),
                            s.getCapacityMin(), s.getCapacityMax()));
        }
        sb.append("\n\nBạn cho mình biết số khách dự kiến, mình gợi ý sảnh phù hợp nhé.");
        return new AnswerResponse(sb.toString(), Intent.KHONG_GIAN,
                List.of("Sảnh nào chứa được 300 khách?", "Giá thuê không gian bao nhiêu?", "Có gói tiệc nào?"),
                "/khong-gian", "Xem tất cả không gian");
    }

    private AnswerResponse traLoiGoiTiec() {
        List<PartyPackage> goi = packageRepository.findByActiveTrueOrderBySortOrderAsc();
        StringBuilder sb = new StringBuilder("Vườn Sen có %d gói tiệc trọn gói:\n".formatted(goi.size()));
        for (PartyPackage g : goi) {
            sb.append("\n• %s: %s một mâm".formatted(g.getName(), tien(g.getPricePerTable())));
            if (g.getDishCount() != null) {
                sb.append(", %d món".formatted(g.getDishCount()));
            }
            if (g.getHoursIncluded() != null) {
                sb.append(", dùng không gian %d tiếng".formatted(g.getHoursIncluded()));
            }
        }
        sb.append("\n\nMột mâm tính cho %d khách.".formatted(booking.guestsPerTable()));
        return new AnswerResponse(sb.toString(), Intent.GOI_TIEC,
                List.of("Thực đơn có món gì?", "Chi phí một tiệc bao nhiêu?", "Có khuyến mãi gì không?"),
                "/goi-tiec", "Xem chi tiết gói tiệc");
    }

    private AnswerResponse traLoiThucDon() {
        var danhMuc = categoryRepository.findByActiveTrueOrderBySortOrderAsc();
        List<Dish> banChay = dishRepository.findByBestSellerTrueAndAvailableTrueOrderBySortOrderAsc();

        StringBuilder sb = new StringBuilder("Thực đơn Vườn Sen chia %d nhóm: ".formatted(danhMuc.size()));
        sb.append(String.join(", ", danhMuc.stream().map(c -> c.getName().toLowerCase()).toList()));
        sb.append(".");

        if (!banChay.isEmpty()) {
            sb.append("\n\nMón được gọi nhiều nhất:");
            for (Dish d : banChay.stream().limit(SO_MON_GOI_Y).toList()) {
                sb.append("\n• %s".formatted(d.getName()));
                if (d.getPrice() != null) {
                    sb.append(" — %s".formatted(tien(d.getPrice())));
                } else if (d.getPriceNote() != null) {
                    sb.append(" — %s".formatted(d.getPriceNote()));
                }
            }
        }
        sb.append("\n\nGiá trên là giá phần ăn tại nhà hàng. Tiệc theo mâm thì tính theo gói tiệc.");
        return new AnswerResponse(sb.toString(), Intent.THUC_DON,
                List.of("Có gói tiệc nào?", "Sảnh nào chứa được 200 khách?", "Đặt tiệc như thế nào?"),
                "/thuc-don", "Xem thực đơn đầy đủ");
    }

    private AnswerResponse traLoiChiPhi() {
        List<PartyPackage> goi = packageRepository.findByActiveTrueOrderBySortOrderAsc();
        BigDecimal reNhat = goi.stream().map(PartyPackage::getPricePerTable)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal datNhat = goi.stream().map(PartyPackage::getPricePerTable)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        String s = """
                Chi phí một tiệc gồm ba phần:

                1. Tiền ăn: giá một mâm nhân số mâm. Gói tiệc từ %s đến %s một mâm, mỗi mâm %d khách.
                2. Phí thuê không gian: tùy sảnh. Tiền ăn đạt mức tối thiểu của sảnh thì được miễn hoàn toàn, chưa đạt thì giảm theo tỉ lệ.
                3. Thuế giá trị gia tăng %s%%.

                Bạn vào trang đặt tiệc, chọn không gian và gói tiệc là hệ thống hiện bảng tạm tính chi tiết ngay, chưa cần điền thông tin liên hệ."""
                .formatted(tien(reNhat), tien(datNhat), booking.guestsPerTable(),
                        booking.vatRate().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString());

        return new AnswerResponse(s, Intent.CHI_PHI,
                List.of("Đặt cọc bao nhiêu?", "Có khuyến mãi gì không?", "Có gói tiệc nào?"),
                "/dat-tiec", "Xem báo giá cho tiệc của bạn");
    }

    private AnswerResponse traLoiDatCoc() {
        String phanTram = booking.depositRate().multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros().toPlainString();
        return new AnswerResponse(
                "Để giữ ngày, bạn đặt cọc %s%% tổng hóa đơn. Số tiền cọc hiện ngay trong bảng tạm tính "
                        .formatted(phanTram)
                        + "khi bạn chọn xong không gian và gói tiệc, nên biết trước chứ không bị bất ngờ.",
                Intent.DAT_COC,
                List.of("Đặt tiệc như thế nào?", "Chi phí một tiệc bao nhiêu?", "Có khuyến mãi gì không?"),
                "/dat-tiec", "Đặt tiệc");
    }

    private AnswerResponse traLoiKhuyenMai() {
        String giam = booking.earlyBirdRate().multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros().toPlainString();
        String s = """
                Vườn Sen có hai mức ưu đãi:

                • Đặt sớm: đặt trước từ %d ngày được giảm %s%% trên tiền ăn và phí thuê.
                • Miễn phí thuê không gian: tiền ăn đạt mức tối thiểu của sảnh thì miễn hoàn toàn phí thuê. Chưa đạt mức thì vẫn được giảm theo tỉ lệ chứ không mất trọn phí thuê."""
                .formatted(booking.earlyBirdDays(), giam);

        return new AnswerResponse(s, Intent.KHUYEN_MAI,
                List.of("Chi phí một tiệc bao nhiêu?", "Đặt cọc bao nhiêu?", "Đặt tiệc như thế nào?"),
                "/dat-tiec", "Xem báo giá");
    }

    private AnswerResponse traLoiQuyTrinh() {
        String s = """
                Đặt tiệc qua ba bước trên website, không cần tài khoản:

                1. Chọn loại sự kiện, ngày, buổi và số khách.
                2. Chọn không gian và gói tiệc. Hệ thống gợi ý sẵn ba phương án phù hợp và hiện bảng tạm tính.
                3. Điền thông tin liên hệ và gửi đơn.

                Về thời gian báo trước: tiệc thường cần báo trước %d ngày, tiệc từ %d mâm trở lên cần %d ngày vì phải chuẩn bị nhân sự và nguyên liệu. Gửi đơn xong bạn nhận mã đơn để tra cứu, bên mình sẽ liên hệ xác nhận."""
                .formatted(booking.minDaysAhead(), booking.largePartyTables(), booking.largePartyMinDays());

        return new AnswerResponse(s, Intent.QUY_TRINH_DAT,
                List.of("Đặt cọc bao nhiêu?", "Tra cứu đơn ở đâu?", "Sảnh nào chứa được 300 khách?"),
                "/dat-tiec", "Đặt tiệc ngay");
    }

    private AnswerResponse traLoiTraCuu() {
        return new AnswerResponse(
                "Bạn vào trang Tra cứu đơn rồi nhập mã đơn nhận được lúc đặt là xem được trạng thái. "
                        + "Nếu bạn đặt khi đã đăng nhập thì xem ở mục Đơn của tôi, không cần nhớ mã.",
                Intent.TRA_CUU_DON,
                List.of("Đặt tiệc như thế nào?", "Số điện thoại liên hệ?", "Có gói tiệc nào?"),
                "/tra-cuu", "Tra cứu đơn");
    }

    private AnswerResponse traLoiLienHe() {
        String s = """
                Thông tin liên hệ Vườn Sen:

                • Địa chỉ: %s
                • Điện thoại: %s
                • Email: %s
                • Giờ mở cửa: %s"""
                .formatted(contact.address(), contact.phone(), contact.email(), contact.openingHours());

        return new AnswerResponse(s, Intent.LIEN_HE, goiYMacDinh(), null, null);
    }

    private AnswerResponse khongHieu() {
        return new AnswerResponse(
                "Xin lỗi, mình chưa hiểu ý bạn. Mình trả lời được về không gian, thực đơn, gói tiệc, "
                        + "chi phí, đặt cọc, khuyến mãi và cách đặt tiệc. Bạn thử một trong các câu dưới đây, "
                        + "hoặc gọi %s để gặp nhân viên tư vấn.".formatted(contact.phone()),
                Intent.KHONG_HIEU, goiYMacDinh(), null, null);
    }

    // ---------------- Tiện ích ----------------

    private List<String> goiYMacDinh() {
        return List.of(
                "Sảnh nào chứa được 300 khách?",
                "Có gói tiệc nào?",
                "Chi phí một tiệc bao nhiêu?",
                "Đặt tiệc như thế nào?");
    }

    private String donViThue(Space space) {
        return "HUT".equals(space.getFeeUnit()) ? "chòi" : "buổi";
    }

    private String tien(BigDecimal amount) {
        if (amount == null) {
            return "liên hệ";
        }
        DecimalFormatSymbols ky = new DecimalFormatSymbols(Locale.forLanguageTag("vi-VN"));
        ky.setGroupingSeparator('.');
        return new DecimalFormat("#,###", ky).format(amount.setScale(0, RoundingMode.HALF_UP)) + "đ";
    }
}
