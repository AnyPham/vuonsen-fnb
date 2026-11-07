# Đo tải website Vườn Sen bằng Apache JMeter

Trả lời câu hỏi: *một người tìm kiếm thì nhanh, nhưng mười người cùng tìm thì bao nhiêu,
đo bằng cách nào, và bài toán tính toán đằng sau là gì.*

---

## 1. Chuẩn bị, làm một lần

**Cài JMeter.** Tải `apache-jmeter-5.6.3.zip` từ trang chính thức Apache rồi giải nén.
Cần Java 8 trở lên; máy đang có Java 17 nên chạy được ngay.

```
https://dlcdn.apache.org/jmeter/binaries/apache-jmeter-5.6.3.zip
```

Luôn đối chiếu mã băm với tệp `.sha512` Apache công bố trước khi giải nén. Tệp tải về đã
được kiểm, mã băm khớp bản gốc.

**Bật MySQL và backend**, giống như khi chạy website bình thường.

**Tạo tài khoản đo tải.** Mỗi khách ảo cần một tài khoản riêng:

```bash
node cong-cu/do-hieu-nang/jmeter/chuan-bi-tai-khoan.js 100
```

Script ghi ra `taikhoan.csv`. Chạy lại nhiều lần không tạo thêm tài khoản rác: nếu tệp cũ
còn dùng được thì giữ nguyên.

---

## 2. Chạy đo

```bash
node cong-cu/do-hieu-nang/jmeter/chay-do-tai.js
```

Mặc định đo lần lượt ở **1, 10, 25, 50, 100 người đồng thời**, mỗi mức 60 giây. Đo một vài
mức thôi thì truyền danh sách:

```bash
node cong-cu/do-hieu-nang/jmeter/chay-do-tai.js 1,10
```

Đo **luồng ghi** (đặt tiệc thật và thanh toán), mặc định 1, 10, 25 người:

```bash
node cong-cu/do-hieu-nang/jmeter/chay-do-tai.js --ghi
```

Luồng ghi sinh đơn thật mang tên khách `Do tai ghi don`. Bộ chạy tự hủy hết các đơn này
trước khi đo và sau khi đo xong, nên không đọng lại đơn chờ xác nhận hay buổi bị giữ chỗ.

Hai tùy chọn khác:

| Tùy chọn | Tác dụng |
|---|---|
| `--doc-lai` | Đọc lại các tệp `.jtl` đã có rồi in bảng, không đo lại. Dùng khi bước hủy đơn lỗi hay chỉ muốn xem lại số |
| Biến `API_URL` | Đo vào máy chủ khác, ví dụ `API_URL=http://localhost:8099`; mặc định `http://localhost:8080` |

Kết quả luồng ghi đo trên mã cũ, trước khi sửa lỗi trùng mã, được giữ riêng ở
`ket-qua/truoc-khi-sua/` để so trước và sau.

Kết quả in ra màn hình, đồng thời lưu ở `ket-qua/`:

| Tệp | Nội dung |
|---|---|
| `ketqua-<n>.jtl` | từng yêu cầu một, dạng CSV |
| `bao-cao-<n>/index.html` | báo cáo đồ thị của chính JMeter |
| `tong-hop.json` | số liệu đã gộp, dùng để dựng bảng |

Luồng ghi dùng cùng các tên đó nhưng thêm tiền tố `ghi-`, và tệp gộp là `tong-hop-ghi.json`.

---

## 3. Kịch bản đo cái gì

Sáu bước, đúng chuỗi việc của một khách đi đặt tiệc:

| Bước | Đường dẫn | Việc của khách |
|---|---|---|
| 1 | `POST /api/auth/login` | Đăng nhập |
| 2 | `GET /api/v1/spaces?guests=200` | Tìm sảnh cho 200 khách |
| 3 | `GET /api/v1/spaces/sanh-ven-song` | Mở xem chi tiết một sảnh |
| 4 | `POST /api/v1/bookings/quote` | Xem giá tiệc |
| 5 | `GET /api/v1/menu/dishes` | Xem thực đơn |
| 6 | `POST /api/v1/dish-orders/quote` | Tính tiền giỏ món |

