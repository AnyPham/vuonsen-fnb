import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Bar, BarChart, CartesianGrid, Cell, ComposedChart, Legend, Line,
  Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { adminApi } from '@/api/endpoints';
import { formatCurrency } from '@/utils/format';
import { Empty, ErrorBlock, Loading } from '@/components/common/StateBlock';

/*
 * Trang thống kê cho quản trị: bốn ô số liệu tổng quan và bảy biểu đồ.
 *
 * Toàn bộ số liệu lấy từ một lần gọi /api/v1/admin/statistics. Backend đã gộp sẵn nên
 * trang này chỉ lo việc vẽ, không tính toán gì thêm. Giữ nguyên nguyên tắc đó: cần thêm
 * con số nào thì sửa ThongKeAggregator bên backend, đừng tính lại ở đây, vì tính hai nơi
 * là sớm muộn cũng lệch nhau.
 */

// Màu lấy từ bảng màu của giao diện để biểu đồ không lạc tông với phần còn lại
const MAU = ['#c6a052', '#b4552f', '#5e5951', '#e3c88a', '#7d8c68', '#8c6f52', '#a8917a'];

// Màu riêng cho bốn trạng thái đơn, xếp đúng thứ tự backend trả về
const MAU_TRANG_THAI = {
  'Chờ xác nhận': '#e3c88a',
  'Đã xác nhận': '#c6a052',
  'Đã hoàn thành': '#7d8c68',
  'Đã hủy': '#c0392b',
};

/** Rút gọn tiền về đơn vị triệu cho trục biểu đồ, vì số tiền Việt quá dài. */
const trieu = (so) => {
  if (!so) return '0';
  if (Math.abs(so) >= 1_000_000_000) return `${(so / 1_000_000_000).toFixed(1)} tỉ`;
  return `${Math.round(so / 1_000_000)} tr`;
};

/*
 * Đổi ngày thành chuỗi yyyy-MM-dd cho ô chọn ngày, theo giờ địa phương.
 *
 * Không dùng toISOString vì hàm đó quy về giờ UTC. Việt Nam lệch +7 nên nửa đêm ngày mùng 1
 * quy về UTC thành ngày cuối của tháng trước: ô từ ngày luôn hiện sớm hơn một ngày, còn ô
 * đến ngày bị lùi một ngày nếu mở trang trước 7 giờ sáng.
 */
const nganGon = (ngay) => [
  ngay.getFullYear(),
  String(ngay.getMonth() + 1).padStart(2, '0'),
  String(ngay.getDate()).padStart(2, '0'),
].join('-');

function OTongQuan({ nhan, giaTri, phu, test }) {
  return (
    <div className="card tk-o" data-test={test}>
      <div className="card-body">
        <div className="tk-o-nhan">{nhan}</div>
        <div className="tk-o-so">{giaTri}</div>
        {phu && <div className="tk-o-phu muted">{phu}</div>}
      </div>
    </div>
  );
}

