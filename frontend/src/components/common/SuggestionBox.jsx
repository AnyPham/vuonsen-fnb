import { useEffect, useState } from 'react';
import { recommendationApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';

/*
 * Khối gợi ý hiện ở bước 2 của form đặt tiệc.
 * Gọi API mỗi khi khách đổi số khách hoặc loại sự kiện, đưa ra ba tổ hợp
 * không gian và gói tiệc phù hợp nhất kèm lý do.
 */
export default function SuggestionBox({ guestCount, eventType, eventDate, onPick }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const guests = Number(guestCount);
    if (!guests || !eventType) {
      setItems([]);
      return undefined;
    }

    setLoading(true);
    const timer = setTimeout(() => {
      recommendationApi
        .suggest({ guestCount: guests, eventType, eventDate: eventDate || null })
        .then(setItems)
        .catch(() => setItems([]))
        .finally(() => setLoading(false));
    }, 400);

    return () => clearTimeout(timer);
  }, [guestCount, eventType, eventDate]);

  if (!eventType || !guestCount) return null;

  return (
    <div
      style={{
        background: 'var(--cream-2)',
        borderRadius: 'var(--r)',
        padding: '18px 20px',
        marginBottom: 22,
      }}
    >
      <div className="eyebrow" style={{ marginBottom: 4 }}>
        Gợi ý cho bạn
      </div>
      <p className="muted" style={{ fontSize: '0.86rem', marginBottom: 14 }}>
        Dựa trên số khách, loại sự kiện và lịch sử đặt tiệc của khách trước.
      </p>

      {loading && <p className="muted">Đang tìm phương án phù hợp…</p>}

      {!loading && items.length === 0 && (
        <p className="muted">Chưa tìm được phương án phù hợp với số khách này.</p>
      )}

      <div className="grid grid-3" style={{ gap: 14 }}>
        {items.map((item, index) => (
          <div key={`${item.spaceId}-${item.packageId}`} className="card">
            <div className="card-body" style={{ padding: 16 }}>
              {index === 0 && (
                <span className="tag tag-CONFIRMED" style={{ marginBottom: 8, display: 'inline-block' }}>
                  Phù hợp nhất
                </span>
              )}
              <strong style={{ display: 'block' }}>{item.spaceName}</strong>
              <span className="muted" style={{ fontSize: '0.86rem' }}>
                {item.packageName} · {item.tableCount} mâm
              </span>

              <div style={{ margin: '10px 0', fontWeight: 600, color: 'var(--green-800)' }}>
                {formatCurrency(item.totalAmount)}
              </div>

              <ul style={{ paddingLeft: 16, fontSize: '0.82rem', color: 'var(--ink-soft)' }}>
                {item.reasons.map((reason) => (
                  <li key={reason}>{reason}</li>
                ))}
              </ul>

              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ marginTop: 12, width: '100%' }}
                onClick={() => onPick(item.spaceId, item.packageId)}
              >
                Chọn phương án này
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