**Vì sao kịch bản này không tạo đơn thật.** Chạy một lượt ở mức 100 người sinh ra vài
nghìn đơn, đụng ràng buộc trùng lịch và sinh lỗi nghiệp vụ. Lỗi đó lẫn vào tỉ lệ lỗi khiến
không còn phân biệt được lỗi do quá tải với lỗi do ràng buộc. Phần ghi được đo riêng bằng
kịch bản thứ hai dưới đây.

### Kịch bản ghi: `vuonsen-ghi-don.jmx`

| Bước | Đường dẫn | Việc của khách |
|---|---|---|
| 1 | `POST /api/auth/login` | Đăng nhập, mỗi khách ảo một lần |
| 2 | `POST /api/v1/bookings` | Đặt tiệc thật |
| 3 | `POST /api/v1/payments` | Tạo yêu cầu thanh toán cọc, máy chủ dựng mã VietQR |
| 4 | `GET /api/v1/payments/order/{mã đơn}` | Mở trang tình hình thanh toán |

**Mỗi lượt đặt một ngày riêng.** Đơn mới được giữ chỗ tạm 15 phút, nên hai lượt cùng ngày
cùng buổi thì lượt sau bị từ chối vì trùng lịch. Đó là luật nghiệp vụ chạy đúng chứ không
phải quá tải, để nó lẫn vào thì tỉ lệ lỗi mất ý nghĩa. Mỗi khách ảo được một dải 50 ngày
riêng, mỗi mức tải một dải 6.000 ngày riêng.

**Tách khỏi kịch bản đọc** vì hai loại tải khác bản chất. Ghi phải kiểm trùng lịch, sinh
mã đơn, ghi lịch sử trạng thái và ghi thư xác nhận, nên chậm hơn và nghẽn sớm hơn. Gộp
chung thì số đo của bước ghi bị các bước đọc nhanh pha loãng mất.

**Không đo bước thanh toán trên cổng VNPay.** Bước đó diễn ra trên máy chủ của VNPay, ngoài
hệ thống. Phần của Vườn Sen là tạo yêu cầu thanh toán và kiểm chữ ký kết quả trả về.

---

## 4. Ba quyết định thiết kế phải giải thích được

### Có thời gian nghĩ giữa các bước

Kịch bản chờ ngẫu nhiên **1 đến 3 giây** sau mỗi bước. Khách thật đọc trang rồi mới bấm tiếp.

Đây là điểm quyết định con số đo được có trả lời đúng câu hỏi hay không. Bỏ nhịp nghĩ đi thì
mỗi luồng gửi yêu cầu liên tục, và mười luồng sinh ra tải tương đương hàng trăm khách thật.
Lúc đó con số thu được không còn là "mười người cùng dùng thì bao nhiêu" nữa.

### Tăng dần số người, không thả cùng lúc

Thả cả trăm luồng trong một khoảnh khắc tạo ra một đỉnh giả ở giây đầu, không giống cách
người dùng thật vào trang. Kịch bản tăng dần trong khoảng bằng một phần tư thời gian đo.

### Mỗi khách ảo một tài khoản riêng

Dùng chung một tài khoản làm sai số đo theo hai hướng ngược nhau: dòng người dùng đó nằm sẵn
trong bộ đệm cơ sở dữ liệu nên nhanh hơn thực tế, đồng thời các luồng tranh khoá khi cùng ghi
thời điểm đăng nhập cuối nên lại chậm hơn thực tế. Người dùng thật mỗi người một tài khoản.

---

## 5. Bài toán tính toán

### Định luật Little

> Little, J. D. C. (1961). *"A Proof for the Queuing Formula: L = λW"*.
> Operations Research, 9(3), tr. 383–387.

$$L = \lambda \times W$$

