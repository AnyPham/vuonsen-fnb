package vn.vuonsen.fnb.modules.user;

public enum Role {
    CUSTOMER,   // khách hàng: xem và đặt tiệc
    STAFF,      // nhân viên: duyệt đơn đặt tiệc
    ADMIN;      // quản trị: toàn quyền

    // Spring Security yêu cầu quyền phải bắt đầu bằng ROLE_
    public String authority() {
        return "ROLE_" + name();
    }
}
