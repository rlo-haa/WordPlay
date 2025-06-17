import java.io.Serializable;

/**
 * 단어 데이터를 관리하는 클래스
 * 단어, 뜻, 통계 정보를 저장
 */
public class WordData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String word;        // 단어
    private String meaning;     // 뜻
    private int totalCount;     // 총 출제 횟수
    private int correctCount;   // 정답 횟수

    public WordData(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
        this.totalCount = 0;
        this.correctCount = 0;
    }

    // 문제 출제 시 호출
    public void increaseTotal() {
        this.totalCount++;
    }

    // 정답 시 호출
    public void increaseCorrect() {
        this.correctCount++;
    }

    // 정답률 계산
    public double getAccuracy() {
        if (totalCount == 0) return 0.0;
        return (double) correctCount / totalCount * 100.0;
    }

    // Getters and Setters
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public int getTotalCount() { return totalCount; }
    public int getCorrectCount() { return correctCount; }

    @Override
    public String toString() {
        return String.format("%s : %s (정답률: %.1f%%, 출제: %d회)",
                word, meaning, getAccuracy(), totalCount);
    }
}