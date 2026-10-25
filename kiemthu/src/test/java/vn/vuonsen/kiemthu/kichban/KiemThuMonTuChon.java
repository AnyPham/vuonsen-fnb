package vn.vuonsen.kiemthu.kichban;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.vuonsen.kiemthu.coso.KiemThuCoSo;
import vn.vuonsen.kiemthu.trang.TrangDatTiec;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Luồng 19 — Đặt sảnh nhỏ với thực đơn tự chọn, ứng với TC-CMENU-01 đến TC-CMENU-08.
 *
 * Hình thức này dành cho nhóm nhỏ không lấy gói tiệc cố định: tự chọn từng món, trả tiền
 * theo đúng số phần đã gọi, cộng phí thuê không gian và vẫn hưởng giảm giá dịp lễ.
 *
 * Giới hạn 40 khách. Trên mức đó lựa chọn này không hiện ra, và máy chủ cũng từ chối nếu
 * có ai gọi thẳng đường dẫn API.
 */
@DisplayName("Luồng 19 — Đặt sảnh nhỏ, thực đơn tự chọn")
class KiemThuMonTuChon extends KiemThuCoSo {

    /* Cụm Chòi Sen nhận từ 8 khách, là không gian nhỏ nhất đang mở. */
    private static final String SANH_NHO = "Cụm Chòi Sen";

    private TrangDatTiec moBuocHai(int soKhach) {
        TrangDatTiec trang = new TrangDatTiec(driver()).mo()
                .dienBuoc1("FAMILY", LocalDate.now().plusDays(20), "NOON", soKhach)
                .tiepTuc();
        trang.chonKhongGian(SANH_NHO);
        return trang;
    }

    @Test
    @DisplayName("TC-CMENU-01 Nhóm nhỏ thì thấy lựa chọn tự chọn món")
    void tcCmenu01_nhomNhoThayLuaChon() {
        TrangDatTiec trang = moBuocHai(20);

        assertThat(trang.coLuaChonTuChonMon()).isTrue();
    }

    @Test
    @DisplayName("TC-CMENU-02 Nhóm trên 40 khách thì không có lựa chọn tự chọn món")
    void tcCmenu02_nhomLonKhongThay() {
        TrangDatTiec trang = moBuocHai(120);

        assertThat(trang.coLuaChonTuChonMon()).isFalse();
    }

    @Test
    @DisplayName("TC-CMENU-03 Bật tự chọn món thì hiện danh sách món của thực đơn")
    void tcCmenu03_hienDanhSachMon() {
        TrangDatTiec trang = moBuocHai(20).chonTuChonMon();

        assertThat(trang.soDongMon()).isPositive();
    }

    @Test
    @DisplayName("TC-CMENU-04 Thêm một phần thì dòng tổng đếm đúng")
    void tcCmenu04_themMotPhan() {
        TrangDatTiec trang = moBuocHai(20).chonTuChonMon().themPhan(0, 1);

        assertThat(trang.tongMonDaChon()).contains("1 món").contains("1 phần");
    }

    @Test
    @DisplayName("TC-CMENU-05 Bấm thêm ba lần liên tiếp thì cộng đủ ba phần")
    void tcCmenu05_bamNhanhKhongMatLanBam() {
        /*
         * Bấm liên tiếp không nghỉ. Trước đây phép cộng nằm ở thành phần giao diện nên cả
         * ba lần bấm cùng đọc một bản danh sách cũ của lần vẽ trước, kết quả chỉ cộng được
         * một phần. Chuyển phép tính vào kho trạng thái mới hết.
         */
        TrangDatTiec trang = moBuocHai(20).chonTuChonMon().themPhan(0, 3);

        assertThat(trang.tongMonDaChon()).contains("3 phần");
    }

    @Test
    @DisplayName("TC-CMENU-06 Bớt về không thì món rời khỏi danh sách đã chọn")
    void tcCmenu06_botVeKhong() {
        TrangDatTiec trang = moBuocHai(20).chonTuChonMon().themPhan(0, 1);
        assertThat(trang.tongMonDaChon()).contains("1 món");

        trang.botPhan(0);

        assertThat(trang.tongMonDaChon()).contains("0 món");
    }

    @Test
    @DisplayName("TC-CMENU-07 Bảng tạm tính hiện tiền ăn và không hiện số mâm")
    void tcCmenu07_bangTamTinhDungCot() {
        TrangDatTiec trang = moBuocHai(20).chonTuChonMon().themPhan(0, 2);
        trang.tongMonDaChon();

        /* Tiền ăn phải hiện vì có món; số mâm thì không, vì tính theo phần chứ không theo mâm. */
        assertThat(trang.choBangTamTinhCoTienAn()).isTrue();
        assertThat(trang.bangTamTinhCoSoMam()).isFalse();
    }

    @Test
    @DisplayName("TC-CMENU-08 Bật tự chọn mà chưa chọn món nào thì không cho sang bước ba")
    void tcCmenu08_chuaChonMonThiChan() {
        TrangDatTiec trang = moBuocHai(20).chonTuChonMon();

        trang.thuTiepTuc();

        /*
         * Câu báo lỗi chỉ xuất hiện ở bước hai, nên nhìn thấy nó là đủ kết luận giao diện
         * đã chặn lại. Không gọi dangOBuoc3() vì hàm đó chờ bước ba hiện ra, mà ở đây đúng
         * ra bước ba không được hiện.
         */
        assertThat(trang.loiGoiTiec()).contains("ít nhất một món");
    }
}
