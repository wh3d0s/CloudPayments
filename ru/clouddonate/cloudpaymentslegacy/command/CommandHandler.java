package ru.clouddonate.cloudpaymentslegacy.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.clouddonate.cloudpaymentslegacy.CloudPayments;
import ru.clouddonate.cloudpaymentslegacy.config.Config;
import ru.clouddonate.cloudpaymentslegacy.shop.Shop;

public final class CommandHandler implements CommandExecutor, TabCompleter {
    private final CloudPayments plugin;

    public CommandHandler(CloudPayments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("cloudpayments.admin")) {
            sender.sendMessage(Config.format(Config.Messages.noPermission));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Config.format("&9&lCloud&b&lPayments &8— &7Помощь"));
            sender.sendMessage(Config.format("&9/" + label + " reload &8- &7Перезагрузить"));
            sender.sendMessage(Config.format("&9/" + label + " debug <on/off> &8- &7Отладка"));
            sender.sendMessage(Config.format("&9/" + label + " migrate &8- &7Перенос TXT -> H2"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                long start = System.currentTimeMillis();
                plugin.reloadConfig();
                Config.load(plugin);
                plugin.getMessengersManager().reload();
                plugin.getAnnouncementsManager().reload();
                plugin.setShop(new Shop(Config.Settings.Shop.shopId, Config.Settings.Shop.shopKey, Config.Settings.Shop.serverId, Config.Settings.requestDelay, plugin));
                sender.sendMessage(Config.format(Config.Messages.reload.replace("{took}", String.valueOf(System.currentTimeMillis() - start))));
            }
            case "debug" -> {
                if (args.length < 2) return true;
                boolean mode = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("enable");
                Config.Settings.debug = mode;
                plugin.getConfig().set("settings.debug-mode", mode);
                plugin.saveConfig();
                sender.sendMessage(Config.format(mode ? Config.Messages.debugEnabled : Config.Messages.debugDisabled));
            }
            case "migrate" -> {
                sender.sendMessage(Config.format("&9Cloud&bPayments &8- &7Миграция запущена..."));
                int count = plugin.getLocalStorage().migrate();
                sender.sendMessage(Config.format("&9Cloud&bPayments &8- &aУспешно перенесено &f" + count + " &aзаписей."));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("cloudpayments.admin")) return Collections.emptyList();
        if (args.length == 1) return Arrays.asList("debug", "reload", "migrate");
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) return Arrays.asList("on", "off");
        return Collections.emptyList();
    }
}