import { useEffect, useState } from 'react';
import Thumb from '@/components/common/Thumb';

/*
 * Khung xem ảnh phóng to ngay trong trang.
 *
 * Giao diện giống hệt khung xem ảnh của trang thư viện: nền tối phủ kín, nút đóng góc
 * phải, hai nút Trước/Sau, bấm ra ngoài hoặc nhấn Esc để đóng, phím mũi tên để chuyển.
 *
 * Dùng thay cho việc bấm ảnh mở đường dẫn gốc ở thẻ mới. Mở thẻ mới thì khách thấy
 * nguyên đường dẫn Cloudinary trên thanh địa chỉ và rời khỏi trang web. Xem ngay trong
 * trang thì không lộ đường dẫn ra thanh địa chỉ và khách vẫn ở lại.
 *
 * images: mảng { url, alt }. index: vị trí ảnh đang mở.
 */
export default function Lightbox({ images, index, onClose, onIndexChange }) {
  const soAnh = images.length;
  const truoc = () => onIndexChange((index - 1 + soAnh) % soAnh);
  const sau = () => onIndexChange((index + 1) % soAnh);

  useEffect(() => {
    const onKey = (event) => {
      if (event.key === 'Escape') onClose();
      if (event.key === 'ArrowLeft') onIndexChange((index - 1 + soAnh) % soAnh);
      if (event.key === 'ArrowRight') onIndexChange((index + 1) % soAnh);
    };
    document.addEventListener('keydown', onKey);
    // Khóa cuộn trang phía sau trong lúc đang xem ảnh
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = '';
    };
  }, [index, soAnh, onClose, onIndexChange]);

  const anh = images[index];
  if (!anh) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label={anh.alt}
      onClick={onClose}
      style={{
        position: 'fixed',
        inset: 0,
        background: 'rgba(18,40,28,.94)',
        display: 'grid',
        placeItems: 'center',
        zIndex: 100,
        padding: 20,
      }}
    >
      <button
        type="button"
        className="btn btn-ghost"
        style={{ position: 'absolute', top: 16, right: 20, color: 'var(--cream)', fontSize: '1.6rem' }}
        onClick={onClose}
        aria-label="Đóng"
      >
        ×
      </button>

      <div onClick={(e) => e.stopPropagation()} style={{ maxWidth: 900, width: '100%' }}>
        <Thumb url={anh.url} variant="v3" icon="🖼️" alt={anh.alt} style={{ borderRadius: 'var(--r)' }} />

        {/* Chỉ có một ảnh thì không cần nút chuyển */}
        {soAnh > 1 && (
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 16 }}>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              style={{ borderColor: 'var(--cream)', color: 'var(--cream)' }}
              onClick={truoc}
            >
              ‹ Trước
            </button>
            <span style={{ color: 'var(--cream)' }}>
              {index + 1} / {soAnh}
            </span>
            <button
              type="button"
              className="btn btn-outline btn-sm"
              style={{ borderColor: 'var(--cream)', color: 'var(--cream)' }}
              onClick={sau}
            >
              Sau ›
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

/*
 * Lưới ảnh nhỏ, bấm ảnh nào thì mở khung xem phóng to tại ảnh đó.
 *
 * Tự giữ trạng thái đang mở ảnh nào, nên mỗi nhóm ảnh (ví dụ ảnh của một đánh giá)
 * dùng một lưới riêng mà không phải khai báo thêm state ở trang cha.
 */
export function LuoiAnh({ images, className, icon = '📷' }) {
  const [dangMo, setDangMo] = useState(null);

  return (
    <>
      <div className={className}>
        {images.map((anh, i) => (
          <button
            key={anh.url}
            type="button"
            onClick={() => setDangMo(i)}
            style={{ padding: 0, border: 0, background: 'none', cursor: 'zoom-in' }}
            aria-label={`Xem lớn: ${anh.alt}`}
          >
            <Thumb url={anh.url} icon={icon} alt={anh.alt} />
          </button>
        ))}
      </div>

      {dangMo !== null && (
        <Lightbox
          images={images}
          index={dangMo}
          onClose={() => setDangMo(null)}
          onIndexChange={setDangMo}
        />
      )}
    </>
  );
}
