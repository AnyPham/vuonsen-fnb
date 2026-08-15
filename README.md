# Vườn Sen — Nền tảng website dịch vụ F&B

Khóa luận tốt nghiệp: *"Nghiên cứu và ứng dụng Spring Boot, ReactJS và SQL trong xây dựng nền tảng
website dịch vụ nhà hàng & cho thuê không gian sự kiện F&B"*
— Phạm Trần Tuấn Anh (21130004, DH21DTA) · GVHD: TS. Nguyễn Thị Phương Trâm

---

## 1. Kiến trúc tổng thể

```
Trình duyệt
    │
    ▼
┌──────────────────────┐   REST + JWT   ┌───────────────────────┐   JPA/JDBC   ┌─────────┐
│  ReactJS + Redux     │ ─────────────► │  Spring Boot 3.3      │ ───────────► │ MySQL 8 │
│  Vite · React Router │ ◄───────────── │  Security · JPA       │ ◄─────────── │         │
└──────────────────────┘     JSON       └───────────────────────┘              └─────────┘
```

Backend chia theo **module nghiệp vụ** (không chia theo tầng kỹ thuật), mỗi module tự chứa
entity – repository – service – controller:

```
backend/src/main/java/vn/vuonsen/fnb/
├── common/          Lớp dùng chung: BaseEntity, xử lý lỗi tập trung, DTO phân trang
├── config/          SecurityConfig, OpenAPI, tài khoản admin khởi tạo, các lớp @ConfigurationProperties
├── security/        JwtService, JwtAuthenticationFilter, AppUserDetails
└── modules/
    ├── auth/        Đăng ký · đăng nhập · refresh token
    ├── user/        Hồ sơ người dùng, phân quyền CUSTOMER / STAFF / ADMIN
    ├── space/       6 không gian sự kiện + bộ lọc theo sức chứa / loại / giá
    ├── menu/        Danh mục & món ăn (hệ thống tab)
    ├── partypackage/ Gói tiệc tính theo mâm
    ├── booking/     ★ Đặt tiệc, PricingService (công thức tính giá), vòng đời đơn
    ├── review/      Đánh giá khách hàng (duyệt trước khi hiển thị)
    └── gallery/     Thư viện ảnh
```

Frontend tổ chức theo **feature-based** — chuẩn khuyến nghị của Redux Toolkit:

```
frontend/src/
├── api/          axiosClient (interceptor tự làm mới token) + endpoints
├── app/          store.js
├── features/     authSlice · catalogSlice · bookingSlice
├── components/   layout (Header/Footer) · common (GoogleMap, StateBlock)
├── pages/        Từng màn hình, kể cả khu quản trị
├── routes/       ProtectedRoute (chặn theo quyền)
└── styles/       global.css — giữ nguyên bảng màu của prototype
```

---

## 2. Chạy dự án

### Cách A — Chạy nhanh, **không cần cài MySQL** (khuyến nghị khi mới bắt đầu)

Backend chạy profile `dev` với H2 in-memory; Flyway tự tạo bảng và nạp dữ liệu mẫu.

```bash
cd backend && mvn spring-boot:run
```

```bash
cd frontend && npm install && npm run dev
```

| Địa chỉ | Nội dung |
|---|---|
| http://localhost:5173 | Giao diện website |
| http://localhost:8080/swagger-ui.html | Tài liệu API tương tác |
| http://localhost:8080/h2-console | Xem CSDL (JDBC URL: `jdbc:h2:mem:vuonsen`, user `sa`, mật khẩu để trống) |

Tài khoản quản trị mặc định: `admin@vuonsen.vn` / `Admin@123`

### Cách B — Chạy với MySQL thật

```sql
CREATE DATABASE vuonsen_fnb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Thông tin kết nối lấy từ biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD` (xem `application.yml`).

### Cách C — Docker (đủ 3 dịch vụ)

```bash
cp .env.example .env
```

Điền `JWT_SECRET` và `ADMIN_PASSWORD` vào `.env`, sau đó:

```bash
docker compose up -d --build
```

Website tại http://localhost, API tại http://localhost:8080.

---

## 3. Công thức tính giá tiệc

Toàn bộ quy tắc nằm ở một chỗ duy nhất: `PricingService`. Frontend **không** tự tính lại
mà luôn gọi `POST /api/v1/bookings/quote`, nhờ vậy số tiền trên màn hình và trong đơn
không bao giờ lệch nhau.

| Thành phần | Cách tính |
|---|---|
| Số mâm | `ceil(số khách / 10)` |
| Tiền ăn | `số mâm × giá gói/mâm` |
| Phí không gian | `0` nếu ≥ 30 mâm; loại tính theo chòi thì `phí × số chòi` |
| Giảm giá | `5% × (tiền ăn + phí không gian)` nếu đặt trước ≥ 60 ngày |
| VAT | `8% × (tiền ăn + phí không gian − giảm giá)` |
| **Tổng cộng** | `tiền ăn + phí không gian − giảm giá + VAT` |

Các tham số (10 khách/mâm, VAT 8%, mốc 30 mâm, 60 ngày, 5%) đặt trong `application.yml`
nhóm `app.booking` — đổi chính sách giá không phải sửa mã nguồn.

Chạy kiểm thử công thức:

