import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RecipeDbTest {
    public static void main(String[] args) {
        String url  = "jdbc:mysql://localhost:3306/inventocook?useSSL=false&serverTimezone=UTC";
        String user = "root";      // MySQL 계정
        String pass = "wjdgns2003@";   // 비밀번호

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("DB 연결 성공");

            String sql = "SELECT id, name, description, continent, country, category, is_solo " +
                    "FROM recipes " +
                    "WHERE country = 'Korea' AND is_solo = 1";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    String desc = rs.getString("description");
                    String continent = rs.getString("continent");
                    String country = rs.getString("country");
                    String category = rs.getString("category");
                    boolean isSolo = rs.getBoolean("is_solo");

                    System.out.println("[" + id + "] " + name +
                            " (" + continent + "/" + country + ", " + category + ", solo=" + isSolo + ")");
                    System.out.println("   - " + desc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}