import { useCallback, useEffect, useMemo, useState } from 'react';
import { adminApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

const THU = ['Hai', 'Ba', 'Tư', 'Năm', 'Sáu', 'Bảy', 'CN'];

const hai = (n) => String(n).padStart(2, '0');
const isoNgay = (d) => `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())}`;

function homNay() {
  return isoNgay(new Date());
}

/*
 * Thứ Hai đầu tuần chứa ngày mùng 1, để lưới tháng luôn bắt đầu từ cột thứ Hai.
 *
 * getDay() trả 0 cho Chủ nhật nên phải quy về 6, không thì tuần có Chủ nhật bị lệch
 * hẳn một hàng.
 */
function dauLuoi(nam, thang) {
  const mot = new Date(nam, thang, 1);
  const lui = (mot.getDay() + 6) % 7;
  return new Date(nam, thang, 1 - lui);
}

function cuoiLuoi(nam, thang) {
  const cuoiThang = new Date(nam, thang + 1, 0);
  const them = (7 - ((cuoiThang.getDay() + 6) % 7) - 1 + 7) % 7;
  return new Date(nam, thang + 1, 0 + them);
}

const TEN_THANG = (nam, thang) => `Tháng ${thang + 1} năm ${nam}`;

/*
 * Lịch đặt chỗ của bộ phận quản trị.
 *
 * Gộp đơn đặt tiệc và đơn đặt món lên cùng một lịch. Trước khi có màn hình này, muốn biết
 * ngày mai có gì thì phải mở hai danh sách rồi tự đối chiếu, mà hai danh sách lại xếp theo
 * hai kiểu khác nhau: đơn tiệc theo ngày tổ chức, đơn món theo giờ nhận.
 *
 * Có hai cách xem. Xem tháng để thấy ngày nào kín ngày nào trống; xem ngày để đọc chi tiết
 * từng đơn trong một ngày.
 */
export default function AdminCalendarPage() {
  const [cheDo, setCheDo] = useState('thang');
  const [moc, setMoc] = useState(() => {
    const d = new Date();
    return { nam: d.getFullYear(), thang: d.getMonth() };
  });
  const [ngayChon, setNgayChon] = useState(homNay);
  const [ngayLich, setNgayLich] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState(null);

  const khoang = useMemo(() => {
    if (cheDo === 'ngay') return { tuNgay: ngayChon, denNgay: ngayChon };
    return {
      tuNgay: isoNgay(dauLuoi(moc.nam, moc.thang)),
      denNgay: isoNgay(cuoiLuoi(moc.nam, moc.thang)),
    };
  }, [cheDo, moc, ngayChon]);

  const tai = useCallback(async () => {
    setDangTai(true);
    setLoi(null);
    try {
      const data = await adminApi.calendar(khoang);
      setNgayLich(data);
    } catch (e) {
      setLoi(e.message || 'Không tải được lịch');
    } finally {
      setDangTai(false);
    }
  }, [khoang]);

  useEffect(() => {
    tai();
  }, [tai]);

  const doiThang = (buoc) => {
    setMoc((cu) => {
      const d = new Date(cu.nam, cu.thang + buoc, 1);
      return { nam: d.getFullYear(), thang: d.getMonth() };
    });
  };

  const tongTiec = ngayLich.reduce((s, n) => s + n.cacMuc.filter((m) => m.loai === 'TIEC').length, 0);
  const tongMon = ngayLich.reduce((s, n) => s + n.cacMuc.filter((m) => m.loai === 'MON').length, 0);

  return (
    <div className="wrap" data-test="admin-calendar">
      <h1>Lịch đặt chỗ</h1>
      <p className="muted">
        Đơn đặt tiệc xếp theo ngày tổ chức, đơn đặt món xếp theo thời điểm khách nhận.
      </p>

      <div className="cal-thanh">
        <div className="cal-nhom">
          <button
            type="button"
            className={cheDo === 'thang' ? 'btn' : 'btn btn-nhat'}
            onClick={() => setCheDo('thang')}
            data-test="calendar-mode-month"
          >
            Xem tháng
          </button>
          <button
            type="button"
            className={cheDo === 'ngay' ? 'btn' : 'btn btn-nhat'}
            onClick={() => setCheDo('ngay')}
            data-test="calendar-mode-day"
          >
            Xem ngày
          </button>
        </div>

        {cheDo === 'thang' ? (
          <div className="cal-nhom">
            <button type="button" className="btn btn-nhat" onClick={() => doiThang(-1)} data-test="calendar-prev">
              ← Tháng trước
            </button>
            <strong data-test="calendar-title">{TEN_THANG(moc.nam, moc.thang)}</strong>
            <button type="button" className="btn btn-nhat" onClick={() => doiThang(1)} data-test="calendar-next">
              Tháng sau →
            </button>
          </div>
        ) : (
          <div className="cal-nhom">
            <label htmlFor="cal-ngay">Ngày</label>
            <input
              id="cal-ngay"
              type="date"
              value={ngayChon}
              onChange={(e) => setNgayChon(e.target.value)}
              data-test="calendar-date"
            />
          </div>
        )}
      </div>

      {dangTai && <Loading label="Đang tải lịch…" />}
      {loi && <ErrorBlock message={loi} onRetry={tai} />}

      {!dangTai && !loi && (
        <>
          <p className="muted" data-test="calendar-summary">
            Khoảng đang xem có {tongTiec} đơn đặt tiệc và {tongMon} đơn đặt món.
          </p>

          {cheDo === 'thang' ? (
            <LuoiThang
              ngayLich={ngayLich}
              thang={moc.thang}
              onChonNgay={(ngay) => {
                setNgayChon(ngay);
                setCheDo('ngay');
              }}
            />
          ) : (
            <ChiTietNgay ngay={ngayLich[0]} />
          )}
        </>
      )}
    </div>
  );
}

function LuoiThang({ ngayLich, thang, onChonNgay }) {
  const nay = homNay();
  return (
    <div className="cal-luoi" data-test="calendar-grid">
      {THU.map((t) => (
        <div key={t} className="cal-thu">
          {t}
        </div>
      ))}
      {ngayLich.map((n) => {
        const trongThang = Number(n.ngay.slice(5, 7)) - 1 === thang;
        const lop = ['cal-o'];
        if (!trongThang) lop.push('cal-o-ngoai');
        if (n.ngay === nay) lop.push('cal-o-nay');
        if (n.cacMuc.length > 0) lop.push('cal-o-co-don');
        return (
          <button
            type="button"
            key={n.ngay}
            className={lop.join(' ')}
            onClick={() => onChonNgay(n.ngay)}
            data-test="calendar-cell"
            data-ngay={n.ngay}
          >
            <span className="cal-so">{Number(n.ngay.slice(8, 10))}</span>
            {n.cacMuc.length === 0 ? (
              <span className="cal-trong">còn trống</span>
            ) : (
              <span className="cal-the-nhom">
                {n.cacMuc.slice(0, 3).map((m) => (
                  <span key={m.maDon} className={`cal-the cal-the-${m.loai.toLowerCase()}`}>
                    {m.loai === 'TIEC' ? m.buoi || 'Tiệc' : `Món ${m.gio?.slice(0, 5) || ''}`}
                  </span>
                ))}
                {n.cacMuc.length > 3 && <span className="cal-the">+{n.cacMuc.length - 3} đơn</span>}
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}

function ChiTietNgay({ ngay }) {
  if (!ngay || ngay.cacMuc.length === 0) {
    return <Empty message="Ngày này chưa có đơn nào." />;
  }
  return (
    <table className="bang" data-test="calendar-day-table">
      <thead>
        <tr>
          <th>Loại</th>
          <th>Mã đơn</th>
          <th>Khách</th>
          <th>Chi tiết</th>
          <th>Buổi / giờ</th>
          <th>Trạng thái</th>
          <th>Tổng tiền</th>
        </tr>
      </thead>
      <tbody>
        {ngay.cacMuc.map((m) => (
          <tr key={`${m.loai}-${m.maDon}`} data-test="calendar-day-row">
            <td>{m.loai === 'TIEC' ? 'Đặt tiệc' : 'Đặt món'}</td>
            <td>{m.maDon}</td>
            <td>{m.tenKhach}</td>
            <td>{m.moTa}</td>
            <td>{m.buoi || m.gio?.slice(0, 5)}</td>
            <td>{m.trangThai}</td>
            <td>{formatCurrency(m.tongTien)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
