import { useMemo } from 'react';
import { useI18n } from '@/i18n';
import { formatCurrency, formatDate, formatDateTime } from '@/utils/format';

// Bộ định dạng đã gắn sẵn ngôn ngữ đang chọn, để trong trang khỏi phải truyền lang từng chỗ
export function useDinhDang() {
  const { lang } = useI18n();
  return useMemo(
    () => ({
      tien: (v) => formatCurrency(v, lang),
      ngay: (v) => formatDate(v, lang),
      ngayGio: (v) => formatDateTime(v, lang),
    }),
    [lang],
  );
}
