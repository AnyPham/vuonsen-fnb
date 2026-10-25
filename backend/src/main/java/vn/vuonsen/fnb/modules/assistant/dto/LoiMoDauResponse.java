package vn.vuonsen.fnb.modules.assistant.dto;

import java.util.List;

/*
 * Màn mở đầu của hộp thoại trợ lý.
 *
 * Khác với /ask ở chỗ khách chưa hỏi gì cả: đây là thứ hiện ra ngay lúc bấm mở hộp thoại,
 * dựng theo lịch sử đặt tiệc của chính khách đó.
 *
 * Gửi dữ liệu có cấu trúc chứ không gửi một đoạn chữ dựng sẵn, để giao diện tự quyết cách
 * bày: thẻ bấm được, nút Đặt lại, hay chỉ dòng chữ. Dựng sẵn chữ ở máy chủ thì giao diện
 * muốn đổi cách hiện lại phải sửa máy chủ.
 */
public record LoiMoDauResponse(
        String loiChao,
        // true khi khách đã có ít nhất một đơn đã thanh toán
        boolean khachQuen,
        // Khách quen: các sảnh từng đặt. Khách mới: các sảnh được đặt nhiều nhất
        List<Muc> sanh,
        List<Muc> goiTiec,
        List<Muc> monMoi,
        /*
         * Thứ khách xem nhiều lần gần đây mà chưa từng đặt.
         *
         * Đây là phần dựng từ kênh thu thập ngầm, khác hẳn ba danh sách trên vốn dựng từ
         * đơn và phiếu thu. Rỗng khi khách chưa đăng nhập hoặc chưa xem đủ để nói lên gì.
         */
        List<Muc> dangQuanTam,
        // Món khách đã gọi và đã trả tiền, nhiều phần nhất trước
        List<Muc> monDaGoi,
        /*
         * Món cùng loại với thứ khách đã gọi nhưng chưa thử bao giờ.
         *
         * Tách khỏi monMoi vì hai thứ khác hẳn nhau: monMoi là món mới thêm vào thực đơn,
         * ai cũng thấy giống nhau; còn đây là gợi ý riêng cho từng khách, dựa trên khẩu
         * vị suy ra từ những gì họ đã gọi.
         */
        List<Muc> monTuongTu,
        List<String> cauHoiGoiY
) {
    /*
     * Một mục gợi ý bấm được.
     *
     * moTa là dòng phụ dưới tiêu đề, chẳng hạn "Bạn đã đặt 2 lần, gần nhất 14/03/2026"
     * hoặc "3.600.000đ một mâm, 7 món".
     */
    public record Muc(Long id, String tieuDe, String moTa, String link, String nhanNut) {
    }
}
