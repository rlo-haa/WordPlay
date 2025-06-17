import java.io.*;
import java.util.*;

/**
 * 여러 단어장을 관리하는 클래스
 */
public class WordBookManager {
    private Map<String, WordBook> wordBooks;
    private WordBook currentWordBook;
    private static final String DATA_FILE = "wordbooks.dat";

    public WordBookManager() {
        this.wordBooks = new HashMap<>();
        this.currentWordBook = null;
        loadData();
    }

    // 단어장 생성
    public boolean createWordBook(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        name = name.trim();
        if (wordBooks.containsKey(name)) {
            return false; // 중복 이름
        }

        WordBook newBook = new WordBook(name);
        wordBooks.put(name, newBook);
        saveData();
        return true;
    }

    // 단어장 삭제
    public boolean deleteWordBook(String name) {
        if (wordBooks.remove(name) != null) {
            if (currentWordBook != null && currentWordBook.getName().equals(name)) {
                currentWordBook = null;
            }
            saveData();
            return true;
        }
        return false;
    }

    // 단어장 이름 변경
    public boolean renameWordBook(String oldName, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }

        newName = newName.trim();
        if (wordBooks.containsKey(newName)) {
            return false; // 새 이름이 이미 존재
        }

        WordBook book = wordBooks.remove(oldName);
        if (book != null) {
            book.setName(newName);
            wordBooks.put(newName, book);
            saveData();
            return true;
        }
        return false;
    }

    // 단어장 선택
    public boolean selectWordBook(String name) {
        WordBook book = wordBooks.get(name);
        if (book != null) {
            currentWordBook = book;
            return true;
        }
        return false;
    }

    // 현재 단어장 반환
    public WordBook getCurrentWordBook() {
        return currentWordBook;
    }

    // 모든 단어장 이름 반환
    public Set<String> getAllWordBookNames() {
        return wordBooks.keySet();
    }

    // 단어장 개수 반환
    public int getWordBookCount() {
        return wordBooks.size();
    }

    // 데이터 저장
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(wordBooks);
            System.out.println("데이터가 저장되었습니다.");
        } catch (IOException e) {
            System.err.println("데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }

    // 데이터 로드
    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            wordBooks = (Map<String, WordBook>) ois.readObject();
            System.out.println("데이터가 로드되었습니다.");
        } catch (FileNotFoundException e) {
            System.out.println("저장된 데이터 파일이 없습니다. 새로 시작합니다.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("데이터 로드 중 오류 발생: " + e.getMessage());
            System.out.println("새로 시작합니다.");
        }
    }
}