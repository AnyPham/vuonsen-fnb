const vnd = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
});

// 15000000 thành "15.000.000 ₫"
export const formatCurrency = (value) => {
  if (value === null || value === undefined || value === '') return '—';
  return vnd.format(Number(value));
};

// "2026-08-15" thành "15/08/2026"
export const formatDate = (value) => {
  if (!value) return '—';
  return new Date(value).toLocaleDateString('vi-VN');
};

export const formatDateTime = (value) => {
  if (!value) return '—';
  return new Date(value).toLocaleString('vi-VN');
};

// Ngày sớm nhất được đặt tiệc là ngày mai
export const tomorrowISO = () => {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().split('T')[0];
};
