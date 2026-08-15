import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import {
  fetchOptions,
  fetchQuote,
  goToStep,
  resetBooking,
  selectBooking,
  submitBooking,
  updateForm,
} from '@/features/booking/bookingSlice';
import { fetchPackages, fetchSpaces, selectPackages, selectSpaces } from '@/features/catalog/catalogSlice';
import { formatCurrency, tomorrowISO } from '@/utils/format';
import { ErrorBlock } from '@/components/common/StateBlock';

const STEP_LABELS = ['1. Sự kiện & số khách', '2. Không gian & gói tiệc', '3. Thông tin liên hệ'];

// Kiểm tra từng bước ngay trên trình duyệt để báo lỗi sớm
function validateStep(step, form) {
  const errors = {};
  if (step === 1) {
    if (!form.eventType) errors.eventType = 'Vui lòng chọn loại hình sự kiện';
    if (!form.eventDate) errors.eventDate = 'Vui lòng chọn ngày tổ chức';
    else if (form.eventDate < tomorrowISO()) errors.eventDate = 'Ngày tổ chức phải sau hôm nay';
    const guests = Number(form.guestCount);
    if (!guests || guests < 10 || guests > 800) errors.guestCount = 'Số khách từ 10 đến 800';
  }
  if (step === 2) {
    if (!form.spaceId) errors.spaceId = 'Vui lòng chọn một không gian';
    if (!form.packageId) errors.packageId = 'Vui lòng chọn một gói tiệc';
  }
  if (step === 3) {
    if (!form.customerName || form.customerName.trim().length < 2)
      errors.customerName = 'Vui lòng nhập họ tên';
    if (!/^[0-9\s.+()-]{9,15}$/.test(form.customerPhone || ''))
      errors.customerPhone = 'Số điện thoại không hợp lệ';
  }
  return errors;
}

export default function BookingPage() {
  const dispatch = useDispatch();
  const { step, form, options, quote, quoteStatus, submitStatus, result, error, fieldErrors } =
    useSelector(selectBooking);
  const spaces = useSelector(selectSpaces);
  const packages = useSelector(selectPackages);

  useEffect(() => {
    dispatch(fetchOptions());
    dispatch(fetchSpaces());
    dispatch(fetchPackages());
  }, [dispatch]);

  // Đổi số khách, không gian, gói tiệc hay ngày thì hỏi lại giá từ server
  useEffect(() => {
    const { spaceId, packageId, guestCount, eventDate } = form;
    if (!spaceId || !packageId || !guestCount) return;
    const timer = setTimeout(() => {
      dispatch(fetchQuote({ spaceId, packageId, guestCount: Number(guestCount), eventDate: eventDate || null }));
    }, 350); // đợi người dùng gõ xong rồi mới gọi API
    return () => clearTimeout(timer);
  }, [dispatch, form.spaceId, form.packageId, form.guestCount, form.eventDate]);

  const clientErrors = validateStep(step, form);
  const set = (patch) => dispatch(updateForm(patch));

  const next = () => {
    if (Object.keys(clientErrors).length === 0) dispatch(goToStep(step + 1));
    else set({}); // render lại để hiện lỗi
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (Object.keys(clientErrors).length > 0) return;
    dispatch(
      submitBooking({
        ...form,
        guestCount: Number(form.guestCount),
        customerEmail: form.customerEmail || null,
        note: form.note || null,
      }),
    );
  };

  if (submitStatus === 'succeeded' && result) {
    return <SuccessPanel result={result} onReset={() => dispatch(resetBooking())} />;
  }

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">Đặt tiệc trực tuyến</div>
          <h2>Ba bước để có báo giá</h2>
          <p className="muted">
            Chi phí hiển thị là tạm tính. Bộ phận kinh doanh sẽ liên hệ xác nhận trong 24 giờ.
          </p>
        </div>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          <div className="card">
            <div className="card-body">
              <div className="steps">
                {STEP_LABELS.map((label, index) => (
                  <div
                    key={label}
                    className={`step ${step === index + 1 ? 'on' : ''} ${step > index + 1 ? 'done' : ''}`}
                  >
                    {label}
                  </div>
                ))}
              </div>

              {error && <ErrorBlock message={error} />}

              <form onSubmit={handleSubmit} noValidate>
                {step === 1 && (
                  <StepEvent form={form} set={set} options={options} errors={clientErrors} />
                )}
                {step === 2 && (
                  <StepChoices
                    form={form}
                    set={set}
                    spaces={spaces.items}
                    packages={packages.items}
                    errors={clientErrors}
                  />
                )}
                {step === 3 && (
                  <StepContact form={form} set={set} errors={{ ...clientErrors, ...fieldErrors }} />
                )}

                <div className="fnav">
                  {step > 1 ? (
                    <button type="button" className="btn btn-ghost" onClick={() => dispatch(goToStep(step - 1))}>
                      ← Quay lại
                    </button>
                  ) : (
                    <span />
                  )}

                  {step < 3 ? (
                    <button type="button" className="btn btn-dark" onClick={next}>
                      Tiếp tục →
                    </button>
                  ) : (
                    <button type="submit" className="btn btn-gold" disabled={submitStatus === 'loading'}>
                      {submitStatus === 'loading' ? 'Đang gửi…' : 'Gửi yêu cầu đặt tiệc'}
                    </button>
                  )}
                </div>
              </form>
            </div>
          </div>

          <EstimatePanel quote={quote} loading={quoteStatus === 'loading'} />
        </div>
      </div>
    </section>
  );
}

