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
| Gói tiệc | 8 |
| Module nghiệp vụ (backend) | 15 |
| Trang giao diện | 32 |
| Migration cơ sở dữ liệu | 29 |
| Kiểm thử đơn vị và tích hợp | 225 bài |
| Kiểm thử giao diện (Selenium) | 170 test case / 19 luồng |

---

## 🚀 Bắt đầu nhanh

Website **chỉ chạy được bằng MySQL trên máy bạn**. Không có chế độ chạy tạm trên bộ nhớ,
nên mọi thứ bạn đặt, sửa hay xoá đều nằm trong cơ sở dữ liệu thật và **còn nguyên sau khi
tắt máy chủ, tắt máy tính**.

**Cần có sẵn:** JDK 17+, Node.js 18+, Maven 3.9+, và MySQL 8 hoặc MariaDB 10.4+ (bản MySQL
đi kèm XAMPP là đủ).

Website gồm ba phần phải chạy **đúng thứ tự**, vì phần sau cần phần trước:

```
MySQL  ──►  Backend  ──►  Frontend  ──►  Bạn mở trình duyệt
cổng 3306   cổng 8080     cổng 5173
nơi chứa    xử lý nghiệp   trang web
dữ liệu     vụ, đọc ghi    bạn nhìn thấy
            vào MySQL
```

Bỏ qua một bước là đứt dây chuyền: thiếu MySQL thì backend không khởi động, thiếu backend
thì trang web mở ra nhưng trống trơn và báo lỗi gọi API.

---

### Bước 0 — Tải mã nguồn về (chỉ làm một lần)

```bash
git clone https://github.com/AnyPham/vuonsen-fnb.git
cd vuonsen-fnb
```

### Bước 1 — Bật MySQL

Mở **XAMPP Control Panel**, bấm **Start** ở dòng **MySQL**. Chờ tới khi dòng đó chuyển
sang nền xanh.

Không cần tạo cơ sở dữ liệu, không cần chạy tệp `.sql` nào. Lần chạy đầu backend tự tạo
cơ sở dữ liệu `vuonsen_fnb`, dựng đủ bảng và nạp dữ liệu mẫu.

Muốn chắc chắn MySQL đã lên thì chạy:

```bash
netstat -ano | findstr :3306
```

Có dòng `LISTENING` là được.

> [!IMPORTANT]
> Chưa bật MySQL thì backend **không khởi động được**. Đó là chủ ý, không phải lỗi: thà
> báo ngay còn hơn chạy được rồi hôm sau mở ra mất sạch dữ liệu.

### Bước 2 — Chạy backend

Mở **cửa sổ dòng lệnh thứ nhất**:

```bash
cd backend
mvn spring-boot:run
```

Không cần truyền thêm cờ nào. Lần đầu mất khoảng một phút vì phải chạy 29 bản di trú và
nạp dữ liệu mẫu; những lần sau nhanh hơn nhiều.

Backend sẵn sàng khi thấy dòng:

```
Started FnbApplication in 12.345 seconds
```

**Để nguyên cửa sổ này, đừng đóng.** Đóng là tắt backend.

### Bước 3 — Chạy frontend

Mở **cửa sổ dòng lệnh thứ hai**, giữ nguyên cửa sổ thứ nhất:

```bash
cd frontend
npm install
npm run dev
```

Lệnh `npm install` chỉ cần chạy lần đầu, hoặc khi ai đó thêm thư viện mới.

Frontend sẵn sàng khi thấy:

```
VITE v5.4.21  ready in 4031 ms
➜  Local:   http://localhost:5173/
```

### Bước 4 — Mở trình duyệt và dùng

Vào **<http://localhost:5173>**. Xong.

| Địa chỉ | Dùng để |
|---|---|
| <http://localhost:5173> | Website |
| <http://localhost:8080/swagger-ui.html> | Tài liệu API, thử API trực tiếp |
| <http://localhost/phpmyadmin> | Xem cơ sở dữ liệu `vuonsen_fnb` (nếu dùng XAMPP) |

