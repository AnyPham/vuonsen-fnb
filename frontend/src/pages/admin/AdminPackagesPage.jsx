import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

const EMPTY_FORM = {
  code: '',
  name: '',
  tagline: '',
  pricePerTable: '',
  dishCount: '',
  hoursIncluded: '',
  featured: false,
  active: true,
  sortOrder: 0,
  features: '',
};

// Màn hình quản trị gói tiệc: thêm gói, sửa gói, ngừng bán gói
export default function AdminPackagesPage() {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setPackages(await adminApi.packages());
    } catch (err) {
      setError(err.message);
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

  const openEdit = (pkg) => {
    setForm({
      code: pkg.code,
      name: pkg.name,
      tagline: pkg.tagline || '',
      pricePerTable: pkg.pricePerTable ?? '',
      dishCount: pkg.dishCount ?? '',
      hoursIncluded: pkg.hoursIncluded ?? '',
      featured: pkg.featured,
      active: pkg.active,
      sortOrder: pkg.sortOrder ?? 0,
      // Mỗi dịch vụ đi kèm là một dòng cho dễ nhập
      features: (pkg.features || []).join('\n'),
    });
    setEditingId(pkg.id);
  };

  const closeForm = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
  };

  const change = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const body = {
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        tagline: form.tagline.trim() || null,
        pricePerTable: Number(form.pricePerTable),
        dishCount: form.dishCount === '' ? null : Number(form.dishCount),
        hoursIncluded: form.hoursIncluded === '' ? null : Number(form.hoursIncluded),
        featured: form.featured,
        active: form.active,
        sortOrder: Number(form.sortOrder) || 0,
        features: form.features.split('\n').map((f) => f.trim()).filter(Boolean),
      };

      if (editingId === 'new') {
        await adminApi.createPackage(body);
      } else {
        await adminApi.updatePackage(editingId, body);
      }
      closeForm();
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const stopSelling = async (pkg) => {
    if (!window.confirm(`Ngừng bán gói "${pkg.name}"?`)) return;
    try {
      await adminApi.stopPackage(pkg.id);
      await load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <div className="eyebrow">Quản trị</div>
          <h2>Gói tiệc</h2>
        </div>

        {error && <ErrorBlock message={error} />}

        {editingId === null && (
          <button type="button" className="btn btn-dark btn-sm" onClick={openNew} style={{ marginBottom: 22 }}>
            Thêm gói tiệc
          </button>
        )}

        {editingId !== null && (
          <form className="card" onSubmit={save} style={{ padding: 22, marginBottom: 26 }}>
            <h3 style={{ marginBottom: 18 }}>
              {editingId === 'new' ? 'Thêm gói tiệc' : 'Sửa gói tiệc'}
            </h3>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="pkg-code">Mã gói *</label>
                <input
                  id="pkg-code"
                  value={form.code}
                  onChange={(e) => change('code', e.target.value)}
                  placeholder="Ví dụ: GOI-CO-BAN"
                  maxLength={40}
                  required
                />
              </div>

              <div className="fgroup">
                <label htmlFor="pkg-name">Tên gói *</label>
                <input
                  id="pkg-name"
                  value={form.name}
                  onChange={(e) => change('name', e.target.value)}
                  maxLength={120}
                  required
                />
              </div>
            </div>

            <div className="fgroup">
              <label htmlFor="pkg-tagline">Câu giới thiệu ngắn</label>
              <input
                id="pkg-tagline"
                value={form.tagline}
                onChange={(e) => change('tagline', e.target.value)}
                maxLength={255}
              />
            </div>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="pkg-price">Giá một mâm *</label>
                <input
                  id="pkg-price"
                  type="number"
                  min="0"
                  step="100000"
                  value={form.pricePerTable}
                  onChange={(e) => change('pricePerTable', e.target.value)}
                  required
                />
                <div className="muted" style={{ fontSize: '0.82rem', marginTop: 6 }}>
                  Một mâm tính cho 10 khách
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="pkg-dishes">Số món</label>
                <input
                  id="pkg-dishes"
                  type="number"
                  min="1"
                  value={form.dishCount}
                  onChange={(e) => change('dishCount', e.target.value)}
                />
              </div>

              <div className="fgroup">
                <label htmlFor="pkg-hours">Số giờ sử dụng</label>
                <input
                  id="pkg-hours"
                  type="number"
                  min="1"
                  value={form.hoursIncluded}
                  onChange={(e) => change('hoursIncluded', e.target.value)}
                />
              </div>
            </div>

            <div className="fgroup">
              <label htmlFor="pkg-features">Dịch vụ đi kèm</label>
              <textarea
                id="pkg-features"
                value={form.features}
                onChange={(e) => change('features', e.target.value)}
                placeholder="Mỗi dòng một dịch vụ"
              />
            </div>

            <div className="fgroup">
              <label htmlFor="pkg-sort">Thứ tự hiển thị</label>
              <input
                id="pkg-sort"
                type="number"
                value={form.sortOrder}
                onChange={(e) => change('sortOrder', e.target.value)}
              />
            </div>

            <div style={{ display: 'flex', gap: 20, margin: '16px 0' }}>
              <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  type="checkbox"
                  checked={form.featured}
                  onChange={(e) => change('featured', e.target.checked)}
                />
                <span>Gắn nhãn được chọn nhiều nhất</span>
              </label>
              <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  type="checkbox"
                  checked={form.active}
                  onChange={(e) => change('active', e.target.checked)}
                />
                <span>Đang bán</span>
              </label>
            </div>

            <div style={{ display: 'flex', gap: 10 }}>
              <button type="submit" className="btn btn-dark btn-sm" disabled={saving}>
                {saving ? 'Đang lưu…' : 'Lưu'}
              </button>
              <button type="button" className="btn btn-ghost btn-sm" onClick={closeForm}>
                Hủy
              </button>
            </div>
          </form>
        )}

        {loading && <Loading />}
        {!loading && packages.length === 0 && <Empty label="Chưa có gói tiệc nào." />}

        {!loading && packages.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Mã gói</th>
                  <th>Tên gói</th>
                  <th>Giá một mâm</th>
                  <th>Số món</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {packages.map((pkg) => (
                  <tr key={pkg.id}>
                    <td>{pkg.code}</td>
                    <td>
                      {pkg.name}
                      {pkg.featured && (
                        <span className="tag tag-CONFIRMED" style={{ marginLeft: 8 }}>
                          Nổi bật
                        </span>
                      )}
                    </td>
                    <td>{formatCurrency(pkg.pricePerTable)}</td>
                    <td>{pkg.dishCount || '—'}</td>
                    <td>{pkg.active ? 'Đang bán' : 'Ngừng bán'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline"
                          onClick={() => openEdit(pkg)}
                        >
                          Sửa
                        </button>
                        {pkg.active && (
                          <button
                            type="button"
                            className="btn btn-sm btn-ghost"
                            onClick={() => stopSelling(pkg)}
                          >
                            Ngừng bán
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
