/*
 * Ba khối lặp lại ở mọi màn hình: đang tải, lỗi, rỗng.
 *
 * Thuộc tính data-test là điểm neo cho kiểm thử tự động bằng Selenium. Đặt ở đây thay
 * vì ở từng trang, vì ba khối này dùng chung khắp ứng dụng: đánh dấu một chỗ là mọi
 * màn hình đều tra được trạng thái tải, lỗi và rỗng theo cùng một locator.
 *
 * Không đổi các giá trị data-test khi sửa giao diện: mã kiểm thử bám vào đó.
 */
import { useI18n } from '@/i18n';

export function Loading({ label }) {
  const { t } = useI18n();
  return <div className="state" data-test="loading">{label || t('common.loading')}</div>;
}

export function ErrorBlock({ message }) {
  const { t } = useI18n();
  return (
    <div className="alert alert-error" data-test="error">
      {message || t('common.error')}
    </div>
  );
}

export function Empty({ label }) {
  const { t } = useI18n();
  return <div className="state" data-test="empty">{label || t('common.empty')}</div>;
}