**Tài khoản quản trị mặc định:** `admin@vuonsen.vn` / `Admin@123`

> [!WARNING]
> Mật khẩu này chỉ dành cho môi trường phát triển. Trước khi đưa website ra Internet,
> đặt biến môi trường `ADMIN_PASSWORD` thành mật khẩu khác.

### Khi muốn dừng

Bấm `Ctrl+C` trong từng cửa sổ dòng lệnh. MySQL thì bấm **Stop** trong XAMPP, hoặc cứ để
chạy cũng không sao.

Dữ liệu nằm trong MySQL nên tắt hết rồi bật lại vẫn còn nguyên. Lần sau chỉ cần làm lại
**bước 1 → 2 → 3**, không phải `git clone` hay `npm install` nữa.

### Nếu tài khoản MySQL có mật khẩu

Cấu hình mặc định dùng user `root` với mật khẩu để trống, đúng như XAMPP vừa cài xong.
Máy nào đã đặt mật khẩu cho `root` thì khai báo khi chạy backend ở bước 2:

```bash
DB_PASSWORD=matkhaucuaban mvn spring-boot:run
```

### Cấu hình — clone về là chạy, không phải khai báo gì

Đây là điều hay bị hỏi nhất, nên nói rõ: **bạn không cần tạo tệp cấu hình nào.** Không
`.env`, không khóa API, không chỉnh `application.yml`. Tải mã nguồn về, bật MySQL, chạy
hai lệnh ở bước 2 và 3 là xong.

Toàn bộ giá trị mặc định đã nhắm sẵn vào một máy XAMPP vừa cài: MySQL ở `localhost:3306`,
user `root`, mật khẩu để trống.

Các tệp dưới đây **đều không bắt buộc**, chỉ cần khi bạn muốn bật thêm thứ gì:

| Tệp | Khi nào cần | Thiếu thì sao |
|---|---|---|
| `backend/tro-ly-ai.properties` | Muốn trợ lý tư vấn trả lời bằng AI thật | Trợ lý vẫn chạy, trả lời dựa trên dữ liệu trong cơ sở dữ liệu |
| `frontend/.env` | Muốn bật bản đồ Google hoặc đổi địa chỉ API | Bản đồ vẫn hiện bằng đường nhúng, API gọi qua proxy của Vite |
| `.env` ở thư mục gốc | **Chỉ dùng cho Docker** | Chạy bằng Maven thì không đụng tới tệp này |

Mỗi tệp đều có bản mẫu đi kèm (`tro-ly-ai.properties.example`, `.env.example`), chép ra
rồi điền khi nào cần. Cả ba tệp thật đều nằm trong `.gitignore` nên không bao giờ bị đẩy
lên kho mã nguồn.

> [!NOTE]
> Đã kiểm chứng bằng cách dựng một bản chỉ gồm các tệp có trong Git — không `.env`, không
> `tro-ly-ai.properties` — rồi chạy trên cơ sở dữ liệu trắng. Backend khởi động trong 11
> giây, tự tạo tài khoản quản trị và nạp đủ dữ liệu mẫu:
>
> ```
> Started FnbApplication in 11.727 seconds
> Đã tạo tài khoản quản trị mặc định: admin@vuonsen.vn
> Trợ lý tư vấn: AI đang tắt trong cấu hình, trả lời bằng cơ chế dự phòng
> ```

### Làm sao biết chắc đang chạy MySQL chứ không phải bộ nhớ tạm

Ba cách, chọn cách nào cũng được.

**Cách 1 — đọc dòng log lúc khởi động.** Backend in ra hồ sơ đang dùng:

```
The following 1 profile is active: "local"
```

Phải là `local` hoặc `prod`. Cả hai đều nối vào MySQL.

