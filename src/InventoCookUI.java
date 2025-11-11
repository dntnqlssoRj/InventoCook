import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class InventoCookUI {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel badgeLabel;
    private JSpinner quantitySpinner;
    private JButton quantityApplyButton;

    private static final Color COLOR_DDAY_SAFE = new Color(230, 248, 230);
    private static final Color COLOR_DDAY_WARNING = new Color(255, 245, 230);
    private static final Color COLOR_DDAY_EXPIRED = new Color(255, 230, 230);

    // 메인 영역 카드 레이아웃 (홈 / 재고관리 / 알림 / 긴급추천)
    private JPanel mainContainer;
    private CardLayout cardLayout;

    private static final String CARD_HOME = "home";
    private static final String CARD_INVENTORY = "inventory";
    private static final String CARD_ALERT = "alert";
    private static final String CARD_EMERGENCY = "emergency";

    public InventoCookUI() {
        initUI();
    }

    private void initUI() {
        // Frame
        frame = new JFrame("InventoCook");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 640);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Top header
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(12, 16, 12, 16));
        top.setBackground(Color.WHITE);
        JLabel title = new JLabel(
                "<html><span style='font-size:18pt;font-weight:600;'>InventoCook</span><br>" +
                        "<span style='font-size:9pt;color:#666;'>냉장고 재고로 요리하는 스마트 인벤토리</span></html>"
        );
        title.setOpaque(false);

        // 오른쪽 상단 뱃지
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTop.setOpaque(false);
        badgeLabel = new JLabel("\u26A0 2");
        badgeLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(6, 8, 6, 8)
        ));
        rightTop.add(badgeLabel);
        top.add(title, BorderLayout.WEST);
        top.add(rightTop, BorderLayout.EAST);

        frame.add(top, BorderLayout.NORTH);

        // Main split: sidebar | main area
        JSplitPane split = new JSplitPane();
        split.setDividerLocation(230);
        split.setDividerSize(1);
        split.setContinuousLayout(true);

        // Sidebar (홈 + 설정만)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new EmptyBorder(16, 12, 16, 12));

        sidebar.add(menuButton("🏠  홈", true, CARD_HOME));   // 홈만 페이지 전환
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(menuButton("⚙  설정", false));            // 설정은 남겨두되 아직 페이지 연결 X
        sidebar.add(Box.createVerticalGlue());

        split.setLeftComponent(sidebar);

        // 메인 카드 컨테이너
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(Color.WHITE);

        JPanel homePanel = createHomePanel();
        JPanel inventoryPanel = createInventoryPanel();
        JPanel alertPanel = createAlertPanel();
        JPanel emergencyPanel = createEmergencyPanel();

        mainContainer.add(homePanel, CARD_HOME);
        mainContainer.add(inventoryPanel, CARD_INVENTORY);
        mainContainer.add(alertPanel, CARD_ALERT);
        mainContainer.add(emergencyPanel, CARD_EMERGENCY);

        // 기본은 홈 화면
        cardLayout.show(mainContainer, CARD_HOME);

        split.setRightComponent(mainContainer);
        frame.add(split, BorderLayout.CENTER);

        frame.getContentPane().setBackground(Color.WHITE);
        frame.setVisible(true);
    }

    // 홈 화면
    private JPanel createHomePanel() {
        JPanel home = new JPanel(new BorderLayout());
        home.setBorder(new EmptyBorder(12, 16, 12, 16));
        home.setBackground(Color.WHITE);

        JLabel title = new JLabel(
                "<html><span style='font-size:16pt;font-weight:600;'>InventoCook 홈</span><br>" +
                        "<span style='font-size:10pt;color:#666;'>냉장고 재고로 요리하는 스마트 인벤토리</span></html>"
        );
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        home.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new GridLayout(1, 3, 12, 0));

        // 각 카드 클릭 시 해당 페이지로 이동
        cards.add(makeHomeCard("📦 인벤토리", "보유 중인 재료를 한눈에 관리", CARD_INVENTORY));
        cards.add(makeHomeCard("⏰ 유통기한 임박", "곧 상할 재료를 먼저 확인", CARD_ALERT));
        cards.add(makeHomeCard("🔥 긴급 추천 메뉴", "임박 재료로 만들 수 있는 레시피", CARD_EMERGENCY));

        home.add(cards, BorderLayout.CENTER);
        return home;
    }

    // 홈 카드 + 클릭 시 카드 전환
    private JPanel makeHomeCard(String title, String desc, String cardName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setBackground(new Color(248, 248, 248));

        JLabel t = new JLabel(
                "<html><span style='font-size:12pt;font-weight:600;white-space:nowrap;'>"
                        + title +
                        "</span></html>"
        );
        JLabel d = new JLabel(
                "<html><span style='font-size:9pt;color:#666;'>" +
                        desc +
                        "</span></html>"
        );
        t.setBorder(new EmptyBorder(0, 0, 4, 0));

        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);

        // 카드 클릭하면 해당 페이지로 전환
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (cardLayout != null && mainContainer != null && cardName != null) {
                    cardLayout.show(mainContainer, cardName);
                }
            }
        });

        return card;
    }

    // 재고관리 화면
    private JPanel createInventoryPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(12, 16, 12, 16));
        main.setBackground(Color.WHITE);

        JLabel sectionTitle =
                new JLabel("<html><span style='font-size:12pt;font-weight:600;'>식재료 인벤토리</span></html>");
        sectionTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        // 상단 검색/필터 바 + 액션 버튼
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topBar.setOpaque(false);
        JTextField searchField = new JTextField(12);
        JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"전체", "야채", "육류", "유제품", "기타"});
        JComboBox<String> locationFilter = new JComboBox<>(new String[]{"전체", "냉장", "냉동", "실온"});
        JComboBox<String> sortFilter = new JComboBox<>(new String[]{"정렬 없음", "유통기한", "이름", "카테고리"});
        topBar.add(new JLabel("검색:"));
        topBar.add(searchField);
        topBar.add(new JLabel("카테고리:"));
        topBar.add(categoryFilter);
        topBar.add(new JLabel("보관 위치:"));
        topBar.add(locationFilter);
        topBar.add(new JLabel("정렬:"));
        topBar.add(sortFilter);
        topBar.add(Box.createHorizontalStrut(8));

        JButton addButton = new JButton("재료 추가");
        styleFlatButton(addButton);
        addButton.addActionListener(e -> onAdd());
        topBar.add(addButton);

        JButton editButton = new JButton("선택 수정");
        styleFlatButton(editButton);
        editButton.addActionListener(e -> onEdit());
        topBar.add(editButton);

        JButton deleteButton = new JButton("선택 삭제");
        styleFlatButton(deleteButton);
        deleteButton.addActionListener(e -> onDelete());
        topBar.add(deleteButton);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionTitle, BorderLayout.NORTH);
        header.add(topBar, BorderLayout.CENTER);
        main.add(header, BorderLayout.NORTH);

        // 테이블
        String[] columns = {"상태", "재료명", "카테고리", "보관 위치", "수량", "D-Day", "유통기한"};
        Object[][] sample = {
                {"✅", "계란", "냉장", "냉장", 12, "D-3", "2025-10-28"},
                {"⚠️", "우유", "유제품", "냉장", 1, "D-1", "2025-10-30"},
                {"🚫", "두부", "냉장", "냉장", 0, "D+2", "2025-10-26"}
        };
        tableModel = new DefaultTableModel(sample, columns) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                try {
                    if (row >= 0 && row < getRowCount()) {
                        // D-Day 컬럼(인덱스 5)에서 값을 가져와서 전체 행에 색상 적용
                        Object value = getValueAt(row, 5); // D-Day 컬럼
                        String dday = (value != null) ? value.toString() : "";
                        Color bgColor = resolveDDayColor(dday);
                        c.setBackground(bgColor);
                        c.setForeground(Color.BLACK);
                    }
                } catch (Exception e) {
                    // 렌더링 오류 시 기본 배경색 사용
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setSelectionBackground(new Color(235, 245, 255));
        table.setSelectionForeground(Color.BLACK);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncQuantityEditorState();
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));

        // RowSorter + 필터 로직
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        Runnable apply = () -> applyFilters(sorter, searchField, categoryFilter, locationFilter);

        // 검색창 입력 시 필터 적용
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                apply.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                apply.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                apply.run();
            }
        });

        // 카테고리 / 보관 위치 변경 시 필터 적용
        categoryFilter.addActionListener(e -> apply.run());
        locationFilter.addActionListener(e -> apply.run());

        // 정렬 콤보박스 동작: 유통기한 / 이름 / 카테고리 기준 정렬
        sortFilter.addActionListener(e -> {
            String opt = (String) sortFilter.getSelectedItem();
            if (opt == null || "정렬 없음".equals(opt)) {
                // 정렬 없음 선택 시 기존 정렬 해제
                sorter.setSortKeys(null);
                return;
            }

            int columnIndex;
            switch (opt) {
                case "유통기한":
                    // 모델 컬럼 인덱스 6 = 유통기한
                    columnIndex = 6;
                    break;
                case "이름":
                    // 모델 컬럼 인덱스 1 = 재료명
                    columnIndex = 1;
                    break;
                case "카테고리":
                    // 모델 컬럼 인덱스 2 = 카테고리
                    columnIndex = 2;
                    break;
                default:
                    sorter.setSortKeys(null);
                    return;
            }

            List<RowSorter.SortKey> keys =
                    Collections.singletonList(new RowSorter.SortKey(columnIndex, SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
        });

        main.add(scroll, BorderLayout.CENTER);

        // 하단 상태바
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottomBar.setOpaque(false);
        JLabel summaryLabel = new JLabel("총 3개 재료 | 임박 1개 | 경과 1개");
        bottomBar.add(summaryLabel);
        bottomBar.add(new JLabel(" | 수량 조정:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        Dimension spinnerSize = quantitySpinner.getPreferredSize();
        spinnerSize.width = 60;
        quantitySpinner.setPreferredSize(spinnerSize);
        quantitySpinner.setEnabled(false);
        bottomBar.add(quantitySpinner);
        quantityApplyButton = new JButton("적용");
        styleFlatButton(quantityApplyButton);
        quantityApplyButton.setEnabled(false);
        quantityApplyButton.addActionListener(e -> applyQuantityChange());
        bottomBar.add(quantityApplyButton);
        main.add(bottomBar, BorderLayout.SOUTH);

        syncQuantityEditorState();
        return main;
    }

    // 인벤토리 검색/필터 공통 적용
    private void applyFilters(TableRowSorter<DefaultTableModel> sorter,
                              JTextField searchField,
                              JComboBox<String> categoryFilter,
                              JComboBox<String> locationFilter) {
        String text = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String category = (String) categoryFilter.getSelectedItem();
        String location = (String) locationFilter.getSelectedItem();

        RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String nameVal = entry.getStringValue(1);   // 재료명
                String catVal = entry.getStringValue(2);    // 카테고리
                String locVal = entry.getStringValue(3);    // 보관 위치

                if (!text.isEmpty() && (nameVal == null || !nameVal.toLowerCase().contains(text))) {
                    return false;
                }
                if (category != null && !"전체".equals(category) && (catVal == null || !catVal.equals(category))) {
                    return false;
                }
                if (location != null && !"전체".equals(location) && (locVal == null || !locVal.equals(location))) {
                    return false;
                }
                return true;
            }
        };

        sorter.setRowFilter(filter);
    }

    // 유통기한 알림 화면(임시)
    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setBackground(Color.WHITE);

        JLabel title =
                new JLabel("<html><span style='font-size:12pt;font-weight:600;'>유통기한 임박 알림</span></html>");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("유통기한이 가까운 재료 목록을 여기에 표시할 예정입니다.");
        placeholder.setForeground(new Color(120, 120, 120));
        panel.add(placeholder, BorderLayout.CENTER);

        return panel;
    }

    // 긴급 추천 메뉴 화면(임시)
    private JPanel createEmergencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setBackground(Color.WHITE);

        JLabel title =
                new JLabel("<html><span style='font-size:12pt;font-weight:600;'>긴급 추천 메뉴</span></html>");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("임박 재료로 만들 수 있는 레시피를 여기에 표시할 예정입니다.");
        placeholder.setForeground(new Color(120, 120, 120));
        panel.add(placeholder, BorderLayout.CENTER);

        return panel;
    }

    // 사이드바 메뉴 버튼
    private JPanel menuButton(String text, boolean selected) {
        return menuButton(text, selected, null);
    }

    private JPanel menuButton(String text, boolean selected, String cardName) {
        JPanel p = new JPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setBorder(new EmptyBorder(6, 8, 6, 8));
        p.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Dialog", Font.PLAIN, 14));
        if (selected) {
            lbl.setForeground(new Color(20, 20, 20));
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(230, 230, 230)),
                    new EmptyBorder(6, 10, 6, 6)
            ));
        } else {
            lbl.setForeground(new Color(90, 90, 90));
        }
        p.add(lbl, BorderLayout.WEST);

        if (cardName != null) {
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (cardLayout != null && mainContainer != null) {
                        cardLayout.show(mainContainer, cardName);
                    }
                }
            });
        }

        return p;
    }

    // 버튼 스타일
    private void styleFlatButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private Color resolveDDayColor(String dday) {
        if (dday == null) return Color.WHITE;
        String normalized = dday.trim().toUpperCase();
        if (normalized.startsWith("D+")) return COLOR_DDAY_EXPIRED;
        if (normalized.startsWith("D-")) {
            try {
                int days = Integer.parseInt(normalized.substring(2));
                return (days <= 2) ? COLOR_DDAY_WARNING : COLOR_DDAY_SAFE;
            } catch (NumberFormatException ignored) {
                return Color.WHITE;
            }
        }
        if ("D".equals(normalized) || "D0".equals(normalized) || "D-0".equals(normalized)) {
            return COLOR_DDAY_WARNING;
        }
        return Color.WHITE;
    }

    private void applyQuantityChange() {
        if (quantitySpinner == null || table == null) return;
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(frame, "수정할 재료를 선택하세요.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object value = quantitySpinner.getValue();
        int qty = (value instanceof Number) ? ((Number) value).intValue() : 0;
        tableModel.setValueAt(qty, modelRow, 4);
    }

    private void syncQuantityEditorState() {
        if (quantitySpinner == null || quantityApplyButton == null || table == null) return;
        int viewRow = table.getSelectedRow();
        boolean hasSelection = viewRow >= 0;
        quantitySpinner.setEnabled(hasSelection);
        quantityApplyButton.setEnabled(hasSelection);
        if (hasSelection) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object current = tableModel.getValueAt(modelRow, 4);
            int qty = 0;
            if (current instanceof Number) {
                qty = ((Number) current).intValue();
            } else if (current != null) {
                try {
                    qty = Integer.parseInt(String.valueOf(current));
                } catch (NumberFormatException ignored) {
                    qty = 0;
                }
            }
            quantitySpinner.setValue(qty);
        } else {
            quantitySpinner.setValue(0);
        }
    }

    // CRUD: 추가
    private void onAdd() {
        if (tableModel == null || frame == null) {
            JOptionPane.showMessageDialog(frame, "테이블이 초기화되지 않았습니다.");
            return;
        }

        try {
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
            form.setBorder(new EmptyBorder(10, 10, 10, 10));

            JComboBox<String> statusField = new JComboBox<>(new String[]{"✅", "⚠️", "🚫"});
            JTextField nameField = new JTextField(20);
            JTextField categoryField = new JTextField(20);
            JComboBox<String> locationField = new JComboBox<>(new String[]{"냉장", "냉동", "실온"});
            JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
            JTextField ddayField = new JTextField("D-0", 10);
            JTextField expField = new JTextField("2025-10-31", 15);

            form.add(new JLabel("상태:"));
            form.add(statusField);
            form.add(new JLabel("재료명:"));
            form.add(nameField);
            form.add(new JLabel("카테고리:"));
            form.add(categoryField);
            form.add(new JLabel("보관 위치:"));
            form.add(locationField);
            form.add(new JLabel("수량:"));
            form.add(qtySpinner);
            form.add(new JLabel("D-Day (예: D-3):"));
            form.add(ddayField);
            form.add(new JLabel("유통기한 (YYYY-MM-DD):"));
            form.add(expField);

            // 스크롤 가능한 패널로 감싸기
            JScrollPane scrollPane = new JScrollPane(form);
            scrollPane.setPreferredSize(new Dimension(400, 250));

            int res = JOptionPane.showConfirmDialog(
                    frame, scrollPane, "재료 추가",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (res == JOptionPane.OK_OPTION) {
                // 입력 검증
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "재료명을 입력해주세요.");
                    return;
                }

                int qty = ((Number) qtySpinner.getValue()).intValue();
                String dday = ddayField.getText().trim();
                String exp = expField.getText().trim();

                tableModel.addRow(new Object[]{
                        statusField.getSelectedItem(),
                        name,
                        categoryField.getText().trim(),
                        locationField.getSelectedItem(),
                        qty,
                        dday.isEmpty() ? "D-0" : dday,
                        exp.isEmpty() ? "2025-10-31" : exp
                });

                // 테이블 새로고침
                tableModel.fireTableDataChanged();
                if (table != null) {
                    table.repaint();
                }
                refreshBadge();
                JOptionPane.showMessageDialog(frame, "재료가 추가되었습니다.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "오류가 발생했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // CRUD: 수정
    private void onEdit() {
        if (table == null || tableModel == null || frame == null) {
            JOptionPane.showMessageDialog(frame, "테이블이 초기화되지 않았습니다.");
            return;
        }

        try {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(frame, "편집할 항목을 선택하세요.",
                        "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int r = table.convertRowIndexToModel(viewRow);
            if (r < 0 || r >= tableModel.getRowCount()) {
                JOptionPane.showMessageDialog(frame, "유효하지 않은 행입니다.");
                return;
            }

            String curStatus = String.valueOf(tableModel.getValueAt(r, 0));
            String curName = String.valueOf(tableModel.getValueAt(r, 1));
            String curCat = String.valueOf(tableModel.getValueAt(r, 2));
            String curLoc = String.valueOf(tableModel.getValueAt(r, 3));
            Object curQtyObj = tableModel.getValueAt(r, 4);
            int curQty = 0;
            if (curQtyObj instanceof Number) {
                curQty = ((Number) curQtyObj).intValue();
            } else if (curQtyObj != null) {
                try {
                    curQty = Integer.parseInt(curQtyObj.toString());
                } catch (NumberFormatException ignored) {
                    curQty = 0;
                }
            }
            String curDday = String.valueOf(tableModel.getValueAt(r, 5));
            String curExp = String.valueOf(tableModel.getValueAt(r, 6));

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
            form.setBorder(new EmptyBorder(10, 10, 10, 10));

            JComboBox<String> statusField = new JComboBox<>(new String[]{"✅", "⚠️", "🚫"});
            statusField.setSelectedItem(curStatus);
            JTextField nameField = new JTextField(curName, 20);
            JTextField categoryField = new JTextField(curCat, 20);
            JComboBox<String> locationField = new JComboBox<>(new String[]{"냉장", "냉동", "실온"});
            locationField.setSelectedItem(curLoc);
            JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(curQty, 0, 9999, 1));
            JTextField ddayField = new JTextField(curDday, 10);
            JTextField expField = new JTextField(curExp, 15);

            form.add(new JLabel("상태:"));
            form.add(statusField);
            form.add(new JLabel("재료명:"));
            form.add(nameField);
            form.add(new JLabel("카테고리:"));
            form.add(categoryField);
            form.add(new JLabel("보관 위치:"));
            form.add(locationField);
            form.add(new JLabel("수량:"));
            form.add(qtySpinner);
            form.add(new JLabel("D-Day (예: D-3):"));
            form.add(ddayField);
            form.add(new JLabel("유통기한 (YYYY-MM-DD):"));
            form.add(expField);

            // 스크롤 가능한 패널로 감싸기
            JScrollPane scrollPane = new JScrollPane(form);
            scrollPane.setPreferredSize(new Dimension(400, 250));

            int res = JOptionPane.showConfirmDialog(
                    frame, scrollPane, "재료 수정",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (res == JOptionPane.OK_OPTION) {
                // 입력 검증
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "재료명을 입력해주세요.");
                    return;
                }

                int qty = ((Number) qtySpinner.getValue()).intValue();
                String dday = ddayField.getText().trim();
                String exp = expField.getText().trim();

                tableModel.setValueAt(statusField.getSelectedItem(), r, 0);
                tableModel.setValueAt(name, r, 1);
                tableModel.setValueAt(categoryField.getText().trim(), r, 2);
                tableModel.setValueAt(locationField.getSelectedItem(), r, 3);
                tableModel.setValueAt(qty, r, 4);
                tableModel.setValueAt(dday.isEmpty() ? "D-0" : dday, r, 5);
                tableModel.setValueAt(exp.isEmpty() ? "2025-10-31" : exp, r, 6);

                // 테이블 새로고침
                tableModel.fireTableDataChanged();
                if (table != null) {
                    table.repaint();
                }
                refreshBadge();
                syncQuantityEditorState();
                JOptionPane.showMessageDialog(frame, "재료가 수정되었습니다.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "수정 중 오류가 발생했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // CRUD: 삭제
    private void onDelete() {
        if (table == null || tableModel == null || frame == null) {
            JOptionPane.showMessageDialog(frame, "테이블이 초기화되지 않았습니다.");
            return;
        }

        try {
            int[] viewRows = table.getSelectedRows();
            if (viewRows.length == 0) {
                JOptionPane.showMessageDialog(frame, "삭제할 항목을 선택하세요.",
                        "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String message = (viewRows.length == 1)
                    ? "선택한 재료를 삭제하겠습니까?"
                    : viewRows.length + "개의 재료를 삭제하겠습니까?";

            int ok = JOptionPane.showConfirmDialog(
                    frame, message,
                    "삭제 확인", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );

            if (ok == JOptionPane.YES_OPTION) {
                // view index를 model index로 변환
                int[] modelRows = new int[viewRows.length];
                for (int i = 0; i < viewRows.length; i++) {
                    modelRows[i] = table.convertRowIndexToModel(viewRows[i]);
                }

                // 내림차순으로 정렬 (뒤에서부터 삭제하여 인덱스 문제 방지)
                Arrays.sort(modelRows);

                // 삭제 실행
                for (int i = modelRows.length - 1; i >= 0; i--) {
                    if (modelRows[i] >= 0 && modelRows[i] < tableModel.getRowCount()) {
                        tableModel.removeRow(modelRows[i]);
                    }
                }

                // 테이블 새로고침
                tableModel.fireTableDataChanged();
                if (table != null) {
                    table.clearSelection();
                    table.repaint();
                }
                refreshBadge();
                syncQuantityEditorState();

                JOptionPane.showMessageDialog(frame,
                        viewRows.length + "개의 재료가 삭제되었습니다.",
                        "완료", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "삭제 중 오류가 발생했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // 뱃지 갱신 (데모용)
    private void refreshBadge() {
        if (tableModel == null || badgeLabel == null) return;
        try {
            int count = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object value = tableModel.getValueAt(i, 6); // 유통기한 컬럼
                if (value != null) {
                    String exp = value.toString();
                    if (exp.contains("2025-10")) count++;
                }
            }
            badgeLabel.setText("\u26A0 " + count);
        } catch (Exception e) {
            // 오류 발생 시 기본값 설정
            badgeLabel.setText("\u26A0 0");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(InventoCookUI::new);
    }
}