function KhungBieuDo({ tieuDe, moTa, test, children }) {
  return (
    <div className="card tk-bd" data-test={test}>
      <div className="card-body">
        <h3 className="tk-bd-tieu-de">{tieuDe}</h3>
        {moTa && <p className="muted tk-bd-mo-ta">{moTa}</p>}
        <div className="tk-bd-khung">
          <ResponsiveContainer width="100%" height="100%">
            {children}
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}

export default function AdminStatsPage() {
  const macDinh = useMemo(() => {
    const den = new Date();
    const tu = new Date(den.getFullYear(), den.getMonth() - 11, 1);
    return { from: nganGon(tu), to: nganGon(den) };
  }, []);

  const [khoang, setKhoang] = useState(macDinh);
  const [duLieu, setDuLieu] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState(null);

  const tai = useCallback(async () => {
    setDangTai(true);
    setLoi(null);
    try {
      // axiosClient đã bóc sẵn response.data, kết quả trả về chính là số liệu
      setDuLieu(await adminApi.dashboard(khoang));
    } catch (e) {
      // axiosClient đưa lỗi về dạng { status, message }, câu báo của máy chủ nằm ở message
      setLoi(e.message || 'Không tải được số liệu thống kê');
    } finally {
      setDangTai(false);
    }
  }, [khoang]);

  useEffect(() => {
    tai();
  }, [tai]);

  const doiKhoang = (event) => {
    event.preventDefault();
    const bieuMau = new FormData(event.target);
    setKhoang({ from: bieuMau.get('from'), to: bieuMau.get('to') });
  };

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head">
          <p className="eyebrow">Quản trị</p>
          <h1>Thống kê kinh doanh</h1>
        </div>

        <form className="card tk-loc" onSubmit={doiKhoang} data-test="stats-filter">
          <div className="card-body form-row">
            <div className="fgroup">
              <label htmlFor="from">Từ ngày</label>
              <input id="from" name="from" type="date" defaultValue={khoang.from} data-test="from" />
            </div>
            <div className="fgroup">
              <label htmlFor="to">Đến ngày</label>
              <input id="to" name="to" type="date" defaultValue={khoang.to} data-test="to" />
            </div>
            <button type="submit" className="btn btn-dark" data-test="apply-range">
              Xem thống kê
            </button>
          </div>
        </form>

        {dangTai && <Loading label="Đang tính số liệu…" />}
        {loi && <ErrorBlock message={loi} />}

        {!dangTai && !loi && duLieu && (
          duLieu.tongQuan.tongSoDon === 0 ? (
            <Empty label="Chưa có đơn nào trong khoảng thời gian này" />
          ) : (
            <>
              {/* ---------- Bốn ô số liệu ---------- */}
              <div className="grid grid-4 tk-tong-quan">
                <OTongQuan
                  test="kpi-doanh-thu"
                  nhan="Doanh thu ghi nhận"
                  giaTri={formatCurrency(duLieu.tongQuan.doanhThu)}
                  phu={`${duLieu.tongQuan.soDonGhiNhan} đơn đã xác nhận hoặc hoàn thành`}
                />
                <OTongQuan
                  test="kpi-so-don"
                  nhan="Tổng số đơn"
                  giaTri={duLieu.tongQuan.tongSoDon}
                  phu={`Tỉ lệ hủy ${duLieu.tongQuan.tiLeHuy}%`}
                />
                <OTongQuan
                  test="kpi-so-khach"
                  nhan="Lượt khách phục vụ"
                  giaTri={duLieu.tongQuan.soKhach.toLocaleString('vi-VN')}
                  phu="Chỉ tính đơn ghi nhận doanh thu"
                />
                <OTongQuan
                  test="kpi-trung-binh"
                  nhan="Giá trị đơn trung bình"
                  giaTri={formatCurrency(duLieu.tongQuan.giaTriDonTrungBinh)}
                  phu={`Từ ${duLieu.khoang.tuNgay} đến ${duLieu.khoang.denNgay}`}
                />
              </div>

              {/* ---------- 1. Doanh thu theo tháng ---------- */}
              <KhungBieuDo
                test="chart-doanh-thu-thang"
                tieuDe="Doanh thu theo tháng"
                moTa="Cột là doanh thu, đường là số đơn. Chỉ tính đơn đã xác nhận và đã hoàn thành, ghi vào tháng tổ chức tiệc."
              >
                <ComposedChart data={duLieu.doanhThuTheoThang}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(42,39,35,0.1)" />
                  <XAxis dataKey="thang" fontSize={12} />
                  <YAxis yAxisId="tien" tickFormatter={trieu} fontSize={12} />
                  <YAxis yAxisId="don" orientation="right" allowDecimals={false} fontSize={12} />
                  <Tooltip
                    formatter={(gt, ten) => (ten === 'Doanh thu' ? formatCurrency(gt) : `${gt} đơn`)}
                  />
                  <Legend />
                  <Bar yAxisId="tien" dataKey="doanhThu" name="Doanh thu" fill="#c6a052" radius={[4, 4, 0, 0]} />
                  <Line yAxisId="don" type="monotone" dataKey="soDon" name="Số đơn" stroke="#b4552f" strokeWidth={2} />
                </ComposedChart>
              </KhungBieuDo>

              <div className="grid grid-2">
                {/* ---------- 2. Đơn theo trạng thái ---------- */}
                <KhungBieuDo
                  test="chart-trang-thai"
                  tieuDe="Đơn theo trạng thái"
                  moTa="Biểu đồ duy nhất tính cả đơn đã hủy, để thấy tỉ lệ hủy thực tế."
                >
                  <PieChart>
                    <Pie
                      data={duLieu.donTheoTrangThai}
                      dataKey="soDon"
                      nameKey="ten"
                      innerRadius="45%"
                      outerRadius="75%"
                      label={({ ten, soDon }) => (soDon > 0 ? `${ten}: ${soDon}` : '')}
                    >
                      {duLieu.donTheoTrangThai.map((muc) => (
                        <Cell key={muc.ten} fill={MAU_TRANG_THAI[muc.ten] || '#5e5951'} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(gt) => `${gt} đơn`} />
                  </PieChart>
                </KhungBieuDo>

                {/* ---------- 3. Tỉ lệ chọn gói tiệc ---------- */}
                <KhungBieuDo
                  test="chart-goi-tiec"
                  tieuDe="Tỉ lệ chọn gói tiệc"
                  moTa="Gói nào được khách chọn nhiều nhất."
                >
                  <PieChart>
                    <Pie
                      data={duLieu.tiLeChonGoi}
                      dataKey="soDon"
                      nameKey="ten"
                      innerRadius="45%"
                      outerRadius="75%"
                      label={({ ten, soDon }) => `${ten}: ${soDon}`}
                    >
                      {duLieu.tiLeChonGoi.map((muc, i) => (
                        <Cell key={muc.ten} fill={MAU[i % MAU.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(gt) => `${gt} đơn`} />
                  </PieChart>
                </KhungBieuDo>
              </div>

              {/* ---------- 4. Doanh thu theo không gian ---------- */}
              <KhungBieuDo
                test="chart-khong-gian"
                tieuDe="Doanh thu theo không gian"
                moTa="Xếp giảm dần để thấy ngay không gian nào đang gánh doanh thu."
              >
                <BarChart data={duLieu.doanhThuTheoKhongGian} layout="vertical" margin={{ left: 30 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(42,39,35,0.1)" />
                  <XAxis type="number" tickFormatter={trieu} fontSize={12} />
                  <YAxis type="category" dataKey="ten" width={130} fontSize={12} />
                  <Tooltip formatter={(gt) => formatCurrency(gt)} />
                  <Bar dataKey="doanhThu" name="Doanh thu" radius={[0, 4, 4, 0]}>
                    {duLieu.doanhThuTheoKhongGian.map((muc, i) => (
                      <Cell key={muc.ten} fill={MAU[i % MAU.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </KhungBieuDo>

              <div className="grid grid-2">
                {/* ---------- 5. Loại sự kiện ---------- */}
                <KhungBieuDo
                  test="chart-loai-su-kien"
                  tieuDe="Đơn theo loại sự kiện"
                  moTa="Nhà hàng đang sống chủ yếu nhờ loại tiệc nào."
                >
                  <BarChart data={duLieu.theoLoaiSuKien}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(42,39,35,0.1)" />
                    <XAxis dataKey="ten" fontSize={11} interval={0} angle={-15} textAnchor="end" height={60} />
                    <YAxis allowDecimals={false} fontSize={12} />
                    <Tooltip formatter={(gt) => `${gt} đơn`} />
                    <Bar dataKey="soDon" name="Số đơn" fill="#b4552f" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </KhungBieuDo>

                {/* ---------- 6. Tỉ lệ lấp đầy ---------- */}
                <KhungBieuDo
                  test="chart-lap-day"
                  tieuDe="Tỉ lệ lấp đầy không gian"
                  moTa="Số buổi đã bán chia cho tổng số buổi có thể bán. Mỗi ngày có ba buổi."
                >
                  <BarChart data={duLieu.lapDayKhongGian} layout="vertical" margin={{ left: 30 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(42,39,35,0.1)" />
                    <XAxis type="number" unit="%" fontSize={12} />
                    <YAxis type="category" dataKey="ten" width={130} fontSize={12} />
                    <Tooltip
                      formatter={(gt, ten, muc) =>
                        `${gt}% — ${muc.payload.soBuoiDaDat}/${muc.payload.tongSoBuoi} buổi`}
                    />
                    <Bar dataKey="tiLe" name="Tỉ lệ lấp đầy" fill="#7d8c68" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </KhungBieuDo>
              </div>

              {/* ---------- 7. Đặt món lẻ ---------- */}
              <KhungBieuDo
                test="chart-dat-mon"
                tieuDe="Đơn đặt món lẻ theo tháng"
                moTa="Cột chồng tách giao tận nhà và ăn tại chỗ, đường là doanh thu. Ghi theo tháng phục vụ, không phải tháng đặt."
              >
                <ComposedChart data={duLieu.donDatMon}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(42,39,35,0.1)" />
                  <XAxis dataKey="thang" fontSize={12} />
                  <YAxis yAxisId="don" allowDecimals={false} fontSize={12} />
                  <YAxis yAxisId="tien" orientation="right" tickFormatter={trieu} fontSize={12} />
                  <Tooltip
                    formatter={(gt, ten) => (ten === 'Doanh thu' ? formatCurrency(gt) : `${gt} đơn`)}
                  />
                  <Legend />
                  <Bar yAxisId="don" dataKey="giaoTanNha" name="Giao tận nhà" stackId="a" fill="#c6a052" />
                  <Bar yAxisId="don" dataKey="anTaiCho" name="Ăn tại chỗ" stackId="a" fill="#8c6f52" radius={[4, 4, 0, 0]} />
                  <Line yAxisId="tien" type="monotone" dataKey="doanhThu" name="Doanh thu" stroke="#b4552f" strokeWidth={2} />
                </ComposedChart>
              </KhungBieuDo>
            </>
          )
        )}
      </div>
    </section>
  );
}
