/*
 * Nhúng bản đồ vị trí trên Google Maps.
 *
 * Có hai cách nhúng, chọn theo việc có khóa API hay không:
 *
 *   - Có khóa: dùng Google Maps Embed API, là đường chính thức có tài liệu.
 *     Khóa lấy ở Google Cloud Console và phải bật thanh toán cho dự án, dù
 *     riêng Embed API thì không tính tiền.
 *   - Không có khóa: dùng đường dẫn nhúng cũ của maps.google.com. Cách này
 *     không nằm trong tài liệu chính thức nhưng chạy được và không cần tài
 *     khoản thanh toán, đủ dùng cho bản chạy thử.
 *
 * Dù theo cách nào cũng luôn kèm đường dẫn mở Google Maps trong tab mới, để
 * khách bấm vào là chỉ đường được.
 */
export default function GoogleMap({ lat, lng, height = 320, zoom = 16, title }) {
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
  const latitude = lat ?? import.meta.env.VITE_MAP_LAT ?? 10.8231;
  const longitude = lng ?? import.meta.env.VITE_MAP_LNG ?? 106.73;
  const toaDo = `${latitude},${longitude}`;

  const src = apiKey
    ? `https://www.google.com/maps/embed/v1/place?key=${apiKey}&q=${toaDo}&zoom=${zoom}`
    : `https://maps.google.com/maps?q=${toaDo}&z=${zoom}&output=embed`;

  return (
    <div>
      <iframe
        title={title ? `Vị trí ${title} trên bản đồ` : 'Vị trí Vườn Sen trên bản đồ'}
        width="100%"
        height={height}
        style={{ border: 0, borderRadius: 'var(--r)', display: 'block' }}
        loading="lazy"
        referrerPolicy="no-referrer-when-downgrade"
        allowFullScreen
        src={src}
      />
      <a
        href={`https://www.google.com/maps/search/?api=1&query=${toaDo}`}
        target="_blank"
        rel="noreferrer"
        style={{ display: 'inline-block', marginTop: 8, fontSize: '0.84rem' }}
      >
        Mở trong Google Maps →
      </a>
    </div>
  );
}
