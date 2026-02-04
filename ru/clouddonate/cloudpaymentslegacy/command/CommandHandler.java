package ru.clouddonate.cloudpaymentslegacy.command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import ru.clouddonate.cloudpaymentslegacy.CloudPayments;
import ru.clouddonate.cloudpaymentslegacy.config.Config;
import ru.clouddonate.cloudpaymentslegacy.shop.Shop;

public final class CommandHandler implements CommandExecutor, TabCompleter {
    private final CloudPayments plugin;
    private final Pattern hexPattern = Pattern.compile("^[a-f0-9]+$");

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
            sender.sendMessage(Config.format("&9/" + label + " setup &8- &7Мастер настройки"));
            sender.sendMessage(Config.format("&9/" + label + " reload &8- &7Перезагрузить"));
            sender.sendMessage(Config.format("&9/" + label + " debug <on/off> &8- &7Отладка"));
            sender.sendMessage(Config.format("&9/" + label + " migrate &8- &7Перенос TXT -> H2"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setup" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Мастер настройки доступен только игрокам.");
                    return true;
                }
                startSetup((Player) sender);
            }
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

    private void startSetup(Player player) {
        player.sendMessage(Config.Messages.Setup.start);
        
        ConversationFactory factory = new ConversationFactory(plugin)
                .withModality(true)
                .withLocalEcho(false)
                .withEscapeSequence("cancel")
                .withFirstPrompt(new ShopIdPrompt());
        
        factory.buildConversation(player).begin();
    }

    private class ShopIdPrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) { return Config.Messages.Setup.enterShopId; }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData("sid", input);
            return new ShopKeyPrompt();
        }
    }

    private class ShopKeyPrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) { return Config.Messages.Setup.enterShopKey; }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.length() < 8 || input.length() > 64) {
                context.getForWhom().sendRawMessage(Config.Messages.Setup.wrongKeyLength); 
                return this;
            }
            if (!hexPattern.matcher(input).matches()) {
                context.getForWhom().sendRawMessage(Config.Messages.Setup.wrongKeyRegex);
                return this;
            }
            context.setSessionData("skey", input);
            return new ServerIdPrompt();
        }
    }


    private class ServerIdPrompt extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) { return Config.Messages.Setup.enterServerId; }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            try {
                if (Integer.parseInt(input) <= 0) throw new NumberFormatException();

                String shopId = (String) context.getSessionData("sid");
                String shopKey = (String) context.getSessionData("skey");

                saveConfigSafe(shopId, shopKey, input);

                plugin.reloadConfig();
                Config.load(plugin);
                plugin.setShop(new Shop(shopId, shopKey, input, Config.Settings.requestDelay, plugin));

                context.getForWhom().sendRawMessage(Config.Messages.Setup.finish);
                return Prompt.END_OF_CONVERSATION;
            } catch (NumberFormatException e) {
                context.getForWhom().sendRawMessage(Config.Messages.Setup.wrongServerId);
                return this;
            }
        }
    }


    private void saveConfigSafe(String shopId, String shopKey, String serverId) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            List<String> newLines = new ArrayList<>();

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("shop-id:")) {
                    newLines.add(line.split(":")[0] + ": \"" + shopId + "\"");
                } else if (trimmed.startsWith("shop-key:")) {
                    newLines.add(line.split(":")[0] + ": \"" + shopKey + "\"");
                } else if (trimmed.startsWith("server-id:")) {
                    newLines.add(line.split(":")[0] + ": \"" + serverId + "\"");
                } else {
                    newLines.add(line);
                }
            }
            Files.write(file.toPath(), newLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("cloudpayments.admin")) return Collections.emptyList();
        if (args.length == 1) return Arrays.asList("setup", "reload", "debug", "migrate");
        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) return Arrays.asList("on", "off");
        return Collections.emptyList();
    }
}