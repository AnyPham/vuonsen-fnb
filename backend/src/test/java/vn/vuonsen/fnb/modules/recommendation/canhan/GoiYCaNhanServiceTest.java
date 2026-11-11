package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.modules.assistant.hanhvi.GhiNhanHanhViService;
import vn.vuonsen.fnb.modules.assistant.hanhvi.LuotXem;
import vn.vuonsen.fnb.modules.booking.Booking;
import vn.vuonsen.fnb.modules.booking.BookingRepository;
import vn.vuonsen.fnb.modules.booking.BookingService;
import vn.vuonsen.fnb.modules.booking.BookingStatus;
import vn.vuonsen.fnb.modules.booking.EventType;
import vn.vuonsen.fnb.modules.booking.TimeSlot;
import vn.vuonsen.fnb.modules.booking.dto.BookingRequest;
import vn.vuonsen.fnb.modules.partypackage.PartyPackageRepository;
import vn.vuonsen.fnb.modules.recommendation.canhan.GoiYCaNhanResponse.LyDo;
import vn.vuonsen.fnb.modules.recommendation.canhan.GoiYCaNhanResponse.SanhGoiY;
import vn.vuonsen.fnb.modules.space.SpaceRepository;
import vn.vuonsen.fnb.modules.user.Role;
import vn.vuonsen.fnb.modules.user.User;
import vn.vuonsen.fnb.modules.user.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm gợi ý thông minh trên dữ liệu mẫu thật (H2 dựng bằng đúng các migration của website).
 *
 * Mỗi bài tự tạo một tài khoản mới để biết chắc hồ sơ của tài khoản đó gồm những gì, không
 * phụ thuộc vào đơn mà bài kiểm thử khác có thể đã tạo cho tài khoản mẫu. Đơn tạo ra được
 * chuyển sang trạng thái hủy khi xong bài, để không làm lệch thứ hạng "nổi bật" của bài khác.
 */
@SpringBootTest
class GoiYCaNhanServiceTest {

    private static final AtomicInteger DEM = new AtomicInteger();

    @Autowired
    private GoiYCaNhanService goiYService;
    @Autowired
    private GoiYCaNhanRepository goiYRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SoThichKhachRepository soThichRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private PartyPackageRepository packageRepository;
    @Autowired
    private GhiNhanHanhViService ghiNhanHanhVi;

    private final List<Long> donDaTao = new ArrayList<>();

    @BeforeEach
    void lamMoi() {
        goiYService.lamMoi();
    }

    @AfterEach
    void huyDonDaTao() {
        for (Long id : donDaTao) {
            Booking don = bookingRepository.findById(id).orElseThrow();
            don.setStatus(BookingStatus.CANCELLED);
            don.setGiuChoDen(null);
            bookingRepository.save(don);
        }
        goiYService.lamMoi();
    }

    // ---------------------------------------------------------------- khách chưa có thông tin

    @Test
    @DisplayName("Khách chưa đăng nhập thấy sảnh nổi bật, xếp theo số tiệc giảm dần")
    void khachChuaDangNhapThaySanhNoiBat() {
        GoiYCaNhanResponse kq = goiYService.goiY(null);

        assertThat(kq.khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.NOI_BAT);
        assertThat(kq.khongGian().items()).hasSize(3);
        List<Long> soTiec = kq.khongGian().items().stream()
                .map(s -> s.lyDo().isEmpty() ? 0L : Long.parseLong(s.lyDo().get(0).giaTri()))
                .toList();
        assertThat(soTiec).isSortedAccordingTo((a, b) -> Long.compare(b, a));

        assertThat(kq.monAn().kieu()).isEqualTo(GoiYCaNhanResponse.NOI_BAT);
        assertThat(kq.monAn().items()).hasSize(3);
    }

    @Test
    @DisplayName("Đã đăng nhập nhưng chưa đặt, chưa xem, chưa khai gì thì vẫn là khách mới")
    void taiKhoanMoiVanLaKhachMoi() {
        Long khach = taoKhach();
        GoiYCaNhanResponse kq = goiYService.goiY(khach);
        assertThat(kq.khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.NOI_BAT);
        assertThat(kq.monAn().kieu()).isEqualTo(GoiYCaNhanResponse.NOI_BAT);
    }

    // ---------------------------------------------------------------- sở thích tự khai

