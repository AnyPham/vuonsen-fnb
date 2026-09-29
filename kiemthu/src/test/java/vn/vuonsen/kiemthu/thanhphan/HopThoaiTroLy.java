package vn.vuonsen.kiemthu.thanhphan;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/*
 * Hộp thoại trợ lý tư vấn nổi ở góc dưới mọi trang.
 *
 * Là Component Object vì nó có mặt trên mọi trang chứ không thuộc riêng trang nào. Kịch bản
 * mở nó từ trang nào cũng dùng chung lớp này.
 */
public class HopThoaiTroLy extends TrangCoSo {

    private static final By NUT_MO = dt("assistant-open");
    private static final By HOP_THOAI = dt("assistant");
    private static final By NUT_DONG = dt("assistant-close");
    private static final By O_NHAP = dt("assistant-input");
    private static final By NUT_GUI = dt("assistant-send");
    private static final By CAU_GOI_Y = dt("assistant-suggestion");

    /*
     * Bong bóng nội dung của tin trợ lý. Bong bóng "Đang tìm câu trả lời" lúc chờ không mang
     * data-test nên không bị đếm nhầm thành một câu trả lời.
     */
    private static final By BONG_TRO_LY = By.cssSelector("[data-test='assistant-msg-bot'] .bong");
    private static final By BONG_KHACH = By.cssSelector("[data-test='assistant-msg-khach'] .bong");

    public HopThoaiTroLy(WebDriver driver) {
        super(driver);
    }

    public HopThoaiTroLy mo() {
        bam(NUT_MO);
        choHien(HOP_THOAI);
        return this;
    }

    public HopThoaiTroLy dong() {
        bam(NUT_DONG);
        cho.until(ExpectedConditions.invisibilityOfElementLocated(HOP_THOAI));
        return this;
    }

    public boolean dangMo() {
        return dangCo(HOP_THOAI);
    }

    public boolean nutMoDangHien() {
        return choHien(NUT_MO).isDisplayed();
    }

    /** Lời chào là bong bóng trợ lý đầu tiên. */
    public String loiChao() {
        return chuCuaTatCa(BONG_TRO_LY).get(0);
    }

    public List<String> cauGoiY() {
        return chuCuaTatCa(CAU_GOI_Y);
    }

    public List<String> cauHoiDaGui() {
        return driver.findElements(BONG_KHACH).stream().map(p -> p.getText().trim()).toList();
    }

    public boolean nutGuiDangKhoa() {
        return !choHien(NUT_GUI).isEnabled();
    }

    /** Gõ câu hỏi, gửi, rồi chờ có thêm một câu trả lời mới và trả về nội dung câu đó. */
    public String hoi(String cauHoi) {
        int soTraLoiTruoc = driver.findElements(BONG_TRO_LY).size();
        nhap(O_NHAP, cauHoi);
        bam(NUT_GUI);
        return choCauTraLoiMoi(soTraLoiTruoc);
    }

    /** Bấm câu gợi ý thứ n (tính từ 0) rồi chờ câu trả lời mới. */
    public String bamGoiY(int thuTu) {
        int soTraLoiTruoc = driver.findElements(BONG_TRO_LY).size();
        bam(choDanhSach(CAU_GOI_Y).get(thuTu));
        return choCauTraLoiMoi(soTraLoiTruoc);
    }

    private String choCauTraLoiMoi(int soTraLoiTruoc) {
        cho.until(d -> d.findElements(BONG_TRO_LY).size() > soTraLoiTruoc);
        List<WebElement> cacCau = driver.findElements(BONG_TRO_LY);
        return cacCau.get(cacCau.size() - 1).getText().trim();
    }
}
