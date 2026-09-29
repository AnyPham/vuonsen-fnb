package vn.vuonsen.kiemthu.coso;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.util.List;

/*
 * Lớp cơ sở của mọi Page Object và Component Object.
 *
 * Giữ WebDriver và cơ chế chờ dùng chung, cung cấp các thao tác nguyên thủy đều đã kèm
 * chờ tường minh: nhập, bấm, đọc chữ. Mọi thao tác trên trang đều đi qua đây, nên cả bộ
 * kiểm thử không có chỗ nào phải tự gọi WebDriverWait, và không có chỗ nào dùng
 * Thread.sleep. Dừng cứng theo giây vừa chậm vừa không đáng tin: đặt ngắn thì trượt khi
 * máy chậm, đặt dài thì chờ phí mỗi lần chạy. Ngoại lệ duy nhất là chế độ trình diễn khi
 * quay video (-Ddemo=true), tắt mặc định và không dùng để phán xét đúng sai.
 *
 * Lớp này và các lớp con không chứa câu khẳng định nào. Chúng chỉ thao tác và đọc dữ liệu
 * trên màn hình; phán xét đúng sai là việc của kịch bản kiểm thử.
 */
public abstract class TrangCoSo {

    protected final WebDriver driver;
    protected final WebDriverWait cho;

    protected TrangCoSo(WebDriver driver) {
        this.driver = driver;
        this.cho = new WebDriverWait(driver, CauHinh.CHO_TOI_DA);
    }

    /**
     * Locator theo thuộc tính data-test, cách định vị ưu tiên số một của đề tài.
     * Thuộc tính này không phục vụ trình bày nên không bị đổi khi sửa giao diện.
     */
    protected static By dt(String ten) {
        return By.cssSelector("[data-test='" + ten + "']");
    }

    /** Chỉ lấy chữ số trong một chuỗi tiền. Ví dụ "90.000.000 ₫" thành 90000000. */
    public static long soTien(String chu) {
        String so = chu == null ? "" : chu.replaceAll("[^0-9]", "");
        return so.isEmpty() ? 0 : Long.parseLong(so);
    }

    protected void moDuongDan(String duongDan) {
        driver.get(CauHinh.BASE_URL + duongDan);
        dungTrinhDien(CauHinh.DEMO_DUNG_MS);
    }

