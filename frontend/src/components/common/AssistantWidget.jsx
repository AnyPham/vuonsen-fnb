import { useEffect, useRef, useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { assistantApi } from '@/api/endpoints';
import { selectUser } from '@/features/auth/authSlice';

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
  const daXinLoiChao = useRef(false);
  const navigate = useNavigate();
  const nguoiDung = useSelector(selectUser);

  /*
   * Đổi người đăng nhập thì xoá sạch hộp thoại.
   *
   * Đây là sửa một lỗi bảo mật thật. Hộp thoại nằm trong khung chung của mọi trang nên
   * không bị dựng lại khi khách đăng xuất; nếu không xoá tay thì lời chào kèm tên khách,
   * danh sách sảnh họ từng đặt và món họ từng gọi vẫn nằm nguyên trên màn hình cho người
   * tiếp theo đọc. Trên máy dùng chung thì đó là lộ lịch sử giao dịch của người khác.
   *
   * Xoá cả khi đăng nhập lẫn khi đăng xuất, vì cả hai đều là đổi danh tính. Đặt lại cờ
   * daXinLoiChao để lần mở kế tiếp xin lại màn mở đầu đúng với người mới.
   */
  const nguoiTruoc = useRef(nguoiDung?.id ?? null);
  useEffect(() => {
    const hienTai = nguoiDung?.id ?? null;
    if (nguoiTruoc.current === hienTai) return;
    nguoiTruoc.current = hienTai;

    setTinNhan([LOI_CHAO]);
    setNguon(null);
    daXinLoiChao.current = false;
  }, [nguoiDung?.id]);

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

  /*
   * Lần đầu mở hộp thoại thì xin máy chủ màn mở đầu đã cá nhân hóa.
   *
   * Gọi lúc mở chứ không gọi lúc nạp trang: phần lớn khách không mở hộp thoại, gọi sẵn
   * cho mọi lượt truy cập là tốn truy vấn vô ích.
   *
   * Chỉ gọi một lần. Khách đóng rồi mở lại thì giữ nguyên cuộc trò chuyện đang có, dựng
   * lại lời chào sẽ xóa mất những gì vừa hỏi.
   *
   * Hỏng thì im lặng giữ lời chào mặc định: không chào được đúng tên là chuyện nhỏ,
   * không đáng để chặn khách đặt câu hỏi.
   */
  useEffect(() => {
    if (!moRong || daXinLoiChao.current) return;
    daXinLoiChao.current = true;

    assistantApi.loiMoDau()
      .then((kq) => {
        setTinNhan((truoc) => {
          /* Khách gõ nhanh hơn mạng thì đừng đạp lên cuộc trò chuyện đã bắt đầu. */
          if (truoc.length > 1) return truoc;
          return [{
            vaiTro: 'bot',
            noiDung: kq.loiChao,
            goiY: kq.cauHoiGoiY,
            nhom: [
              { nhan: kq.khachQuen ? 'Nơi bạn từng đặt' : 'Được đặt nhiều nhất', muc: kq.sanh },
              { nhan: 'Món bạn từng gọi', muc: kq.monDaGoi },
              { nhan: 'Bạn đang quan tâm', muc: kq.dangQuanTam },
              { nhan: 'Cùng loại bạn hay gọi', muc: kq.monTuongTu },
              { nhan: 'Gói tiệc phổ biến', muc: kq.goiTiec },
              { nhan: 'Món mới', muc: kq.monMoi },
            ].filter((n) => n.muc?.length > 0),
          }];
        });
      })
      .catch(() => { /* giữ lời chào mặc định */ });
  }, [moRong]);

  const hoi = async (cauHoi) => {
    const cau = (cauHoi ?? dangGo).trim();
    if (!cau || dangCho) return;

    // Gửi kèm các lượt hỏi đáp trước để trợ lý hiểu câu hỏi nối tiếp. Bỏ lời chào mặc định
    // vì đó không phải câu trợ lý đã trả lời khách.
    const lichSu = tinNhan
      .filter((tn) => tn !== LOI_CHAO)
      .slice(-10)
      .map((tn) => ({ role: tn.vaiTro === 'khach' ? 'user' : 'assistant', content: tn.noiDung }));

    setTinNhan((truoc) => [...truoc, { vaiTro: 'khach', noiDung: cau }]);
    setDangGo('');
    setDangCho(true);

    try {
      const kq = await assistantApi.ask(cau, lichSu);
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
        data-test="assistant-open"
        onClick={() => setMoRong(true)}
      >
        💬
      </button>
    );
  }

  return (
    <div className="tro-ly" role="dialog" aria-label="Trợ lý tư vấn Vườn Sen" data-test="assistant">
      <div className="tro-ly-dau">
        <div>
          <strong>Trợ lý Vườn Sen</strong>
          <div className="muted" style={{ fontSize: '0.76rem' }}>
            {NHAN_NGUON[nguon] || 'Trả lời dựa trên dữ liệu của nhà hàng'}
          </div>
        </div>
        <button type="button" aria-label="Đóng" onClick={() => setMoRong(false)} data-test="assistant-close">
          ×
        </button>
      </div>

      <div className="tro-ly-than">
        {tinNhan.map((tn, i) => (
          <div key={i} className={`tro-ly-tin ${tn.vaiTro}`} data-test={`assistant-msg-${tn.vaiTro}`}>
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

            {/*
              * Các nhóm gợi ý của màn mở đầu: nơi từng đặt, thứ đang quan tâm, gói phổ
              * biến, món mới. Mỗi mục là một thẻ bấm được dẫn thẳng tới trang liên quan,
              * để khách đi tiếp bằng một cú bấm thay vì phải gõ câu hỏi.
              */}
            {tn.nhom?.map((n) => (
              <div key={n.nhan} className="tro-ly-nhom" data-test="assistant-group">
                <div className="tro-ly-nhom-nhan">{n.nhan}</div>
                {n.muc.map((m) => (
                  <button
                    key={`${n.nhan}-${m.id}`}
                    type="button"
                    className="tro-ly-the"
                    onClick={() => moTrang(m.link)}
                    data-test="assistant-card"
                  >
                    <span className="tro-ly-the-ten">{m.tieuDe}</span>
                    {m.moTa && <span className="tro-ly-the-mo-ta">{m.moTa}</span>}
                    <span className="tro-ly-the-nut">{m.nhanNut} →</span>
                  </button>
                ))}
              </div>
            ))}

            {tn.goiY?.length > 0 && (
              <div className="tro-ly-goi-y">
                {tn.goiY.map((g) => (
                  <button key={g} type="button" onClick={() => hoi(g)} disabled={dangCho} data-test="assistant-suggestion">
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
          data-test="assistant-input"
        />
        <button type="submit" className="btn btn-dark btn-sm" disabled={dangCho || !dangGo.trim()} data-test="assistant-send">
          Gửi
        </button>
      </form>
    </div>
  );
}
