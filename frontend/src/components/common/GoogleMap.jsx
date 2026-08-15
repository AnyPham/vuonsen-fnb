// Nhúng bản đồ vị trí nhà hàng bằng Google Maps Embed API
export default function GoogleMap({ lat, lng, height = 320, zoom = 16 }) {
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
  const latitude = lat ?? import.meta.env.VITE_MAP_LAT ?? 10.8231;
  const longitude = lng ?? import.meta.env.VITE_MAP_LNG ?? 106.73;

  if (!apiKey) {
    return (
      <div
        style={{
          height,
          display: 'grid',
          placeItems: 'center',
          borderRadius: 'var(--r)',
          border: '1px dashed var(--line)',
          fontSize: '0.85rem',
          textAlign: 'center',
          padding: 16,
        }}
      >
        Đặt <code>VITE_GOOGLE_MAPS_API_KEY</code> trong file <code>.env</code> để hiện bản đồ
      </div>
    );
  }

  return (
    <iframe
      title="Vị trí Vườn Sen trên bản đồ"
      width="100%"
      height={height}
      style={{ border: 0, borderRadius: 'var(--r)' }}
      loading="lazy"
      referrerPolicy="no-referrer-when-downgrade"
      src={`https://www.google.com/maps/embed/v1/place?key=${apiKey}&q=${latitude},${longitude}&zoom=${zoom}`}
    />
  );
}