// Bước 1: chọn loại sự kiện, ngày và số khách
function StepEvent({ form, set, options, errors }) {
  return (
    <>
      <div className="fgroup">
        <label htmlFor="eventType">Loại hình sự kiện *</label>
        <select
          id="eventType"
          value={form.eventType}
          onChange={(e) => set({ eventType: e.target.value })}
        >
          <option value="">— Chọn loại hình —</option>
          {options.eventTypes.map((type) => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
        {errors.eventType && <div className="err">{errors.eventType}</div>}
      </div>

      <div className="form-row">
        <div className="fgroup">
          <label htmlFor="eventDate">Ngày tổ chức *</label>
          <input
            id="eventDate"
            type="date"
            min={tomorrowISO()}
            value={form.eventDate}
            onChange={(e) => set({ eventDate: e.target.value })}
          />
          {errors.eventDate && <div className="err">{errors.eventDate}</div>}
        </div>

        <div className="fgroup">
          <label htmlFor="timeSlot">Buổi *</label>
          <select id="timeSlot" value={form.timeSlot} onChange={(e) => set({ timeSlot: e.target.value })}>
            {options.timeSlots.map((slot) => (
              <option key={slot.value} value={slot.value}>
                {slot.label}
              </option>
            ))}
          </select>
        </div>

        <div className="fgroup">
          <label htmlFor="guestCount">Số khách dự kiến *</label>
          <input
            id="guestCount"
            type="number"
            min="10"
            max="800"
            placeholder="Ví dụ: 150"
            value={form.guestCount}
            onChange={(e) => set({ guestCount: e.target.value })}
          />
          {errors.guestCount && <div className="err">{errors.guestCount}</div>}
        </div>
      </div>
    </>
  );
}

// Bước 2: chọn không gian và gói tiệc
function StepChoices({ form, set, spaces, packages, errors }) {
  const guests = Number(form.guestCount) || 0;

  return (
    <>
      <div className="fgroup">
        <label>Chọn không gian *</label>
        <div className="picks">
          {spaces.map((space) => {
            const fits = guests === 0 || (guests >= space.capacityMin && guests <= space.capacityMax);
            return (
              <button
                key={space.id}
                type="button"
                className={`pick ${form.spaceId === space.id ? 'sel' : ''}`}
                onClick={() => set({ spaceId: space.id })}
                disabled={!fits}
                title={fits ? '' : `Không gian này phục vụ ${space.capacityMin}–${space.capacityMax} khách`}
                style={fits ? undefined : { opacity: 0.45, cursor: 'not-allowed' }}
              >
                <span className="t">{space.name}</span>
                <span className="s">
                  {space.capacityMin}–{space.capacityMax} khách · {formatCurrency(space.rentalFee)}
                </span>
              </button>
            );
          })}
        </div>
        {errors.spaceId && <div className="err">{errors.spaceId}</div>}
      </div>

      <div className="fgroup">
        <label>Chọn gói tiệc *</label>
        <div className="picks">
          {packages.map((pkg) => (
            <button
              key={pkg.id}
              type="button"
              className={`pick ${form.packageId === pkg.id ? 'sel' : ''}`}
              onClick={() => set({ packageId: pkg.id })}
            >
              <span className="t">{pkg.name}</span>
              <span className="s">{formatCurrency(pkg.pricePerTable)} / mâm</span>
            </button>
          ))}
        </div>
        {errors.packageId && <div className="err">{errors.packageId}</div>}
      </div>
    </>
  );
}

// Bước 3: nhập thông tin liên hệ
function StepContact({ form, set, errors }) {
  return (
    <>
      <div className="form-row">
        <div className="fgroup">
          <label htmlFor="customerName">Họ và tên *</label>
          <input
            id="customerName"
            value={form.customerName}
            placeholder="Nguyễn Văn A"
            onChange={(e) => set({ customerName: e.target.value })}
          />
          {errors.customerName && <div className="err">{errors.customerName}</div>}
        </div>

        <div className="fgroup">
          <label htmlFor="customerPhone">Số điện thoại *</label>
          <input
            id="customerPhone"
            type="tel"
            value={form.customerPhone}
            placeholder="09xx xxx xxx"
            onChange={(e) => set({ customerPhone: e.target.value })}
          />
          {errors.customerPhone && <div className="err">{errors.customerPhone}</div>}
        </div>
      </div>

      <div className="fgroup">
        <label htmlFor="customerEmail">Email</label>
        <input
          id="customerEmail"
          type="email"
          value={form.customerEmail}
          placeholder="ban@email.com"
          onChange={(e) => set({ customerEmail: e.target.value })}
        />
        {errors.customerEmail && <div className="err">{errors.customerEmail}</div>}
      </div>

      <div className="fgroup">
        <label htmlFor="note">Yêu cầu thêm</label>
        <textarea
          id="note"
          value={form.note}
          placeholder="Ví dụ: cần 2 bàn chay, có 3 khách dị ứng hải sản, muốn dựng sân khấu bên trái…"
          onChange={(e) => set({ note: e.target.value })}
        />
      </div>
    </>
  );
}

// Khối tạm tính hiện bên phải form
function EstimatePanel({ quote, loading }) {
  return (
    <aside className="estimate">
      <h3 style={{ color: 'var(--gold-light)', marginBottom: 16 }}>Chi phí tạm tính</h3>

      {!quote && !loading && (
        <p style={{ opacity: 0.75, fontSize: '0.9rem' }}>
          Chọn số khách, không gian và gói tiệc để xem bảng kê chi phí.
        </p>
      )}

      {loading && <p style={{ opacity: 0.75 }}>Đang tính…</p>}

      {quote && (
        <>
          <div className="est-row">
            <span>Số mâm (10 khách/mâm)</span>
            <span>{quote.tableCount} mâm</span>
          </div>
          <div className="est-row">
            <span>Đơn giá gói tiệc</span>
            <span>{formatCurrency(quote.unitPrice)}</span>
          </div>
          <div className="est-row">
            <span>Tiền ăn</span>
            <span>{formatCurrency(quote.foodAmount)}</span>
          </div>
          <div className="est-row">
            <span>Thuê không gian</span>
            <span>{Number(quote.spaceFee) === 0 ? 'Miễn phí' : formatCurrency(quote.spaceFee)}</span>
          </div>
          {Number(quote.discountAmount) > 0 && (
            <div className="est-row">
              <span>Giảm giá</span>
              <span>− {formatCurrency(quote.discountAmount)}</span>
            </div>
          )}
          <div className="est-row">
            <span>VAT {Number(quote.vatRate) * 100}%</span>
            <span>{formatCurrency(quote.vatAmount)}</span>
          </div>

          <div className="est-total">
            <span>Tạm tính</span>
            <span className="val">{formatCurrency(quote.totalAmount)}</span>
          </div>

          {quote.appliedRules?.length > 0 && (
            <ul className="est-rules">
              {quote.appliedRules.map((rule) => (
                <li key={rule}>{rule}</li>
              ))}
            </ul>
          )}
        </>
      )}
    </aside>
  );
}

// Màn hình báo gửi yêu cầu thành công
function SuccessPanel({ result, onReset }) {
  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 720 }}>
        <div className="card">
          <div className="card-body center">
            <div style={{ fontSize: '3rem' }}>✅</div>
            <h2>Đã nhận yêu cầu đặt tiệc</h2>
            <p className="muted" style={{ marginBottom: 24 }}>
              Mã đơn của bạn là <strong>{result.code}</strong>. Hãy lưu lại để tra cứu.
            </p>

            <table>
              <tbody>
                <tr>
                  <th>Loại hình</th>
                  <td>{result.eventTypeLabel}</td>
                </tr>
                <tr>
                  <th>Ngày & buổi</th>
                  <td>
                    {result.eventDate} · {result.timeSlotLabel}
                  </td>
                </tr>
                <tr>
                  <th>Số khách</th>
                  <td>
                    {result.guestCount} khách ({result.tableCount} mâm)
                  </td>
                </tr>
                <tr>
                  <th>Không gian</th>
                  <td>{result.spaceName}</td>
                </tr>
                <tr>
                  <th>Gói tiệc</th>
                  <td>{result.packageName}</td>
                </tr>
                <tr>
                  <th>Tạm tính</th>
                  <td>
                    <strong>{formatCurrency(result.totalAmount)}</strong>
                  </td>
                </tr>
              </tbody>
            </table>

            <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginTop: 24 }}>
              <button type="button" className="btn btn-outline" onClick={onReset}>
                Gửi yêu cầu khác
              </button>
              <Link to={`/tra-cuu?code=${result.code}`} className="btn btn-dark">
                Tra cứu đơn này
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
