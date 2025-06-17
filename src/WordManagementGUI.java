import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

/**
 * 단어 관리를 위한 GUI 클래스
 */
public class WordManagementGUI extends JFrame {
    private WordBook wordBook;
    private JList<WordData> wordList;
    private DefaultListModel<WordData> listModel;
    private JTextField wordField;
    private JTextField meaningField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JLabel statusLabel;

    public WordManagementGUI(WordBook wordBook) {
        this.wordBook = wordBook;
        initializeGUI();
        refreshWordList();
        setVisible(true);
    }

    private void initializeGUI() {
        setTitle("WordPlay - 단어 관리 [" + wordBook.getName() + "]");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 패널 - 단어 입력
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);

        // 중앙 패널 - 단어 목록
        JPanel listPanel = createListPanel();
        add(listPanel, BorderLayout.CENTER);

        // 하단 패널 - 상태 표시
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        setSize(600, 500);
        setLocationRelativeTo(null);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("단어 추가/수정"));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                new TitledBorder("단어 추가/수정")));

        // 입력 필드 패널
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 단어 입력
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        fieldsPanel.add(new JLabel("단어:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        wordField = new JTextField(20);
        fieldsPanel.add(wordField, gbc);

        // 뜻 입력
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        fieldsPanel.add(new JLabel("뜻:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        meaningField = new JTextField(20);
        fieldsPanel.add(meaningField, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout());

        addButton = new JButton("추가");
        addButton.addActionListener(new AddButtonListener());
        buttonPanel.add(addButton);

        updateButton = new JButton("수정");
        updateButton.addActionListener(new UpdateButtonListener());
        updateButton.setEnabled(false);
        buttonPanel.add(updateButton);

        deleteButton = new JButton("삭제");
        deleteButton.addActionListener(new DeleteButtonListener());
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);

        JButton clearButton = new JButton("지우기");
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(clearButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                new TitledBorder("단어 목록")));

        listModel = new DefaultListModel<>();
        wordList = new JList<>(listModel);
        wordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onWordSelected();
            }
        });

        // 커스텀 렌더러로 단어 표시 개선
        wordList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof WordData) {
                    WordData wordData = (WordData) value;
                    setText(String.format("<html><b>%s</b> : %s<br><small>정답률: %.1f%% (출제: %d회)</small></html>",
                            wordData.getWord(), wordData.getMeaning(),
                            wordData.getAccuracy(), wordData.getTotalCount()));
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(wordList);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("단어 개수: 0개");
        panel.add(statusLabel);
        return panel;
    }

    private void onWordSelected() {
        WordData selected = wordList.getSelectedValue();
        if (selected != null) {
            wordField.setText(selected.getWord());
            meaningField.setText(selected.getMeaning());
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
            addButton.setEnabled(false);
        } else {
            clearFields();
        }
    }

    private void clearFields() {
        wordField.setText("");
        meaningField.setText("");
        wordList.clearSelection();
        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void refreshWordList() {
        listModel.clear();
        Collection<WordData> words = wordBook.getAllWordData();
        for (WordData wordData : words) {
            listModel.addElement(wordData);
        }
        statusLabel.setText("단어 개수: " + wordBook.getWordCount() + "개");
    }

    private void showMessage(String message, boolean isError) {
        int messageType = isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        JOptionPane.showMessageDialog(this, message, isError ? "오류" : "알림", messageType);
    }

    // 버튼 리스너 클래스들
    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String word = wordField.getText().trim();
            String meaning = meaningField.getText().trim();

            if (word.isEmpty() || meaning.isEmpty()) {
                showMessage("단어와 뜻을 모두 입력해주세요.", true);
                return;
            }

            if (wordBook.addWord(word, meaning)) {
                showMessage("단어가 추가되었습니다.", false);
                clearFields();
                refreshWordList();
            } else {
                showMessage("단어 추가에 실패했습니다. (중복된 단어이거나 빈 값)", true);
            }
        }
    }

    private class UpdateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            WordData selected = wordList.getSelectedValue();
            if (selected == null) {
                showMessage("수정할 단어를 선택해주세요.", true);
                return;
            }

            String newMeaning = meaningField.getText().trim();
            if (newMeaning.isEmpty()) {
                showMessage("뜻을 입력해주세요.", true);
                return;
            }

            if (wordBook.updateWord(selected.getWord(), newMeaning)) {
                showMessage("단어가 수정되었습니다.", false);
                refreshWordList();
                clearFields();
            } else {
                showMessage("단어 수정에 실패했습니다.", true);
            }
        }
    }

    private class DeleteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            WordData selected = wordList.getSelectedValue();
            if (selected == null) {
                showMessage("삭제할 단어를 선택해주세요.", true);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    WordManagementGUI.this,
                    "단어 '" + selected.getWord() + "'을(를) 정말 삭제하시겠습니까?",
                    "삭제 확인",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (wordBook.removeWord(selected.getWord())) {
                    showMessage("단어가 삭제되었습니다.", false);
                    refreshWordList();
                    clearFields();
                } else {
                    showMessage("단어 삭제에 실패했습니다.", true);
                }
            }
        }
    }

    public static void openWordManagement(WordBook wordBook) {
        if (wordBook == null) {
            JOptionPane.showMessageDialog(null, "단어장을 선택해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            WordManagementGUI gui = new WordManagementGUI(wordBook);
            gui.setVisible(true);
        });
    }
}