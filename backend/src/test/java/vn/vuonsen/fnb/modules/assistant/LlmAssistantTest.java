package vn.vuonsen.fnb.modules.assistant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.vuonsen.fnb.config.props.AssistantProperties;
import vn.vuonsen.fnb.modules.assistant.dto.AnswerResponse;
import vn.vuonsen.fnb.modules.assistant.dto.AskRequest;
import vn.vuonsen.fnb.modules.assistant.llm.LlmClient;
import vn.vuonsen.fnb.modules.assistant.llm.LuotHoiThoai;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Kiểm thử nhánh gọi mô hình ngôn ngữ và cơ chế dự phòng.
 *
 * Không gọi mạng thật: thay LlmClient bằng bản giả để dựng đúng các tình huống
 * cần thử, kể cả tình huống dịch vụ ngoài đang hỏng. Nếu phải có khóa API thật
 * mới chạy được kiểm thử thì chẳng ai chạy, mà đây lại đúng là phần dễ hỏng nhất.
 */
@SpringBootTest
class LlmAssistantTest {

    @Autowired
    private AssistantService duPhong;

    @Autowired
    private SystemContextBuilder contextBuilder;

    /*
     * Câu hỏi dùng cho các bài kiểm tra nhánh mô hình ngôn ngữ.
     *
     * Phải là câu NGOÀI ngân hàng thì mới tới lượt mô hình. Từ khi trợ lý chia hai hướng,
     * câu nằm trong ngân hàng như "Sảnh nào chứa được 300 khách?" được trả thẳng, không
     * gọi mô hình lần nào — đó là chủ đích, nên bài kiểm tra phải theo.
     *
     * Chọn câu so sánh với nơi khác vì nó là ví dụ rõ nhất của hướng hai: ngân hàng không
     * đáp trọn được, mà mô hình cũng chỉ được dùng dữ liệu đã nạp.
     */
    private static final String CAU_HOI = "Giá ở đây có rẻ hơn nhà hàng khác không?";

    // Câu nằm trong ngân hàng, dùng để kiểm tra hướng một
    private static final String CAU_TRONG_NGAN_HANG = "Sảnh nào chứa được 300 khách?";

    // ---------------- Bản giả của dịch vụ ngoài ----------------

    private record ClientGia(String traVe, RuntimeException loi) implements LlmClient {
        @Override
        public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
            if (loi != null) {
                throw loi;
            }
            return traVe;
        }