    /** Dừng cho người xem kịp nhìn, chỉ ở chế độ trình diễn. Chạy kiểm thử bình thường thì trả về ngay. */
    public static void dungTrinhDien(long miliGiay) {
        if (!CauHinh.CHE_DO_DEMO) {
            return;
        }
        try {
            Thread.sleep(miliGiay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /*
     * Chế độ trình diễn: tô viền đỏ phần tử sắp thao tác, dừng một nhịp, rồi gỡ viền.
     *
     * Gỡ viền trước khi thao tác thật, để ảnh chụp khi kịch bản trượt không dính viền trình
     * diễn. Phần tử bị vẽ lại trong lúc dừng thì bỏ qua phần trình diễn, thao tác thật phía sau
     * vẫn tự tìm lại phần tử như thường.
     */
    private void trinhDien(WebElement phanTu) {
        if (!CauHinh.CHE_DO_DEMO) {
            return;
        }
        try {
            chayJs("const p = arguments[0];"
                    + "p.scrollIntoView({block: 'center', inline: 'nearest', behavior: 'instant'});"
                    + "p.style.outline = '3px solid #e11d48';"
                    + "p.style.outlineOffset = '2px';", phanTu);
            dungTrinhDien(CauHinh.DEMO_DUNG_MS);
            chayJs("arguments[0].style.outline = ''; arguments[0].style.outlineOffset = '';", phanTu);
        } catch (StaleElementReferenceException e) {
            // Phần tử đã bị vẽ lại, không còn gì để tô
        }
    }

    protected WebElement choHien(By locator) {
        return cho.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /*
     * Chế độ trình diễn: cuộn tới phần tử mà kịch bản vừa đọc để kiểm tra, tô viền xanh và dừng lâu
     * hơn một nhịp. Người xem video thấy đúng con số hay dòng chữ đang được so với kết quả mong đợi,
     * kể cả khi nó nằm ngoài khung nhìn lúc thao tác. Viền xanh để phân biệt với viền đỏ của thao
     * tác bấm, gõ. caKhoi là tô cả khối chứa phần tử, ví dụ cả bảng tạm tính hay cả dòng trong bảng.
     */
    protected void trinhDienKetQua(By locator, boolean caKhoi) {
        if (!CauHinh.CHE_DO_DEMO) {
            return;
        }
        try {
            trinhDienKetQua(driver.findElement(locator), caKhoi);
        } catch (NoSuchElementException e) {
            // Không còn phần tử để tô, bỏ qua phần trình diễn
        }
    }

    protected void trinhDienKetQua(WebElement phanTu, boolean caKhoi) {
        if (!CauHinh.CHE_DO_DEMO) {
            return;
        }
        try {
            WebElement khoi = (WebElement) chayJs("const p = arguments[1]"
                    + " ? (arguments[0].closest('aside, .card, tr') || arguments[0]) : arguments[0];"
                    + "p.scrollIntoView({block: 'center', inline: 'nearest', behavior: 'instant'});"
                    + "p.style.outline = '3px solid #2563eb';"
                    + "p.style.outlineOffset = '3px';"
                    + "return p;", phanTu, caKhoi);
            dungTrinhDien(CauHinh.DEMO_DUNG_MS * 2);
            chayJs("arguments[0].style.outline = ''; arguments[0].style.outlineOffset = '';", khoi);
        } catch (StaleElementReferenceException e) {
            // Phần tử đã bị vẽ lại, không còn gì để tô
        }
    }

    protected void bam(By locator) {
        bam(cho.until(ExpectedConditions.elementToBeClickable(locator)));
    }

    /*
     * Bấm một phần tử, cuộn nó ra giữa màn hình ngay lập tức trước khi bấm.
     *
     * Selenium có tự cuộn tới phần tử, nhưng tính tọa độ bấm ngay sau lệnh cuộn. Trang đặt cuộn
     * mượt thì lúc đó hiệu ứng cuộn còn đang chạy, tọa độ tính ra nằm ngoài màn hình và lệnh
     * bấm báo "element click intercepted". Phần tử sát đáy trang còn có thể bị thanh menu dính
     * hay nút nổi che mất. Cuộn ra giữa với behavior instant tránh được cả hai.
     *
     * Còn một trường hợp: khối nội dung tải xong sau lệnh cuộn, hiện ra phía trên và đẩy phần
     * tử đi chỗ khác, lệnh bấm rơi vào tọa độ cũ. Khi bị chặn thì cuộn lại rồi bấm lại, cho tới
     * khi bấm được hoặc hết thời gian chờ.
     */
    protected void bam(WebElement phanTu) {
        trinhDien(phanTu);
        new WebDriverWait(driver, CauHinh.CHO_TOI_DA)
                .ignoring(ElementClickInterceptedException.class)
                .until(d -> {
                    chayJs("arguments[0].scrollIntoView({block: 'center', inline: 'nearest', behavior: 'instant'});", phanTu);
                    if (!phanTu.isDisplayed() || !phanTu.isEnabled()) {
                        return false;
                    }
                    phanTu.click();
                    return true;
                });
    }

    /*
     * Nhập chữ vào ô, xóa giá trị cũ trước.
     *
     * Không dùng clear(). Với ô nhập do React điều khiển, clear() đổi giá trị trên màn hình
     * nhưng không phát sự kiện nhập liệu, nên state của React vẫn giữ giá trị cũ và form gửi
     * lên sai dữ liệu. Chọn hết rồi xóa bằng phím thì React nhận đủ sự kiện như người gõ thật.
     */
    protected void nhap(By locator, String giaTri) {
        WebElement o = choHien(locator);
        trinhDien(o);
        o.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if (!giaTri.isEmpty()) {
            o.sendKeys(giaTri);
        }
    }

    /*
     * Đặt giá trị cho ô ngày, ô ngày giờ bằng JavaScript theo cách React vẫn nhận được.
     *
     * Gõ phím vào ô type=date hay datetime-local phụ thuộc định dạng ngày theo ngôn ngữ của
     * máy: máy tiếng Việt gõ ngày trước, máy tiếng Anh gõ tháng trước, cùng một kịch bản chạy
     * hai máy ra hai kết quả. Gán thẳng thuộc tính value thì React không hay biết, vì React
     * theo dõi giá trị qua setter gốc của ô nhập. Nên phải gọi setter gốc rồi phát sự kiện
     * input, đúng như trình duyệt làm khi người dùng chọn ngày.
     */
    protected void datGiaTri(By locator, String giaTri) {
        WebElement o = choHien(locator);
        trinhDien(o);
        chayJs("const o = arguments[0];"
                + "Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set.call(o, arguments[1]);"
                + "o.dispatchEvent(new Event('input', { bubbles: true }));"
                + "o.dispatchEvent(new Event('change', { bubbles: true }));", o, giaTri);
    }

    /** Chọn một mục trong ô thả xuống theo giá trị. */
    protected void chon(By locator, String giaTri) {
        WebElement o = choHien(locator);
        /*
         * Chờ ô thả xuống có sẵn lựa chọn cần chọn.
         *
         * Loại sự kiện, buổi và danh mục đều do máy chủ trả về rồi mới đổ vào ô. Mở trang xong
         * chọn ngay thì gặp lúc ô còn rỗng, Selenium báo "Cannot locate option with value".
         */
        By luaChon = By.cssSelector("option[value='" + giaTri + "']");
        cho.ignoring(StaleElementReferenceException.class)
                .until(d -> !d.findElement(locator).findElements(luaChon).isEmpty());
        trinhDien(o);
        new Select(driver.findElement(locator)).selectByValue(giaTri);
    }

    protected String chuCua(By locator) {
        WebElement phanTu = choHien(locator);
        String chu = phanTu.getText().trim();
        trinhDienKetQua(phanTu, false);
        return chu;
    }

    /** Có phần tử khớp locator đang nằm trong trang ở thời điểm gọi hay không, không chờ. */
    protected boolean dangCo(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /** Chờ có ít nhất một phần tử hiện ra rồi trả về toàn bộ phần tử khớp locator. */
    protected List<WebElement> choDanhSach(By locator) {
        choHien(locator);
        return driver.findElements(locator);
    }

    /** Chữ của mọi phần tử khớp locator, sau khi chờ có ít nhất một phần tử hiện ra. */
    protected List<String> chuCuaTatCa(By locator) {
        return choDanhSach(locator).stream().map(p -> p.getText().trim()).toList();
    }

    /*
     * Chờ chữ của phần tử đổi khác giá trị cũ.
     *
     * Dùng cho bảng tạm tính: đổi số khách xong, giao diện đợi người dùng gõ xong rồi mới hỏi
     * lại máy chủ, nên con số cập nhật trễ vài trăm mili giây. Đọc ngay sau khi gõ thì đọc
     * trúng con số cũ, kịch bản lúc đạt lúc trượt.
     */
    protected String choChuDoi(By locator, String chuCu) {
        cho.until(d -> {
            try {
                String hienTai = d.findElement(locator).getText().trim();
                return !hienTai.isEmpty() && !hienTai.equals(chuCu);
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                return false;
            }
        });
        return chuCua(locator);
    }

    /*
     * Đọc chữ của một danh sách khi danh sách đã ổn định.
     *
     * Bộ lọc và tab danh mục gọi lại máy chủ rồi vẽ lại danh sách, có khi sau mỗi phím gõ.
     * Không thể chờ "danh sách đổi khác" vì có trường hợp kết quả mới trùng kết quả cũ, chờ
     * mãi không thấy đổi. Nên chờ tới khi không còn khối đang tải, danh sách hoặc khối rỗng
     * đã hiện, và hai lần đọc liên tiếp cách nhau một nhịp chờ cho cùng một kết quả.
     */
    protected List<String> docDanhSachOnDinh(By locator) {
        java.util.concurrent.atomic.AtomicReference<List<String>> lanTruoc = new java.util.concurrent.atomic.AtomicReference<>();
        return cho.until(d -> {
            try {
                if (!d.findElements(dt("loading")).isEmpty()) {
                    lanTruoc.set(null);
                    return null;
                }
                List<String> hienTai = d.findElements(locator).stream().map(p -> p.getText().trim()).toList();
                if (hienTai.isEmpty() && d.findElements(dt("empty")).isEmpty()) {
                    lanTruoc.set(null);
                    return null;
                }
                if (hienTai.equals(lanTruoc.get())) {
                    return hienTai;
                }
                lanTruoc.set(hienTai);
                return null;
            } catch (StaleElementReferenceException e) {
                lanTruoc.set(null);
                return null;
            }
        });
    }

    /** Bấm Đồng ý ở hộp xác nhận confirm() của trình duyệt. Hộp này không nằm trong trang. */
    protected void dongYHopThoai() {
        Alert hopThoai =cho.until(ExpectedConditions.alertIsPresent());
        dungTrinhDien(CauHinh.DEMO_DUNG_MS);
        hopThoai.accept();
    }

    protected Object chayJs(String maJs, Object... thamSo) {
        return ((JavascriptExecutor) driver).executeScript(maJs, thamSo);
    }

    /** Xóa sạch localStorage, như khi phiên đăng nhập hết hạn hoặc người dùng xóa dữ liệu trình duyệt. */
    public void xoaLocalStorage() {
        chayJs("window.localStorage.clear();");
    }

    /** Đường dẫn hiện tại, bỏ phần máy chủ. Ví dụ: /dang-nhap. */
    public String duongDanHienTai() {
        return URI.create(driver.getCurrentUrl()).getPath();
    }

    /**
     * Chờ trang chuyển tới đường dẫn cho trước. Trả về false nếu hết thời gian chờ mà vẫn
     * chưa tới, để kịch bản nhận được câu báo lỗi rõ ràng thay vì ngoại lệ hết giờ.
     */
    public boolean coChuyenToi(String duongDan) {
        try {
            cho.until(d -> duongDan.equals(URI.create(d.getCurrentUrl()).getPath()));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Tải lại trang như người dùng nhấn F5. */
    public void taiLaiTrang() {
        driver.navigate().refresh();
    }

    /**
     * Chờ màn hình "Không đủ quyền truy cập" hiện ra. Màn hình này dùng chung cho mọi trang
     * được bảo vệ, hiện khi đã đăng nhập nhưng vai trò không được phép vào trang đó.
     */
    public boolean dangHienKhongDuQuyen() {
        choHien(dt("access-denied"));
        return true;
    }

    /** Chữ của khối thông báo lỗi dùng chung, sau khi chờ nó hiện ra. */
    public String thongBaoLoiChung() {
        return chuCua(dt("error"));
    }

    /*
     * Chữ của khối thông báo lỗi nếu nó hiện ra trong thời gian chờ, chuỗi rỗng nếu không.
     *
     * Dùng khi đặc tả đòi có thông báo nhưng giao diện có thể không hiện gì. Không ném ngoại lệ
     * hết giờ, để kịch bản trượt bằng một câu khẳng định rõ ràng và được xếp đúng loại Trượt
     * trong báo cáo, thay vì bị xếp vào loại Lỗi như kịch bản không chạy hết.
     */
    public String thongBaoLoiNeuCo() {
        try {
            return thongBaoLoiChung();
        } catch (TimeoutException e) {
            return "";
        }
    }
}