**Cách 2 — đếm trong cơ sở dữ liệu.** Mở phpMyAdmin xem bảng `vuonsen_fnb`, hoặc chạy:

```bash
mysql -u root -e "SELECT COUNT(*) FROM vuonsen_fnb.dishes;"
```

Ra 126 là dữ liệu đã nằm trong MySQL thật.

**Cách 3 — thử tắt bật.** Đặt một đơn trên web, tắt backend bằng `Ctrl+C`, chạy lại, rồi
vào **Đơn của tôi**. Đơn còn đó nghĩa là dữ liệu đã lưu xuống đĩa.

> [!NOTE]
> **Dự án không còn chế độ chạy trên bộ nhớ tạm.** Trước đây có hồ sơ `dev` dùng H2 giữ
> dữ liệu trên RAM, tắt backend là mất sạch, và đã gây hiểu nhầm: có người đặt đơn xong mở
> MySQL ra tìm lại không thấy. Nay trình điều khiển H2 chỉ còn trong phạm vi kiểm thử
> (`<scope>test</scope>` ở `backend/pom.xml`), nên máy chủ **không thể** khởi động bằng
> H2 nữa. Ai lỡ gõ `-Dspring-boot.run.profiles=dev` sẽ nhận lỗi dừng hẳn:
>
> ```
> Cannot load driver class: org.h2.Driver
> ```
>
> Đó là hàng rào cố ý, không phải hỏng hóc. Cứ bỏ cờ đó đi và chạy `mvn spring-boot:run`.

---

## 🛠 Cài đặt đầy đủ

### Cách 1 — MySQL cài trên máy (mặc định)

