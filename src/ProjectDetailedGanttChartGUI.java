import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * JFreeChart 없이 순수 Swing으로 그리는 간단 간트 차트 GUI.
 * PBL 프로젝트 일정(10/27 ~ 12/22)을 막대바로 표시한다.
 */
public class ProjectDetailedGanttChartGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProjectDetailedGanttChartGUI chartApp = new ProjectDetailedGanttChartGUI();
            chartApp.initUI();
        });
    }

    private void initUI() {
        // 1. 작업 목록 생성
        List<TaskInfo> tasks = createTasks();

        // 2. 패널 생성
        GanttPanel panel = new GanttPanel(tasks);

        // 3. 프레임 설정
        JFrame frame = new JFrame("InventoCook 세부 개발 간트 차트 (10/27 ~ 12/22)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.setPreferredSize(new Dimension(1000, 550));
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ---------------------------------------------------------------------
    // 데이터 정의
    // ---------------------------------------------------------------------

    /** 작업 하나를 표현하기 위한 간단한 DTO */
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

    /**
     * month는 1부터 시작하는 실제 월을 입력받아 0부터 시작하는 Calendar의 월로 변환한다.
     */
    private Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** 기존 JFreeChart용 createDataset() 대신, Task 목록을 만든다. */
    private List<TaskInfo> createTasks() {
        List<TaskInfo> list = new ArrayList<>();

        // 주차 1~2: 기초 설계/ERD & UI-UX
        list.add(new TaskInfo(
                "기초 설계/ERD & UI-UX",
                getDate(2025, 10, 27),
                getDate(2025, 11, 9)
        ));

        // 주차 3: GUI 프레임워크 & DB 초기 구축
        list.add(new TaskInfo(
                "GUI 프레임워크 & DB 초기 구축",
                getDate(2025, 11, 10),
                getDate(2025, 11, 16)
        ));

        // 주차 4: 식재료 인벤토리 관리 기능
        list.add(new TaskInfo(
                "1. 식재료 인벤토리 관리 기능",
                getDate(2025, 11, 17),
                getDate(2025, 11, 23)
        ));

        // 주차 5: 유통기한 임박 알림 기능
        list.add(new TaskInfo(
                "2. 유통기한 임박 알림 기능",
                getDate(2025, 11, 24),
                getDate(2025, 11, 30)
        ));

        // 주차 6: 긴급 추천 메뉴 로직
        list.add(new TaskInfo(
                "3. 긴급 추천 메뉴 로직",
                getDate(2025, 12, 1),
                getDate(2025, 12, 7)
        ));

        // 주차 7: 통합 테스트 및 디버깅
        list.add(new TaskInfo(
                "통합 테스트 및 디버깅",
                getDate(2025, 12, 8),
                getDate(2025, 12, 14)
        ));

        // 주차 8: 최종 점검/보강 및 발표 준비
        list.add(new TaskInfo(
                "최종 점검/보강 및 발표 준비",
                getDate(2025, 12, 15),
                getDate(2025, 12, 22)
        ));

        return list;
    }

    // ---------------------------------------------------------------------
    // 실제 그리기를 담당하는 패널
    // ---------------------------------------------------------------------

    private static class GanttPanel extends JPanel {
        private final List<TaskInfo> tasks;
        private final Date minDate;
        private final Date maxDate;
        private final SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd");

        GanttPanel(List<TaskInfo> tasks) {
            this.tasks = tasks;

            // 전체 기간의 최소/최대 날짜 계산
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

            // 여백 설정
            int leftMargin = 200;
            int rightMargin = 40;
            int topMargin = 60;
            int bottomMargin = 60;

            // 제목
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("맑은 고딕", Font.BOLD, 20));
            String title = "InventoCook 세부 개발 일정 (10/27 ~ 12/22)";
            FontMetrics fm = g2.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2.drawString(title, (w - titleWidth) / 2, 30);

            if (tasks.isEmpty() || minDate == null || maxDate == null) {
                g2.drawString("표시할 작업이 없습니다.", leftMargin, topMargin);
                g2.dispose();
                return;
            }

            // 전체 기간을 일(day) 단위로 환산
            long minMillis = minDate.getTime();
            long maxMillis = maxDate.getTime();
            long totalMillis = maxMillis - minMillis;
            double totalDays = totalMillis / (1000.0 * 60 * 60 * 24);

            int chartWidth = w - leftMargin - rightMargin;
            int barHeight = 20;
            int barGap = 14;

            // 타임라인 축
            int axisY = h - bottomMargin;
            g2.setColor(new Color(220, 220, 220));
            g2.drawLine(leftMargin, axisY, w - rightMargin, axisY);

            // 하단 축 날짜 (시작/끝만 간단히 표시)
            g2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            g2.setColor(Color.GRAY);
            g2.drawString(dateFmt.format(minDate), leftMargin, axisY + 20);
            String endLabel = dateFmt.format(maxDate);
            int endLabelWidth = g2.getFontMetrics().stringWidth(endLabel);
            g2.drawString(endLabel, w - rightMargin - endLabelWidth, axisY + 20);

            // 각 작업에 대한 막대 그리기
            int y = topMargin;
            for (TaskInfo t : tasks) {
                // 왼쪽 작업 이름
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(t.name, 20, y + barHeight - 4);

                // 기간 → X 좌표 변환
                long startMillis = t.start.getTime();
                long endMillis = t.end.getTime();
                double startDaysFromMin = (startMillis - minMillis) / (1000.0 * 60 * 60 * 24);
                double endDaysFromMin = (endMillis - minMillis) / (1000.0 * 60 * 60 * 24);

                int barX = leftMargin + (int) (chartWidth * (startDaysFromMin / totalDays));
                int barEndX = leftMargin + (int) (chartWidth * (endDaysFromMin / totalDays));
                int barW = Math.max(8, barEndX - barX);

                // 막대 배경
                g2.setColor(new Color(255, 230, 180));
                g2.fillRoundRect(barX, y, barW, barHeight, 8, 8);
                g2.setColor(new Color(255, 140, 0));
                g2.drawRoundRect(barX, y, barW, barHeight, 8, 8);

                y += barHeight + barGap;
            }

            g2.dispose();
        }
    }
}