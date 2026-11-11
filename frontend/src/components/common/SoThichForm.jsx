import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { goiYApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';

const LOAI_TIEC = ['WEDDING', 'CORPORATE', 'BIRTHDAY', 'FAMILY', 'OTHER'];
const KIEU = ['TRONG_NHA', 'NGOAI_TROI'];
const TAM_GIA = ['TIET_KIEM', 'VUA_PHAI', 'CAO_CAP'];

// .fgroup trong global.css kéo mọi input rộng 100% và mọi label thành khối riêng, hợp với ô
// nhập chữ nhưng không hợp với ô đánh dấu, nên ô đánh dấu và nút chọn đè lại bằng hai kiểu này
const NHAN_LUA_CHON = { display: 'inline-flex', alignItems: 'center', gap: 8, margin: '0 18px 8px 0', fontWeight: 400, cursor: 'pointer' };
const O_LUA_CHON = { width: 'auto', padding: 0, margin: 0 };

// Nhãn dùng chung với câu lý do ở trang chủ, nơi chúng đứng giữa câu nên viết thường
const hoaDau = (s) => s.charAt(0).toUpperCase() + s.slice(1);

/*
 * Sở thích khách tự khai, phần "thông tin hồ sơ kê khai" của gợi ý thông minh.
 *
 * Mọi ô đều không bắt buộc. Khách chưa đặt lần nào mà khai ở đây thì trang chủ đã gợi ý
 * riêng ngay, không phải đợi tới đơn đầu tiên. Không hỏi giới tính hay tuổi: hệ gợi ý không
 * dùng tới, hỏi thêm chỉ làm khách ngại.
 */
export default function SoThichForm() {
  const { t } = useI18n();
  const [form, setForm] = useState({ loaiTiec: [], soKhach: '', kieuKhongGian: '', tamGia: '', anChay: false });
  const [trangThai, setTrangThai] = useState('dang-tai');
  const [loi, setLoi] = useState(null);

  useEffect(() => {
    goiYApi
      .soThich()
      .then((st) => {
        setForm({
          loaiTiec: st.loaiTiec || [],
          soKhach: st.soKhach ?? '',
          kieuKhongGian: st.kieuKhongGian || '',
          tamGia: st.tamGia || '',
          anChay: !!st.anChay,
        });
        setTrangThai('san-sang');
      })
      .catch(() => setTrangThai('san-sang'));
  }, []);

  const doi = (patch) => {
    setForm((cu) => ({ ...cu, ...patch }));
    setTrangThai('san-sang');
  };

  const batTatLoai = (loai) =>
    doi({
      loaiTiec: form.loaiTiec.includes(loai)
        ? form.loaiTiec.filter((x) => x !== loai)
        : [...form.loaiTiec, loai],
    });

  const luu = async (e) => {
    e.preventDefault();
    setTrangThai('dang-luu');
    setLoi(null);
    try {
      await goiYApi.luuSoThich({
        loaiTiec: form.loaiTiec,
        soKhach: form.soKhach === '' ? null : Number(form.soKhach),
        kieuKhongGian: form.kieuKhongGian || null,
        tamGia: form.tamGia || null,
        anChay: form.anChay,
      });
      setTrangThai('da-luu');
    } catch (err) {
      setLoi(err.fieldErrors?.soKhach || err.message);
      setTrangThai('san-sang');
    }
  };

  if (trangThai === 'dang-tai') return null;

  return (
    <div className="card" style={{ marginTop: 24 }} data-test="so-thich">
      <div className="card-body">
        <h3 style={{ marginBottom: 4 }}>{t('soThich.tieuDe')}</h3>
        <p className="muted" style={{ fontSize: '0.9rem', marginBottom: 16 }}>
          {t('soThich.moTa')}
        </p>
        {loi && <div className="alert alert-error">{loi}</div>}
        {trangThai === 'da-luu' && (
          <div className="alert alert-success" data-test="so-thich-da-luu">
            {t('soThich.daLuu')} <Link to="/">{t('soThich.xemTrangChu')}</Link>
          </div>
        )}

        <form onSubmit={luu}>
          <div className="fgroup">
            <label>{t('soThich.loaiTiec')}</label>
            <div>
              {LOAI_TIEC.map((loai) => (
                <label key={loai} style={NHAN_LUA_CHON}>
                  <input
                    type="checkbox"
                    checked={form.loaiTiec.includes(loai)}
                    onChange={() => batTatLoai(loai)}
                    data-test={`so-thich-loai-${loai}`}
                    style={O_LUA_CHON}
                  />
                  {hoaDau(t('goiY.loaiTiec.' + loai))}
                </label>
              ))}
            </div>
          </div>

          <div className="form-row">
            <div className="fgroup">
              <label htmlFor="soThichSoKhach">{t('soThich.soKhach')}</label>
              <input
                id="soThichSoKhach"
                type="number"
                min="1"
                placeholder={t('soThich.soKhachGoiY')}
                value={form.soKhach}
                onChange={(e) => doi({ soKhach: e.target.value })}
                data-test="so-thich-so-khach"
              />
            </div>
            <div className="fgroup">
              <label htmlFor="soThichTamGia">{t('soThich.tamGia')}</label>
              <select
                id="soThichTamGia"
                value={form.tamGia}
                onChange={(e) => doi({ tamGia: e.target.value })}
                data-test="so-thich-tam-gia"
              >
                <option value="">{t('soThich.khongRo')}</option>
                {TAM_GIA.map((m) => (
                  <option key={m} value={m}>
                    {hoaDau(t('goiY.tamGia.' + m))}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="fgroup">
            <label>{t('soThich.kieuKhongGian')}</label>
            <div>
              {['', ...KIEU].map((k) => (
                <label key={k || 'khong'} style={NHAN_LUA_CHON}>
                  <input
                    type="radio"
                    name="kieuKhongGian"
                    checked={form.kieuKhongGian === k}
                    onChange={() => doi({ kieuKhongGian: k })}
                    data-test={`so-thich-kieu-${k || 'KHONG'}`}
                    style={O_LUA_CHON}
                  />
                  {k ? hoaDau(t('goiY.kieu.' + k)) : t('soThich.khongQuanTrong')}
                </label>
              ))}
            </div>
          </div>

          <div className="fgroup">
            <label style={NHAN_LUA_CHON}>
              <input
                type="checkbox"
                checked={form.anChay}
                onChange={(e) => doi({ anChay: e.target.checked })}
                data-test="so-thich-an-chay"
                style={O_LUA_CHON}
              />
              {t('soThich.anChay')}
            </label>
          </div>

          <div className="fnav">
            <span />
            <button type="submit" className="btn btn-dark" disabled={trangThai === 'dang-luu'} data-test="so-thich-luu">
              {trangThai === 'dang-luu' ? t('profile.saving') : t('soThich.luu')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