    @Test
    @DisplayName("Khai tiệc cưới 250 khách, tầm cao cấp: hai sảnh lớn cao cấp đứng đầu")
    void soThichTiecCuoiLon() {
        Long khach = taoKhach();
        khaiSoThich(khach, Set.of(EventType.WEDDING), 250, null, SoThichGoiY.TamGia.CAO_CAP, false);

        GoiYCaNhanResponse kq = goiYService.goiY(khach);

        assertThat(kq.khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.CA_NHAN);
        assertThat(kq.khongGian().canCu().coSoThich()).isTrue();
        assertThat(kq.khongGian().items().subList(0, 2)).extracting(SanhGoiY::slug)
                .containsExactlyInAnyOrder("sanh-sen-vang", "sanh-ven-song");
        assertThat(kq.khongGian().items().get(0).lyDo()).extracting(LyDo::ma).contains("LOAI_TIEC");
    }

    @Test
    @DisplayName("Khai họp mặt gia đình 30 khách, tiết kiệm: Nhà Rường Gỗ và Cụm Chòi Sen đứng đầu")
    void soThichGiaDinhNho() {
        Long khach = taoKhach();
        khaiSoThich(khach, Set.of(EventType.FAMILY), 30, null, SoThichGoiY.TamGia.TIET_KIEM, false);

        GoiYCaNhanResponse kq = goiYService.goiY(khach);

        assertThat(kq.khongGian().items().subList(0, 2)).extracting(SanhGoiY::slug)
                .containsExactlyInAnyOrder("nha-ruong-go", "cum-choi-sen");
    }

    @Test
    @DisplayName("Khai ăn chay thì phần món gợi ý toàn món chay")
    void soThichAnChay() {
        Long khach = taoKhach();
        khaiSoThich(khach, Set.of(), null, null, null, true);

        GoiYCaNhanResponse kq = goiYService.goiY(khach);

        assertThat(kq.monAn().kieu()).isEqualTo(GoiYCaNhanResponse.CA_NHAN);
        assertThat(kq.monAn().items()).isNotEmpty()
                .allMatch(GoiYCaNhanResponse.MonGoiY::vegetarian);
        // Chỉ khai ăn chay, không khai gì về tiệc, nên phần sảnh vẫn là nổi bật
        assertThat(kq.khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.NOI_BAT);
    }

    // ---------------------------------------------------------------- lịch sử đặt tiệc

    @Test
    @DisplayName("Hai lần đặt họp mặt gia đình ở Nhà Rường Gỗ: gợi ý riêng, ghi rõ đã đặt hai lần")
    void lichSuDatTiec() {
        Long khach = taoKhach();
        datTiec(khach, "nha-ruong-go", EventType.FAMILY, 40);
        datTiec(khach, "nha-ruong-go", EventType.FAMILY, 45);

        GoiYCaNhanResponse kq = goiYService.goiY(khach);

        assertThat(kq.khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.CA_NHAN);
        assertThat(kq.khongGian().canCu().soTiec()).isEqualTo(2);
        SanhGoiY nhaRuong = kq.khongGian().items().stream()
                .filter(s -> s.slug().equals("nha-ruong-go")).findFirst()
                .orElseThrow(() -> new AssertionError("Sảnh khách đã đặt hai lần phải nằm trong gợi ý"));
        assertThat(nhaRuong.daDat()).isEqualTo(2);
        assertThat(nhaRuong.lyDo()).contains(LyDo.of("LOAI_TIEC", "FAMILY"));
        // Sảnh tiệc cưới 300 khách trở lên không hợp với hồ sơ này
        assertThat(kq.khongGian().items()).extracting(SanhGoiY::slug).doesNotContain("sanh-ven-song");
    }

    @Test
    @DisplayName("Điểm nội dung của sảnh đã đặt cao hơn sảnh không liên quan")
    void diemNoiDungPhanBietDuoc() {
        Long khach = taoKhach();
        datTiec(khach, "nha-ruong-go", EventType.FAMILY, 40);

        List<BoGoiYKhongGian.KetQua> xepHang = goiYService.xepHangSanh(khach);
        Long nhaRuong = spaceRepository.findBySlug("nha-ruong-go").orElseThrow().getId();
        Long venSong = spaceRepository.findBySlug("sanh-ven-song").orElseThrow().getId();

        double diemNhaRuong = xepHang.stream().filter(k -> k.sanhId().equals(nhaRuong)).findFirst().orElseThrow().diemNoiDung();
        double diemVenSong = xepHang.stream().filter(k -> k.sanhId().equals(venSong)).findFirst().orElseThrow().diemNoiDung();
        assertThat(diemNhaRuong).isGreaterThan(diemVenSong + 0.3);
    }

    @Test
    @DisplayName("Đơn đã hủy không tính vào hồ sơ")
    void donHuyKhongTinh() {
        Long khach = taoKhach();
        Long don = datTiec(khach, "nha-ruong-go", EventType.FAMILY, 40);
        Booking b = bookingRepository.findById(don).orElseThrow();
        b.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(b);

        assertThat(goiYService.goiY(khach).khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.NOI_BAT);
    }

