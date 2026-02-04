package ru.clouddonate.cloudpaymentslegacy.config;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import ru.clouddonate.cloudpaymentslegacy.CloudPayments;

public final class Config {

    public static String format(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void load(CloudPayments plugin) {
        FileConfiguration c = plugin.getConfig();

        Messages.noPermission = format(c.getString("messages.noPermission"));
        Messages.reload = format(c.getString("messages.reload"));
        Messages.debugDisabled = format(c.getString("messages.debug-disabled"));
        Messages.debugEnabled = format(c.getString("messages.debug-enabled"));

        Settings.debug = c.getBoolean("settings.debug-mode");
        Settings.checkUpdates = c.getBoolean("settings.check-updates");
        Settings.requestDelay = c.getLong("settings.request-delay");
        Settings.Shop.shopId = c.getString("settings.shop.shop-id");
        Settings.Shop.shopKey = c.getString("settings.shop.shop-key");
        Settings.Shop.serverId = c.getString("settings.shop.server-id");

        LocalStorage.Payments.enabled = c.getBoolean("local-storage.payments.enabled");
        LocalStorage.Payments.type = c.getString("local-storage.payments.type", "TXT");
        LocalStorage.Payments.format = c.getString("local-storage.payments.format");
        LocalStorage.Statistic.enabled = c.getBoolean("local-storage.statistic.enabled");

        Messengers.Telegram.enabled = c.getBoolean("messengers.telegram.enabled");
        Messengers.Telegram.apiToken = c.getString("messengers.telegram.api-token");
        Messengers.Telegram.ids = c.getStringList("messengers.telegram.ids");
    }

    public static class Messages {
        public static String noPermission, reload, debugDisabled, debugEnabled;
    }

    public static class LocalStorage {
        public static class Payments {
            public static boolean enabled;
            public static String type, format;
        }
        public static class Statistic {
            public static boolean enabled;
        }
    }

    public static class Settings {
        public static boolean debug, checkUpdates;
        public static long requestDelay;
        public static class Shop {
            public static String shopId, shopKey, serverId;
        }
    }

    public static class Messengers {
        public static class Telegram {
            public static boolean enabled;
            public static List<String> ids;
            public static String apiToken;
        }
    }

    private Config() {}
}