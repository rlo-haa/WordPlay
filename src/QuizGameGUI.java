import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * GUI 기반 퀴즈 게임
 */
public class QuizGameGUI extends JFrame {
    private WordBook wordBook;
    private java.util.List<String> wordList;
    private int currentQuestionIndex;
    private int correctAnswers;
    private boolean meaningFirst; // true: 뜻 보고 단어 맞히기, false: 단어 보고 뜻 맞히기

    // GUI 컴포넌트
    private JLabel questionLabel;
    private JLabel progressLabel;
    private JButton[] optionButtons;
    private JLabel resultLabel;
    private JButton nextButton;
    private JButton finishButton;

    public QuizGameGUI(WordBook wordBook) {
        this.wordBook = wordBook;
        this.wordList = new ArrayList<>(wordBook.getAllWords());
        Collections.shuffle(this.wordList);
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;

        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("WordPlay - 퀴즈 게임");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 패널 (진행률)
        JPanel topPanel = new JPanel();
        progressLabel = new JLabel();
        topPanel.add(progressLabel);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널 (문제 및 선택지)
        JPanel centerPanel = new JPanel(new BorderLayout());

        // 문제 표시
        questionLabel = new JLabel("", JLabel.CENTER);
        questionLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.add(questionLabel, BorderLayout.NORTH);

        // 선택지 버튼들
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        optionButtons = new JButton[4];
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
            optionButtons[i].addActionListener(new OptionButtonListener(i));
            optionsPanel.add(optionButtons[i]);
        }

        centerPanel.add(optionsPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 하단 패널 (결과 및 다음 버튼)
        JPanel bottomPanel = new JPanel(new FlowLayout());

        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        bottomPanel.add(resultLabel);

        nextButton = new JButton("다음 문제");
        nextButton.addActionListener(e -> nextQuestion());
        nextButton.setVisible(false);
        bottomPanel.add(nextButton);

        finishButton = new JButton("결과 보기");
        finishButton.addActionListener(e -> showFinalResult());
        finishButton.setVisible(false);
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // 모드 선택 다이얼로그
        selectMode();

        setSize(600, 500);
        setLocationRelativeTo(null);
        setVisible(true);

        showQuestion();
    }

    private void selectMode() {
        String[] options = {"뜻 보고 단어 맞히기", "단어 보고 뜻 맞히기"};
        int choice = JOptionPane.showOptionDialog(this,
                "퀴즈 모드를 선택하세요:", "모드 선택",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]);

        meaningFirst = (choice == 0);
    }

    private void showQuestion() {
        if (currentQuestionIndex >= wordList.size()) {
            showFinalResult();
            return;
        }

        // 진행률 업데이트
        progressLabel.setText(String.format("문제 %d / %d",
                currentQuestionIndex + 1, wordList.size()));

        String currentWord = wordList.get(currentQuestionIndex);
        WordData currentWordData = wordBook.getWordData(currentWord);
        currentWordData.increaseTotal();

        // 문제 표시
        if (meaningFirst) {
            questionLabel.setText("<html><center>다음 뜻에 해당하는 단어는?<br><br><b>" +
                    currentWordData.getMeaning() + "</b></center></html>");
        } else {
            questionLabel.setText("<html><center>다음 단어의 뜻은?<br><br><b>" +
                    currentWordData.getWord() + "</b></center></html>");
        }

        // 선택지 생성
        generateOptions(currentWordData);

        // 버튼 상태 초기화
        for (JButton button : optionButtons) {
            button.setEnabled(true);
            button.setBackground(null);
        }

        resultLabel.setText(" ");
        nextButton.setVisible(false);
        finishButton.setVisible(false);
    }

    private void generateOptions(WordData correctWordData) {
        java.util.List<String> options = new ArrayList<>();

        // 정답 추가
        if (meaningFirst) {
            options.add(correctWordData.getWord());
        } else {
            options.add(correctWordData.getMeaning());
        }

        // 오답 3개 추가
        java.util.List<WordData> allWords = new ArrayList<>(wordBook.getAllWordData());
        allWords.remove(correctWordData);
        Collections.shuffle(allWords);

        int wrongAnswersAdded = 0;
        for (WordData wordData : allWords) {
            if (wrongAnswersAdded >= 3) break;

            String option = meaningFirst ? wordData.getWord() : wordData.getMeaning();
            if (!options.contains(option)) {
                options.add(option);
                wrongAnswersAdded++;
            }
        }

        // 선택지가 4개 미만인 경우 더미 데이터 추가
        while (options.size() < 4) {
            options.add("선택지 " + options.size());
        }

        // 선택지 섞기
        Collections.shuffle(options);

        // 버튼에 설정
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options.get(i));
        }
    }

    private class OptionButtonListener implements ActionListener {
        private int optionIndex;

        public OptionButtonListener(int optionIndex) {
            this.optionIndex = optionIndex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedAnswer = optionButtons[optionIndex].getText();
            String currentWord = wordList.get(currentQuestionIndex);
            WordData currentWordData = wordBook.getWordData(currentWord);

            String correctAnswer = meaningFirst ? currentWordData.getWord() : currentWordData.getMeaning();
            boolean isCorrect = selectedAnswer.equals(correctAnswer);

            // 모든 버튼 비활성화
            for (JButton button : optionButtons) {
                button.setEnabled(false);
            }

            // 정답/오답 표시
            if (isCorrect) {
                optionButtons[optionIndex].setBackground(Color.GREEN);
                resultLabel.setText("정답입니다!");
                resultLabel.setForeground(Color.BLUE);
                currentWordData.increaseCorrect();
                correctAnswers++;
            } else {
                optionButtons[optionIndex].setBackground(Color.RED);
                // 정답 버튼 표시
                for (int i = 0; i < 4; i++) {
                    if (optionButtons[i].getText().equals(correctAnswer)) {
                        optionButtons[i].setBackground(Color.GREEN);
                        break;
                    }
                }
                resultLabel.setText("틀렸습니다. 정답: " + correctAnswer);
                resultLabel.setForeground(Color.RED);
            }

            // 다음 버튼 또는 완료 버튼 표시
            if (currentQuestionIndex < wordList.size() - 1) {
                nextButton.setVisible(true);
            } else {
                finishButton.setVisible(true);
            }
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        showQuestion();
    }

    private void showFinalResult() {
        double accuracy = (double) correctAnswers / wordList.size() * 100;

        String message = String.format(
                "퀴즈 완료!\n\n" +
                        "총 문제 수: %d개\n" +
                        "정답 수: %d개\n" +
                        "정답률: %.1f%%\n\n" +
                        "수고하셨습니다!",
                wordList.size(), correctAnswers, accuracy
        );

        JOptionPane.showMessageDialog(this, message, "퀴즈 결과", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public static void startQuiz(WordBook wordBook) {
        SwingUtilities.invokeLater(() -> new QuizGameGUI(wordBook));
    }
}