package vn.vuonsen.fnb.modules.assistant;

/*
 * Cho biết câu trả lời do đâu mà ra.
 *
 * Giao diện dùng để nói thật với khách đang ở chế độ nào, và khi bảo vệ đồ án thì
 * nhìn vào đây thấy ngay cơ chế dự phòng có thực sự chạy hay không, thay vì phải
 * đoán qua nhật ký máy chủ.
 */
public enum AnswerSource {

    // Mô hình ngôn ngữ ngoài trả lời, trên nền dữ liệu hệ thống gửi kèm
    MO_HINH_NGON_NGU,

    // Hệ thống tự trả lời: hoặc chưa bật mô hình, hoặc gọi ra ngoài không thành
    DU_PHONG
}
