import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useSearchParams } from 'react-router-dom';
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
import { bookingApi } from '@/api/endpoints';
import { useI18n } from '@/i18n';
import { useDinhDang } from '@/i18n/dinhDang';
import { ErrorBlock } from '@/components/common/StateBlock';
import SuggestionBox from '@/components/common/SuggestionBox';

const STEP_KEYS = ['booking.step1', 'booking.step2', 'booking.step3'];

// Số ngày phải báo trước, lấy quy định từ backend chứ không tự đặt ra ở đây
function leadTimeDays(rules, guestCount) {
  if (!rules) return 1;
  const tables = Math.ceil((Number(guestCount) || 0) / (rules.guestsPerTable || 10));
  return tables >= rules.largePartyTables ? rules.largePartyMinDays : rules.minDaysAhead;
}

function earliestDate(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}

// Gói tiệc dài hơn thời lượng buổi thì không phục vụ được. Gói thuê trọn ngày là ngoại lệ.
function packageFitsSlot(pkg, slotHours, fullDayHours) {
  if (!pkg.hoursIncluded || !slotHours) return true;
  if (fullDayHours && pkg.hoursIncluded >= fullDayHours) return true;
  return pkg.hoursIncluded <= slotHours;
}

/*
 * Kiểm tra từng bước ngay trên trình duyệt để báo lỗi sớm.
 *
 * Nhận hàm dịch t qua tham số chứ không gọi useI18n bên trong: đây là hàm thường, không
 * phải component, gọi hook ở đây thì React báo lỗi lúc chạy.
 */
function validateStep(step, form, rules, t) {
  const errors = {};
  if (step === 1) {
    if (!form.eventType) errors.eventType = t('booking.errEventType');
    if (!form.eventDate) {
      errors.eventDate = t('booking.errEventDate');
    } else {
      const days = leadTimeDays(rules, form.guestCount);
      if (form.eventDate < earliestDate(days)) {
        errors.eventDate = `Tiệc quy mô này cần đặt trước ít nhất ${days} ngày`;
      }
    }
    const guests = Number(form.guestCount);
    const min = rules?.minGuests ?? 10;
    const max = rules?.maxGuests ?? 800;
    if (!guests || guests < min || guests > max) {
      errors.guestCount = `Số khách từ ${min} đến ${max}`;
    }
  }
  if (step === 2) {
    if (!form.spaceId) errors.spaceId = t('booking.errSpace');
    // Gói tiệc không bắt buộc, nhưng phải chọn rõ: một gói, hoặc chỉ thuê không gian
    if (!form.packageId && !form.noPackage) {
      errors.packageId = t('booking.errPackage');
    }
  }
  if (step === 3) {
    if (!form.customerName || form.customerName.trim().length < 2)
      errors.customerName = t('booking.errName');
    if (!/^[0-9\s.+()-]{9,15}$/.test(form.customerPhone || ''))
      errors.customerPhone = t('booking.errPhone');
  }
  return errors;
}

