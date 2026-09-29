import { useEffect, useRef, useState } from 'react';
import { DANH_SACH_NGON_NGU, useI18n } from '@/i18n';

/*
 * Ô chọn ngôn ngữ ở góc phải thanh đầu trang.
 *
 * Đặt ngoài thẻ nav chứ không nằm trong danh sách mục menu: trên điện thoại cả menu bị
 * thu vào nút ba gạch, để bên trong thì khách nước ngoài phải mở menu ra mới thấy, mà
 * họ chưa đọc được tiếng Việt thì không biết nút nào để bấm. Để ngoài thì ở màn hình
 * nào nút này cũng hiện sẵn cạnh nút ba gạch.
 */
export default function LanguageSwitcher() {
  const { lang, setLang, t } = useI18n();
  const [open, setOpen] = useState(false);
  const boc = useRef(null);

  const dangChon = DANH_SACH_NGON_NGU.find((n) => n.ma === lang) || DANH_SACH_NGON_NGU[0];

  // Bấm ra ngoài hoặc nhấn Esc thì đóng, giống menu quản trị
  useEffect(() => {
    if (!open) return undefined;

    const bamNgoai = (e) => {
      if (boc.current && !boc.current.contains(e.target)) setOpen(false);
    };
    const nhanEsc = (e) => {
      if (e.key === 'Escape') setOpen(false);
    };

    document.addEventListener('mousedown', bamNgoai);
    document.addEventListener('keydown', nhanEsc);
    return () => {
      document.removeEventListener('mousedown', bamNgoai);
      document.removeEventListener('keydown', nhanEsc);
    };
  }, [open]);

  const chon = (ma) => {
    setLang(ma);
    setOpen(false);
  };

  return (
    <div className={`dropdown lang-switch ${open ? 'open' : ''}`} ref={boc}>
      <button
        type="button"
        aria-expanded={open}
        aria-haspopup="listbox"
        aria-label={t('lang.choose')}
        title={t('lang.choose')}
        data-test="lang-switch"
        onClick={() => setOpen((v) => !v)}
      >
        <span aria-hidden="true">🌐</span>
        <span className="lang-ma">{dangChon.ngan}</span>
        <span aria-hidden="true" className="lang-mui">▾</span>
      </button>

      {open && (
        <div className="dropdown-panel" role="listbox" aria-label={t('lang.label')}>
          {DANH_SACH_NGON_NGU.map((n) => (
            <button
              key={n.ma}
              type="button"
              role="option"
              aria-selected={n.ma === lang}
              className={`lang-muc ${n.ma === lang ? 'dang-chon' : ''}`}
              data-test={`lang-option-${n.ma}`}
              onClick={() => chon(n.ma)}
            >
              <span aria-hidden="true">{n.co}</span>
              <span>{n.ten}</span>
              {n.ma === lang && <span aria-hidden="true" className="lang-dau">✓</span>}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
