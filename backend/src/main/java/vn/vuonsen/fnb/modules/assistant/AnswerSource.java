package vn.vuonsen.fnb.modules.assistant;

/*
 * Cho biết câu trả lời do đâu mà ra.
 *
 * Giao diện dùng để nói thật với khách đang ở chế độ nào, và khi bảo vệ đồ án thì
 * nhìn vào đây thấy ngay cơ chế dự phòng có thực sự chạy hay không, thay vì phải
 * đoán qua nhật ký máy chủ.
 */
public enum AnswerSource {

    /*
     * Câu hỏi nằm trong ngân hàng: hệ thống nhận ra ý định rồi dựng lời đáp từ cơ sở dữ
     * liệu, không gọi mô hình ngôn ngữ lần nào.
     *
     * Đây là hướng mặc định và chiếm phần lớn lượt hỏi. Chọn như vậy vì ba lẽ: câu trả
     * lời giống hệt nhau qua mọi lần hỏi, mọi con số truy được về đúng bảng nào trong cơ
     * sở dữ liệu, và không tốn một lượt gọi dịch vụ ngoài nào.
     */
    NGAN_HANG_CAU_HOI,

    // Mô hình ngôn ngữ ngoài trả lời, trên nền dữ liệu hệ thống gửi kèm
    MO_HINH_NGON_NGU,

    // Hệ thống tự trả lời: hoặc chưa bật mô hình, hoặc gọi ra ngoài không thành
    DU_PHONG
}