Trình tự chạy nằm ở phần [Bắt đầu nhanh](#-bắt-đầu-nhanh) bên trên. Mục này chỉ nói thêm
mấy chi tiết mà phần đó lược bớt cho gọn.

**Cơ sở dữ liệu tự sinh.** Chuỗi kết nối có `createDatabaseIfNotExist=true` nên MySQL tự
tạo `vuonsen_fnb` ở lần chạy đầu. Ai muốn tự tạo trước để chắc chắn bảng mã thì chạy:

```sql
CREATE DATABASE vuonsen_fnb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Dữ liệu mẫu Flyway nạp sẵn.** Ngay lần chạy đầu đã có: 6 không gian, 8 gói tiệc, 126
món, 52 đơn đặt tiệc, 35 đơn đặt món, 19 đánh giá, 1 tài khoản quản trị và 5 khách hàng.

**Đổi tài khoản kết nối.** Khai biến môi trường, đừng sửa tệp cấu hình — sửa tệp là lần
sau kéo mã mới về sẽ xung đột:

```bash
DB_USER=root DB_PASSWORD=matkhaucuaban mvn spring-boot:run
```

**Kiểm tra dữ liệu đã vào MySQL chưa.** Mở phpMyAdmin hoặc chạy:

```bash
mysql -u root -e "SELECT COUNT(*) AS so_mon FROM vuonsen_fnb.dishes;"
```

Ra 126 là Flyway đã nạp xong.

**Bộ dữ liệu lớn — không bắt buộc.** Nạp bộ dữ liệu 24 tháng để xem hệ thống ở mức tải gần thực tế:
khoảng 1.500 khách hàng, 1.600 đơn đặt tiệc, 1.500 đơn đặt món và 460 đánh giá.

```bash
mysql -u root vuonsen_fnb < cong-cu/du-lieu-mau/du-lieu-lon.sql
```

Bộ này còn thêm bốn tài khoản vai trò `STAFF` (`bao.tran@vuonsen.vn`,
`hoang.long@vuonsen.vn`, `kim.loan@vuonsen.vn`, `quoc.khanh@vuonsen.vn`). Dữ liệu Flyway
nạp sẵn chỉ có một tài khoản quản trị và năm khách hàng, nên ai cần đăng nhập bằng vai
trò nhân viên thì nạp thêm bước này. Toàn bộ 170 test case Selenium không cần tới nó.

### Khi MySQL không chịu nối

Bốn lỗi hay gặp nhất, kèm cách nhận ra và cách chữa.

**`Communications link failure` hoặc `Connection refused`** — MySQL chưa chạy. Mở XAMPP
Control Panel bấm Start ở dòng MySQL. Kiểm tra lại bằng:

```bash
netstat -ano | findstr :3306
```

Có dòng `LISTENING` là MySQL đã sẵn sàng.

**Đã bật MySQL mà vẫn báo không nối được** — nhiều máy cài sẵn MySQL Server riêng bên cạnh
XAMPP, và bản đó có thể nghe **cổng khác 3306**. Xem cổng thật bằng:

```bash
netstat -ano | findstr "330"
```

Nếu MySQL của bạn nằm ở cổng khác, trỏ lại bằng biến môi trường thay vì sửa tệp cấu hình:

```bash
DB_URL="jdbc:mysql://localhost:3305/vuonsen_fnb?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false" mvn spring-boot:run
```

**`Access denied for user 'root'@'localhost'`** — tài khoản `root` trên máy bạn có mật khẩu.
Khai báo khi chạy:

```bash
DB_PASSWORD=matkhaucuaban mvn spring-boot:run
```

**`Cannot load driver class: org.h2.Driver`** — bạn đang truyền cờ `-Dspring-boot.run.profiles=dev`.
Hồ sơ đó chỉ dành cho `mvn test`, website không chạy bằng nó. Bỏ cờ đi, gõ `mvn spring-boot:run`.

**Đặt đơn trên web xong mở MySQL không thấy** — kiểm tra xem bạn có đang mở đúng cơ sở dữ
liệu `vuonsen_fnb` trên đúng máy chủ MySQL mà backend đang nối tới không. Dòng log lúc
khởi động cho biết hồ sơ đang dùng; chạy thêm `SELECT COUNT(*) FROM vuonsen_fnb.dish_orders;`
trước và sau khi đặt đơn, con số phải tăng.

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
| `local` *(mặc định)* | MySQL `localhost:3306` | Chạy website hằng ngày. Không phải truyền cờ gì |
| `prod` | MySQL qua biến môi trường | Chạy thật, dùng cho Docker |
| `dev` | H2 trên RAM | **Chỉ dành cho `mvn test`.** Không khởi động được website |

Hai hồ sơ chạy website đều nối vào MySQL. Không có lựa chọn nào khác, nên không có đường
nào vô tình chạy vào bộ nhớ tạm.

**Vì sao bài kiểm thử lại dùng H2?** Vì ba lý do, và không lý do nào ảnh hưởng tới dữ liệu
thật của bạn:

Thứ nhất, `mvn test` phải chạy được trên máy chưa cài MySQL, chẳng hạn máy của bạn cùng
nhóm hoặc máy chủ tích hợp liên tục. Thứ hai, mỗi lần chạy cần một cơ sở dữ liệu trắng để
kết quả không phụ thuộc dữ liệu còn sót từ lần trước. Thứ ba, và quan trọng nhất, bài kiểm
thử tạo rồi xoá rất nhiều bản ghi — cho chúng đụng vào MySQL thật là hỏng dữ liệu đang dùng.

`maven-surefire-plugin` trong `backend/pom.xml` ép `spring.profiles.active=dev` cho riêng
lúc chạy kiểm thử, nên dù hồ sơ mặc định là `local` thì `mvn test` vẫn nằm gọn trên H2.
Có thể tự kiểm chứng: đếm số dòng trong MySQL, chạy `mvn test`, đếm lại — con số không đổi.

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
| Trợ lý tư vấn AI | `ASSISTANT_LLM_PROVIDER`, `ASSISTANT_LLM_API_KEY` | Dựng câu trả lời từ dữ liệu thật trong cơ sở dữ liệu — xem [Trợ lý tư vấn](#-trợ-lý-tư-vấn) |
| Bản đồ Google | `VITE_GOOGLE_MAPS_API_KEY` | Hiện bản đồ bằng đường nhúng không cần khoá |

---

## 🧪 Kiểm thử

### Kiểm thử phía máy chủ — 225 bài

Chạy trên H2 nên **không cần MySQL và không phải khởi động gì trước**. Dữ liệu trong MySQL
không bị chạm tới.

```bash
cd backend
mvn test
```

### Kiểm thử giao diện bằng Selenium — 170 test case

Bộ kiểm thử điều khiển trình duyệt thật, nên **backend và frontend phải đang chạy**
trước khi gọi lệnh. Cần cài sẵn Google Chrome.

```bash
# Cửa sổ 1
cd backend && mvn spring-boot:run

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
<summary><b>19 luồng nghiệp vụ được kiểm thử</b></summary>

<br>

| Luồng | Nội dung | Test case |
|---|---|---|
| 1 | Đăng ký và đăng nhập | 10 |
| 2 | Xem và lọc không gian | 8 |
| 3 | Trang chi tiết không gian | 6 |
| 4 | Thực đơn và chi tiết món | 12 |
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
| 16 | Xem và lọc gói tiệc | 10 |
| 17 | Lịch đặt chỗ quản trị | 10 |
| 18 | Thông báo khuyến mãi trang chủ | 8 |
| 19 | Đặt sảnh nhỏ, thực đơn tự chọn | 8 |

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
└── kichban/      170 test case chia 19 luồng nghiệp vụ
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

## 🤖 Trợ lý tư vấn

Hộp thoại trợ lý trả lời câu hỏi của khách về không gian, thực đơn, gói tiệc và cách đặt.
Điểm cốt lõi: **trợ lý chỉ được nói những gì có trong cơ sở dữ liệu**. Mỗi lần khách hỏi, hệ
thống đọc thẳng từ cơ sở dữ liệu ra 126 món kèm giá, 6 không gian, 8 gói tiệc, luật tính giá
và các dịp lễ đang giảm giá, chép vào chỉ dẫn gửi kèm, rồi ra lệnh không được nói ra ngoài
phạm vi đó. Sửa giá trong trang quản trị là trợ lý biết ngay, không phải khởi động lại.

Chọn nguồn trả lời bằng `ASSISTANT_LLM_PROVIDER`:

| Nguồn | Chi phí | Cần gì | Ghi chú |
|---|---|---|---|
| `gemini` *(mặc định)* | Miễn phí trong hạn mức | Khóa ở [AI Studio](https://aistudio.google.com/apikey), không cần thẻ | Tiếng Việt tốt, đủ dùng cho quy mô một nhà hàng |
| `ollama` | Miễn phí hoàn toàn | [Ollama](https://ollama.com) cài trên máy | Chạy được cả khi mất mạng, dữ liệu không rời khỏi máy; chất lượng tùy cấu hình máy |
| `anthropic` | Trả theo lượng dùng | Tín dụng ở [Console](https://console.anthropic.com) | Chất lượng cao nhất |

**Không khai báo gì cũng chạy.** Thiếu khóa thì trợ lý tự lùi về cơ chế dự phòng dựng sẵn
trong hệ thống: nhận diện ý định bằng từ khóa rồi ghép câu từ dữ liệu thật. Câu chữ cứng
nhắc hơn nhưng không bao giờ sai số liệu, và không cần mạng. Nhật ký khởi động luôn nói rõ
đang dùng nguồn nào.

Dùng Ollama thì cài xong chạy thêm một lệnh để tải mô hình về:

```bash
ollama pull qwen2.5:3b
```

Hướng dẫn đầy đủ cho cả ba nguồn nằm trong `backend/tro-ly-ai.properties.example`.

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
