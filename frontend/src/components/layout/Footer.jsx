import GoogleMap from '@/components/common/GoogleMap';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="wrap">
        <div className="grid grid-3">
          <div>
            <h4>🌿 Vườn Sen</h4>
            <p>Khu ẩm thực sân vườn và dịch vụ cho thuê không gian tiệc ven sông Sài Gòn.</p>
          </div>

          <div>
            <h4>Liên hệ</h4>
            <p>1147 Bình Quới, Phường 28, Bình Thạnh, TP.HCM</p>
            <p>
              Điện thoại: <a href="tel:+842812345678">(028) 1234 5678</a>
            </p>
            <p>
              Email: <a href="mailto:datban@vuonsen.vn">datban@vuonsen.vn</a>
            </p>
            <p>Mở cửa: 9:00 — 22:00 hằng ngày</p>
          </div>

          <div>
            <h4>Vị trí</h4>
            <GoogleMap height={180} />
          </div>
        </div>

        <div className="footer-bottom">
          © {new Date().getFullYear()} Vườn Sen · Tiểu luận tốt nghiệp — Phạm Trần Tuấn Anh (21130004)
        </div>
      </div>
    </footer>
  );
}
