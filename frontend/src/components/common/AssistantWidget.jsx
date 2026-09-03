import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { assistantApi } from '@/api/endpoints';

const LOI_CHAO = {
  vaiTro: 'bot',
  noiDung:
    'Chào bạn, mình là trợ lý của Vườn Sen. Mình giúp được về không gian, thực đơn, '
    + 'gói tiệc, chi phí và cách đặt tiệc. Bạn cần hỏi gì ạ?',
  goiY: [
    'Sảnh nào chứa được 300 khách?',
    'Có gói tiệc nào?',
    'Chi phí một tiệc bao nhiêu?',
    'Đặt tiệc như thế nào?',
  ],
};

/*
 * Nhãn hiện dưới tên trợ lý, cho khách biết câu trả lời vừa rồi do đâu mà ra.
 *
 * Nói thật chuyện này có lợi cho cả hai phía: khách biết mình đang nói chuyện với
 * máy, còn lúc bảo vệ đồ án thì nhìn vào đây thấy ngay cơ chế dự phòng có chạy hay
 * không, khỏi phải mở nhật ký máy chủ ra dò.
 */
const NHAN_NGUON = {
  MO_HINH_NGON_NGU: 'Trả lời bằng mô hình ngôn ngữ, dựa trên dữ liệu nhà hàng',
  DU_PHONG: 'Trả lời từ dữ liệu nhà hàng',
};

/*
 * Hộp thoại trợ lý tư vấn, hiện ở góc dưới bên phải mọi trang.
 *
 * Câu trả lời do máy chủ dựng từ dữ liệu thật, phía giao diện chỉ hiển thị.
 * Mỗi câu trả lời kèm vài câu hỏi gợi ý để khách bấm tiếp, và có thể kèm một
 * đường dẫn tới trang liên quan.
 */
export default function AssistantWidget() {
  const [moRong, setMoRong] = useState(false);
  const [tinNhan, setTinNhan] = useState([LOI_CHAO]);
  const [dangGo, setDangGo] = useState('');
  const [dangCho, setDangCho] = useState(false);
  const [nguon, setNguon] = useState(null);

  const cuoiDanhSach = useRef(null);
  const oNhap = useRef(null);
  const navigate = useNavigate();

  // Luôn cuộn xuống tin nhắn mới nhất
  useEffect(() => {
    if (moRong) {
      cuoiDanhSach.current?.scrollIntoView({ block: 'end' });
    }
  }, [tinNhan, moRong, dangCho]);

  useEffect(() => {
    if (moRong) oNhap.current?.focus();
  }, [moRong]);

  // Nhấn Esc thì đóng hộp thoại
  useEffect(() => {
    if (!moRong) return undefined;
    const onEsc = (e) => {
      if (e.key === 'Escape') setMoRong(false);
    };
    document.addEventListener('keydown', onEsc);
    return () => document.removeEventListener('keydown', onEsc);
  }, [moRong]);

  const hoi = async (cauHoi) => {
    const cau = (cauHoi ?? dangGo).trim();
    if (!cau || dangCho) return;

    setTinNhan((truoc) => [...truoc, { vaiTro: 'khach', noiDung: cau }]);
    setDangGo('');
    setDangCho(true);

    try {
      const kq = await assistantApi.ask(cau);
      setNguon(kq.source ?? null);
      setTinNhan((truoc) => [...truoc, {
        vaiTro: 'bot',
        noiDung: kq.answer,
        goiY: kq.suggestions,
        link: kq.link,
        nhanLink: kq.linkLabel,
      }]);
    } catch (err) {
      setTinNhan((truoc) => [...truoc, {
        vaiTro: 'bot',
        noiDung: 'Xin lỗi, mình chưa trả lời được lúc này. Bạn thử lại giúp mình nhé.',
      }]);
    } finally {
      setDangCho(false);
    }
  };

  const moTrang = (duongDan) => {
    setMoRong(false);
    navigate(duongDan);
  };

  if (!moRong) {
    return (
      <button
        type="button"
        className="tro-ly-nut"
        aria-label="Mở trợ lý tư vấn"
        onClick={() => setMoRong(true)}
      >
        💬
      </button>
    );
  }

  return (
    <div className="tro-ly" role="dialog" aria-label="Trợ lý tư vấn Vườn Sen">
      <div className="tro-ly-dau">
        <div>
          <strong>Trợ lý Vườn Sen</strong>
          <div className="muted" style={{ fontSize: '0.76rem' }}>
            {NHAN_NGUON[nguon] || 'Trả lời dựa trên dữ liệu của nhà hàng'}
          </div>
        </div>
        <button type="button" aria-label="Đóng" onClick={() => setMoRong(false)}>
          ×
        </button>
      </div>

      <div className="tro-ly-than">
        {tinNhan.map((tn, i) => (
          <div key={i} className={`tro-ly-tin ${tn.vaiTro}`}>
            <div className="bong">{tn.noiDung}</div>

            {tn.link && (
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ marginTop: 8 }}
                onClick={() => moTrang(tn.link)}
              >
                {tn.nhanLink || 'Xem thêm'} →
              </button>
            )}

            {tn.goiY?.length > 0 && (
              <div className="tro-ly-goi-y">
                {tn.goiY.map((g) => (
                  <button key={g} type="button" onClick={() => hoi(g)} disabled={dangCho}>
                    {g}
                  </button>
                ))}
              </div>
            )}
          </div>
        ))}

        {dangCho && (
          <div className="tro-ly-tin bot">
            <div className="bong muted">Đang tìm câu trả lời…</div>
          </div>
        )}

        <div ref={cuoiDanhSach} />
      </div>

      <form
        className="tro-ly-chan"
        onSubmit={(e) => {
          e.preventDefault();
          hoi();
        }}
      >
        <input
          ref={oNhap}
          value={dangGo}
          onChange={(e) => setDangGo(e.target.value)}
          placeholder="Nhập câu hỏi của bạn…"
          maxLength={500}
          aria-label="Câu hỏi"
        />
        <button type="submit" className="btn btn-dark btn-sm" disabled={dangCho || !dangGo.trim()}>
          Gửi
        </button>
      </form>
    </div>
  );
}
