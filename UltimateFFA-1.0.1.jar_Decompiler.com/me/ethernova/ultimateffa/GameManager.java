package me.ethernova.ultimateffa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class GameManager {
   private final Main plugin;
   public Map<UUID, Integer> tempBets = new HashMap();
   public Map<UUID, Integer> activeBets = new HashMap();
   public Map<UUID, List<BlockState>> duelBlockCache = new HashMap();
   public HashMap<UUID, Long> combatTag = new HashMap();
   public Map<UUID, UUID> lastDamager = new HashMap();
   public Map<UUID, UUID> pendingInvite = new HashMap();
   public Map<UUID, String> playerState = new HashMap();
   public Map<UUID, Long> combatLog = new HashMap();
   public Map<UUID, Integer> lobbySwordTimer = new HashMap();
   public Map<UUID, Map<String, Integer>> arenaStreaks = new HashMap();
   public Map<UUID, Long> spawnProtection = new HashMap();
   public Set<UUID> buildMode = new HashSet();
   public Set<UUID> jumpCooldown = new HashSet();
   public Map<String, LinkedList<UUID>> kitQueues = new HashMap();
   public Map<UUID, GameManager.InviteData> pendingInvites = new HashMap();
   public Map<UUID, String> inviteKit = new HashMap();
   public List<GameManager.DuelMatch> activeDuels = new ArrayList();
   public Map<UUID, UUID> duelRequests = new HashMap();
   public Map<UUID, String> duelKitRequests = new HashMap();
   public Map<UUID, String> editingKit = new HashMap();
   public Map<UUID, Integer> bounties = new HashMap();
   public Map<UUID, List<GameManager.Mission>> activeMissionsList = new HashMap();
   public Map<UUID, UUID> spectatorTarget = new HashMap();
   public Map<UUID, GameManager.PlayerSettings> playerSettings = new HashMap();

   public GameManager(Main plugin) {
      this.plugin = plugin;
   }

   public void startGlobalTasks() {
      (new BukkitRunnable() {
         public void run() {
            Iterator var1 = Bukkit.getOnlinePlayers().iterator();

            while(var1.hasNext()) {
               Player p = (Player)var1.next();
               GameManager.this.updateScoreboard(p);
               GameManager.this.handleCombatLog(p);
               GameManager.this.handleLobbySword(p);
               if (System.currentTimeMillis() / 1000L % 60L == 0L) {
                  String globalPath = "players." + p.getUniqueId() + ".playtime";
                  GameManager.this.plugin.statsConfig.set(globalPath, GameManager.this.plugin.statsConfig.getInt(globalPath, 0) + 1);
                  String state = (String)GameManager.this.playerState.get(p.getUniqueId());
                  if (state != null && state.startsWith("FFA:")) {
                     String arena = state.split(":")[1];
                     String arenaPath = "players." + p.getUniqueId() + "." + arena + ".playtime";
                     GameManager.this.plugin.statsConfig.set(arenaPath, GameManager.this.plugin.statsConfig.getInt(arenaPath, 0) + 1);
                  }
               }
            }

         }
      }).runTaskTimer(this.plugin, 0L, 20L);
      (new BukkitRunnable() {
         public void run() {
            Iterator var1 = Bukkit.getOnlinePlayers().iterator();

            while(var1.hasNext()) {
               Player p = (Player)var1.next();
               GameManager.this.runAuraLogic(p);
               GameManager.this.runPetFollowLogic(p);
            }

         }
      }).runTaskTimer(this.plugin, 0L, 5L);
      (new BukkitRunnable() {
         public void run() {
            Iterator var1 = Bukkit.getOnlinePlayers().iterator();

            while(true) {
               Player p;
               String t;
               do {
                  do {
                     if (!var1.hasNext()) {
                        return;
                     }

                     p = (Player)var1.next();
                  } while(p.getOpenInventory() == null);

                  t = ChatColor.stripColor(p.getOpenInventory().getTitle()).toUpperCase();
               } while(!t.contains("TIENDA") && !t.contains("ADMIN") && !t.contains("GESTIÓN") && !t.contains("ESTADÍSTICAS") && !t.contains("RANKING") && !t.contains("SISTEMA") && !t.contains("TOP") && !t.contains("MASCOTAS") && !t.contains("VARIANTES") && !t.contains("SELECTOR") && !t.contains("ARMERÍA") && !t.contains("PRESTIGIO") && !t.contains("DISEÑO") && !t.contains("COLOR") && !t.contains("MATERIAL") && !t.contains("EDITOR") && !t.contains("EDITANDO") && !t.contains("DUELOS") && !t.contains("COLA") && !t.contains("MISIONES") && !t.contains("CALENDARIO") && !t.contains("BORRAR") && !t.contains("ARENAS") && !t.contains("AJUSTES"));

               GameManager.this.plugin.cosmeticsManager.fillDiscoGlass(p.getOpenInventory().getTopInventory());
            }
         }
      }).runTaskTimer(this.plugin, 0L, 10L);
      this.startFFAMapResetTask();
   }

   public void startFFAMapResetTask() {
      (new BukkitRunnable() {
         public void run() {
            if (GameManager.this.plugin.arenasConfig.contains("ffa")) {
               Iterator var1 = GameManager.this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

               while(var1.hasNext()) {
                  String arena = (String)var1.next();
                  if (GameManager.this.plugin.arenasConfig.getBoolean("ffa." + arena + ".allowBuild")) {
                     GameManager.this.checkAndResetArena(arena);
                  }
               }

            }
         }
      }).runTaskTimer(this.plugin, 12000L, 12000L);
   }

   private void checkAndResetArena(final String arena) {
      if (this.plugin.restoreManager.hasChanges(arena)) {
         Iterator var2 = Bukkit.getOnlinePlayers().iterator();

         while(var2.hasNext()) {
            Player p = (Player)var2.next();
            if (arena.equals(this.getPlayerArena(p))) {
               p.sendMessage("§c⚠ §lLA ARENA SE REGENERARÁ EN 5 SEGUNDOS ⚠");
               p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.5F);
            }
         }

         (new BukkitRunnable() {
            public void run() {
               GameManager.this.plugin.restoreManager.restoreArena(arena);
               GameManager.this.cleanArenaEntities(arena);
               Iterator var1 = Bukkit.getOnlinePlayers().iterator();

               while(var1.hasNext()) {
                  Player p = (Player)var1.next();
                  if (arena.equals(GameManager.this.getPlayerArena(p))) {
                     p.sendMessage("§a✔ Arena regenerada.");
                  }
               }

            }
         }).runTaskLater(this.plugin, 100L);
      }

   }

   public boolean toggleBuildMode(Player p) {
      if (this.buildMode.contains(p.getUniqueId())) {
         this.buildMode.remove(p.getUniqueId());
         return false;
      } else {
         this.buildMode.add(p.getUniqueId());
         return true;
      }
   }

   public void sendToLobby(Player p) {
      this.spectatorTarget.remove(p.getUniqueId());
      this.combatTag.remove(p.getUniqueId());
      this.lastDamager.remove(p.getUniqueId());
      this.lobbySwordTimer.remove(p.getUniqueId());
      Location lobby = this.plugin.getConfig().getLocation("lobby");
      if (lobby == null) {
         if (p.isOp()) {
            p.sendMessage(ChatColor.RED + "¡No hay lobby! Usa /ffa set lobby");
         }

      } else {
         p.teleport(lobby);
         p.setGameMode(GameMode.ADVENTURE);
         p.setHealth(20.0D);
         p.setFoodLevel(20);
         p.setSaturation(20.0F);
         p.setFireTicks(0);
         Iterator var3 = p.getActivePotionEffects().iterator();

         while(var3.hasNext()) {
            PotionEffect eff = (PotionEffect)var3.next();
            p.removePotionEffect(eff.getType());
         }

         p.getInventory().clear();
         p.getInventory().setArmorContents((ItemStack[])null);
         p.setAllowFlight(true);
         p.setFlying(false);
         p.setInvisible(false);
         var3 = Bukkit.getOnlinePlayers().iterator();

         while(var3.hasNext()) {
            Player on = (Player)var3.next();
            on.showPlayer(this.plugin, p);
         }

         this.playerState.put(p.getUniqueId(), "LOBBY");
         this.giveLobbyItems(p);
         this.plugin.cosmeticsManager.restorePet(p);
         this.plugin.cosmeticsManager.applyActiveTrim(p);
      }
   }

   public void giveLobbyItems(Player p) {
      if ("LOBBY".equals(this.playerState.get(p.getUniqueId()))) {
         p.getInventory().clear();
         p.getInventory().setItem(0, this.createItem(Material.DIAMOND_SWORD, "§aPvP Mode §7(Mantén)"));
         p.getInventory().setItem(1, this.createItem(Material.EMERALD, "§aTienda de Cosméticos"));
         p.getInventory().setItem(3, this.createItem(Material.WRITABLE_BOOK, "§eMisiones Diarias"));
         p.getInventory().setItem(4, this.createItem(Material.COMPASS, "§aArenas FFA"));
         p.getInventory().setItem(5, this.createItem(Material.CLOCK, "§6Calendario de Premios"));
         p.getInventory().setItem(7, this.createItem(Material.NETHER_STAR, "§6Ranking & Stats"));
         p.getInventory().setItem(8, this.createItem(Material.ENDER_EYE, "§eEspectar"));
      }
   }

   public void joinFFA(Player p, String arena) {
      Location spawn = this.plugin.arenasConfig.getLocation("ffa." + arena + ".spawn");
      if (spawn != null) {
         this.plugin.cosmeticsManager.removePet(p);
         p.getInventory().clear();
         p.getInventory().setArmorContents((ItemStack[])null);
         p.teleport(spawn);
         this.plugin.gameManager.playerState.put(p.getUniqueId(), "FFA:" + arena);
         p.setHealth(20.0D);
         p.setFoodLevel(20);
         p.setFireTicks(0);
         Iterator var4 = p.getActivePotionEffects().iterator();

         while(var4.hasNext()) {
            PotionEffect pe = (PotionEffect)var4.next();
            p.removePotionEffect(pe.getType());
         }

         this.applyKit(p, arena);
         this.spawnProtection.put(p.getUniqueId(), System.currentTimeMillis() + 3000L);
         p.sendTitle("§b§l" + arena.toUpperCase(), "§e⚔ ¡Que comience la carnicería! ⚔", 10, 30, 10);
         p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.5F);
         p.sendMessage(this.plugin.msg("prefix") + "Has aterrizado en §6§l" + arena);
         p.sendMessage("§7\ud83d\udee1 Tienes 3 segundos de protección.");
         p.setGameMode(GameMode.SURVIVAL);
      }
   }

   public void applyKit(Player p, String arenaOrKitName) {
      String kitName = "default";
      if (this.plugin.arenasConfig.contains("ffa." + arenaOrKitName + ".kit")) {
         kitName = this.plugin.arenasConfig.getString("ffa." + arenaOrKitName + ".kit");
      } else if (this.plugin.kitsConfig.contains("kits." + arenaOrKitName)) {
         kitName = arenaOrKitName;
      }

      String path = "kits." + kitName;
      List list;
      ItemStack[] armor;
      if (this.plugin.kitsConfig.contains(path + ".inventory")) {
         list = this.plugin.kitsConfig.getList(path + ".inventory");
         if (list != null) {
            armor = (ItemStack[])list.toArray(new ItemStack[0]);
            p.getInventory().setContents(armor);
         }
      }

      if (this.plugin.kitsConfig.contains(path + ".armor")) {
         list = this.plugin.kitsConfig.getList(path + ".armor");
         if (list != null) {
            armor = (ItemStack[])list.toArray(new ItemStack[0]);
            p.getInventory().setArmorContents(armor);
         }
      } else {
         p.getInventory().setArmorContents((ItemStack[])null);
      }

      String realArena = this.getPlayerArena(p);
      if (realArena == null && this.plugin.arenasConfig.contains("ffa." + arenaOrKitName)) {
         realArena = arenaOrKitName;
      }

      if (realArena != null) {
         boolean hasFire = this.plugin.statsConfig.getBoolean("players." + p.getUniqueId() + "." + realArena + ".fire_unlock", false);
         if (hasFire) {
            ItemStack[] var7 = p.getInventory().getContents();
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               ItemStack item = var7[var9];
               if (item != null && item.getType().name().contains("SWORD")) {
                  item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
               }
            }

            p.sendMessage("§6\ud83d\udd25 Aspecto Ígneo II aplicado (Recompensa de Racha 50) \ud83d\udd25");
         }
      }

      this.plugin.cosmeticsManager.applyActiveTrim(p);
      p.updateInventory();
   }

   public void joinQueue(Player p, String kit) {
      UUID id = p.getUniqueId();
      String finalKit = kit;
      if (kit.equalsIgnoreCase("Random")) {
         Iterator var5 = this.kitQueues.keySet().iterator();

         while(var5.hasNext()) {
            String kitName = (String)var5.next();
            if (!((LinkedList)this.kitQueues.get(kitName)).isEmpty() && !kitName.equalsIgnoreCase("Random")) {
               finalKit = kitName;
               break;
            }
         }
      } else {
         LinkedList<UUID> randomQueue = (LinkedList)this.kitQueues.get("Random");
         if (randomQueue != null && !randomQueue.isEmpty()) {
            UUID opponentId = (UUID)randomQueue.poll();
            Player opponent = Bukkit.getPlayer(opponentId);
            if (opponent != null && opponent.isOnline()) {
               this.startDuel(p, opponent, kit);
               return;
            }
         }
      }

      ((LinkedList)this.kitQueues.computeIfAbsent(finalKit, (k) -> {
         return new LinkedList();
      })).add(id);
      this.playerState.put(id, "QUEUE");
      p.getInventory().clear();
      this.giveQueueItems(p);
      p.sendMessage(ChatColor.GREEN + "Buscando partida (Kit: " + ChatColor.YELLOW + finalKit + "§a)...");
      this.checkQueue(finalKit);
   }

   public void removeFromQueues(Player p) {
      Iterator var2 = this.kitQueues.values().iterator();

      while(var2.hasNext()) {
         LinkedList<UUID> q = (LinkedList)var2.next();
         q.remove(p.getUniqueId());
      }

      p.getInventory().clear();
      this.giveLobbyItems(p);
      this.playerState.put(p.getUniqueId(), "LOBBY");
      this.plugin.cosmeticsManager.restorePet(p);
   }

   private void checkQueue(String kit) {
      LinkedList<UUID> q = (LinkedList)this.kitQueues.get(kit);
      if (q != null && q.size() >= 2) {
         Player p1 = Bukkit.getPlayer((UUID)q.poll());
         Player p2 = Bukkit.getPlayer((UUID)q.poll());
         String kitToUse = kit;
         if (kit.equalsIgnoreCase("Random")) {
            kitToUse = this.getRandomKitName();
         }

         if (p1 != null && p2 != null) {
            this.startDuel(p1, p2, kitToUse);
         }
      }

   }

   private void startCountdownTask(final Player p1, final Player p2, final GameManager.DuelMatch m) {
      (new BukkitRunnable() {
         int count = 3;

         public void run() {
            if (this.count > 0) {
               String color = this.count == 1 ? "§a" : (this.count == 2 ? "§e" : "§c");
               p1.sendTitle(color + this.count, "§7Ronda " + (m.p1Wins + m.p2Wins + 1), 0, 20, 0);
               p2.sendTitle(color + this.count, "§7Ronda " + (m.p1Wins + m.p2Wins + 1), 0, 20, 0);
               p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F);
               p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F);
               --this.count;
            } else {
               p1.sendTitle("§6§l¡A PELEAR!", "", 0, 10, 10);
               p2.sendTitle("§6§l¡A PELEAR!", "", 0, 10, 10);
               p1.playSound(p1.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
               p2.playSound(p2.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
               this.cancel();
            }

         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   public void endDuel(GameManager.DuelMatch match, Player winner, Player loser) {
      this.cleanArenaEntities(match.arenaName);
      if (!match.ended) {
         match.ended = true;
         if (match.taskId != -1) {
            Bukkit.getScheduler().cancelTask(match.taskId);
         }

         this.activeDuels.remove(match);
         this.combatTag.remove(match.p1);
         this.combatTag.remove(match.p2);
         this.lastDamager.remove(match.p1);
         this.lastDamager.remove(match.p2);
         this.plugin.restoreManager.restoreArena(match.arenaName);
         if (winner != null) {
            winner.setHealth(20.0D);
            winner.setFoodLevel(20);
            winner.sendMessage(this.plugin.msg("duel.victory"));
            winner.sendTitle("§6§lVICTORIA", "§eHas ganado el duelo", 10, 60, 20);
            winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            int prize = match.betAmount > 0 ? match.betAmount * 2 : 20;
            this.plugin.cosmeticsManager.addMoney(winner, prize);
            if (match.betAmount > 0) {
               winner.sendMessage("§a¡Has ganado el bote de $" + prize + "!");
            }

            this.addMissionProgress(winner, GameManager.MissionType.GLADIADOR, 1);
         }

         if (loser == null && winner != null) {
            UUID loserId = match.p1.equals(winner.getUniqueId()) ? match.p2 : match.p1;
            loser = Bukkit.getPlayer(loserId);
         }

         if (loser != null && loser.isOnline()) {
            loser.sendMessage(this.plugin.msg("duel.defeat"));
            loser.sendTitle("§c§lDERROTA", "§7Suerte la próxima", 10, 60, 20);
            if (match.betAmount > 0) {
               loser.sendMessage("§cPerdiste tu apuesta de $" + match.betAmount);
            }
         }

         Player p1 = Bukkit.getPlayer(match.p1);
         Player p2 = Bukkit.getPlayer(match.p2);
         if (p1 != null && p1.isOnline()) {
            this.sendToLobby(p1);
         }

         if (p2 != null && p2.isOnline()) {
            this.sendToLobby(p2);
         }

         List<UUID> toRemove = new ArrayList();
         Iterator var7 = this.spectatorTarget.entrySet().iterator();

         while(true) {
            Entry entry;
            do {
               if (!var7.hasNext()) {
                  var7 = toRemove.iterator();

                  while(var7.hasNext()) {
                     UUID specId = (UUID)var7.next();
                     Player spec = Bukkit.getPlayer(specId);
                     if (spec != null) {
                        spec.sendMessage("§cEl duelo ha terminado.");
                        this.sendToLobby(spec);
                     }
                  }

                  return;
               }

               entry = (Entry)var7.next();
            } while(!((UUID)entry.getValue()).equals(match.p1) && !((UUID)entry.getValue()).equals(match.p2));

            toRemove.add((UUID)entry.getKey());
         }
      }
   }

   public void openArenasGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§3§lARENAS §8» §fSelección");
      if (this.plugin.arenasConfig.contains("ffa")) {
         Iterator var3 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

         while(var3.hasNext()) {
            String key = (String)var3.next();
            ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + key);
            item.setItemMeta(meta);
            inv.addItem(new ItemStack[]{item});
         }
      }

      inv.setItem(22, this.createItem(Material.ARROW, "§c« Volver"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openRankingMainMenu(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§6§lSISTEMA DE RANKING");
      ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta)skull.getItemMeta();
      meta.setOwningPlayer(p);
      meta.setDisplayName("§e§lTU PERFIL");
      meta.setLore(Arrays.asList("§7Haz clic para ver", "§7tus estadísticas detalladas."));
      skull.setItemMeta(meta);
      inv.setItem(11, skull);
      inv.setItem(13, this.createItem(Material.BEACON, "§b§lTOP GLOBAL KILLS"));
      inv.setItem(15, this.createItem(Material.DIAMOND_SWORD, "§a§lTOP POR ARENAS"));
      inv.setItem(22, this.createItem(Material.ARROW, "§cCerrar"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openPersonalStats(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 45, "§e§lESTADÍSTICAS: " + p.getName());
      int gKills = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills");
      int gDeaths = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.deaths");
      double kdr = gDeaths == 0 ? (double)gKills : (double)gKills / (double)gDeaths;
      int playtime = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".playtime", 0);
      ItemStack global = new ItemStack(Material.NETHER_STAR);
      ItemMeta gm = global.getItemMeta();
      gm.setDisplayName("§6§lGLOBALES");
      gm.setLore(Arrays.asList("§7Kills: §a" + gKills, "§7Muertes: §c" + gDeaths, "§7KDR: §e" + String.format("%.2f", kdr), "§7Tiempo: §b" + this.formatTime(playtime), "§7Rango: " + this.calculateRank(gKills)));
      global.setItemMeta(gm);
      inv.setItem(4, global);
      int slot;
      Iterator var11;
      String a;
      int ak;
      int ad;
      ItemStack i;
      ItemMeta im;
      if (this.plugin.arenasConfig.contains("ffa")) {
         slot = 19;
         var11 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

         while(var11.hasNext()) {
            a = (String)var11.next();
            if (slot >= 44) {
               break;
            }

            ak = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + "." + a + ".kills");
            ad = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + "." + a + ".deaths");
            i = this.createItem(Material.PAPER, "§aArena: " + a);
            im = i.getItemMeta();
            im.setLore(Arrays.asList("§7Kills: " + ak, "§7Muertes: " + ad));
            i.setItemMeta(im);
            inv.setItem(slot++, i);
         }
      }

      inv.setItem(40, this.createItem(Material.ARROW, "§cVolver al Ranking"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
      if (this.plugin.arenasConfig.contains("ffa")) {
         slot = 19;
         var11 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

         while(var11.hasNext()) {
            a = (String)var11.next();
            if (slot >= 44) {
               break;
            }

            ak = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + "." + a + ".kills");
            ad = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + "." + a + ".deaths");
            i = this.createItem(Material.PAPER, "§aArena: " + a);
            im = i.getItemMeta();
            im.setLore(Arrays.asList("§7Kills: " + ak, "§7Muertes: " + ad));
            i.setItemMeta(im);
            inv.setItem(slot++, i);
         }
      }

      inv.setItem(40, this.createItem(Material.ARROW, "§cVolver al Ranking"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openAdminGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§c§lADMIN §8» §fPanel de Control");
      ItemStack info = this.createItem(Material.PAPER, ChatColor.YELLOW + "Estado del Servidor");
      ItemMeta im = info.getItemMeta();
      long max = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
      long total = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
      im.setLore(Arrays.asList(ChatColor.GRAY + "Players: " + Bukkit.getOnlinePlayers().size(), ChatColor.GRAY + "RAM: " + total + "MB / " + max + "MB"));
      info.setItemMeta(im);
      inv.setItem(4, info);
      inv.setItem(10, this.createItem(Material.MAP, ChatColor.GOLD + "Gestor de Arenas"));
      inv.setItem(11, this.createItem(Material.CHEST, ChatColor.GOLD + "Editor de Kits"));
      inv.setItem(13, this.createItem(Material.BEACON, ChatColor.AQUA + "Reload Plugin"));
      inv.setItem(15, this.createItem(Material.BARRIER, ChatColor.RED + "Resetear Stats"));
      inv.setItem(16, this.createItem(Material.PLAYER_HEAD, ChatColor.LIGHT_PURPLE + "Gestión de Usuarios"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openPlayerEditor(Player p, String targetName) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "Administrar: " + targetName);
      ItemStack money = this.createItem(Material.EMERALD, "§aDar $1000");
      ItemMeta mm = money.getItemMeta();
      mm.setLore(Collections.singletonList("§7Añade fondos al jugador."));
      money.setItemMeta(mm);
      inv.setItem(11, money);
      ItemStack reset = this.createItem(Material.TNT, "§cReset Stats");
      ItemMeta rm = reset.getItemMeta();
      rm.setLore(Collections.singletonList("§7Borra todas las estadísticas."));
      reset.setItemMeta(rm);
      inv.setItem(13, reset);
      ItemStack tp = this.createItem(Material.COMPASS, "§bTeletransportarse");
      ItemMeta tm = tp.getItemMeta();
      tm.setLore(Collections.singletonList("§7Ir a la posición del jugador."));
      tp.setItemMeta(tm);
      inv.setItem(15, tp);
      inv.setItem(22, this.createItem(Material.ARROW, "§cVolver"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openKitSelector(Player p, boolean isDirectChallenge) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, isDirectChallenge ? "Kit para Reto" : "Kit para Cola");
      inv.setItem(0, this.createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Random"));
      if (this.plugin.kitsConfig.contains("kits")) {
         Iterator var4 = this.plugin.kitsConfig.getConfigurationSection("kits").getKeys(false).iterator();

         while(var4.hasNext()) {
            String key = (String)var4.next();
            inv.addItem(new ItemStack[]{this.createItem(Material.CHEST, ChatColor.AQUA + key)});
         }
      }

      inv.setItem(22, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openDuelMenu(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§9§lDUELOS §8» §fMenú Principal");
      inv.setItem(11, this.createItem(Material.FIREWORK_ROCKET, ChatColor.AQUA + "Cola Rápida (Random)"));
      inv.setItem(13, this.createItem(Material.NETHER_STAR, ChatColor.GREEN + "Rank: " + this.calculateRank(this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills"))));
      inv.setItem(15, this.createItem(Material.CHEST, ChatColor.GOLD + "Cola por Kit"));
      inv.setItem(22, this.createItem(Material.ARROW, "§c« Volver al Lobby"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openKitEditorList(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "Editor: Selecciona Kit");
      if (this.plugin.kitsConfig.contains("kits")) {
         Iterator var3 = this.plugin.kitsConfig.getConfigurationSection("kits").getKeys(false).iterator();

         while(var3.hasNext()) {
            String key = (String)var3.next();
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + key);
            meta.setLore(Arrays.asList(ChatColor.YELLOW + "Clic Izq: " + ChatColor.GRAY + "EDITAR CONTENIDO", ChatColor.RED + "Clic Der: " + ChatColor.GRAY + "BORRAR KIT"));
            item.setItemMeta(meta);
            inv.addItem(new ItemStack[]{item});
         }
      }

      inv.setItem(49, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openArenaEditorGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "Borrar Arenas FFA");
      if (this.plugin.arenasConfig.contains("ffa")) {
         Iterator var3 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

         while(var3.hasNext()) {
            String key = (String)var3.next();
            ItemStack item = new ItemStack(Material.TNT);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + key);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Clic para ELIMINAR"));
            item.setItemMeta(meta);
            inv.addItem(new ItemStack[]{item});
         }
      }

      inv.setItem(49, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openMissionsGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§e§lMISIONES §8» §fDiarias");
      UUID id = p.getUniqueId();
      if (!this.activeMissionsList.containsKey(id)) {
         List<GameManager.Mission> mList = new ArrayList();
         mList.add(new GameManager.Mission(GameManager.MissionType.CAZADOR, 10, 500));
         mList.add(new GameManager.Mission(GameManager.MissionType.GLADIADOR, 3, 300));
         mList.add(new GameManager.Mission(GameManager.MissionType.INVERSIONISTA, 1000, 200));
         this.activeMissionsList.put(id, mList);
      }

      List<GameManager.Mission> missions = (List)this.activeMissionsList.get(id);
      int[] slots = new int[]{11, 13, 15};

      for(int i = 0; i < 3; ++i) {
         GameManager.Mission m = (GameManager.Mission)missions.get(i);
         ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
         ItemMeta meta = item.getItemMeta();
         boolean isDone = m.current >= m.goal;
         String status = isDone ? ChatColor.GREEN + "¡COMPLETA!" : ChatColor.YELLOW + "EN PROGRESO";
         meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Misión: " + m.type.name());
         meta.setLore(Arrays.asList(ChatColor.GRAY + "--------------------", ChatColor.WHITE + "Objetivo: " + ChatColor.YELLOW + (m.type == GameManager.MissionType.CAZADOR ? "Mata 10 jugadores" : (m.type == GameManager.MissionType.GLADIADOR ? "Gana 3 duelos 1v1" : "Gasta $1000 en tienda")), ChatColor.WHITE + "Progreso: " + ChatColor.AQUA + m.current + "/" + m.goal, ChatColor.WHITE + "Recompensa: " + ChatColor.GOLD + "$" + m.reward, "", ChatColor.WHITE + "Estado: " + status, ChatColor.GRAY + "--------------------"));
         item.setItemMeta(meta);
         inv.setItem(slots[i], item);
         inv.setItem(slots[i] + 9, new ItemStack(isDone ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE));
      }

      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      inv.setItem(22, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      p.openInventory(inv);
   }

   public void openDailyRewardsGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 45, "§6§lCALENDARIO §8» §fPremios");
      long lastClaim = this.plugin.statsConfig.getLong("players." + p.getUniqueId() + ".last_claim", 0L);
      int streak = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".daily_streak", 0);
      long now = System.currentTimeMillis();
      if (now - lastClaim > 172800000L) {
         streak = 0;
         this.plugin.statsConfig.set("players." + p.getUniqueId() + ".daily_streak", 0);
      }

      boolean canClaim = now - lastClaim >= 86400000L || lastClaim == 0L;
      int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16};
      int[] rewards = new int[]{100, 250, 500, 1000, 2000, 3500, 5000};

      for(int i = 0; i < 7; ++i) {
         boolean claimed = i < streak;
         boolean available = i == streak && canClaim;
         ItemStack item = new ItemStack(claimed ? Material.MINECART : (available ? Material.CHEST_MINECART : Material.BARRIER));
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Día " + (i + 1));
         List<String> lore = new ArrayList();
         lore.add(ChatColor.GRAY + "Premio: " + ChatColor.GREEN + "$" + rewards[i]);
         lore.add("");
         if (claimed) {
            lore.add(ChatColor.RED + "¡YA RECLAMADO!");
         } else if (available) {
            lore.add(ChatColor.GREEN + "¡HAZ CLIC PARA RECLAMAR!");
         } else {
            lore.add(ChatColor.GRAY + "BLOQUEADO");
         }

         meta.setLore(lore);
         item.setItemMeta(meta);
         inv.setItem(slots[i], item);
      }

      inv.setItem(40, this.createItem(Material.ARROW, "§c« Volver"));
      this.plugin.cosmeticsManager.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openSpectatorGUI(Player p) {
      this.plugin.cosmeticsManager.openSpectatorGUI(p);
   }

   public void openTopKillsList(Player p, String arena) {
      this.plugin.cosmeticsManager.openTopKillsList(p, arena);
   }

   public String getPlayerArena(Player p) {
      String state = (String)this.playerState.get(p.getUniqueId());
      return state != null && state.startsWith("FFA:") ? state.split(":")[1] : null;
   }

   public GameManager.DuelMatch getMatch(Player p) {
      Iterator var2 = this.activeDuels.iterator();

      GameManager.DuelMatch m;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         m = (GameManager.DuelMatch)var2.next();
      } while(!m.p1.equals(p.getUniqueId()) && !m.p2.equals(p.getUniqueId()));

      return m;
   }

   public boolean isInDuel(Player p) {
      return this.getMatch(p) != null;
   }

   private ItemStack createItem(Material m, String n) {
      ItemStack i = new ItemStack(m);
      ItemMeta meta = i.getItemMeta();
      meta.setDisplayName(n);
      i.setItemMeta(meta);
      return i;
   }

   private void handleCombatLog(Player p) {
      if (this.combatTag.containsKey(p.getUniqueId())) {
         long end = (Long)this.combatTag.get(p.getUniqueId());
         if (System.currentTimeMillis() > end) {
            this.combatTag.remove(p.getUniqueId());
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(this.plugin.msg("combat.end-actionbar")));
         } else {
            int seconds = (int)((end - System.currentTimeMillis()) / 1000L);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(this.plugin.msg("combat.start-actionbar").replace("15s", seconds + "s")));
         }
      }

   }

   public void handleLobbySword(Player p) {
      UUID id = p.getUniqueId();
      String state = (String)this.playerState.getOrDefault(id, "LOBBY");
      if (state.equals("LOBBY")) {
         ItemStack hand = p.getInventory().getItemInMainHand();
         long now = System.currentTimeMillis();
         boolean inCombat = this.combatTag.containsKey(id) && (Long)this.combatTag.get(id) > now;
         if (hand != null && hand.getType() == Material.DIAMOND_SWORD) {
            int currentT = (Integer)this.lobbySwordTimer.getOrDefault(id, 0);
            if (currentT >= 3) {
               return;
            }

            int t = currentT + 1;
            this.lobbySwordTimer.put(id, t);
            String barra = "§a" + "▌".repeat(t) + "§7" + "▌".repeat(3 - t);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§eCargando PvP: " + barra));
            if (t < 3) {
               p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.5F + (float)t * 0.5F);
            }

            if (t == 3) {
               p.sendMessage("§a§l¡MODO PVP ACTIVADO!");
               p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
               p.sendTitle("§a⚔ PVP ⚔", "§7Ya puedes pelear", 0, 20, 10);
               p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0.0D, 1.0D, 0.0D), 50, 0.5D, 1.0D, 0.5D, 0.1D);
               this.plugin.cosmeticsManager.removePet(p);
               p.getInventory().clear();
               p.setGameMode(GameMode.SURVIVAL);
               p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
               p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
               p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
               p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
               p.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
               p.getInventory().addItem(new ItemStack[]{new ItemStack(Material.GOLDEN_APPLE, 8)});
               this.plugin.cosmeticsManager.applyActiveTrim(p);
               p.updateInventory();
            }
         } else if (this.lobbySwordTimer.containsKey(id)) {
            if (inCombat) {
               return;
            }

            this.lobbySwordTimer.remove(id);
            if (p.getGameMode() == GameMode.SURVIVAL) {
               p.sendMessage("§cModo PvP Desactivado.");
               p.playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0F, 1.5F);
               this.resetLobbyStats(p);
            }
         }

      }
   }

   public void updateScoreboard(Player p) {
      if (p != null && p.isOnline()) {
         Scoreboard board = p.getScoreboard();
         if (board.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
         }

         Objective oldObj = board.getObjective("UltFFA");
         if (oldObj != null) {
            oldObj.unregister();
         }

         if (!this.getSettings(p.getUniqueId()).scoreboard) {
            p.setScoreboard(board);
         } else {
            Objective obj = board.registerNewObjective("UltFFA", "dummy", "§5§lU§d§lL§5§lT§d§lI§5§lM§d§lA§5§lT§d§lE §f§lFFA");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            UUID id = p.getUniqueId();
            String state = (String)this.playerState.getOrDefault(id, "LOBBY");
            int kills = this.plugin.statsConfig.getInt("players." + id + ".global.kills", 0);
            int money = this.plugin.cosmeticsManager.getBalance(p);
            int ping = this.getPing(p);
            int prestige = this.plugin.statsConfig.getInt("players." + id + ".prestige", 0);
            String stars = "";

            for(int i = 0; i < prestige; ++i) {
               stars = stars + "⭐";
            }

            List<String> lines = new ArrayList();
            lines.add("§7§m--------------------");
            int playtime;
            String line;
            int pot;
            String st;
            int nextGoal;
            int tKills;
            if (state.equals("LOBBY")) {
               playtime = this.plugin.statsConfig.getInt("players." + id + ".playtime", 0);
               String serverRank = "§7Usuario";
               if (p.hasPermission("ffa.admin")) {
                  serverRank = "§c§lADMIN";
               } else if (p.hasPermission("ffa.vip")) {
                  serverRank = "§6§lVIP";
               }

               line = this.calculateRank(kills);
               pot = this.getNextRankGoal(kills);
               int fighting = 0;
               Iterator var18 = this.playerState.values().iterator();

               while(var18.hasNext()) {
                  st = (String)var18.next();
                  if (!st.equals("LOBBY") && !st.equals("SPECTATOR")) {
                     ++fighting;
                  }
               }

               lines.add("§d§l\ud83d\udc64 §fUsuario: §7" + p.getName());
               lines.add("§b§l\ud83d\udd30 §fRango: " + serverRank);
               lines.add("§5§l\ud83d\udc8e §fELO: " + line);
               if (pot > 0) {
                  lines.add(this.getProgressBar(kills, pot, 10, '|', ChatColor.AQUA, ChatColor.GRAY));
               } else {
                  lines.add("§8[§5||||||||||§8]");
               }

               lines.add("");
               lines.add("§b§l⚡ §fConectados: §a" + Bukkit.getOnlinePlayers().size());
               lines.add("§c§l⚔ §fCombatiendo: §c" + fighting);
               lines.add("");
               lines.add("§e§l\ud83d\udcb0 §fDinero: §6$" + money);
               if (prestige > 0) {
                  lines.add("§6§l\ud83c\udf1f §fPrestigio: " + stars);
               }

               lines.add("§a§l\ud83d\udcdc §fMisión: §f" + this.getActiveMissionName(p));
               lines.add("");
               lines.add("§9§l\ud83d\udcf6 §fPing: §7" + ping + "ms");
               lines.add("§4§l☠ §fKills: §c" + kills);
               lines.add("§6§l⌚ §fTiempo: §7" + this.formatTimeShort(playtime));
               lines.add("");
               lines.add("§fEstado: §7En Lobby");
            } else {
               String arenaName;
               if (state.equals("QUEUE")) {
                  arenaName = "Cargando...";
                  nextGoal = 0;
                  Iterator var28 = this.kitQueues.entrySet().iterator();

                  while(var28.hasNext()) {
                     Entry<String, LinkedList<UUID>> entry = (Entry)var28.next();
                     if (((LinkedList)entry.getValue()).contains(id)) {
                        arenaName = (String)entry.getKey();
                        nextGoal = ((LinkedList)entry.getValue()).size();
                        break;
                     }
                  }

                  lines.add("§d§l\ud83d\udd0e §fEstado: §eBuscando");
                  lines.add("");
                  lines.add("§b§l\ud83d\udee1 §fKit: §a" + arenaName);
                  lines.add("§e§l\ud83d\udc65 §fEn cola: §b" + nextGoal);
                  lines.add("");
                  lines.add("§7Espere por favor...");
               } else if (state.startsWith("FFA:")) {
                  arenaName = state.split(":")[1];
                  nextGoal = this.plugin.statsConfig.getInt("players." + id + "." + arenaName + ".kills", 0);
                  int arenaDeaths = this.plugin.statsConfig.getInt("players." + id + "." + arenaName + ".deaths", 0);
                  double kdr = arenaDeaths == 0 ? (double)nextGoal : (double)nextGoal / (double)arenaDeaths;
                  tKills = this.getStreak(p, arenaName);
                  st = this.calculateRank(kills);
                  int playersInArena = 0;
                  Iterator var21 = this.playerState.values().iterator();

                  while(var21.hasNext()) {
                     String st = (String)var21.next();
                     if (st.equals("FFA:" + arenaName)) {
                        ++playersInArena;
                     }
                  }

                  lines.add("§d§l⚔ §fArena: §e" + arenaName);
                  lines.add("§b§l\ud83d\udc65 §fJugadores: §a" + playersInArena);
                  lines.add("");
                  lines.add("§5§l\ud83d\udc8e §fELO: " + st);
                  lines.add("§c§l\ud83d\udd25 §fRacha: §c" + tKills);
                  lines.add("§4§l☠ §fKills: §c" + nextGoal);
                  lines.add("§6§l\ud83d\udcca §fKDR: §7" + String.format("%.2f", kdr));
                  lines.add("");
                  lines.add("§e§l\ud83d\udcb0 §fDinero: §6$" + money);
                  if (prestige > 0) {
                     lines.add("§6§l\ud83c\udf1f §fPrestigio: " + stars);
                  }

                  lines.add("§9§l\ud83d\udcf6 §fPing: §7" + ping + "ms");
                  lines.add("");
                  lines.add("§fEstado: §aCombatiendo");
               } else {
                  Player opponent;
                  if (state.equals("DUEL")) {
                     GameManager.DuelMatch match = this.getMatch(p);
                     if (match != null) {
                        opponent = match.p1.equals(id) ? Bukkit.getPlayer(match.p2) : Bukkit.getPlayer(match.p1);
                        line = opponent != null ? opponent.getName() : "Desconectado";
                        pot = match.betAmount > 0 ? match.betAmount * 2 : 0;
                        lines.add("§c§l⚔ §fRival: §c" + line);
                        lines.add("§b§l\ud83d\udee1 §fKit: §7" + match.kitName);
                        lines.add("");
                        lines.add("§fRonda: §e" + match.currentRound);
                        lines.add("§6§l\ud83c\udfc6 §fMarcador: §e" + match.p1Wins + " - " + match.p2Wins);
                        if (pot > 0) {
                           lines.add("§e§l\ud83d\udcb0 §fBote: §6$" + pot);
                        }

                        lines.add("");
                        lines.add("§9§l\ud83d\udcf6 §fPing: §7" + ping + "ms");
                        lines.add("§d§l⌚ §fTiempo: §7" + this.formatTime(match.remainingTime));
                        lines.add("");
                        lines.add("§fEstado: §cEn Duelo");
                     }
                  } else if (state.equals("SPECTATOR")) {
                     UUID targetId = (UUID)this.spectatorTarget.get(id);
                     opponent = targetId != null ? Bukkit.getPlayer(targetId) : null;
                     line = opponent != null ? opponent.getName() : "Nadie";
                     lines.add("§b§l\ud83d\udc41 §fViendo a: §e" + line);
                     lines.add("");
                     lines.add("§9§l\ud83d\udcf6 §fPing: §7" + ping + "ms");
                     lines.add("§fEstado: §7Espectando");
                  }
               }
            }

            lines.add("§7§m--------------------");
            lines.add("§d§nmc.tuserver.com");
            playtime = lines.size();

            Iterator var34;
            for(var34 = lines.iterator(); var34.hasNext(); --playtime) {
               line = (String)var34.next();
               Score s = obj.getScore(line);
               s.setScore(playtime);
            }

            if (board.getObjective("showhealth") == null) {
               Objective healthObj = board.registerNewObjective("showhealth", "health", "§c❤");
               healthObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
            }

            var34 = Bukkit.getOnlinePlayers().iterator();

            while(var34.hasNext()) {
               Player target = (Player)var34.next();
               String teamName = target.getName();
               Team team = board.getTeam(teamName);
               if (team == null) {
                  team = board.registerNewTeam(teamName);
               }

               tKills = this.plugin.statsConfig.getInt("players." + target.getUniqueId() + ".global.kills", 0);
               int tPrestige = this.plugin.statsConfig.getInt("players." + target.getUniqueId() + ".prestige", 0);
               String tRank = this.calculateRank(tKills);
               StringBuilder tStars = new StringBuilder();

               for(int i = 0; i < tPrestige; ++i) {
                  tStars.append("⭐");
               }

               StringBuilder prefix = new StringBuilder();
               if (tPrestige > 0) {
                  prefix.append("§e[").append(tStars).append("] ");
               }

               if (tRank.contains("Bronce")) {
                  prefix.append("§c[Bronze] ");
               } else if (tRank.contains("Plata")) {
                  prefix.append("§f[Silver] ");
               } else if (tRank.contains("Oro")) {
                  prefix.append("§6[Gold] ");
               } else if (tRank.contains("Diamante")) {
                  prefix.append("§b[Diamond] ");
               } else if (tRank.contains("LEYENDA")) {
                  prefix.append("§5§l[LEYENDA] ");
               } else {
                  prefix.append("§7");
               }

               team.setPrefix(prefix.toString());
               team.setSuffix(" §8[Lvl " + tKills + "]");
               if (!team.hasEntry(target.getName())) {
                  team.addEntry(target.getName());
               }
            }

            p.setLevel(kills);
            nextGoal = this.getNextRankGoal(kills);
            if (nextGoal > 0) {
               float progress = (float)kills / (float)nextGoal;
               if (progress > 1.0F) {
                  progress = 1.0F;
               }

               p.setExp(progress);
            } else {
               p.setExp(1.0F);
            }

            p.setScoreboard(board);
         }
      }
   }

   public void giveQueueItems(Player p) {
      ItemStack cancelItem = new ItemStack(Material.RED_DYE);
      ItemMeta meta = cancelItem.getItemMeta();
      meta.setDisplayName(ChatColor.RED + "Cancelar Búsqueda");
      meta.setLore(Arrays.asList(ChatColor.GRAY + "Clic derecho para salir de la cola."));
      cancelItem.setItemMeta(meta);
      p.getInventory().setItem(8, cancelItem);
   }

   public void removeQueueItems(Player p) {
      p.getInventory().setItem(8, new ItemStack(Material.AIR));
   }

   public int getStreak(Player p, String arena) {
      return arena == null ? 0 : (Integer)((Map)this.arenaStreaks.computeIfAbsent(p.getUniqueId(), (k) -> {
         return new HashMap();
      })).getOrDefault(arena, 0);
   }

   public void addStreak(Player p, String arena) {
      if (arena != null) {
         int current = this.getStreak(p, arena);
         ((Map)this.arenaStreaks.computeIfAbsent(p.getUniqueId(), (k) -> {
            return new HashMap();
         })).put(arena, current + 1);
      }
   }

   public void resetStreak(Player p, String arena) {
      if (arena != null) {
         if (this.arenaStreaks.containsKey(p.getUniqueId())) {
            ((Map)this.arenaStreaks.get(p.getUniqueId())).put(arena, 0);
         }

      }
   }

   public void setStreakManual(Player p, String arena, int amount) {
      if (arena != null) {
         ((Map)this.arenaStreaks.computeIfAbsent(p.getUniqueId(), (k) -> {
            return new HashMap();
         })).put(arena, amount);
      }
   }

   public String calculateRank(int kills) {
      if (kills < 10) {
         return ChatColor.GRAY + "Unranked";
      } else if (kills < 50) {
         return ChatColor.RED + "Bronce";
      } else if (kills < 100) {
         return ChatColor.WHITE + "Plata";
      } else if (kills < 250) {
         return ChatColor.GOLD + "Oro";
      } else {
         return kills < 500 ? ChatColor.AQUA + "Diamante" : ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "LEYENDA";
      }
   }

   public void resetPlayerStats(Player p) {
      this.plugin.statsConfig.set("players." + p.getUniqueId(), (Object)null);
      this.plugin.saveStats();
   }

   public void resetGlobalStats() {
      if (this.plugin.statsConfig.contains("players")) {
         Iterator var1 = this.plugin.statsConfig.getConfigurationSection("players").getKeys(false).iterator();

         while(true) {
            String path;
            do {
               if (!var1.hasNext()) {
                  this.plugin.saveStats();
                  Bukkit.broadcastMessage(ChatColor.RED + "§l[ADMIN] §c¡Se han reiniciado las estadísticas y la economía global!");
                  return;
               }

               String uuid = (String)var1.next();
               path = "players." + uuid;
               this.plugin.statsConfig.set(path + ".global", (Object)null);
               this.plugin.statsConfig.set(path + ".balance", 0);
               this.plugin.statsConfig.set(path + ".prestige", 0);
               this.plugin.statsConfig.set(path + ".daily_streak", 0);
               this.plugin.statsConfig.set(path + ".last_claim", 0);
            } while(!this.plugin.arenasConfig.contains("ffa"));

            Iterator var4 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

            while(var4.hasNext()) {
               String arena = (String)var4.next();
               this.plugin.statsConfig.set(path + "." + arena, (Object)null);
            }
         }
      }
   }

   public void saveAllData() {
      this.plugin.saveArenas();
      this.plugin.saveStats();
      this.plugin.saveKits();
   }

   public void setLobby(Player p) {
      this.plugin.getConfig().set("lobby", p.getLocation());
      this.plugin.saveConfig();
      p.sendMessage("§b§lFFA §8» §f§aLobby principal establecido en tu ubicación.");
      p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F);
   }

   public void deleteLobby() {
      this.plugin.arenasConfig.set("spawn.lobby", (Object)null);
      this.plugin.saveArenas();
   }

   private void runPetFollowLogic(Player p) {
      if (this.plugin.cosmeticsManager.activePets.containsKey(p.getUniqueId())) {
         Entity pet = (Entity)this.plugin.cosmeticsManager.activePets.get(p.getUniqueId());
         if (pet != null && pet.isValid()) {
            if (pet instanceof Warden) {
               ((Warden)pet).clearAnger(p);
               ((Warden)pet).setTarget((LivingEntity)null);
               p.removePotionEffect(PotionEffectType.DARKNESS);
            }

            Iterator var3 = pet.getNearbyEntities(20.0D, 20.0D, 20.0D).iterator();

            while(var3.hasNext()) {
               Entity nearby = (Entity)var3.next();
               if (nearby instanceof Player) {
                  ((Player)nearby).removePotionEffect(PotionEffectType.DARKNESS);
               }
            }

            if (pet instanceof Wither) {
               ((Wither)pet).setTarget((LivingEntity)null);
            }

            if (!p.getPassengers().contains(pet)) {
               double dist = pet.getLocation().distance(p.getLocation());
               if (dist > 15.0D) {
                  pet.teleport(p.getLocation());
               } else if (dist > 3.0D) {
                  Location target = p.getLocation();
                  if (pet instanceof Phantom) {
                     target.add(0.0D, 3.0D, 0.0D);
                  }

                  Vector dir = target.toVector().subtract(pet.getLocation().toVector()).normalize().multiply(0.4D);
                  pet.setVelocity(dir);
                  Location look = pet.getLocation();
                  look.setDirection(dir);
                  if (pet.getType() == EntityType.GIANT) {
                     Location newLoc = pet.getLocation().add(dir);
                     newLoc.setDirection(dir);
                     pet.teleport(newLoc);
                  } else {
                     try {
                        pet.setRotation(look.getYaw(), look.getPitch());
                     } catch (Exception var9) {
                     }
                  }
               }

            }
         }
      }
   }

   private void runAuraLogic(Player p) {
      String aura;
      if (this.plugin.cosmeticsManager.auraPreview.containsKey(p.getUniqueId())) {
         aura = (String)this.plugin.cosmeticsManager.auraPreview.get(p.getUniqueId());
      } else {
         aura = this.plugin.cosmeticsManager.getActiveCosmetic(p, "aura");
      }

      if (aura != null && !aura.equals("NONE")) {
         Location loc = p.getLocation().add(0.0D, 1.0D, 0.0D);
         long time = System.currentTimeMillis();
         if (aura.equals("HALO")) {
            p.getWorld().spawnParticle(Particle.GLOW, loc.clone().add(0.0D, 1.2D, 0.0D), 1);
         } else if (aura.equals("LAVA")) {
            p.getWorld().spawnParticle(Particle.DRIP_LAVA, loc, 5, 0.3D, 0.5D, 0.3D);
         } else if (aura.equals("REDSTONE")) {
            p.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0.5D, 1.0D, 0.5D, new DustOptions(Color.RED, 1.0F));
         } else if (aura.equals("HEART")) {
            p.getWorld().spawnParticle(Particle.HEART, loc.clone().add(0.0D, 1.0D, 0.0D), 1);
         } else if (aura.equals("MUSIC")) {
            p.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(0.0D, 1.2D, 0.0D), 1);
         } else if (aura.equals("SNOW")) {
            p.getWorld().spawnParticle(Particle.SNOWBALL, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("FROST")) {
            p.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 5, 0.5D, 0.1D, 0.5D);
         } else if (aura.equals("STORM")) {
            p.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 10, 0.5D, 2.0D, 0.5D);
         } else if (aura.equals("INK")) {
            p.getWorld().spawnParticle(Particle.SQUID_INK, loc, 5, 0.5D, 0.5D, 0.5D, 0.1D);
         } else if (aura.equals("ENCHANTED")) {
            p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 10, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("CHERRY")) {
            p.getWorld().spawnParticle(Particle.CHERRY_LEAVES, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("SCULK")) {
            p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("PORTAL")) {
            p.getWorld().spawnParticle(Particle.PORTAL, loc, 10, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("OCEANIC")) {
            p.getWorld().spawnParticle(Particle.BUBBLE_POP, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("RAGE")) {
            p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc.clone().add(0.0D, 1.0D, 0.0D), 1);
         } else if (aura.equals("CREEPER")) {
            p.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 2, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("TEARS")) {
            p.getWorld().spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, loc.clone().add(0.0D, 1.0D, 0.0D), 2);
         } else if (aura.equals("VOID")) {
            p.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 5, 0.5D, 0.5D, 0.5D, 0.05D);
         } else if (aura.equals("EMERALD")) {
            p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("CYBER")) {
            p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("WITCH")) {
            p.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 10, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("TOXIC")) {
            p.getWorld().spawnParticle(Particle.SLIME, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("TOTEM")) {
            p.getWorld().spawnParticle(Particle.TOTEM, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("CRITICAL")) {
            p.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("ENERGY")) {
            p.getWorld().spawnParticle(Particle.END_ROD, loc, 3, 0.5D, 1.0D, 0.5D, 0.05D);
         } else if (aura.equals("GALACTIC")) {
            p.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 5, 0.5D, 1.0D, 0.5D, 0.05D);
         } else if (aura.equals("SPIRIT")) {
            p.getWorld().spawnParticle(Particle.SOUL, loc, 3, 0.5D, 1.0D, 0.5D, 0.05D);
         } else if (aura.equals("CANDY")) {
            p.getWorld().spawnParticle(Particle.WAX_ON, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("NATURE")) {
            p.getWorld().spawnParticle(Particle.COMPOSTER, loc, 5, 0.5D, 1.0D, 0.5D);
         } else if (aura.equals("FLAME")) {
            p.getWorld().spawnParticle(Particle.FLAME, loc, 5, 0.5D, 1.0D, 0.5D, 0.05D);
         } else if (aura.equals("BLACK_HOLE")) {
            p.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 2, 0.1D, 0.1D, 0.1D, 0.05D);
            p.getWorld().spawnParticle(Particle.PORTAL, loc, 5, 0.5D, 0.5D, 0.5D);
         } else {
            double height;
            double rad;
            if (aura.equals("SHIELD")) {
               height = 1.0D;
               rad = (double)time / 1000.0D * 2.0D;

               for(int i = 0; i < 3; ++i) {
                  double angle = rad + (double)i * 2.0943951023931953D;
                  double x = Math.cos(angle) * height;
                  double z = Math.sin(angle) * height;
                  p.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(x, 0.0D, z), 0);
                  p.getWorld().spawnParticle(Particle.WAX_OFF, loc.clone().add(x, 0.5D, z), 0);
               }
            } else if (aura.equals("THUNDERSTORM")) {
               p.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0.0D, 1.5D, 0.0D), 2, 0.2D, 0.1D, 0.2D, 0.02D);
               if (Math.random() > 0.8D) {
                  p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 3, 0.4D, 1.0D, 0.4D, 0.1D);
               }
            } else {
               double z;
               double x;
               double z;
               double x;
               if (aura.equals("DNA")) {
                  height = 2.2D;
                  rad = 0.6D;
                  x = (double)time / 1000.0D * 3.0D;

                  for(z = 0.0D; z < height; z += 0.2D) {
                     x = rad * Math.cos(z * 3.0D + x);
                     z = rad * Math.sin(z * 3.0D + x);
                     p.getWorld().spawnParticle(Particle.REDSTONE, p.getLocation().add(x, z, z), 0, new DustOptions(Color.FUCHSIA, 0.8F));
                     p.getWorld().spawnParticle(Particle.REDSTONE, p.getLocation().add(-x, z, -z), 0, new DustOptions(Color.AQUA, 0.8F));
                  }
               } else if (aura.equals("RADAR")) {
                  height = 3.0D;
                  rad = (double)(time % 2000L) / 2000.0D * height;
                  int points = 20;

                  for(int i = 0; i < points; ++i) {
                     z = 6.283185307179586D * (double)i / (double)points;
                     x = Math.cos(z) * rad;
                     z = Math.sin(z) * rad;
                     p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation().add(x, 0.2D, z), 0);
                  }
               } else {
                  int i;
                  if (aura.equals("RAINBOW")) {
                     int r = (int)(Math.sin((double)time / 500.0D) * 127.0D + 128.0D);
                     i = (int)(Math.sin((double)time / 500.0D + 2.0D) * 127.0D + 128.0D);
                     int b = (int)(Math.sin((double)time / 500.0D + 4.0D) * 127.0D + 128.0D);
                     DustOptions dust = new DustOptions(Color.fromRGB(r, i, b), 1.5F);
                     p.getWorld().spawnParticle(Particle.REDSTONE, loc.add(0.0D, 1.0D, 0.0D), 5, 0.5D, 1.0D, 0.5D, 0.0D, dust);
                  } else {
                     Location head;
                     if (!aura.equals("ANGEL") && !aura.equals("DEVIL")) {
                        if (aura.equals("CORONA")) {
                           head = loc.clone().add(0.0D, 1.2D, 0.0D);

                           for(i = 0; i < 360; i += 45) {
                              rad = Math.toRadians((double)i + (double)time / 10.0D);
                              x = Math.cos(rad) * 0.4D;
                              z = Math.sin(rad) * 0.4D;
                              p.getWorld().spawnParticle(Particle.FLAME, head.clone().add(x, 0.0D, z), 0, 0.0D, 0.0D, 0.0D, 0.0D);
                           }
                        }
                     } else {
                        head = p.getLocation().add(0.0D, 1.5D, 0.0D).add(p.getLocation().getDirection().multiply(-0.3D));
                        Vector dir = p.getLocation().getDirection().normalize();
                        Vector up = new Vector(0, 1, 0);
                        Vector right = dir.clone().crossProduct(up).normalize();

                        for(x = 0.0D; x < 1.0D; x += 0.2D) {
                           z = 0.5D + x * 0.5D;
                           Location lR = head.clone().add(right.clone().multiply(z)).add(0.0D, x, 0.0D);
                           Location lL = head.clone().add(right.clone().multiply(-z)).add(0.0D, x, 0.0D);
                           if (aura.equals("ANGEL")) {
                              p.getWorld().spawnParticle(Particle.END_ROD, lR, 0);
                              p.getWorld().spawnParticle(Particle.END_ROD, lL, 0);
                           } else {
                              p.getWorld().spawnParticle(Particle.FLAME, lR, 0, 0.0D, 0.0D, 0.0D, 0.02D);
                              p.getWorld().spawnParticle(Particle.FLAME, lL, 0, 0.0D, 0.0D, 0.0D, 0.02D);
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public void restoreAllArenasForce() {
      if (this.plugin.restoreManager != null) {
         this.plugin.restoreManager.restoreAll();
         Bukkit.getLogger().info("[UltimateFFA] ✔ Todos los mapas (FFA y Duelos) han sido restaurados.");
      }

   }

   public String getAvailableDuelArena() {
      if (!this.plugin.arenasConfig.contains("duels")) {
         return null;
      } else {
         Iterator var1 = this.plugin.arenasConfig.getConfigurationSection("duels").getKeys(false).iterator();

         String arenaName;
         boolean occupied;
         do {
            do {
               do {
                  if (!var1.hasNext()) {
                     return null;
                  }

                  arenaName = (String)var1.next();
               } while(!this.plugin.arenasConfig.contains("duels." + arenaName + ".1"));
            } while(!this.plugin.arenasConfig.contains("duels." + arenaName + ".2"));

            occupied = false;
            Iterator var4 = this.activeDuels.iterator();

            while(var4.hasNext()) {
               GameManager.DuelMatch m = (GameManager.DuelMatch)var4.next();
               if (m.arenaName.equals(arenaName)) {
                  occupied = true;
                  break;
               }
            }
         } while(occupied);

         return arenaName;
      }
   }

   public void startDuel(final Player p1, final Player p2, String kitName) {
      String finalKit = kitName;
      if (kitName.equalsIgnoreCase("Random")) {
         finalKit = this.getRandomKitName();
      }

      if (finalKit != null && !finalKit.equals("default")) {
         int bet = 0;
         if (this.tempBets.containsKey(p2.getUniqueId())) {
            bet = (Integer)this.tempBets.remove(p2.getUniqueId());
            if (this.plugin.cosmeticsManager.getBalance(p1) < bet || this.plugin.cosmeticsManager.getBalance(p2) < bet) {
               p1.sendMessage("§cDinero insuficiente para la apuesta.");
               p2.sendMessage("§cDinero insuficiente para la apuesta.");
               return;
            }

            this.plugin.cosmeticsManager.addMoney(p1, -bet);
            this.plugin.cosmeticsManager.addMoney(p2, -bet);
            this.activeBets.put(p1.getUniqueId(), bet);
            this.activeBets.put(p2.getUniqueId(), bet);
            p1.sendMessage("§6-$" + bet + " (Apuesta)");
            p2.sendMessage("§6-$" + bet + " (Apuesta)");
            this.addMissionProgress(p1, GameManager.MissionType.INVERSIONISTA, bet);
            this.addMissionProgress(p2, GameManager.MissionType.INVERSIONISTA, bet);
         }

         String arenaName = this.getAvailableDuelArena();
         if (arenaName == null) {
            p1.sendMessage("§cNo hay arenas de duelo disponibles.");
            p2.sendMessage("§cNo hay arenas de duelo disponibles.");
            if (bet > 0) {
               this.plugin.cosmeticsManager.addMoney(p1, bet);
               this.plugin.cosmeticsManager.addMoney(p2, bet);
            }

         } else {
            this.preparePlayerForDuel(p1);
            this.preparePlayerForDuel(p2);
            final GameManager.DuelMatch match = new GameManager.DuelMatch();
            match.p1 = p1.getUniqueId();
            match.p2 = p2.getUniqueId();
            match.arenaName = arenaName;
            match.kitName = finalKit;
            match.betAmount = bet;
            this.activeDuels.add(match);
            this.activeDuels.add(match);
            match.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
               int time = 900;

               public void run() {
                  if (p1.isOnline() && p2.isOnline()) {
                     match.remainingTime = this.time;
                     if (this.time <= 0) {
                        GameManager.this.endDuel(match, (Player)null, (Player)null);
                     }

                     --this.time;
                  } else {
                     GameManager.this.endDuel(match, p1.isOnline() ? p1 : p2, p1.isOnline() ? p2 : p1);
                  }
               }
            }, 0L, 20L);
            this.startRound(match);
         }
      } else {
         p1.sendMessage("§cError: No se pudo seleccionar un kit válido.");
         p2.sendMessage("§cError: No se pudo seleccionar un kit válido.");
      }
   }

   public void startRound(final GameManager.DuelMatch m) {
      this.cleanArenaEntities(m.arenaName);
      final Player p1 = Bukkit.getPlayer(m.p1);
      final Player p2 = Bukkit.getPlayer(m.p2);
      if (p1 != null && p2 != null) {
         this.addMissionProgress(p1, GameManager.MissionType.GLADIADOR, 1);
         this.addMissionProgress(p2, GameManager.MissionType.GLADIADOR, 1);
         this.playerState.put(p1.getUniqueId(), "DUEL");
         this.playerState.put(p2.getUniqueId(), "DUEL");
         this.plugin.cosmeticsManager.removePet(p1);
         this.plugin.cosmeticsManager.removePet(p2);
         p1.setGameMode(GameMode.SURVIVAL);
         p2.setGameMode(GameMode.SURVIVAL);
         p1.getInventory().clear();
         p1.getInventory().setArmorContents((ItemStack[])null);
         p2.getInventory().clear();
         p2.getInventory().setArmorContents((ItemStack[])null);
         Location l1 = this.plugin.arenasConfig.getLocation("duels." + m.arenaName + ".1");
         Location l2 = this.plugin.arenasConfig.getLocation("duels." + m.arenaName + ".2");
         if (l1 != null) {
            p1.teleport(l1);
         }

         if (l2 != null) {
            p2.teleport(l2);
         }

         this.heal(p1);
         this.heal(p2);
         this.applyKit(p1, m.kitName);
         this.applyKit(p2, m.kitName);
         PotionEffect freeze = new PotionEffect(PotionEffectType.SLOW, 200, 255);
         PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 200, 250);
         p1.addPotionEffect(freeze);
         p1.addPotionEffect(jump);
         p2.addPotionEffect(freeze);
         p2.addPotionEffect(jump);
         (new BukkitRunnable() {
            int count = 10;

            public void run() {
               if (p1.isOnline() && p2.isOnline()) {
                  if (this.count > 0) {
                     if (this.count == 10 || this.count <= 5) {
                        String color = this.count <= 3 ? "§c" : "§e";
                        p1.sendTitle(color + this.count, "§7Ronda " + m.currentRound, 0, 20, 0);
                        p2.sendTitle(color + this.count, "§7Ronda " + m.currentRound, 0, 20, 0);
                        p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F);
                        p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F);
                     }

                     --this.count;
                  } else {
                     p1.sendTitle("§6§l¡A PELEAR!", "", 5, 15, 5);
                     p2.sendTitle("§6§l¡A PELEAR!", "", 5, 15, 5);
                     p1.playSound(p1.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
                     p2.playSound(p2.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
                     this.cancel();
                  }

               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 0L, 20L);
      } else {
         this.endDuel(m, p1 != null ? p1 : p2, (Player)null);
      }
   }

   public void handleDuelDeath(Player v, Player k, final GameManager.DuelMatch m) {
      if (!m.ended) {
         if (k != null && k.getUniqueId().equals(m.p1)) {
            ++m.p1Wins;
         } else {
            ++m.p2Wins;
         }

         int winsNeeded = 2;
         Player p1;
         Player p2;
         if (m.p1Wins < winsNeeded && m.p2Wins < winsNeeded) {
            ++m.currentRound;
            this.plugin.restoreManager.restoreArena(m.arenaName);
            this.cleanArenaEntities(m.arenaName);
            p1 = Bukkit.getPlayer(m.p1);
            p2 = Bukkit.getPlayer(m.p2);
            if (p1 == null || !p1.isOnline() || p2 == null || !p2.isOnline()) {
               Player winner = p1 != null && p1.isOnline() ? p1 : p2;
               this.endDuel(m, winner, (Player)null);
               return;
            }

            String scoreTitle = "§a" + m.p1Wins + " - " + m.p2Wins;
            p1.sendTitle("§eRONDA TERMINADA", scoreTitle, 10, 40, 10);
            p2.sendTitle("§eRONDA TERMINADA", scoreTitle, 10, 40, 10);
            (new BukkitRunnable() {
               public void run() {
                  if (!m.ended) {
                     Player checkP1 = Bukkit.getPlayer(m.p1);
                     Player checkP2 = Bukkit.getPlayer(m.p2);
                     if (checkP1 != null && checkP1.isOnline() && checkP2 != null && checkP2.isOnline()) {
                        GameManager.this.startRound(m);
                     } else {
                        GameManager.this.endDuel(m, checkP1 != null ? checkP1 : checkP2, (Player)null);
                     }

                  }
               }
            }).runTaskLater(this.plugin, 60L);
         } else {
            p1 = m.p1Wins > m.p2Wins ? Bukkit.getPlayer(m.p1) : Bukkit.getPlayer(m.p2);
            p2 = v != null ? v : (p1.getUniqueId().equals(m.p1) ? Bukkit.getPlayer(m.p2) : Bukkit.getPlayer(m.p1));
            this.endDuel(m, p1, p2);
         }

      }
   }

   public void setSpectator(final Player p, Player target) {
      this.playerState.put(p.getUniqueId(), "SPECTATOR");
      if (target != null) {
         this.spectatorTarget.put(p.getUniqueId(), target.getUniqueId());
      }

      p.setGameMode(GameMode.ADVENTURE);
      p.getInventory().clear();
      p.getInventory().setArmorContents((ItemStack[])null);
      p.updateInventory();
      Iterator var3 = p.getActivePotionEffects().iterator();

      while(var3.hasNext()) {
         PotionEffect effect = (PotionEffect)var3.next();
         p.removePotionEffect(effect.getType());
      }

      p.setAllowFlight(true);
      p.setFlying(true);
      p.setFlySpeed(0.1F);
      p.setCollidable(false);
      p.setInvisible(true);
      var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player on = (Player)var3.next();
         on.hidePlayer(this.plugin, p);
      }

      if (target != null && target.isOnline()) {
         p.teleport(target.getLocation().add(0.0D, 3.0D, 0.0D));
         p.sendTitle("§bESPECTANDO", "§fA: §e" + target.getName(), 0, 40, 10);
      }

      (new BukkitRunnable() {
         public void run() {
            if (p.isOnline()) {
               p.getInventory().clear();
               p.getInventory().setArmorContents((ItemStack[])null);
               ItemStack leave = new ItemStack(Material.RED_BED);
               ItemMeta meta = leave.getItemMeta();
               meta.setDisplayName("§c§lSALIR AL LOBBY §7(Clic derecho)");
               leave.setItemMeta(meta);
               p.getInventory().setItem(8, leave);
               p.updateInventory();
            }
         }
      }).runTaskLater(this.plugin, 2L);
   }

   public void spectateDuel(Player spectator, Player target) {
      if (target != null && target.isOnline()) {
         GameManager.DuelMatch match = this.getMatch(target);
         if (match == null) {
            spectator.sendMessage(ChatColor.RED + target.getName() + " no está en un duelo ahora mismo.");
         } else {
            this.setSpectator(spectator, target);
            spectator.sendMessage(ChatColor.GREEN + "\ud83d\udd2d Ahora estás espectando el duelo de " + target.getName());
         }
      } else {
         spectator.sendMessage(ChatColor.RED + "El jugador no está conectado.");
      }
   }

   private void heal(Player p) {
      p.setHealth(20.0D);
      p.setFoodLevel(20);
      p.setFireTicks(0);
      Iterator var2 = p.getActivePotionEffects().iterator();

      while(var2.hasNext()) {
         PotionEffect effect = (PotionEffect)var2.next();
         p.removePotionEffect(effect.getType());
      }

   }

   private String getRandomKitName() {
      if (this.plugin.kitsConfig.isConfigurationSection("kits")) {
         List<String> kits = new ArrayList(this.plugin.kitsConfig.getConfigurationSection("kits").getKeys(false));
         if (kits.size() > 1) {
            kits.remove("default");
         }

         if (!kits.isEmpty()) {
            return (String)kits.get((new Random()).nextInt(kits.size()));
         }
      }

      return "default";
   }

   public void teleportToSpawn(Player p) {
      if (this.plugin.arenasConfig.contains("spawn")) {
         p.teleport(this.plugin.arenasConfig.getLocation("spawn"));
      } else {
         p.teleport(p.getWorld().getSpawnLocation());
      }

   }

   public void sendDuelInvite(Player p, Player target, String kitName, int bet) {
      this.duelRequests.put(target.getUniqueId(), p.getUniqueId());
      this.duelKitRequests.put(target.getUniqueId(), kitName);
      if (bet > 0) {
         this.tempBets.put(target.getUniqueId(), bet);
      }

      p.sendMessage(this.plugin.msg("duel.invite-sent").replace("%target%", target.getName()));
      target.sendMessage("§8§m--------------------------------");
      target.sendMessage("§b§lDUELO 1v1 §8» §e" + p.getName() + " §fte ha desafiado.");
      target.sendMessage("§fKit: §a" + kitName);
      if (bet > 0) {
         target.sendMessage("§6§lAPUESTA: §e$" + bet);
      }

      target.sendMessage("");
      TextComponent accept = new TextComponent("§a§l[ACEPTAR]  ");
      accept.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/1v1 accept " + p.getName()));
      TextComponent deny = new TextComponent("§c§l[RECHAZAR]");
      deny.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/1v1 deny " + p.getName()));
      target.spigot().sendMessage(new BaseComponent[]{accept, deny});
      target.sendMessage("§8§m--------------------------------");
      target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
   }

   public void preparePlayerForDuel(Player p) {
      this.playerState.put(p.getUniqueId(), "DUEL");
      p.setGameMode(GameMode.SURVIVAL);
      p.getInventory().clear();
      p.getInventory().setArmorContents((ItemStack[])null);
      p.setHealth(20.0D);
      p.setFoodLevel(20);
      p.setFireTicks(0);
      Iterator var2 = p.getActivePotionEffects().iterator();

      while(var2.hasNext()) {
         PotionEffect effect = (PotionEffect)var2.next();
         p.removePotionEffect(effect.getType());
      }

      this.plugin.cosmeticsManager.removePet(p);
   }

   public String getFreeArena() {
      if (!this.plugin.arenasConfig.contains("arenas")) {
         return null;
      } else {
         Iterator var1 = this.plugin.arenasConfig.getConfigurationSection("arenas").getKeys(false).iterator();

         String arenaName;
         boolean isOccupied;
         do {
            if (!var1.hasNext()) {
               return null;
            }

            arenaName = (String)var1.next();
            isOccupied = false;
            Iterator var4 = this.activeDuels.iterator();

            while(var4.hasNext()) {
               GameManager.DuelMatch match = (GameManager.DuelMatch)var4.next();
               if (match.arenaName.equals(arenaName)) {
                  isOccupied = true;
                  break;
               }
            }
         } while(isOccupied);

         return arenaName;
      }
   }

   public void resetLobbyStats(Player p) {
      this.combatTag.remove(p.getUniqueId());
      this.lobbySwordTimer.remove(p.getUniqueId());
      this.playerState.put(p.getUniqueId(), "LOBBY");
      p.setGameMode(GameMode.ADVENTURE);
      p.setHealth(20.0D);
      p.setFoodLevel(20);
      p.getInventory().clear();
      p.getInventory().setArmorContents((ItemStack[])null);
      this.giveLobbyItems(p);
      this.plugin.cosmeticsManager.restorePet(p);
      this.plugin.cosmeticsManager.applyActiveTrim(p);
   }

   public void addMissionProgress(Player p, GameManager.MissionType type, int amount) {
      if (this.activeMissionsList.containsKey(p.getUniqueId())) {
         List<GameManager.Mission> missions = (List)this.activeMissionsList.get(p.getUniqueId());
         Iterator var5 = missions.iterator();

         while(var5.hasNext()) {
            GameManager.Mission m = (GameManager.Mission)var5.next();
            if (m.type == type && m.current < m.goal) {
               m.current += amount;
               if (m.current >= m.goal) {
                  m.current = m.goal;
                  this.plugin.cosmeticsManager.addMoney(p, m.reward);
                  p.sendMessage("§a§l¡MISIÓN COMPLETADA! §f(" + type.name() + ") §6+$" + m.reward);
                  p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
               }
            }
         }

      }
   }

   private String formatTime(int seconds) {
      int m = seconds / 60;
      int s = seconds % 60;
      return String.format("%02d:%02d", m, s);
   }

   private String formatTimeHours(int seconds) {
      int h = seconds / 3600;
      int m = seconds % 3600 / 60;
      return h > 0 ? h + "h " + m + "m" : m + "m";
   }

   private String getKDR(int kills, int deaths) {
      return deaths == 0 ? String.format("%.2f", (double)kills) : String.format("%.2f", (double)kills / (double)deaths);
   }

   private void addMissionLines(Player p, List<String> lines) {
      if (!this.activeMissionsList.containsKey(p.getUniqueId())) {
         lines.add("Misión: §cNinguna");
      } else {
         boolean found = false;
         Iterator var4 = ((List)this.activeMissionsList.get(p.getUniqueId())).iterator();

         while(var4.hasNext()) {
            GameManager.Mission m = (GameManager.Mission)var4.next();
            if (m.current < m.goal) {
               String shortName = m.type.name().charAt(0) + m.type.name().substring(1).toLowerCase();
               lines.add("Misión: §b" + shortName);
               int percentage = m.current * 100 / m.goal;
               String color = percentage >= 50 ? "§a" : "§c";
               lines.add("Progreso: " + color + m.current + "/" + m.goal + " §7(" + percentage + "%)");
               found = true;
               break;
            }
         }

         if (!found) {
            lines.add("Misión: §a¡Todas completas!");
         }

      }
   }

   public void startScoreboardTask() {
      (new BukkitRunnable() {
         public void run() {
            Iterator var1 = Bukkit.getOnlinePlayers().iterator();

            while(var1.hasNext()) {
               Player p = (Player)var1.next();
               GameManager.this.updateScoreboard(p);
               String state = (String)GameManager.this.playerState.getOrDefault(p.getUniqueId(), "LOBBY");
               int gTime = GameManager.this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".playtime", 0);
               GameManager.this.plugin.statsConfig.set("players." + p.getUniqueId() + ".playtime", gTime + 1);
               if (state.startsWith("FFA:")) {
                  String arena = state.split(":")[1];
                  int aTime = GameManager.this.plugin.statsConfig.getInt("players." + p.getUniqueId() + "." + arena + ".playtime", 0);
                  GameManager.this.plugin.statsConfig.set("players." + p.getUniqueId() + "." + arena + ".playtime", aTime + 1);
               }
            }

         }
      }).runTaskTimer(this.plugin, 20L, 20L);
   }

   private String getProgressBar(int current, int max, int totalBars, char symbol, ChatColor completedColor, ChatColor notCompletedColor) {
      float percent = (float)current / (float)max;
      int progressBars = (int)((float)totalBars * percent);
      if (progressBars > totalBars) {
         progressBars = totalBars;
      }

      StringBuilder sb = new StringBuilder();
      sb.append("§8[");

      for(int i = 0; i < totalBars; ++i) {
         if (i < progressBars) {
            sb.append(completedColor).append(symbol);
         } else {
            sb.append(notCompletedColor).append(symbol);
         }
      }

      sb.append("§8]");
      return sb.toString();
   }

   private String formatTimeShort(int seconds) {
      if (seconds < 60) {
         return seconds + "s";
      } else {
         int minutes = seconds / 60;
         int hours = minutes / 60;
         return hours > 0 ? hours + "h " + minutes % 60 + "m" : minutes + "m";
      }
   }

   private int getPing(Player p) {
      try {
         Object entityPlayer = p.getClass().getMethod("getHandle").invoke(p);
         return (Integer)entityPlayer.getClass().getField("ping").get(entityPlayer);
      } catch (Exception var3) {
         return 0;
      }
   }

   private String getActiveMissionName(Player p) {
      if (!this.activeMissionsList.containsKey(p.getUniqueId())) {
         return "§7Sin Misión";
      } else {
         Iterator var2 = ((List)this.activeMissionsList.get(p.getUniqueId())).iterator();

         GameManager.Mission m;
         do {
            if (!var2.hasNext()) {
               return "§a¡Todo Completado!";
            }

            m = (GameManager.Mission)var2.next();
         } while(m.current >= m.goal);

         String niceName = m.type.name().charAt(0) + m.type.name().substring(1).toLowerCase();
         return "§a" + niceName + " §7(" + m.current + "/" + m.goal + ")";
      }
   }

   public GameManager.PlayerSettings getSettings(UUID id) {
      this.playerSettings.putIfAbsent(id, new GameManager.PlayerSettings());
      return (GameManager.PlayerSettings)this.playerSettings.get(id);
   }

   public boolean acceptsDuels(Player p) {
      return this.getSettings(p.getUniqueId()).allowRequests;
   }

   public boolean acceptsSpectators(Player p) {
      return this.getSettings(p.getUniqueId()).allowSpectators;
   }

   public int getNextRankGoal(int kills) {
      if (kills < 10) {
         return 10;
      } else if (kills < 50) {
         return 50;
      } else if (kills < 100) {
         return 100;
      } else if (kills < 250) {
         return 250;
      } else {
         return kills < 500 ? 500 : 0;
      }
   }

   public void cleanArenaEntities(String arenaName) {
      Location loc = null;
      if (this.plugin.arenasConfig.contains("duels." + arenaName + ".1")) {
         loc = this.plugin.arenasConfig.getLocation("duels." + arenaName + ".1");
      } else if (this.plugin.arenasConfig.contains("ffa." + arenaName + ".spawn")) {
         loc = this.plugin.arenasConfig.getLocation("ffa." + arenaName + ".spawn");
      }

      if (loc != null) {
         Iterator var3 = loc.getWorld().getNearbyEntities(loc, 80.0D, 80.0D, 80.0D).iterator();

         while(true) {
            Entity e;
            do {
               do {
                  if (!var3.hasNext()) {
                     return;
                  }

                  e = (Entity)var3.next();
               } while(e instanceof Player);
            } while(!(e instanceof EnderCrystal) && !(e instanceof Arrow) && !(e instanceof Item) && !(e instanceof TNTPrimed) && !(e instanceof ExperienceOrb));

            e.remove();
         }
      }
   }

   public static class DuelMatch {
      public UUID p1;
      public UUID p2;
      public String arenaName;
      public String kitName;
      public int bet;
      public int taskId = -1;
      public int maxRounds;
      public int currentRound = 1;
      public int remainingTime;
      public int betAmount;
      public int p1Wins = 0;
      public int p2Wins = 0;
      public boolean ended = false;

      public DuelMatch() {
      }

      public DuelMatch(UUID p1, UUID p2, String arenaName, String kitName, int maxRounds, int bet) {
         this.p1 = p1;
         this.p2 = p2;
         this.arenaName = arenaName;
         this.kitName = kitName;
         this.maxRounds = maxRounds;
         this.bet = bet;
      }
   }

   public static enum MissionType {
      CAZADOR,
      GLADIADOR,
      INVERSIONISTA;

      // $FF: synthetic method
      private static GameManager.MissionType[] $values() {
         return new GameManager.MissionType[]{CAZADOR, GLADIADOR, INVERSIONISTA};
      }
   }

   public static class Mission {
      public GameManager.MissionType type;
      public int current;
      public int goal;
      public int reward;

      public Mission(GameManager.MissionType type, int goal, int reward) {
         this.type = type;
         this.goal = goal;
         this.reward = reward;
         this.current = 0;
      }
   }

   public static class PlayerSettings {
      public boolean scoreboard = true;
      public boolean allowRequests = true;
      public boolean allowSpectators = true;
      public long time = -1L;
   }

   public static class InviteData {
      public UUID inviter;
      public int bet;

      public InviteData(UUID i, int b) {
         this.inviter = i;
         this.bet = b;
      }

      public UUID getInviter() {
         return this.inviter;
      }
   }
}
