import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * 액션 게임 GUI - 단어 매칭 퍼즐 게임
 */
public class ActionGameGUI extends JFrame {
    private WordBook wordBook;
    private java.util.List<WordData> gameWords;
    private java.util.List<JButton> wordButtons;
    private java.util.List<JButton> meaningButtons;
    private JButton selectedButton;
    private int matchedPairs;
    private int totalPairs;
    private int attempts;
    private long startTime;

    // GUI 컴포넌트
    private JLabel statusLabel;
    private JPanel gamePanel;
    private JButton resetButton;
    private JButton exitButton;

    public ActionGameGUI(WordBook wordBook) {
        this.wordBook = wordBook;
        this.gameWords = new ArrayList<>();
        this.wordButtons = new ArrayList<>();
        this.meaningButtons = new ArrayList<>();
        this.selectedButton = null;
        this.matchedPairs = 0;
        this.attempts = 0;

        initializeGame();
        initializeGUI();
    }

    private void initializeGame() {
        // 게임용 단어 선택 (최대 8개)
        java.util.List<WordData> allWords = new ArrayList<>(wordBook.getAllWordData());
        Collections.shuffle(allWords);

        int gameSize = Math.min(8, allWords.size());
        for (int i = 0; i < gameSize; i++) {
            gameWords.add(allWords.get(i));
        }

        totalPairs = gameWords.size();
        startTime = System.currentTimeMillis();
    }

    private void initializeGUI() {
        setTitle("WordPlay - 단어 매칭 게임");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 패널 (상태 정보)
        JPanel topPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel();
        updateStatus();
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);

        // 게임 패널
        setupGamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // 하단 패널 (버튼들)
        JPanel bottomPanel = new JPanel(new FlowLayout());

        resetButton = new JButton("다시 시작");
        resetButton.addActionListener(e -> resetGame());
        bottomPanel.add(resetButton);

        exitButton = new JButton("게임 종료");
        exitButton.addActionListener(e -> dispose());
        bottomPanel.add(exitButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        // 게임 시작 안내
        JOptionPane.showMessageDialog(this,
                "단어 매칭 게임에 오신 것을 환영합니다!\n\n" +
                        "왼쪽의 단어와 오른쪽의 뜻을 매칭하세요.\n" +
                        "단어를 클릭한 후 해당하는 뜻을 클릭하면 됩니다.\n\n" +
                        "행운을 빕니다!",
                "게임 방법", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupGamePanel() {
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 왼쪽 패널 (단어들)
        JPanel leftPanel = new JPanel(new GridLayout(totalPairs, 1, 5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("단어"));

        // 오른쪽 패널 (뜻들)
        JPanel rightPanel = new JPanel(new GridLayout(totalPairs, 1, 5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("뜻"));

        // 단어 버튼들 생성
        for (WordData wordData : gameWords) {
            JButton wordButton = new JButton(wordData.getWord());
            wordButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
            wordButton.addActionListener(new WordButtonListener(wordData));
            wordButtons.add(wordButton);
            leftPanel.add(wordButton);
        }

        // 뜻 버튼들 생성 (순서 섞기)
        java.util.List<WordData> shuffledMeanings = new ArrayList<>(gameWords);
        Collections.shuffle(shuffledMeanings);

        for (WordData wordData : shuffledMeanings) {
            JButton meaningButton = new JButton("<html><center>" +
                    wordData.getMeaning() + "</center></html>");
            meaningButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
            meaningButton.addActionListener(new MeaningButtonListener(wordData));
            meaningButtons.add(meaningButton);
            rightPanel.add(meaningButton);
        }

        gamePanel.add(leftPanel, BorderLayout.WEST);
        gamePanel.add(rightPanel, BorderLayout.EAST);
    }

    private class WordButtonListener implements ActionListener {
        private WordData wordData;

        public WordButtonListener(WordData wordData) {
            this.wordData = wordData;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();

            // 이미 매칭된 버튼은 클릭 불가
            if (!button.isEnabled()) return;

            // 이전 선택 해제
            if (selectedButton != null) {
                selectedButton.setBackground(null);
            }

            // 새로운 선택
            selectedButton = button;
            selectedButton.setBackground(Color.CYAN);
        }
    }

    private class MeaningButtonListener implements ActionListener {
        private WordData wordData;

        public MeaningButtonListener(WordData wordData) {
            this.wordData = wordData;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton meaningButton = (JButton) e.getSource();

            // 이미 매칭된 버튼은 클릭 불가
            if (!meaningButton.isEnabled()) return;

            // 단어가 선택되지 않은 경우
            if (selectedButton == null) {
                JOptionPane.showMessageDialog(ActionGameGUI.this,
                        "먼저 왼쪽에서 단어를 선택하세요!", "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            attempts++;

            // 매칭 확인
            WordData selectedWordData = null;
            for (WordData wd : gameWords) {
                if (wd.getWord().equals(selectedButton.getText())) {
                    selectedWordData = wd;
                    break;
                }
            }

            if (selectedWordData != null && selectedWordData.equals(wordData)) {
                // 정답!
                selectedButton.setBackground(Color.GREEN);
                selectedButton.setEnabled(false);
                meaningButton.setBackground(Color.GREEN);
                meaningButton.setEnabled(false);

                // 통계 업데이트
                selectedWordData.increaseTotal();
                selectedWordData.increaseCorrect();

                matchedPairs++;
                selectedButton = null;

                updateStatus();

                // 게임 완료 확인
                if (matchedPairs == totalPairs) {
                    gameCompleted();
                }
            } else {
                // 오답
                selectedButton.setBackground(Color.RED);
                meaningButton.setBackground(Color.RED);

                if (selectedWordData != null) {
                    selectedWordData.increaseTotal();
                }

                // 1초 후 색상 원래대로
                javax.swing.Timer timer = new javax.swing.Timer(1000, ae -> {
                    selectedButton.setBackground(null);
                    meaningButton.setBackground(null);
                });
                timer.setRepeats(false);
                timer.start();

                selectedButton = null;
                updateStatus();
            }
        }
    }

    private void updateStatus() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        statusLabel.setText(String.format("매칭 완료: %d/%d | 시도 횟수: %d | 경과 시간: %d초",
                matchedPairs, totalPairs, attempts, elapsed));
    }

    private void gameCompleted() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        double accuracy = totalPairs > 0 ? (double) matchedPairs / attempts * 100 : 0;

        String message = String.format(
                "축하합니다! 게임 완료!\n\n" +
                        "매칭 완료: %d개\n" +
                        "총 시도 횟수: %d번\n" +
                        "성공률: %.1f%%\n" +
                        "경과 시간: %d초\n\n" +
                        "훌륭합니다!",
                matchedPairs, attempts, accuracy, elapsed
        );

        JOptionPane.showMessageDialog(this, message, "게임 완료!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetGame() {
        // 게임 상태 초기화
        matchedPairs = 0;
        attempts = 0;
        selectedButton = null;
        startTime = System.currentTimeMillis();

        // 새로운 단어 선택
        gameWords.clear();
        wordButtons.clear();
        meaningButtons.clear();

        initializeGame();

        // GUI 재구성
        remove(gamePanel);
        setupGamePanel();
        add(gamePanel, BorderLayout.CENTER);

        updateStatus();
        revalidate();
        repaint();
    }

    public static void startGame(WordBook wordBook) {
        SwingUtilities.invokeLater(() -> new ActionGameGUI(wordBook));
    }
}