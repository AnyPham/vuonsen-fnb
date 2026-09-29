import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { paymentApi, vnpayApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import { ErrorBlock, Loading } from '@/components/common/StateBlock';
import MaQr from '@/components/common/MaQr';

const NHAN_TRANG_THAI = {
  CHUA_TRA: 'tag-PENDING',
  COC_THIEU: 'tag-PENDING',
  DA_COC: 'tag-CONFIRMED',
  DA_TRA_DU: 'tag-COMPLETED',
};

/*
 * Trang thanh toán của khách.
 *
 * Nhập mã đơn là xem được đơn còn nợ bao nhiêu, bấm một nút là có mã QR chuyển khoản mang
 * sẵn số tiền và nội dung. Không bắt đăng nhập, giống trang tra cứu đơn: có mã đơn thì coi
 * như là chủ đơn. Đường dẫn phía máy chủ chỉ tạo phiếu chờ đối soát chứ không tự cộng tiền,
 * nên biết mã đơn cũng không tự biến đơn thành đã thanh toán được.
 */
export default function PaymentPage() {
  const [thamSo] = useSearchParams();
  const [maDon, setMaDon] = useState(thamSo.get('ma') || '');
  const [tinhHinh, setTinhHinh] = useState(null);
  const [dangTai, setDangTai] = useState(false);
  const [loi, setLoi] = useState(null);

  // Cổng VNPay chỉ hiện khi máy chủ đã được cấu hình, chưa cấu hình thì ẩn hẳn cho gọn
  const [coVnPay, setCoVnPay] = useState(false);

  const tra = useCallback(async (ma) => {
    if (!ma || !ma.trim()) {
      setLoi(t('payment.needCode'));
      return;
    }
    setDangTai(true);
    setLoi(null);
    try {
      setTinhHinh(await paymentApi.tinhHinh(ma.trim()));
    } catch (e) {
      setTinhHinh(null);
      setLoi(e.message || t('payment.lookupFailed'));
    } finally {
      setDangTai(false);
    }
  }, []);

  // Vào từ đường dẫn có sẵn mã đơn thì tra luôn, khách khỏi bấm thêm một nhịp
  useEffect(() => {
    const ma = thamSo.get('ma');
    if (ma) tra(ma);
  }, [thamSo, tra]);

  // Hỏi máy chủ xem cổng VNPay đã mở chưa. Hỏi hỏng thì coi như chưa mở, ẩn nút đi cho lành
  useEffect(() => {
    vnpayApi
      .trangThai()
      .then((kq) => setCoVnPay(!!kq.daMoCong))
      .catch(() => setCoVnPay(false));
  }, []);

  /*
   * Chuyển sang cổng VNPay.
   *
   * Máy chủ trả về địa chỉ, trình duyệt tự chuyển cả trang sang đó. Chuyển cả trang chứ không
   * mở tab mới, vì cổng thanh toán cần đưa khách quay lại đúng trang kết quả sau khi trả xong.
   */
  const traQuaVnPay = async (mucDich) => {
    setDangTai(true);
    setLoi(null);
    try {
      const kq = await vnpayApi.tao(tinhHinh.maDon, mucDich);
      window.location.href = kq.payUrl;
    } catch (e) {
      setLoi(e.message || t('payment.gatewayFailed'));
      setDangTai(false);
    }
  };

  const taoYeuCau = async (mucDich) => {
    setDangTai(true);
    setLoi(null);
    try {
      setTinhHinh(await paymentApi.taoYeuCau(tinhHinh.maDon, mucDich));
    } catch (e) {
      setLoi(e.message || t('payment.requestFailed'));
    } finally {
      setDangTai(false);
    }
  };

  // Chỉ có ở chế độ chạy thử: đi hết luồng mà không phải chuyển tiền thật
  const giaLapBaoCo = async () => {
    setDangTai(true);
    setLoi(null);
    try {
      setTinhHinh(await paymentApi.giaLapBaoCo(tinhHinh.maDon));
    } catch (e) {
      setLoi(e.message || t('payment.simulateFailed'));
    } finally {
      setDangTai(false);
    }
  };

  const { t, lang } = useI18n();
  const dd = useDinhDang();

  // Nhãn enum do backend gửi kèm cả hai thứ tiếng
  const nhan = (viet, anh) => (lang === 'en' && anh ? anh : viet);

  const ck = tinhHinh?.chuyenKhoan;
  const conNo = tinhHinh && Number(tinhHinh.conPhaiTra) > 0;
  const chuaDuCoc = tinhHinh && Number(tinhHinh.daThu) < Number(tinhHinh.canCoc);

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 860 }}>
        <div className="section-head">
          <div className="eyebrow">{t('paymentResult.eyebrow')}</div>
          <h2>{t('payment.title')}</h2>
          <p className="muted">
            {t('payment.desc')}
          </p>
        </div>

        <form
          className="card"
          style={{ marginBottom: 24 }}
          onSubmit={(e) => {
            e.preventDefault();
            tra(maDon);
          }}
        >
          <div className="card-body form-row">
            <div className="fgroup" style={{ marginBottom: 0 }}>
              <label htmlFor="ma-don">{t('payment.codeLabel')}</label>
              <input
                id="ma-don"
                data-test="payment-code"
                value={maDon}
                placeholder={t('payment.codePlaceholder')}
                onChange={(e) => setMaDon(e.target.value)}
              />
            </div>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button type="submit" className="btn btn-dark" data-test="payment-search">
                {t('track.submit')}
              </button>
            </div>
          </div>
        </form>

        {loi && <ErrorBlock message={loi} />}
        {dangTai && <Loading />}

        {tinhHinh && (
          <>
            <div className="card" style={{ marginBottom: 24 }}>
              <div className="card-body">
                <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 10 }}>
                  <div>
                    <h3 style={{ marginBottom: 4 }}>{tinhHinh.maDon}</h3>
                    <p className="muted" style={{ fontSize: '0.9rem' }}>
                      {nhan(tinhHinh.loaiDon, tinhHinh.loaiDonEn)} · {tinhHinh.tenKhach}
                    </p>
                  </div>
                  <span
                    className={`tag ${NHAN_TRANG_THAI[tinhHinh.trangThai] || 'tag-PENDING'}`}
                    data-test="payment-status"
                  >
                    {t('paymentStatus.' + tinhHinh.trangThai)}
                  </span>
                </div>

                <table style={{ marginTop: 18 }}>
                  <tbody>
                    <tr>
                      <th>{t('payment.total')}</th>
                      <td>{dd.tien(tinhHinh.tongTien)}</td>
                    </tr>
                    {Number(tinhHinh.canCoc) > 0 && (
                      <tr>
                        <th>{t('payment.depositDue')}</th>
                        <td>{dd.tien(tinhHinh.canCoc)}</td>
                      </tr>
                    )}
                    <tr>
                      <th>{t('payment.paid')}</th>
                      <td data-test="payment-paid">{dd.tien(tinhHinh.daThu)}</td>
                    </tr>
                    <tr>
                      <th>{t('payment.outstanding')}</th>
                      <td>
                        <strong>{dd.tien(tinhHinh.conPhaiTra)}</strong>
                      </td>
                    </tr>
                  </tbody>
                </table>

                {conNo && !tinhHinh.phieuChoDoiSoat && (
                  <div style={{ display: 'flex', gap: 10, marginTop: 18, flexWrap: 'wrap' }}>
                    {chuaDuCoc && Number(tinhHinh.canCoc) > 0 && (
                      <button
                        type="button"
                        className="btn btn-gold"
                        onClick={() => taoYeuCau('DEPOSIT')}
                        data-test="pay-deposit"
                      >
                        {t('payment.payDeposit', { tien: dd.tien(Number(tinhHinh.canCoc) - Number(tinhHinh.daThu)) })}
                      </button>
                    )}
                    <button
                      type="button"
                      className="btn btn-outline"
                      onClick={() => taoYeuCau(Number(tinhHinh.canCoc) > 0 ? 'BALANCE' : 'FULL')}
                      data-test="pay-full"
                    >
                      {t('payment.payAll', { tien: dd.tien(tinhHinh.conPhaiTra) })}
                    </button>

                    {/* Cổng thanh toán tự động, chỉ hiện khi nhà hàng đã khai báo VNPay */}
                    {coVnPay && (
                      <button
                        type="button"
                        className="btn btn-dark"
                        onClick={() => traQuaVnPay(
                          chuaDuCoc && Number(tinhHinh.canCoc) > 0 ? 'DEPOSIT' : 'BALANCE')}
                        data-test="pay-vnpay"
                      >
                        {t('payment.payVnpay')}
                      </button>
                    )}
                  </div>
                )}

                {!conNo && (
                  <p className="muted" style={{ marginTop: 18 }}>
                    {t('payment.settled')}
                  </p>
                )}
              </div>
            </div>

            {tinhHinh.phieuChoDoiSoat && ck && (
              <div className="card" style={{ marginBottom: 24 }} data-test="payment-qr-block">
                <div className="card-body">
                  <h3 style={{ marginBottom: 6 }}>{t('payment.qrTitle')}</h3>
                  <p className="muted" style={{ fontSize: '0.9rem', marginBottom: 18 }}>
                    {t('payment.qrDesc')}
                  </p>

                  {ck.chayThu && (
                    <div
                      className="err"
                      style={{ marginBottom: 16, padding: 12, borderRadius: 10, background: '#fdf0f0' }}
                      data-test="sandbox-warning"
                    >
                      {t('payment.sandboxWarning')}
                    </div>
                  )}

                  <div
                    style={{
                      display: 'grid',
                      gap: 24,
                      gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
                      alignItems: 'center',
                    }}
                  >
                    <MaQr chuoi={ck.chuoiQr} />

                    <table>
                      <tbody>
                        <tr>
                          <th>{t('payment.bank')}</th>
                          <td>{ck.nganHang}</td>
                        </tr>
                        <tr>
                          <th>{t('payment.accountNumber')}</th>
                          <td data-test="bank-account">{ck.soTaiKhoan}</td>
                        </tr>
                        <tr>
                          <th>{t('payment.accountName')}</th>
                          <td>{ck.tenChuTaiKhoan}</td>
                        </tr>
                        <tr>
                          <th>{t('payment.amount')}</th>
                          <td>
                            <strong>{dd.tien(ck.soTien)}</strong>
                          </td>
                        </tr>
                        <tr>
                          <th>{t('payment.transferNote')}</th>
                          <td>
                            <strong>{ck.noiDung}</strong>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <p className="muted" style={{ fontSize: '0.88rem', marginTop: 18 }}>
                    {t('payment.afterTransfer')}
                  </p>

                  {ck.chayThu && (
                    <button
                      type="button"
                      className="btn btn-dark btn-sm"
                      style={{ marginTop: 14 }}
                      onClick={giaLapBaoCo}
                      data-test="sandbox-confirm"
                    >
                      {t('payment.simulate')}
                    </button>
                  )}
                </div>
              </div>
            )}

            {tinhHinh.lichSu.length > 0 && (
              <div className="card">
                <div className="card-body">
                  <h3 style={{ marginBottom: 14 }}>{t('payment.historyTitle')}</h3>
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr>
                          <th>{t('payment.receiptCode')}</th>
                          <th>{t('payment.purpose')}</th>
                          <th>{t('payment.amount')}</th>
                          <th>{t('payment.method')}</th>
                          <th>{t('payment.status')}</th>
                          <th>{t('payment.time')}</th>
                        </tr>
                      </thead>
                      <tbody>
                        {tinhHinh.lichSu.map((p) => (
                          <tr key={p.id} data-test="payment-row">
                            <td>{p.code}</td>
                            <td>{nhan(p.purposeLabel, p.purposeLabelEn)}</td>
                            <td>{dd.tien(p.amount)}</td>
                            <td>{nhan(p.methodLabel, p.methodLabelEn)}</td>
                            <td>
                              <span className={`tag tag-${p.status === 'CONFIRMED' ? 'COMPLETED' : 'PENDING'}`}>
                                {nhan(p.statusLabel, p.statusLabelEn)}
                              </span>
                            </td>
                            <td>{dd.ngayGio(p.confirmedAt || p.createdAt)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </section>
  );
}
