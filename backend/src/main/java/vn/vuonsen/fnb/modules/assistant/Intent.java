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
    KHONG_HIEU
}
