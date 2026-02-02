package me.ethernova.ultimateffa;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class QueueListener implements Listener {
   private final Main plugin;

   public QueueListener(Main plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onQueueItemClick(PlayerInteractEvent e) {
      Player p = e.getPlayer();
      ItemStack item = e.getItem();
      Action action = e.getAction();
      if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && item != null && item.getType() == Material.RED_DYE && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Cancelar Búsqueda")) {
         e.setCancelled(true);
         this.plugin.gameManager.removeFromQueues(p);
         p.sendMessage(ChatColor.YELLOW + "Has cancelado la búsqueda.");
      }

   }
}
