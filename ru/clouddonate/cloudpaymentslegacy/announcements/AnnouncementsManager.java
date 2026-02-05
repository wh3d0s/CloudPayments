package ru.clouddonate.cloudpaymentslegacy.announcements;

import java.util.ArrayList;
import java.util.List;
import ru.clouddonate.cloudpaymentslegacy.CloudPayments;
import ru.clouddonate.cloudpaymentslegacy.api.Manager;
import ru.clouddonate.cloudpaymentslegacy.config.Config;
import ru.clouddonate.cloudpaymentslegacy.http.GetResult;

public class AnnouncementsManager extends Manager {
   private final List<Announcement> announcements = new ArrayList();

   public AnnouncementsManager(CloudPayments cloudPayments) {
      super(cloudPayments);
      this.reload();
   }

   public void reload() {
      this.announcements.clear();
      this.getCloudPayments().getConfig().getConfigurationSection("in-game-announcements").getKeys(false).forEach((key) -> {
         this.announcements.add(this.build(key, this.getCloudPayments().getConfig().getStringList("in-game-announcements." + key)));
      });
   }

   public Announcement build(String productName, List<String> actions) {
      Announcement announcement = new Announcement(productName);
      actions.forEach((action) -> {
         String message;
         if (action.startsWith("{CHAT_MESSAGE}")) {
            message = action.split("\\{CHAT_MESSAGE}")[1];
            announcement.getActions().put(Config.format(message), AnnounceEnum.CHAT_MESSAGE);
         } else if (action.startsWith("{TITLE_MESSAGE}")) {
            message = action.split("\\{TITLE_MESSAGE}")[1];
            announcement.getActions().put(Config.format(message), AnnounceEnum.TITLE_MESSAGE);
         } else if (action.startsWith("{ACTIONBAR_MESSAGE}")) {
            message = action.split("\\{ACTIONBAR_MESSAGE}")[1];
            announcement.getActions().put(Config.format(message), AnnounceEnum.ACTIONBAR_MESSAGE);
         } else if (action.startsWith("{COMMAND}")) {
            message = action.split("\\{COMMAND}")[1];
            announcement.getActions().put(Config.format(message), AnnounceEnum.COMMAND);
         } else if (action.startsWith("{SOUND}")) {
            message = action.split("\\{SOUND}")[1];
            announcement.getActions().put(Config.format(message), AnnounceEnum.SOUND);
         }

      });
      return announcement;
   }

   public void process(GetResult result) {
      this.announcements.forEach((announcement) -> {
         if (announcement.getProductName().equals(result.getName())) {
            announcement.process(result);
         }

      });
   }
}
