package vn.vuonsen.fnb.modules.assistant.hanhvi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/*
 * Ghi lại việc khách mở một trang chi tiết.
 *
 * Ghi ở phía máy chủ, ngay trong lời gọi API lấy chi tiết, chứ không để giao diện tự gọi
 * một API riêng để báo "tôi vừa xem". Hai lý do. Một là giao diện quên gọi một chỗ thì
 * chỗ đó mất dữ liệu mà không ai biết. Hai là dữ liệu do trình duyệt tự khai thì ai cũng
 * bơm được số lượt xem giả.
 *
 * Nguyên tắc tuyệt đối: ghi nhật ký hỏng thì trang vẫn phải mở được. Khách vào xem một
 * sảnh mà nhận lỗi 500 chỉ vì không ghi được dòng thống kê là đánh đổi sai hoàn toàn.
 * Vì vậy bắt hết ngoại lệ, và chạy trong giao dịch riêng để nếu có hỏng thì không kéo
 * theo giao dịch đang đọc dữ liệu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GhiNhanHanhViService {

    private final LuotXemRepository luotXemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ghiLuotXem(Long userId, LuotXem.LoaiDoiTuong loai, Long doiTuongId) {
        if (doiTuongId == null) {
            return;
        }
        try {
            luotXemRepository.save(LuotXem.ghi(userId, loai, doiTuongId));
        } catch (RuntimeException e) {
            log.warn("Không ghi được lượt xem {} {}: {}", loai, doiTuongId, e.getMessage());
        }
    }
}
