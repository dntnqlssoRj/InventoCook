import inventocook.model.Recipe;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableColumnModel;
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
import java.util.Deque;
import java.util.ArrayDeque;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoCookUI {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel badgeLabel;
    private JSpinner quantitySpinner;
    private JButton quantityApplyButton;

    // 임박(near-expiry) 기능용
    private DefaultTableModel alertModel;
    private JTable alertTable;
    private static final int IMMINENT_DAYS = 3; // D-3 이하면 임박으로 간주

    private static final Color COLOR_DDAY_SAFE = new Color(230, 248, 230);
    private static final Color COLOR_DDAY_WARNING = new Color(255, 245, 230);
    private static final Color COLOR_DDAY_EXPIRED = new Color(255, 230, 230);

    // 날짜 포맷터 (YYYY-MM-DD)
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 메인 영역 카드 레이아웃 (홈 / 재고관리 / 알림 / 긴급추천)
    private JPanel mainContainer;
    private CardLayout cardLayout;
    private String currentCard = CARD_HOME;
    private Deque<String> navStack = new ArrayDeque<>();

    private static final String CARD_HOME = "home";
    private static final String CARD_INVENTORY = "inventory";
    private static final String CARD_ALERT = "alert";
    private static final String CARD_EMERGENCY = "emergency";

    private DefaultTableModel recipeModel;
    private JTable recipeTable;
    private JLabel topRecipeLabel;

    private List<Recipe> RECIPE_DB = new ArrayList<>();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventocook?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER =  "root"; //"본인계정"
    private static final String DB_PASS =  "wjdgns2003@"; //"본인비밀번호"

    public InventoCookUI() {
        // 레시피를 MySQL DB에서 먼저 로드
        loadRecipesFromDb();
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
                    showCard(cardName);
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

        // 상단 검색/필터 바 + 액션 버튼 (작은 화면 대응: 2줄 레이아웃)
        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));

        // 1줄차: 검색/필터
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterRow.setOpaque(false);
        JTextField searchField = new JTextField(12);
        JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"전체", "야채", "육류", "유제품", "기타"});
        JComboBox<String> locationFilter = new JComboBox<>(new String[]{"전체", "냉장", "냉동", "실온"});
        JComboBox<String> sortFilter = new JComboBox<>(new String[]{"정렬 없음", "유통기한", "이름", "카테고리"});

        filterRow.add(new JLabel("검색:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("카테고리:"));
        filterRow.add(categoryFilter);
        filterRow.add(new JLabel("보관 위치:"));
        filterRow.add(locationFilter);
        filterRow.add(new JLabel("정렬:"));
        filterRow.add(sortFilter);

        // 2줄차: 액션 버튼
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttonRow.setOpaque(false);

        JButton addButton = new JButton("재료 추가");
        styleFlatButton(addButton);
        addButton.addActionListener(e -> onAdd());
        buttonRow.add(addButton);

        JButton editButton = new JButton("선택 수정");
        styleFlatButton(editButton);
        editButton.addActionListener(e -> onEdit());
        buttonRow.add(editButton);

        JButton deleteButton = new JButton("선택 삭제");
        styleFlatButton(deleteButton);
        deleteButton.addActionListener(e -> onDelete());
        buttonRow.add(deleteButton);

        // 상단 바에 두 줄 추가
        topBar.add(filterRow);
        topBar.add(buttonRow);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // 상단 라인: 제목 + 뒤로가기
        JPanel headerTopLine = new JPanel(new BorderLayout());
        headerTopLine.setOpaque(false);
        headerTopLine.add(sectionTitle, BorderLayout.WEST);

        JButton backButtonInv = new JButton("← 뒤로");
        styleFlatButton(backButtonInv);
        backButtonInv.addActionListener(e -> goBack());
        JPanel backWrapInv = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrapInv.setOpaque(false);
        backWrapInv.add(backButtonInv);
        headerTopLine.add(backWrapInv, BorderLayout.EAST);

        header.add(headerTopLine, BorderLayout.NORTH);
        header.add(topBar, BorderLayout.CENTER);
        main.add(header, BorderLayout.NORTH);

        // 테이블
        String[] columns = {"재료명", "카테고리", "보관 위치", "수량", "D-Day", "유통기한"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
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
                        // D-Day 컬럼(인덱스 4)에서 값을 가져와서 전체 행에 색상 적용 (상태 제거로 인덱스 변경)
                        Object value = getValueAt(row, 4); // D-Day 컬럼 (상태 제거로 인덱스 변경)
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

        // DB에서 인벤토리 로딩
        loadInventoryFromDb();
        // 로딩 후 필터/정렬 다시 적용
        apply.run();

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
                    // 모델 컬럼 인덱스 5 = 유통기한
                    columnIndex = 5;
                    break;
                case "이름":
                    // 모델 컬럼 인덱스 0 = 재료명
                    columnIndex = 0;
                    break;
                case "카테고리":
                    // 모델 컬럼 인덱스 1 = 카테고리
                    columnIndex = 1;
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


        // 실행 시 현재 날짜 기준으로 D-Day 전체 갱신
        recalculateAllDays();

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
                String nameVal = entry.getStringValue(0);   // 재료명
                String catVal = entry.getStringValue(1);    // 카테고리
                String locVal = entry.getStringValue(2);    // 보관 위치

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

    // 유통기한 임박/경과 알림 화면
    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setBackground(Color.WHITE);

        // 제목 + 새로고침 버튼
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel title =
                new JLabel("<html><span style='font-size:12pt;font-weight:600;'>유통기한 임박 알림</span><span style='font-size:10pt;color:#888;'>  (기준: D-" + IMMINENT_DAYS + " 이하)</span></html>");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        titleBar.add(title, BorderLayout.WEST);

        JButton backButtonAlert = new JButton("← 뒤로");
        styleFlatButton(backButtonAlert);
        backButtonAlert.addActionListener(e -> goBack());
        JPanel backWrapAlert = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrapAlert.setOpaque(false);
        backWrapAlert.add(backButtonAlert);
        titleBar.add(backWrapAlert, BorderLayout.EAST);

        JButton refreshBtn = new JButton("새로고침");
        styleFlatButton(refreshBtn);
        refreshBtn.addActionListener(e -> rebuildAlertData());
        JPanel rightAlert = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightAlert.setOpaque(false);
        rightAlert.add(refreshBtn);
        rightAlert.add(backButtonAlert);
        titleBar.add(rightAlert, BorderLayout.EAST);

        panel.add(titleBar, BorderLayout.NORTH);

        // 알림 테이블 (임박/경과 항목만)
        String[] cols = {"재료명", "유통기한", "D-Day", "수량", "보관 위치", "카테고리"};
        alertModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        alertTable = new JTable(alertModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                try {
                    if (row >= 0 && row < getRowCount()) {
                        Object ddayVal = getValueAt(row, 2); // D-Day
                        String dday = (ddayVal != null) ? ddayVal.toString() : "";
                        Color bg = resolveDDayColor(dday);
                        c.setBackground(bg);
                        c.setForeground(Color.BLACK);
                    }
                } catch (Exception ex) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        alertTable.setRowHeight(32);
        alertTable.setFillsViewportHeight(true);
        alertTable.setShowGrid(false);
        alertTable.setIntercellSpacing(new Dimension(0, 0));
        alertTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        alertTable.setSelectionBackground(new Color(235, 245, 255));
        alertTable.setSelectionForeground(Color.BLACK);

        // 유통기한 오름차순 정렬
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(alertModel);
        sorter.setComparator(1, (a, b) -> { // 유통기한(YYYY-MM-DD) 비교
            try {
                LocalDate la = LocalDate.parse(String.valueOf(a).trim(), DATE_FMT);
                LocalDate lb = LocalDate.parse(String.valueOf(b).trim(), DATE_FMT);
                return la.compareTo(lb);
            } catch (Exception ex) {
                return String.valueOf(a).compareTo(String.valueOf(b));
            }
        });
        alertTable.setRowSorter(sorter);
        sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING)));

        JScrollPane sp = new JScrollPane(alertTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
        panel.add(sp, BorderLayout.CENTER);

        // 초기 데이터 구성
        rebuildAlertData();

        return panel;
    }

    // 긴급 추천 메뉴 화면
    private JPanel createEmergencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setBackground(Color.WHITE);

        // 상단 타이틀 + 버튼
        JLabel title =
                new JLabel("<html><span style='font-size:12pt;font-weight:600;'>긴급 추천 메뉴</span><span style='font-size:10pt;color:#888;'>  (임박 재료 + 매칭률 우선)</span></html>");
        title.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel topLine = new JPanel(new BorderLayout());
        topLine.setOpaque(false);
        topLine.add(title, BorderLayout.WEST);

        // 오른쪽: 새로고침 + 뒤로
        JButton refreshBtn = new JButton("새로고침");
        styleFlatButton(refreshBtn);

        JButton backButtonEmg = new JButton("← 뒤로");
        styleFlatButton(backButtonEmg);
        backButtonEmg.addActionListener(e -> goBack());

        JPanel rightEmg = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightEmg.setOpaque(false);
        rightEmg.add(refreshBtn);
        rightEmg.add(backButtonEmg);
        topLine.add(rightEmg, BorderLayout.EAST);

        // 검색/카테고리 필터 바
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setOpaque(false);
        JTextField recipeSearchField = new JTextField(16);
        JComboBox<String> recipeCategoryFilter = new JComboBox<>(new String[]{
                "전체", "볶음밥/덮밥", "국/찌개", "면/파스타", "반찬", "기타"
        });

        filterBar.add(new JLabel("레시피 검색:"));
        filterBar.add(recipeSearchField);
        filterBar.add(new JLabel("카테고리:"));
        filterBar.add(recipeCategoryFilter);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(topLine, BorderLayout.NORTH);
        header.add(filterBar, BorderLayout.CENTER);

        // 상단 추천 레시피 하이라이트 영역
        topRecipeLabel = new JLabel("현재 인벤토리 기준 상위 추천 레시피가 여기 표시됩니다.");
        topRecipeLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
        JPanel highlightPanel = new JPanel(new BorderLayout());
        highlightPanel.setOpaque(false);
        highlightPanel.add(topRecipeLabel, BorderLayout.WEST);
        header.add(highlightPanel, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);

        // 레시피 추천 테이블 (전체 레시피 + 매칭률 기반 정렬)
        String[] cols = {"레시피명", "카테고리", "매칭률(%)", "보유 재료 수", "필요 재료 수", "임박 재료 수", "부족 재료 수", "설명"};
        recipeModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // 매칭률과 수량 관련 컬럼은 숫자 정렬을 위해 Integer 사용
                if (columnIndex >= 2 && columnIndex <= 6) {
                    return Integer.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        recipeTable = new JTable(recipeModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    Object value = getValueAt(row, col);
                    // 설명 컬럼(인덱스 7) 위에 마우스를 올리면 전체 설명을 툴팁으로 표시
                    if (col == 7 && value != null) {
                        return "<html><body style='width:400px;'>" + value.toString() + "</body></html>";
                    }
                }
                return super.getToolTipText(e);
            }
        };
        recipeTable.setRowHeight(32);
        recipeTable.setFillsViewportHeight(true);
        recipeTable.setShowGrid(false);
        recipeTable.setIntercellSpacing(new Dimension(0, 0));
        recipeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeTable.setSelectionBackground(new Color(235, 245, 255));
        recipeTable.setSelectionForeground(Color.BLACK);
        // 설명 컬럼이 너무 잘리지 않도록 기본 폭 조정 + 가로 스크롤 허용
        recipeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        TableColumnModel colModel = recipeTable.getColumnModel();
        if (colModel.getColumnCount() >= 8) {
            colModel.getColumn(0).setPreferredWidth(140); // 레시피명
            colModel.getColumn(1).setPreferredWidth(80);  // 카테고리
            colModel.getColumn(2).setPreferredWidth(80);  // 매칭률
            colModel.getColumn(3).setPreferredWidth(90);  // 보유 재료 수
            colModel.getColumn(4).setPreferredWidth(90);  // 필요 재료 수
            colModel.getColumn(5).setPreferredWidth(90);  // 임박 재료 수
            colModel.getColumn(6).setPreferredWidth(90);  // 부족 재료 수
            colModel.getColumn(7).setPreferredWidth(400); // 설명 컬럼 넓게
        }

        JScrollPane sp = new JScrollPane(recipeTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
        panel.add(sp, BorderLayout.CENTER);

        // 테이블 정렬/검색/필터를 위한 RowSorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(recipeModel);
        recipeTable.setRowSorter(sorter);

        Runnable applyRecipeFilter = () -> {
            String text = recipeSearchField.getText() != null
                    ? recipeSearchField.getText().trim().toLowerCase()
                    : "";
            String category = (String) recipeCategoryFilter.getSelectedItem();

            RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String nameVal = String.valueOf(entry.getValue(0));  // 레시피명
                    String catVal = String.valueOf(entry.getValue(1));   // 카테고리

                    if (!text.isEmpty()) {
                        if (nameVal == null || !nameVal.toLowerCase().contains(text)) {
                            return false;
                        }
                    }
                    if (category != null && !"전체".equals(category)) {
                        if (catVal == null || !catVal.equals(category)) {
                            return false;
                        }
                    }
                    return true;
                }
            };
            sorter.setRowFilter(filter);
        };

        // 검색창 입력 시 필터 적용
        recipeSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyRecipeFilter.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyRecipeFilter.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyRecipeFilter.run();
            }
        });

        // 카테고리 변경 시 필터 적용
        recipeCategoryFilter.addActionListener(e -> applyRecipeFilter.run());

        // 새로고침 버튼: 매칭률/임박 재계산
        refreshBtn.addActionListener(e -> rebuildRecipeRecommendations());

        // 레시피 더블클릭 시 상세 정보 팝업
        recipeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && recipeTable.getSelectedRow() >= 0) {
                    int viewRow = recipeTable.getSelectedRow();
                    int modelRow = recipeTable.convertRowIndexToModel(viewRow);
                    String recipeName = String.valueOf(recipeTable.getModel().getValueAt(modelRow, 0));
                    Recipe target = null;
                    for (Recipe r : RECIPE_DB) {
                        if (r.getName().equals(recipeName)) {
                            target = r;
                            break;
                        }
                    }
                    if (target != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("필요 재료:\n");
                        for (String ing : target.getIngredients()) {
                            boolean has = hasIngredient(ing);
                            sb.append("- ").append(ing);
                            if (has) sb.append(" (보유 중)");
                            sb.append("\n");
                        }
                        JOptionPane.showMessageDialog(frame, sb.toString(), target.getName(),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // 초기 추천 목록 구성 (전체 레시피 + 매칭률)
        rebuildRecipeRecommendations();

        // 기본 정렬: 매칭률(%) 기준 내림차순
        List<RowSorter.SortKey> sortKeys =
                Collections.singletonList(new RowSorter.SortKey(2, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);

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
                        showCard(cardName);
                    }
                }
            });
        }

        return p;
    }

    // 카드 전환 공통 처리: 이전 카드 히스토리 스택에 저장
    private void showCard(String cardName) {
        if (cardLayout == null || mainContainer == null || cardName == null) return;
        if (currentCard != null && !currentCard.equals(cardName)) {
            navStack.push(currentCard);
        }
        cardLayout.show(mainContainer, cardName);
        currentCard = cardName;
    }

    // 뒤로가기: 스택에서 이전 카드 꺼내 전환 (없으면 홈)
    private void goBack() {
        if (cardLayout == null || mainContainer == null) return;
        if (navStack.isEmpty()) {
            cardLayout.show(mainContainer, CARD_HOME);
            currentCard = CARD_HOME;
            return;
        }
        String prev = navStack.pop();
        cardLayout.show(mainContainer, prev);
        currentCard = prev;
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

        // 이미 경과(D+N) or 오늘(D-0)은 빨간색
        if (normalized.startsWith("D+")) return COLOR_DDAY_EXPIRED;
        if ("D".equals(normalized) || "D0".equals(normalized) || "D-0".equals(normalized)) {
            return COLOR_DDAY_EXPIRED;
        }

        // 남은 날(D-N) 규칙: N>=3 초록, N==2|1 노랑
        if (normalized.startsWith("D-")) {
            try {
                int days = Integer.parseInt(normalized.substring(2));
                if (days >= 3) return COLOR_DDAY_SAFE;       // D-3 이상: 초록
                if (days == 2 || days == 1) return COLOR_DDAY_WARNING; // D-2, D-1: 노랑
                if (days == 0) return COLOR_DDAY_EXPIRED;    // 안전망
            } catch (NumberFormatException ignored) {
                // no-op -> fall-through
            }
        }

        return Color.WHITE;
    }

    private void recalculateAllDays() {
        if (tableModel == null) return;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object expObj = tableModel.getValueAt(i, 5);
            if (expObj != null) {
                String exp = expObj.toString().trim();
                if (!exp.isEmpty()) {
                    try {
                        String newDday = calculateDDay(exp);
                        tableModel.setValueAt(newDday, i, 4);
                    } catch (Exception e) {
                        tableModel.setValueAt("D-0", i, 4);
                    }
                }
            }
        }
        if (table != null) table.repaint();
        rebuildAlertData();
        rebuildRecipeRecommendations();
    }

    // expiryStr(YYYY-MM-DD)까지 남은 일수 (오늘 기준, 음수면 경과)
    private long daysUntil(String expiryStr) {
        if (expiryStr == null || expiryStr.isBlank()) return Long.MAX_VALUE;
        try {
            LocalDate today = LocalDate.now();
            LocalDate expiry = LocalDate.parse(expiryStr.trim(), DATE_FMT);
            return ChronoUnit.DAYS.between(today, expiry);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    // 임박(D-IMMINENT_DAYS 이하) 또는 이미 경과한 항목인지 여부
    private boolean isImminentOrExpired(String expiryStr) {
        long d = daysUntil(expiryStr);
        return d <= IMMINENT_DAYS; // d<0(경과)도 포함
    }

    // 메인 인벤토리 테이블에서 임박/경과 항목을 읽어와 알림 테이블을 갱신
    private void rebuildAlertData() {
        if (alertModel == null) return;
        alertModel.setRowCount(0);
        if (tableModel == null) return;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = String.valueOf(tableModel.getValueAt(i, 0));
            String cat = String.valueOf(tableModel.getValueAt(i, 1));
            String loc = String.valueOf(tableModel.getValueAt(i, 2));
            Object qObj = tableModel.getValueAt(i, 3);
            String dday = String.valueOf(tableModel.getValueAt(i, 4));
            String exp = String.valueOf(tableModel.getValueAt(i, 5));

            if (isImminentOrExpired(exp)) {
                int qty = 0;
                if (qObj instanceof Number) qty = ((Number) qObj).intValue();
                else {
                    try {
                        qty = Integer.parseInt(String.valueOf(qObj));
                    } catch (Exception ignored) {
                        qty = 0;
                    }
                }
                alertModel.addRow(new Object[]{name, exp, dday, qty, loc, cat});
            }
        }

        // 정렬 갱신
        if (alertTable != null && alertTable.getRowSorter() != null) {
            alertTable.getRowSorter().allRowsChanged();
        }
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
        tableModel.setValueAt(qty, modelRow, 3);
    }

    private void syncQuantityEditorState() {
        if (quantitySpinner == null || quantityApplyButton == null || table == null) return;
        int viewRow = table.getSelectedRow();
        boolean hasSelection = viewRow >= 0;
        quantitySpinner.setEnabled(hasSelection);
        quantityApplyButton.setEnabled(hasSelection);
        if (hasSelection) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object current = tableModel.getValueAt(modelRow, 3);
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

            JTextField nameField = new JTextField(20);
            JComboBox<String> categoryField = new JComboBox<>(new String[]{"야채", "육류", "유제품", "기타"});
            JComboBox<String> locationField = new JComboBox<>(new String[]{"냉장", "냉동", "실온"});
            JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
            JTextField expField = new JTextField("2025-10-31", 15);

            form.add(new JLabel("재료명:"));
            form.add(nameField);
            form.add(new JLabel("카테고리:"));
            form.add(categoryField);
            form.add(new JLabel("보관 위치:"));
            form.add(locationField);
            form.add(new JLabel("수량:"));
            form.add(qtySpinner);
            form.add(new JLabel("유통기한 (YYYY-MM-DD):"));
            form.add(expField);

            // 다이얼로그에 직접 form 사용 (스크롤 패널 제거)
            int res = JOptionPane.showConfirmDialog(
                    frame, form, "재료 추가",
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
                String exp = expField.getText().trim();
                if (exp.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "유통기한(YYYY-MM-DD)을 입력하세요.");
                    return;
                }
                String dday = calculateDDay(exp);
                String category = (categoryField.getSelectedItem() != null ? categoryField.getSelectedItem().toString() : "");
                Object location = locationField.getSelectedItem();

                // 1) DB에 먼저 INSERT
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO inventory_items (name, category, location, quantity, expiry_date) " +
                                     "VALUES (?, ?, ?, ?, ?)"
                     )) {
                    ps.setString(1, name);
                    ps.setString(2, category);
                    ps.setString(3, String.valueOf(location));
                    ps.setInt(4, qty);
                    ps.setString(5, exp);
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "DB에 재료를 저장하는 중 오류가 발생했습니다: " + ex.getMessage(),
                            "DB 오류", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

                // 2) 테이블 모델에도 반영
                tableModel.addRow(new Object[]{
                        name,
                        category,
                        location,
                        qty,
                        dday,
                        exp
                });

                // 테이블 새로고침
                tableModel.fireTableDataChanged();
                if (table != null) {
                    table.repaint();
                }
                refreshBadge();
                rebuildAlertData();
                rebuildRecipeRecommendations();
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

            String curName = String.valueOf(tableModel.getValueAt(r, 0));
            String curCat = String.valueOf(tableModel.getValueAt(r, 1));
            String curLoc = String.valueOf(tableModel.getValueAt(r, 2));
            Object curQtyObj = tableModel.getValueAt(r, 3);
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
            String curDday = String.valueOf(tableModel.getValueAt(r, 4));
            String curExp = String.valueOf(tableModel.getValueAt(r, 5));

            JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
            form.setBorder(new EmptyBorder(10, 10, 10, 10));

            JTextField nameField = new JTextField(curName, 20);
            JComboBox<String> categoryField = new JComboBox<>(new String[]{"야채", "육류", "유제품", "기타"});
            categoryField.setEditable(false);
            categoryField.setSelectedItem(curCat);
            JComboBox<String> locationField = new JComboBox<>(new String[]{"냉장", "냉동", "실온"});
            locationField.setSelectedItem(curLoc);
            JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(curQty, 0, 9999, 1));
            JTextField ddayField = new JTextField(curDday, 10);
            ddayField.setEditable(false); // 자동 계산 표시만
            JTextField expField = new JTextField(curExp, 15);

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

            // 유통기한 입력에 따라 D-Day 실시간 갱신
            expField.getDocument().addDocumentListener(new DocumentListener() {
                private void update() {
                    String exp = expField.getText().trim();
                    try {
                        ddayField.setText(calculateDDay(exp));
                    } catch (Exception ex) {
                        ddayField.setText(curDday);
                    }
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });

            // 다이얼로그에 직접 form 사용 (스크롤 패널 제거)
            int res = JOptionPane.showConfirmDialog(
                    frame, form, "재료 수정",
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
                String exp = expField.getText().trim();
                if (exp.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "유통기한(YYYY-MM-DD)을 입력하세요.");
                    return;
                }
                String dday = calculateDDay(exp);
                String newCategory = (categoryField.getSelectedItem() != null ? categoryField.getSelectedItem().toString() : "");
                Object newLocation = locationField.getSelectedItem();

                // 1) DB UPDATE (기존 값 기준으로 1개 행 갱신)
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE inventory_items " +
                                     "SET name = ?, category = ?, location = ?, quantity = ?, expiry_date = ? " +
                                     "WHERE name = ? AND category = ? AND location = ? AND expiry_date = ? " +
                                     "LIMIT 1"
                     )) {
                    ps.setString(1, name);
                    ps.setString(2, newCategory);
                    ps.setString(3, String.valueOf(newLocation));
                    ps.setInt(4, qty);
                    ps.setString(5, exp);
                    ps.setString(6, curName);
                    ps.setString(7, curCat);
                    ps.setString(8, curLoc);
                    ps.setString(9, curExp);
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "DB에서 재료를 수정하는 중 오류가 발생했습니다: " + ex.getMessage(),
                            "DB 오류", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

                // 2) 테이블 모델에도 반영
                tableModel.setValueAt(name, r, 0);
                tableModel.setValueAt(newCategory, r, 1);
                tableModel.setValueAt(newLocation, r, 2);
                tableModel.setValueAt(qty, r, 3);
                tableModel.setValueAt(dday, r, 4);
                tableModel.setValueAt(exp, r, 5);

                // 테이블 새로고침
                tableModel.fireTableDataChanged();
                if (table != null) {
                    table.repaint();
                }
                refreshBadge();
                rebuildAlertData();
                syncQuantityEditorState();
                rebuildRecipeRecommendations();
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

                // 먼저 DB에서 삭제
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement ps = conn.prepareStatement(
                             "DELETE FROM inventory_items " +
                                     "WHERE name = ? AND category = ? AND location = ? AND expiry_date = ? " +
                                     "LIMIT 1"
                     )) {

                    for (int i = modelRows.length - 1; i >= 0; i--) {
                        int modelRow = modelRows[i];
                        if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                            String name = String.valueOf(tableModel.getValueAt(modelRow, 0));
                            String cat = String.valueOf(tableModel.getValueAt(modelRow, 1));
                            String loc = String.valueOf(tableModel.getValueAt(modelRow, 2));
                            String exp = String.valueOf(tableModel.getValueAt(modelRow, 5));

                            ps.setString(1, name);
                            ps.setString(2, cat);
                            ps.setString(3, loc);
                            ps.setString(4, exp);
                            ps.executeUpdate();
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "DB에서 재료를 삭제하는 중 오류가 발생했습니다: " + ex.getMessage(),
                            "DB 오류", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

                // 그 다음 테이블 모델에서 삭제
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
                rebuildAlertData();
                rebuildRecipeRecommendations();

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

    // 뱃지 갱신: 임박/경과 재료 개수 표시
    private void refreshBadge() {
        if (tableModel == null || badgeLabel == null) return;
        try {
            int count = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Object value = tableModel.getValueAt(i, 5); // 유통기한 컬럼
                if (value != null && isImminentOrExpired(String.valueOf(value))) {
                    count++;
                }
            }
            badgeLabel.setText("\u26A0 " + count);
        } catch (Exception e) {
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

    // 유통기한(YYYY-MM-DD) 문자열을 받아 D-Day 문자열(D-?, D+?, D-0)로 계산
    private String calculateDDay(String expiryStr) {
        if (expiryStr == null || expiryStr.isBlank()) return "D-0";
        LocalDate today = LocalDate.now(); // 기준: 오늘
        LocalDate expiry = LocalDate.parse(expiryStr.trim(), DATE_FMT);
        long diff = ChronoUnit.DAYS.between(today, expiry); // expiry - today
        if (diff > 0) return "D-" + diff;
        if (diff == 0) return "D-0";
        return "D+" + Math.abs(diff);
    }

    // 현재 인벤토리 상태를 기반으로 레시피 추천 목록을 다시 계산
    private void rebuildRecipeRecommendations() {
        if (recipeModel == null) return;
        recipeModel.setRowCount(0);
        if (tableModel == null || RECIPE_DB == null || RECIPE_DB.isEmpty()) return;

        List<RecipeMatch> matches = new ArrayList<>();

        for (Recipe recipe : RECIPE_DB) {
            int have = 0;
            int imminent = 0;
            int missing = 0;

            List<String> ingredients = recipe.getIngredients();
            int total = (ingredients != null) ? ingredients.size() : 0;

            if (ingredients != null) {
                for (String ing : ingredients) {
                    boolean has = false;
                    boolean isImminent = false;

                    // 인벤토리에서 해당 재료 검색 (이름 완전 일치, 대소문자 무시)
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String name = String.valueOf(tableModel.getValueAt(i, 0));
                        if (name != null && name.trim().equalsIgnoreCase(ing.trim())) {
                            // 수량 체크
                            Object qObj = tableModel.getValueAt(i, 3);
                            int qty = 0;
                            if (qObj instanceof Number) {
                                qty = ((Number) qObj).intValue();
                            } else if (qObj != null) {
                                try {
                                    qty = Integer.parseInt(String.valueOf(qObj));
                                } catch (NumberFormatException ignored) {
                                    qty = 0;
                                }
                            }
                            if (qty > 0) {
                                has = true;
                                Object expObj = tableModel.getValueAt(i, 5);
                                String exp = (expObj != null) ? String.valueOf(expObj) : null;
                                if (isImminentOrExpired(exp)) {
                                    isImminent = true;
                                }
                            }
                            break;
                        }
                    }

                    if (has) {
                        have++;
                        if (isImminent) imminent++;
                    } else {
                        missing++;
                    }
                }
            }

            int matchPercent = 0;
            if (total > 0) {
                matchPercent = (int) Math.round((have * 100.0) / total);
            }

            RecipeMatch match = new RecipeMatch(recipe, have, imminent, missing, matchPercent);
            matches.add(match);
        }

        // 매칭률 우선, 그 다음 임박 재료가 많은 순으로 정렬
        matches.sort((a, b) -> {
            int cmp = Integer.compare(b.matchPercent, a.matchPercent);
            if (cmp != 0) return cmp;
            return Integer.compare(b.score, a.score);
        });

        // 상단 하이라이트 라벨 업데이트 (최상위 추천 1개 기준)
        if (topRecipeLabel != null) {
            if (!matches.isEmpty()) {
                RecipeMatch top = matches.get(0);
                int totalCount = (top.recipe.getIngredients() != null)
                        ? top.recipe.getIngredients().size()
                        : 0;
                String labelText = String.format(
                        "<html><span style='font-size:10pt;color:#333;'>현재 최상위 추천:</span> " +
                                "<span style='font-weight:600;'>%s</span> " +
                                "<span style='font-size:9pt;color:#666;'>(매칭률 %d%%, 보유 %d / %d, 임박 %d개)</span></html>",
                        top.recipe.getName(),
                        top.matchPercent,
                        top.haveCount,
                        totalCount,
                        top.imminentCount
                );
                topRecipeLabel.setText(labelText);
            } else {
                topRecipeLabel.setText("표시할 레시피가 없습니다.");
            }
        }

        for (RecipeMatch m : matches) {
            String category = inferRecipeCategory(m.recipe.getName());
            int totalCount = (m.recipe.getIngredients() != null ? m.recipe.getIngredients().size() : 0);
            recipeModel.addRow(new Object[]{
                    m.recipe.getName(),
                    category,
                    m.matchPercent,
                    m.haveCount,
                    totalCount,
                    m.imminentCount,
                    m.missingCount,
                    m.recipe.getDescription()
            });
        }
    }

    // 레시피 이름 기반 간단 카테고리 분류
    private String inferRecipeCategory(String recipeName) {
        if (recipeName == null) return "기타";
        String n = recipeName;

        if (n.contains("볶음밥") || n.contains("덮밥")) {
            return "볶음밥/덮밥";
        }
        if (n.contains("국") || n.contains("찌개") || n.contains("탕")) {
            return "국/찌개";
        }
        if (n.contains("파스타") || n.contains("우동") || n.contains("국수") || n.contains("라면")) {
            return "면/파스타";
        }
        if (n.contains("전") || n.contains("볶음") || n.contains("조림") || n.contains("나물") || n.contains("무침")) {
            return "반찬";
        }
        return "기타";
    }

    // 인벤토리에 해당 재료가 있는지 단순 체크
    private boolean hasIngredient(String ingredientName) {
        if (tableModel == null || ingredientName == null) return false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = String.valueOf(tableModel.getValueAt(i, 0));
            if (name != null && name.trim().equalsIgnoreCase(ingredientName.trim())) {
                Object qObj = tableModel.getValueAt(i, 3);
                int qty = 0;
                if (qObj instanceof Number) {
                    qty = ((Number) qObj).intValue();
                } else if (qObj != null) {
                    try {
                        qty = Integer.parseInt(String.valueOf(qObj));
                    } catch (NumberFormatException ignored) {
                        qty = 0;
                    }
                }
                return qty > 0;
            }
        }
        return false;
    }

    // MySQL에서 레시피 + 재료 목록을 로딩
    private void loadRecipesFromDb() {
        RECIPE_DB.clear();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql =
                    "SELECT r.name, r.description, i.name AS ingredient_name " +
                            "FROM recipes r " +
                            "LEFT JOIN recipe_ingredients ri ON ri.recipe_id = r.id " +
                            "LEFT JOIN ingredients i ON i.id = ri.ingredient_id " +
                            "ORDER BY r.name";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            Map<String, TempRecipe> tempMap = new LinkedHashMap<>();
            while (rs.next()) {
                String name = rs.getString("name");
                String desc = rs.getString("description");
                String ingName = rs.getString("ingredient_name");

                TempRecipe temp = tempMap.get(name);
                if (temp == null) {
                    temp = new TempRecipe(desc);
                    tempMap.put(name, temp);
                }
                if (ingName != null && !ingName.isBlank()) {
                    temp.ingredients.add(ingName.trim());
                }
            }

            for (Map.Entry<String, TempRecipe> entry : tempMap.entrySet()) {
                String name = entry.getKey();
                TempRecipe t = entry.getValue();
                RECIPE_DB.add(new Recipe(name, t.ingredients, t.description));
            }

            System.out.println("레시피 로딩 완료: " + RECIPE_DB.size() + "개");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ignored) {}
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignored) {}
            try {
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }

        // DB에 아무 것도 없으면, 기본 샘플 레시피를 하나 넣어준다 (옵션)
        if (RECIPE_DB.isEmpty()) {
            List<String> ings = new ArrayList<>(Arrays.asList("계란", "밥", "대파"));
            RECIPE_DB.add(new Recipe("샘플 계란볶음밥", ings, "DB가 비어 있을 때 표시되는 샘플 레시피입니다."));
        }
    }

    // DB 로딩용 임시 레시피 구조체
    private static class TempRecipe {
        final String description;
        final List<String> ingredients = new ArrayList<>();

        TempRecipe(String description) {
            this.description = description;
        }
    }

    // 레시피와 매칭 점수
    private static class RecipeMatch {
        final Recipe recipe;
        final int haveCount;
        final int imminentCount;
        final int missingCount;
        final int matchPercent;
        final int score;

        RecipeMatch(Recipe recipe, int haveCount, int imminentCount, int missingCount, int matchPercent) {
            this.recipe = recipe;
            this.haveCount = haveCount;
            this.imminentCount = imminentCount;
            this.missingCount = missingCount;
            this.matchPercent = matchPercent;
            // 임박 재료와 보유 재료를 약간 가중치로 더 반영
            this.score = imminentCount * 10 + haveCount;
        }
    }

    // MySQL에서 인벤토리 목록 로딩
    private void loadInventoryFromDb() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql =
                    "SELECT name, category, location, quantity, expiry_date " +
                            "FROM inventory_items " +
                            "ORDER BY expiry_date ASC, name ASC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String cat = rs.getString("category");
                String loc = rs.getString("location");
                int qty = rs.getInt("quantity");
                String exp = rs.getString("expiry_date");
                String dday = calculateDDay(exp);

                tableModel.addRow(new Object[]{
                        name,
                        cat,
                        loc,
                        qty,
                        dday,
                        exp
                });
            }

            // 로딩 후 D-Day 색상/뱃지/알림/레시피 추천 갱신
            if (table != null) {
                table.repaint();
            }
            refreshBadge();
            rebuildAlertData();
            rebuildRecipeRecommendations();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException ignored) {}
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignored) {}
            try {
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }
}