    // ---------------------------------------------------------------- lượt xem

    @Test
    @DisplayName("Xem một sảnh hai lần là có hồ sơ: sảnh đó đứng đầu, kèm lý do đã xem")
    void xemHaiLanThanhHoSo() {
        Long khach = taoKhach();
        Long vuonCau = spaceRepository.findBySlug("vuon-cau").orElseThrow().getId();
        ghiNhanHanhVi.ghiLuotXem(khach, LuotXem.LoaiDoiTuong.KHONG_GIAN, vuonCau);
        ghiNhanHanhVi.ghiLuotXem(khach, LuotXem.LoaiDoiTuong.KHONG_GIAN, vuonCau);

        GoiYCaNhanResponse kq = goiYService.goiY(khach);

        assertThat(kq.khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.CA_NHAN);
        assertThat(kq.khongGian().canCu().soSanhDaXem()).isEqualTo(1);
        assertThat(kq.khongGian().items().get(0).slug()).isEqualTo("vuon-cau");
        assertThat(kq.khongGian().items().get(0).lyDo()).contains(LyDo.of("DA_XEM", 2L));
    }

    @Test
    @DisplayName("Xem một lần chưa tính là quan tâm, khách vẫn nhận sảnh nổi bật")
    void xemMotLanChuaTinh() {
        Long khach = taoKhach();
        Long vuonCau = spaceRepository.findBySlug("vuon-cau").orElseThrow().getId();
        ghiNhanHanhVi.ghiLuotXem(khach, LuotXem.LoaiDoiTuong.KHONG_GIAN, vuonCau);

        assertThat(goiYService.goiY(khach).khongGian().kieu()).isEqualTo(GoiYCaNhanResponse.NOI_BAT);
    }

    // ---------------------------------------------------------------- món

    @Test
    @DisplayName("Khách đã gọi món: gợi ý món theo khẩu vị và không lặp lại món đã gọi")
    void monTheoLichSuKhongLapLai() {
        Long khach = userRepository.findByEmailIgnoreCase("quoc.bao@example.com").orElseThrow().getId();
        List<Long> daGoi = goiYRepository.monDaGoiTrongDonMonCua(khach).stream()
                .map(d -> (Long) d[1]).toList();
        assertThat(daGoi).as("dữ liệu mẫu V15 có đơn món của tài khoản này").isNotEmpty();

        GoiYCaNhanResponse kq = goiYService.goiY(khach);

        assertThat(kq.monAn().kieu()).isEqualTo(GoiYCaNhanResponse.CA_NHAN);
        assertThat(kq.monAn().items()).hasSize(3)
                .extracting(GoiYCaNhanResponse.MonGoiY::id).doesNotContainAnyElementsOf(daGoi);
        assertThat(kq.monAn().items()).allMatch(m -> !m.lyDo().isEmpty());
    }

    // ---------------------------------------------------------------- tiện ích

    private Long taoKhach() {
        String email = "goiy." + System.nanoTime() + DEM.incrementAndGet() + "@example.com";
        return userRepository.save(User.builder()
                .fullName("Khách thử gợi ý")
                .email(email)
                .passwordHash("khong-dang-nhap-duoc")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build()).getId();
    }

    private void khaiSoThich(Long khach, Set<EventType> loai, Integer soKhach,
                             SoThichGoiY.KieuKhongGian kieu, SoThichGoiY.TamGia tamGia, boolean anChay) {
        SoThichKhach st = SoThichKhach.builder().userId(khach).soKhach(soKhach)
                .kieuKhongGian(kieu).tamGia(tamGia).anChay(anChay).build();
        st.datLoaiTiec(loai);
        soThichRepository.save(st);
    }

    /* Đặt một tiệc qua đúng BookingService của website, vào ngày xa và ngẫu nhiên để khỏi trùng lịch. */
    private Long datTiec(Long khach, String slugSanh, EventType loai, int soKhach) {
        Long sanh = spaceRepository.findBySlug(slugSanh).orElseThrow().getId();
        Long goi = packageRepository.findAll().stream()
                .filter(g -> "DONG-QUE".equals(g.getCode()))
                .findFirst().orElseThrow().getId();
        LocalDate ngay = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(500, 3000));
        TimeSlot buoi = TimeSlot.values()[ThreadLocalRandom.current().nextInt(TimeSlot.values().length)];
        Long id = bookingService.create(new BookingRequest(loai, ngay, buoi, soKhach, sanh, goi, null,
                "Khách thử gợi ý", "0901234567", null, null), khach).id();
        donDaTao.add(id);
        return id;
    }
}
