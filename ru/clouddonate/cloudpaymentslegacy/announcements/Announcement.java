package ru.clouddonate.cloudpaymentslegacy.announcements;

import java.util.LinkedHashMap;
import lombok.Generated;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import ru.clouddonate.cloudpaymentslegacy.CloudPayments;
import ru.clouddonate.cloudpaymentslegacy.config.Config;
import ru.clouddonate.cloudpaymentslegacy.http.GetResult;

public final class Announcement {
   private final String productName;
   private final LinkedHashMap<String, AnnounceEnum> actions = new LinkedHashMap();

   public Announcement(String productName) {
      this.productName = productName;
   }

   public void process(GetResult result) {
      this.getActions().forEach((action, announceEnum) -> {
         switch(announceEnum) {
         case CHAT_MESSAGE:
            Bukkit.broadcastMessage(this.format(action, result));
            break;
         case TITLE_MESSAGE:
            String title = this.format(action.split("::")[0], result);
            String subtitle = this.format(action.split("::")[1], result);
            Bukkit.getOnlinePlayers().forEach((player) -> {
               player.sendTitle(title, subtitle, 20, 20, 20);
            });
            break;
         case ACTIONBAR_MESSAGE:
            Bukkit.getOnlinePlayers().forEach((player) -> {
               player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (new ComponentBuilder(this.format(action, result))).create());
            });
            break;
         case SOUND:
            Sound sound = Sound.valueOf(this.format(action, result));
            Bukkit.getOnlinePlayers().forEach((player) -> {
               if (player.getLocation().getWorld() != null) {
                  player.getLocation().getWorld().playSound(player.getLocation(), sound, 1.0F, 1.0F);
               }

            });
            break;
         case COMMAND:
            Bukkit.getScheduler().runTask(CloudPayments.getPlugin(CloudPayments.class), () -> {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.format(action, result));
            });
         }

      });
   }

   private String format(String msg, GetResult result) {
      return Config.format(msg).replaceAll("<nickname>", result.getNickname()).replaceAll("<price>", String.valueOf(result.getPrice())).replaceAll("<product>", result.getName()).replaceAll("<count>", String.valueOf(result.getAmount()));
   }

   @Generated
   public String getProductName() {
      return this.productName;
   }

   @Generated
   public LinkedHashMap<String, AnnounceEnum> getActions() {
      return this.actions;
   }
}
