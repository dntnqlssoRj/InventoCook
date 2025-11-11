package inventocook.ui;

import inventocook.model.Ingredient;
import inventocook.repo.InventoryRepository;
import inventocook.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final MainFrame app;
    private final JTable table;
    private final DefaultTableModel model;

    public InventoryPanel(MainFrame app) {
        super(new BorderLayout());
        this.app = app;

        model = new DefaultTableModel(
                new Object[]{"이름", "카테고리", "유통기한(YYYY-MM-DD)", "수량"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(makeToolbar(), BorderLayout.NORTH);
        add(makeStatusBar(), BorderLayout.SOUTH);
    }

    private JComponent makeToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton add = new JButton("추가");
        JButton edit = new JButton("수정");
        JButton del = new JButton("삭제");
        JButton up = new JButton("▲ 수량+1");
        JButton down = new JButton("▼ 수량-1");
        JButton save = new JButton("저장");
        JButton load = new JButton("불러오기");

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        del.addActionListener(e -> onDelete());
        up.addActionListener(e -> adjustQty(+1));
        down.addActionListener(e -> adjustQty(-1));
        save.addActionListener(e -> saveToRepo());
        load.addActionListener(e -> loadFromRepo());

        p.add(add); p.add(edit); p.add(del);
        p.add(new JSeparator(SwingConstants.VERTICAL));
        p.add(up); p.add(down);
        p.add(new JSeparator(SwingConstants.VERTICAL));
        p.add(save); p.add(load);
        return p;
    }

    private JComponent makeStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel tip = new JLabel("Tip: 행 더블클릭 = 수정, Delete = 삭제");
        p.add(tip, BorderLayout.WEST);
        return p;
    }

    // --- Repo 연동 ---

    public void loadFromRepo() {
        InventoryRepository repo = app.inventoryRepo;
        model.setRowCount(0);
        for (var i : repo.findAll()) {
            model.addRow(new Object[]{i.name, i.category, DateUtil.format(i.expiry), i.qty});
        }
    }

    public void saveToRepo() {
        app.inventoryRepo.saveAll(getAllFromTable());
        JOptionPane.showMessageDialog(this, "저장 완료", "정보", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<Ingredient> getAllFromTable() {
        List<Ingredient> list = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            String name = (String) model.getValueAt(r, 0);
            String cat  = (String) model.getValueAt(r, 1);
            LocalDate exp = DateUtil.parse((String) model.getValueAt(r, 2));
            int qty = Integer.parseInt(String.valueOf(model.getValueAt(r, 3)));
            list.add(new Ingredient(name, cat, exp, qty));
        }
        return list;
    }

    // --- CRUD ---

    private void onAdd() {
        Ingredient i = showEditorDialog(null);
        if (i != null) {
            model.addRow(new Object[]{i.name, i.category, DateUtil.format(i.expiry), i.qty});
            app.refreshBanner();
        }
    }

    private void onEdit() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        Ingredient base = new Ingredient(
                (String) model.getValueAt(r, 0),
                (String) model.getValueAt(r, 1),
                DateUtil.parse((String) model.getValueAt(r, 2)),
                Integer.parseInt(String.valueOf(model.getValueAt(r, 3)))
        );
        Ingredient edited = showEditorDialog(base);
        if (edited != null) {
            model.setValueAt(edited.name, r, 0);
            model.setValueAt(edited.category, r, 1);
            model.setValueAt(DateUtil.format(edited.expiry), r, 2);
            model.setValueAt(edited.qty, r, 3);
            app.refreshBanner();
        }
    }

    private void onDelete() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        int ok = JOptionPane.showConfirmDialog(this, "삭제하시겠습니까?", "확인",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            model.removeRow(r);
            app.refreshBanner();
        }
    }

    private void adjustQty(int delta) {
        int r = table.getSelectedRow();
        if (r < 0) return;
        int cur = Integer.parseInt(String.valueOf(model.getValueAt(r, 3)));
        int next = cur + delta;
        if (next < 0) {
            JOptionPane.showMessageDialog(this, "수량은 0 미만이 될 수 없습니다.",
                    "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        model.setValueAt(next, r, 3);
        app.refreshBanner();
    }

    // --- 입력 다이얼로그 ---

    private Ingredient showEditorDialog(Ingredient base) {
        JTextField name = new JTextField(base == null ? "" : base.name);
        JTextField cat  = new JTextField(base == null ? "" : base.category);
        JTextField exp  = new JTextField(base == null ? "" : DateUtil.format(base.expiry));
        JSpinner qty    = new JSpinner(new SpinnerNumberModel(base == null ? 1 : base.qty, 0, 9999, 1));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("이름")); form.add(name);
        form.add(new JLabel("카테고리")); form.add(cat);
        form.add(new JLabel("유통기한(YYYY-MM-DD)")); form.add(exp);
        form.add(new JLabel("수량")); form.add(qty);

        int ok = JOptionPane.showConfirmDialog(
                this, form,
                base == null ? "재료 추가" : "재료 수정",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (ok != JOptionPane.OK_OPTION) return null;

        // 검증
        String n = name.getText().trim();
        String c = cat.getText().trim();
        LocalDate d;
        try {
            if (n.isEmpty()) throw new IllegalArgumentException("이름은 필수입니다.");
            d = DateUtil.parse(exp.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "입력값을 확인하세요. 날짜 형식: YYYY-MM-DD",
                    "입력 오류",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }
        int q = (Integer) qty.getValue();
        return new Ingredient(n, c, d, q);
    }
}