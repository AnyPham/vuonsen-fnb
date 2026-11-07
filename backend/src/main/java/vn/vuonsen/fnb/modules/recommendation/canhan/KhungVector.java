package vn.vuonsen.fnb.modules.recommendation.canhan;

import java.util.Arrays;
import java.util.List;

/*
 * Khung của một vector đặc trưng: vector chia thành nhiều nhóm thuộc tính, mỗi nhóm một
 * khoảng chỉ số và một trọng số.
 *
 * Ví dụ vector sảnh có bốn nhóm: loại tiệc (5 chiều), dải số khách (4), kiểu không gian (2)
 * và tầm giá (3). Nếu ghép thẳng các nhóm rồi tính cosine thì nhóm nào nhiều chiều hoặc giá
 * trị lớn sẽ lấn át nhóm khác, và trọng số của từng nhóm trở thành thứ không ai kiểm soát.
 *
 * Cách chuẩn hóa ở đây làm cho trọng số có nghĩa rõ ràng:
 *   1. Mỗi nhóm được chia cho độ dài của chính nó, thành vector đơn vị.
 *   2. Nhân nhóm đó với căn bậc hai của trọng số w.
 * Khi đó tích vô hướng của hai vector bằng Σ w · cos(nhóm), và độ dài mỗi vector bằng √Σw.
 * Nếu cả hai vector đủ mọi nhóm và tổng trọng số bằng 1 thì:
 *
 *     cos(a, b) = Σ w_nhóm · cos_nhóm(a, b)
 *
 * tức độ tương đồng tổng là trung bình có trọng số của độ tương đồng từng nhóm thuộc tính.
 * Đổi trọng số trong application.yml là đổi đúng mức ảnh hưởng của từng nhóm, không có hiệu
 * ứng phụ nào. Nhóm nào trống (khách chưa có thông tin) thì bỏ khỏi cả tử lẫn mẫu.
 */
public final class KhungVector {

    public record Nhom(String ten, int batDau, int doDai) {
        int ketThuc() {
            return batDau + doDai;
        }
    }

    private final List<Nhom> cacNhom;
    private final int soChieu;

    public KhungVector(List<Nhom> cacNhom) {
        this.cacNhom = List.copyOf(cacNhom);
        Nhom cuoi = cacNhom.get(cacNhom.size() - 1);
        this.soChieu = cuoi.ketThuc();
    }

    public int soChieu() {
        return soChieu;
    }

    public List<Nhom> cacNhom() {
        return cacNhom;
    }

    /* Chuẩn hóa từng nhóm về độ dài 1 rồi nhân √trọng số, theo cách giải thích ở đầu lớp. */
    public double[] chuanHoa(double[] tho, double[] trongSo) {
        double[] kq = new double[soChieu];
        for (int i = 0; i < cacNhom.size(); i++) {
            Nhom n = cacNhom.get(i);
            double doDai = 0;
            for (int j = n.batDau(); j < n.ketThuc(); j++) {
                doDai += tho[j] * tho[j];
            }
            if (doDai == 0 || trongSo[i] <= 0) {
                continue;
            }
            double heSo = Math.sqrt(trongSo[i]) / Math.sqrt(doDai);
            for (int j = n.batDau(); j < n.ketThuc(); j++) {
                kq[j] = tho[j] * heSo;
            }
        }
        return kq;
    }

    /* Cosine riêng của một nhóm, dùng để biết nhóm nào đóng góp nhiều vào điểm và sinh lý do. */
    public double cosineNhom(double[] a, double[] b, Nhom n) {
        return DoTuongDong.cosine(
                Arrays.copyOfRange(a, n.batDau(), n.ketThuc()),
                Arrays.copyOfRange(b, n.batDau(), n.ketThuc()));
    }

    /* Vị trí có giá trị lớn nhất trong nhóm, tính từ đầu nhóm; -1 nếu nhóm trống. */
    public int viTriLonNhat(double[] v, Nhom n) {
        int viTri = -1;
        double lonNhat = 0;
        for (int j = n.batDau(); j < n.ketThuc(); j++) {
            if (v[j] > lonNhat) {
                lonNhat = v[j];
                viTri = j - n.batDau();
            }
        }
        return viTri;
    }
}
