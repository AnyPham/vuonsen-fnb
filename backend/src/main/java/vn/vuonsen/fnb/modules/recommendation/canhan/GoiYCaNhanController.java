package vn.vuonsen.fnb.modules.recommendation.canhan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.recommendation.canhan.SoThichGoiY.KieuKhongGian;
import vn.vuonsen.fnb.modules.recommendation.canhan.SoThichGoiY.TamGia;
import vn.vuonsen.fnb.security.AppUserDetails;

import java.util.Set;

/*
 * Gợi ý thông minh trên trang chủ và sở thích khách tự khai.
 *
 * GET /api/v1/recommendations/ca-nhan mở cho cả khách chưa đăng nhập, khi đó trả sảnh và món
 * nổi bật. Khách đã đăng nhập thì bộ lọc JWT gắn danh tính trước khi tới đây, nên cùng một
 * đường dẫn trả bản riêng cho từng người.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "12. Gợi ý")
public class GoiYCaNhanController {

    private final GoiYCaNhanService goiYCaNhanService;
    private final SoThichKhachRepository soThichRepository;

    public record SoThichRequest(
            Set<EventType> loaiTiec,
            @Min(value = 1, message = "Số khách phải lớn hơn 0")
            @Max(value = 2000, message = "Số khách tối đa 2000") Integer soKhach,
            KieuKhongGian kieuKhongGian,
            TamGia tamGia,
            boolean anChay
    ) {
    }

    @GetMapping("/api/v1/recommendations/ca-nhan")
    @Operation(summary = "Sảnh và món gợi ý cho trang chủ: theo hồ sơ nếu có, nếu không thì nổi bật")
    public ResponseEntity<GoiYCaNhanResponse> goiY(@AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(goiYCaNhanService.goiY(principal == null ? null : principal.getUserId()));
    }

    @GetMapping("/api/v1/me/so-thich")
    @Operation(summary = "Sở thích đã khai của tài khoản đang đăng nhập")
    public ResponseEntity<SoThichGoiY> laySoThich(@AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(soThichRepository.findByUserId(principal.getUserId())
                .map(SoThichKhach::choGoiY)
                .orElse(new SoThichGoiY(Set.of(), null, null, null, false)));
    }

    @PutMapping("/api/v1/me/so-thich")
    @Transactional
    @Operation(summary = "Lưu sở thích để trang chủ gợi ý sát hơn")
    public ResponseEntity<SoThichGoiY> luuSoThich(@AuthenticationPrincipal AppUserDetails principal,
                                                  @Valid @RequestBody SoThichRequest request) {
        SoThichKhach st = soThichRepository.findByUserId(principal.getUserId())
                .orElseGet(() -> SoThichKhach.builder().userId(principal.getUserId()).build());
        st.datLoaiTiec(request.loaiTiec());
        st.setSoKhach(request.soKhach());
        st.setKieuKhongGian(request.kieuKhongGian());
        st.setTamGia(request.tamGia());
        st.setAnChay(request.anChay());
        return ResponseEntity.ok(soThichRepository.save(st).choGoiY());
    }
}
