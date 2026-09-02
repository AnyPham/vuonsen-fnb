import { useCallback, useEffect, useState } from 'react';
import { adminApi, menuApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import ImageUpload from '@/components/common/ImageUpload';

// Form rỗng dùng khi bấm nút thêm món mới
const EMPTY_FORM = {
  categoryId: '',
  name: '',
  description: '',
  price: '',
  priceNote: '',
  imageUrl: '',
  bestSeller: false,
  available: true,
  sortOrder: 0,
};

// Màn hình quản trị thực đơn: thêm món, sửa món, ngừng bán món
export default function AdminMenuPage() {
  const [dishes, setDishes] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // editingId = null nghĩa là đang đóng form, 'new' là thêm mới, số là sửa món đó
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [dishList, categoryList] = await Promise.all([
        adminApi.dishes(),
        menuApi.categories(),
      ]);
      setDishes(dishList);
      setCategories(categoryList);
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
    setForm({ ...EMPTY_FORM, categoryId: categories[0]?.id || '' });
    setEditingId('new');
  };

  const openEdit = (dish) => {
    setForm({
      categoryId: dish.categoryId,
      name: dish.name,
      description: dish.description || '',
      price: dish.price ?? '',
      priceNote: dish.priceNote || '',
      imageUrl: dish.imageUrl || '',
      bestSeller: dish.bestSeller,
      available: dish.available,
      sortOrder: dish.sortOrder ?? 0,
    });
    setEditingId(dish.id);
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
        categoryId: Number(form.categoryId),
        name: form.name.trim(),
        description: form.description.trim() || null,
        // Món tính giá linh hoạt thì để trống ô giá và ghi chú cách tính
        price: form.price === '' ? null : Number(form.price),
        priceNote: form.priceNote.trim() || null,
        imageUrl: form.imageUrl.trim() || null,
        bestSeller: form.bestSeller,
        available: form.available,
        sortOrder: Number(form.sortOrder) || 0,
      };

      if (editingId === 'new') {
        await adminApi.createDish(body);
      } else {
        await adminApi.updateDish(editingId, body);
      }
      closeForm();
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const stopSelling = async (dish) => {
    if (!window.confirm(`Ngừng bán món "${dish.name}"?`)) return;
    try {
      await adminApi.stopDish(dish.id);
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
          <h2>Thực đơn</h2>
        </div>

        {error && <ErrorBlock message={error} />}

        {editingId === null && (
          <button type="button" className="btn btn-dark btn-sm" onClick={openNew} style={{ marginBottom: 22 }}>
            Thêm món mới
          </button>
        )}

        {editingId !== null && (
          <form className="card" onSubmit={save} style={{ padding: 22, marginBottom: 26 }}>
            <h3 style={{ marginBottom: 18 }}>
              {editingId === 'new' ? 'Thêm món mới' : 'Sửa món'}
            </h3>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="dish-category">Danh mục *</label>
                <select
                  id="dish-category"
                  value={form.categoryId}
                  onChange={(e) => change('categoryId', e.target.value)}
                  required
                >
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div className="fgroup">
                <label htmlFor="dish-name">Tên món *</label>
                <input
                  id="dish-name"
                  value={form.name}
                  onChange={(e) => change('name', e.target.value)}
                  maxLength={160}
                  required
                />
              </div>
            </div>

            <div className="form-row">
              <div className="fgroup">
                <label htmlFor="dish-price">Giá một phần</label>
                <input
                  id="dish-price"
                  type="number"
                  min="0"
                  step="1000"
                  value={form.price}
                  onChange={(e) => change('price', e.target.value)}
                />
                <div className="muted" style={{ fontSize: '0.82rem', marginTop: 6 }}>
                  Để trống nếu món tính giá linh hoạt
                </div>
              </div>

              <div className="fgroup">
                <label htmlFor="dish-price-note">Ghi chú giá</label>
                <input
                  id="dish-price-note"
                  value={form.priceNote}
                  onChange={(e) => change('priceNote', e.target.value)}
                  placeholder="Ví dụ: Theo cân"
                  maxLength={60}
                />
              </div>
            </div>

            <div className="fgroup">
              <label htmlFor="dish-description">Mô tả</label>
              <input
                id="dish-description"
                value={form.description}
                onChange={(e) => change('description', e.target.value)}
                maxLength={500}
              />
            </div>

            <ImageUpload
              id="dish-image"
              label="Ảnh món ăn"
              value={form.imageUrl}
              onChange={(url) => change('imageUrl', url)}
            />

            <div className="fgroup">
              <label htmlFor="dish-sort">Thứ tự hiển thị</label>
              <input
                id="dish-sort"
                type="number"
                value={form.sortOrder}
                onChange={(e) => change('sortOrder', e.target.value)}
              />
            </div>

            <div style={{ display: 'flex', gap: 20, margin: '16px 0' }}>
              <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  type="checkbox"
                  checked={form.bestSeller}
                  onChange={(e) => change('bestSeller', e.target.checked)}
                />
                <span>Món bán chạy</span>
              </label>
              <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input
                  type="checkbox"
                  checked={form.available}
                  onChange={(e) => change('available', e.target.checked)}
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
        {!loading && dishes.length === 0 && <Empty label="Chưa có món nào trong thực đơn." />}

        {!loading && dishes.length > 0 && (
          <div className="table-wrap card">
            <table>
              <thead>
                <tr>
                  <th>Tên món</th>
                  <th>Danh mục</th>
                  <th>Giá</th>
                  <th>Bán chạy</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {dishes.map((dish) => (
                  <tr key={dish.id}>
                    <td>{dish.name}</td>
                    <td>{dish.categoryName}</td>
                    <td>{dish.price ? formatCurrency(dish.price) : dish.priceNote || '—'}</td>
                    <td>{dish.bestSeller ? 'Có' : '—'}</td>
                    <td>{dish.available ? 'Đang bán' : 'Ngừng bán'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline"
                          onClick={() => openEdit(dish)}
                        >
                          Sửa
                        </button>
                        {dish.available && (
                          <button
                            type="button"
                            className="btn btn-sm btn-ghost"
                            onClick={() => stopSelling(dish)}
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
