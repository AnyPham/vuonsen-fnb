import { pushApi } from '@/api/endpoints';

/*
 * Đăng ký nhận thông báo đẩy của trình duyệt.
 *
 * Ba thứ phải có đủ thì mới chạy được: service worker, Push API và Notification API.
 * Safari trên iOS chỉ cho dùng khi khách đã thêm website vào màn hình chính, nên phải
 * kiểm từng thứ thay vì giả định trình duyệt nào cũng có.
 */

export function trinhDuyetHoTro() {
  return (
    typeof window !== 'undefined'
    && 'serviceWorker' in navigator
    && 'PushManager' in window
    && 'Notification' in window
  );
}

export function trangThaiQuyen() {
  if (!trinhDuyetHoTro()) return 'khong-ho-tro';
  return Notification.permission; // 'default' | 'granted' | 'denied'
}

/* Khóa công khai VAPID là chuỗi base64url, Push API lại đòi mảng byte. */
function sangMangByte(base64Url) {
  const dem = '='.repeat((4 - (base64Url.length % 4)) % 4);
  const base64 = (base64Url + dem).replace(/-/g, '+').replace(/_/g, '/');
  const tho = window.atob(base64);
  return Uint8Array.from([...tho].map((c) => c.charCodeAt(0)));
}

async function dangKySW() {
  const dk = await navigator.serviceWorker.register('/sw.js');
  await navigator.serviceWorker.ready;
  return dk;
}

/*
 * Bật nhận thông báo. Trả về mã lý do thay vì ném lỗi, để giao diện nói cho khách biết
 * vì sao không bật được chứ không chỉ im lặng.
 */
export async function batThongBao(ngonNgu = 'vi') {
  if (!trinhDuyetHoTro()) return 'khong-ho-tro';

  const cauHinh = await pushApi.khoaCongKhai().catch(() => null);
  if (!cauHinh || !cauHinh.bat || !cauHinh.khoa) return 'may-chu-chua-bat';

  const quyen = await Notification.requestPermission();
  if (quyen !== 'granted') return 'tu-choi';

  const dk = await dangKySW();
  const daCo = await dk.pushManager.getSubscription();
  const thueBao = daCo || (await dk.pushManager.subscribe({
    /* Bắt buộc true: trình duyệt không cho đăng ký thầm rồi gửi tin không hiện gì. */
    userVisibleOnly: true,
    applicationServerKey: sangMangByte(cauHinh.khoa),
  }));

  const json = thueBao.toJSON();
  await pushApi.dangKy({
    endpoint: thueBao.endpoint,
    p256dh: json.keys.p256dh,
    auth: json.keys.auth,
    ngonNgu,
  });
  return 'da-bat';
}

export async function tatThongBao() {
  if (!trinhDuyetHoTro()) return;
  const dk = await navigator.serviceWorker.getRegistration();
  const thueBao = dk && (await dk.pushManager.getSubscription());
  if (!thueBao) return;
  await pushApi.huyDangKy(thueBao.endpoint).catch(() => {});
  await thueBao.unsubscribe();
}

/* Máy này đã đăng ký chưa. Dùng để hiện đúng trạng thái nút lúc mở trang. */
export async function dangBat() {
  if (!trinhDuyetHoTro() || Notification.permission !== 'granted') return false;
  const dk = await navigator.serviceWorker.getRegistration();
  if (!dk) return false;
  return Boolean(await dk.pushManager.getSubscription());
}
