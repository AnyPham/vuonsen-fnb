import { useI18n } from '@/i18n';

/*
 * Nhúng bản đồ vị trí trên Google Maps, bấm vào là chỉ đường.
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
 * Bấm vào bản đồ hay vào đường dẫn bên dưới đều mở chỉ đường từ vị trí hiện tại
 * của khách tới nhà hàng. Bản đồ nhúng là trang của Google, khác nguồn với
 * website, nên website không đổi được việc bấm bên trong nó dẫn đi đâu. Vì vậy
 * phủ lên trên một lớp đường dẫn trong suốt. Đổi lại khách không kéo thả được
 * bản đồ nhỏ này, nhưng ô bản đồ chỉ cao vài trăm điểm ảnh, kéo thả trong đó vừa
 * khó vừa hay kẹt cuộn trang trên điện thoại, còn chỉ đường mới là việc khách cần.
 */

/*
 * Điểm đến khi chỉ đường. Dùng địa chỉ chữ thay vì tọa độ: tọa độ ghim trên bản
 * đồ có thể lệch vài chục mét, rơi xuống mép sông thì Google bám vào con đường
 * gần nhất và có thể dẫn khách sang bờ bên kia. Địa chỉ chữ thì Google tra ra
 * đúng số nhà, cũng chính là địa chỉ website đang ghi ở chân trang.
 *
 * Giữ bản tiếng Việt có dấu kể cả khi giao diện đang ở tiếng Anh, vì tra địa chỉ
 * Việt Nam bằng bản có dấu cho kết quả chính xác nhất.
 */
export const DIA_CHI_VUON_SEN = '1147 Bình Quới, Phường 28, Bình Thạnh, TP.HCM';

/*
 * Đường dẫn chỉ đường theo chuẩn Maps URLs của Google, không cần khóa API.
 *
 * Không truyền điểm xuất phát: Google tự lấy vị trí hiện tại của người bấm nếu
 * họ cho phép, không cho thì hiện ô để họ tự nhập. Trên điện thoại có cài app
 * Google Maps thì đường dẫn mở thẳng trong app. Cũng không chọn sẵn phương tiện,
 * để khách tự chọn đi xe máy, ô tô hay đi bộ.
 */
export function duongDanChiDuong(diemDen = DIA_CHI_VUON_SEN) {
  return `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(diemDen)}`;
}

export default function GoogleMap({ lat, lng, height = 320, zoom = 16, title, diemDen }) {
  const { t } = useI18n();
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
  const latitude = lat ?? import.meta.env.VITE_MAP_LAT ?? 10.8231;
  const longitude = lng ?? import.meta.env.VITE_MAP_LNG ?? 106.73;
  const toaDo = `${latitude},${longitude}`;
  const chiDuong = duongDanChiDuong(diemDen);

  const src = apiKey
    ? `https://www.google.com/maps/embed/v1/place?key=${apiKey}&q=${toaDo}&zoom=${zoom}`
    : `https://maps.google.com/maps?q=${toaDo}&z=${zoom}&output=embed`;

  return (
    <div>
      <div style={{ position: 'relative' }}>
        <iframe
          data-test="map"
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
          href={chiDuong}
          target="_blank"
          rel="noreferrer"
          aria-label={t('map.directions')}
          data-test="map-directions-overlay"
          style={{
            position: 'absolute',
            inset: 0,
            borderRadius: 'var(--r)',
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: 'flex-start',
            padding: 10,
            textDecoration: 'none',
          }}
        >
          <span
            style={{
              background: 'rgba(255, 255, 255, 0.94)',
              color: 'var(--green-900)',
              borderRadius: 999,
              padding: '5px 12px',
              fontSize: '0.8rem',
              fontWeight: 600,
              boxShadow: 'var(--shadow-sm)',
            }}
          >
            {t('map.tapForDirections')}
          </span>
        </a>
      </div>
      <a
        href={chiDuong}
        target="_blank"
        rel="noreferrer"
        data-test="map-directions-link"
        style={{ display: 'inline-block', marginTop: 8, fontSize: '0.84rem' }}
      >
        {t('map.directions')} →
      </a>
    </div>
  );
}
