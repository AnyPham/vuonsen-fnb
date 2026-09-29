<div align="center">

# 🌿 Vườn Sen

**Nền tảng đặt tiệc và cho thuê không gian sự kiện F&B**

Website đặt tiệc trọn gói với báo giá tự động, đặt món lẻ, thanh toán VietQR và VNPay,
trợ lý tư vấn, giao diện song ngữ Việt – Anh.

[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)](https://vite.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Selenium](https://img.shields.io/badge/Selenium-4.40-43B02A?logo=selenium&logoColor=white)](https://www.selenium.dev/)

[Bắt đầu nhanh](#-bắt-đầu-nhanh) ·
[Cài đặt đầy đủ](#-cài-đặt-đầy-đủ) ·
[Kiểm thử](#-kiểm-thử) ·
[Cấu trúc dự án](#-cấu-trúc-dự-án) ·
[API](#-api)

</div>

---

## Giới thiệu

Vườn Sen là khu ẩm thực sân vườn ven sông kiêm dịch vụ cho thuê không gian tổ chức tiệc.
Dự án số hoá toàn bộ quy trình, từ lúc khách tìm hiểu cho tới lúc nhà hàng thu tiền.

**Phía khách** — chọn không gian, xem thực đơn, nhận báo giá tự động theo số khách và ngày
tổ chức, gửi yêu cầu đặt tiệc, đặt món lẻ giao tận nhà, tra cứu đơn bằng mã và thanh toán
bằng mã QR ngân hàng.

**Phía nhân viên và quản trị** — duyệt đơn, ghi nhận tiền cọc, đối soát phiếu thu, quản lý
thực đơn, không gian, ngày lễ giảm giá và tài khoản người dùng.

Đề tài tốt nghiệp ngành Công nghệ thông tin, Trường Đại học Nông Lâm TP.HCM.

### Quy mô hiện tại

| Hạng mục | Số lượng |
|---|---|
| Không gian sự kiện | 6 |
| Món ăn trong thực đơn | 126 |
| Gói tiệc | 3 |
| Module nghiệp vụ (backend) | 15 |
| Controller | 24 |
| Trang giao diện | 32 |
| Migration cơ sở dữ liệu | 22 |
| Kiểm thử đơn vị và tích hợp | 158 bài |
| Kiểm thử giao diện (Selenium) | 130 test case / 15 luồng |

---

## 🚀 Bắt đầu nhanh

Cách nhanh nhất để xem website chạy — **không cần cài MySQL**. Backend dùng H2 chạy trên RAM
và tự nạp sẵn dữ liệu mẫu.

**Yêu cầu:** JDK 17+, Node.js 18+, Maven 3.9+

```bash
git clone https://github.com/AnyPham/vuonsen-fnb.git
cd vuonsen-fnb
```

Mở **hai cửa sổ dòng lệnh**.

Cửa sổ 1 — backend:

```bash
cd backend
mvn spring-boot:run
```

Cửa sổ 2 — frontend:

```bash
cd frontend
npm install
npm run dev
```

Mở trình duyệt vào **<http://localhost:5173>**. Xong.

| Địa chỉ | Dùng để |
|---|---|
| <http://localhost:5173> | Website |
| <http://localhost:8080/swagger-ui.html> | Tài liệu API, thử API trực tiếp |
| <http://localhost:8080/h2-console> | Xem cơ sở dữ liệu — JDBC URL `jdbc:h2:mem:vuonsen`, user `sa`, bỏ trống mật khẩu |

**Tài khoản quản trị mặc định:** `admin@vuonsen.vn` / `Admin@123`

> [!WARNING]
> Mật khẩu này chỉ dành cho môi trường phát triển. Trước khi đưa website ra Internet,
> đặt biến môi trường `ADMIN_PASSWORD` thành mật khẩu khác.

Dữ liệu H2 nằm trên RAM nên tắt backend là mất. Muốn giữ lại dữ liệu thì dùng MySQL
theo hướng dẫn bên dưới.

---

## 🛠 Cài đặt đầy đủ

### Cách 1 — MySQL cài trên máy

Phù hợp khi phát triển lâu dài và cần dữ liệu không mất sau mỗi lần khởi động lại.

**Bước 1.** Tạo cơ sở dữ liệu rỗng. Flyway tự tạo toàn bộ bảng khi backend khởi động,
không phải chạy script SQL nào bằng tay.

```sql
CREATE DATABASE vuonsen_fnb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Bước 2.** Chạy backend với profile `local`. Profile này kết nối `localhost:3306`,
user `root`, mật khẩu để trống — đúng cấu hình mặc định của XAMPP.

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Dùng user hoặc mật khẩu khác thì truyền biến môi trường, không sửa tệp cấu hình:

```bash
DB_URL="jdbc:mysql://localhost:3306/vuonsen_fnb?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh" \
DB_USER=root \
DB_PASSWORD=matkhaucuaban \
mvn spring-boot:run
```

**Bước 3.** Chạy frontend như phần bắt đầu nhanh.

**Bước 4 — không bắt buộc.** Nạp bộ dữ liệu 24 tháng để xem hệ thống ở mức tải gần thực tế:
khoảng 1.500 khách hàng, 1.600 đơn đặt tiệc, 1.500 đơn đặt món và 460 đánh giá.

```bash
mysql -u root vuonsen_fnb < cong-cu/du-lieu-mau/du-lieu-lon.sql
```

### Cách 2 — Docker

Chạy cả ba dịch vụ bằng một lệnh: MySQL, backend và frontend phục vụ qua Nginx.

```bash
cp .env.example .env
```

Mở `.env` và điền hai giá trị **bắt buộc** — thiếu là container không khởi động:

```env
JWT_SECRET=           # tối thiểu 32 ký tự, sinh bằng: openssl rand -base64 48
ADMIN_PASSWORD=       # mật khẩu tài khoản quản trị
```

```bash
docker compose up -d --build
```

Website chạy ở <http://localhost>, API ở <http://localhost:8080>.

```bash
docker compose logs -f backend   # xem log
docker compose down              # dừng, giữ lại dữ liệu
docker compose down -v           # dừng và xoá sạch dữ liệu
```

### Tuỳ chọn cho frontend

Frontend chạy được ngay mà không cần tệp `.env`. Muốn bật bản đồ Google hoặc đổi địa chỉ API
thì tạo tệp cấu hình:

```bash
cd frontend
cp .env.example .env
```

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `VITE_API_BASE_URL` | *(trống)* | Để trống thì dùng proxy của Vite trỏ về `localhost:8080` |
| `VITE_GOOGLE_MAPS_API_KEY` | *(trống)* | Để trống thì bản đồ vẫn hiện bằng đường nhúng không cần khoá |
| `VITE_MAP_LAT` / `VITE_MAP_LNG` | Toạ độ nhà hàng | Vị trí ghim trên bản đồ |

### Ba profile của backend

| Profile | Cơ sở dữ liệu | Dùng khi |
|---|---|---|
| `dev` *(mặc định)* | H2 trên RAM | Chạy thử, không cần cài gì thêm |
| `local` | MySQL `localhost:3306` | Phát triển với dữ liệu lưu lại được |
| `prod` | MySQL qua biến môi trường | Chạy thật, dùng cho Docker |

---

## 🌍 Dịch vụ ngoài — đều không bắt buộc

Dự án chạy đầy đủ khi chưa khai báo dịch vụ nào bên dưới. Mỗi tính năng tự lùi về cơ chế
dự phòng thay vì báo lỗi, nên tải mã nguồn về là chạy được ngay, không bị chặn ở bước
đăng ký dịch vụ.

| Tính năng | Biến môi trường | Chưa khai báo thì sao |
|---|---|---|
| Gửi email xác nhận | `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | Ghi toàn văn thư vào bảng `email_logs`, luồng nghiệp vụ vẫn chạy trọn vẹn |
| Cổng thanh toán VNPay | `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` | Giao diện ẩn nút VNPay, khách trả bằng mã VietQR |
| Chuyển khoản VietQR | Mục `app.payment` trong `application.yml` | Dùng số tài khoản thử, kèm cảnh báo đừng chuyển tiền thật |
| Trợ lý tư vấn AI | `ASSISTANT_LLM_API_KEY` | Dựng câu trả lời từ dữ liệu thật trong cơ sở dữ liệu |
| Bản đồ Google | `VITE_GOOGLE_MAPS_API_KEY` | Hiện bản đồ bằng đường nhúng không cần khoá |

---

## 🧪 Kiểm thử

### Kiểm thử phía máy chủ — 158 bài

Chạy trên H2 nên **không cần MySQL và không phải khởi động gì trước**.

```bash
cd backend
mvn test
```

### Kiểm thử giao diện bằng Selenium — 130 test case

Bộ kiểm thử điều khiển trình duyệt thật, nên **backend và frontend phải đang chạy**
trước khi gọi lệnh. Cần cài sẵn Google Chrome.

```bash
# Cửa sổ 1
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Cửa sổ 2
cd frontend && npm run dev

# Cửa sổ 3
cd kiemthu && mvn test
```

Chạy riêng một luồng hoặc một test case:

```bash
mvn test -Dtest=KiemThuDatTiec                      # cả luồng đặt tiệc
mvn test -Dtest=KiemThuDatTiec#tcBook01_diTronBaBuoc # một test case
```

Đổi địa chỉ website hoặc API mà bộ kiểm thử trỏ tới:

```bash
mvn test -Dbase.url=http://localhost:4173 -Dapi.url=http://localhost:8080
```

<details>
<summary><b>15 luồng nghiệp vụ được kiểm thử</b></summary>

<br>

| Luồng | Nội dung | Test case |
|---|---|---|
| 1 | Đăng ký và đăng nhập | 10 |
| 2 | Xem và lọc không gian | 8 |
| 3 | Trang chi tiết không gian | 6 |
| 4 | Thực đơn và chi tiết món | 8 |
| 5 | Đặt tiệc ba bước | 13 |
| 6 | Ràng buộc khi đặt tiệc | 11 |
| 7 | Tra cứu đơn đặt tiệc | 4 |
| 8 | Đặt món lẻ | 12 |
| 9 | Ràng buộc khi đặt món | 9 |
| 10 | Gửi đánh giá | 7 |
| 11 | Trợ lý tư vấn | 5 |
| 12 | Phân quyền | 7 |
| 13 | Quản trị đơn | 8 |
| 14 | Giảm giá ngày lễ | 10 |
| 15 | Quản trị ngày lễ | 12 |

</details>

### Kiểm tra bản dịch

Hai bước kiểm tra riêng cho phần song ngữ. Chúng bắt được loại lỗi mà trình biên dịch bỏ qua,
vì đó là lỗi lúc chạy chứ không phải lỗi cú pháp.

```bash
node cong-cu/kiem-tra/doi-chieu-ban-dich.js   # hai tệp ngôn ngữ có lệch khoá không
node cong-cu/kiem-tra/kiem-hook-dich.js       # có chỗ nào gọi hàm dịch mà thiếu hook không
```

---

## 📁 Cấu trúc dự án

```
vuonsen-fnb/
├── backend/            Spring Boot — API, nghiệp vụ, cơ sở dữ liệu
├── frontend/           React + Vite — giao diện người dùng
├── kiemthu/            Selenium + JUnit — kiểm thử giao diện tự động
├── cong-cu/            Script sinh dữ liệu mẫu, sinh tài liệu, kiểm tra bản dịch
└── docker-compose.yml
```

### Backend chia theo module nghiệp vụ

Mỗi thư mục trong `modules/` tự chứa đầy đủ entity, repository, service và controller của
nghiệp vụ đó, thay vì gom hết entity vào một chỗ và hết service vào một chỗ khác.

```
backend/src/main/java/vn/vuonsen/fnb/
├── common/               Lớp cơ sở, xử lý ngoại lệ, từ điển song ngữ
├── config/               Cấu hình và tham số nghiệp vụ đọc từ application.yml
├── security/             JWT, phân quyền
└── modules/
    ├── auth/             Đăng ký, đăng nhập, quên mật khẩu
    ├── booking/          Đặt tiệc, tính giá, đặt cọc
    ├── dishorder/        Đặt món lẻ
    ├── payment/          VietQR, VNPay, sổ phiếu thu
    ├── menu/             Thực đơn và món ăn
    ├── space/            Không gian sự kiện
    ├── partypackage/     Gói tiệc
    ├── holiday/          Ngày lễ giảm giá
    ├── review/           Đánh giá của khách
    ├── gallery/          Thư viện ảnh
    ├── statistic/        Thống kê doanh thu
    ├── notification/     Gửi email
    ├── user/             Quản trị tài khoản
    ├── assistant/        Trợ lý tư vấn
    └── recommendation/   Gợi ý không gian và gói tiệc
```

### Bộ kiểm thử theo mô hình Page Object Model

```
kiemthu/src/test/java/vn/vuonsen/kiemthu/
├── coso/         Hạ tầng dùng chung: cấu hình, mở trình duyệt, gọi API dựng dữ liệu
├── thanhphan/    Khối lặp lại nhiều trang: thanh điều hướng, bảng tạm tính
├── trang/        Một lớp cho một trang, giấu hết locator bên trong
├── luong/        Chuỗi thao tác dùng lại: đăng nhập, báo giá tiệc
└── kichban/      130 test case chia 15 luồng nghiệp vụ
```

Locator bắt theo thuộc tính `data-test` chứ không theo class CSS hay cấu trúc thẻ. Class CSS
đổi mỗi lần sửa giao diện, cấu trúc thẻ đổi mỗi lần thay bố cục — hai thứ đó làm kịch bản
trượt mà không phải vì website hỏng.

---

## 💰 Cách tính giá tiệc

Công thức nằm trong `PricingService`. Frontend gọi `POST /api/v1/bookings/quote` để lấy kết quả
chứ không tự tính lại, nhờ vậy số tiền hiển thị và số tiền lưu trong đơn luôn khớp nhau.

```
số mâm         = làm tròn lên (số khách / 10)
tiền ăn        = số mâm × giá một mâm của gói tiệc
mức tối thiểu  = phí thuê không gian × 10
phí không gian = 0 nếu tiền ăn đạt mức tối thiểu,
                 chưa đạt thì trả theo tỉ lệ còn thiếu
giảm giá       = 5% nếu đặt trước từ 60 ngày, hoặc mức giảm của ngày lễ
                 (hai ưu đãi không cộng dồn, lấy mức cao hơn)
VAT            = 8%
tổng cộng      = tiền ăn + phí không gian − giảm giá + VAT
tiền cọc       = 30% tổng cộng
```

Phí thuê giảm dần theo tiền ăn thay vì miễn phí đột ngột, để khách đặt đông hơn không bao giờ
trả ít tiền hơn khách đặt ít. Toàn bộ tham số nằm ở mục `app.booking` trong `application.yml`,
đổi chính sách giá không cần sửa mã nguồn.

---

## 🔌 API

Tài liệu đầy đủ và thử trực tiếp tại **<http://localhost:8080/swagger-ui.html>** khi backend
đang chạy. Bảng dưới đây chỉ liệt kê các đường dẫn hay dùng nhất.

| Phương thức | Đường dẫn | Quyền |
|---|---|---|
| `POST` | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh` | Công khai |
| `POST` | `/api/auth/forgot-password`, `/api/auth/reset-password` | Công khai |
| `GET` | `/api/v1/spaces`, `/api/v1/spaces/{slug}` | Công khai |
| `GET` | `/api/v1/menu/categories`, `/api/v1/menu/dishes` | Công khai |
| `GET` | `/api/v1/packages`, `/api/v1/holidays`, `/api/v1/gallery` | Công khai |
| `POST` | `/api/v1/bookings/quote` — báo giá, không tạo đơn | Công khai |
| `POST` | `/api/v1/bookings` | Công khai |
| `GET` | `/api/v1/bookings/track/{code}` | Công khai |
| `POST` | `/api/v1/dish-orders` | Công khai |
| `GET` | `/api/v1/dish-orders/track/{code}` | Công khai |
| `GET` | `/api/v1/payments/order/{maDon}` — tình hình thanh toán | Công khai |
| `POST` | `/api/v1/payments` — tạo yêu cầu, trả về mã VietQR | Công khai |
| `POST` | `/api/v1/payments/vnpay` | Công khai |
| `GET` | `/api/v1/bookings/my`, `/api/v1/dish-orders/my`, `/api/v1/me` | Cần đăng nhập |
| `GET` | `/api/v1/admin/bookings`, `/api/v1/admin/dish-orders` | ADMIN, STAFF |
| `GET` | `/api/v1/admin/payments`, `/api/v1/admin/statistics` | ADMIN, STAFF |
| `POST` | `/api/v1/admin/dishes`, `/api/v1/admin/spaces` | ADMIN |
| `POST` | `/api/v1/admin/users` | ADMIN |

Mọi phản hồi chứa nội dung do người nhập đều trả kèm bản tiếng Anh trong cùng một lần gọi
(`name` và `nameEn`), giao diện tự chọn theo ngôn ngữ đang dùng.

---

## 🗄 Cơ sở dữ liệu

Flyway quản lý toàn bộ script tạo bảng trong `backend/src/main/resources/db/migration`.
Hibernate chạy ở chế độ `validate`, nên entity lệch với bảng là báo lỗi ngay lúc khởi động
thay vì âm thầm sai dữ liệu.

> [!IMPORTANT]
> Migration đã chạy thì không sửa nữa, cần đổi gì thì thêm tệp mới. Flyway lưu mã kiểm tra
> của từng tệp, sửa tệp cũ sẽ làm hỏng cơ sở dữ liệu của người khác.

Các bảng chính: `users`, `spaces`, `dishes`, `party_packages`, `bookings`, `dish_orders`,
`payments`, `reviews`, `holiday_discounts`, `gallery_images`.

Bảng `bookings` và `dish_order_items` chép lại tên và giá tại thời điểm khách đặt thay vì chỉ
giữ khoá ngoại. Bảng giá thay đổi theo thời gian mà hoá đơn cũ phải giữ đúng con số đã chốt
với khách.

---

## 🌐 Giao diện song ngữ

Website hỗ trợ tiếng Việt và tiếng Anh, đổi bằng nút 🌐 ở góc phải thanh đầu trang.

- **Mặc định luôn là tiếng Việt.** Cố ý không đoán theo ngôn ngữ trình duyệt: nhiều máy của
  người Việt cài Windows bản tiếng Anh, đoán theo đó thì khách người Việt mở web lại thấy
  tiếng Anh — hỏng hơn là không đoán.
- Lựa chọn được nhớ lại nên khách nước ngoài chỉ cần đổi một lần.
- Tiền và ngày đổi định dạng theo ngôn ngữ: `15.000.000 ₫` ↔ `15,000,000 VND`.
- Thuộc tính `lang` của thẻ `html`, tiêu đề tab và thẻ mô tả cũng đổi theo.

Chuỗi giao diện nằm trong `frontend/src/i18n/vi.json` và `en.json`. Nội dung do cơ sở dữ liệu
quản lý có cột tiếng Anh riêng và tự lùi về bản tiếng Việt khi chưa dịch.

---

## 📄 Giấy phép và nguồn ảnh

Mã nguồn dùng cho mục đích học tập.

Ảnh minh hoạ lấy từ Pexels, Openverse và Wikimedia Commons, chỉ dùng ảnh mang giấy phép CC0,
phạm vi công cộng, CC BY hoặc CC BY-SA. Bảng ghi nguồn từng ảnh kèm tên tác giả nằm ở
`cong-cu/du-lieu-mau/mon-an/ghi-nguon-anh.md`.

---

<div align="center">

**Phạm Trần Tuấn Anh** · MSSV 21130004 · Lớp DH21DTA

Khoa Công nghệ thông tin — Trường Đại học Nông Lâm TP.HCM

Giảng viên hướng dẫn: TS. Nguyễn Thị Phương Trâm

</div>
