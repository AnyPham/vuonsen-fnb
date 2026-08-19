# Vườn Sen

Website dịch vụ nhà hàng và cho thuê không gian tổ chức tiệc. Khách hàng xem không gian, thực đơn,
gói tiệc và đặt tiệc trực tuyến với chi phí được tính tự động. Quản trị viên duyệt và theo dõi đơn.

Đồ án tốt nghiệp ngành Công nghệ thông tin, Trường Đại học Nông Lâm TP.HCM.

## Công nghệ sử dụng

**Backend**

- Java 17, Spring Boot 3.3
- Spring Security, JWT
- Spring Data JPA, Flyway
- MySQL 8 (dùng H2 khi chạy thử)

**Frontend**

- React 18, Vite
- Redux Toolkit
- React Router 6
- Axios

**Khác:** Docker, Swagger UI

## Tính năng

Phía khách hàng:

- Đăng ký, đăng nhập bằng JWT
- Xem 6 không gian sự kiện, lọc theo số khách, loại không gian và giá thuê
- Xem thực đơn chia tab theo danh mục món
- Xem các gói tiệc tính theo mâm
- Thư viện ảnh, bấm vào ảnh để xem phóng to
- Đặt tiệc qua form 3 bước, chi phí tính tự động theo số khách
- Tra cứu đơn bằng mã đơn, không cần tài khoản
- Xem lại lịch sử đơn đã đặt

Phía quản trị:

- Danh sách đơn, lọc theo trạng thái, khoảng ngày và từ khoá
- Duyệt, hoàn thành hoặc huỷ đơn
- Thống kê số đơn theo từng trạng thái

Phần quản lý không gian và duyệt đánh giá hiện mới có API, chưa làm giao diện.

## Yêu cầu

- JDK 17 trở lên
- Node.js 18 trở lên
- Maven 3.9 trở lên
- MySQL 8 (không bắt buộc, xem cách chạy nhanh bên dưới)

## Cài đặt và chạy

### Cách 1: Chạy nhanh, không cần cài MySQL

Backend dùng H2 chạy trên RAM, Flyway tự tạo bảng và nạp sẵn dữ liệu mẫu.

```bash
cd backend
mvn spring-boot:run
```

Mở terminal thứ hai:

```bash
cd frontend
npm run dev
```

- Giao diện: http://localhost:5173
- Tài liệu API: http://localhost:8080/swagger-ui.html
- Xem cơ sở dữ liệu: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:vuonsen`, user `sa`, mật khẩu để trống)

Tài khoản quản trị mặc định: `admin@vuonsen.vn` / `Admin@123`

### Cách 2: Chạy với MySQL

Tạo database:

```sql
CREATE DATABASE vuonsen_fnb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Đặt các biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD`, sau đó:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Cách 3: Chạy bằng Docker

```bash
cp .env.example .env
```

Điền `JWT_SECRET` và `ADMIN_PASSWORD` vào file `.env`, rồi chạy:

```bash
docker compose up -d --build
```

Website chạy tại http://localhost, API tại http://localhost:8080.

## Cấu trúc thư mục

```
backend/
  src/main/java/vn/vuonsen/fnb/
    common/       Lớp dùng chung, xử lý lỗi tập trung
    config/       Cấu hình bảo mật, Swagger, tài khoản admin
    security/     Xử lý JWT
    modules/      auth, user, space, menu, partypackage, booking, review, gallery
  src/main/resources/
    db/migration/ Script tạo bảng và dữ liệu mẫu

frontend/
  src/
    api/          Gọi API
    features/     Redux slice
    components/   Component dùng chung
    pages/        Các trang giao diện
    routes/       Phân quyền truy cập trang
```

Mỗi module ở backend tự chứa đầy đủ entity, repository, service và controller của nghiệp vụ đó.

## Cách tính giá tiệc

Công thức nằm trong `PricingService`. Frontend gọi API `POST /api/v1/bookings/quote` để lấy kết quả
chứ không tự tính lại, nhờ vậy số tiền hiển thị và số tiền lưu trong đơn luôn khớp nhau.

```
số mâm         = làm tròn lên (số khách / 10)
tiền ăn        = số mâm x giá một mâm của gói tiệc
mức tối thiểu  = phí thuê không gian x 10
phí không gian = 0 nếu tiền ăn đạt mức tối thiểu,
                 chưa đạt thì trả theo tỉ lệ còn thiếu
giảm giá       = 5% nếu đặt trước từ 60 ngày
VAT            = 8%
tổng cộng      = tiền ăn + phí không gian - giảm giá + VAT
tiền cọc       = 30% tổng cộng
```

Phí thuê giảm dần theo tiền ăn thay vì miễn phí đột ngột, để khách đặt đông hơn không bao giờ
trả ít tiền hơn khách đặt ít.

Các tham số đặt trong `application.yml` phần `app.booking`, đổi chính sách giá không cần sửa
mã nguồn.

## API chính

| Phương thức | Đường dẫn | Quyền |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Công khai |
| GET | `/api/v1/spaces` | Công khai |
| GET | `/api/v1/menu/categories`, `/api/v1/menu/dishes` | Công khai |
| GET | `/api/v1/packages` | Công khai |
| POST | `/api/v1/bookings/quote` | Công khai |
| POST | `/api/v1/bookings` | Công khai |
| GET | `/api/v1/bookings/track/{code}` | Công khai |
| GET | `/api/v1/bookings/my` | Cần đăng nhập |
| GET | `/api/v1/admin/bookings` | ADMIN, STAFF |
| PATCH | `/api/v1/admin/bookings/{id}/status` | ADMIN, STAFF |

Danh sách đầy đủ xem tại Swagger UI.

## Cơ sở dữ liệu

Flyway quản lý toàn bộ script tạo bảng trong `backend/src/main/resources/db/migration`.
Hibernate chỉ ở chế độ `validate` nên nếu entity lệch với bảng sẽ báo lỗi ngay khi khởi động.

Các bảng chính: `users`, `refresh_tokens`, `spaces`, `dishes`, `party_packages`, `bookings`,
`reviews`, `gallery_images`.

Bảng `bookings` lưu lại giá tại thời điểm khách đặt, nên sau này nhà hàng tăng giá thì
số tiền của đơn cũ vẫn giữ nguyên.

## Kiểm thử

```bash
cd backend
mvn test
```

## Hướng phát triển

- Thay ảnh giữ chỗ bằng ảnh thật của nhà hàng
- Gửi email xác nhận khi khách đặt tiệc
- Bổ sung trang quản trị cho thực đơn và gói tiệc
- Tích hợp thanh toán đặt cọc qua VNPay hoặc MoMo
- Viết thêm kiểm thử tích hợp cho luồng đặt tiệc

## Tác giả

Phạm Trần Tuấn Anh — MSSV 21130004, lớp DH21DTA
Khoa Công nghệ thông tin, Trường Đại học Nông Lâm TP.HCM

Giảng viên hướng dẫn: TS. Nguyễn Thị Phương Trâm
