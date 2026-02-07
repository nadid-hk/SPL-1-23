import java.io.*;
import java.util.*;

public class FileDatabase {

    private String fileName;
    private List<String> columns;

    /* ================= CREATE TABLE ================= */
    public FileDatabase(String fileName, List<String> columns) {
        this.fileName = fileName;
        this.columns = columns;

        File file = new File(fileName);
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(String.join("|", columns));
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* ================= INSERT ================= */
    public void insert(Map<String, String> record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {

            StringBuilder row = new StringBuilder();
            for (String col : columns) {
                String value = record.containsKey(col) ? record.get(col) : "";
                row.append(value).append("|");
            }
            row.deleteCharAt(row.length() - 1);

            bw.write(row.toString());
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* ================= SELECT * ================= */
    public List<Map<String, String>> readAll() {
        List<Map<String, String>> results = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String header = br.readLine();
            String[] cols = header.split("\\|");

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\|", -1);
                Map<String, String> record = new LinkedHashMap<>();

                for (int i = 0; i < cols.length; i++) {
                    record.put(cols[i], i < values.length ? values[i] : "");
                }
                results.add(record);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /* ================= SELECT WHERE ================= */
    public Map<String, String> find(String key, String value) {
        for (Map<String, String> record : readAll()) {
            if (value.equals(record.get(key))) {
                return record;
            }
        }
        return null;
    }

    /* ================= SELECT SINGLE VALUE ================= */
    // Example: get age where id = A001
    public String getValueByPrimaryKey(String primaryKey,
                                       String primaryValue,
                                       String targetColumn) {

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String header = br.readLine();
            String[] cols = header.split("\\|");

            int pkIndex = -1;
            int targetIndex = -1;

            for (int i = 0; i < cols.length; i++) {
                if (cols[i].equals(primaryKey)) pkIndex = i;
                if (cols[i].equals(targetColumn)) targetIndex = i;
            }

            if (pkIndex == -1 || targetIndex == -1) {
                return null; // column not found
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\|", -1);
                if (values[pkIndex].equals(primaryValue)) {
                    return values[targetIndex];
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* ================= UPDATE ================= */
    public void update(String key, String value, Map<String, String> newData) {

        File temp = new File("temp.db");

        try (
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp))
        ) {
            String header = br.readLine();
            bw.write(header);
            bw.newLine();

            String[] cols = header.split("\\|");
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\|", -1);
                Map<String, String> record = new LinkedHashMap<>();

                for (int i = 0; i < cols.length; i++) {
                    record.put(cols[i], values[i]);
                }

                if (value.equals(record.get(key))) {
                    for (String k : newData.keySet()) {
                        record.put(k, newData.get(k));
                    }
                }

                bw.write(String.join("|", record.values()));
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        replaceOriginalFile(temp);
    }

    /* ================= DELETE ================= */
    public void delete(String key, String value) {

        File temp = new File("temp.db");

        try (
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp))
        ) {
            String header = br.readLine();
            bw.write(header);
            bw.newLine();

            String[] cols = header.split("\\|");
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\|", -1);

                if (!values[Arrays.asList(cols).indexOf(key)].equals(value)) {
                    bw.write(line);
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        replaceOriginalFile(temp);
    }

    /* ================= FILE REPLACE ================= */
    private void replaceOriginalFile(File temp) {
        File original = new File(fileName);
        original.delete();
        temp.renameTo(original);
    }
}
