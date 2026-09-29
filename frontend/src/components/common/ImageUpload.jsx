import { useState } from 'react';

/*
 * Ô nhập ảnh cho trang quản trị.
 *
 * Có hai cách điền: chọn tệp để tải thẳng lên Cloudinary, hoặc dán sẵn đường
 * dẫn ảnh vào ô. Cách dán luôn dùng được, kể cả khi chưa khai báo Cloudinary.
 *
 * Ảnh đi thẳng từ trình duyệt lên Cloudinary bằng upload preset dạng unsigned,
 * nên trang web không cần giữ API secret. Đổi lại phải tạo preset unsigned
 * trong phần cài đặt của Cloudinary.
 */
export default function ImageUpload({ value, onChange, label = 'Ảnh', id = 'anh' }) {
  const cloudName = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
  const preset = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET;
  const chuaCauHinh = !cloudName || !preset;

  const [dangTai, setDangTai] = useState(false);
  const [loi, setLoi] = useState(null);

  const chonTep = async (event) => {
    const tep = event.target.files?.[0];
    if (!tep) return;

    setDangTai(true);
    setLoi(null);
    try {
      const form = new FormData();
      form.append('file', tep);
      form.append('upload_preset', preset);

      const res = await fetch(`https://api.cloudinary.com/v1_1/${cloudName}/image/upload`, {
        method: 'POST',
        body: form,
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data?.error?.message || 'Tải ảnh lên không thành công');
      }
      onChange(data.secure_url);
    } catch (err) {
      setLoi(err.message);
    } finally {
      setDangTai(false);
      // Xóa giá trị để chọn lại đúng tệp đó vẫn kích hoạt được
      event.target.value = '';
    }
  };

  /*
   * Không hiện đường dẫn ảnh dạng chữ.
   *
   * Đường dẫn Cloudinary dài cả trăm ký tự, hiện ra chỉ làm rối form và dễ bị sửa tay
   * nhầm một ký tự là hỏng ảnh. Người quản trị chỉ cần thấy ảnh và đổi được ảnh, nên
   * giữ lại ảnh xem trước cùng hai nút Đổi ảnh, Bỏ ảnh. Giá trị đường dẫn vẫn nằm
   * trong state của form và gửi lên máy chủ như cũ.
   */
  const coAnh = Boolean(value);

  return (
    <div className="fgroup">
      <label htmlFor={id}>{label}</label>

      <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginTop: 4 }}>
        {coAnh ? (
          <img
            src={value}
            alt="Ảnh đang dùng"
            style={{ height: 96, width: 136, objectFit: 'cover', borderRadius: 10, border: '1px solid var(--line)' }}
          />
        ) : (
          <div
            className="muted"
            style={{
              height: 96, width: 136, borderRadius: 10, border: '1px dashed var(--line)',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.82rem',
            }}
          >
            Chưa có ảnh
          </div>
        )}

        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <label
            className="btn btn-outline btn-sm"
            style={{ cursor: chuaCauHinh || dangTai ? 'not-allowed' : 'pointer', opacity: chuaCauHinh ? 0.55 : 1 }}
          >
            {dangTai ? 'Đang tải lên…' : coAnh ? 'Đổi ảnh' : 'Chọn ảnh từ máy'}
            <input
              id={id}
              type="file"
              accept="image/*"
              hidden
              disabled={chuaCauHinh || dangTai}
              onChange={chonTep}
            />
          </label>

          {coAnh && (
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => onChange('')}>
              Bỏ ảnh
            </button>
          )}
        </div>
      </div>

      {chuaCauHinh && (
        <div className="muted" style={{ fontSize: '0.82rem', marginTop: 6 }}>
          Chưa khai báo Cloudinary nên chưa tải ảnh lên được. Điền
          {' '}
          <code>VITE_CLOUDINARY_CLOUD_NAME</code> và <code>VITE_CLOUDINARY_UPLOAD_PRESET</code>
          {' '}
          trong <code>frontend/.env</code> rồi khởi động lại giao diện.
        </div>
      )}

      {loi && <div className="err">{loi}</div>}
    </div>
  );
}
