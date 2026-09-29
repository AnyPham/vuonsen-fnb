import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatDate } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

const EMPTY_FORM = {
  name: '',
  startDate: '',
  endDate: '',
  percent: 10,
  active: true,
};

// Hôm nay dạng yyyy-MM-dd theo giờ máy, để so với ngày của dịp lễ
function today() {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

// 0.15 thành 15. Làm tròn vì 0.15 * 100 trong JavaScript ra 15.000000000000002
const toPercent = (rate) => Math.round(Number(rate) * 100);

// Máy chủ báo lỗi từng ô thì gộp lại cho quản trị đọc, không thì dùng câu báo chung
function errorText(err) {
  const fields = err.fieldErrors;
  if (Array.isArray(fields) && fields.length > 0) {
    return fields.map((f) => f.message || f).join('. ');
  }
  if (fields && typeof fields === 'object' && Object.keys(fields).length > 0) {
    return Object.values(fields).join('. ');
  }
  return err.message;
}

function statusOf(holiday, now) {
  if (!holiday.active) return 'Đã tắt';
  if (holiday.endDate < now) return 'Đã qua';
  if (holiday.startDate <= now) return 'Đang diễn ra';
  return 'Sắp tới';
}

/*
 * Màn hình quản trị giảm giá ngày lễ.
 *
 * Mỗi dòng là một khoảng ngày của một năm cụ thể. Tết Nguyên Đán và Giỗ Tổ tính theo
 * âm lịch nên mỗi năm rơi vào ngày dương khác nhau, sang năm mới phải thêm dòng mới.
 */
export default function AdminHolidaysPage() {
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setHolidays(await adminApi.holidays());
    } catch (err) {
      setError(errorText(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const openNew = () => {
    setForm(EMPTY_FORM);
    setEditingId('new');
  };

  const openEdit = (holiday) => {
    setForm({
      name: holiday.name,
      startDate: holiday.startDate,
      endDate: holiday.endDate,
      percent: toPercent(holiday.discountRate),
      active: holiday.active,
    });
    setEditingId(holiday.id);
  };

  const closeForm = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
  };

  const change = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const save = async (event) => {
    event.preventDefault();
    if (form.endDate < form.startDate) {
      setError('Ngày kết thúc không được trước ngày bắt đầu');
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const body = {
        name: form.name.trim(),
        startDate: form.startDate,
        endDate: form.endDate,
        // Máy chủ nhận tỉ lệ: nhập 15% thì gửi đi 0.15
        discountRate: Number(form.percent) / 100,
        active: form.active,
      };

      if (editingId === 'new') {
        await adminApi.createHoliday(body);
      } else {
        await adminApi.updateHoliday(editingId, body);
      }
      closeForm();
      await load();
    } catch (err) {
      setError(errorText(err));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (holiday) => {
    if (!window.confirm(`Xóa dịp "${holiday.name}" ngày ${formatDate(holiday.startDate)}?`)) return;
    try {
      await adminApi.deleteHoliday(holiday.id);
      await load();
    } catch (err) {
      setError(errorText(err));
    }
  };

  const now = today();

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">Quản trị</div>
          <h2>Giảm giá ngày lễ</h2>
        </div>

        <p className="muted" style={{ marginBottom: 18 }}>
          Ngày khách tới (ngày tổ chức tiệc hoặc ngày nhận món) rơi vào dịp đang áp dụng thì
          được giảm theo mức của dịp đó. Ưu đãi dịp lễ không cộng dồn với ưu đãi đặt sớm, hệ
          thống lấy mức cao hơn. Đơn đã đặt giữ nguyên số tiền đã chốt, sửa hay xóa dịp lễ
          không làm đổi đơn cũ.
        </p>

        {error && <ErrorBlock message={error} />}

        {editingId === null && (
          <button
            type="button"
            className="btn btn-dark btn-sm"
            onClick={openNew}
            style={{ marginBottom: 22 }}
            data-test="holiday-new"
          >
            Thêm dịp lễ
          </button>
        )}

        {editingId !== null && (
          <form className="card" onSubmit={save} style={{ padding: 22, marginBottom: 26 }}>
            <h3 style={{ marginBottom: 18 }}>
              {editingId === 'new' ? 'Thêm dịp lễ' : 'Sửa dịp lễ'}
            </h3>

            <div className="fgroup">
              <label htmlFor="holiday-name">Tên dịp lễ *</label>
              <input
                id="holiday-name"
                value={form.name}
                onChange={(e) => change('name', e.target.value)}
                placeholder="Ví dụ: Tết Nguyên Đán"
                maxLength={120}
                required
                data-test="holiday-name"
              />
            </div>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="holiday-start">Từ ngày *</label>
                <input
                  id="holiday-start"
                  type="date"
                  value={form.startDate}
                  onChange={(e) => change('startDate', e.target.value)}
                  required
                  data-test="holiday-start"
                />
              </div>

              <div className="fgroup">
                <label htmlFor="holiday-end">Đến ngày *</label>
                <input
                  id="holiday-end"
                  type="date"
                  value={form.endDate}
                  min={form.startDate || undefined}
                  onChange={(e) => change('endDate', e.target.value)}
                  required
                  data-test="holiday-end"
                />
                <div className="muted" style={{ fontSize: '0.82rem', marginTop: 6 }}>
                  Lễ một ngày thì chọn trùng ngày bắt đầu
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="holiday-percent">Mức giảm (%) *</label>
                <input
                  id="holiday-percent"
                  type="number"
                  min="10"
                  max="20"
                  step="1"
                  value={form.percent}
                  onChange={(e) => change('percent', e.target.value)}
                  required
                  data-test="holiday-percent"
                />
                <div className="muted" style={{ fontSize: '0.82rem', marginTop: 6 }}>
                  Chính sách cho phép từ 10% đến 20%
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', gap: 20, margin: '16px 0' }}>
              <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  type="checkbox"
                  checked={form.active}
                  onChange={(e) => change('active', e.target.checked)}
                  data-test="holiday-active"
                />
                <span>Đang áp dụng</span>
              </label>
            </div>

            <div style={{ display: 'flex', gap: 10 }}>
              <button type="submit" className="btn btn-dark btn-sm" disabled={saving} data-test="holiday-save">
                {saving ? 'Đang lưu…' : 'Lưu'}
              </button>
              <button type="button" className="btn btn-ghost btn-sm" onClick={closeForm}>
                Hủy
              </button>
            </div>
          </form>
        )}

        {loading && <Loading />}
        {!loading && holidays.length === 0 && <Empty label="Chưa có dịp lễ nào." />}

        {!loading && holidays.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Dịp lễ</th>
                  <th>Thời gian</th>
                  <th>Mức giảm</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {holidays.map((holiday) => {
                  const status = statusOf(holiday, now);
                  return (
                    <tr key={holiday.id} data-test="admin-holiday-row">
                      <td>{holiday.name}</td>
                      <td>
                        {holiday.startDate === holiday.endDate
                          ? formatDate(holiday.startDate)
                          : `${formatDate(holiday.startDate)} – ${formatDate(holiday.endDate)}`}
                      </td>
                      <td>{toPercent(holiday.discountRate)}%</td>
                      <td className={status === 'Đã qua' || status === 'Đã tắt' ? 'muted' : undefined}>
                        {status}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button
                            type="button"
                            className="btn btn-sm btn-outline"
                            onClick={() => openEdit(holiday)}
                            data-test="holiday-edit"
                          >
                            Sửa
                          </button>
                          <button
                            type="button"
                            className="btn btn-sm btn-ghost"
                            onClick={() => remove(holiday)}
                            data-test="holiday-delete"
                          >
                            Xóa
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
