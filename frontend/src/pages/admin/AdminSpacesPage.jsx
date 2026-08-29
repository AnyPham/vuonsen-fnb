import { useCallback, useEffect, useState } from 'react';
import { adminApi, spaceApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

const EMPTY_FORM = {
  code: '',
  name: '',
  slug: '',
  spaceType: 'OUTDOOR',
  shortDesc: '',
  description: '',
  capacityMin: '',
  capacityMax: '',
  rentalFee: '',
  feeUnit: 'SESSION',
  unitCapacity: '',
  active: true,
  sortOrder: 0,
  amenities: '',
};

// Đổi tên không gian thành slug để dùng trong đường dẫn, ví dụ "Vườn Cau" thành "vuon-cau"
function toSlug(text) {
  return text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

// Màn hình quản trị không gian: thêm, sửa, ngừng kinh doanh
export default function AdminSpacesPage() {
  const [spaces, setSpaces] = useState([]);
  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [spaceList, typeList] = await Promise.all([adminApi.spaces(), spaceApi.types()]);
      setSpaces(spaceList);
      setTypes(typeList);
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

  const openEdit = (space) => {
    setForm({
      code: space.code,
      name: space.name,
      slug: space.slug,
      spaceType: space.type,
      shortDesc: space.shortDesc || '',
      description: space.description || '',
      capacityMin: space.capacityMin ?? '',
      capacityMax: space.capacityMax ?? '',
      rentalFee: space.rentalFee ?? '',
      feeUnit: space.feeUnit || 'SESSION',
      unitCapacity: space.unitCapacity ?? '',
      active: space.active,
      sortOrder: space.sortOrder ?? 0,
      amenities: (space.amenities || []).join('\n'),
    });
    setEditingId(space.id);
  };

  const closeForm = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
  };

  const change = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  // Gõ tên xong tự điền slug, nhưng vẫn sửa tay được nếu muốn
  const changeName = (value) => {
    setForm((prev) => ({
      ...prev,
      name: value,
      slug: prev.slug === toSlug(prev.name) || prev.slug === '' ? toSlug(value) : prev.slug,
    }));
  };

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const body = {
        code: form.code.trim().toUpperCase(),
        name: form.name.trim(),
        slug: form.slug.trim(),
        spaceType: form.spaceType,
        shortDesc: form.shortDesc.trim() || null,
        description: form.description.trim() || null,
        capacityMin: Number(form.capacityMin),
        capacityMax: Number(form.capacityMax),
        rentalFee: Number(form.rentalFee),
        feeUnit: form.feeUnit,
        unitCapacity: form.unitCapacity === '' ? null : Number(form.unitCapacity),
        active: form.active,
        sortOrder: Number(form.sortOrder) || 0,
        amenities: form.amenities.split('\n').map((a) => a.trim()).filter(Boolean),
      };

      if (editingId === 'new') {
        await adminApi.createSpace(body);
      } else {
        await adminApi.updateSpace(editingId, body);
      }
      closeForm();
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const stopRenting = async (space) => {
    if (!window.confirm(`Ngừng kinh doanh không gian "${space.name}"?`)) return;
    try {
      await adminApi.stopSpace(space.id);
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
          <h2>Không gian sự kiện</h2>
        </div>

        {error && <ErrorBlock message={error} />}

        {editingId === null && (
          <button type="button" className="btn btn-dark btn-sm" onClick={openNew} style={{ marginBottom: 22 }}>
            Thêm không gian
          </button>
        )}

        {editingId !== null && (
          <form className="card" onSubmit={save} style={{ padding: 22, marginBottom: 26 }}>
            <h3 style={{ marginBottom: 18 }}>
              {editingId === 'new' ? 'Thêm không gian' : 'Sửa không gian'}
            </h3>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="space-code">Mã không gian *</label>
                <input
                  id="space-code"
                  value={form.code}
                  onChange={(e) => change('code', e.target.value)}
                  placeholder="Ví dụ: VUON-CAU"
                  maxLength={40}
                  required
                />
              </div>

              <div className="fgroup">
                <label htmlFor="space-name">Tên không gian *</label>
                <input
                  id="space-name"
                  value={form.name}
                  onChange={(e) => changeName(e.target.value)}
                  maxLength={120}
                  required
                />
              </div>

              <div className="fgroup">
                <label htmlFor="space-slug">Đường dẫn *</label>
                <input
                  id="space-slug"
                  value={form.slug}
                  onChange={(e) => change('slug', e.target.value)}
                  maxLength={140}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="space-type">Loại không gian *</label>
                <select
                  id="space-type"
                  value={form.spaceType}
                  onChange={(e) => change('spaceType', e.target.value)}
                  required
                >
                  {types.map((t) => (
                    <option key={t.value} value={t.value}>{t.label}</option>
                  ))}
                </select>
              </div>

              <div className="fgroup">
                <label htmlFor="space-min">Sức chứa tối thiểu *</label>
                <input
                  id="space-min"
                  type="number"
                  min="1"
                  value={form.capacityMin}
                  onChange={(e) => change('capacityMin', e.target.value)}
                  required
                />
              </div>

              <div className="fgroup">
                <label htmlFor="space-max">Sức chứa tối đa *</label>
                <input
                  id="space-max"
                  type="number"
                  min="1"
                  value={form.capacityMax}
                  onChange={(e) => change('capacityMax', e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="space-fee">Phí thuê *</label>
                <input
                  id="space-fee"
                  type="number"
                  min="0"
                  step="100000"
                  value={form.rentalFee}
                  onChange={(e) => change('rentalFee', e.target.value)}
                  required
                />
              </div>

              <div className="fgroup">
                <label htmlFor="space-fee-unit">Cách tính phí</label>
                <select
                  id="space-fee-unit"
                  value={form.feeUnit}
                  onChange={(e) => change('feeUnit', e.target.value)}
                >
                  {/* Giá trị phải khớp với PricingService, chỗ đó so sánh với chuỗi "HUT" */}
                  <option value="SESSION">Theo buổi</option>
                  <option value="HUT">Theo chòi</option>
                </select>
              </div>

              <div className="fgroup">
                <label htmlFor="space-unit">Số khách một chòi</label>
                <input
                  id="space-unit"
                  type="number"
                  min="1"
                  value={form.unitCapacity}
                  onChange={(e) => change('unitCapacity', e.target.value)}
                />
                <div className="muted" style={{ fontSize: '0.82rem', marginTop: 6 }}>
                  Chỉ điền khi tính phí theo chòi
                </div>
              </div>
            </div>

            <div className="fgroup">
              <label htmlFor="space-short">Mô tả ngắn</label>
              <input
                id="space-short"
                value={form.shortDesc}
                onChange={(e) => change('shortDesc', e.target.value)}
                maxLength={500}
              />
            </div>

            <div className="fgroup">
              <label htmlFor="space-desc">Mô tả chi tiết</label>
              <textarea
                id="space-desc"
                value={form.description}
                onChange={(e) => change('description', e.target.value)}
              />
            </div>

            <div className="fgroup">
              <label htmlFor="space-amenities">Tiện ích</label>
              <textarea
                id="space-amenities"
                value={form.amenities}
                onChange={(e) => change('amenities', e.target.value)}
                placeholder="Mỗi dòng một tiện ích"
              />
            </div>

            <div className="fgroup">
              <label htmlFor="space-sort">Thứ tự hiển thị</label>
              <input
                id="space-sort"
                type="number"
                value={form.sortOrder}
                onChange={(e) => change('sortOrder', e.target.value)}
              />
            </div>

            <label style={{ display: 'flex', gap: 8, alignItems: 'center', margin: '16px 0' }}>
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => change('active', e.target.checked)}
              />
              <span>Đang cho thuê</span>
            </label>

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
        {!loading && spaces.length === 0 && <Empty label="Chưa có không gian nào." />}

        {!loading && spaces.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Mã</th>
                  <th>Tên không gian</th>
                  <th>Loại</th>
                  <th>Sức chứa</th>
                  <th>Phí thuê</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {spaces.map((space) => (
                  <tr key={space.id}>
                    <td>{space.code}</td>
                    <td>{space.name}</td>
                    <td>{space.typeLabel}</td>
                    <td>{space.capacityMin} – {space.capacityMax} khách</td>
                    <td>{formatCurrency(space.rentalFee)}</td>
                    <td>{space.active ? 'Đang cho thuê' : 'Đã ngừng'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline"
                          onClick={() => openEdit(space)}
                        >
                          Sửa
                        </button>
                        {space.active && (
                          <button
                            type="button"
                            className="btn btn-sm btn-ghost"
                            onClick={() => stopRenting(space)}
                          >
                            Ngừng
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
