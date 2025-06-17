import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 학습 통계를 표시하는 GUI 클래스
 */
public class StatisticsGUI extends JFrame {
    private WordBook wordBook;
    private JTable statisticsTable;
    private DefaultTableModel tableModel;
    private JLabel totalWordsLabel;
    private JLabel averageAccuracyLabel;
    private JLabel totalQuestionsLabel;
    private JTable weakWordsTable;
    private DefaultTableModel weakWordsModel;
    private JButton refreshButton;
    private JButton studyWeakWordsButton;
    private JButton selectAllButton;
    private JButton deselectAllButton;

    public StatisticsGUI(WordBook wordBook) {
        this.wordBook = wordBook;
        initializeGUI();
        loadStatistics();
        setVisible(true);
    }

    private void initializeGUI() {
        setTitle("WordPlay - 학습 통계 [" + wordBook.getName() + "]");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 패널 - 전체 통계
        JPanel summaryPanel = createSummaryPanel();
        add(summaryPanel, BorderLayout.NORTH);

        // 중앙 패널 - 상세 통계 테이블
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // 하단 패널 - 버튼들
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                new TitledBorder("전체 통계")));

        // 총 단어 수
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel totalLabel = new JLabel("총 단어 수", JLabel.CENTER);
        totalLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        totalWordsLabel = new JLabel("0개", JLabel.CENTER);
        totalWordsLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        totalWordsLabel.setForeground(Color.BLUE);
        totalPanel.add(totalLabel, BorderLayout.NORTH);
        totalPanel.add(totalWordsLabel, BorderLayout.CENTER);

        // 평균 정답률
        JPanel avgPanel = new JPanel(new BorderLayout());
        avgPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel avgLabel = new JLabel("평균 정답률", JLabel.CENTER);
        avgLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        averageAccuracyLabel = new JLabel("0.0%", JLabel.CENTER);
        averageAccuracyLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        averageAccuracyLabel.setForeground(Color.GREEN);
        avgPanel.add(avgLabel, BorderLayout.NORTH);
        avgPanel.add(averageAccuracyLabel, BorderLayout.CENTER);

        // 총 출제 횟수
        JPanel questionsPanel = new JPanel(new BorderLayout());
        questionsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel questionsLabel = new JLabel("총 출제 횟수", JLabel.CENTER);
        questionsLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        totalQuestionsLabel = new JLabel("0회", JLabel.CENTER);
        totalQuestionsLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 18));
        totalQuestionsLabel.setForeground(Color.ORANGE);
        questionsPanel.add(questionsLabel, BorderLayout.NORTH);
        questionsPanel.add(totalQuestionsLabel, BorderLayout.CENTER);

        panel.add(totalPanel);
        panel.add(avgPanel);
        panel.add(questionsPanel);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 탭 패널 생성
        JTabbedPane tabbedPane = new JTabbedPane();

        // 전체 통계 탭
        JPanel allStatsPanel = createAllStatisticsPanel();
        tabbedPane.addTab("전체 단어 통계", allStatsPanel);

        // 취약 단어 탭
        JPanel weakWordsPanel = createWeakWordsPanel();
        tabbedPane.addTab("취약 단어 (정답률 50% 미만)", weakWordsPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAllStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 테이블 모델 설정
        String[] columnNames = {"단어", "뜻", "출제 횟수", "정답 횟수", "정답률(%)", "상태"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 편집 불가
            }
        };

        statisticsTable = new JTable(tableModel);
        statisticsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 테이블 컬럼 너비 설정
        statisticsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // 단어
        statisticsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 뜻
        statisticsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 출제 횟수
        statisticsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 정답 횟수
        statisticsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 정답률
        statisticsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 상태

        // 정렬 기능 추가
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        statisticsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(statisticsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createWeakWordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 설명 라벨
        JLabel descLabel = new JLabel("정답률이 50% 미만인 단어들입니다. 체크박스를 선택하여 집중적으로 학습해보세요!");
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(descLabel, BorderLayout.NORTH);

        // 취약 단어 테이블 (체크박스 포함)
        String[] weakColumnNames = {"선택", "단어", "뜻", "출제 횟수", "정답 횟수", "정답률(%)"};
        weakWordsModel = new DefaultTableModel(weakColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 첫 번째 컬럼(체크박스)만 편집 가능
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class; // 체크박스
                }
                return String.class;
            }
        };

        weakWordsTable = new JTable(weakWordsModel);
        weakWordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 컬럼 너비 설정
        weakWordsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // 체크박스
        weakWordsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 단어
        weakWordsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // 뜻
        weakWordsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 출제 횟수
        weakWordsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 정답 횟수
        weakWordsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 정답률

        JScrollPane weakScrollPane = new JScrollPane(weakWordsTable);
        panel.add(weakScrollPane, BorderLayout.CENTER);

        // 취약 단어 관련 버튼들
        JPanel weakButtonPanel = new JPanel(new FlowLayout());

        selectAllButton = new JButton("전체 선택");
        selectAllButton.addActionListener(e -> selectAllWeakWords(true));
        weakButtonPanel.add(selectAllButton);

        deselectAllButton = new JButton("전체 해제");
        deselectAllButton.addActionListener(e -> selectAllWeakWords(false));
        weakButtonPanel.add(deselectAllButton);

        studyWeakWordsButton = new JButton("선택한 취약 단어로 퀴즈하기");
        studyWeakWordsButton.addActionListener(new StudyWeakWordsListener());
        weakButtonPanel.add(studyWeakWordsButton);

        panel.add(weakButtonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void selectAllWeakWords(boolean select) {
        for (int i = 0; i < weakWordsModel.getRowCount(); i++) {
            weakWordsModel.setValueAt(select, i, 0);
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        refreshButton = new JButton("새로고침");
        refreshButton.addActionListener(e -> loadStatistics());
        panel.add(refreshButton);

        JButton exportButton = new JButton("통계 내보내기");
        exportButton.addActionListener(new ExportStatisticsListener());
        panel.add(exportButton);

        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);

        return panel;
    }

    private void loadStatistics() {
        // 전체 통계 계산
        Collection<WordData> allWords = wordBook.getAllWordData();
        int totalWords = allWords.size();
        int totalQuestions = 0;
        int totalCorrect = 0;

        // 테이블 데이터 초기화
        tableModel.setRowCount(0);
        weakWordsModel.setRowCount(0);

        for (WordData wordData : allWords) {
            totalQuestions += wordData.getTotalCount();
            totalCorrect += wordData.getCorrectCount();

            // 전체 통계 테이블에 추가
            String status = getWordStatus(wordData);
            Object[] row = {
                    wordData.getWord(),
                    wordData.getMeaning(),
                    wordData.getTotalCount(),
                    wordData.getCorrectCount(),
                    String.format("%.1f", wordData.getAccuracy()),
                    status
            };
            tableModel.addRow(row);

            // 취약 단어 테이블에 추가 (정답률 50% 미만)
            if (wordData.getTotalCount() > 0 && wordData.getAccuracy() < 50.0) {
                Object[] weakRow = {
                        false, // 기본적으로 체크박스는 선택되지 않음
                        wordData.getWord(),
                        wordData.getMeaning(),
                        wordData.getTotalCount(),
                        wordData.getCorrectCount(),
                        String.format("%.1f", wordData.getAccuracy())
                };
                weakWordsModel.addRow(weakRow);
            }
        }

        // 전체 통계 업데이트
        totalWordsLabel.setText(totalWords + "개");

        double averageAccuracy = totalQuestions > 0 ? (double) totalCorrect / totalQuestions * 100 : 0.0;
        averageAccuracyLabel.setText(String.format("%.1f%%", averageAccuracy));

        totalQuestionsLabel.setText(totalQuestions + "회");

        // 정답률에 따른 색상 변경
        if (averageAccuracy >= 80) {
            averageAccuracyLabel.setForeground(Color.GREEN);
        } else if (averageAccuracy >= 60) {
            averageAccuracyLabel.setForeground(Color.ORANGE);
        } else {
            averageAccuracyLabel.setForeground(Color.RED);
        }
    }

    private String getWordStatus(WordData wordData) {
        if (wordData.getTotalCount() == 0) {
            return "미출제";
        }

        double accuracy = wordData.getAccuracy();
        if (accuracy >= 80) {
            return "우수";
        } else if (accuracy >= 60) {
            return "보통";
        } else {
            return "취약";
        }
    }

    // 취약 단어 학습 리스너
    private class StudyWeakWordsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 체크된 단어들 찾기
            List<Integer> selectedIndexes = new ArrayList<>();
            for (int i = 0; i < weakWordsModel.getRowCount(); i++) {
                Boolean isSelected = (Boolean) weakWordsModel.getValueAt(i, 0);
                if (isSelected != null && isSelected) {
                    selectedIndexes.add(i);
                }
            }

            if (selectedIndexes.isEmpty()) {
                JOptionPane.showMessageDialog(StatisticsGUI.this,
                        "학습할 취약 단어를 체크박스로 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 선택된 취약 단어들로 임시 단어장 생성
            WordBook tempWordBook = new WordBook("취약 단어 집중 학습");
            for (int index : selectedIndexes) {
                String word = (String) weakWordsModel.getValueAt(index, 1);
                String meaning = (String) weakWordsModel.getValueAt(index, 2);
                tempWordBook.addWord(word, meaning);
            }

            // 퀴즈 시작
            QuizGameGUI.startQuiz(tempWordBook);
        }
    }

    // 통계 내보내기 리스너
    private class ExportStatisticsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(wordBook.getName()).append(" 학습 통계 ===\n\n");

            // 전체 통계
            sb.append("총 단어 수: ").append(totalWordsLabel.getText()).append("\n");
            sb.append("평균 정답률: ").append(averageAccuracyLabel.getText()).append("\n");
            sb.append("총 출제 횟수: ").append(totalQuestionsLabel.getText()).append("\n\n");

            // 단어별 상세 통계
            sb.append("=== 단어별 상세 통계 ===\n");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                sb.append(String.format("%s : %s (출제: %s회, 정답: %s회, 정답률: %s%%, 상태: %s)\n",
                        tableModel.getValueAt(i, 0), tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2), tableModel.getValueAt(i, 3),
                        tableModel.getValueAt(i, 4), tableModel.getValueAt(i, 5)));
            }

            // 텍스트 영역에 표시
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(StatisticsGUI.this, scrollPane,
                    "통계 내보내기", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void openStatistics(WordBook wordBook) {
        if (wordBook == null) {
            JOptionPane.showMessageDialog(null, "단어장을 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (wordBook.getWordCount() == 0) {
            JOptionPane.showMessageDialog(null, "단어장에 단어가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            StatisticsGUI gui = new StatisticsGUI(wordBook);
            gui.setVisible(true);
        });
    }
}