import { Link, useLocation } from 'react-router-dom';
import { useI18n } from '@/i18n';

/*
 * Khối mời đăng nhập, hiện thay cho nút gửi đơn khi khách chưa có phiên.
 *
 * Vì sao chặn ngay ở giao diện thay vì để khách bấm gửi rồi nhận lỗi 401: khách điền xong
 * cả biểu mẫu ba bước mới biết mình không gửi được là mất công vô ích, và thông báo lỗi
 * kỹ thuật thì không nói được phải làm gì tiếp.
 *
 * Chặn ở đây chỉ là lớp tiện lợi. Chặn thật nằm ở máy chủ: hai đường POST tạo đơn đều
 * đòi đăng nhập, nên gọi thẳng API cũng không qua được.
 *
 * Đường dẫn hiện tại được gửi kèm sang trang đăng nhập, để đăng nhập xong khách quay lại
 * đúng chỗ đang dở chứ không bị ném về trang chủ.
 */
export default function CanDangNhap({ moTa }) {
  const { t } = useI18n();
  const viTri = useLocation();
  const quayLai = encodeURIComponent(viTri.pathname + viTri.search);

  return (
    <div className="card" data-test="can-dang-nhap" style={{ marginTop: 8 }}>
      <div className="card-body">
        <p style={{ marginTop: 0 }}>{moTa || t('auth.needLoginToOrder')}</p>
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <Link
            to={`/dang-nhap?quay-lai=${quayLai}`}
            className="btn btn-dark"
            data-test="can-dang-nhap-login"
          >
            {t('login.title')}
          </Link>
          <Link
            to={`/dang-ky?quay-lai=${quayLai}`}
            className="btn btn-outline"
            data-test="can-dang-nhap-register"
          >
            {t('register.title')}
          </Link>
        </div>
      </div>
    </div>
  );
}
