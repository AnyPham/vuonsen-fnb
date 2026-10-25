package vn.vuonsen.fnb.modules.assistant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.menu.Dish;
import vn.vuonsen.fnb.modules.menu.DishRepository;
import vn.vuonsen.fnb.modules.payment.PaymentRepository;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Dựng hồ sơ khách cho trợ lý.
 *
 * Toàn bộ dữ liệu đọc tươi từ cơ sở dữ liệu mỗi lần gọi, không giữ bản sao trong bộ nhớ.
 * Lý do là bài toán đồng bộ: khách vừa thanh toán xong ở tab khác rồi quay lại mở hộp
 * thoại thì phải thấy ngay đơn vừa trả tiền. Giữ bộ nhớ đệm ở đây tiết kiệm được vài mili
 * giây nhưng đổi lại là trợ lý nói sai lịch sử của chính khách, không đáng.
 *
 * Danh tính luôn lấy từ userId mà tầng bảo mật giải ra khỏi thẻ truy cập, không bao giờ
 * lấy từ dữ liệu trình duyệt gửi lên. Nếu nhận userId theo lời trình duyệt thì sửa một con
 * số là đọc được lịch sử đặt tiệc của người khác.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HoSoKhachService {

    /* Liệt kê quá nhiều sảnh thì lời chào thành bảng kê. Ba cái là đủ để khách nhận ra. */
    private static final int SO_SANH_TOI_DA = 3;

    /* Liệt kê ba món là đủ gợi nhớ; nhiều hơn thì lời chào thành thực đơn. */
    private static final int SO_MON_TOI_DA = 3;

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;

    @Transactional(readOnly = true)
    public HoSoKhach layHoSo(Long userId) {
        if (userId == null) {
            return HoSoKhach.khachLa();
        }
        User khach = userRepository.findById(userId).orElse(null);
        if (khach == null) {
            /* Thẻ còn hạn nhưng tài khoản đã bị xóa. Hiếm, nhưng xảy ra thì coi như khách lạ. */
            log.warn("Trợ lý: không tìm thấy tài khoản {} tuy thẻ còn hợp lệ", userId);
            return HoSoKhach.khachLa();
        }

        List<Booking> daTra = paymentRepository.timDonTiecDaThanhToan(userId);
        return new HoSoKhach(true, tenGoi(khach), daTra.size(),
                gomTheoSanh(daTra), gomTheoMon(userId));
    }

    /*
     * Gộp món khách đã gọi từ hai nguồn: đơn đặt món lẻ và thực đơn tự chọn của tiệc.
     *
     * Cộng dồn số phần khi một món xuất hiện ở cả hai nguồn. Khách gọi Chả cá hai lần
     * trong đơn món và một lần trong tiệc thì với việc gợi ý, điều đáng quan tâm là ba
     * lần, không phải chuyện chia ra hai đường.
     */
    private List<HoSoKhach.MonDaGoi> gomTheoMon(Long userId) {
        Map<Long, Integer> soPhan = new LinkedHashMap<>();
        for (List<Object[]> nguon : List.of(
                paymentRepository.monDaGoiTrongDonMon(userId),
                paymentRepository.monDaGoiTrongDonTiec(userId))) {
            for (Object[] d : nguon) {
                if (d[0] == null || d[1] == null) {
                    continue;
                }
                Long id = ((Number) d[0]).longValue();
                soPhan.merge(id, ((Number) d[1]).intValue(), Integer::sum);
            }
        }
        if (soPhan.isEmpty()) {
            return List.of();
        }

        List<Dish> cacMon = dishRepository.findAllById(soPhan.keySet());
        return cacMon.stream()
                .sorted(Comparator.comparing(
                        (Dish m) -> soPhan.getOrDefault(m.getId(), 0)).reversed())
                .limit(SO_MON_TOI_DA)
                .map(m -> new HoSoKhach.MonDaGoi(
                        m.getId(),
                        m.getName(),
                        m.getCategory() == null ? null : m.getCategory().getId(),
                        soPhan.getOrDefault(m.getId(), 0)))
                .toList();
    }

    /*
     * Gom các đơn lại theo không gian.
     *
     * Khách đặt Sảnh Sen ba lần thì lời chào nhắc một lần kèm số lần, chứ không kể ra ba
     * dòng giống nhau. Giữ thứ tự theo lần đặt gần nhất, vì nơi khách vừa dùng mới là nơi
     * họ dễ đặt lại nhất.
     */
    private List<HoSoKhach.SanhDaDat> gomTheoSanh(List<Booking> cacDon) {
        Map<Long, List<Booking>> theoSanh = new LinkedHashMap<>();
        for (Booking don : cacDon) {
            if (don.getSpace() == null) {
                continue;
            }
            theoSanh.computeIfAbsent(don.getSpace().getId(), k -> new ArrayList<>()).add(don);
        }

        List<HoSoKhach.SanhDaDat> ketQua = new ArrayList<>();
        for (List<Booking> nhom : theoSanh.values()) {
            LocalDate ganNhat = nhom.stream()
                    .map(Booking::getEventDate)
                    .filter(java.util.Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            ketQua.add(HoSoKhach.sanh(nhom.get(0).getSpace(), ganNhat, nhom.size()));
        }

        ketQua.sort(Comparator.comparing(
                HoSoKhach.SanhDaDat::lanGanNhat,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return ketQua.size() > SO_SANH_TOI_DA ? ketQua.subList(0, SO_SANH_TOI_DA) : ketQua;
    }

    /*
     * Lấy tên riêng để chào cho tự nhiên.
     *
     * Họ tên người Việt đặt họ trước tên sau, nên tên riêng là từ cuối. Chào "Chào anh
     * Nguyễn" thì sai, phải là tên cuối. Tên chỉ có một từ thì dùng nguyên.
     */
    private String tenGoi(User khach) {
        String hoTen = khach.getFullName();
        if (hoTen == null || hoTen.isBlank()) {
            return null;
        }
        String[] phan = hoTen.trim().split("\\s+");
        return phan[phan.length - 1];
    }
}
