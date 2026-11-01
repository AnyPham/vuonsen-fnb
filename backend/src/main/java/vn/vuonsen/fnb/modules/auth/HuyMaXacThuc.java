package vn.vuonsen.fnb.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

/*
 * Hủy mã xác nhận trong một giao dịch riêng.
 *
 * Phải tách thành lớp riêng vì hai lẽ.
 *
 * Một, việc hủy mã xảy ra ngay trước lúc ném lỗi báo "nhập sai quá nhiều lần". Nằm chung
 * giao dịch với lời gọi thì ngoại lệ cuộn ngược cả lệnh hủy, mã cũ vẫn còn nguyên và chốt
 * chặn dò mã thành vô dụng. REQUIRES_NEW cho lệnh hủy tự cam kết, không phụ thuộc kết cục
 * của giao dịch bao ngoài.
 *
 * Hai, Spring chỉ áp @Transactional khi lời gọi đi qua proxy. Để hàm này nằm ngay trong
 * XacThucEmailService rồi tự gọi chính mình thì chú thích bị bỏ qua lặng lẽ, mà nhìn mã
 * vẫn tưởng là có hiệu lực.
 */
@Component
@RequiredArgsConstructor
class HuyMaXacThuc {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void thucHien(Long userId) {
        userRepository.findById(userId).ifPresent((User u) -> {
            u.setVerificationCode(null);
            u.setVerificationExpiresAt(null);
            userRepository.save(u);
        });
    }
}
