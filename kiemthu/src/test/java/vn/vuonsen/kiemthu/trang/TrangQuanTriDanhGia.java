package vn.vuonsen.kiemthu.trang;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.vuonsen.kiemthu.coso.TrangCoSo;

import java.util.List;

/** Trang quản trị duyệt đánh giá: /quan-tri/danh-gia. */
public class TrangQuanTriDanhGia extends TrangCoSo {

    public static final String DUONG_DAN = "/quan-tri/danh-gia";

    private static final By TAB_CHO_DUYET = dt("tab-pending");
    private static final By TAB_DA_DUYET = dt("tab-approved");
    private static final By DONG = dt("admin-review-row");
    private static final By NOI_DUNG = dt("admin-review-content");
    private static final By NUT_DUYET = dt("action-approve");

    public TrangQuanTriDanhGia(WebDriver driver) {
        super(driver);
    }

    public TrangQuanTriDanhGia mo() {
        moDuongDan(DUONG_DAN);
        choHien(TAB_CHO_DUYET);
        return this;
    }

    public TrangQuanTriDanhGia xemChoDuyet() {
        bam(TAB_CHO_DUYET);
        return this;
    }

    public TrangQuanTriDanhGia xemDaDuyet() {
        bam(TAB_DA_DUYET);
        return this;
    }

    public List<String> noiDungDangHien() {
        return docDanhSachOnDinh(NOI_DUNG);
    }

    /** Duyệt đánh giá có nội dung cho trước rồi chờ nó rời khỏi danh sách chờ duyệt. */
    public TrangQuanTriDanhGia duyet(String noiDung) {
        WebElement dong = choDanhSach(DONG).stream()
                .filter(d -> d.findElement(NOI_DUNG).getText().trim().equals(noiDung))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không thấy đánh giá: " + noiDung));
        bam(dong.findElement(NUT_DUYET));
        cho.until(d -> {
            try {
                return d.findElements(NOI_DUNG).stream().noneMatch(p -> p.getText().trim().equals(noiDung));
            } catch (StaleElementReferenceException e) {
                return false;
            }
        });
        return this;
    }
}
