/*
 * Ô ảnh dùng chung cho không gian, món ăn và thư viện.
 *
 * Có đường dẫn ảnh thì hiện ảnh thật, chưa có thì giữ nguyên khối màu như cũ.
 * Nhờ vậy thay ảnh chỉ cần điền đường dẫn trong trang quản trị, không phải
 * sửa code, mà chỗ nào chưa có ảnh vẫn hiển thị tử tế chứ không vỡ khung.
 */
export default function Thumb({ url, alt, variant = '', icon = '🌿', label, style }) {
  if (url) {
    return (
      <img
        className="thumb"
        src={url}
        alt={alt || label || ''}
        loading="lazy"
        style={style}
      />
    );
  }

  return (
    <div className={`ph ${variant}`.trim()} style={style}>
      <span>{icon}</span>
      {label && <span>{label}</span>}
    </div>
  );
}
