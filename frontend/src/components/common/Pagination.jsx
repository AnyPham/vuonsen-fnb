/*
 * Thanh phân trang dùng chung cho mọi danh sách có nhiều dòng.
 *
 * Nhận nguyên đối tượng phân trang của máy chủ, dạng { page, size, totalElements, totalPages }.
 * Chỉ có một trang thì không vẽ gì, để trang nào ít dữ liệu vẫn gọn như cũ.
 *
 * Luôn hiện tổng số dòng, vì thiếu con số đó thì người dùng không biết mình đang xem phần nào
 * của bao nhiêu, cũng không biết còn dữ liệu phía sau hay không.
 */
import { useI18n } from '@/i18n';

export default function Pagination({ trang, doiTrang, donVi }) {
  const { t } = useI18n();
  const nhanDonVi = donVi || t('common.rows');
  if (!trang || trang.totalElements === 0) return null;

  const hienTai = trang.page ?? 0;
  const tong = trang.totalPages ?? 1;
  const tu = hienTai * (trang.size || 0) + 1;
  const den = Math.min(tu + (trang.content?.length || 0) - 1, trang.totalElements);

  return (
    <div
      className="phan-trang"
      data-test="pagination"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: 14,
        flexWrap: 'wrap',
        marginTop: 20,
      }}
    >
      <span className="muted" style={{ fontSize: '0.88rem' }} data-test="pagination-summary">
        {t('pagination.summary', { tu, den, tong: trang.totalElements, donVi: nhanDonVi })}
      </span>

      {tong > 1 && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <button
            type="button"
            className="btn btn-sm btn-outline"
            data-test="pagination-prev"
            disabled={hienTai <= 0}
            onClick={() => doiTrang(hienTai - 1)}
          >
            {t('pagination.prev')}
          </button>

          <span className="muted" style={{ fontSize: '0.88rem' }}>
            {t('pagination.position', { hienTai: hienTai + 1, tong })}
          </span>

          <button
            type="button"
            className="btn btn-sm btn-outline"
            data-test="pagination-next"
            disabled={hienTai >= tong - 1}
            onClick={() => doiTrang(hienTai + 1)}
          >
            {t('pagination.next')}
          </button>
        </div>
      )}
    </div>
  );
}
