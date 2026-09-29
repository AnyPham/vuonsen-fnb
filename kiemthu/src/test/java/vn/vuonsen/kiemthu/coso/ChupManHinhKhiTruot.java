package vn.vuonsen.kiemthu.coso;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Chụp màn hình khi một kịch bản trượt, lưu vào target/anh-loi.
 *
 * Dùng AfterTestExecutionCallback chứ không dùng TestWatcher. TestWatcher chạy sau
 * @AfterEach, lúc đó trình duyệt đã đóng nên không còn gì để chụp. Callback này chạy ngay
 * khi thân kịch bản vừa kết thúc, trước @AfterEach, nên màn hình vẫn còn nguyên đúng lúc
 * xảy ra lỗi.
 */
public class ChupManHinhKhiTruot implements AfterTestExecutionCallback {

    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        if (!(context.getRequiredTestInstance() instanceof KiemThuCoSo kiemThu) || kiemThu.driver() == null) {
            return;
        }
        WebDriver driver = kiemThu.driver();

        // Chụp hỏng thì chỉ in cảnh báo, không được che mất lỗi thật của kịch bản
        try {
            Files.createDirectories(CauHinh.THU_MUC_ANH_LOI);
            String ten = context.getRequiredTestClass().getSimpleName()
                    + "_" + context.getRequiredTestMethod().getName()
                    + "_" + LocalDateTime.now().format(GIO) + ".png";
            Path tep = CauHinh.THU_MUC_ANH_LOI.resolve(ten);
            Files.write(tep, ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            System.out.println("[anh-loi] " + tep.toAbsolutePath());
        } catch (Exception loi) {
            System.out.println("[anh-loi] Không chụp được màn hình: " + loi.getMessage());
        }
    }
}
