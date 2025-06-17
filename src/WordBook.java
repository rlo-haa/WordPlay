import java.io.*;
import java.util.*;

/**
 * 단어장 클래스
 * 단어들과 그 정보를 관리
 */
public class WordBook implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Map<String, WordData> words;

    public WordBook(String name) {
        this.name = name;
        this.words = new HashMap<>();
    }

    // 단어 추가
    public boolean addWord(String word, String meaning) {
        if (word == null || word.trim().isEmpty() ||
                meaning == null || meaning.trim().isEmpty()) {
            return false;
        }

        word = word.trim();
        meaning = meaning.trim();

        if (words.containsKey(word)) {
            return false; // 중복 단어
        }

        words.put(word, new WordData(word, meaning));
        return true;
    }

    // 단어 삭제
    public boolean removeWord(String word) {
        return words.remove(word) != null;
    }

    // 단어 수정
    public boolean updateWord(String word, String newMeaning) {
        WordData wordData = words.get(word);
        if (wordData != null) {
            wordData.setMeaning(newMeaning.trim());
            return true;
        }
        return false;
    }

    // 단어 검색
    public WordData getWordData(String word) {
        return words.get(word);
    }

    // 모든 단어 반환
    public Set<String> getAllWords() {
        return words.keySet();
    }

    // 랜덤 단어 반환
    public String getRandomWord() {
        if (words.isEmpty()) return null;

        List<String> wordList = new ArrayList<>(words.keySet());
        Random random = new Random();
        return wordList.get(random.nextInt(wordList.size()));
    }

    // 단어 개수 반환
    public int getWordCount() {
        return words.size();
    }

    // 통계가 낮은 단어들 반환 (정답률 50% 미만)
    public List<WordData> getLowAccuracyWords() {
        List<WordData> lowAccuracyWords = new ArrayList<>();
        for (WordData wordData : words.values()) {
            if (wordData.getTotalCount() > 0 && wordData.getAccuracy() < 50.0) {
                lowAccuracyWords.add(wordData);
            }
        }
        return lowAccuracyWords;
    }

    // 모든 단어 데이터 반환
    public Collection<WordData> getAllWordData() {
        return words.values();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return String.format("%s (%d개 단어)", name, words.size());
    }
}