package vn.vuonsen.fnb.modules.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.vuonsen.fnb.common.exception.BusinessException;
import vn.vuonsen.fnb.modules.auth.dto.RegisterRequest;
import vn.vuonsen.fnb.modules.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Hai người đăng ký cùng một email cùng lúc.
 *
 * Cả hai cùng qua bước kiểm tra "email đã có chưa" vì chưa bên nào kịp lưu, và database
 * chặn người sau bằng ràng buộc UNIQUE. Bài này dựng lại đúng khoảnh khắc đó: lúc kiểm
 * tra thì báo chưa có, nhưng lúc lưu thì database từ chối.
 *
 * Giả lập bằng mock thay vì chạy hai luồng thật, để lần nào chạy cũng rơi đúng vào
 * khoảnh khắc chen ngang, kết quả lặp lại được.
 */
class DangKyTrungEmailTest {

    @Test
    @DisplayName("Email bị người khác đăng ký chen ngang: báo email đã được đăng ký, không lỗi 500")
    void chenNgangBaoLoiDeHieu() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("mat-khau-da-ma-hoa");

        // Lúc kiểm tra: chưa ai dùng email này
        when(userRepository.existsByEmailIgnoreCase("khach@vuonsen.vn")).thenReturn(false);
        // Lúc lưu: người kia vừa lưu xong trước một nhịp, database từ chối
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException(
                "trùng", new RuntimeException("Duplicate entry 'khach@vuonsen.vn' for key 'uk_users_email'")));

        AuthService service = new AuthService(userRepository, null, passwordEncoder, null, null, null);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("Tuấn Anh", "khach@vuonsen.vn", "0901234567", "matkhau123")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email này đã được đăng ký");
    }
}
