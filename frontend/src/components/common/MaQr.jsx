import { useEffect, useRef, useState } from 'react';
import QRCode from 'qrcode';

/*
 * Vẽ một chuỗi thành ảnh mã QR ngay trong trình duyệt.
 *
 * Vẽ tại chỗ chứ không gọi dịch vụ sinh ảnh QR bên ngoài: không phụ thuộc một dịch vụ có
 * thể chết, không gửi số tài khoản và số tiền của nhà hàng ra ngoài, và mã vẫn hiện được
 * khi máy không ra được internet.
 *
 * Mức sửa lỗi đặt ở M: mã QR chịu được khoảng 15% diện tích bị che hoặc mờ, đủ cho trường
 * hợp khách chụp màn hình rồi quét lại từ ảnh chụp.
 */
export default function MaQr({ chuoi, kichThuoc = 240, alt = 'Mã QR chuyển khoản' }) {
  const anhRef = useRef(null);
  const [loi, setLoi] = useState(null);

  useEffect(() => {
    if (!chuoi || !anhRef.current) return;

    QRCode.toDataURL(chuoi, {
      errorCorrectionLevel: 'M',
      margin: 1,
      width: kichThuoc,
      color: { dark: '#14301f', light: '#ffffff' },
    })
      .then((duLieuAnh) => {
        if (anhRef.current) anhRef.current.src = duLieuAnh;
        setLoi(null);
      })
      .catch(() => setLoi('Không vẽ được mã QR, bạn chuyển khoản thủ công theo thông tin bên dưới.'));
  }, [chuoi, kichThuoc]);

  if (!chuoi) return null;

  return (
    <div style={{ textAlign: 'center' }}>
      <img
        ref={anhRef}
        alt={alt}
        data-test="qr-image"
        width={kichThuoc}
        height={kichThuoc}
        style={{
          width: kichThuoc,
          height: kichThuoc,
          background: '#fff',
          borderRadius: 12,
          border: '1px solid var(--line)',
        }}
      />
      {loi && <p className="err">{loi}</p>}
    </div>
  );
}
