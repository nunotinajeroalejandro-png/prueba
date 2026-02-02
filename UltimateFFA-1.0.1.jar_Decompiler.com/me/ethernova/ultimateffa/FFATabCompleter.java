package me.ethernova.ultimateffa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class FFATabCompleter implements TabCompleter {
   private final Main plugin;

   public FFATabCompleter(Main plugin) {
      this.plugin = plugin;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      String sub;
      if (command.getName().equalsIgnoreCase("1v1")) {
         if (args.length == 1) {
            return (List)StringUtil.copyPartialMatches(args[0], Arrays.asList("invite", "accept", "deny", "leave", "spectate"), new ArrayList());
         } else {
            if (args.length == 2) {
               sub = args[0].toLowerCase();
               if (Arrays.asList("invite", "accept", "deny", "spectate").contains(sub)) {
                  return null;
               }
            }

            return Collections.emptyList();
         }
      } else {
         if (command.getName().equalsIgnoreCase("ffa")) {
            if (args.length == 1) {
               List<String> subs = new ArrayList(Arrays.asList("join", "leave", "lobby", "shop", "stats", "top", "settings", "missions", "daily", "bounty", "prestige", "spectate", "help"));
               if (sender.hasPermission("ffa.admin")) {
                  subs.addAll(Arrays.asList("admin", "reload", "set", "createarena", "deletearena", "setkit", "kitsettings", "addmoney", "setmoney", "resetplayer"));
               }

               return (List)StringUtil.copyPartialMatches(args[0], subs, new ArrayList());
            }

            sub = args[0].toLowerCase();
            if (args.length == 2) {
               if ((sub.equals("join") || sub.equals("deletearena") || sub.equals("tparena")) && this.plugin.arenasConfig.contains("ffa")) {
                  return (List)StringUtil.copyPartialMatches(args[1], new ArrayList(this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false)), new ArrayList());
               }

               if (sub.equals("spectate") || sub.equals("addmoney") || sub.equals("setmoney") || sub.equals("resetplayer")) {
                  return null;
               }

               if (sub.equals("bounty")) {
                  return null;
               }

               if (sub.equals("set") && sender.hasPermission("ffa.admin")) {
                  return (List)StringUtil.copyPartialMatches(args[1], Arrays.asList("lobby", "spawn", "allowbuild", "duelspawn"), new ArrayList());
               }

               if (sub.equals("kitsettings") && sender.hasPermission("ffa.admin")) {
                  return (List)StringUtil.copyPartialMatches(args[1], Collections.singletonList("build"), new ArrayList());
               }

               if (sub.equals("createarena") && sender.hasPermission("ffa.admin")) {
                  return Collections.singletonList("<nombre>");
               }

               if (sub.equals("setkit") && sender.hasPermission("ffa.admin")) {
                  List<String> suggestions = new ArrayList();
                  if (this.plugin.arenasConfig.contains("ffa")) {
                     suggestions.addAll(this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false));
                  }

                  if (this.plugin.kitsConfig.contains("kits")) {
                     suggestions.addAll(this.plugin.kitsConfig.getConfigurationSection("kits").getKeys(false));
                  }

                  return (List)StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList());
               }
            }

            if (args.length == 3) {
               if (sub.equals("set") && (args[1].equalsIgnoreCase("spawn") || args[1].equalsIgnoreCase("allowbuild")) && this.plugin.arenasConfig.contains("ffa")) {
                  return (List)StringUtil.copyPartialMatches(args[2], new ArrayList(this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false)), new ArrayList());
               }

               if (sub.equals("set") && args[1].equalsIgnoreCase("duelspawn")) {
                  if (this.plugin.arenasConfig.contains("duels")) {
                     return (List)StringUtil.copyPartialMatches(args[2], this.plugin.arenasConfig.getConfigurationSection("duels").getKeys(false), new ArrayList());
                  }

                  return Collections.singletonList("<NombreArena>");
               }

               if (sub.equals("kitsettings") && args[1].equalsIgnoreCase("build") && this.plugin.kitsConfig.contains("kits")) {
                  return (List)StringUtil.copyPartialMatches(args[2], new ArrayList(this.plugin.kitsConfig.getConfigurationSection("kits").getKeys(false)), new ArrayList());
               }

               if (sub.equals("bounty")) {
                  return (List)StringUtil.copyPartialMatches(args[2], Arrays.asList("100", "500", "1000", "5000"), new ArrayList());
               }
            }

            if (args.length == 4) {
               if (sub.equals("set") && args[1].equalsIgnoreCase("duelspawn")) {
                  return (List)StringUtil.copyPartialMatches(args[3], Arrays.asList("1", "2"), new ArrayList());
               }

               if (sub.equals("set") && args[1].equalsIgnoreCase("allowbuild")) {
                  return (List)StringUtil.copyPartialMatches(args[3], Arrays.asList("true", "false"), new ArrayList());
               }

               if (sub.equals("kitsettings") && args[1].equalsIgnoreCase("build")) {
                  return (List)StringUtil.copyPartialMatches(args[3], Arrays.asList("true", "false"), new ArrayList());
               }
            }
         }

         return Collections.emptyList();
      }
   }
}
