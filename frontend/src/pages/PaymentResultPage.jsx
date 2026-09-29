import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { vnpayApi } from '@/api/endpoints';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

/*
 * Nơi VNPay đưa khách quay lại sau khi thanh toán.
 *
 * Trang này không tự kết luận thành công hay thất bại dựa vào tham số trên thanh địa chỉ, vì
 * ai cũng sửa được tham số đó. Nó gom toàn bộ tham số gửi sang máy chủ, máy chủ kiểm chữ ký
 * rồi mới trả lời, và chỉ tin vào câu trả lời của máy chủ.
 */
export default function PaymentResultPage() {
  const [thamSo] = useSearchParams();
  const [ketQua, setKetQua] = useState(null);
  const [dangKiem, setDangKiem] = useState(true);
  const [loi, setLoi] = useState(null);
  const { t, lang } = useI18n();
  const dd = useDinhDang();

  // Chặn gọi hai lần khi React dựng lại component ở chế độ phát triển
  const daGoi = useRef(false);

  useEffect(() => {
    if (daGoi.current) return;
    daGoi.current = true;

    const duLieu = {};
    thamSo.forEach((giaTri, khoa) => { duLieu[khoa] = giaTri; });

    if (!duLieu.vnp_TxnRef) {
      setLoi(t('paymentResult.missingRef'));
      setDangKiem(false);
      return;
    }

    vnpayApi
      .xacNhan(duLieu)
      .then(setKetQua)
      .catch((e) => setLoi(e.message || t('paymentResult.verifyFailed')))
      .finally(() => setDangKiem(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [thamSo]);

  const daNhanTien = ketQua?.status === 'CONFIRMED';

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 620 }}>
        <div className="section-head center">
          <div className="eyebrow center">{t('paymentResult.eyebrow')}</div>
          <h2>{t('paymentResult.title')}</h2>
        </div>

        {dangKiem && <Loading label={t('paymentResult.verifying')} />}
        {loi && <ErrorBlock message={loi} />}

        {ketQua && (
          <div className="card" data-test="vnpay-result">
            <div className="card-body">
              <span className={`tag tag-${daNhanTien ? 'COMPLETED' : 'CANCELLED'}`} data-test="vnpay-status">
                {lang === 'en' ? ketQua.statusLabelEn || ketQua.statusLabel : ketQua.statusLabel}
              </span>

              <h3 style={{ marginTop: 14 }}>
                {daNhanTien ? t('paymentResult.success') : t('paymentResult.failed')}
              </h3>

              <table style={{ marginTop: 16 }}>
                <tbody>
                  <tr>
                    <th>{t('paymentResult.receiptCode')}</th>
                    <td>{ketQua.code}</td>
                  </tr>
                  <tr>
                    <th>{t('paymentResult.order')}</th>
                    <td>{ketQua.orderCode}</td>
                  </tr>
                  <tr>
                    <th>{t('paymentResult.purpose')}</th>
                    <td>{lang === 'en' ? ketQua.purposeLabelEn || ketQua.purposeLabel : ketQua.purposeLabel}</td>
                  </tr>
                  <tr>
                    <th>{t('paymentResult.amount')}</th>
                    <td><strong>{dd.tien(ketQua.amount)}</strong></td>
                  </tr>
                  {ketQua.reference && (
                    <tr>
                      <th>{t('paymentResult.reference')}</th>
                      <td>{ketQua.reference}</td>
                    </tr>
                  )}
                </tbody>
              </table>

              <p className="muted" style={{ fontSize: '0.9rem', marginTop: 16 }}>
                {daNhanTien ? t('paymentResult.successNote') : t('paymentResult.failedNote')}
              </p>

              <div style={{ display: 'flex', gap: 10, marginTop: 18, flexWrap: 'wrap' }}>
                <Link to={`/thanh-toan?ma=${ketQua.orderCode}`} className="btn btn-outline btn-sm">
                  {t('paymentResult.viewStatus')}
                </Link>
                <Link to="/" className="btn btn-ghost btn-sm">
                  {t('common.backHome')}
                </Link>
              </div>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
