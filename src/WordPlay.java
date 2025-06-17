import javax.swing.*;
import java.awt.*;

/**
 * WordPlay 메인 클래스
 */
public class WordPlay extends JFrame {
    private WordBookManager manager;

    public WordPlay() {
        this.manager = new WordBookManager();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("WordPlay - 나만의 단어장 게임");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 타이틀 패널
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.decode("#2E86C1"));
        JLabel titleLabel = new JLabel("WordPlay");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("나만의 단어장 게임");
        subtitleLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.WHITE);

        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(Box.createVerticalStrut(20));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createVerticalStrut(20));

        add(titlePanel, BorderLayout.NORTH);

        // 메인 패널
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 단어장 선택 패널
        JPanel wordBookPanel = createWordBookPanel();
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(wordBookPanel, gbc);

        // 기능 버튼들
        JButton manageWordsBtn = createStyledButton("단어 관리", Color.decode("#28B463"));
        manageWordsBtn.addActionListener(e -> openWordManagement());
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        mainPanel.add(manageWordsBtn, gbc);

        JButton quizBtn = createStyledButton("퀴즈 게임", Color.decode("#E74C3C"));
        quizBtn.addActionListener(e -> startQuizGame());
        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(quizBtn, gbc);

        JButton actionGameBtn = createStyledButton("매칭 게임", Color.decode("#AF7AC5"));
        actionGameBtn.addActionListener(e -> startActionGame());
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(actionGameBtn, gbc);

        JButton statisticsBtn = createStyledButton("학습 통계", Color.decode("#F39C12"));
        statisticsBtn.addActionListener(e -> showStatistics());
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(statisticsBtn, gbc);


        add(mainPanel, BorderLayout.CENTER);

        // 하단 패널
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBackground(Color.decode("#F8F9FA"));

        JButton exitBtn = new JButton("프로그램 종료");
        exitBtn.addActionListener(e -> exitProgram());
        bottomPanel.add(exitBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        setSize(500, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createWordBookPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("단어장 관리"));

        // 단어장 목록
        JPanel listPanel = new JPanel(new FlowLayout());
        JLabel currentLabel = new JLabel("현재 단어장: ");
        JComboBox<String> wordBookCombo = new JComboBox<>();
        updateWordBookCombo(wordBookCombo);

        wordBookCombo.addActionListener(e -> {
            String selected = (String) wordBookCombo.getSelectedItem();
            if (selected != null && !selected.equals("선택하세요")) {
                manager.selectWordBook(selected);
            }
        });

        listPanel.add(currentLabel);
        listPanel.add(wordBookCombo);

        // 단어장 관리 버튼들
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton createBtn = new JButton("생성");
        createBtn.addActionListener(e -> createWordBook(wordBookCombo));

        JButton deleteBtn = new JButton("삭제");
        deleteBtn.addActionListener(e -> deleteWordBook(wordBookCombo));

        JButton renameBtn = new JButton("이름변경");
        renameBtn.addActionListener(e -> renameWordBook(wordBookCombo));

        buttonPanel.add(createBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(renameBtn);

        panel.add(listPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(180, 50));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void updateWordBookCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        combo.addItem("선택하세요");
        for (String name : manager.getAllWordBookNames()) {
            combo.addItem(name);
        }
        if (manager.getCurrentWordBook() != null) {
            combo.setSelectedItem(manager.getCurrentWordBook().getName());
        }
    }

    private void createWordBook(JComboBox<String> combo) {
        String name = JOptionPane.showInputDialog(this, "새 단어장 이름을 입력하세요:");
        if (name != null && !name.trim().isEmpty()) {
            if (manager.createWordBook(name.trim())) {
                updateWordBookCombo(combo);
                JOptionPane.showMessageDialog(this, "단어장이 생성되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "단어장 생성에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteWordBook(JComboBox<String> combo) {
        String selected = (String) combo.getSelectedItem();
        if (selected == null || selected.equals("선택하세요")) {
            JOptionPane.showMessageDialog(this, "삭제할 단어장을 선택하세요.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "'" + selected + "' 단어장을 삭제하시겠습니까?",
                "확인", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            if (manager.deleteWordBook(selected)) {
                updateWordBookCombo(combo);
                JOptionPane.showMessageDialog(this, "단어장이 삭제되었습니다.");
            }
        }
    }

    private void renameWordBook(JComboBox<String> combo) {
        String selected = (String) combo.getSelectedItem();
        if (selected == null || selected.equals("선택하세요")) {
            JOptionPane.showMessageDialog(this, "이름을 변경할 단어장을 선택하세요.");
            return;
        }

        String newName = JOptionPane.showInputDialog(this, "새 이름을 입력하세요:", selected);
        if (newName != null && !newName.trim().isEmpty()) {
            if (manager.renameWordBook(selected, newName.trim())) {
                updateWordBookCombo(combo);
                JOptionPane.showMessageDialog(this, "단어장 이름이 변경되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "이름 변경에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openWordManagement() {
        if (manager.getCurrentWordBook() == null) {
            JOptionPane.showMessageDialog(this, "먼저 단어장을 선택하세요.");
            return;
        }

        new WordManagementGUI(manager.getCurrentWordBook());
    }

    private void startQuizGame() {
        if (manager.getCurrentWordBook() == null) {
            JOptionPane.showMessageDialog(this, "먼저 단어장을 선택하세요.");
            return;
        }

        // 단어 개수 검증 추가
        if (manager.getCurrentWordBook().getWordCount() < 4) {
            JOptionPane.showMessageDialog(this,
                    "퀴즈 게임을 위해서는 최소 4개의 단어가 필요합니다.\n" +
                            "현재 단어 개수: " + manager.getCurrentWordBook().getWordCount() + "개",
                    "단어 부족", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // QuizGameGUI 클래스가 존재하는 경우
            new QuizGameGUI(manager.getCurrentWordBook()).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "퀴즈 게임을 시작할 수 없습니다.\nQuizGameGUI 클래스를 확인하세요.",
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startActionGame() {
        if (manager.getCurrentWordBook() == null) {
            JOptionPane.showMessageDialog(this, "먼저 단어장을 선택하세요.");
            return;
        }

        // 단어 개수 검증 추가
        if (manager.getCurrentWordBook().getWordCount() < 3) {
            JOptionPane.showMessageDialog(this,
                    "매칭 게임을 위해서는 최소 3개의 단어가 필요합니다.\n" +
                            "현재 단어 개수: " + manager.getCurrentWordBook().getWordCount() + "개",
                    "단어 부족", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // ActionGameGUI 클래스가 존재하는 경우
            new ActionGameGUI(manager.getCurrentWordBook()).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "매칭 게임을 시작할 수 없습니다.\nActionGameGUI 클래스를 확인하세요.",
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStatistics() {
        if (manager.getCurrentWordBook() == null) {
            JOptionPane.showMessageDialog(this, "먼저 단어장을 선택하세요.");
            return;
        }

        new StatisticsGUI(manager.getCurrentWordBook());
    }



    private void exitProgram() {
        manager.saveData();
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new WordPlay());
    }
}