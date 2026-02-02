package me.ethernova.ultimateffa;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FFAExpansion extends PlaceholderExpansion {
   private final Main plugin;

   public FFAExpansion(Main plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getIdentifier() {
      return "ultimateffa";
   }

   @NotNull
   public String getAuthor() {
      return "ethernova";
   }

   @NotNull
   public String getVersion() {
      return "1.0.1";
   }

   public boolean persist() {
      return true;
   }

   public String onPlaceholderRequest(Player p, @NotNull String params) {
      if (p == null) {
         return "";
      } else if (params.equalsIgnoreCase("kills")) {
         return String.valueOf(this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills"));
      } else if (params.equalsIgnoreCase("deaths")) {
         return String.valueOf(this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.deaths"));
      } else if (params.equalsIgnoreCase("streak")) {
         String arena = this.plugin.gameManager.getPlayerArena(p);
         return String.valueOf(this.plugin.gameManager.getStreak(p, arena));
      } else if (params.equalsIgnoreCase("rank")) {
         int kills = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills");
         return this.plugin.gameManager.calculateRank(kills);
      } else {
         return params.equalsIgnoreCase("money") ? String.valueOf(this.plugin.cosmeticsManager.getBalance(p)) : null;
      }
   }
}
