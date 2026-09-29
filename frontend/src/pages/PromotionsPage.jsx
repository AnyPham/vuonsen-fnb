import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { holidayApi } from '@/api/endpoints';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';

// 0.15 thành "15%"
const phanTram = (ti) => `${Math.round(Number(ti) * 100)}%`;

/*
 * Dịp đang diễn ra thì hiện nhãn khác dịp còn ở tương lai, vì hai việc khách cần làm khác
 * nhau: một bên đặt được ngay, một bên phải chờ tới ngày.
 */
function tinhTrang(dip, t) {
  const homNay = new Date();
  homNay.setHours(0, 0, 0, 0);
  const batDau = new Date(dip.startDate);
  const ketThuc = new Date(dip.endDate);

  if (batDau <= homNay && homNay <= ketThuc) {
    return { nhan: t('promotions.running'), lop: 'tag-CONFIRMED' };
  }
  const conLai = Math.ceil((batDau - homNay) / 86400000);
  return {
    nhan: conLai <= 30 ? t('promotions.daysLeft', { n: conLai }) : t('promotions.upcoming'),
    lop: 'tag-PENDING',
  };
}

// Trang ưu đãi: các dịp lễ đang giảm giá, để khách chủ động chọn ngày tổ chức
export default function PromotionsPage() {
  const [dsDip, setDsDip] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState(null);
  const { t, tDb } = useI18n();
  const dd = useDinhDang();

  useEffect(() => {
    holidayApi
      .list()
      .then(setDsDip)
      .catch((e) => setLoi(e.message))
      .finally(() => setDangTai(false));
  }, []);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">{t('promotions.eyebrow')}</div>
          <h2>{t('promotions.title')}</h2>
          <p className="muted">
            {t('promotions.desc')}
          </p>
        </div>

        {dangTai && <Loading />}
        {loi && <ErrorBlock message={loi} />}
        {!dangTai && !loi && dsDip.length === 0 && (
          <Empty label={t('promotions.empty')} />
        )}

        {dsDip.length > 0 && (
          <div className="grid grid-3">
            {dsDip.map((dip) => {
              const tt = tinhTrang(dip, t);
              return (
                <article key={dip.id} className="card" data-test="promotion-card">
                  <div className="card-body">
                    <span className={`tag ${tt.lop}`}>{tt.nhan}</span>
                    <h3 style={{ marginTop: 12 }}>{tDb(dip, 'name')}</h3>
                    <div
                      style={{
                        fontFamily: 'var(--serif)',
                        fontSize: '2.4rem',
                        color: 'var(--green-800)',
                        margin: '10px 0',
                      }}
                      data-test="promotion-rate"
                    >
                      −{phanTram(dip.discountRate)}
                    </div>
                    <p className="muted" style={{ fontSize: '0.92rem' }}>
                      {t('promotions.range', { tu: dd.ngay(dip.startDate), den: dd.ngay(dip.endDate) })}
                    </p>
                    <Link
                      to={`/dat-tiec?ngay=${dip.startDate}`}
                      className="btn btn-outline btn-sm"
                      style={{ marginTop: 14 }}
                    >
                      {t('promotions.book')}
                    </Link>
                  </div>
                </article>
              );
            })}
          </div>
        )}

        <div className="card" style={{ marginTop: 32 }}>
          <div className="card-body">
            <h3 style={{ marginBottom: 10 }}>{t('promotions.othersTitle')}</h3>
            <ul style={{ paddingLeft: 18, fontSize: '0.94rem', lineHeight: 1.9 }}>
              <li>{t('promotions.other1')}</li>
              <li>{t('promotions.other2')}</li>
              <li>{t('promotions.other3')}</li>
            </ul>
            <p className="muted" style={{ fontSize: '0.88rem', marginTop: 12 }}>
              {t('promotions.noStack')}
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
