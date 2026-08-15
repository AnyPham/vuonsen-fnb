// Ba khối lặp lại ở mọi màn hình: đang tải, lỗi, rỗng
export function Loading({ label = 'Đang tải…' }) {
  return <div className="state">{label}</div>;
}

export function ErrorBlock({ message }) {
  return <div className="alert alert-error">{message || 'Đã có lỗi xảy ra'}</div>;
}

export function Empty({ label = 'Chưa có dữ liệu' }) {
  return <div className="state">{label}</div>;
}
