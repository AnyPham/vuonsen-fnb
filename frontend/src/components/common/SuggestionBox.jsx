import { useEffect, useState } from 'react';
import { recommendationApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';

/*
 * Khối gợi ý, dùng ở bước 2 của form đặt tiệc và ở trang danh sách không gian.
 *
 * Gọi API mỗi khi khách đổi số khách hoặc loại sự kiện, đưa ra ba tổ hợp không
 * gian và gói tiệc phù hợp nhất kèm lý do.
 *
 * Chỉ số khách là bắt buộc, giống ràng buộc của API. Chưa chọn loại sự kiện thì
 * vẫn gợi ý được, chỉ là dựa chủ yếu vào sức chứa nên kém sát hơn. Ở trang không
 * gian khách hay xem lướt trước khi biết mình tổ chức tiệc gì, bắt chọn loại sự
 * kiện mới cho xem gợi ý thì mất tác dụng.
 */
export default function SuggestionBox({
  guestCount,
  eventType,
  eventDate,
  onPick,
  nhanNut = 'Chọn phương án này',
  tieuDe = 'Gợi ý cho bạn',
}) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const guests = Number(guestCount);
    if (!guests) {
      setItems([]);
      return undefined;
    }

    setLoading(true);
    const timer = setTimeout(() => {
      recommendationApi
        .suggest({
          guestCount: guests,
          eventType: eventType || null,
          eventDate: eventDate || null,
        })
        .then(setItems)
        .catch(() => setItems([]))
        .finally(() => setLoading(false));
    }, 400);

    return () => clearTimeout(timer);
  }, [guestCount, eventType, eventDate]);

  if (!Number(guestCount)) return null;

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
        {tieuDe}
      </div>
      <p className="muted" style={{ fontSize: '0.86rem', marginBottom: 14 }}>
        {eventType
          ? 'Dựa trên số khách, loại sự kiện, tầm giá gói tiệc và lịch sử đặt tiệc của khách trước.'
          : 'Đang gợi ý theo số khách. Chọn thêm loại sự kiện để gợi ý sát hơn.'}
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

              {/* Thực đơn gợi ý sẵn cho gói, gấp lại cho đỡ dài */}
              {item.menu?.length > 0 && (
                <details style={{ marginTop: 12 }}>
                  <summary style={{ cursor: 'pointer', fontSize: '0.84rem', fontWeight: 600 }}>
                    Thực đơn gợi ý ({item.menu.length} món)
                  </summary>
                  <ul style={{ paddingLeft: 16, marginTop: 8, fontSize: '0.82rem' }}>
                    {item.menu.map((dish) => (
                      <li key={dish.id} style={{ marginBottom: 4 }}>
                        {dish.name}
                        <span className="muted" style={{ display: 'block', fontSize: '0.76rem' }}>
                          {dish.categoryName}
                          {' · '}
                          {dish.price ? formatCurrency(dish.price) : dish.priceNote}
                        </span>
                      </li>
                    ))}
                  </ul>
                </details>
              )}

              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ marginTop: 12, width: '100%' }}
                onClick={() => onPick(item.spaceId, item.packageId)}
              >
                {nhanNut}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
