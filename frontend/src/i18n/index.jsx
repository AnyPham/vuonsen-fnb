import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import vi from './vi.json';
import en from './en.json';

const BANG_DICH = { vi, en };

export const DANH_SACH_NGON_NGU = [
  { ma: 'vi', ten: 'Tiếng Việt', ngan: 'VI', co: '🇻🇳' },
  { ma: 'en', ten: 'English', ngan: 'EN', co: '🇬🇧' },
];

const KHOA_LUU = 'vuonsen.lang';
const MAC_DINH = 'vi';

/*
 * Cố ý không tự đoán ngôn ngữ theo navigator.language.
 *
 * Rất nhiều máy của người Việt cài Windows bản tiếng Anh, đoán theo trình duyệt thì
 * khách người Việt mở web ra lại thấy tiếng Anh — hỏng hơn là không đoán. Khách nước
 * ngoài chủ động đổi bằng nút trên thanh đầu trang, chỉ cần đổi một lần vì lựa chọn
 * được nhớ lại ở lần sau.
 */
const docNgonNguDaLuu = () => {
  try {
    const luu = localStorage.getItem(KHOA_LUU);
    return BANG_DICH[luu] ? luu : MAC_DINH;
  } catch {
    // Trình duyệt chặn localStorage (chế độ ẩn danh) thì vẫn chạy được, chỉ là không nhớ
    return MAC_DINH;
  }
};

// Tra khóa dạng "nav.spaces" trong đối tượng lồng nhau
const tra = (nguon, khoa) => khoa.split('.').reduce((o, phan) => (o == null ? undefined : o[phan]), nguon);

// Thay {{ten}} trong câu bằng giá trị truyền vào
const thayBien = (chuoi, bien) => {
  if (!bien) return chuoi;
  return chuoi.replace(/\{\{(\w+)\}\}/g, (nguyen, ten) => (bien[ten] === undefined ? nguyen : String(bien[ten])));
};

const NgonNguContext = createContext(null);

export function LanguageProvider({ children }) {
  const [lang, setLang] = useState(docNgonNguDaLuu);

  useEffect(() => {
    try {
      localStorage.setItem(KHOA_LUU, lang);
    } catch {
      // không lưu được thì thôi, không phải lỗi đáng báo cho khách
    }
    /*
     * Đổi luôn thuộc tính lang của thẻ html.
     *
     * Không phải chi tiết làm cho đẹp: trình đọc màn hình chọn giọng đọc theo thuộc tính
     * này, để nguyên "vi" mà nội dung là tiếng Anh thì máy đọc tiếng Anh bằng giọng
     * tiếng Việt, nghe không ra chữ gì. Lighthouse cũng chấm đúng mục này.
     */
    document.documentElement.lang = lang;

    /*
     * Đổi cả tiêu đề tab và thẻ mô tả.
     *
     * Hai thứ này nằm ở index.html nên không tự đổi theo React. Để nguyên thì khách xem
     * bản tiếng Anh vẫn thấy tên tab tiếng Việt, mà đây cũng là dòng Google lấy làm tiêu
     * đề kết quả tìm kiếm.
     */
    const boDich = BANG_DICH[lang] || BANG_DICH[MAC_DINH];
    if (boDich.site) {
      document.title = boDich.site.title;
      const the = document.querySelector('meta[name="description"]');
      if (the) the.setAttribute('content', boDich.site.description);
    }
  }, [lang]);

  const t = useCallback(
    (khoa, bien) => {
      const cau = tra(BANG_DICH[lang], khoa);
      if (typeof cau === 'string') return thayBien(cau, bien);

      // Thiếu bản dịch thì lùi về tiếng Việt, không để giao diện trống
      const cauViet = tra(BANG_DICH[MAC_DINH], khoa);
      if (typeof cauViet === 'string') return thayBien(cauViet, bien);

      // Thiếu cả tiếng Việt là lỗi của người viết mã, trả chính khóa ra cho dễ thấy
      return khoa;
    },
    [lang],
  );

  /*
   * Lấy nội dung do cơ sở dữ liệu quản lý theo đúng ngôn ngữ.
   *
   * Backend trả về cả hai bản trong một lần gọi: name và nameEn. Chưa dịch thì cột tiếng
   * Anh để trống, hàm này tự lùi về bản tiếng Việt nên không bao giờ hiện ra ô rỗng.
   */
  const tDb = useCallback(
    (doiTuong, truong) => {
      if (!doiTuong) return '';
      if (lang !== 'vi') {
        const banDich = doiTuong[`${truong}En`];
        if (banDich) return banDich;
      }
      return doiTuong[truong] ?? '';
    },
    [lang],
  );

  // Dịch một mảng cụm từ ngắn (tiện ích, hạng mục gói tiệc) theo cùng quy tắc lùi về tiếng Việt
  const tDbList = useCallback(
    (viet, anh) => {
      if (lang === 'vi' || !Array.isArray(anh) || anh.length === 0) return viet || [];
      return (viet || []).map((cum, i) => anh[i] || cum);
    },
    [lang],
  );

  const giaTri = useMemo(() => ({ lang, setLang, t, tDb, tDbList }), [lang, t, tDb, tDbList]);

  return <NgonNguContext.Provider value={giaTri}>{children}</NgonNguContext.Provider>;
}

export function useI18n() {
  const ctx = useContext(NgonNguContext);
  if (!ctx) throw new Error('useI18n phải nằm trong LanguageProvider');
  return ctx;
}
