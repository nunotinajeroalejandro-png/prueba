package me.ethernova.ultimateffa;

import java.util.Iterator;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {
   private final Main plugin;

   public EventListener(Main plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent e) {
      final Player victim = e.getEntity();
      final Player killer = victim.getKiller();
      if (this.plugin.gameManager.isInDuel(victim)) {
         final GameManager.DuelMatch match = this.plugin.gameManager.getMatch(victim);
         if (match != null) {
            e.setDeathMessage((String)null);
            e.getDrops().clear();
            e.setDroppedExp(0);
            (new BukkitRunnable() {
               public void run() {
                  if (victim.isOnline()) {
                     victim.spigot().respawn();
                     (new BukkitRunnable() {
                        public void run() {
                           EventListener.this.plugin.gameManager.handleDuelDeath(victim, killer, match);
                        }
                     }).runTaskLater(EventListener.this.plugin, 2L);
                  }

               }
            }).runTaskLater(this.plugin, 1L);
            return;
         }
      }

      e.setDeathMessage((String)null);
      e.getDrops().clear();
      e.setDroppedExp(0);
      this.plugin.cosmeticsManager.removePet(victim);
      this.plugin.gameManager.combatTag.remove(victim.getUniqueId());
      (new BukkitRunnable() {
         public void run() {
            if (victim.isDead()) {
               victim.spigot().respawn();
            }

            EventListener.this.plugin.gameManager.sendToLobby(victim);
         }
      }).runTaskLater(this.plugin, 1L);
      this.updateStats(victim, false);
      String kArena;
      if (killer != null) {
         this.updateStats(killer, true);
         killer.incrementStatistic(Statistic.PLAYER_KILLS);
         killer.playSound(killer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
         kArena = this.plugin.gameManager.getPlayerArena(killer);
         int bounty = this.plugin.statsConfig.getInt("players." + victim.getUniqueId() + ".bounty", 0);
         if (bounty > 0) {
            this.plugin.cosmeticsManager.addMoney(killer, bounty);
            this.plugin.statsConfig.set("players." + victim.getUniqueId() + ".bounty", 0);
            this.plugin.saveStats();
            Bukkit.broadcastMessage("§6§l¡RECOMPENSA! §e" + killer.getName() + " §7ha cobrado §6$" + bounty + " §7por la cabeza de §c" + victim.getName());
            killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 2.0F);
            killer.getWorld().spawnParticle(Particle.TOTEM, victim.getLocation().add(0.0D, 1.0D, 0.0D), 20, 0.5D, 0.5D, 0.5D, 0.1D);
         }

         String deathMsgID = this.plugin.cosmeticsManager.getActiveCosmetic(killer, "msg");
         String deathText = "§e<victim> §7fue eliminado por §e<killer>";
         if (deathMsgID != null && !deathMsgID.equals("NONE")) {
            try {
               deathText = CosmeticsManager.DeathMessage.valueOf(deathMsgID).text;
            } catch (Exception var22) {
            }
         }

         double hp = (double)Math.round(killer.getHealth() * 10.0D) / 10.0D;
         String finalMsg = ChatColor.translateAlternateColorCodes('&', deathText.replace("<victim>", victim.getName()).replace("<killer>", killer.getName()).replace("<hp>", String.valueOf(hp)));
         Bukkit.broadcastMessage(finalMsg);
         String soundName = this.plugin.cosmeticsManager.getActiveCosmetic(killer, "sound");
         if (soundName != null && !soundName.equals("NONE")) {
            Sound soundToPlay = null;

            try {
               byte var14 = -1;
               switch(soundName.hashCode()) {
               case -2125864634:
                  if (soundName.equals("VILLAGER")) {
                     var14 = 1;
                  }
                  break;
               case -1781303918:
                  if (soundName.equals("ENDERMAN")) {
                     var14 = 9;
                  }
                  break;
               case -1348968106:
                  if (soundName.equals("LEVEL_UP")) {
                     var14 = 4;
                  }
                  break;
               case -591166271:
                  if (soundName.equals("EXPLODE")) {
                     var14 = 5;
                  }
                  break;
               case 66486:
                  if (soundName.equals("CAT")) {
                     var14 = 6;
                  }
                  break;
               case 2034947:
                  if (soundName.equals("BELL")) {
                     var14 = 8;
                  }
                  break;
               case 2050513:
                  if (soundName.equals("BURP")) {
                     var14 = 3;
                  }
                  break;
               case 2193179:
                  if (soundName.equals("GOAT")) {
                     var14 = 7;
                  }
                  break;
               case 62437548:
                  if (soundName.equals("ANVIL")) {
                     var14 = 0;
                  }
                  break;
               case 67899228:
                  if (soundName.equals("GLASS")) {
                     var14 = 2;
                  }
               }

               switch(var14) {
               case 0:
                  soundToPlay = Sound.BLOCK_ANVIL_LAND;
                  break;
               case 1:
                  soundToPlay = Sound.ENTITY_VILLAGER_TRADE;
                  break;
               case 2:
                  soundToPlay = Sound.BLOCK_GLASS_BREAK;
                  break;
               case 3:
                  soundToPlay = Sound.ENTITY_PLAYER_BURP;
                  break;
               case 4:
                  soundToPlay = Sound.ENTITY_PLAYER_LEVELUP;
                  break;
               case 5:
                  soundToPlay = Sound.ENTITY_GENERIC_EXPLODE;
                  break;
               case 6:
                  soundToPlay = Sound.ENTITY_CAT_AMBIENT;
                  break;
               case 7:
                  soundToPlay = Sound.ENTITY_GOAT_SCREAMING_AMBIENT;
                  break;
               case 8:
                  soundToPlay = Sound.BLOCK_BELL_USE;
                  break;
               case 9:
                  soundToPlay = Sound.ENTITY_ENDERMAN_SCREAM;
                  break;
               default:
                  try {
                     soundToPlay = Sound.valueOf(soundName);
                  } catch (Exception var20) {
                  }
               }

               if (soundToPlay != null) {
                  killer.playSound(killer.getLocation(), soundToPlay, 1.0F, 1.0F);
               }
            } catch (Exception var21) {
            }
         }

         int streak;
         if (kArena != null) {
            this.plugin.gameManager.addStreak(killer, kArena);
            streak = this.plugin.gameManager.getStreak(killer, kArena);
            if (streak == 5) {
               Bukkit.broadcastMessage("§e§lRACHA §8» §b" + killer.getName() + " §7ha llegado a §a5 §7bajas.");
               this.plugin.cosmeticsManager.addMoney(killer, 500);
               killer.playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
            } else if (streak == 10) {
               Bukkit.broadcastMessage("§6§lRACHA §8» §b" + killer.getName() + " §7ha llegado a §610 §7bajas.");
               this.plugin.cosmeticsManager.addMoney(killer, 2000);
               killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
               killer.playSound(killer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.0F);
            } else if (streak == 20) {
               Bukkit.broadcastMessage("§c§lRACHA §8» §b" + killer.getName() + " §7domina con §c20 §7bajas.");
               this.plugin.cosmeticsManager.addMoney(killer, 8000);
               killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
               killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
               killer.getWorld().strikeLightningEffect(killer.getLocation());
            } else if (streak == 30) {
               Bukkit.broadcastMessage("§4§lRACHA §8» §b" + killer.getName() + " §7es IMPARABLE con §430 §7bajas.");
               this.plugin.cosmeticsManager.addMoney(killer, 32000);
               killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
               killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
               killer.getWorld().strikeLightningEffect(killer.getLocation());
            } else if (streak == 50) {
               Bukkit.broadcastMessage("§d§lLEYENDA §8» §b" + killer.getName() + " §7ha desbloqueado §6ASPECTO ÍGNEO PERMANENTE §7en §e" + kArena);
               this.plugin.cosmeticsManager.addMoney(killer, 128000);
               killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
               killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
               this.plugin.statsConfig.set("players." + killer.getUniqueId() + "." + kArena + ".fire_unlock", true);
               this.plugin.saveStats();
               ItemStack hand = killer.getInventory().getItemInMainHand();
               if (hand != null && hand.getType().name().contains("SWORD")) {
                  hand.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
                  killer.sendMessage("§6§l¡TU ESPADA AHORA QUEMA!");
               }

               killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            }
         }

         this.plugin.gameManager.addMissionProgress(killer, GameManager.MissionType.CAZADOR, 1);
         streak = this.plugin.statsConfig.getInt("players." + killer.getUniqueId() + ".daily_kills", 0) + 1;
         this.plugin.statsConfig.set("players." + killer.getUniqueId() + ".daily_kills", streak);
         this.plugin.saveStats();
         victim.sendMessage("§cTe mató §e" + killer.getName() + " §7(" + hp + " HP)");
         String kState = (String)this.plugin.gameManager.playerState.getOrDefault(killer.getUniqueId(), "LOBBY");
         if (kState.startsWith("FFA")) {
            killer.setHealth(20.0D);
            killer.setFoodLevel(20);
            String arenaName = kState.split(":")[1];
            this.plugin.gameManager.applyKit(killer, arenaName);
         }

         int moneyPerKill = killer.hasPermission("ffa.vip") ? 20 : 10;
         this.plugin.cosmeticsManager.addMoney(killer, moneyPerKill);
         Location loc = victim.getLocation().add(0.0D, 2.5D, 0.0D);
         final ArmorStand as = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
         as.setVisible(false);
         as.setGravity(false);
         as.setSmall(true);
         as.setCustomName(ChatColor.GOLD + "+$" + moneyPerKill);
         as.setCustomNameVisible(true);
         as.setMarker(true);
         String effectName = this.plugin.cosmeticsManager.getActiveCosmetic(killer, "effect");
         if (effectName != null && !effectName.equals("NONE")) {
            try {
               CosmeticsManager.KillEffect effect = CosmeticsManager.KillEffect.valueOf(effectName);
               this.plugin.cosmeticsManager.playKillEffect(killer, victim.getLocation(), effect);
            } catch (Exception var19) {
            }
         }

         (new BukkitRunnable() {
            public void run() {
               as.remove();
            }
         }).runTaskLater(this.plugin, 30L);
      }

      kArena = this.plugin.gameManager.getPlayerArena(victim);
      if (kArena != null) {
         this.plugin.gameManager.resetStreak(victim, kArena);
         this.plugin.statsConfig.set("players." + victim.getUniqueId() + "." + kArena + ".streak", 0);
         this.plugin.saveStats();
      }

   }

   private void updateStats(Player p, boolean isKill) {
      String pathGlobal = "players." + p.getUniqueId() + ".global." + (isKill ? "kills" : "deaths");
      this.plugin.statsConfig.set(pathGlobal, this.plugin.statsConfig.getInt(pathGlobal) + 1);
      String arena = this.plugin.gameManager.getPlayerArena(p);
      if (arena != null) {
         String pathArena = "players." + p.getUniqueId() + "." + arena + "." + (isKill ? "kills" : "deaths");
         this.plugin.statsConfig.set(pathArena, this.plugin.statsConfig.getInt(pathArena) + 1);
      }

      if (isKill) {
         int currentKills = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills");
         String oldRank = this.plugin.gameManager.calculateRank(currentKills - 1);
         String newRank = this.plugin.gameManager.calculateRank(currentKills);
         if (!oldRank.equals(newRank)) {
            Bukkit.broadcastMessage("§8§m--------------------------------------");
            Bukkit.broadcastMessage("   §6§l★ ASCENSO DE RANGO ★");
            Bukkit.broadcastMessage("   §fEl jugador §b" + p.getName() + " §fha subido a:");
            Bukkit.broadcastMessage("   §e§l" + newRank.toUpperCase());
            Bukkit.broadcastMessage("§8§m--------------------------------------");
            p.sendTitle("§6§l¡ASCENSO!", "§fAhora eres rango §e" + newRank, 10, 60, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
         }
      }

      this.plugin.saveStats();
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
      if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
         Player victim = (Player)e.getEntity();
         Player attacker = (Player)e.getDamager();
         UUID vid = victim.getUniqueId();
         UUID aid = attacker.getUniqueId();
         String vState = (String)this.plugin.gameManager.playerState.getOrDefault(vid, "LOBBY");
         String aState = (String)this.plugin.gameManager.playerState.getOrDefault(aid, "LOBBY");
         if ("LOBBY".equals(vState) && "LOBBY".equals(aState)) {
            boolean vEsGuerrero = (Integer)this.plugin.gameManager.lobbySwordTimer.getOrDefault(vid, 0) >= 3 || this.plugin.gameManager.combatTag.containsKey(vid);
            boolean aEsGuerrero = (Integer)this.plugin.gameManager.lobbySwordTimer.getOrDefault(aid, 0) >= 3 || this.plugin.gameManager.combatTag.containsKey(aid);
            if (!vEsGuerrero || !aEsGuerrero) {
               e.setCancelled(true);
               return;
            }
         } else if (!vState.equals(aState)) {
            e.setCancelled(true);
            return;
         }

         if (!e.isCancelled()) {
            long expire = System.currentTimeMillis() + 15000L;
            this.plugin.gameManager.combatTag.put(vid, expire);
            this.plugin.gameManager.combatTag.put(aid, expire);
            this.plugin.gameManager.lastDamager.put(vid, aid);
            victim.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c⚔ EN COMBATE ⚔"));
            attacker.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c⚔ EN COMBATE ⚔"));
         }

      }
   }

   @EventHandler
   public void onEnvironmentalDamage(EntityDamageEvent e) {
      if (e.getEntity() instanceof Player) {
         Player p = (Player)e.getEntity();
         if ("LOBBY".equals(this.plugin.gameManager.playerState.get(p.getUniqueId())) && e.getCause() != DamageCause.ENTITY_ATTACK && e.getCause() != DamageCause.ENTITY_SWEEP_ATTACK) {
            e.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) {
      final Player p = e.getPlayer();
      UUID id = p.getUniqueId();
      if (p.isDead()) {
         p.spigot().respawn();
      }

      this.plugin.gameManager.activeDuels.removeIf((match) -> {
         return match.p1.equals(id) || match.p2.equals(id);
      });
      this.plugin.gameManager.playerState.put(id, "LOBBY");
      this.plugin.gameManager.combatTag.remove(id);
      this.plugin.gameManager.lobbySwordTimer.remove(id);
      this.plugin.gameManager.lastDamager.remove(id);
      this.plugin.gameManager.spectatorTarget.remove(id);
      this.plugin.gameManager.buildMode.remove(id);
      String joinID = this.plugin.cosmeticsManager.getActiveCosmetic(p, "join");
      if (!joinID.equals("NONE")) {
         try {
            String text = CosmeticsManager.JoinMessage.valueOf(joinID).text;
            e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', text.replace("<player>", p.getName())));
         } catch (Exception var8) {
            e.setJoinMessage("§7[+] " + p.getName());
         }
      } else {
         e.setJoinMessage("§7[+] " + p.getName());
      }

      (new BukkitRunnable() {
         public void run() {
            if (p.isOnline()) {
               EventListener.this.plugin.gameManager.teleportToSpawn(p);
               EventListener.this.plugin.gameManager.sendToLobby(p);
               EventListener.this.plugin.gameManager.playerState.remove(p.getUniqueId());
               int kills = EventListener.this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills", 0);
               p.setLevel(kills);
               p.setExp(0.0F);
            }
         }
      }).runTaskLater(this.plugin, 5L);
      if (this.plugin.arenasConfig.contains("ffa")) {
         Iterator var9 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

         while(var9.hasNext()) {
            String arena = (String)var9.next();
            int savedStreak = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + "." + arena + ".streak", 0);
            if (savedStreak > 0) {
               this.plugin.gameManager.setStreakManual(p, arena, savedStreak);
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onQuit(PlayerQuitEvent e) {
      Player p = e.getPlayer();
      UUID id = p.getUniqueId();
      long now = System.currentTimeMillis();
      if (this.plugin.gameManager.combatTag.containsKey(id) && now < (Long)this.plugin.gameManager.combatTag.get(id) && !this.plugin.gameManager.isInDuel(p)) {
         Bukkit.broadcastMessage("§c" + p.getName() + " se desconectó en combate.");
         UUID attackerId = (UUID)this.plugin.gameManager.lastDamager.get(id);
         if (attackerId != null) {
            Player attacker = Bukkit.getPlayer(attackerId);
            if (attacker != null && attacker.isOnline()) {
               this.plugin.cosmeticsManager.addMoney(attacker, 20);
               attacker.sendMessage("§a§lKILL §8» §e" + p.getName() + " §7(Desconexión).");
            }
         }

         p.setHealth(0.0D);
      }

      GameManager.DuelMatch match = this.plugin.gameManager.getMatch(p);
      if (match != null && !match.ended) {
         UUID winnerId = match.p1.equals(id) ? match.p2 : match.p1;
         Player winner = Bukkit.getPlayer(winnerId);
         this.plugin.gameManager.endDuel(match, winner, (Player)null);
      }

      e.setQuitMessage("§7[-] " + p.getName());
      this.plugin.gameManager.removeFromQueues(p);
      this.plugin.gameManager.playerState.remove(id);
      this.plugin.gameManager.combatTag.remove(id);
      this.plugin.gameManager.lastDamager.remove(id);
      this.plugin.gameManager.spectatorTarget.remove(id);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
      Player p = e.getPlayer();
      if (!p.hasPermission("ffa.admin")) {
         String state = (String)this.plugin.gameManager.playerState.getOrDefault(p.getUniqueId(), "LOBBY");
         if (!"LOBBY".equals(state)) {
            if (this.plugin.gameManager.isInDuel(p)) {
               String msg = e.getMessage().toLowerCase();
               if (!msg.startsWith("/ffa leave") && !msg.startsWith("/leave") && !msg.startsWith("/1v1 leave")) {
                  e.setCancelled(true);
                  p.sendMessage("§cNo puedes usar comandos durante un duelo.");
               }
            } else {
               if (this.plugin.gameManager.combatTag.containsKey(p.getUniqueId())) {
                  e.setCancelled(true);
                  p.sendMessage("§cEstás en combate.");
               }

            }
         }
      }
   }

   @EventHandler
   public void onFoodLevelChange(FoodLevelChangeEvent e) {
      if (e.getEntity() instanceof Player) {
         Player p = (Player)e.getEntity();
         if ("LOBBY".equals(this.plugin.gameManager.playerState.get(p.getUniqueId()))) {
            e.setCancelled(true);
            p.setFoodLevel(20);
         }
      }

   }

   @EventHandler
   public void onPlayerDropItem(PlayerDropItemEvent e) {
      Player p = e.getPlayer();
      String state = (String)this.plugin.gameManager.playerState.getOrDefault(p.getUniqueId(), "LOBBY");
      if ("LOBBY".equals(this.plugin.gameManager.playerState.get(p.getUniqueId())) && !this.plugin.gameManager.buildMode.contains(p.getUniqueId())) {
         e.setCancelled(true);
      }

      if ("QUEUE".equals(this.plugin.gameManager.playerState.get(p.getUniqueId()))) {
         e.setCancelled(true);
      }

      if ("SPECTATOR".equals(state)) {
         e.setCancelled(true);
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent e) {
      Player p = e.getPlayer();
      String state = (String)this.plugin.gameManager.playerState.getOrDefault(p.getUniqueId(), "LOBBY");
      ItemStack item = e.getItem();
      if (state.equals("SPECTATOR")) {
         e.setCancelled(true);
         if (item != null && item.getType() == Material.RED_BED) {
            this.plugin.gameManager.sendToLobby(p);
            p.sendMessage(this.plugin.msg("spectator.leave"));
         }

      } else {
         if (state.equals("LOBBY")) {
            if (p.getGameMode() == GameMode.SURVIVAL && item != null && (item.getType().isEdible() || item.getType() == Material.POTION || item.getType() == Material.BOW)) {
               return;
            }

            if (!e.hasItem() || !e.getItem().hasItemMeta()) {
               return;
            }

            e.setCancelled(true);
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (name.contains("Arenas FFA")) {
               this.plugin.gameManager.openArenasGUI(p);
            } else if (name.contains("Misiones Diarias")) {
               this.plugin.gameManager.openMissionsGUI(p);
            } else if (name.contains("Calendario")) {
               this.plugin.gameManager.openDailyRewardsGUI(p);
            } else if (name.contains("Tienda")) {
               this.plugin.cosmeticsManager.openShopGUI(p);
            } else if (name.contains("Duelos")) {
               this.plugin.gameManager.openDuelMenu(p);
            } else if (name.contains("Espectar")) {
               this.plugin.cosmeticsManager.openSpectatorGUI(p);
            } else if (name.contains("Ranking")) {
               this.plugin.gameManager.openRankingMainMenu(p);
            }

            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.5F);
         }

      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent e) {
      if (e.getCurrentItem() != null) {
         Player p = (Player)e.getWhoClicked();
         int slot = e.getSlot();
         String titleRaw = e.getView().getTitle();
         String titleClean = ChatColor.stripColor(titleRaw).toUpperCase();
         if (titleClean.contains("TIENDA") || titleClean.contains("ARENAS") || titleClean.contains("ADMIN") || titleClean.contains("GESTIÓN") || titleClean.contains("RANKING") || titleClean.contains("ESTADÍSTICAS") || titleClean.contains("TOP") || titleClean.contains("MASCOTAS") || titleClean.contains("VARIANTES") || titleClean.contains("SELECTOR") || titleClean.contains("ARMERÍA") || titleClean.contains("DISEÑO") || titleClean.contains("COLOR") || titleClean.contains("MATERIAL") || titleClean.contains("PRESTIGIO") || titleClean.contains("EDITOR") || titleClean.contains("EDITANDO") || titleClean.contains("DUELOS") || titleClean.contains("COLA") || titleClean.contains("MISIONES") || titleClean.contains("CALENDARIO") || titleClean.contains("RETO") || titleClean.contains("KIT") || titleClean.contains("AJUSTES")) {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getView().getBottomInventory()) {
               e.setCancelled(true);
            }

            this.plugin.cosmeticsManager.handleShopClick(p, titleRaw, e.getCurrentItem(), e.isRightClick());
         }

         if (titleRaw.equals("§8Ajustes Personales")) {
            e.setCancelled(true);
            GameManager.PlayerSettings settings = this.plugin.gameManager.getSettings(p.getUniqueId());
            if (slot == 10) {
               settings.scoreboard = !settings.scoreboard;
               if (settings.scoreboard) {
                  this.plugin.gameManager.updateScoreboard(p);
               } else {
                  p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
               }

               this.plugin.cosmeticsManager.openSettingsGUI(p);
               p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            }

            if (slot == 12) {
               settings.allowRequests = !settings.allowRequests;
               this.plugin.cosmeticsManager.openSettingsGUI(p);
               p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            }

            if (slot == 14) {
               if (settings.time == -1L) {
                  settings.time = 6000L;
               } else if (settings.time == 6000L) {
                  settings.time = 18000L;
               } else {
                  settings.time = -1L;
               }

               if (settings.time == -1L) {
                  p.resetPlayerTime();
               } else {
                  p.setPlayerTime(settings.time, false);
               }

               this.plugin.cosmeticsManager.openSettingsGUI(p);
               p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            }

            if (slot == 16) {
               settings.allowSpectators = !settings.allowSpectators;
               this.plugin.cosmeticsManager.openSettingsGUI(p);
               p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            }
         }

      }
   }

   @EventHandler
   public void onProjectileLaunch(final ProjectileLaunchEvent e) {
      if (!e.getEntity().hasMetadata("isPreview")) {
         if (e.getEntity().getShooter() instanceof Player) {
            Player p = (Player)e.getEntity().getShooter();
            final String trail = this.plugin.cosmeticsManager.getActiveCosmetic(p, "trail");
            if (trail != null && !trail.equals("NONE")) {
               (new BukkitRunnable() {
                  public void run() {
                     if (!e.getEntity().isDead() && !e.getEntity().isOnGround()) {
                        EventListener.this.plugin.cosmeticsManager.spawnTrailParticle(e.getEntity().getLocation(), trail);
                     } else {
                        this.cancel();
                     }
                  }
               }).runTaskTimer(this.plugin, 1L, 1L);
            }

         }
      }
   }

   @EventHandler
   public void onDoubleJump(PlayerToggleFlightEvent e) {
      final Player p = e.getPlayer();
      final UUID id = p.getUniqueId();
      if ("LOBBY".equals(this.plugin.gameManager.playerState.get(id))) {
         if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
            e.setCancelled(true);
            p.setFlying(false);
            if (!this.plugin.gameManager.jumpCooldown.contains(id)) {
               ItemStack hand = p.getInventory().getItemInMainHand();
               if (hand != null && hand.getType() == Material.DIAMOND_SWORD) {
                  p.setAllowFlight(false);
               } else {
                  p.setVelocity(p.getLocation().getDirection().multiply(1.2D).setY(0.8D));
                  p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0F, 1.2F);
                  p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 10, 0.2D, 0.1D, 0.2D, 0.1D);
                  p.setAllowFlight(false);
                  this.plugin.gameManager.jumpCooldown.add(id);
                  (new BukkitRunnable() {
                     public void run() {
                        if (p.isOnGround()) {
                           EventListener.this.plugin.gameManager.jumpCooldown.remove(id);
                           if ("LOBBY".equals(EventListener.this.plugin.gameManager.playerState.get(id))) {
                              ItemStack currentItem = p.getInventory().getItemInMainHand();
                              if (currentItem.getType() != Material.DIAMOND_SWORD) {
                                 p.setAllowFlight(true);
                              }
                           }

                           this.cancel();
                        }

                     }
                  }).runTaskTimer(this.plugin, 10L, 5L);
               }
            }
         }
      }
   }

   @EventHandler
   public void onWorldChange(PlayerChangedWorldEvent e) {
      Player p = e.getPlayer();
      if ("LOBBY".equals(this.plugin.gameManager.playerState.get(p.getUniqueId()))) {
         p.getInventory().clear();
         p.getInventory().setArmorContents((ItemStack[])null);
      }

   }

   @EventHandler
   public void onItemChange(PlayerItemHeldEvent e) {
      Player p = e.getPlayer();
      if ("LOBBY".equals(this.plugin.gameManager.playerState.get(p.getUniqueId()))) {
         ItemStack newItem = p.getInventory().getItem(e.getNewSlot());
         if (newItem != null && newItem.getType() == Material.DIAMOND_SWORD) {
            p.setAllowFlight(false);
            p.setFlying(false);
         } else if (!this.plugin.gameManager.jumpCooldown.contains(p.getUniqueId())) {
            p.setAllowFlight(true);
         }

      }
   }

   @EventHandler
   public void onEntityTarget(EntityTargetEvent e) {
      if (this.plugin.cosmeticsManager.activePets.containsValue(e.getEntity()) || e.getEntityType() == EntityType.WITHER || e.getEntityType().name().equals("WARDEN")) {
         e.setCancelled(true);
      }

   }

   @EventHandler
   public void onEntityChangeBlock(EntityChangeBlockEvent e) {
      if (this.plugin.cosmeticsManager.activePets.containsValue(e.getEntity())) {
         e.setCancelled(true);
      }

   }

   @EventHandler
   public void onEntityCombust(EntityCombustEvent e) {
      if (this.plugin.cosmeticsManager.activePets.containsValue(e.getEntity())) {
         e.setCancelled(true);
      }

   }

   @EventHandler
   public void onProjectileLaunchWither(ProjectileLaunchEvent e) {
      if (e.getEntity().getShooter() instanceof Wither) {
         e.setCancelled(true);
      }

   }

   @EventHandler
   public void onEntityChangeBlockWither(EntityChangeBlockEvent e) {
      if (e.getEntityType() == EntityType.WITHER) {
         e.setCancelled(true);
      }

   }

   @EventHandler
   public void onChat(AsyncPlayerChatEvent e) {
      Player p = e.getPlayer();
      int prestige = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".prestige", 0);
      if (prestige > 0) {
         String stars = "";

         for(int i = 0; i < prestige; ++i) {
            stars = stars + "⭐";
         }

         e.setFormat(ChatColor.YELLOW + "[" + stars + "] " + ChatColor.RESET + e.getFormat());
      }

   }

   private void blockCommand(PlayerCommandPreprocessEvent e, Player p) {
      String msg = e.getMessage().toLowerCase();
      if (!msg.startsWith("/ffa leave") && !msg.startsWith("/1v1 leave") && !msg.startsWith("/leave")) {
         e.setCancelled(true);
         p.sendMessage(this.plugin.msg("combat.blocked-command"));
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onRespawn(PlayerRespawnEvent e) {
      Player p = e.getPlayer();
      if (this.plugin.gameManager.isInDuel(p)) {
         GameManager.DuelMatch match = this.plugin.gameManager.getMatch(p);
         Location spawn = this.plugin.arenasConfig.getLocation("duels." + match.arenaName + ".1");
         if (spawn != null) {
            e.setRespawnLocation(spawn);
         }
      }

   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockBreak(BlockBreakEvent e) {
      this.handlePlayerBuild(e.getPlayer(), e.getBlock(), (BlockState)null, e);
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockPlace(BlockPlaceEvent e) {
      this.handlePlayerBuild(e.getPlayer(), e.getBlock(), e.getBlockReplacedState(), e);
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBucketEmpty(PlayerBucketEmptyEvent e) {
      this.handlePlayerBuild(e.getPlayer(), e.getBlock().getRelative(e.getBlockFace()), (BlockState)null, e);
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBucketFill(PlayerBucketFillEvent e) {
      this.handlePlayerBuild(e.getPlayer(), e.getBlock(), (BlockState)null, e);
   }

   private void handlePlayerBuild(Player p, Block block, BlockState oldState, Cancellable e) {
      String arena = null;
      boolean allowBuild = false;
      GameManager.DuelMatch match = this.plugin.gameManager.getMatch(p);
      if (match != null) {
         arena = match.arenaName;
         allowBuild = this.plugin.kitsConfig.getBoolean("kits." + match.kitName + ".allowBuild", false);
      } else {
         arena = this.plugin.gameManager.getPlayerArena(p);
         if (arena != null) {
            allowBuild = this.plugin.arenasConfig.getBoolean("ffa." + arena + ".allowBuild", false);
         }
      }

      if (arena != null) {
         if (oldState != null) {
            this.plugin.restoreManager.recordBlockState(arena, oldState);
         } else {
            this.plugin.restoreManager.recordBlock(arena, block);
         }

         if (!allowBuild && !this.canBypass(p)) {
            e.setCancelled(true);
         }
      } else if (!this.canBypass(p)) {
         e.setCancelled(true);
      }

   }

   @EventHandler
   public void onBlockForm(BlockFormEvent e) {
      String arena = this.getArenaFromLocation(e.getBlock().getLocation());
      if (arena != null) {
         e.setCancelled(true);
      }

   }

   @EventHandler
   public void onEntityExplode(EntityExplodeEvent e) {
      e.setYield(0.0F);
      String arena = this.getArenaFromLocation(e.getLocation());
      if (arena != null) {
         Iterator var3 = e.blockList().iterator();

         while(var3.hasNext()) {
            Block b = (Block)var3.next();
            this.plugin.restoreManager.recordBlock(arena, b);
         }
      } else {
         e.blockList().clear();
      }

   }

   private String getArenaFromLocation(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         Iterator var2 = this.plugin.gameManager.activeDuels.iterator();

         Location spawn;
         while(var2.hasNext()) {
            GameManager.DuelMatch m = (GameManager.DuelMatch)var2.next();
            spawn = this.plugin.arenasConfig.getLocation("duels." + m.arenaName + ".1");
            if (spawn != null && spawn.getWorld().equals(loc.getWorld()) && spawn.distance(loc) < 200.0D) {
               return m.arenaName;
            }
         }

         String arena;
         if (this.plugin.arenasConfig.contains("duels")) {
            var2 = this.plugin.arenasConfig.getConfigurationSection("duels").getKeys(false).iterator();

            while(var2.hasNext()) {
               arena = (String)var2.next();
               spawn = this.plugin.arenasConfig.getLocation("duels." + arena + ".1");
               if (spawn != null && spawn.getWorld().equals(loc.getWorld()) && spawn.distance(loc) < 200.0D) {
                  return arena;
               }
            }
         }

         if (this.plugin.arenasConfig.contains("ffa")) {
            var2 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

            while(var2.hasNext()) {
               arena = (String)var2.next();
               spawn = this.plugin.arenasConfig.getLocation("ffa." + arena + ".spawn");
               if (spawn != null && spawn.getWorld().equals(loc.getWorld()) && spawn.distance(loc) < 200.0D) {
                  return arena;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private String getArenaName(Player p) {
      GameManager.DuelMatch match = this.plugin.gameManager.getMatch(p);
      return match != null ? match.arenaName : this.plugin.gameManager.getPlayerArena(p);
   }

   private boolean isBuildAllowed(String arenaName) {
      if (this.plugin.arenasConfig.contains("ffa." + arenaName)) {
         return this.plugin.arenasConfig.getBoolean("ffa." + arenaName + ".allowBuild", false);
      } else {
         Iterator var2 = this.plugin.gameManager.activeDuels.iterator();

         GameManager.DuelMatch m;
         do {
            if (!var2.hasNext()) {
               return false;
            }

            m = (GameManager.DuelMatch)var2.next();
         } while(!m.arenaName.equals(arenaName));

         return this.plugin.kitsConfig.getBoolean("kits." + m.kitName + ".allowBuild", false);
      }
   }

   private boolean canBypass(Player p) {
      return p.getGameMode() == GameMode.CREATIVE && p.hasPermission("ffa.admin") || this.plugin.gameManager.buildMode.contains(p.getUniqueId());
   }
}
