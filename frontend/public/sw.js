/*
 * Service worker của Vườn Sen.
 *
 * Chỉ làm một việc: nhận thông báo đẩy và hiện lên cho khách, kể cả khi khách đã đóng tab.
 * Cố ý không làm bộ nhớ đệm ngoại tuyến. Dữ liệu của website là giá tiệc và lịch trống,
 * hai thứ đổi liên tục; cho khách xem bản cũ đã lưu còn tệ hơn là báo mất mạng.
 */

self.addEventListener('install', () => {
  /* Kích hoạt ngay, không chờ tab cũ đóng hết, để khách vừa cho phép là nhận được luôn. */
  self.skipWaiting();
});

self.addEventListener('activate', (su) => {
  su.waitUntil(self.clients.claim());
});

self.addEventListener('push', (su) => {
  let tin = {};
  try {
    tin = su.data ? su.data.json() : {};
  } catch {
    tin = {};
  }

  const tieuDe = tin.tieuDe || 'Vườn Sen';
  const tuyChon = {
    body: tin.noiDung || 'Có ưu đãi mới cho dịp lễ sắp tới.',
    icon: '/icon-192.png',
    badge: '/icon-192.png',
    /* Cùng một thẻ thì tin mới thay tin cũ, khách không bị dồn một chồng thông báo. */
    tag: 'vuonsen-khuyen-mai',
    renotify: true,
    data: { duongDan: tin.duongDan || '/uu-dai' },
    actions: [{ action: 'mo', title: 'Xem ưu đãi' }],
  };

  su.waitUntil(self.registration.showNotification(tieuDe, tuyChon));
});

self.addEventListener('notificationclick', (su) => {
  su.notification.close();
  const duongDan = (su.notification.data && su.notification.data.duongDan) || '/uu-dai';

  /*
   * Đang mở sẵn một tab của website thì chuyển tab đó sang trang ưu đãi, chứ không mở
   * thêm tab mới. Bấm thông báo ba lần mà ra ba tab thì phiền.
   */
  su.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((cacTab) => {
      for (const tab of cacTab) {
        if (new URL(tab.url).origin === self.location.origin && 'focus' in tab) {
          tab.navigate(duongDan);
          return tab.focus();
        }
      }
      return self.clients.openWindow(duongDan);
    }),
  );
});
