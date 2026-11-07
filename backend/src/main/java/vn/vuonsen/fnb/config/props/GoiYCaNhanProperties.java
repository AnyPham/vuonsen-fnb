package vn.vuonsen.fnb.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import vn.vuonsen.fnb.modules.recommendation.canhan.BoGoiYKhongGian;

/*
 * Tham số của gợi ý thông minh trên trang chủ, đọc từ mục app.goi-y-ca-nhan trong
 * application.yml.
 *
 * Mọi con số quyết định kết quả gợi ý đều nằm ở đây chứ không viết cứng trong mã: trọng số
 * từng nhóm thuộc tính, tỉ lệ trộn hai cách gợi ý, số láng giềng, độ đo tương đồng. Phần đánh
 * giá ngoại tuyến (DanhGiaGoiYKhongGianTest) chạy thử nhiều bộ tham số trên cùng dữ liệu, rồi
 * bộ cho kết quả tốt được ghi lại vào đây.
 */
@ConfigurationProperties(prefix = "app.goi-y-ca-nhan")
public record GoiYCaNhanProperties(

        // Trọng số bốn nhóm thuộc tính của sảnh, tổng bằng 1
        double trongSoLoaiTiec,
        double trongSoSoKhach,
        double trongSoKieuKhongGian,
        double trongSoTamGia,

        // Tỉ lệ của lọc theo nội dung khi trộn với lọc cộng tác, phần còn lại là lọc cộng tác
        double alpha,

        // Số khách giống nhất được dùng để bầu sảnh
        int soLangGieng,

        // Độ đo so sánh hai khách: COSINE, PEARSON hoặc JACCARD
        BoGoiYKhongGian.DoDo doDo,

        // Đơn đang chờ xác nhận chỉ tính một phần, vì khách có thể chưa chốt
        double trongSoDonCho,

        // Một sảnh xem đi xem lại cộng vào hồ sơ nhẹ hơn một đơn đã đặt
        double trongSoLuotXem,
        int soNgayLuotXem,
        int soLanXemToiThieu,

        // Sở thích tự khai cộng vào hồ sơ ngang một đơn
        double trongSoSoThich,

        // Khách mới thấy sảnh và món được đặt nhiều nhất trong ngần ấy ngày
        int soNgayNoiBat,

        int soSanh,

        // Trọng số ba nhóm thuộc tính của món và tỉ lệ trộn riêng cho món
        double trongSoDanhMucMon,
        double trongSoChayMon,
        double trongSoTamGiaMon,
        double alphaMon,
        int soMon,

        // Dữ liệu của mọi khách được giữ trong bộ nhớ ngần ấy giây rồi mới đọc lại
        int giayLamMoi
) {

    public double[] trongSoNhomSanh() {
        return new double[]{trongSoLoaiTiec, trongSoSoKhach, trongSoKieuKhongGian, trongSoTamGia};
    }

    public double[] trongSoNhomMon() {
        return new double[]{trongSoDanhMucMon, trongSoChayMon, trongSoTamGiaMon};
    }

    public BoGoiYKhongGian.CauHinh cauHinhSanh() {
        return new BoGoiYKhongGian.CauHinh(alpha, soLangGieng, doDo);
    }
}
