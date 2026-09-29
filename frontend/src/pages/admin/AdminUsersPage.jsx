import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatDate } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import Pagination from '@/components/common/Pagination';

const ROLE_OPTIONS = [
  { value: '', label: 'Tất cả vai trò' },
  { value: 'CUSTOMER', label: 'Khách hàng' },
  { value: 'STAFF', label: 'Nhân viên' },
  { value: 'ADMIN', label: 'Quản trị' },
];

const NHAN_VAI_TRO = { CUSTOMER: 'Khách hàng', STAFF: 'Nhân viên', ADMIN: 'Quản trị' };

/*
 * Quản trị tài khoản người dùng.
 *
 * Trước đây muốn có tài khoản nhân viên phải sửa thẳng cột role trong cơ sở dữ liệu, vì trang
 * đăng ký chỉ tạo ra tài khoản khách hàng. Màn hình này thay cho việc đó.
 */
export default function AdminUsersPage() {
  const [filters, setFilters] = useState({ role: '', keyword: '' });
  const [trang, setTrang] = useState(0);
  const [page, setPage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [moTao, setMoTao] = useState(false);
  const [formTao, setFormTao] = useState({
    fullName: '', email: '', phone: '', password: '', role: 'STAFF',
  });

  const doiLoc = (patch) => {
    setFilters((truoc) => ({ ...truoc, ...patch }));
    setTrang(0);
  };

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { page: trang, size: 20 };
      if (filters.role) params.role = filters.role;
      if (filters.keyword) params.keyword = filters.keyword;
      setPage(await adminApi.users(params));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filters, trang]);

  useEffect(() => {
    load();
  }, [load]);

  const tao = async (event) => {
    event.preventDefault();
    try {
      await adminApi.createUser(formTao);
      setMoTao(false);
      setFormTao({ fullName: '', email: '', phone: '', password: '', role: 'STAFF' });
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const doiVaiTro = async (u, vaiTroMoi) => {
    if (!window.confirm(`Đổi vai trò của ${u.email} thành ${NHAN_VAI_TRO[vaiTroMoi]}?`)) return;
    try {
      await adminApi.changeUserRole(u.id, vaiTroMoi);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const doiTrangThai = async (u) => {
    const moKhoa = !u.enabled;
    if (!window.confirm(`${moKhoa ? 'Mở khóa' : 'Khóa'} tài khoản ${u.email}?`)) return;
    try {
      await adminApi.changeUserEnabled(u.id, moKhoa);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  const datLaiMatKhau = async (u) => {
    const matKhau = window.prompt(
      `Đặt lại mật khẩu cho ${u.email}.\n`
      + 'Nhập mật khẩu mới rồi báo trực tiếp cho họ, hệ thống không gửi mật khẩu qua email:',
      '',
    );
    if (!matKhau) return;
    try {
      await adminApi.resetUserPassword(u.id, matKhau);
      window.alert('Đã đặt lại mật khẩu. Nhớ báo trực tiếp cho người dùng.');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">Quản trị</div>
          <h2>Tài khoản người dùng</h2>
        </div>

        <div className="card" style={{ marginBottom: 24 }}>
          <div className="card-body form-row">
            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="u-role">Vai trò</label>
              <select
                id="u-role"
                data-test="user-filter-role"
                value={filters.role}
                onChange={(e) => doiLoc({ role: e.target.value })}
              >
                {ROLE_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>

            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="u-keyword">Tìm theo tên hoặc email</label>
              <input
                id="u-keyword"
                data-test="user-filter-keyword"
                value={filters.keyword}
                placeholder="Nguyễn Văn A hoặc email@..."
                onChange={(e) => doiLoc({ keyword: e.target.value })}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button
                type="button"
                className="btn btn-dark btn-sm"
                onClick={() => setMoTao((v) => !v)}
                data-test="open-create-user"
              >
                Tạo tài khoản nhân viên
              </button>
            </div>
          </div>
        </div>

        {moTao && (
          <form className="card" style={{ marginBottom: 24 }} onSubmit={tao} data-test="create-user-form">
            <div className="card-body">
              <h3 style={{ marginBottom: 4 }}>Tạo tài khoản mới</h3>
              <p className="muted" style={{ fontSize: '0.88rem', marginBottom: 16 }}>
                Hệ thống gửi thư báo cho người được cấp tài khoản nhưng cố ý không gửi kèm mật
                khẩu. Bạn báo mật khẩu trực tiếp cho họ và nhắc đổi ngay sau lần đăng nhập đầu.
              </p>

              <div className="form-row">
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="nu-name">Họ tên</label>
                  <input
                    id="nu-name"
                    data-test="new-user-name"
                    required
                    value={formTao.fullName}
                    onChange={(e) => setFormTao({ ...formTao, fullName: e.target.value })}
                  />
                </div>
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="nu-email">Email</label>
                  <input
                    id="nu-email"
                    data-test="new-user-email"
                    type="email"
                    required
                    value={formTao.email}
                    onChange={(e) => setFormTao({ ...formTao, email: e.target.value })}
                  />
                </div>
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="nu-phone">Số điện thoại</label>
                  <input
                    id="nu-phone"
                    value={formTao.phone}
                    onChange={(e) => setFormTao({ ...formTao, phone: e.target.value })}
                  />
                </div>
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="nu-pass">Mật khẩu</label>
                  <input
                    id="nu-pass"
                    data-test="new-user-password"
                    type="password"
                    required
                    minLength={6}
                    value={formTao.password}
                    onChange={(e) => setFormTao({ ...formTao, password: e.target.value })}
                  />
                </div>
                <div className="fgroup" style={{ marginBottom: 0 }}>
                  <label htmlFor="nu-role">Vai trò</label>
                  <select
                    id="nu-role"
                    data-test="new-user-role"
                    value={formTao.role}
                    onChange={(e) => setFormTao({ ...formTao, role: e.target.value })}
                  >
                    <option value="STAFF">Nhân viên</option>
                    <option value="ADMIN">Quản trị</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'flex', gap: 10, marginTop: 18 }}>
                <button type="submit" className="btn btn-dark btn-sm" data-test="create-user-submit">
                  Tạo tài khoản
                </button>
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => setMoTao(false)}>
                  Bỏ qua
                </button>
              </div>
            </div>
          </form>
        )}

        {error && <ErrorBlock message={error} />}
        {loading && <Loading />}
        {page && page.content.length === 0 && !loading && (
          <Empty label="Không có tài khoản nào khớp bộ lọc." />
        )}

        {page && page.content.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Họ tên</th>
                  <th>Email</th>
                  <th>Điện thoại</th>
                  <th>Vai trò</th>
                  <th>Trạng thái</th>
                  <th>Tạo ngày</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {page.content.map((u) => (
                  <tr key={u.id} data-test="admin-user-row">
                    <td>{u.fullName}</td>
                    <td data-test="admin-user-email">{u.email}</td>
                    <td>{u.phone || <span className="muted">—</span>}</td>
                    <td>
                      <select
                        value={u.role}
                        data-test="user-role-select"
                        onChange={(e) => doiVaiTro(u, e.target.value)}
                        style={{ padding: '6px 8px', fontSize: '0.86rem' }}
                      >
                        <option value="CUSTOMER">Khách hàng</option>
                        <option value="STAFF">Nhân viên</option>
                        <option value="ADMIN">Quản trị</option>
                      </select>
                    </td>
                    <td>
                      <span className={`tag tag-${u.enabled ? 'CONFIRMED' : 'CANCELLED'}`}>
                        {u.enabled ? 'Đang dùng' : 'Đã khóa'}
                      </span>
                    </td>
                    <td>{formatDate(u.createdAt)}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline"
                          onClick={() => datLaiMatKhau(u)}
                          data-test="user-reset-password"
                        >
                          Đặt lại mật khẩu
                        </button>
                        <button
                          type="button"
                          className="btn btn-sm btn-ghost"
                          onClick={() => doiTrangThai(u)}
                          data-test="user-toggle-enabled"
                        >
                          {u.enabled ? 'Khóa' : 'Mở khóa'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination trang={page} doiTrang={setTrang} donVi="tài khoản" />
      </div>
    </section>
  );
}