| Ký hiệu | Nghĩa |
|---|---|
| $L$ | số yêu cầu đang nằm trong hệ thống |
| $\lambda$ | thông lượng, số yêu cầu hoàn thành mỗi giây |
| $W$ | thời gian một yêu cầu ở trong hệ thống |

Định luật này đúng với mọi hàng đợi ổn định, **không cần giả định gì về phân bố** thời gian
đến hay thời gian phục vụ. Đó là lý do nó dùng được ở đây mà không phải chứng minh thêm.

**Dùng để làm gì.** Hai việc.

Thứ nhất là **kiểm tra phép đo có tự mâu thuẫn không**. Lấy thông lượng nhân thời gian phản
hồi trung bình rồi so với số người. Lệch nhiều nghĩa là số liệu không đáng tin, thường do máy
đo bị nghẽn chứ không phải máy chủ chậm.

Thứ hai là **giải thích vì sao chậm, chứ không chỉ nói là chậm**. Khi thông lượng đã chạm
trần, $\lambda$ không tăng được nữa, nên theo công thức $W = L / \lambda$ thời gian chờ tăng
gần tuyến tính theo số người. Đó chính là định nghĩa của bão hoà.

### Vì sao đọc p95 chứ không đọc trung bình

Trung bình che mất đuôi chậm. Một trăm lượt nhanh và một lượt mất mười giây vẫn cho trung
bình đẹp, nhưng người gặp lượt mười giây đó mới là người rời trang.

| Chỉ số | Nghĩa |
|---|---|
| p95 | 95% số yêu cầu nhanh hơn mức này |
| p99 | 99% số yêu cầu nhanh hơn mức này |

Ngưỡng chấp nhận chọn là **p95 dưới 1 giây**, lấy theo ba mốc thời gian phản hồi của Nielsen:

> Nielsen, J. (1993). *Usability Engineering*. Academic Press, mục Response Times.
> Ba mốc này Nielsen kế thừa từ Miller, R. B. (1968), *"Response time in man-computer
> conversational transactions"*, AFIPS Fall Joint Computer Conference.

| Thời gian | Người dùng cảm nhận |
|---|---|
| 0,1 giây | Thấy như hệ thống phản ứng tức thì |
| 1 giây | Thấy có chờ, nhưng mạch suy nghĩ không bị đứt |
| 10 giây | Giới hạn giữ được sự chú ý; lâu hơn là bỏ sang việc khác |

Chọn mốc 1 giây vì đặt tiệc là chuỗi nhiều bước liên tiếp: mỗi bước dưới 1 giây thì khách đi
một mạch không bị ngắt quãng.

### Điểm bão hoà

Tìm bằng cách tăng dần số người và nhìn thông lượng. Khi thông lượng thôi tăng mà thời gian
phản hồi vẫn tăng, đó là điểm bão hoà. Trước điểm đó, thêm người thì phục vụ được thêm; sau
điểm đó, thêm người chỉ làm hàng đợi dài ra.

---

## 6. Giới hạn của phép đo, phải nói rõ khi báo cáo

**Máy đo và máy chủ chung một máy tính.** JMeter, backend và MySQL cùng tranh CPU và RAM của
một chiếc laptop. Số đo được vì vậy xấu hơn một máy chủ thật, và chỉ dùng để so sánh giữa các
lần đo trên cùng máy, không phải cam kết năng lực.

**Website chưa công khai.** Phép đo chạy trong mạng nội bộ, không có độ trễ đường truyền, không
có CDN, không có cân bằng tải. Kết quả là giới hạn trên của tốc độ, thực tế khi public sẽ chậm
hơn.

**Luồng ghi đo ở ít mức tải hơn.** Đặt tiệc và thanh toán đo tới 25 người đồng thời, vì mỗi
lượt sinh một đơn thật. Đặt món lẻ mới đo tới bước tính tiền, chưa đo bước gửi đơn món.

**Backend chạy bằng `mvn spring-boot:run`.** Chế độ này tắt một phần tối ưu của máy ảo Java.
Bản đóng gói chạy thật sẽ nhanh hơn.
