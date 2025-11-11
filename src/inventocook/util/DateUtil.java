package inventocook.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static String format(LocalDate d) { return d == null ? "" : d.format(F); }
    public static LocalDate parse(String s) { return (s==null||s.isBlank()) ? null : LocalDate.parse(s.trim(), F); }
}
