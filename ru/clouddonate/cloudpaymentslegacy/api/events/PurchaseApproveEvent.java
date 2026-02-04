package ru.clouddonate.cloudpaymentslegacy.api.events;

import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.clouddonate.cloudpaymentslegacy.http.GetResult;

public final class PurchaseApproveEvent extends Event {
   public static HandlerList handlerList = new HandlerList();
   private final GetResult result;

   public PurchaseApproveEvent(GetResult result) {
      this.result = result;
   }

   public HandlerList getHandlers() {
      return handlerList;
   }

   @Generated
   public GetResult getResult() {
      return this.result;
   }
}
