package inventocook.repo;

import inventocook.model.Ingredient;
import inventocook.util.DateUtil;

import java.io.*;
        import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileInventoryRepositoryCSV implements InventoryRepository {
    private final File file;

    public FileInventoryRepositoryCSV(File file) { this.file = file; }

    @Override public List<Ingredient> findAll() {
        List<Ingredient> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line; // 헤더 스킵 가능
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] t = line.split(",", -1);
                if (t.length < 4) continue;
                String name = unescape(t[0]);
                String cat = unescape(t[1]);
                LocalDate exp = DateUtil.parse(t[2]);
                int qty = Integer.parseInt(t[3]);
                list.add(new Ingredient(name, cat, exp, qty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override public void saveAll(List<Ingredient> list) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            bw.write("# name,category,expiry,qty\n");
            for (var i : list) {
                String line = String.join(",",
                        escape(i.name),
                        escape(i.category==null?"":i.category),
                        DateUtil.format(i.expiry),
                        String.valueOf(i.qty)
                );
                bw.write(line); bw.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(",", "\\,");
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (char ch : s.toCharArray()) {
            if (esc) { sb.append(ch); esc = false; }
            else if (ch == '\\') esc = true;
            else sb.append(ch);
        }
        return sb.toString();
    }
}