```bash
cd backend && mvn test
```

---

## 4. Bảng dữ liệu

Flyway quản lý schema (`backend/src/main/resources/db/migration`), Hibernate chỉ ở chế độ
`validate` — sai lệch giữa entity và bảng sẽ báo lỗi ngay khi khởi động.

| Bảng | Vai trò |
|---|---|
| `users`, `refresh_tokens` | Tài khoản, phân quyền, phiên đăng nhập |
| `spaces`, `space_amenities`, `space_images` | Không gian sự kiện và tiện ích |
| `dish_categories`, `dishes` | Thực đơn theo danh mục |
| `party_packages`, `package_features` | Gói tiệc và nội dung từng gói |
| `bookings`, `booking_status_history` | Đơn đặt tiệc và nhật ký đổi trạng thái |
| `reviews` | Đánh giá khách hàng |
| `gallery_images` | Thư viện ảnh |

Bảng `bookings` lưu **bản sao** giá tại thời điểm đặt (`unit_price`, `food_amount`, `vat_amount`…).
Nhà hàng tăng giá sau này không làm thay đổi số tiền đã báo cho khách.

Vòng đời đơn: `PENDING → CONFIRMED → COMPLETED`, có thể `CANCELLED` từ hai trạng thái đầu.
Bước chuyển không hợp lệ bị `BookingStatus.canTransitionTo()` chặn lại.

---

## 5. Bảo mật

- Mật khẩu băm bằng **BCrypt**, không bao giờ lưu dạng rõ.
- **Access token** (60 phút) dùng gọi API; **refresh token** (14 ngày) lưu trong CSDL nên
  có thể thu hồi khi đăng xuất — điều mà JWT thuần túy không làm được.
- Refresh token **xoay vòng**: token cũ bị thu hồi ngay khi dùng, chống tấn công phát lại.
- API stateless, tắt CSRF, CORS chỉ mở cho origin khai báo trong `app.cors.allowed-origins`.
- Phân quyền hai lớp: theo đường dẫn ở `SecurityConfig`, theo phương thức bằng `@PreAuthorize`.

---

## 6. Danh mục API chính

| Phương thức | Đường dẫn | Quyền |
|---|---|---|
| `POST` | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh` | Công khai |
| `GET` | `/api/v1/spaces` (lọc `guests`, `type`, `maxPrice`) | Công khai |
| `GET` | `/api/v1/menu/categories`, `/api/v1/menu/dishes` | Công khai |
| `GET` | `/api/v1/packages` | Công khai |
| `POST` | `/api/v1/bookings/quote` | Công khai |
| `POST` | `/api/v1/bookings` | Công khai (khách vãng lai đặt được) |
| `GET` | `/api/v1/bookings/track/{code}` | Công khai |
| `GET` | `/api/v1/bookings/my` | Đã đăng nhập |
| `GET` | `/api/v1/admin/bookings` | ADMIN / STAFF |
| `PATCH` | `/api/v1/admin/bookings/{id}/status` | ADMIN / STAFF |

Danh sách đầy đủ xem tại Swagger UI.

---

## 7. Việc còn lại

Khung đã dựng xong phần lõi. Các hạng mục nên làm tiếp:

- [ ] Tải ảnh thật cho không gian, món ăn, thư viện (hiện dùng khối màu giữ chỗ)
- [ ] Gửi email xác nhận đơn đặt tiệc (`BookingService.create` đã đánh dấu chỗ cắm)
- [ ] Màn hình quản trị cho không gian / thực đơn / gói tiệc (API backend đã sẵn sàng)
- [ ] Tích hợp cổng thanh toán đặt cọc (VNPay hoặc MoMo)
- [ ] Bổ sung kiểm thử tích hợp cho luồng đặt tiệc bằng MockMvc
- [ ] Tối ưu SEO: thẻ meta động, sitemap, dữ liệu có cấu trúc `schema.org/Restaurant`

---

## 8. Lưu ý về môi trường Windows

Thư mục dự án đặt tại `E:\Web-ThietKe-DichVu-FB-TLTN`.

Tên cũ chứa ký tự `&` đã làm hỏng mọi lệnh `npm run`: `cmd.exe` hiểu `&` là ký tự nối lệnh
nên đường dẫn bị cắt đôi, báo lỗi `'B' is not recognized as an internal or external command`.
Thư mục đã được đổi sang dạng ASCII, không dấu, không khoảng trắng nên lỗi này không còn.

**Giữ nguyên quy ước đặt tên này** khi tạo thư mục con hoặc di chuyển dự án — tránh ký tự
`&`, `%`, `^` và dấu tiếng Việt trong đường dẫn để không gặp lại vấn đề tương tự với npm,
Docker hay các công cụ dòng lệnh khác.

### Mở bằng IntelliJ IDEA

Mở **thư mục gốc** của dự án (không phải riêng `backend/`) để thấy cả hai phần. Khi IntelliJ
hiện thông báo *"Maven build script found"*, bấm **Load** để nó nhận `backend/pom.xml` và tải
thư viện. Sau đó chạy `FnbApplication` bằng nút ▶ bên cạnh hàm `main`.

Frontend chạy ở terminal tích hợp (`Alt+F12`):

```bash
cd frontend && npm run dev
```
