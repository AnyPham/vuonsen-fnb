package vn.vuonsen.fnb.modules.recommendation.canhan;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.SimilarityInput;

import java.util.List;
import java.util.Set;

/*
 * Các độ đo tương đồng mà hệ gợi ý dùng. Đây là chỗ duy nhất trong mã nguồn tính độ tương
 * đồng; mọi nơi khác chỉ gọi vào đây.
 *
 * Ba độ đo, mỗi độ đo hợp với một kiểu dữ liệu:
 *
 *   cosine  — hai vector số thực (vector hồ sơ, vector đặc trưng sảnh, vector món).
 *             cos(a, b) = (a · b) / (‖a‖ · ‖b‖). Chỉ đo hướng, không đo độ dài, nên khách đặt
 *             mười tiệc cưới và khách đặt một tiệc cưới vẫn được coi là giống nhau.
 *   Pearson — cũng trên hai vector số thực, nhưng trừ trung bình của từng vector trước rồi mới
 *             tính cosine. Dùng để so sánh với cosine trong phần đánh giá.
 *   Jaccard — hai tập hợp, ở đây là tập sảnh hai khách đã đặt. |A ∩ B| / |A ∪ B|. Là cách so
 *             người với người kinh điển của lọc cộng tác trên dữ liệu có hoặc không.
 *
 * Thư viện: cosine và Pearson gọi Apache Commons Math 3.6.1, Jaccard gọi Apache Commons Text
 * 1.15.0. Hàm RealVector.cosine của Commons Math làm đúng công thức trên: lấy dotProduct chia
 * cho tích hai getNorm, và ném MathArithmeticException khi một trong hai vector bằng không.
 * Hệ gợi ý coi vector bằng không là "chưa có thông tin" nên chặn trước, trả 0.
 *
 * Vì sao không dùng thư viện gợi ý trọn gói như Apache Mahout: bản cuối của phần gợi ý
 * (mahout-mr) là 0.13.0 phát hành tháng 4/2017, kéo theo hadoop-client, lucene và guava đời
 * cũ, dễ xung đột với Spring Boot 3. Mahout cũng giấu phép so khớp bên trong lớp
 * GenericUserBasedRecommender. Ở đây dùng thư viện cho phần toán, còn dựng hồ sơ, tìm láng
 * giềng và kết hợp điểm tự viết để chỉ ra được từng bước.
 */
public final class DoTuongDong {

    private static final JaccardSimilarity JACCARD = new JaccardSimilarity();

    private DoTuongDong() {
    }

    /* Cosine của hai vector. Một trong hai vector bằng không thì trả 0, không ném lỗi. */
    public static double cosine(double[] a, double[] b) {
        if (laVectorKhong(a) || laVectorKhong(b)) {
            return 0;
        }
        return new ArrayRealVector(a, false).cosine(new ArrayRealVector(b, false));
    }

    /*
     * Hệ số tương quan Pearson của hai vector, nằm trong [-1, 1].
     *
     * Vector có mọi phần tử bằng nhau thì độ lệch chuẩn bằng 0 và Pearson không xác định,
     * Commons Math trả NaN; quy về 0, tức coi như không có tương quan.
     */
    public static double pearson(double[] a, double[] b) {
        if (a.length < 2 || laVectorKhong(a) || laVectorKhong(b)) {
            return 0;
        }
        double r = new PearsonsCorrelation().correlation(a, b);
        return Double.isNaN(r) ? 0 : r;
    }

    /*
     * Jaccard của hai tập.
     *
     * Commons Text coi hai tập cùng rỗng là giống nhau hoàn toàn và trả 1. Với hệ gợi ý thì
     * hai khách chưa đặt gì không có căn cứ nào để coi là giống nhau, nên trả 0.
     */
    public static <T> double jaccard(Set<T> a, Set<T> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        return JACCARD.apply(dauVao(a), dauVao(b));
    }

    /*
     * Cosine của hai vector có hoặc không, viết dưới dạng tập: |A ∩ B| / √(|A| · |B|).
     *
     * Dùng cho lọc cộng tác theo món: mỗi món là một cột của ma trận khách × món, ô bằng 1 nếu
     * khách từng gọi món đó. Cosine của hai cột như vậy rút gọn đúng thành công thức trên, nên
     * tính thẳng trên tập cho nhanh thay vì dựng hai vector dài bằng số khách. Bài kiểm thử
     * DoTuongDongTest đối chiếu kết quả với RealVector.cosine trên vector đầy đủ.
     */
    public static <T> double cosineTapHop(Set<T> a, Set<T> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        Set<T> nho = a.size() <= b.size() ? a : b;
        Set<T> lon = nho == a ? b : a;
        long chung = nho.stream().filter(lon::contains).count();
        return chung / Math.sqrt((double) a.size() * b.size());
    }

    private static boolean laVectorKhong(double[] v) {
        for (double x : v) {
            if (x != 0) {
                return false;
            }
        }
        return true;
    }

    /* Commons Text chỉ nhận chuỗi hoặc SimilarityInput, nên bọc tập thành SimilarityInput. */
    private static <T> SimilarityInput<T> dauVao(Set<T> tap) {
        List<T> ds = List.copyOf(tap);
        return new SimilarityInput<>() {
            @Override
            public T at(int i) {
                return ds.get(i);
            }

            @Override
            public int length() {
                return ds.size();
            }
        };
    }
}
