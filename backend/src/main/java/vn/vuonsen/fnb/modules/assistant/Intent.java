package vn.vuonsen.fnb.modules.assistant;

// Các loại câu hỏi trợ lý nhận ra được
public enum Intent {
    CHAO_HOI,
    KHONG_GIAN,
    GOI_TIEC,
    THUC_DON,
    CHI_PHI,
    DAT_COC,
    // Hủy đơn thì được hoàn lại bao nhiêu tiền cọc
    HOAN_COC,
    // Trả tiền bằng cách nào: mã VietQR hay cổng VNPay
    THANH_TOAN,
    // Đặt sảnh nhỏ, tự chọn từng món thay vì lấy trọn một gói tiệc
    SANH_NHO,
    KHUYEN_MAI,
    QUY_TRINH_DAT,
    TRA_CUU_DON,
    LIEN_HE,
    /*
     * Khách so sánh Vườn Sen với nơi khác, ví dụ "giá ở đây có rẻ hơn nhà hàng khác không".
     *
     * Tách riêng vì đây là loại câu duy nhất mà ngân hàng câu trả lời không đáp được trọn
     * vẹn: muốn so thì phải có số liệu của nơi khác, mà hệ thống chỉ có số liệu của chính
     * mình. Xếp cùng nhánh với KHONG_HIEU để mô hình ngôn ngữ diễn đạt, nhưng vẫn chỉ được
     * dùng dữ liệu đã nạp, tuyệt đối không bịa giá của nhà hàng khác.
     */
    SO_SANH,
    KHONG_HIEU;

    /*
     * Câu đã có sẵn lời đáp dựng từ cơ sở dữ liệu, không cần tới mô hình ngôn ngữ.
     *
     * Dùng để chia hai hướng xử lý trong AssistantFacade.
     */
    public boolean coSanTrongNganHang() {
        return this != SO_SANH && this != KHONG_HIEU;
    }
}