export default function BookingPage() {
  const dispatch = useDispatch();
  const { step, form, options, quote, quoteStatus, submitStatus, result, error, fieldErrors } =
    useSelector(selectBooking);
  const spaces = useSelector(selectSpaces);
  const [thamSo] = useSearchParams();
  const { t } = useI18n();

  /*
   * Buổi đã kín của từng không gian trong ngày khách chọn.
   *
   * Chỉ hỏi khi đã có ngày. Gọi hỏng thì để rỗng và coi như chưa biết: bước gửi đơn phía
   * máy chủ vẫn kiểm tra lại, nên mất khối này cũng không cho đặt trùng được.
   */
  const [tinhTrangTrong, setTinhTrangTrong] = useState({});
  const packages = useSelector(selectPackages);

  useEffect(() => {
    dispatch(fetchOptions());
    dispatch(fetchSpaces());
    dispatch(fetchPackages());
  }, [dispatch]);

  /*
   * Vào từ trang chi tiết một không gian thì chọn sẵn đúng không gian đó.
   *
   * Địa chỉ có dạng /dat-tiec?khong-gian=<slug>. Chỉ chọn khi khách chưa tự chọn gì, để thao tác
   * của khách không bị ghi đè lúc danh sách không gian tải xong.
   */
  useEffect(() => {
    const slug = thamSo.get('khong-gian');
    const danhSach = spaces.items || [];
    if (!slug || form.spaceId || danhSach.length === 0) return;
    const khongGian = danhSach.find((kg) => kg.slug === slug);
    if (khongGian) dispatch(updateForm({ spaceId: khongGian.id }));
  }, [dispatch, thamSo, spaces.items, form.spaceId]);

  // Đổi số khách, không gian, gói tiệc hay ngày thì hỏi lại giá từ server
  useEffect(() => {
    const { spaceId, packageId, noPackage, guestCount, eventDate } = form;
    if (!spaceId || !guestCount || (!packageId && !noPackage)) return;
    const timer = setTimeout(() => {
      dispatch(fetchQuote({
        spaceId,
        packageId: packageId || null,
        guestCount: Number(guestCount),
        eventDate: eventDate || null,
      }));
    }, 350); // đợi người dùng gõ xong rồi mới gọi API
    return () => clearTimeout(timer);
  }, [dispatch, form.spaceId, form.packageId, form.noPackage, form.guestCount, form.eventDate]);

  const rules = options.rules;
  const slotHours = options.timeSlots?.find((s) => s.value === form.timeSlot)?.durationHours;
  const clientErrors = validateStep(step, form, rules, t);
  useEffect(() => {
    if (!form.eventDate) {
      setTinhTrangTrong({});
      return undefined;
    }
    let conHieuLuc = true;
    bookingApi
      .availability(form.eventDate)
      .then((kq) => {
        if (!conHieuLuc) return;
        const theoKhongGian = {};
        kq.khongGian.forEach((k) => { theoKhongGian[k.spaceId] = k; });
        setTinhTrangTrong(theoKhongGian);
      })
      .catch(() => { if (conHieuLuc) setTinhTrangTrong({}); });
    return () => { conHieuLuc = false; };
  }, [form.eventDate]);

  const set = (patch) => dispatch(updateForm(patch));

  // Chỉ báo lỗi sau khi khách bấm Tiếp tục, tránh vừa mở form đã thấy chữ đỏ
  const [showErrors, setShowErrors] = useState(false);
  const errors = showErrors ? clientErrors : {};

  const next = () => {
    if (Object.keys(clientErrors).length === 0) {
      setShowErrors(false);
      dispatch(goToStep(step + 1));
    } else {
      setShowErrors(true);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (Object.keys(clientErrors).length > 0) {
      setShowErrors(true);
      return;
    }
    // noPackage chỉ là cờ của giao diện, backend nhận biết qua packageId để trống
    const { noPackage, ...duLieu } = form;
    dispatch(
      submitBooking({
        ...duLieu,
        packageId: noPackage ? null : duLieu.packageId,
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
          <div className="eyebrow center">{t('booking.eyebrow')}</div>
          <h2>{t('booking.title')}</h2>
          <p className="muted">
            {t('booking.desc')}
          </p>
        </div>

        <div className="grid grid-2" style={{ alignItems: 'start' }}>
          <div className="card">
            <div className="card-body">
              <div className="steps">
                {STEP_KEYS.map((khoa, index) => (
                  <div
                    key={khoa}
                    className={`step ${step === index + 1 ? 'on' : ''} ${step > index + 1 ? 'done' : ''}`}
                  >
                    {t(khoa)}
                  </div>
                ))}
              </div>

              {error && <ErrorBlock message={error} />}

              <form onSubmit={handleSubmit} noValidate>
                {step === 1 && (
                  <StepEvent form={form} set={set} options={options} errors={errors} rules={rules} />
                )}
                {step === 2 && (
                  <StepChoices
                    form={form}
                    set={set}
                    spaces={spaces.items}
                    packages={packages.items}
                    errors={errors}
                    slotHours={slotHours}
                    fullDayHours={rules?.fullDayPackageHours}
                    tinhTrangTrong={tinhTrangTrong}
                  />
                )}
                {step === 3 && (
                  <StepContact form={form} set={set} errors={{ ...errors, ...fieldErrors }} />
                )}

                <div className="fnav">
                  {step > 1 ? (
                    <button type="button" className="btn btn-ghost" onClick={() => dispatch(goToStep(step - 1))} data-test="prev-step">
                      {t('booking.back')}
                    </button>
                  ) : (
                    <span />
                  )}

                  {step < 3 ? (
                    <button type="button" className="btn btn-dark" onClick={next} data-test="next-step">
                      {t('booking.next')}
                    </button>
                  ) : (
                    <button type="submit" className="btn btn-gold" disabled={submitStatus === 'loading'} data-test="submit-booking">
                      {submitStatus === 'loading' ? t('booking.sending') : t('booking.submit')}
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
function StepEvent({ form, set, options, errors, rules }) {
  const { t, lang } = useI18n();
  const nhanTuyChon = (o) => (lang === 'en' && o.labelEn ? o.labelEn : o.label);

  return (
    <>
      <div className="fgroup">
        <label htmlFor="eventType">{t('booking.eventType')}</label>
        <select
          id="eventType"
          data-test="event-type"
          value={form.eventType}
          onChange={(e) => set({ eventType: e.target.value })}
        >
          <option value="">{t('booking.chooseType')}</option>
          {options.eventTypes.map((type) => (
            <option key={type.value} value={type.value}>
              {nhanTuyChon(type)}
            </option>
          ))}
        </select>
        {errors.eventType && <div className="err" data-test="error-event-type">{errors.eventType}</div>}
      </div>

      <div className="form-row">
        <div className="fgroup">
          <label htmlFor="eventDate">{t('booking.eventDate')}</label>
          <input
            id="eventDate"
            data-test="event-date"
            type="date"
            min={earliestDate(leadTimeDays(rules, form.guestCount))}
            value={form.eventDate}
            onChange={(e) => set({ eventDate: e.target.value })}
          />
          {errors.eventDate && <div className="err" data-test="error-event-date">{errors.eventDate}</div>}
        </div>

        <div className="fgroup">
          <label htmlFor="timeSlot">{t('booking.timeSlot')}</label>
          <select id="timeSlot" data-test="time-slot" value={form.timeSlot} onChange={(e) => set({ timeSlot: e.target.value })}>
            {options.timeSlots.map((slot) => (
              <option key={slot.value} value={slot.value}>
                {nhanTuyChon(slot)}
              </option>
            ))}
          </select>
        </div>

        <div className="fgroup">
          <label htmlFor="guestCount">{t('booking.guestCount')}</label>
          <input
            id="guestCount"
            data-test="guest-count"
            type="number"
            min="10"
            max="800"
            placeholder={t('booking.guestPlaceholder')}
            value={form.guestCount}
            onChange={(e) => set({ guestCount: e.target.value })}
          />
          {errors.guestCount && <div className="err" data-test="error-guest-count">{errors.guestCount}</div>}
        </div>
      </div>
    </>
  );
}

// Bước 2: chọn không gian và gói tiệc
function StepChoices({ form, set, spaces, packages, errors, slotHours, fullDayHours, tinhTrangTrong }) {
  const guests = Number(form.guestCount) || 0;
  const { t, tDb } = useI18n();
  const dd = useDinhDang();

  return (
    <>
      <SuggestionBox
        guestCount={form.guestCount}
        eventType={form.eventType}
        eventDate={form.eventDate}
        onPick={(spaceId, packageId) => set({ spaceId, packageId, noPackage: false })}
      />

      <div className="fgroup">
        <label>{t('booking.chooseSpace')}</label>
        <div className="picks">
          {spaces.map((space) => {
            // Chỉ chặn khi vượt sức chứa. Khách ít hơn mức tối thiểu vẫn đặt được,
            // mức tính tiền do backend quyết định và ghi rõ trong bảng tạm tính.
            const fits = guests === 0 || guests <= space.capacityMax;
            const belowMinimum = guests > 0 && guests < space.capacityMin;

            /*
             * Buổi khách đang chọn đã có tiệc, hoặc cả ngày đã cho thuê trọn.
             *
             * Chỉ báo chứ không khóa nút. Đây là ảnh chụp tình trạng lúc mở trang, mà trong
             * lúc khách điền form có thể có đơn vừa bị hủy làm buổi đó trống trở lại. Khóa
             * theo ảnh chụp cũ thì chặn oan khách. Máy chủ vẫn kiểm tra lại lúc gửi đơn nên
             * không có cách nào đặt trùng lọt qua được.
             */
            const trong = tinhTrangTrong[space.id];
            const kinBuoiNay = !!trong && (trong.kinCaNgay
              || (!!form.timeSlot && trong.buoiDaKin.includes(form.timeSlot)));
            return (
              <button
                key={space.id}
                type="button"
                className={`pick ${form.spaceId === space.id ? 'sel' : ''}`}
                onClick={() => set({ spaceId: space.id })}
                data-test="space-pick"
                disabled={!fits}
                title={fits
                  ? (kinBuoiNay ? t('booking.spaceBusyTitle') : '')
                  : t('booking.spaceTooSmall', { max: space.capacityMax })}
                style={fits ? undefined : { opacity: 0.45, cursor: 'not-allowed' }}
              >
                <span className="t">{tDb(space, 'name')}</span>
                <span className="s">
                  {t('spaceDetail.guestRange', { min: space.capacityMin, max: space.capacityMax })}
                  {' · '}{dd.tien(space.rentalFee)}
                  {belowMinimum && t('booking.belowMinimum')}
                </span>
                {kinBuoiNay && (
                  <span className="s" style={{ color: 'var(--danger)' }} data-test="space-busy">
                    {trong.kinCaNgay ? t('booking.bookedAllDay') : t('booking.bookedThisSlot')}
                  </span>
                )}
              </button>
            );
          })}
        </div>
        {errors.spaceId && <div className="err" data-test="error-space">{errors.spaceId}</div>}
      </div>

      <div className="fgroup">
        <label>{t('booking.choosePackage')}</label>
        <div className="picks">
          {packages.map((pkg) => {
            const fits = packageFitsSlot(pkg, slotHours, fullDayHours);
            return (
              <button
                key={pkg.id}
                type="button"
                className={`pick ${form.packageId === pkg.id ? 'sel' : ''}`}
                onClick={() => set({ packageId: pkg.id, noPackage: false })}
                data-test="package-pick"
                disabled={!fits}
                title={fits ? '' : t('booking.packageTooLong', { gio: pkg.hoursIncluded })}
                style={fits ? undefined : { opacity: 0.45, cursor: 'not-allowed' }}
              >
                <span className="t">{tDb(pkg, 'name')}</span>
                <span className="s">{dd.tien(pkg.pricePerTable)} / {t('packages.perTable')}</span>
              </button>
            );
          })}
          {/* Khách chỉ cần mặt bằng: tự lo ăn uống, hoặc sự kiện không có tiệc */}
          <button
            type="button"
            className={`pick ${form.noPackage && !form.packageId ? 'sel' : ''}`}
            onClick={() => set({ packageId: null, noPackage: true })}
            data-test="no-package-pick"
          >
            <span className="t">{t('booking.noPackage')}</span>
            <span className="s">{t('booking.noPackageDesc')}</span>
          </button>
        </div>
        {errors.packageId && <div className="err" data-test="error-package">{errors.packageId}</div>}
      </div>
    </>
  );
}

// Bước 3: nhập thông tin liên hệ
function StepContact({ form, set, errors }) {
  const { t } = useI18n();

  return (
    <>
      <div className="form-row">
        <div className="fgroup">
          <label htmlFor="customerName">{t('booking.customerName')}</label>
          <input
            id="customerName"
            data-test="customer-name"
            value={form.customerName}
            placeholder={t('booking.namePlaceholder')}
            onChange={(e) => set({ customerName: e.target.value })}
          />
          {errors.customerName && <div className="err" data-test="error-customer-name">{errors.customerName}</div>}
        </div>

        <div className="fgroup">
          <label htmlFor="customerPhone">{t('booking.customerPhone')}</label>
          <input
            id="customerPhone"
            data-test="customer-phone"
            type="tel"
            value={form.customerPhone}
            placeholder="09xx xxx xxx"
            onChange={(e) => set({ customerPhone: e.target.value })}
          />
          {errors.customerPhone && <div className="err" data-test="error-customer-phone">{errors.customerPhone}</div>}
        </div>
      </div>

      <div className="fgroup">
        <label htmlFor="customerEmail">Email</label>
        <input
          id="customerEmail"
          data-test="customer-email"
          type="email"
          value={form.customerEmail}
          placeholder="ban@email.com"
          onChange={(e) => set({ customerEmail: e.target.value })}
        />
        {errors.customerEmail && <div className="err">{errors.customerEmail}</div>}
      </div>

      <div className="fgroup">
        <label htmlFor="note">{t('booking.note')}</label>
        <textarea
          id="note"
          value={form.note}
          placeholder={t('booking.notePlaceholder')}
          onChange={(e) => set({ note: e.target.value })}
        />
      </div>
    </>
  );
}

// Khối tạm tính hiện bên phải form
function EstimatePanel({ quote, loading }) {
  const { t, lang } = useI18n();
  const dd = useDinhDang();
  const quyTac = (lang === 'en' && quote?.appliedRulesEn?.length ? quote.appliedRulesEn : quote?.appliedRules) || [];

  return (
    <aside className="estimate">
      <h3 style={{ color: 'var(--gold-light)', marginBottom: 16 }}>{t('booking.estimateTitle')}</h3>

      {!quote && !loading && (
        <p style={{ opacity: 0.75, fontSize: '0.9rem' }}>
          {t('booking.estimateHint')}
        </p>
      )}

      {loading && <p style={{ opacity: 0.75 }}>{t('booking.calculating')}</p>}

      {quote && (
        <>
          {/* Đơn chỉ thuê không gian không có mâm và tiền ăn, ẩn ba dòng này đi */}
          {Number(quote.tableCount) > 0 && (
            <>
              <div className="est-row">
                <span>{t('booking.tableCount')}</span>
                <span data-test="table-count">{t('booking.tables', { n: quote.tableCount })}</span>
              </div>
              <div className="est-row">
                <span>{t('booking.unitPrice')}</span>
                <span data-test="unit-price">{dd.tien(quote.unitPrice)}</span>
              </div>
              <div className="est-row">
                <span>{t('booking.foodAmount')}</span>
                <span data-test="food-total">{dd.tien(quote.foodAmount)}</span>
              </div>
            </>
          )}
          <div className="est-row">
            <span>{t('booking.spaceFee')}</span>
            <span data-test="rental-fee">{Number(quote.spaceFee) === 0 ? t('track.free') : dd.tien(quote.spaceFee)}</span>
          </div>
          {Number(quote.discountAmount) > 0 && (
            <div className="est-row">
              <span>{t('booking.discount')}</span>
              <span data-test="discount">− {dd.tien(quote.discountAmount)}</span>
            </div>
          )}
          <div className="est-row">
            <span>{t('booking.vat', { ti: Number(quote.vatRate) * 100 })}</span>
            <span data-test="vat">{dd.tien(quote.vatAmount)}</span>
          </div>

          <div className="est-total">
            <span>{t('booking.subtotal')}</span>
            <span className="val" data-test="grand-total">{dd.tien(quote.totalAmount)}</span>
          </div>

          <div className="est-row" style={{ borderBottom: 'none' }}>
            <span>{t('booking.deposit')}</span>
            <span data-test="deposit">{dd.tien(quote.depositAmount)}</span>
          </div>

          {quyTac.length > 0 && (
            <ul className="est-rules" data-test="price-rules">
              {quyTac.map((rule) => (
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
  const { t, tDb, lang } = useI18n();
  const dd = useDinhDang();
  const nhan = (viet, anh) => (lang === 'en' && anh ? anh : viet);

  return (
    <section className="section">
      <div className="wrap" style={{ maxWidth: 720 }}>
        <div className="card">
          <div className="card-body center">
            <div style={{ fontSize: '3rem' }}>✅</div>
            <h2>{t('booking.doneTitle')}</h2>
            <p className="muted" style={{ marginBottom: 24 }}>
              {t('booking.donePrefix')} <strong data-test="booking-code">{result.code}</strong>{t('booking.doneSuffix')}
            </p>

            <table>
              <tbody>
                <tr>
                  <th>{t('track.eventType')}</th>
                  <td>{nhan(result.eventTypeLabel, result.eventTypeLabelEn)}</td>
                </tr>
                <tr>
                  <th>{t('track.dateSlot')}</th>
                  <td>
                    {dd.ngay(result.eventDate)} · {nhan(result.timeSlotLabel, result.timeSlotLabelEn)}
                  </td>
                </tr>
                <tr>
                  <th>{t('track.guests')}</th>
                  <td>
                    {result.tableCount > 0
                      ? t('track.guestsWithTables', { khach: result.guestCount, mam: result.tableCount })
                      : t('common.guests', { n: result.guestCount })}
                  </td>
                </tr>
                <tr>
                  <th>{t('track.space')}</th>
                  <td>{tDb(result, 'spaceName')}</td>
                </tr>
                <tr>
                  <th>{t('track.package')}</th>
                  <td>{tDb(result, 'packageName')}</td>
                </tr>
                <tr>
                  <th>{t('booking.subtotal')}</th>
                  <td>
                    <strong>{dd.tien(result.totalAmount)}</strong>
                  </td>
                </tr>
              </tbody>
            </table>

            <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginTop: 24 }}>
              <button type="button" className="btn btn-outline" onClick={onReset}>
                {t('booking.sendAnother')}
              </button>
              <Link to={`/tra-cuu?code=${result.code}`} className="btn btn-outline">
                {t('booking.lookup')}
              </Link>
              {/* Đóng cọc là việc tiếp theo khách phải làm, để nút đó nổi nhất */}
              <Link to={`/thanh-toan?ma=${result.code}`} className="btn btn-gold" data-test="go-pay">
                {t('booking.payDeposit')}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
