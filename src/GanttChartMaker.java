import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * JFreeChart 없이 순수 Swing으로 그리는 간단 간트 차트.
 * 2025-10-27 ~ 2025-12-22 일정의 4단계(설계/개발/테스트/발표)를 표시한다.
 */
public class GanttChartMaker {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GanttChartMaker().initUI();
        });
    }

    private void initUI() {
        List<TaskInfo> tasks = createTasks();
        GanttPanel panel = new GanttPanel(tasks);

        JFrame frame = new JFrame("프로젝트 개발 간트 차트 (2025-10-27 ~ 2025-12-22)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setPreferredSize(new Dimension(900, 500));
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ----------------------------- 데이터 정의 -----------------------------

    private static class TaskInfo {
        final String name;
        final Date start;
        final Date end;

        TaskInfo(String name, Date start, Date end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }

    private Date getDate(int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month - 1, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 기존 JFreeChart용 createDataset()을 대체하는 작업 목록 생성 메서드.
     */
    private List<TaskInfo> createTasks() {
        List<TaskInfo> list = new ArrayList<>();

        // 1. 설계 (10월 27일 ~ 11월 9일) : 14일간
        list.add(new TaskInfo(
                "설계",
                getDate(2025, 10, 27),
                getDate(2025, 11, 9)
        ));

        // 2. 개발 (11월 10일 ~ 12월 8일)
        list.add(new TaskInfo(
                "개발",
                getDate(2025, 11, 10),
                getDate(2025, 12, 8)
        ));

        // 3. 테스트 (12월 9일 ~ 12월 15일)
        list.add(new TaskInfo(
                "테스트",
                getDate(2025, 12, 9),
                getDate(2025, 12, 15)
        ));

        // 4. 발표 (12월 22일 하루)
        list.add(new TaskInfo(
                "발표",
                getDate(2025, 12, 22),
                getDate(2025, 12, 22)
        ));

        return list;
    }

    // ----------------------------- 그리기 패널 -----------------------------

    private static class GanttPanel extends JPanel {
        private final List<TaskInfo> tasks;
        private final Date minDate;
        private final Date maxDate;
        private final SimpleDateFormat fmt = new SimpleDateFormat("MM/dd");

        GanttPanel(List<TaskInfo> tasks) {
            this.tasks = tasks;
            Date min = null, max = null;
            for (TaskInfo t : tasks) {
                if (min == null || t.start.before(min)) min = t.start;
                if (max == null || t.end.after(max)) max = t.end;
            }
            this.minDate = min;
            this.maxDate = max;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int leftMargin = 140;
            int rightMargin = 40;
            int topMargin = 60;
            int bottomMargin = 60;

            // 제목
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 18));
            String title = "프로젝트 개발 일정";
            FontMetrics fm = g2.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(title, (w - titleWidth) / 2, 30);

            if (tasks.isEmpty() || minDate == null || maxDate == null) {
                g2.drawString("표시할 작업이 없습니다.", leftMargin, topMargin);
                g2.dispose();
                return;
            }

            long minMillis = minDate.getTime();
            long maxMillis = maxDate.getTime();
            long totalMillis = maxMillis - minMillis;
            double totalDays = totalMillis / (1000.0 * 60 * 60 * 24);

            int chartWidth = w - leftMargin - rightMargin;
            int barHeight = 24;
            int barGap = 12;

            int axisY = h - bottomMargin;
            g2.setColor(new Color(220, 220, 220));
            g2.drawLine(leftMargin, axisY, w - rightMargin, axisY);

            g2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            g2.setColor(Color.GRAY);
            g2.drawString(fmt.format(minDate), leftMargin, axisY + 16);
            String endLabel = fmt.format(maxDate);
            int endWidth = g2.getFontMetrics().stringWidth(endLabel);
            g2.drawString(endLabel, w - rightMargin - endWidth, axisY + 16);

            int y = topMargin;
            for (TaskInfo t : tasks) {
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(t.name, 20, y + barHeight - 6);

                long startMillis = t.start.getTime();
                long endMillis = t.end.getTime();
                double startDays = (startMillis - minMillis) / (1000.0 * 60 * 60 * 24);
                double endDays = (endMillis - minMillis) / (1000.0 * 60 * 60 * 24);

                int barX = leftMargin + (int) (chartWidth * (startDays / totalDays));
                int barEndX = leftMargin + (int) (chartWidth * (endDays / totalDays));
                int barW = Math.max(6, barEndX - barX + 1);

                Color fill = new Color(52, 152, 219);
                if ("발표".equals(t.name)) {
                    fill = new Color(231, 76, 60); // 발표는 붉은색 계열
                }

                g2.setColor(fill);
                g2.fillRoundRect(barX, y, barW, barHeight, 10, 10);
                g2.setColor(fill.darker());
                g2.drawRoundRect(barX, y, barW, barHeight, 10, 10);

                y += barHeight + barGap;
            }

            g2.dispose();
        }
    }
}