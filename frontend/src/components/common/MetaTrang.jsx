import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useI18n } from '@/i18n';

/*
 * Đặt tiêu đề tab, thẻ mô tả và thẻ canonical theo từng trang.
 *
 * Trước đây cả website dùng chung một tiêu đề và một mô tả lấy từ index.html, nên kết quả
 * tìm kiếm của trang thực đơn và trang đặt tiệc hiện y hệt nhau, người tìm không biết bấm
 * vào đâu. Mỗi trang một mô tả riêng thì dòng hiển thị trên Google mới nói đúng nội dung.
 *
 * Gom vào một thành phần đặt ở gốc ứng dụng thay vì gọi trong từng trang: 32 trang mà mỗi
 * trang tự gọi thì sót một trang là trang đó âm thầm dùng lại mô tả của trang trước.
 */

/* Bảng tra đường dẫn sang khóa dịch. Đường dẫn có tham số thì khớp bằng tiền tố. */
const TRANG = [
  ['/khong-gian/', 'chiTietKhongGian'],
  ['/thuc-don/', 'chiTietMon'],
];

const TRANG_CHINH_XAC = {
  '/': 'trangChu',
  '/khong-gian': 'khongGian',
  '/thuc-don': 'thucDon',
  '/goi-tiec': 'goiTiec',
  '/dat-tiec': 'datTiec',
  '/dat-mon': 'datMon',
  '/uu-dai': 'uuDai',
  '/thu-vien': 'thuVien',
  '/danh-gia': 'danhGia',
  '/tra-cuu': 'traCuu',
  '/tra-cuu-mon': 'traCuuMon',
  '/thanh-toan': 'thanhToan',
  '/dang-nhap': 'dangNhap',
  '/dang-ky': 'dangKy',
  '/quen-mat-khau': 'quenMatKhau',
  '/dat-lai-mat-khau': 'quenMatKhau',
  '/ho-so': 'hoSo',
  '/don-cua-toi': 'donCuaToi',
};

/*
 * Trang nội dung riêng của từng khách và khu quản trị thì bảo công cụ tìm kiếm đừng lập
 * chỉ mục, khớp với danh sách đã chặn trong robots.txt. Chặn ở hai nơi vì robots.txt chỉ
 * ngăn thu thập, còn thẻ noindex mới ngăn trang lọt vào kết quả qua đường dẫn bên ngoài.
 */
const KHONG_LAP_CHI_MUC = ['/quan-tri', '/tra-cuu', '/ho-so', '/don-cua-toi', '/dang-nhap', '/dang-ky', '/quen-mat-khau', '/dat-lai-mat-khau'];

function datThe(ten, noiDung, theoThuocTinh = 'name') {
  let the = document.querySelector(`meta[${theoThuocTinh}="${ten}"]`);
  if (!the) {
    the = document.createElement('meta');
    the.setAttribute(theoThuocTinh, ten);
    document.head.appendChild(the);
  }
  the.setAttribute('content', noiDung);
}

function datCanonical(duongDan) {
  let the = document.querySelector('link[rel="canonical"]');
  if (!the) {
    the = document.createElement('link');
    the.setAttribute('rel', 'canonical');
    document.head.appendChild(the);
  }
  the.setAttribute('href', window.location.origin + duongDan);
}

function khoaCuaDuongDan(duongDan) {
  if (TRANG_CHINH_XAC[duongDan]) return TRANG_CHINH_XAC[duongDan];
  const khop = TRANG.find(([tienTo]) => duongDan.startsWith(tienTo));
  if (khop) return khop[1];
  if (duongDan.startsWith('/quan-tri')) return 'quanTri';
  return null;
}

export default function MetaTrang() {
  const { pathname } = useLocation();
  const { t, lang } = useI18n();

  useEffect(() => {
    const khoa = khoaCuaDuongDan(pathname);
    const tenSite = t('site.title');

    if (khoa) {
      const tieuDe = t(`meta.${khoa}.title`);
      const moTa = t(`meta.${khoa}.description`);
      /* Tiêu đề trang đứng trước tên nhà hàng, vì Google cắt phần đuôi khi quá dài. */
      document.title = khoa === 'trangChu' ? tenSite : `${tieuDe} — Vườn Sen`;
      datThe('description', moTa);
      datThe('og:title', khoa === 'trangChu' ? tenSite : tieuDe, 'property');
      datThe('og:description', moTa, 'property');
    } else {
      document.title = tenSite;
      datThe('description', t('site.description'));
    }

    datCanonical(pathname);
    const an = KHONG_LAP_CHI_MUC.some((p) => pathname.startsWith(p));
    datThe('robots', an ? 'noindex, nofollow' : 'index, follow');
  }, [pathname, lang, t]);

  return null;
}
