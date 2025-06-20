import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * 액션 게임 GUI - 단어 매칭 퍼즐 게임
 */
public class ActionGameGUI extends JFrame {
    private WordBook wordBook;
    private java.util.List<WordData> gameWords;
    private java.util.List<GameButton> gameButtons;
    private GameButton selectedButton;
    private int matchedPairs;
    private int totalPairs;
    private int attempts;
    private long startTime;

    // GUI 컴포넌트
    private JLabel statusLabel;
    private JPanel gamePanel;
    private JButton resetButton;
    private JButton exitButton;

    // 게임 버튼 클래스
    private class GameButton extends JButton {
        public final WordData wordData;
        public final boolean isWord; // true면 단어, false면 뜻
        public boolean isMatched = false;

        public GameButton(WordData wordData, boolean isWord) {
            this.wordData = wordData;
            this.isWord = isWord;

            if (isWord) {
                setText(wordData.getWord());
                setFont(new Font("Malgun Gothic", Font.BOLD, 14));
            } else {
                setText("<html><center>" + wordData.getMeaning() + "</center></html>");
                setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
            }

            addActionListener(new GameButtonListener());
            setPreferredSize(new Dimension(180, 60));
        }
    }

    public ActionGameGUI(WordBook wordBook) {
        this.wordBook = wordBook;
        this.gameWords = new ArrayList<>();
        this.gameButtons = new ArrayList<>();
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

        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);

        // 게임 시작 안내
        JOptionPane.showMessageDialog(this,
                "단어 매칭 게임에 오신 것을 환영합니다!\n\n" +
                        "화면에 섞여 있는 단어와 뜻을 찾아 매칭하세요.\n" +
                        "단어를 클릭한 후 해당하는 뜻을 클릭하거나,\n" +
                        "뜻을 클릭한 후 해당하는 단어를 클릭하면 됩니다.\n\n" +
                        "행운을 빕니다!",
                "게임 방법", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupGamePanel() {
        gamePanel = new JPanel();
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 그리드 레이아웃 설정 (4열로 배치)
        int cols = 4;
        int rows = (int) Math.ceil((double)(totalPairs * 2) / cols);
        gamePanel.setLayout(new GridLayout(rows, cols, 10, 10));

        // 모든 버튼들을 리스트에 추가
        java.util.List<GameButton> allButtons = new ArrayList<>();

        // 단어 버튼들 추가
        for (WordData wordData : gameWords) {
            allButtons.add(new GameButton(wordData, true));
        }

        // 뜻 버튼들 추가
        for (WordData wordData : gameWords) {
            allButtons.add(new GameButton(wordData, false));
        }

        // 버튼들 섞기
        Collections.shuffle(allButtons);

        // 게임 버튼 리스트에 저장하고 패널에 추가
        gameButtons.clear();
        for (GameButton button : allButtons) {
            gameButtons.add(button);
            gamePanel.add(button);
        }

        // 빈 공간이 있다면 빈 패널로 채우기
        int totalCells = rows * cols;
        int usedCells = totalPairs * 2;
        for (int i = usedCells; i < totalCells; i++) {
            gamePanel.add(new JPanel());
        }
    }

    private class GameButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GameButton clickedButton = (GameButton) e.getSource();

            // 이미 매칭된 버튼은 클릭 불가
            if (clickedButton.isMatched || !clickedButton.isEnabled()) {
                return;
            }

            // 첫 번째 선택인 경우
            if (selectedButton == null) {
                selectedButton = clickedButton;
                selectedButton.setBackground(Color.CYAN);
                return;
            }

            // 같은 버튼을 다시 클릭한 경우 (선택 해제)
            if (selectedButton == clickedButton) {
                selectedButton.setBackground(null);
                selectedButton = null;
                return;
            }

            // 같은 타입(둘 다 단어이거나 둘 다 뜻)을 선택한 경우
            if (selectedButton.isWord == clickedButton.isWord) {
                // 이전 선택 해제하고 새로 선택
                selectedButton.setBackground(null);
                selectedButton = clickedButton;
                selectedButton.setBackground(Color.CYAN);
                return;
            }

            // 두 번째 선택 (단어와 뜻의 조합)
            attempts++;

            // 매칭 확인
            boolean isCorrectMatch = selectedButton.wordData.equals(clickedButton.wordData);

            if (isCorrectMatch) {
                // 정답!
                selectedButton.setBackground(Color.GREEN);
                selectedButton.setEnabled(false);
                selectedButton.isMatched = true;

                clickedButton.setBackground(Color.GREEN);
                clickedButton.setEnabled(false);
                clickedButton.isMatched = true;

                // 통계 업데이트
                selectedButton.wordData.increaseTotal();
                selectedButton.wordData.increaseCorrect();

                matchedPairs++;
                selectedButton = null;

                updateStatus();

                // 게임 완료 확인
                if (matchedPairs == totalPairs) {
                    gameCompleted();
                }
            } else {
                // 오답
                final GameButton currentSelectedButton = selectedButton;
                final GameButton currentClickedButton = clickedButton;

                selectedButton.setBackground(Color.RED);
                clickedButton.setBackground(Color.RED);

                JOptionPane.showMessageDialog(ActionGameGUI.this,
                        "틀렸습니다. 다시 시도해보세요!", "알림", JOptionPane.INFORMATION_MESSAGE);

                // 통계 업데이트
                selectedButton.wordData.increaseTotal();

                selectedButton = null;

                // 1초 후 색상 원래대로
                javax.swing.Timer timer = new javax.swing.Timer(1000, ae -> {
                    if (currentSelectedButton != null && !currentSelectedButton.isMatched) {
                        currentSelectedButton.setBackground(null);
                    }
                    if (currentClickedButton != null && !currentClickedButton.isMatched) {
                        currentClickedButton.setBackground(null);
                    }
                });
                timer.setRepeats(false);
                timer.start();

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
        double accuracy = attempts > 0 ? (double) matchedPairs / attempts * 100 : 0;

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
        gameButtons.clear();

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