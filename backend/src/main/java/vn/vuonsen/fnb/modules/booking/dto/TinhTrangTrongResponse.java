package vn.vuonsen.fnb.modules.booking.dto;

import java.time.LocalDate;
import java.util.List;

/*
 * Tình trạng còn trống của một ngày, trả về cho khách xem trước khi chọn.
 *
 * Trước đây khách phải điền hết ba bước rồi bấm gửi mới biết sảnh đã có người đặt. Trả sẵn
 * danh sách buổi đã kín để giao diện báo ngay từ lúc chọn không gian.
 */
public record TinhTrangTrongResponse(
        LocalDate ngay,
        List<KhongGian> khongGian
) {

    /**
     * @param buoiDaKin các buổi đã có tiệc được xác nhận, dạng MORNING / NOON / EVENING
     * @param kinCaNgay đã có đơn thuê trọn ngày nên không nhận thêm buổi nào
     */
    public record KhongGian(
            Long spaceId,
            String spaceName,
            List<String> buoiDaKin,
            boolean kinCaNgay
    ) {
    }
}
