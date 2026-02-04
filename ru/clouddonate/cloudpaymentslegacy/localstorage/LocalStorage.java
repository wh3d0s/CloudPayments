package ru.clouddonate.cloudpaymentslegacy.localstorage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.clouddonate.cloudpaymentslegacy.CloudPayments;
import ru.clouddonate.cloudpaymentslegacy.api.Manager;
import ru.clouddonate.cloudpaymentslegacy.config.Config;
import ru.clouddonate.cloudpaymentslegacy.file.FileUtil;
import ru.clouddonate.cloudpaymentslegacy.http.GetResult;

public class LocalStorage extends Manager {

    private final String dbUrl;
    private final File txtFile;

    public LocalStorage(CloudPayments cloudPayments) {
        super(cloudPayments);
        this.dbUrl = "jdbc:h2:" + cloudPayments.getDataFolder().getAbsolutePath() + "/local/database";
        this.txtFile = new File(cloudPayments.getDataFolder(), "local/payments.txt");
        
        if ("H2".equalsIgnoreCase(Config.LocalStorage.Payments.type)) {
            initDatabase();
        }
    }

    private void initDatabase() {
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS payments (id INT PRIMARY KEY, nickname VARCHAR(64), product VARCHAR(255), amount INT, price DOUBLE, date TIMESTAMP)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPayment(GetResult result) {
        if (!Config.LocalStorage.Payments.enabled) return;
        if ("H2".equalsIgnoreCase(Config.LocalStorage.Payments.type)) saveToH2(result);
        else saveToTxt(result);
    }

    private void saveToTxt(GetResult result) {
        String line = Config.LocalStorage.Payments.format
                .replace("<date>", java.time.LocalDate.now().toString())
                .replace("<count>", String.valueOf(result.getAmount()))
                .replace("<nickname>", result.getNickname())
                .replace("<product_name>", result.getName())
                .replace("<price>", String.valueOf(result.getPrice()))
                .replace("<payment_id>", String.valueOf(result.getId()));
        FileUtil.appendToFile(txtFile.getAbsolutePath(), line);
    }

    private void saveToH2(GetResult result) {
        String sql = "MERGE INTO payments (id, nickname, product, amount, price, date) KEY(id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, result.getId());
            ps.setString(2, result.getNickname());
            ps.setString(3, result.getName());
            ps.setInt(4, result.getAmount());
            ps.setDouble(5, result.getPrice());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int migrate() {
        if (!txtFile.exists()) return 0;
        int count = 0;

        Pattern flexiblePattern = Pattern.compile("(?i)^.*?:?\\s*(.*?)\\s*-\\s*(.*?)\\s*\\[.*?:?\\s*(\\d+)\\].*?\\+?([\\d.]+).*?ID.*?(\\d+)$");

        try (Connection conn = DriverManager.getConnection(dbUrl, "sa", "");
             PreparedStatement ps = conn.prepareStatement("MERGE INTO payments (id, nickname, product, amount, price, date) KEY(id) VALUES (?, ?, ?, ?, ?, ?)")) {
            
            for (String line : Files.readAllLines(txtFile.toPath(), StandardCharsets.UTF_8)) {
                Matcher m = flexiblePattern.matcher(line);
                if (m.find()) {
                    try {
                        ps.setInt(1, Integer.parseInt(m.group(5)));
                        ps.setString(2, m.group(1));
                        ps.setString(3, m.group(2));
                        ps.setInt(4, Integer.parseInt(m.group(3)));
                        ps.setDouble(5, Double.parseDouble(m.group(4)));
                        ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                        ps.addBatch();
                        count++;
                    } catch (Exception ignored) {}
                }
            }
            ps.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public void addStatistic(GetResult getResult) {}
}