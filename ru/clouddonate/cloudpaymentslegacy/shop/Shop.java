package ru.clouddonate.cloudpaymentslegacy.shop;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import ru.clouddonate.cloudpaymentslegacy.CloudPayments;
import ru.clouddonate.cloudpaymentslegacy.api.events.PurchaseApproveEvent;
import ru.clouddonate.cloudpaymentslegacy.config.Config;
import ru.clouddonate.cloudpaymentslegacy.http.GetResult;

@Getter
public final class Shop {

    private static final String API_URL = "https://api.cdonate.ru/api/v1/shops/%s/purchases/pending?server_id=%s";
    private static final String APPROVE_URL = "https://api.cdonate.ru/api/v1/shops/%s/purchases/%d/approve";
    
    private final String shopId;
    private final String shopKey;
    private final String serverId;
    private final long requestDelay;
    private final CloudPayments plugin;
    private BukkitRunnable runnable;

    public Shop(String shopId, String shopKey, String serverId, long requestDelay, CloudPayments plugin) {
        this.shopId = shopId;
        this.shopKey = shopKey;
        this.serverId = serverId;
        this.requestDelay = Math.max(requestDelay, 20L);
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                checkPayments();
            }
        };
        this.runnable.runTaskTimerAsynchronously(plugin, requestDelay * 20L, requestDelay * 20L);
    }

    private void checkPayments() {
        try {
            HttpURLConnection conn = createConnection(String.format(API_URL, shopId, serverId), "GET");
            if (conn.getResponseCode() != 200) {
                if (Config.Settings.debug) plugin.getLogger().warning("Failed to fetch data: " + conn.getResponseCode());
                return;
            }

            GetResult[] results;
            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                results = plugin.getConverterService().gson.fromJson(reader, GetResult[].class);
            }

            if (results == null) return;
            if (Config.Settings.debug) plugin.getLogger().info("GET return " + results.length + " results");

            for (GetResult data : results) {
                processPayment(data);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("[CloudPayments] Error fetching shop data: " + e.getMessage());
        }
    }

    private void processPayment(GetResult data) {
        List<String> commands = new ArrayList<>();
        for (String cmd : data.getCommands()) {
            commands.add(cmd.replace("{user}", data.getNickname()).replace("{amount}", String.valueOf(data.getAmount())));
        }

        if (!commands.isEmpty()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (String command : commands) {
                    try {
                        if (Config.Settings.debug) plugin.getLogger().info("Executing: " + command);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to execute: " + command);
                        e.printStackTrace();
                    }
                }
            });
        }

        approvePayment(data);
    }

    private void approvePayment(GetResult data) {
        try {
            HttpURLConnection conn = createConnection(String.format(APPROVE_URL, shopId, data.getId()), "POST");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write("{}".getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 204) {
                notifySuccess(data);
            } else if (Config.Settings.debug) {
                plugin.getLogger().warning("Failed to approve ID " + data.getId() + ". Code: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error approving purchase " + data.getId());
        }
    }

    private void notifySuccess(GetResult data) {
        String defaultMsg = "✅ Пришёл платёж: ID {id}\n\n👤 Ник: {user}\n🚪 Товар: {product} (x{amount})\n🔥 Сумма: {price} руб";
        String msg = plugin.getConfig().getString("messengers.format", defaultMsg)
                .replace("{id}", String.valueOf(data.getId()))
                .replace("{user}", data.getNickname())
                .replace("{product}", data.getName())
                .replace("{amount}", String.valueOf(data.getAmount()))
                .replace("{price}", String.valueOf(data.getPrice()))
                .replace("\\n", "\n");

        plugin.getMessengersManager().getConnectedMessengers().forEach(m -> m.sendMessage(msg));
        plugin.getAnnouncementsManager().process(data);
        plugin.getLocalStorage().addPayment(data);
        
        plugin.getServer().getScheduler().runTask(plugin, () -> 
            plugin.getServer().getPluginManager().callEvent(new PurchaseApproveEvent(data))
        );
    }

    private HttpURLConnection createConnection(String urlStr, String method) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("X-Shop-Key", shopKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        return conn;
    }

    public void setRunnable(BukkitRunnable runnable) {
        this.runnable = runnable;
    }
}