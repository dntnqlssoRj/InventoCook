package inventocook.ui;

import inventocook.repo.FileInventoryRepositoryCSV;
import inventocook.repo.InventoryRepository;
import inventocook.service.ExpirationChecker;
import inventocook.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;

public class MainFrame extends JFrame {
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel banner = new JLabel("임박 재료가 없습니다.");

    // 저장 경로(사용자 홈 디렉터리)
    private final File storageFile = new File(System.getProperty("user.home"), "inventocook_inventory.csv");

    public int expiryThresholdDays = 3; // 간단 설정값

    public final InventoryRepository inventoryRepo = new FileInventoryRepositoryCSV(storageFile);
    public final ExpirationChecker expirationChecker = new ExpirationChecker();

    private final InventoryPanel inventoryPanel = new InventoryPanel(this);

    public MainFrame() {
        super("InventoCook — 인벤토리 관리");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(makeTopBar(), BorderLayout.NORTH);

        tabs.addTab("인벤토리", inventoryPanel);
        tabs.addTab("레시피", new PlaceholderPanel("레시피 탭 (추가 예정)"));
        tabs.addTab("추천", new PlaceholderPanel("추천 탭 (추가 예정)"));
        add(tabs, BorderLayout.CENTER);

        setJMenuBar(makeMenuBar());

        // 초기 로드 및 임박 배너 갱신
        inventoryPanel.loadFromRepo();
        refreshBanner();

        // 10초마다 배너 자동 갱신
        new javax.swing.Timer(10_000, e -> refreshBanner()).start();
    }

    private JComponent makeTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        banner.setOpaque(true);
        banner.setBackground(new Color(245, 248, 252));
        banner.setForeground(new Color(30, 60, 90));
        banner.setFont(banner.getFont().deriveFont(Font.BOLD, 14f));
        p.add(banner, BorderLayout.CENTER);

        JButton settingsBtn = new JButton("설정(D-일)");
        settingsBtn.addActionListener(this::onChangeThreshold);
        p.add(settingsBtn, BorderLayout.EAST);
        return p;
    }

    private JMenuBar makeMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu file = new JMenu("파일");
        JMenuItem load = new JMenuItem("불러오기");
        JMenuItem save = new JMenuItem("저장하기");
        JMenuItem exit = new JMenuItem("종료");
        load.addActionListener(e -> {
            inventoryPanel.loadFromRepo();
            refreshBanner();
        });
        save.addActionListener(e -> inventoryPanel.saveToRepo());
        exit.addActionListener(e -> System.exit(0));
        file.add(load); file.add(save); file.addSeparator(); file.add(exit);

        JMenu view = new JMenu("보기");
        JCheckBoxMenuItem dark = new JCheckBoxMenuItem("다크모드(간이)");
        dark.addActionListener(e -> toggleDarkMode(dark.isSelected()));
        view.add(dark);

        mb.add(file);
        mb.add(view);
        return mb;
    }

    private void toggleDarkMode(boolean on) {
        Color bg = on ? new Color(40, 44, 52) : Color.WHITE;
        Color fg = on ? new Color(220, 220, 220) : Color.DARK_GRAY;
        SwingUtilities.invokeLater(() -> {
            setComponentColors(this.getContentPane(), bg, fg);
            banner.setBackground(on ? new Color(50, 54, 62) : new Color(245, 248, 252));
            banner.setForeground(on ? new Color(230, 230, 230) : new Color(30, 60, 90));
        });
    }

    private void setComponentColors(Component c, Color bg, Color fg) {
        if (c instanceof JComponent jc) {
            jc.setOpaque(true);
            jc.setBackground(bg);
            jc.setForeground(fg);
        }
        if (c instanceof Container cont) {
            for (Component child : cont.getComponents()) setComponentColors(child, bg, fg);
        }
    }

    public void refreshBanner() {
        var list = inventoryRepo.findAll();
        var due = expirationChecker.dueWithin(list, expiryThresholdDays);
        if (due.isEmpty()) {
            banner.setText("임박 재료가 없습니다. (D-" + expiryThresholdDays + ")");
            banner.setBackground(new Color(245, 248, 252));
        } else {
            String names = String.join(", ", due.stream().map(i -> i.name).toList());
            banner.setText("임박: " + names + " — " + LocalDate.now() + " 기준 D-" + expiryThresholdDays);
            banner.setBackground(new Color(255, 240, 240));
        }
    }

    private void onChangeThreshold(ActionEvent e) {
        String input = JOptionPane.showInputDialog(this, "임박 임계일(D) 입력", expiryThresholdDays);
        if (input == null) return;
        try {
            int d = Integer.parseInt(input.trim());
            if (d <= 0 || d > 30) throw new IllegalArgumentException();
            expiryThresholdDays = d;
            refreshBanner();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "1~30 사이 정수를 입력하세요", "입력 오류", JOptionPane.WARNING_MESSAGE);
        }
    }
}