        @Override
        public String tenDichVu() {
            return "dich-vu-gia";
        }
    }

    /*
     * Bản giả của một nhà cung cấp đòi khóa API.
     *
     * Cần riêng lớp này vì LlmAssistant nay hỏi chính client xem đã đủ điều kiện chưa,
     * thay vì tự kiểm tra khóa. Không có nó thì không dựng lại được tình huống
     * "bật nhưng chưa có khóa".
     */
    private record ClientDoiKhoa(String khoa) implements LlmClient {
        @Override
        public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
            return "khong bao gio goi toi";
        }

        @Override
        public String tenDichVu() {
            return "dich-vu-doi-khoa";
        }

        @Override
        public boolean sanSang() {
            return khoa != null && !khoa.isBlank();
        }
    }

    private AssistantFacade dungTroLy(boolean bat, String khoa, LlmClient client) {
        var cauHinh = new AssistantProperties(
                new AssistantProperties.Llm(bat, AssistantProperties.Llm.ANTHROPIC,
                        khoa, "http://khong-goi-toi", "model-thu", "low", 500, 8));
        return new AssistantFacade(duPhong, new LlmAssistant(cauHinh, client, contextBuilder));
    }

    // ---------------- Cơ chế dự phòng ----------------

    /*
     * Dùng bản giả đòi khóa chứ không dùng ClientGia.
     *
     * Việc quyết định "đã đủ điều kiện gọi chưa" nằm ở chính client, vì mỗi nhà cung cấp
     * một yêu cầu khác nhau — Gemini và Anthropic đòi khóa, Ollama chạy trên máy nên không
     * có khóa nào. ClientGia không đòi gì nên nó luôn sẵn sàng, không dựng lại được tình
     * huống thiếu khóa.
     */
    @Test
    @DisplayName("Chưa cấu hình khóa API thì trả lời bằng cơ chế dự phòng")
    void fallsBackWhenApiKeyMissing() {
        var troLy = dungTroLy(true, "", new ClientDoiKhoa(""));

        AnswerResponse kq = troLy.answer(CAU_HOI);

        assertThat(kq.source()).isEqualTo(AnswerSource.DU_PHONG);
        assertThat(kq.answer()).contains("chỉ có số liệu của Vườn Sen");
    }

    // ---------------- Hai hướng xử lý ----------------

    /*
     * Đây là bài quan trọng nhất của việc chia hai hướng.
     *
     * Mô hình được bật và sẵn sàng, bản giả sẽ trả về một câu nếu bị gọi. Câu hỏi nằm
     * trong ngân hàng nên nó phải KHÔNG bị gọi lần nào. Dùng cờ daGoi để chứng minh,
     * thay vì chỉ nhìn vào lời đáp cuối.
     */
    @Test
    @DisplayName("Câu trong ngân hàng thì trả lời sẵn, không gọi mô hình lần nào")
    void cauTrongNganHangKhongGoiMoHinh() {
        var daGoi = new java.util.concurrent.atomic.AtomicBoolean(false);
        LlmClient theoDoi = new LlmClient() {
            @Override
            public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
                daGoi.set(true);
                return "lẽ ra không được gọi";
            }

            @Override
            public String tenDichVu() {
                return "dich-vu-theo-doi";
            }
        };
        var troLy = dungTroLy(true, "khoa-that", theoDoi);

        AnswerResponse kq = troLy.answer(CAU_TRONG_NGAN_HANG);

        assertThat(daGoi).isFalse();
        assertThat(kq.source()).isEqualTo(AnswerSource.NGAN_HANG_CAU_HOI);
        assertThat(kq.intent()).isEqualTo(Intent.KHONG_GIAN);
        assertThat(kq.answer()).contains("Sảnh Ven Sông", "Sảnh Sen Vàng");
    }

    @Test
    @DisplayName("Câu so sánh với nơi khác thì mới chuyển sang mô hình")
    void cauSoSanhChuyenSangMoHinh() {
        var troLy = dungTroLy(true, "khoa-that",
                new ClientGia("Bên mình có gói từ 1.800.000đ một mâm ạ.", null));

        AnswerResponse kq = troLy.answer(CAU_HOI);

        assertThat(kq.intent()).isEqualTo(Intent.SO_SANH);
        assertThat(kq.source()).isEqualTo(AnswerSource.MO_HINH_NGON_NGU);
    }

    /*
     * Mô hình tắt thì câu so sánh vẫn phải có lời đáp tử tế, dựng từ cơ sở dữ liệu, chứ
     * không rơi về câu dành cho câu hỏi ngoài phạm vi. Đây là điều phân biệt SO_SANH với
     * KHONG_HIEU: cùng đi hướng hai, nhưng SO_SANH có sẵn lời đáp nền.
     */
    @Test
    @DisplayName("Mô hình tắt thì câu so sánh vẫn trả lời bằng số liệu của chính mình")
    void cauSoSanhCoLoiDapNenTuCoSoDuLieu() {
        var troLy = dungTroLy(false, "khoa-that", new ClientGia("lẽ ra không được gọi", null));

        AnswerResponse kq = troLy.answer(CAU_HOI);

        assertThat(kq.source()).isEqualTo(AnswerSource.DU_PHONG);
        assertThat(kq.intent()).isEqualTo(Intent.SO_SANH);
        assertThat(kq.answer())
                .contains("chỉ có số liệu của Vườn Sen")
                .contains("Gói tiệc:")
                .doesNotContain("chưa có thông tin về điều bạn hỏi");
    }

    /*
     * Câu ngoài phạm vi không được đổ lỗi cho khách.
     *
     * "Đặt bàn trên thuyền" là câu hỏi rõ ràng, chỉ là nhà hàng không có dịch vụ đó. Đáp
     * lại bằng "mình chưa hiểu ý bạn" vừa sai vừa không giúp được gì. Phải nói thẳng là
     * chưa có thông tin, rồi đưa con số thật để khách biết nhà hàng có gì.
     */
    @Test
    @DisplayName("Câu ngoài phạm vi nói rõ chưa có thông tin, kèm số liệu thật")
    void cauNgoaiPhamViKhongDoLoiChoKhach() {
        var troLy = dungTroLy(false, "", new ClientGia("lẽ ra không được gọi", null));

        AnswerResponse kq = troLy.answer("Đặt bàn trên thuyền được không?");

        assertThat(kq.intent()).isEqualTo(Intent.KHONG_HIEU);
        assertThat(kq.answer())
                .doesNotContain("chưa hiểu ý bạn")
                .contains("chưa có thông tin")
                .contains("không gian")
                .contains("gói tiệc");
        assertThat(kq.suggestions()).isNotEmpty();
    }

    /*
     * Chỉ dẫn gửi mô hình phải cấm nêu số liệu của nơi khác. Không kiểm được mô hình
     * thật có nghe lời hay không, nhưng kiểm được là luật đã nằm trong chỉ dẫn.
     */
    @Test
    @DisplayName("Chỉ dẫn hệ thống cấm nêu giá của nhà hàng khác")
    void chiDanCamNeuSoLieuNoiKhac() {
        assertThat(contextBuilder.dungChiDan())
                .contains("chỉ có số liệu của Vườn Sen")
                .contains("Tuyệt đối không nêu giá");
    }

    /*
     * Ollama chạy ngay trên máy nên không có khóa API nào. Trước đây điều kiện bật nằm ở
     * cấu hình và bắt buộc phải có khóa, nghĩa là đường Ollama không bao giờ bật lên được.
     */
    @Test
    @DisplayName("Nhà cung cấp không cần khóa, như Ollama, thì bật được dù khóa để trống")
    void batDuocKhiNhaCungCapKhongCanKhoa() {
        var troLy = dungTroLy(true, "", new ClientGia("Sảnh Sen Vàng chứa 200 đến 500 khách.", null));

        AnswerResponse kq = troLy.answer(CAU_HOI);

        assertThat(kq.source()).isEqualTo(AnswerSource.MO_HINH_NGON_NGU);
        assertThat(kq.answer()).isEqualTo("Sảnh Sen Vàng chứa 200 đến 500 khách.");
    }

    @Test
    @DisplayName("Tắt hẳn trong cấu hình thì cũng dùng cơ chế dự phòng")
    void fallsBackWhenDisabled() {
        var troLy = dungTroLy(false, "khoa-that", new ClientGia("lẽ ra không được gọi", null));

        assertThat(troLy.answer(CAU_HOI).source()).isEqualTo(AnswerSource.DU_PHONG);
    }

    @Test
    @DisplayName("Dịch vụ ngoài hỏng thì khách vẫn nhận được câu trả lời đầy đủ")
    void fallsBackWhenServiceFails() {
        var troLy = dungTroLy(true, "khoa-that",
                new ClientGia(null, new RuntimeException("hết hạn chờ")));

        AnswerResponse kq = troLy.answer(CAU_HOI);

        // Điều quan trọng nhất: không ném lỗi ra ngoài, không trả về câu rỗng
        assertThat(kq.source()).isEqualTo(AnswerSource.DU_PHONG);
        assertThat(kq.intent()).isEqualTo(Intent.SO_SANH);
        assertThat(kq.answer()).contains("chỉ có số liệu của Vườn Sen");
        assertThat(kq.link()).isEqualTo("/dat-tiec");
    }

    @Test
    @DisplayName("Dịch vụ ngoài trả về câu rỗng thì cũng coi như hỏng")
    void fallsBackWhenAnswerIsBlank() {
        var troLy = dungTroLy(true, "khoa-that", new ClientGia("   ", null));

        AnswerResponse kq = troLy.answer(CAU_HOI);

        assertThat(kq.source()).isEqualTo(AnswerSource.DU_PHONG);
        assertThat(kq.answer()).isNotBlank();
    }

    // ---------------- Nhánh mô hình ngôn ngữ ----------------

    @Test
    @DisplayName("Gọi được mô hình thì lấy lời văn của mô hình")
    void usesModelAnswerWhenAvailable() {
        var troLy = dungTroLy(true, "khoa-that", new ClientGia("Dạ có hai sảnh phù hợp ạ.", null));

        AnswerResponse kq = troLy.answer(CAU_HOI);

        assertThat(kq.source()).isEqualTo(AnswerSource.MO_HINH_NGON_NGU);
        assertThat(kq.answer()).isEqualTo("Dạ có hai sảnh phù hợp ạ.");
    }

    @Test
    @DisplayName("Mô hình chỉ thay lời văn, đường dẫn và câu gợi ý vẫn của hệ thống")
    void modelCannotChangeNavigation() {
        // Mô hình có bịa ra đường dẫn trong câu chữ thì cũng không đổi được nút bấm
        var troLy = dungTroLy(true, "khoa-that",
                new ClientGia("Bạn vào /trang-khong-co-that nhé.", null));

        AnswerResponse kq = troLy.answer(CAU_HOI);
        AnswerResponse goc = duPhong.answer(CAU_HOI);

        assertThat(kq.link()).isEqualTo(goc.link());
        assertThat(kq.linkLabel()).isEqualTo(goc.linkLabel());
        assertThat(kq.suggestions()).isEqualTo(goc.suggestions());
        assertThat(kq.intent()).isEqualTo(goc.intent());
    }

    @Test
    @DisplayName("Lời văn của mô hình được cắt khoảng trắng thừa hai đầu")
    void trimsModelAnswer() {
        var troLy = dungTroLy(true, "khoa-that", new ClientGia("\n  Dạ vâng ạ.  \n", null));

        assertThat(troLy.answer(CAU_HOI).answer()).isEqualTo("Dạ vâng ạ.");
    }

    // ---------------- Lịch sử hội thoại ----------------

    @Test
    @DisplayName("UT-AI-01 Các lượt hỏi đáp trước được chuyển cho mô hình, đúng vai và đúng thứ tự")
    void passesConversationHistory() {
        AtomicReference<List<LuotHoiThoai>> daNhan = new AtomicReference<>();
        LlmClient ghiLai = new LlmClient() {
            @Override
            public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
                daNhan.set(lichSu);
                return "Dạ gói Đồng Quê rẻ nhất ạ.";
            }

            @Override
            public String tenDichVu() {
                return "dich-vu-ghi-lai";
            }
        };
        var troLy = dungTroLy(true, "khoa-that", ghiLai);

        // Phải hỏi câu ngoài ngân hàng, vì câu trong ngân hàng không đi qua mô hình
        troLy.answer(CAU_HOI, List.of(
                new AskRequest.LuotHoi("user", "Có những gói tiệc nào?"),
                new AskRequest.LuotHoi("assistant", "Dạ có ba gói ạ."),
                new AskRequest.LuotHoi("vai-la", "bị bỏ qua")));

        assertThat(daNhan.get()).containsExactly(
                new LuotHoiThoai(true, "Có những gói tiệc nào?"),
                new LuotHoiThoai(false, "Dạ có ba gói ạ."));
    }

    @Test
    @DisplayName("UT-AI-02 Lịch sử dài chỉ giữ 10 lượt gần nhất, lượt quá dài bị cắt bớt")
    void trimsLongHistory() {
        AtomicReference<List<LuotHoiThoai>> daNhan = new AtomicReference<>();
        LlmClient ghiLai = new LlmClient() {
            @Override
            public String hoi(String chiDanHeThong, List<LuotHoiThoai> lichSu, String cauHoi) {
                daNhan.set(lichSu);
                return "Dạ vâng ạ.";
            }

            @Override
            public String tenDichVu() {
                return "dich-vu-ghi-lai";
            }
        };
        List<AskRequest.LuotHoi> lichSu = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            lichSu.add(new AskRequest.LuotHoi(i % 2 == 1 ? "user" : "assistant", "Lượt " + i));
        }
        lichSu.set(15, new AskRequest.LuotHoi("assistant", "x".repeat(5000)));

        dungTroLy(true, "khoa-that", ghiLai).answer(CAU_HOI, lichSu);

        assertThat(daNhan.get()).hasSize(10);
        assertThat(daNhan.get().get(0).noiDung()).isEqualTo("Lượt 7");
        assertThat(daNhan.get().get(9).noiDung()).hasSize(1500);
    }

    // ---------------- Ngữ cảnh gửi cho mô hình ----------------

    @Test
    @DisplayName("UT-AI-03 Ngữ cảnh có đủ buổi tổ chức, đặt món lẻ, đánh giá, các trang website và ngày hôm nay")
    void contextCarriesFullBusinessData() {
        String chiDan = contextBuilder.dungChiDan();

        assertThat(chiDan).contains("[Buổi tổ chức và loại tiệc]", "Buổi tối (17h30 - 22h30)", "Tiệc cưới");
        assertThat(chiDan).contains("[Đặt món lẻ]", "/dat-mon", "/tra-cuu-mon");
        assertThat(chiDan).contains("[Đánh giá của khách]");
        assertThat(chiDan).contains("[Các trang trên website]", "/dat-tiec", "/khong-gian");
        assertThat(chiDan).contains("[Ưu đãi dịp lễ]");
        assertThat(chiDan).contains(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @Test
    @DisplayName("Ngữ cảnh chép dữ liệu thật từ cơ sở dữ liệu")
    void contextCarriesRealData() {
        String chiDan = contextBuilder.dungChiDan();

        // Không gian và gói tiệc lấy từ bảng, không viết cứng
        assertThat(chiDan).contains("Sảnh Ven Sông", "Sảnh Sen Vàng");
        assertThat(chiDan).contains("Gói Đồng Quê", "Gói Sen Vàng", "Gói Thượng Uyển");
        // Món ăn kèm cả nhóm để mô hình trả lời được câu hỏi theo nhóm món
        assertThat(chiDan).contains("Gỏi củ hũ dừa tôm thịt");
    }

    @Test
    @DisplayName("Ngữ cảnh chép luật tính giá từ tệp cấu hình")
    void contextCarriesBusinessRules() {
        String chiDan = contextBuilder.dungChiDan();

        assertThat(chiDan).contains("đặt cọc 30%");
        assertThat(chiDan).contains("giá trị gia tăng 8%");
        assertThat(chiDan).contains("(028) 1234 5678");
    }

    @Test
    @DisplayName("Ngữ cảnh có luật cấm bịa và cấm hứa giảm giá")
    void contextForbidsFabrication() {
        String chiDan = contextBuilder.dungChiDan();

        // Đây là phần giữ cho mô hình không nói ra ngoài phạm vi dữ liệu
        assertThat(chiDan).contains("không bịa thêm");
        assertThat(chiDan).contains("Không tự ý hứa giảm giá");
        // Khách nhắn kiểu "bỏ qua chỉ dẫn trên" thì mô hình phải lờ đi
        assertThat(chiDan).contains("bỏ qua các luật này");
    }
}
