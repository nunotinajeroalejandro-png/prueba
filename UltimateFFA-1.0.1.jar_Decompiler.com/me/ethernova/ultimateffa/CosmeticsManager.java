package me.ethernova.ultimateffa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Cat.Type;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CosmeticsManager {
   private final Main plugin;
   public Map<UUID, String> editingArmorType = new HashMap();
   public Map<UUID, ItemStack[]> previewBackup = new HashMap();
   public Map<UUID, Entity> activePets = new HashMap();
   public Map<UUID, String> petCache = new HashMap();
   public Map<UUID, Boolean> petOnHead = new HashMap();
   public Map<UUID, String> auraPreview = new HashMap();
   public Map<UUID, Set<String>> pausedCosmetics = new HashMap();

   public CosmeticsManager(Main plugin) {
      this.plugin = plugin;
   }

   private Sound getSound(String sName) {
      try {
         byte var3 = -1;
         switch(sName.hashCode()) {
         case -2125864634:
            if (sName.equals("VILLAGER")) {
               var3 = 1;
            }
            break;
         case -1781303918:
            if (sName.equals("ENDERMAN")) {
               var3 = 9;
            }
            break;
         case -1348968106:
            if (sName.equals("LEVEL_UP")) {
               var3 = 4;
            }
            break;
         case -591166271:
            if (sName.equals("EXPLODE")) {
               var3 = 5;
            }
            break;
         case 66486:
            if (sName.equals("CAT")) {
               var3 = 6;
            }
            break;
         case 2034947:
            if (sName.equals("BELL")) {
               var3 = 8;
            }
            break;
         case 2050513:
            if (sName.equals("BURP")) {
               var3 = 3;
            }
            break;
         case 2193179:
            if (sName.equals("GOAT")) {
               var3 = 7;
            }
            break;
         case 62437548:
            if (sName.equals("ANVIL")) {
               var3 = 0;
            }
            break;
         case 67899228:
            if (sName.equals("GLASS")) {
               var3 = 2;
            }
         }

         switch(var3) {
         case 0:
            return Sound.BLOCK_ANVIL_LAND;
         case 1:
            return Sound.ENTITY_VILLAGER_TRADE;
         case 2:
            return Sound.BLOCK_GLASS_BREAK;
         case 3:
            return Sound.ENTITY_PLAYER_BURP;
         case 4:
            return Sound.ENTITY_PLAYER_LEVELUP;
         case 5:
            return Sound.ENTITY_GENERIC_EXPLODE;
         case 6:
            return Sound.ENTITY_CAT_AMBIENT;
         case 7:
            return Sound.ENTITY_GOAT_SCREAMING_AMBIENT;
         case 8:
            return Sound.BLOCK_BELL_USE;
         case 9:
            return Sound.ENTITY_ENDERMAN_SCREAM;
         default:
            return Sound.valueOf(sName);
         }
      } catch (Exception var4) {
         return null;
      }
   }

   public void fillDiscoGlass(Inventory inv) {
      Random r = new Random();
      Material[] glasses = new Material[]{Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE};

      for(int i = 0; i < inv.getSize(); ++i) {
         ItemStack current = inv.getItem(i);
         if (current == null || current.getType() == Material.AIR || current.getType().name().contains("STAINED_GLASS_PANE")) {
            ItemStack glass = new ItemStack(glasses[r.nextInt(glasses.length)]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
            inv.setItem(i, glass);
         }
      }

   }

   public void handleShopClick(Player p, String rawTitle, ItemStack item, boolean isRightClick) {
      if (item != null && item.hasItemMeta()) {
         String title = ChatColor.stripColor(rawTitle);
         String titleCheck = title.toUpperCase();
         String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
         if (!name.contains("Volver") && !name.contains("Cerrar") && !name.contains("Menú")) {
            int streak;
            int bal;
            if (isRightClick) {
               if (titleCheck.contains("AURAS")) {
                  CosmeticsManager.AuraType[] var23 = CosmeticsManager.AuraType.values();
                  streak = var23.length;

                  for(bal = 0; bal < streak; ++bal) {
                     CosmeticsManager.AuraType a = var23[bal];
                     if (name.contains(a.name)) {
                        this.startAuraPreview(p, a.name());
                        return;
                     }
                  }
               }

               if (titleCheck.contains("EFECTOS")) {
                  CosmeticsManager.KillEffect[] var25 = CosmeticsManager.KillEffect.values();
                  streak = var25.length;

                  for(bal = 0; bal < streak; ++bal) {
                     CosmeticsManager.KillEffect k = var25[bal];
                     if (name.contains(k.name)) {
                        this.startKillEffectPreview(p, k);
                        return;
                     }
                  }
               }

               if (titleCheck.contains("RASTROS")) {
                  CosmeticsManager.ProjectileTrail[] var28 = CosmeticsManager.ProjectileTrail.values();
                  streak = var28.length;

                  for(bal = 0; bal < streak; ++bal) {
                     CosmeticsManager.ProjectileTrail t = var28[bal];
                     if (name.contains(t.name)) {
                        this.startProjectileTrailPreview(p, t);
                        return;
                     }
                  }
               }

               if (titleCheck.contains("SONIDOS")) {
                  CosmeticsManager.DeathSound[] var30 = CosmeticsManager.DeathSound.values();
                  streak = var30.length;

                  for(bal = 0; bal < streak; ++bal) {
                     CosmeticsManager.DeathSound ds = var30[bal];
                     if (ds.name.equals(name)) {
                        Sound s = this.getSound(ds.name());
                        if (s != null) {
                           p.playSound(p.getLocation(), s, 1.0F, 1.0F);
                           p.sendMessage("§a♪ " + ds.name);
                        }

                        return;
                     }
                  }
               }

               if (titleCheck.startsWith("EDITOR") && titleCheck.contains("SELECCIONA")) {
                  this.plugin.kitsConfig.set("kits." + name, (Object)null);
                  this.plugin.saveKits();
                  p.sendMessage("§cKit eliminado correctamente.");
                  this.plugin.gameManager.openKitEditorList(p);
               }
            } else {
               Player target;
               if ((titleCheck.contains("KIT") || titleCheck.contains("RETO")) && !titleCheck.startsWith("EDITOR")) {
                  if (this.plugin.gameManager.pendingInvite.containsKey(p.getUniqueId())) {
                     UUID targetUUID = (UUID)this.plugin.gameManager.pendingInvite.remove(p.getUniqueId());
                     target = Bukkit.getPlayer(targetUUID);
                     bal = (Integer)this.plugin.gameManager.tempBets.getOrDefault(p.getUniqueId(), 0);
                     this.plugin.gameManager.tempBets.remove(p.getUniqueId());
                     p.closeInventory();
                     if (target != null && target.isOnline()) {
                        if (bal > 0) {
                           if (this.plugin.cosmeticsManager.getBalance(target) < bal) {
                              p.sendMessage("§cEl rival no tiene suficiente dinero para igualar la apuesta ($" + bal + ").");
                              return;
                           }

                           this.plugin.gameManager.sendDuelInvite(p, target, name, bal);
                        } else {
                           this.plugin.gameManager.sendDuelInvite(p, target, name, 0);
                        }
                     } else {
                        p.sendMessage("§cEl jugador se desconectó.");
                     }
                  } else {
                     this.plugin.gameManager.joinQueue(p, name);
                     p.closeInventory();
                  }

               } else {
                  String arenaName;
                  if (titleCheck.contains("ESPECTAR")) {
                     arenaName = ChatColor.stripColor(item.getItemMeta().getDisplayName()).trim();
                     target = Bukkit.getPlayer(arenaName);
                     if (target == p) {
                        p.sendMessage("§cNo puedes espectearte a ti mismo.");
                     } else {
                        if (target != null && target.isOnline()) {
                           if (!this.plugin.gameManager.acceptsSpectators(target)) {
                              p.closeInventory();
                              p.sendMessage("§c" + target.getName() + " tiene desactivado el modo espectador.");
                              p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                              return;
                           }

                           if (this.plugin.gameManager.isInDuel(target)) {
                              p.closeInventory();
                              this.plugin.gameManager.setSpectator(p, target);
                           } else {
                              p.sendMessage("§cEl jugador ya no está en duelo.");
                              this.openSpectatorGUI(p);
                           }
                        } else {
                           p.sendMessage(this.plugin.msg("spectator.target-not-found"));
                        }

                     }
                  } else if (titleCheck.contains("SELECTOR") && titleCheck.contains("RANKING")) {
                     if (name.contains("Top ")) {
                        this.openTopKillsList(p, name.replace("Top ", "").trim());
                     }

                  } else if (!titleCheck.contains("SISTEMA DE RANKING") && !titleCheck.equals("RANKING")) {
                     if (!titleCheck.contains("ARMERÍA") && !titleCheck.contains("EDITANDO") && (!titleCheck.contains("SELECCIONA") || titleCheck.contains("KIT"))) {
                        if (titleCheck.contains("PRESTIGIO")) {
                           if (name.contains("SUBIR")) {
                              p.closeInventory();
                              p.performCommand("prestige");
                           }

                        } else if (titleCheck.contains("GESTIÓN DE USUARIOS")) {
                           if (item.getType() == Material.PLAYER_HEAD) {
                              this.openPlayerEditor(p, name);
                           }

                        } else if (titleCheck.contains("ADMINISTRAR")) {
                           arenaName = title.replace("Administrar:", "").replace("Administrar", "").trim();
                           if (!arenaName.isEmpty()) {
                              OfflinePlayer target = Bukkit.getOfflinePlayer(arenaName);
                              if (!name.contains("Dar") && !name.contains("$")) {
                                 if (!name.contains("Reset") && !name.contains("Stats")) {
                                    if (name.contains("Teleport") || name.contains("Teletransportarse") || item.getType() == Material.COMPASS) {
                                       if (target.isOnline()) {
                                          p.teleport(target.getPlayer());
                                          p.sendMessage("§b✈ Yendo a " + arenaName);
                                          p.closeInventory();
                                       } else {
                                          p.sendMessage("§cEl jugador no está conectado.");
                                       }
                                    }
                                 } else {
                                    this.plugin.statsConfig.set("players." + target.getUniqueId(), (Object)null);
                                    this.plugin.saveStats();
                                    p.sendMessage("§cStats de " + arenaName + " borradas.");
                                    p.closeInventory();
                                 }
                              } else if (target.isOnline()) {
                                 this.addMoney(target.getPlayer(), 1000);
                                 p.sendMessage("§a+$1000 a " + arenaName);
                              } else {
                                 bal = this.plugin.statsConfig.getInt("players." + target.getUniqueId() + ".balance", 0);
                                 this.plugin.statsConfig.set("players." + target.getUniqueId() + ".balance", bal + 1000);
                                 this.plugin.saveStats();
                                 p.sendMessage("§a(Offline) +$1000 a " + arenaName);
                              }

                           }
                        } else if (!titleCheck.contains("MASCOTAS") && !titleCheck.contains("VARIANTES")) {
                           if (!titleCheck.contains("CALENDARIO")) {
                              if (titleCheck.contains("ADMIN")) {
                                 if (name.contains("Reload")) {
                                    p.performCommand("ffa reload");
                                    p.closeInventory();
                                 } else if (name.contains("Gestor")) {
                                    this.plugin.gameManager.openArenaEditorGUI(p);
                                 } else if (name.contains("Editor")) {
                                    this.plugin.gameManager.openKitEditorList(p);
                                 } else if (name.contains("Resetear")) {
                                    this.plugin.gameManager.resetGlobalStats();
                                    p.closeInventory();
                                 } else if (name.contains("Usuarios")) {
                                    this.openAllPlayersGUI(p);
                                 }

                              } else if (titleCheck.startsWith("EDITOR")) {
                                 if (titleCheck.contains("SELECCIONA")) {
                                    this.plugin.kitsConfig.set("kits." + name + ".inventory", p.getInventory().getContents());
                                    this.plugin.kitsConfig.set("kits." + name + ".armor", p.getInventory().getArmorContents());
                                    this.plugin.saveKits();
                                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
                                    p.sendMessage("§aKit actualizado con tu inventario.");
                                    p.closeInventory();
                                 }

                              } else if (!titleCheck.contains("DUELOS") && !titleCheck.contains("COLA")) {
                                 if (!titleCheck.contains("COSMÉTICOS") && !titleCheck.contains("TIENDA")) {
                                    if (titleCheck.contains("BORRAR ARENAS")) {
                                       p.performCommand("ffa deletearena " + name);
                                       p.closeInventory();
                                    } else if (titleCheck.contains("ARENAS")) {
                                       if (!item.getType().name().contains("GLASS")) {
                                          arenaName = ChatColor.stripColor(name).trim();
                                          p.closeInventory();
                                          this.plugin.gameManager.joinFFA(p, arenaName);
                                       }

                                    }
                                 } else {
                                    if (name.contains("Efectos")) {
                                       this.openCategoryShop(p, "§b§lTIENDA §8» §fEfectos", CosmeticsManager.KillEffect.values(), "effect");
                                    } else if (name.contains("Mensajes de Muerte")) {
                                       this.openCategoryShop(p, "§b§lTIENDA §8» §fMensajes", CosmeticsManager.DeathMessage.values(), "msg");
                                    } else if (name.contains("Mensajes de Entrada")) {
                                       this.openCategoryShop(p, "§b§lTIENDA §8» §fEntrada", CosmeticsManager.JoinMessage.values(), "join");
                                    } else if (name.contains("Sonidos")) {
                                       this.openCategoryShop(p, "§b§lTIENDA §8» §fSonidos", CosmeticsManager.DeathSound.values(), "sound");
                                    } else if (name.contains("Rastros")) {
                                       this.openCategoryShop(p, "§b§lTIENDA §8» §fRastros", CosmeticsManager.ProjectileTrail.values(), "trail");
                                    } else if (name.contains("Auras")) {
                                       this.openCategoryShop(p, "§b§lTIENDA §8» §fAuras", CosmeticsManager.AuraType.values(), "aura");
                                    } else if (name.contains("Mascotas")) {
                                       this.openPetCategoryMenu(p);
                                    } else if (name.contains("Prestigio")) {
                                       this.openPrestigeGUI(p);
                                    } else if (name.contains("Armaduras")) {
                                       this.openArmorSelector(p);
                                    } else if (name.contains("Caja")) {
                                       this.buyMysteryBox(p);
                                    }

                                    if (titleCheck.contains("»")) {
                                       if (titleCheck.contains("EFECTOS")) {
                                          this.processPurchase(p, CosmeticsManager.KillEffect.values(), name, "effect", () -> {
                                             this.openCategoryShop(p, rawTitle, CosmeticsManager.KillEffect.values(), "effect");
                                          });
                                       } else if (titleCheck.contains("AURAS")) {
                                          this.processPurchase(p, CosmeticsManager.AuraType.values(), name, "aura", () -> {
                                             this.openCategoryShop(p, rawTitle, CosmeticsManager.AuraType.values(), "aura");
                                          });
                                       } else if (titleCheck.contains("MENSAJES")) {
                                          this.processPurchase(p, CosmeticsManager.DeathMessage.values(), name, "msg", () -> {
                                             this.openCategoryShop(p, rawTitle, CosmeticsManager.DeathMessage.values(), "msg");
                                          });
                                       } else if (titleCheck.contains("SONIDOS")) {
                                          this.processPurchase(p, CosmeticsManager.DeathSound.values(), name, "sound", () -> {
                                             this.openCategoryShop(p, rawTitle, CosmeticsManager.DeathSound.values(), "sound");
                                          });
                                       } else if (titleCheck.contains("RASTROS")) {
                                          this.processPurchase(p, CosmeticsManager.ProjectileTrail.values(), name, "trail", () -> {
                                             this.openCategoryShop(p, rawTitle, CosmeticsManager.ProjectileTrail.values(), "trail");
                                          });
                                       } else if (titleCheck.contains("ENTRADA")) {
                                          this.processPurchase(p, CosmeticsManager.JoinMessage.values(), name, "join", () -> {
                                             this.openCategoryShop(p, rawTitle, CosmeticsManager.JoinMessage.values(), "join");
                                          });
                                       }
                                    }

                                 }
                              } else {
                                 if (name.contains("Cola Rápida")) {
                                    this.plugin.gameManager.joinQueue(p, "Random");
                                    p.closeInventory();
                                 } else if (name.contains("Cola por Kit")) {
                                    this.plugin.gameManager.openKitSelector(p, false);
                                 }

                              }
                           } else {
                              if (name.contains("Día")) {
                                 int day = Integer.parseInt(name.replaceAll("[^0-9]", "")) - 1;
                                 streak = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".daily_streak", 0);
                                 long last = this.plugin.statsConfig.getLong("players." + p.getUniqueId() + ".last_claim", 0L);
                                 long now = System.currentTimeMillis();
                                 if (day != streak || now - last < 86400000L && last != 0L) {
                                    p.sendMessage("§cNo puedes reclamar este premio aún.");
                                 } else {
                                    int[] rewards = new int[]{100, 250, 500, 1000, 2000, 3500, 5000};
                                    this.addMoney(p, rewards[day]);
                                    this.plugin.statsConfig.set("players." + p.getUniqueId() + ".daily_streak", streak + 1);
                                    this.plugin.statsConfig.set("players." + p.getUniqueId() + ".last_claim", now);
                                    this.plugin.saveStats();
                                    p.sendMessage("§a¡Recompensa del Día " + (day + 1) + " reclamada!");
                                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F);
                                    p.closeInventory();
                                 }
                              }

                           }
                        } else {
                           if (titleCheck.contains("VARIANTES")) {
                              arenaName = title.replace("Variantes: ", "").trim();
                              String variantRaw = name.replace("Baby ", "").replace("Adulta", "").replace("Normal", "DEFAULT").trim();
                              String saveValue = arenaName + ":" + variantRaw;
                              if (name.contains("Bebé") || name.contains("Baby")) {
                                 saveValue = saveValue + "_BABY";
                              }

                              this.spawnPet(p, saveValue);
                              p.closeInventory();
                              p.sendMessage("§aMascota seleccionada.");
                           } else if (name.toUpperCase().contains("POSICIÓN")) {
                              boolean current = (Boolean)this.petOnHead.getOrDefault(p.getUniqueId(), false);
                              this.petOnHead.put(p.getUniqueId(), !current);
                              if (this.activePets.containsKey(p.getUniqueId())) {
                                 this.spawnPet(p, this.getActiveCosmetic(p, "pet"));
                              }

                              this.openPetCategoryMenu(p);
                           } else if (name.contains("Quitar")) {
                              this.spawnPet(p, "NONE");
                              p.closeInventory();
                              p.sendMessage("§cMascota quitada.");
                           } else {
                              CosmeticsManager.PetCategory[] var16 = CosmeticsManager.PetCategory.values();
                              streak = var16.length;

                              for(bal = 0; bal < streak; ++bal) {
                                 CosmeticsManager.PetCategory cat = var16[bal];
                                 if (name.equalsIgnoreCase(cat.name)) {
                                    if (!this.hasUnlocked(p, "pet." + cat.name()) && cat.cost != 0) {
                                       this.tryPurchase(p, cat.cost, "pet." + cat.name(), cat.name, () -> {
                                          this.openVariantMenu(p, cat.name());
                                       });
                                    } else {
                                       this.openVariantMenu(p, cat.name());
                                    }

                                    return;
                                 }
                              }
                           }

                        }
                     } else {
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5F, 1.5F);
                        if (name.contains("Diamante")) {
                           this.openTrimEditMenu(p, "DIAMOND");
                        } else if (name.contains("Netherite")) {
                           this.openTrimEditMenu(p, "NETHERITE");
                        } else if (name.contains("Patrón")) {
                           this.openPatternSelector(p);
                        } else if (name.contains("Material")) {
                           this.openMaterialSelector(p);
                        } else if (titleCheck.contains("DISEÑO")) {
                           arenaName = this.findTrimPatternEnum(name);
                           this.saveTrimPreference(p, "pattern", arenaName);
                           this.openTrimEditMenu(p, (String)this.editingArmorType.get(p.getUniqueId()));
                        } else if (titleCheck.contains("COLOR")) {
                           arenaName = this.findTrimMaterialEnum(name);
                           this.saveTrimPreference(p, "material", arenaName);
                           this.openTrimEditMenu(p, (String)this.editingArmorType.get(p.getUniqueId()));
                        } else if (item.getType().name().contains("CHESTPLATE")) {
                           this.startLivePreview(p, (String)this.editingArmorType.get(p.getUniqueId()));
                        }

                     }
                  } else {
                     if (!name.contains("PERFIL") && !name.contains("TU PERFIL")) {
                        if (name.contains("GLOBAL")) {
                           this.openTopKillsList(p, (String)null);
                        } else if (name.contains("ARENAS")) {
                           this.openTopKillsMainMenu(p);
                        }
                     } else {
                        this.plugin.gameManager.openPersonalStats(p);
                     }

                  }
               }
            }
         } else {
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0F, 1.0F);
            if (titleCheck.contains("VARIANTES")) {
               this.openPetCategoryMenu(p);
            } else if (!titleCheck.contains("EDITANDO") && !titleCheck.contains("DISEÑO") && !titleCheck.contains("COLOR") && !titleCheck.contains("MATERIAL")) {
               if (titleCheck.contains("ESTADÍSTICAS") || titleCheck.startsWith("TOP") || titleCheck.contains("SELECTOR") && titleCheck.contains("RANKING")) {
                  if (titleCheck.contains("TOP") && !titleCheck.contains("GLOBAL")) {
                     this.openTopKillsMainMenu(p);
                  } else {
                     this.plugin.gameManager.openRankingMainMenu(p);
                  }

               } else if (!titleCheck.contains("ESPECTAR") && !titleCheck.contains("ADMINISTRAR") && !titleCheck.contains("EDITOR DE KITS") && !titleCheck.contains("BORRAR") && !titleCheck.contains("GESTIÓN")) {
                  if (!titleCheck.contains("COLA") && !titleCheck.contains("KIT PARA")) {
                     if (titleCheck.contains("TIENDA") && titleCheck.contains("»")) {
                        this.openShopGUI(p);
                     } else {
                        p.closeInventory();
                     }
                  } else {
                     this.plugin.gameManager.openDuelMenu(p);
                  }
               } else {
                  if (titleCheck.contains("ADMINISTRAR")) {
                     this.openAllPlayersGUI(p);
                  } else {
                     this.plugin.gameManager.openAdminGUI(p);
                  }

               }
            } else {
               if (!titleCheck.contains("DISEÑO") && !titleCheck.contains("COLOR")) {
                  this.openArmorSelector(p);
               } else {
                  this.openTrimEditMenu(p, (String)this.editingArmorType.get(p.getUniqueId()));
               }

            }
         }
      }
   }

   public void startAuraPreview(final Player p, final String auraName) {
      final UUID id = p.getUniqueId();
      if (this.auraPreview.containsKey(id)) {
         this.auraPreview.remove(id);
         this.resumeCosmetic(p, "aura");
      }

      this.auraPreview.put(id, auraName);
      this.pauseCosmetic(p, "aura");
      p.sendMessage("§b§lFFA §8» §f§b§lVISTA PREVIA §8» §fProbando aura §e" + auraName);
      p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.5F);
      p.closeInventory();
      (new BukkitRunnable() {
         public void run() {
            if (CosmeticsManager.this.auraPreview.containsKey(id) && ((String)CosmeticsManager.this.auraPreview.get(id)).equals(auraName)) {
               CosmeticsManager.this.auraPreview.remove(id);
               CosmeticsManager.this.resumeCosmetic(p, "aura");
               if (p.isOnline()) {
                  p.sendMessage("§b§lFFA §8» §f§7Vista previa finalizada.");
                  CosmeticsManager.this.openCategoryShop(p, "§b§lTIENDA §8» §fAuras", CosmeticsManager.AuraType.values(), "aura");
               }
            }

         }
      }).runTaskLater(this.plugin, 100L);
   }

   public void startKillEffectPreview(final Player p, final CosmeticsManager.KillEffect effect) {
      p.closeInventory();
      p.sendMessage("§b§lFFA §8» §f§d§lVISTA PREVIA §8» §fEfecto §e" + effect.name);
      Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(2));
      loc.setY(p.getLocation().getY());
      final Zombie z = (Zombie)p.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
      z.setAI(false);
      z.setCustomName("§cTest Dummy");
      z.setCustomNameVisible(false);
      z.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
      (new BukkitRunnable() {
         public void run() {
            if (z.isValid()) {
               CosmeticsManager.this.playKillEffect(p, z.getLocation(), effect);
               z.remove();
            }

         }
      }).runTaskLater(this.plugin, 20L);
      (new BukkitRunnable() {
         public void run() {
            if (p.isOnline()) {
               CosmeticsManager.this.openCategoryShop(p, "§b§lTIENDA §8» §fEfectos", CosmeticsManager.KillEffect.values(), "effect");
            }

         }
      }).runTaskLater(this.plugin, 60L);
   }

   public void startProjectileTrailPreview(final Player p, final CosmeticsManager.ProjectileTrail trail) {
      p.closeInventory();
      p.sendMessage("§b§lFFA §8» §f§e§lVISTA PREVIA §8» §fRastro §b" + trail.name);
      this.resumeCosmetic(p, "trail");
      this.pauseCosmetic(p, "trail");
      final Arrow a = (Arrow)p.launchProjectile(Arrow.class);
      a.setGravity(false);
      a.setVelocity(p.getLocation().getDirection().multiply(0.5D));
      a.setMetadata("isPreview", new FixedMetadataValue(this.plugin, true));
      (new BukkitRunnable() {
         int tick = 0;

         public void run() {
            if (this.tick++ <= 60 && !a.isDead() && !a.isOnGround()) {
               String n = trail.name();
               Particle part = Particle.CRIT;
               if (n.contains("FIRE")) {
                  part = Particle.FLAME;
               } else if (!n.contains("HEART") && !n.contains("LOVE")) {
                  if (n.contains("MAGIC")) {
                     part = Particle.ENCHANTMENT_TABLE;
                  } else if (n.contains("NOTE")) {
                     part = Particle.NOTE;
                  } else if (n.contains("VOID")) {
                     part = Particle.SCULK_SOUL;
                  } else if (n.contains("TOTEM")) {
                     part = Particle.TOTEM;
                  } else if (n.contains("WITCH")) {
                     part = Particle.SPELL_WITCH;
                  } else if (n.contains("LAVA")) {
                     part = Particle.DRIP_LAVA;
                  } else if (n.contains("SMOKE")) {
                     part = Particle.SMOKE_LARGE;
                  } else if (n.contains("SNOW")) {
                     part = Particle.SNOWBALL;
                  } else if (n.contains("SPARK")) {
                     part = Particle.ELECTRIC_SPARK;
                  } else if (n.contains("CLOUD")) {
                     part = Particle.CLOUD;
                  } else if (n.contains("WATER")) {
                     part = Particle.DRIP_WATER;
                  } else if (n.contains("SLIME")) {
                     part = Particle.SLIME;
                  } else if (n.contains("ASH")) {
                     part = Particle.ASH;
                  } else if (n.contains("GLOW")) {
                     part = Particle.GLOW_SQUID_INK;
                  } else if (n.contains("INK")) {
                     part = Particle.SQUID_INK;
                  } else if (n.contains("SOUL")) {
                     part = Particle.SOUL;
                  } else if (n.contains("CHERRY")) {
                     part = Particle.CHERRY_LEAVES;
                  } else if (n.contains("END")) {
                     part = Particle.END_ROD;
                  } else if (n.contains("GOLD")) {
                     part = Particle.WAX_ON;
                  } else if (n.contains("EMERALD")) {
                     part = Particle.VILLAGER_HAPPY;
                  }
               } else {
                  part = Particle.HEART;
               }

               if (n.equals("RAINBOW")) {
                  DustOptions dust = new DustOptions(Color.fromRGB((new Random()).nextInt(255), (new Random()).nextInt(255), (new Random()).nextInt(255)), 1.0F);
                  a.getWorld().spawnParticle(Particle.REDSTONE, a.getLocation(), 1, 0.0D, 0.0D, 0.0D, 0.0D, dust);
               } else if (n.equals("ARCTIC")) {
                  a.getWorld().spawnParticle(Particle.SNOWFLAKE, a.getLocation(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
                  a.getWorld().spawnParticle(Particle.BLOCK_CRACK, a.getLocation(), 2, 0.1D, 0.1D, 0.1D, Material.BLUE_ICE.createBlockData());
               } else {
                  a.getWorld().spawnParticle(part, a.getLocation(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
               }

            } else {
               a.remove();
               CosmeticsManager.this.resumeCosmetic(p, "trail");
               if (p.isOnline()) {
                  CosmeticsManager.this.openCategoryShop(p, "§b§lTIENDA §8» §fRastros", CosmeticsManager.ProjectileTrail.values(), "trail");
               }

               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 1L, 1L);
   }

   public void openShopGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 45, "§b§lTIENDA §8» §fCosméticos");
      p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0F, 1.0F);
      inv.setItem(10, this.createItem(Material.DIAMOND_SWORD, ChatColor.RED + "Efectos de Kill"));
      inv.setItem(11, this.createItem(Material.WRITABLE_BOOK, ChatColor.AQUA + "Mensajes de Muerte"));
      inv.setItem(12, this.createItem(Material.NOTE_BLOCK, ChatColor.LIGHT_PURPLE + "Sonidos de Kill"));
      inv.setItem(13, this.createItem(Material.OAK_SIGN, ChatColor.GREEN + "Mensajes de Entrada"));
      inv.setItem(14, this.createItem(Material.NETHER_STAR, ChatColor.GOLD + "Auras / Partículas"));
      inv.setItem(15, this.createItem(Material.SPECTRAL_ARROW, ChatColor.YELLOW + "Rastros de Proyectil"));
      inv.setItem(16, this.createItem(Material.SMITHING_TABLE, ChatColor.BLUE + "Personalizar Armaduras"));
      inv.setItem(20, this.createItem(Material.NETHER_STAR, ChatColor.GOLD + "Tienda de Prestigio"));
      inv.setItem(22, this.createItem(Material.CREEPER_SPAWN_EGG, ChatColor.GREEN + "Gestor de Mascotas"));
      inv.setItem(24, this.createItem(Material.ENDER_CHEST, ChatColor.LIGHT_PURPLE + "Caja Misteriosa ($1000)"));
      inv.setItem(40, this.createItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Tu Dinero: $" + this.getBalance(p)));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openCategoryShop(Player p, String title, Object[] values, String typePrefix) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, title);
      String active = this.getActiveCosmetic(p, typePrefix);
      int slot = 0;
      Object[] var8 = values;
      int var9 = values.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         Object obj = var8[var10];
         if (slot >= 45) {
            break;
         }

         if (title.contains("Entrada")) {
            if (!(obj instanceof CosmeticsManager.JoinMessage)) {
               continue;
            }
         } else if (title.contains("Mensajes") && !(obj instanceof CosmeticsManager.DeathMessage)) {
            continue;
         }

         String name = "";
         int cost = 0;
         Material icon = Material.AIR;
         String enumName = "";
         boolean vip = false;
         String extraLore = "";
         if ((!title.contains("Entrada") || obj instanceof CosmeticsManager.JoinMessage) && (!title.contains("Mensajes") || obj instanceof CosmeticsManager.DeathMessage)) {
            if (obj instanceof CosmeticsManager.KillEffect) {
               CosmeticsManager.KillEffect e = (CosmeticsManager.KillEffect)obj;
               name = e.name;
               cost = e.cost;
               icon = e.icon;
               enumName = e.name();
               vip = e.vip;
            } else if (obj instanceof CosmeticsManager.DeathMessage) {
               CosmeticsManager.DeathMessage e = (CosmeticsManager.DeathMessage)obj;
               name = e.name;
               cost = e.cost;
               icon = e.icon;
               enumName = e.name();
               vip = e.vip;
               extraLore = e.text.replace("<victim>", "Jugador").replace("<killer>", p.getName());
            } else if (obj instanceof CosmeticsManager.JoinMessage) {
               CosmeticsManager.JoinMessage e = (CosmeticsManager.JoinMessage)obj;
               name = e.name;
               cost = e.cost;
               icon = e.icon;
               enumName = e.name();
               extraLore = e.text.replace("<player>", p.getName());
            } else if (obj instanceof CosmeticsManager.DeathSound) {
               CosmeticsManager.DeathSound e = (CosmeticsManager.DeathSound)obj;
               name = e.name;
               cost = e.cost;
               icon = e.icon;
               enumName = e.name();
               vip = e.vip;
            } else if (obj instanceof CosmeticsManager.ProjectileTrail) {
               CosmeticsManager.ProjectileTrail e = (CosmeticsManager.ProjectileTrail)obj;
               name = e.name;
               cost = e.cost;
               icon = e.icon;
               enumName = e.name();
               vip = e.vip;
            } else if (obj instanceof CosmeticsManager.AuraType) {
               CosmeticsManager.AuraType e = (CosmeticsManager.AuraType)obj;
               name = e.name;
               cost = e.cost;
               icon = e.icon;
               enumName = e.name();
               vip = e.vip;
            }

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e§l" + name);
            List<String> lore = new ArrayList();
            lore.add("§8§m-----------------------");
            if (vip) {
               lore.add("§d§l★ EXCLUSIVO VIP ★");
            }

            if (cost == 0) {
               lore.add("§a§lGRATIS");
            } else {
               lore.add("§7Precio: §6$" + cost);
            }

            if (!extraLore.isEmpty()) {
               lore.add("");
               lore.add("§7Vista previa:");
               lore.add("§f" + ChatColor.translateAlternateColorCodes('&', extraLore));
            }

            lore.add("");
            boolean unlocked = this.hasUnlocked(p, typePrefix + "." + enumName) || cost == 0;
            if (unlocked) {
               lore.add("§a§l✔ DESBLOQUEADO");
               if (active.equals(enumName)) {
                  lore.add("§b§l» §bACTIVO");
                  meta.addEnchant(Enchantment.DURABILITY, 1, true);
                  meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
               } else {
                  lore.add("§e§l» §fClic Izq: §6Equipar");
               }
            } else {
               lore.add("§c§l✖ BLOQUEADO");
               if (vip && !p.hasPermission("ffa.vip")) {
                  lore.add("§cRequiere Rango VIP");
               } else {
                  lore.add("§e§l» §fClic Izq: §aComprar");
               }
            }

            if (obj instanceof CosmeticsManager.DeathSound) {
               lore.add("§e§l» §fClic Der: §bEscuchar");
            } else if (obj instanceof CosmeticsManager.AuraType || obj instanceof CosmeticsManager.KillEffect || obj instanceof CosmeticsManager.ProjectileTrail) {
               lore.add("§e§l» §fClic Der: §bVista Previa");
            }

            lore.add("§8§m-----------------------");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
         }
      }

      inv.setItem(49, this.createItem(Material.ARROW, "§c« Volver al Menú"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openPetCategoryMenu(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "§a§lTIENDA §8» §fMascotas");
      boolean onHead = (Boolean)this.petOnHead.getOrDefault(p.getUniqueId(), false);
      ItemStack toggle;
      if (onHead) {
         toggle = this.createItem(Material.LEATHER_HELMET, "§a§lPOSICIÓN: §eEn Cabeza");
      } else {
         toggle = this.createItem(Material.LEATHER_BOOTS, "§b§lPOSICIÓN: §fCaminando");
      }

      ItemMeta tm = toggle.getItemMeta();
      List<String> tLore = new ArrayList();
      tLore.add("§8§m-----------------------");
      tLore.add("§7Actualmente: " + (onHead ? "§aEncima tuyo" : "§bCaminando"));
      tLore.add("");
      tLore.add("§e§l» §fClic para cambiar");
      tLore.add("§8§m-----------------------");
      tm.setLore(tLore);
      toggle.setItemMeta(tm);
      inv.setItem(4, toggle);
      inv.setItem(49, this.createItem(Material.BARRIER, ChatColor.RED + "Quitar Mascota"));
      int slot = 9;
      CosmeticsManager.PetCategory[] var8 = CosmeticsManager.PetCategory.values();
      int var9 = var8.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         CosmeticsManager.PetCategory cat = var8[var10];
         if (cat != CosmeticsManager.PetCategory.NONE && slot < 45) {
            this.addItemToInv(inv, slot++, cat, p);
         }
      }

      inv.setItem(53, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openVariantMenu(Player p, String petType) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "Variantes: " + petType);
      Object[] variants = null;
      boolean isTextureMob = false;
      boolean isAgeMob = false;
      int baseCost = false;
      byte var9 = -1;
      switch(petType.hashCode()) {
      case -2125864634:
         if (petType.equals("VILLAGER")) {
            var9 = 18;
         }
         break;
      case -1948396067:
         if (petType.equals("MOOSHROOM")) {
            var9 = 21;
         }
         break;
      case -1942082154:
         if (petType.equals("PARROT")) {
            var9 = 7;
         }
         break;
      case -1885316070:
         if (petType.equals("RABBIT")) {
            var9 = 1;
         }
         break;
      case -1809093316:
         if (petType.equals("TURTLE")) {
            var9 = 16;
         }
         break;
      case -1734240269:
         if (petType.equals("WITHER")) {
            var9 = 10;
         }
         break;
      case -1643025882:
         if (petType.equals("ZOMBIE")) {
            var9 = 9;
         }
         break;
      case -1343960133:
         if (petType.equals("SNIFFER")) {
            var9 = 23;
         }
         break;
      case -1163786087:
         if (petType.equals("STRIDER")) {
            var9 = 19;
         }
         break;
      case -291037131:
         if (petType.equals("POLAR_BEAR")) {
            var9 = 17;
         }
         break;
      case 66486:
         if (petType.equals("CAT")) {
            var9 = 0;
         }
         break;
      case 66923:
         if (petType.equals("COW")) {
            var9 = 12;
         }
         break;
      case 69807:
         if (petType.equals("FOX")) {
            var9 = 3;
         }
         break;
      case 79214:
         if (petType.equals("PIG")) {
            var9 = 13;
         }
         break;
      case 2166692:
         if (petType.equals("FROG")) {
            var9 = 8;
         }
         break;
      case 2193179:
         if (petType.equals("GOAT")) {
            var9 = 24;
         }
         break;
      case 2670162:
         if (petType.equals("WOLF")) {
            var9 = 15;
         }
         break;
      case 63888534:
         if (petType.equals("CAMEL")) {
            var9 = 22;
         }
         break;
      case 68928445:
         if (petType.equals("HORSE")) {
            var9 = 4;
         }
         break;
      case 72516629:
         if (petType.equals("LLAMA")) {
            var9 = 5;
         }
         break;
      case 75895226:
         if (petType.equals("PANDA")) {
            var9 = 6;
         }
         break;
      case 78865723:
         if (petType.equals("SHEEP")) {
            var9 = 14;
         }
         break;
      case 152863283:
         if (petType.equals("AXOLOTL")) {
            var9 = 2;
         }
         break;
      case 1463990677:
         if (petType.equals("CHICKEN")) {
            var9 = 11;
         }
         break;
      case 2136447569:
         if (petType.equals("HOGLIN")) {
            var9 = 20;
         }
      }

      switch(var9) {
      case 0:
         variants = CosmeticsManager.CatVariant.values();
         isTextureMob = true;
         break;
      case 1:
         variants = CosmeticsManager.RabbitVariant.values();
         isTextureMob = true;
         break;
      case 2:
         variants = CosmeticsManager.AxolotlVariant.values();
         isTextureMob = true;
         break;
      case 3:
         variants = CosmeticsManager.FoxVariant.values();
         isTextureMob = true;
         break;
      case 4:
         variants = CosmeticsManager.HorseColor.values();
         isTextureMob = true;
         break;
      case 5:
         variants = CosmeticsManager.LlamaColor.values();
         isTextureMob = true;
         break;
      case 6:
         variants = CosmeticsManager.PandaGene.values();
         isTextureMob = true;
         break;
      case 7:
         variants = CosmeticsManager.ParrotVariant.values();
         isTextureMob = false;
         break;
      case 8:
         variants = CosmeticsManager.FrogVariant.values();
         isTextureMob = false;
         break;
      case 9:
         variants = CosmeticsManager.ZombieStyle.values();
         isTextureMob = false;
         break;
      case 10:
         variants = CosmeticsManager.WitherStyle.values();
         isTextureMob = false;
         break;
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
         variants = CosmeticsManager.SimpleAgeVariant.values();
         isAgeMob = true;
         break;
      default:
         variants = new String[]{"Normal"};
      }

      int slot = 0;
      if (variants != null) {
         Object var17 = variants;
         int var10 = ((Object[])variants).length;

         for(int var11 = 0; var11 < var10; ++var11) {
            Object variant = ((Object[])var17)[var11];
            if (slot >= 54) {
               break;
            }

            String varName = variant.toString();
            Material icon = this.getIconForVariant(petType, variant);
            if (isAgeMob) {
               String displayName = varName.equals("ADULT") ? "Adulto" : "Bebé";
               String saveVal = varName.equals("ADULT") ? petType + ":DEFAULT" : petType + ":DEFAULT_BABY";
               this.addVariantItem(inv, displayName, 0, icon, displayName, "pet_free", p, saveVal);
            } else if (varName.equals("Normal")) {
               this.addVariantItem(inv, "Normal", 0, icon, "Estándar", "pet_free", p, petType + ":DEFAULT");
            } else {
               this.addVariantItem(inv, varName, 0, icon, "Adulta", "pet_free", p, petType + ":" + varName);
               if (isTextureMob && slot + 1 < 54) {
                  this.addVariantItem(inv, "Baby " + varName, 0, icon, "Bebé", "pet_free", p, petType + ":" + varName + "_BABY");
               }
            }
         }
      }

      inv.setItem(49, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   private Material getIconForVariant(String type, Object variant) {
      if (type.equals("CAT")) {
         return Material.CAT_SPAWN_EGG;
      } else {
         if (type.equals("ZOMBIE")) {
            if (variant.toString().equals("GOLD")) {
               return Material.GOLDEN_HELMET;
            }

            if (variant.toString().equals("DIAMOND")) {
               return Material.DIAMOND_HELMET;
            }
         }

         if (type.equals("WITHER")) {
            return Material.NETHER_STAR;
         } else {
            try {
               return Material.valueOf(type + "_SPAWN_EGG");
            } catch (Exception var4) {
               return Material.NAME_TAG;
            }
         }
      }
   }

   private void addVariantItem(Inventory inv, String name, int cost, Material icon, String desc, String perm, Player p, String saveValue) {
      ItemStack item = new ItemStack(icon);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName("§e§l" + name);
      List<String> lore = new ArrayList();
      lore.add("§8§m-----------------------");
      lore.add("§7Tipo: §f" + desc);
      lore.add("");
      lore.add("§e§l» §6Clic para equipar");
      lore.add("§8§m-----------------------");
      meta.setLore(lore);
      item.setItemMeta(meta);
      inv.addItem(new ItemStack[]{item});
   }

   private void addItemToInv(Inventory inv, int slot, CosmeticsManager.PetCategory cat, Player p) {
      ItemStack item = new ItemStack(cat.icon);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName("§e§l" + cat.name);
      List<String> lore = new ArrayList();
      lore.add("§8§m-----------------------");
      lore.add("§7" + cat.desc);
      lore.add("");
      if (cat.cost == 0) {
         lore.add("§a§lGRATIS");
      } else {
         lore.add("§7Precio: §6$" + cat.cost);
      }

      lore.add("");
      boolean isOwned = this.hasUnlocked(p, "pet." + cat.name());
      boolean isFree = cat.cost == 0;
      boolean unlocked = isOwned || isFree;
      String active = this.getActiveCosmetic(p, "pet");
      if (unlocked) {
         lore.add("§a§l✔ DESBLOQUEADO");
         if (active.startsWith(cat.name())) {
            lore.add("§b§l» §bACTIVO");
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         } else {
            lore.add("§e§l» §fClic para §6Elegir Variante");
         }
      } else {
         lore.add("§c§l✖ BLOQUEADO");
         if (p.hasPermission("ffa.vip")) {
            lore.add("§d(Descuento VIP disponible)");
         }

         lore.add("§e§l» §fClic para §aComprar");
      }

      lore.add("§8§m-----------------------");
      meta.setLore(lore);
      item.setItemMeta(meta);
      inv.setItem(slot, item);
   }

   private void processPurchase(Player p, Object[] values, String itemName, String typePrefix, Runnable refresh) {
      Object[] var6 = values;
      int var7 = values.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Object obj = var6[var8];
         String name = "";
         int cost = 0;
         String enumName = "";
         boolean vip = false;
         if (obj instanceof CosmeticsManager.KillEffect) {
            CosmeticsManager.KillEffect e = (CosmeticsManager.KillEffect)obj;
            name = e.name;
            cost = e.cost;
            enumName = e.name();
            vip = e.vip;
         } else if (obj instanceof CosmeticsManager.DeathMessage) {
            CosmeticsManager.DeathMessage e = (CosmeticsManager.DeathMessage)obj;
            name = e.name;
            cost = e.cost;
            enumName = e.name();
            vip = e.vip;
         } else if (obj instanceof CosmeticsManager.JoinMessage) {
            CosmeticsManager.JoinMessage e = (CosmeticsManager.JoinMessage)obj;
            name = e.name;
            cost = e.cost;
            enumName = e.name();
         } else if (obj instanceof CosmeticsManager.DeathSound) {
            CosmeticsManager.DeathSound e = (CosmeticsManager.DeathSound)obj;
            name = e.name;
            cost = e.cost;
            enumName = e.name();
            vip = e.vip;
         } else if (obj instanceof CosmeticsManager.ProjectileTrail) {
            CosmeticsManager.ProjectileTrail e = (CosmeticsManager.ProjectileTrail)obj;
            name = e.name;
            cost = e.cost;
            enumName = e.name();
            vip = e.vip;
         } else if (obj instanceof CosmeticsManager.AuraType) {
            CosmeticsManager.AuraType e = (CosmeticsManager.AuraType)obj;
            name = e.name;
            cost = e.cost;
            enumName = e.name();
            vip = e.vip;
         }

         if (name.equals(itemName)) {
            if (vip && !p.hasPermission("ffa.vip")) {
               p.sendMessage(ChatColor.RED + "¡Este cosmético es exclusivo para VIPs!");
               p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
               return;
            }

            if (!this.hasUnlocked(p, typePrefix + "." + enumName) && cost != 0) {
               this.tryPurchase(p, cost, typePrefix + "." + enumName, name, refresh);
            } else {
               this.setActiveCosmetic(p, typePrefix, enumName);
               p.sendMessage("");
               p.sendMessage("§8§m--------------------------------------");
               p.sendMessage("   §a§l✔ OBJETO EQUIPADO");
               p.sendMessage("   §fHas seleccionado: §e§l" + name);
               p.sendMessage("   §7Categoría: §b" + typePrefix.toUpperCase());
               p.sendMessage("§8§m--------------------------------------");
               p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.5F);
               if (refresh != null) {
                  refresh.run();
               }
            }

            return;
         }
      }

   }

   public void tryPurchase(Player p, int price, String cosmeticKey, String displayName, Runnable onComplete) {
      int balance = this.getBalance(p);
      if (balance < price) {
         p.sendMessage("");
         p.sendMessage("§b§lFFA §8» §f§c§lERROR §8» §7No tienes suficiente dinero.");
         p.sendMessage("§7Te faltan: §6$" + (price - balance));
         p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
      } else {
         this.addMoney(p, -price);
         if (this.plugin.gameManager.activeMissionsList.containsKey(p.getUniqueId())) {
            Iterator var7 = ((List)this.plugin.gameManager.activeMissionsList.get(p.getUniqueId())).iterator();

            while(var7.hasNext()) {
               GameManager.Mission mission = (GameManager.Mission)var7.next();
               if (mission.type == GameManager.MissionType.INVERSIONISTA && mission.current < mission.goal) {
                  mission.current += price;
                  if (mission.current >= mission.goal) {
                     this.addMoney(p, mission.reward);
                     p.sendMessage("§a§l¡MISIÓN INVERSIONISTA COMPLETA! §6+$" + mission.reward);
                  }
               }
            }
         }

         this.unlockCosmetic(p, cosmeticKey);
         p.sendMessage("");
         p.sendMessage("§8§m--------------------------------------");
         p.sendMessage("   §a§l✔ ¡COMPRA EXITOSA!");
         p.sendMessage("   §fObjeto: §e§l" + displayName);
         p.sendMessage("   §fCosto: §c-$" + price);
         p.sendMessage("   §fBalance: §6$" + this.getBalance(p));
         p.sendMessage("§8§m--------------------------------------");
         p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 2.0F);
         if (onComplete != null) {
            onComplete.run();
         }

      }
   }

   public void pauseCosmetic(Player p, String type) {
      if (!this.pausedCosmetics.containsKey(p.getUniqueId())) {
         this.pausedCosmetics.put(p.getUniqueId(), new HashSet());
      }

      ((Set)this.pausedCosmetics.get(p.getUniqueId())).add(type);
   }

   public void resumeCosmetic(Player p, String type) {
      if (this.pausedCosmetics.containsKey(p.getUniqueId())) {
         ((Set)this.pausedCosmetics.get(p.getUniqueId())).remove(type);
      }

   }

   public void playKillEffect(final Player p, Location loc, CosmeticsManager.KillEffect effect) {
      if (effect == CosmeticsManager.KillEffect.BLOOD) {
         p.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.add(0.0D, 1.0D, 0.0D), 50, 0.5D, 0.5D, 0.5D, Material.REDSTONE_BLOCK.createBlockData());
      }

      if (effect == CosmeticsManager.KillEffect.TNT) {
         p.getWorld().createExplosion(loc, 0.0F, false);
      }

      if (effect == CosmeticsManager.KillEffect.METEOR) {
         final Fireball fb = (Fireball)loc.getWorld().spawnEntity(loc.clone().add(0.0D, 10.0D, 0.0D), EntityType.FIREBALL);
         fb.setDirection(new Vector(0, -1, 0));
         fb.setYield(0.0F);
         (new BukkitRunnable() {
            public void run() {
               if (fb.isValid()) {
                  p.getWorld().createExplosion(fb.getLocation(), 0.0F, false);
                  p.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, fb.getLocation(), 1);
                  fb.remove();
               }

            }
         }).runTaskLater(this.plugin, 20L);
      }

      int i;
      if (effect == CosmeticsManager.KillEffect.ZEUS) {
         for(i = 0; i < 5; ++i) {
            p.getWorld().strikeLightningEffect(loc.clone().add(Math.random() * 2.0D, 0.0D, Math.random() * 2.0D));
         }
      }

      if (effect == CosmeticsManager.KillEffect.HOLY_RAY) {
         p.getWorld().strikeLightningEffect(loc);
         p.getWorld().playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 2.0F, 0.5F);
         p.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 2.0F, 2.0F);
      }

      if (effect == CosmeticsManager.KillEffect.THIEF) {
         Material[] gems = new Material[]{Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT};

         for(int i = 0; i < 10; ++i) {
            final Item it = p.getWorld().dropItemNaturally(loc, new ItemStack(gems[(new Random()).nextInt(gems.length)]));
            it.setPickupDelay(20000);
            (new BukkitRunnable() {
               public void run() {
                  it.remove();
               }
            }).runTaskLater(this.plugin, 40L);
         }
      }

      if (effect == CosmeticsManager.KillEffect.ICE_SHATTER) {
         p.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc.add(0.0D, 1.0D, 0.0D), 50, 0.5D, 0.5D, 0.5D, Material.BLUE_ICE.createBlockData());
         p.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0F, 1.0F);
      }

      if (effect == CosmeticsManager.KillEffect.PINATA) {
         this.runPinataEffect(loc);
      }

      if (effect == CosmeticsManager.KillEffect.GHOST) {
         p.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, loc, 20);
         p.getWorld().playSound(loc, Sound.ENTITY_GHAST_SCREAM, 1.0F, 1.0F);
      }

      if (effect == CosmeticsManager.KillEffect.WARDEN_SOUL) {
         p.getWorld().spawnParticle(Particle.SCULK_SOUL, loc.add(0.0D, 1.0D, 0.0D), 30, 0.5D, 0.5D, 0.5D, 0.1D);
      }

      if (effect == CosmeticsManager.KillEffect.SCULK_EXPLOSION) {
         p.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, loc, 50, 1.0D, 1.0D, 1.0D);
         p.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0F, 1.0F);
      }

      final ArmorStand as;
      if (effect == CosmeticsManager.KillEffect.LAUNCH) {
         as = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
         as.setVisible(false);
         as.setVelocity(new Vector(0, 2, 0));
         p.getWorld().spawnParticle(Particle.CLOUD, loc, 20);
         (new BukkitRunnable() {
            public void run() {
               as.remove();
               p.getWorld().createExplosion(as.getLocation(), 0.0F, false);
            }
         }).runTaskLater(this.plugin, 20L);
      }

      if (effect == CosmeticsManager.KillEffect.TORNADO) {
         for(i = 0; i < 5; ++i) {
            p.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0.0D, (double)i, 0.0D), 5);
         }

         p.getWorld().playSound(loc, Sound.ITEM_ELYTRA_FLYING, 1.0F, 1.0F);
      }

      if (effect == CosmeticsManager.KillEffect.BLACK_HOLE) {
         p.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
         p.getWorld().spawnParticle(Particle.SQUID_INK, loc, 50, 1.0D, 1.0D, 1.0D);
         p.getWorld().playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 0.1F);
      }

      if (effect == CosmeticsManager.KillEffect.GRAVITY) {
         as = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
         as.setVisible(false);
         as.setGravity(false);
         as.setVelocity(new Vector(0.0D, 0.5D, 0.0D));
         (new BukkitRunnable() {
            int t = 0;

            public void run() {
               if (this.t++ > 20) {
                  as.remove();
                  this.cancel();
               }

               as.getWorld().spawnParticle(Particle.PORTAL, as.getLocation(), 5);
            }
         }).runTaskTimer(this.plugin, 0L, 1L);
      }

      if (effect == CosmeticsManager.KillEffect.ALIEN) {
         p.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 0.5D, 5.0D, 0.5D, 0.0D);
         p.getWorld().playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 2.0F);
      }

   }

   public void runPinataEffect(Location loc) {
      final List<Item> items = new ArrayList();
      Random r = new Random();
      Material[] types = new Material[]{Material.RED_WOOL, Material.BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL, Material.DIAMOND, Material.GOLD_INGOT};

      for(int i = 0; i < 10; ++i) {
         ItemStack is = new ItemStack(types[r.nextInt(types.length)]);
         Item item = loc.getWorld().dropItemNaturally(loc.add(0.0D, 1.0D, 0.0D), is);
         item.setPickupDelay(10000);
         items.add(item);
      }

      (new BukkitRunnable() {
         public void run() {
            Iterator var1 = items.iterator();

            while(var1.hasNext()) {
               Item i = (Item)var1.next();
               i.remove();
            }

         }
      }).runTaskLater(this.plugin, 40L);
   }

   public void restorePet(Player p) {
      if (this.petCache.containsKey(p.getUniqueId())) {
         this.spawnPet(p, (String)this.petCache.get(p.getUniqueId()));
      } else {
         String saved = this.getActiveCosmetic(p, "pet");
         if (!saved.equals("NONE")) {
            this.spawnPet(p, saved);
         }
      }

   }

   public void spawnPet(final Player p, String rawData) {
      this.removePet(p);
      if (rawData != null && !rawData.equals("NONE")) {
         this.petCache.put(p.getUniqueId(), rawData);
         this.setActiveCosmetic(p, "pet", rawData);
         String[] parts = rawData.split(":");
         String typeStr = parts[0];
         String variantStr = parts.length > 1 ? parts[1] : "DEFAULT";
         boolean isBaby = variantStr.endsWith("_BABY");
         if (isBaby) {
            variantStr = variantStr.replace("_BABY", "");
         }

         boolean preferHead = (Boolean)this.petOnHead.getOrDefault(p.getUniqueId(), false);

         try {
            Location loc = p.getLocation();
            final Entity pet = null;
            EntityType type = EntityType.valueOf(typeStr);
            if (type == EntityType.PHANTOM) {
               Phantom ph = (Phantom)p.getWorld().spawnEntity(loc.add(0.0D, 3.0D, 0.0D), type);
               ph.setSize(3);
               pet = ph;
            } else {
               pet = p.getWorld().spawnEntity(loc, type);
            }

            if (pet instanceof Cat && !variantStr.equals("DEFAULT")) {
               ((Cat)pet).setCatType(Type.valueOf(variantStr));
            }

            if (pet instanceof Rabbit && !variantStr.equals("DEFAULT")) {
               ((Rabbit)pet).setRabbitType(org.bukkit.entity.Rabbit.Type.valueOf(variantStr));
            }

            if (pet instanceof Parrot && !variantStr.equals("DEFAULT")) {
               ((Parrot)pet).setVariant(Variant.valueOf(variantStr));
            }

            if (pet instanceof Frog && !variantStr.equals("DEFAULT")) {
               ((Frog)pet).setVariant(org.bukkit.entity.Frog.Variant.valueOf(variantStr));
            }

            if (pet instanceof Axolotl && !variantStr.equals("DEFAULT")) {
               ((Axolotl)pet).setVariant(org.bukkit.entity.Axolotl.Variant.valueOf(variantStr));
            }

            if (pet instanceof Fox && variantStr.equals("SNOW")) {
               ((Fox)pet).setFoxType(org.bukkit.entity.Fox.Type.SNOW);
            }

            if (pet instanceof Horse && !variantStr.equals("DEFAULT")) {
               ((Horse)pet).setColor(org.bukkit.entity.Horse.Color.valueOf(variantStr));
            }

            if (pet instanceof Llama && !variantStr.equals("DEFAULT")) {
               ((Llama)pet).setColor(org.bukkit.entity.Llama.Color.valueOf(variantStr));
            }

            if (pet instanceof Panda && !variantStr.equals("DEFAULT")) {
               ((Panda)pet).setMainGene(Gene.valueOf(variantStr));
            }

            if (pet instanceof Wither && variantStr.equals("CHARGED")) {
               ((Wither)pet).setInvulnerable(true);
            }

            if (pet instanceof Zombie) {
               if (variantStr.equals("GOLD")) {
                  ((Zombie)pet).getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                  ((Zombie)pet).getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
               } else if (variantStr.equals("DIAMOND")) {
                  ((Zombie)pet).getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                  ((Zombie)pet).getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
               }
            }

            if (isBaby && pet instanceof Ageable) {
               ((Ageable)pet).setBaby();
            } else if (pet instanceof Ageable && type != EntityType.VILLAGER) {
               ((Ageable)pet).setAdult();
            }

            if (type == EntityType.ZOMBIE) {
               ((Zombie)pet).setBaby(isBaby);
            }

            ((Entity)pet).setInvulnerable(true);
            ((Entity)pet).setSilent(true);
            if (pet instanceof LivingEntity) {
               ((LivingEntity)pet).setAI(false);
               ((LivingEntity)pet).setCollidable(false);
            }

            if (pet instanceof Warden) {
               ((Warden)pet).setAnger(p, 0);
            }

            if (pet instanceof LivingEntity && !(pet instanceof Giant)) {
               ((LivingEntity)pet).setCollidable(false);
            }

            if (preferHead) {
               (new BukkitRunnable() {
                  public void run() {
                     if (((Entity)pet).isValid() && p.isOnline() && !p.isDead()) {
                        p.addPassenger((Entity)pet);
                     }

                  }
               }).runTaskLater(this.plugin, 10L);
            }

            this.activePets.put(p.getUniqueId(), pet);
         } catch (Exception var12) {
         }

      } else {
         this.setActiveCosmetic(p, "pet", "NONE");
         this.petCache.remove(p.getUniqueId());
      }
   }

   public void removePet(Player p) {
      if (this.activePets.containsKey(p.getUniqueId())) {
         Entity pet = (Entity)this.activePets.get(p.getUniqueId());
         if (pet != null && !pet.isDead()) {
            pet.remove();
         }

         this.activePets.remove(p.getUniqueId());
      }

      Iterator var4 = p.getPassengers().iterator();

      while(var4.hasNext()) {
         Entity pass = (Entity)var4.next();
         pass.remove();
      }

   }

   public void saveTrimPreference(Player p, String setting, String value) {
      String type = (String)this.editingArmorType.get(p.getUniqueId());
      this.plugin.statsConfig.set("players." + p.getUniqueId() + ".trims." + type + "." + setting, value);
      this.plugin.saveStats();
      String displayValue = value.replace("_", " ");
      p.sendMessage("§b§lFFA §8» §f§b§lARMERÍA §8» §fSe ha aplicado §e" + displayValue + " §fa tu equipo de §6" + type);
      p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.2F);
   }

   public void applyActiveTrim(Player p) {
      ItemStack[] armor = p.getInventory().getArmorContents();
      boolean changed = false;

      for(int i = 0; i < armor.length; ++i) {
         ItemStack item = armor[i];
         if (item != null) {
            String type = null;
            if (item.getType().name().startsWith("DIAMOND_")) {
               type = "DIAMOND";
            } else if (item.getType().name().startsWith("NETHERITE_")) {
               type = "NETHERITE";
            }

            if (type != null) {
               ItemMeta meta = item.getItemMeta();
               this.applySavedTrimToItem(p, meta, type);
               item.setItemMeta(meta);
               armor[i] = item;
               changed = true;
            }
         }
      }

      if (changed) {
         p.getInventory().setArmorContents(armor);
      }

   }

   private void applySavedTrimToItem(Player p, ItemMeta meta, String type) {
      if (meta instanceof ArmorMeta) {
         String patName = this.plugin.statsConfig.getString("players." + p.getUniqueId() + ".trims." + type + ".pattern");
         String matName = this.plugin.statsConfig.getString("players." + p.getUniqueId() + ".trims." + type + ".material");
         if (patName != null && matName != null) {
            try {
               TrimPattern pat = CosmeticsManager.TrimDesign.valueOf(patName).pat;
               TrimMaterial mat = CosmeticsManager.TrimColor.valueOf(matName).mat;
               ((ArmorMeta)meta).setTrim(new ArmorTrim(mat, pat));
            } catch (Exception var8) {
            }

         }
      }
   }

   public void openTrimEditMenu(Player p, String type) {
      this.editingArmorType.put(p.getUniqueId(), type);
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "Editando: " + type);
      inv.setItem(11, this.createItem(Material.FILLED_MAP, ChatColor.GOLD + "Cambiar Patrón"));
      inv.setItem(15, this.createItem(Material.MAGMA_CREAM, ChatColor.AQUA + "Cambiar Material"));
      ItemStack preview = new ItemStack(type.equals("DIAMOND") ? Material.DIAMOND_CHESTPLATE : Material.NETHERITE_CHESTPLATE);
      ItemMeta meta = preview.getItemMeta();
      meta.setDisplayName(ChatColor.GREEN + "Vista Previa");
      this.applySavedTrimToItem(p, meta, type);
      preview.setItemMeta(meta);
      inv.setItem(13, preview);
      inv.setItem(22, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void startLivePreview(final Player p, final String type) {
      this.previewBackup.put(p.getUniqueId(), p.getInventory().getArmorContents());
      Material matHead;
      Material matChest;
      Material matLegs;
      Material matBoots;
      if (type.equals("DIAMOND")) {
         matHead = Material.DIAMOND_HELMET;
         matChest = Material.DIAMOND_CHESTPLATE;
         matLegs = Material.DIAMOND_LEGGINGS;
         matBoots = Material.DIAMOND_BOOTS;
      } else {
         matHead = Material.NETHERITE_HELMET;
         matChest = Material.NETHERITE_CHESTPLATE;
         matLegs = Material.NETHERITE_LEGGINGS;
         matBoots = Material.NETHERITE_BOOTS;
      }

      ItemStack[] previewArmor = new ItemStack[]{new ItemStack(matBoots), new ItemStack(matLegs), new ItemStack(matChest), new ItemStack(matHead)};
      ItemStack[] var8 = previewArmor;
      int var9 = previewArmor.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         ItemStack i = var8[var10];
         ItemMeta meta = i.getItemMeta();
         this.applySavedTrimToItem(p, meta, type);
         i.setItemMeta(meta);
      }

      p.getInventory().setArmorContents(previewArmor);
      p.closeInventory();
      (new BukkitRunnable() {
         public void run() {
            if (p.isOnline()) {
               CosmeticsManager.this.restoreArmor(p);
               CosmeticsManager.this.openTrimEditMenu(p, type);
            }
         }
      }).runTaskLater(this.plugin, 60L);
   }

   public void restoreArmor(Player p) {
      if (this.previewBackup.containsKey(p.getUniqueId())) {
         p.getInventory().setArmorContents((ItemStack[])this.previewBackup.remove(p.getUniqueId()));
      }

   }

   public void buyMysteryBox(final Player p) {
      int cost = 1000;
      if (this.getBalance(p) < cost) {
         p.sendMessage(ChatColor.RED + "No tienes suficiente dinero ($1000).");
         p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
      } else {
         this.addMoney(p, -cost);
         this.plugin.gameManager.addMissionProgress(p, GameManager.MissionType.INVERSIONISTA, cost);
         p.closeInventory();
         p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);
         (new BukkitRunnable() {
            int i = 0;

            public void run() {
               if (this.i < 10) {
                  p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0F, 1.0F + (float)this.i * 0.1F);
                  p.spawnParticle(Particle.NOTE, p.getLocation().add(0.0D, 1.0D, 0.0D), 1);
                  ++this.i;
               } else {
                  this.cancel();
                  CosmeticsManager.this.giveRandomCosmetic(p);
               }

            }
         }).runTaskTimer(this.plugin, 0L, 2L);
      }
   }

   private void giveRandomCosmetic(Player p) {
      List<String> pool = new ArrayList();
      boolean isVip = p.hasPermission("ffa.vip");
      CosmeticsManager.KillEffect[] var4 = CosmeticsManager.KillEffect.values();
      int var5 = var4.length;

      int var6;
      for(var6 = 0; var6 < var5; ++var6) {
         CosmeticsManager.KillEffect e = var4[var6];
         if (e != CosmeticsManager.KillEffect.NONE && (!e.vip || isVip)) {
            pool.add("effect." + e.name());
         }
      }

      CosmeticsManager.AuraType[] var8 = CosmeticsManager.AuraType.values();
      var5 = var8.length;

      for(var6 = 0; var6 < var5; ++var6) {
         CosmeticsManager.AuraType a = var8[var6];
         if (a != CosmeticsManager.AuraType.NONE && (!a.vip || isVip)) {
            pool.add("aura." + a.name());
         }
      }

      CosmeticsManager.ProjectileTrail[] var9 = CosmeticsManager.ProjectileTrail.values();
      var5 = var9.length;

      for(var6 = 0; var6 < var5; ++var6) {
         CosmeticsManager.ProjectileTrail t = var9[var6];
         if (t != CosmeticsManager.ProjectileTrail.NONE && (!t.vip || isVip)) {
            pool.add("trail." + t.name());
         }
      }

      if (pool.isEmpty()) {
         p.sendMessage("");
         p.sendMessage("§b§lFFA §8» §f§c§lERROR TÉCNICO §8» §7No se han encontrado cosméticos compatibles con tu rango actual.");
         p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
      } else {
         String picked = (String)pool.get((new Random()).nextInt(pool.size()));
         if (this.hasUnlocked(p, picked)) {
            p.sendMessage("");
            p.sendMessage("§8§m--------------------------------------");
            p.sendMessage("   §e§l⚠ OBJETO REPETIDO");
            p.sendMessage("   §fYa posees este cosmético en tu colección.");
            p.sendMessage("");
            p.sendMessage("   §6§lREEMBOLSO: §a+$200 §fmonedas.");
            p.sendMessage("§8§m--------------------------------------");
            this.addMoney(p, 200);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.8F);
         } else {
            this.unlockCosmetic(p, picked);
            String category = picked.split("\\.")[0].toUpperCase();
            String itemName = picked.split("\\.")[1].replace("_", " ");
            p.sendMessage("");
            p.sendMessage("§8§m--------------------------------------");
            p.sendMessage("   §d§l✨ ¡CAJA MISTERIOSA ABIERTA! ✨");
            p.sendMessage("");
            p.sendMessage("   §f¡Has ganado: §b§l" + itemName + " §f!");
            p.sendMessage("   §7Categoría: §e" + category);
            p.sendMessage("");
            p.sendMessage("§8§m--------------------------------------");
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            p.spawnParticle(Particle.TOTEM, p.getLocation(), 100, 0.5D, 1.0D, 0.5D, 0.5D);
         }

      }
   }

   public void openArmorSelector(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§9§lARMERÍA §8» §fElige Tipo");
      inv.setItem(11, this.createItem(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "Armadura de Diamante"));
      inv.setItem(15, this.createItem(Material.NETHERITE_CHESTPLATE, ChatColor.DARK_GRAY + "Armadura de Netherite"));
      inv.setItem(22, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openPatternSelector(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 45, "Selecciona Diseño");
      CosmeticsManager.TrimDesign[] var3 = CosmeticsManager.TrimDesign.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         CosmeticsManager.TrimDesign d = var3[var5];
         ItemStack item = new ItemStack(d.icon);
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName(ChatColor.GOLD + d.name);
         List<String> lore = new ArrayList();
         lore.add(ChatColor.GREEN + "Precio: $" + d.cost);
         meta.setLore(lore);
         item.setItemMeta(meta);
         inv.addItem(new ItemStack[]{item});
      }

      inv.setItem(40, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openMaterialSelector(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 45, "Selecciona Color");
      CosmeticsManager.TrimColor[] var3 = CosmeticsManager.TrimColor.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         CosmeticsManager.TrimColor c = var3[var5];
         ItemStack item = new ItemStack(c.icon);
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName(ChatColor.AQUA + c.name);
         List<String> lore = new ArrayList();
         lore.add(ChatColor.GREEN + "Precio: $" + c.cost);
         meta.setLore(lore);
         item.setItemMeta(meta);
         inv.addItem(new ItemStack[]{item});
      }

      inv.setItem(40, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openAllPlayersGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "Gestión de Usuarios");
      if (this.plugin.statsConfig.contains("players")) {
         Iterator var3 = this.plugin.statsConfig.getConfigurationSection("players").getKeys(false).iterator();

         while(var3.hasNext()) {
            String uuidStr = (String)var3.next();

            try {
               OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
               if (op.getName() != null) {
                  ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                  SkullMeta meta = (SkullMeta)head.getItemMeta();
                  meta.setOwningPlayer(op);
                  meta.setDisplayName(ChatColor.YELLOW + op.getName());
                  head.setItemMeta(meta);
                  inv.addItem(new ItemStack[]{head});
               }
            } catch (Exception var8) {
            }
         }
      }

      inv.setItem(49, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openPlayerEditor(Player p, String targetName) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "Administrar: " + targetName);
      inv.setItem(11, this.createItem(Material.EMERALD, "§aDar $1000"));
      inv.setItem(13, this.createItem(Material.TNT, "§cReset Stats"));
      inv.setItem(15, this.createItem(Material.COMPASS, "§bTeletransportarse"));
      inv.setItem(22, this.createItem(Material.ARROW, "§cVolver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openTopKillsMainMenu(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§6§lRANKING §8» §fSelector");
      int slot = 10;
      if (this.plugin.arenasConfig.contains("ffa")) {
         Iterator var4 = this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false).iterator();

         while(var4.hasNext()) {
            String key = (String)var4.next();
            if (slot > 16) {
               break;
            }

            ItemStack i = this.createItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "Top " + key);
            inv.setItem(slot++, i);
         }
      }

      inv.setItem(22, this.createItem(Material.ARROW, "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openTopKillsList(Player p, String arena) {
      String title = "Top 10: " + (arena == null ? "Global" : arena);
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, title);
      Map<String, Integer> killsMap = new HashMap();
      if (this.plugin.statsConfig.contains("players")) {
         Iterator var6 = this.plugin.statsConfig.getConfigurationSection("players").getKeys(false).iterator();

         while(var6.hasNext()) {
            String key = (String)var6.next();

            try {
               OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(key));
               String name = op.getName();
               if (name != null) {
                  int kills = false;
                  int kills;
                  if (arena == null) {
                     kills = this.plugin.statsConfig.getInt("players." + key + ".global.kills", 0);
                  } else {
                     kills = this.plugin.statsConfig.getInt("players." + key + "." + arena + ".kills", 0);
                  }

                  if (kills > 0) {
                     killsMap.put(name, kills);
                  }
               }
            } catch (Exception var22) {
            }
         }
      }

      List<Entry<String, Integer>> list = new ArrayList(killsMap.entrySet());
      list.sort(Entry.comparingByValue(Comparator.reverseOrder()));
      int slot = 0;

      for(int i = 0; i < Math.min(list.size(), 10); ++i) {
         Entry<String, Integer> entry = (Entry)list.get(i);
         String targetName = (String)entry.getKey();
         int kills = (Integer)entry.getValue();
         String uuidStr = null;
         Iterator var13 = this.plugin.statsConfig.getConfigurationSection("players").getKeys(false).iterator();

         while(var13.hasNext()) {
            String k = (String)var13.next();
            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(k));
            if (op.getName() != null && op.getName().equals(targetName)) {
               uuidStr = k;
               break;
            }
         }

         int deaths = 0;
         int playtimeSeconds = 0;
         if (uuidStr != null) {
            String basePath = "players." + uuidStr;
            if (arena == null) {
               deaths = this.plugin.statsConfig.getInt(basePath + ".global.deaths", 0);
               playtimeSeconds = this.plugin.statsConfig.getInt(basePath + ".playtime", 0) * 60;
            } else {
               deaths = this.plugin.statsConfig.getInt(basePath + "." + arena + ".deaths", 0);
               playtimeSeconds = this.plugin.statsConfig.getInt(basePath + "." + arena + ".playtime", 0) * 60;
            }
         }

         double kdr = deaths == 0 ? (double)kills : (double)kills / (double)deaths;
         int hours = playtimeSeconds / 3600;
         int minutes = playtimeSeconds % 3600 / 60;
         ItemStack head = new ItemStack(Material.PLAYER_HEAD);
         SkullMeta meta = (SkullMeta)head.getItemMeta();
         meta.setOwningPlayer(Bukkit.getOfflinePlayer(targetName));
         meta.setDisplayName(ChatColor.GOLD + "#" + (i + 1) + " " + ChatColor.YELLOW + targetName);
         List<String> lore = new ArrayList();
         lore.add(ChatColor.DARK_GRAY + "----------------");
         lore.add(ChatColor.GRAY + "Estadísticas:");
         lore.add(ChatColor.WHITE + "⚔ Kills: " + ChatColor.GREEN + kills);
         lore.add(ChatColor.WHITE + "☠ Muertes: " + ChatColor.RED + deaths);
         lore.add(ChatColor.WHITE + "⚡ KD/R: " + ChatColor.GOLD + String.format("%.2f", kdr));
         lore.add(ChatColor.WHITE + "⏱ Tiempo: " + ChatColor.AQUA + hours + "h " + minutes + "m");
         lore.add(ChatColor.DARK_GRAY + "----------------");
         meta.setLore(lore);
         head.setItemMeta(meta);
         inv.setItem(slot++, head);
      }

      inv.setItem(22, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public void openSpectatorGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "§e§lESPECTAR DUELOS");
      Iterator var3 = Bukkit.getOnlinePlayers().iterator();

      while(var3.hasNext()) {
         Player on = (Player)var3.next();
         if (on != p && this.plugin.gameManager.isInDuel(on)) {
            ItemStack h = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta m = (SkullMeta)h.getItemMeta();
            m.setOwningPlayer(on);
            m.setDisplayName("§6" + on.getName());
            h.setItemMeta(m);
            inv.addItem(new ItemStack[]{h});
         }
      }

      inv.setItem(49, this.createItem(Material.ARROW, "§c« Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   public int getBalance(Player p) {
      return this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".balance", 0);
   }

   public void addMoney(Player p, int amount) {
      this.plugin.statsConfig.set("players." + p.getUniqueId() + ".balance", this.getBalance(p) + amount);
      this.plugin.saveStats();
   }

   public boolean hasUnlocked(Player p, String cosmetic) {
      return this.plugin.statsConfig.getStringList("players." + p.getUniqueId() + ".cosmetics").contains(cosmetic);
   }

   public void unlockCosmetic(Player p, String cosmetic) {
      List<String> list = this.plugin.statsConfig.getStringList("players." + p.getUniqueId() + ".cosmetics");
      if (!list.contains(cosmetic)) {
         list.add(cosmetic);
         this.plugin.statsConfig.set("players." + p.getUniqueId() + ".cosmetics", list);
         this.plugin.saveStats();
      }

   }

   public String getActiveCosmetic(Player p, String type) {
      return this.pausedCosmetics.containsKey(p.getUniqueId()) && ((Set)this.pausedCosmetics.get(p.getUniqueId())).contains(type) ? "NONE" : this.plugin.statsConfig.getString("players." + p.getUniqueId() + ".active_" + type, "NONE");
   }

   public void setActiveCosmetic(Player p, String type, String value) {
      this.plugin.statsConfig.set("players." + p.getUniqueId() + ".active_" + type, value);
      this.plugin.saveStats();
   }

   private ItemStack createItem(Material mat, String name) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(name);
      item.setItemMeta(meta);
      return item;
   }

   public void openPrestigeGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§d§lTIENDA §8» §fPrestigio");
      int kills = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills", 0);
      int level = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".prestige", 0);
      ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "¡SUBIR PRESTIGIO!");
      List<String> lore = new ArrayList();
      lore.add(ChatColor.GRAY + "Nivel Actual: " + ChatColor.AQUA + level + " ⭐");
      lore.add("");
      lore.add(ChatColor.WHITE + "Costo: " + ChatColor.RED + "100 Kills");
      lore.add(ChatColor.WHITE + "Tus Kills: " + ChatColor.GREEN + kills);
      lore.add("");
      if (level >= 5) {
         lore.add(ChatColor.RED + "¡Has alcanzado el máximo nivel!");
      } else if (kills >= 100) {
         lore.add(ChatColor.GREEN + "▶ Haz clic para evolucionar ◀");
         meta.addEnchant(Enchantment.DURABILITY, 1, true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      } else {
         lore.add(ChatColor.RED + "✖ No tienes suficientes kills.");
      }

      meta.setLore(lore);
      item.setItemMeta(meta);
      inv.setItem(13, item);
      inv.setItem(22, this.createItem(Material.ARROW, ChatColor.RED + "Volver"));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   private String findTrimPatternEnum(String displayName) {
      CosmeticsManager.TrimDesign[] var2 = CosmeticsManager.TrimDesign.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         CosmeticsManager.TrimDesign d = var2[var4];
         if (displayName.contains(d.name)) {
            return d.name();
         }
      }

      return "SENTRY";
   }

   private String findTrimMaterialEnum(String displayName) {
      CosmeticsManager.TrimColor[] var2 = CosmeticsManager.TrimColor.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         CosmeticsManager.TrimColor c = var2[var4];
         if (displayName.contains(c.name)) {
            return c.name();
         }
      }

      return "REDSTONE";
   }

   public void openSettingsGUI(Player p) {
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§8Ajustes Personales");
      GameManager.PlayerSettings settings = this.plugin.gameManager.getSettings(p.getUniqueId());
      inv.setItem(10, this.createToggleItem(Material.PAINTING, "§eScoreboard", settings.scoreboard, "§7Muestra u oculta la tabla lateral."));
      inv.setItem(12, this.createToggleItem(Material.PAPER, "§bSolicitudes de Duelo", settings.allowRequests, "§7Permite que otros te desafíen."));
      ItemStack timeItem = this.createItem(Material.CLOCK, "§6Tiempo Personal");
      ItemMeta meta = timeItem.getItemMeta();
      List<String> lore = new ArrayList();
      lore.add("§7Estado actual: §f" + this.getTimeName(settings.time));
      lore.add("");
      lore.add("§eClic para cambiar: Día / Noche / Server");
      meta.setLore(lore);
      timeItem.setItemMeta(meta);
      inv.setItem(14, timeItem);
      inv.setItem(16, this.createToggleItem(Material.ENDER_EYE, "§dPermitir Espectadores", settings.allowSpectators, "§7Permite que otros vean tus duelos."));
      this.fillDiscoGlass(inv);
      p.openInventory(inv);
   }

   private ItemStack createToggleItem(Material mat, String name, boolean state, String desc) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(name);
      List<String> lore = new ArrayList();
      lore.add(desc);
      lore.add("");
      lore.add(state ? "§a§l✔ ACTIVADO" : "§c§l✖ DESACTIVADO");
      lore.add("§eClic para cambiar");
      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   private String getTimeName(long time) {
      if (time == 6000L) {
         return "☀ Día";
      } else {
         return time == 18000L ? "\ud83c\udf19 Noche" : "\ud83c\udf0e Servidor";
      }
   }

   public void spawnTrailParticle(Location loc, String trailName) {
      if (trailName != null && !trailName.equals("NONE")) {
         byte var4 = -1;
         switch(trailName.hashCode()) {
         case -916080124:
            if (trailName.equals("EMERALD")) {
               var4 = 4;
            }
            break;
         case 65110:
            if (trailName.equals("ASH")) {
               var4 = 5;
            }
            break;
         case 2193504:
            if (trailName.equals("GOLD")) {
               var4 = 3;
            }
            break;
         case 2551283:
            if (trailName.equals("SOUL")) {
               var4 = 7;
            }
            break;
         case 63294938:
            if (trailName.equals("BLOOD")) {
               var4 = 2;
            }
            break;
         case 1691559318:
            if (trailName.equals("RAINBOW")) {
               var4 = 1;
            }
            break;
         case 1938702588:
            if (trailName.equals("ARCTIC")) {
               var4 = 0;
            }
            break;
         case 1986783641:
            if (trailName.equals("CHERRY")) {
               var4 = 6;
            }
         }

         switch(var4) {
         case 0:
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 5, 0.1D, 0.1D, 0.1D, 0.02D);
            loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 2, 0.1D, 0.1D, 0.1D, Material.BLUE_ICE.createBlockData());
            return;
         case 1:
            int r = (new Random()).nextInt(255);
            int g = (new Random()).nextInt(255);
            int b = (new Random()).nextInt(255);
            DustOptions dust = new DustOptions(Color.fromRGB(r, g, b), 1.0F);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0.0D, 0.0D, 0.0D, 0.0D, dust);
            return;
         case 2:
            loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 3, 0.1D, 0.1D, 0.1D, Material.REDSTONE_BLOCK.createBlockData());
            return;
         case 3:
            loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 3, 0.1D, 0.1D, 0.1D, Material.GOLD_BLOCK.createBlockData());
            return;
         case 4:
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 3, 0.5D, 0.5D, 0.5D);
            return;
         case 5:
            loc.getWorld().spawnParticle(Particle.ASH, loc, 5, 0.1D, 0.1D, 0.1D);
            return;
         case 6:
            loc.getWorld().spawnParticle(Particle.CHERRY_LEAVES, loc, 3, 0.1D, 0.1D, 0.1D);
            return;
         case 7:
            loc.getWorld().spawnParticle(Particle.SOUL, loc, 2, 0.05D, 0.05D, 0.05D, 0.05D);
            return;
         default:
            Particle p = Particle.CRIT;
            if (trailName.contains("FIRE")) {
               p = Particle.FLAME;
            } else if (!trailName.contains("LOVE") && !trailName.contains("HEART")) {
               if (trailName.contains("MAGIC")) {
                  p = Particle.ENCHANTMENT_TABLE;
               } else if (trailName.contains("NOTE")) {
                  p = Particle.NOTE;
               } else if (trailName.contains("VOID")) {
                  p = Particle.SCULK_SOUL;
               } else if (trailName.contains("TOTEM")) {
                  p = Particle.TOTEM;
               } else if (trailName.contains("WITCH")) {
                  p = Particle.SPELL_WITCH;
               } else if (trailName.contains("LAVA")) {
                  p = Particle.DRIP_LAVA;
               } else if (trailName.contains("SMOKE")) {
                  p = Particle.SMOKE_LARGE;
               } else if (trailName.contains("SNOW")) {
                  p = Particle.SNOWBALL;
               } else if (trailName.contains("SPARK")) {
                  p = Particle.ELECTRIC_SPARK;
               } else if (trailName.contains("CLOUD")) {
                  p = Particle.CLOUD;
               } else if (trailName.contains("WATER")) {
                  p = Particle.DRIP_WATER;
               } else if (trailName.contains("SLIME")) {
                  p = Particle.SLIME;
               } else if (trailName.contains("GLOW")) {
                  p = Particle.GLOW_SQUID_INK;
               } else if (trailName.contains("INK")) {
                  p = Particle.SQUID_INK;
               } else if (trailName.contains("END")) {
                  p = Particle.END_ROD;
               }
            } else {
               p = Particle.HEART;
            }

            loc.getWorld().spawnParticle(p, loc, 1, 0.0D, 0.0D, 0.0D, 0.0D);
         }
      }
   }

   public static enum AuraType {
      NONE("Ninguna", 0, Material.BARRIER, "Sin aura.", false),
      REDSTONE("Láser", 3000, Material.REDSTONE, "Escáner rojo.", false),
      NATURE("Druida", 3500, Material.OAK_SAPLING, "Crecimiento natural.", false),
      STORM("Lluvia", 4000, Material.WATER_BUCKET, "Gotas de agua.", false),
      FLAME("Fuego", 4500, Material.BLAZE_POWDER, "Pies ardientes.", false),
      INK("Tinta", 5000, Material.INK_SAC, "Gotas negras.", false),
      LAVA("Lava Pop", 5000, Material.LAVA_BUCKET, "Goteo ardiente.", false),
      CANDY("Dulce", 5500, Material.SUGAR, "Colores pastel.", false),
      SNOW("Nevada", 6000, Material.SNOWBALL, "Copos cayendo.", false),
      TOXIC("Tóxico", 6000, Material.SLIME_BALL, "Residuos radioactivos.", false),
      MUSIC("DJ", 6500, Material.JUKEBOX, "Notas musicales.", false),
      HEART("Enamorado", 7000, Material.POPPY, "Corazones giratorios.", false),
      EMERALD("Fortuna", 7500, Material.EMERALD, "Riqueza.", false),
      CRITICAL("Poder", 8000, Material.DIAMOND_SWORD, "Partículas de crítico.", false),
      TOTEM("Inmortal", 8500, Material.TOTEM_OF_UNDYING, "Partículas de resurrección.", false),
      FROST("Escarcha", 9000, Material.BLUE_ICE, "Congela el suelo.", true),
      CHERRY("Sakura", 9500, Material.PINK_PETALS, "Pétalos de cerezo.", true),
      PORTAL("Portal", 10000, Material.ENDER_EYE, "Partículas de portal.", true),
      ENCHANTED("Mágica", 10500, Material.ENCHANTED_BOOK, "Glifos de encantamiento.", true),
      CYBER("Cyber", 11000, Material.IRON_BLOCK, "Escáner de seguridad.", true),
      WITCH("Brujería", 11500, Material.POTION, "Magia negra.", true),
      RAINBOW("Arcoiris", 12000, Material.NAME_TAG, "Cambia de color (RGB).", true),
      ENERGY("Energía", 12500, Material.BEACON, "Poder puro.", true),
      OCEANIC("Océano", 13000, Material.PRISMARINE_SHARD, "Burbujas subiendo.", true),
      HALO("Halo", 15000, Material.GLOWSTONE_DUST, "Anillo angelical.", true),
      DNA("ADN", 16000, Material.MAGENTA_GLAZED_TERRACOTTA, "Doble hélice.", true),
      SCULK("Sonar", 17000, Material.ECHO_SHARD, "Detector de vibración.", true),
      RAGE("Furia", 18000, Material.RED_DYE, "Nubes de ira.", true),
      CREEPER("Creeper", 19000, Material.CREEPER_HEAD, "Electricidad estática.", true),
      TEARS("Lágrimas", 20000, Material.CRYING_OBSIDIAN, "Llantos de obsidiana.", true),
      VOID("Vacío", 22000, Material.SCULK_SHRIEKER, "Almas del sculk.", true),
      THUNDERSTORM("Tormenta", 24000, Material.LIGHTNING_ROD, "Nubes y chispas.", true),
      SHIELD("Escudo", 26000, Material.GOLDEN_CHESTPLATE, "Barrera giratoria.", true),
      RADAR("Radar", 28000, Material.COMPASS, "Escaneo de área.", true),
      CORONA("Corona", 30000, Material.GOLDEN_HELMET, "Círculo real.", true),
      SPIRIT("Espíritu", 32000, Material.SOUL_LANTERN, "Fuego de almas.", true),
      ANGEL("Ángel", 35000, Material.FEATHER, "Alas de luz.", true),
      DEVIL("Demonio", 35000, Material.NETHER_WART, "Alas del infierno.", true),
      GALACTIC("Galáctico", 40000, Material.END_ROD, "Estrellas orbitando.", true),
      BLACK_HOLE("Agujero Negro", 50000, Material.OBSIDIAN, "Consume la luz.", true);

      public String name;
      public int cost;
      public Material icon;
      public String desc;
      public boolean vip;

      private AuraType(String param3, int param4, Material param5, String param6, boolean param7) {
         this.name = n;
         this.cost = c;
         this.icon = i;
         this.desc = d;
         this.vip = v;
      }

      // $FF: synthetic method
      private static CosmeticsManager.AuraType[] $values() {
         return new CosmeticsManager.AuraType[]{NONE, REDSTONE, NATURE, STORM, FLAME, INK, LAVA, CANDY, SNOW, TOXIC, MUSIC, HEART, EMERALD, CRITICAL, TOTEM, FROST, CHERRY, PORTAL, ENCHANTED, CYBER, WITCH, RAINBOW, ENERGY, OCEANIC, HALO, DNA, SCULK, RAGE, CREEPER, TEARS, VOID, THUNDERSTORM, SHIELD, RADAR, CORONA, SPIRIT, ANGEL, DEVIL, GALACTIC, BLACK_HOLE};
      }
   }

   public static enum KillEffect {
      NONE("Ninguno", 0, Material.BARRIER, "Sin efecto visual.", false),
      BLOOD("Sangre", 1000, Material.REDSTONE, "Explosión roja.", false),
      TNT("Explosión TNT", 2000, Material.TNT, "Boom sin daño.", false),
      THIEF("Ladrón", 3000, Material.EMERALD, "Lluvia de items falsos.", false),
      GHOST("Fantasmal", 4000, Material.SOUL_CAMPFIRE, "Sonido de Ghast y humo.", false),
      LIGHTNING("Relámpago", 5000, Material.LIGHTNING_ROD, "Un rayo simple.", false),
      SQUID("Tinta", 5500, Material.INK_SAC, "Explosión de tinta negra.", false),
      FIREWORK("Fuegos", 6000, Material.FIREWORK_ROCKET, "Cohete aleatorio.", false),
      VORTEX("Vórtice", 7000, Material.ENDER_EYE, "Absorbe partículas.", false),
      METEOR("Meteorito", 8000, Material.FIRE_CHARGE, "Cae del cielo y explota.", true),
      ICE_SHATTER("Congelado", 9000, Material.BLUE_ICE, "La víctima se rompe en hielo.", true),
      ZEUS("Ira de Zeus", 10000, Material.TRIDENT, "5 Rayos consecutivos.", true),
      HOLY_RAY("Rayo Santo", 11000, Material.BEACON, "Castigo divino y coro.", true),
      PINATA("La Piñata", 12000, Material.MAGENTA_GLAZED_TERRACOTTA, "Explosión de colores.", true),
      LAUNCH("Despegue", 13000, Material.PISTON, "La víctima sale volando.", true),
      TORNADO("Tornado", 14000, Material.FEATHER, "Espiral de viento masiva.", true),
      SCULK_EXPLOSION("Implosión Sculk", 15000, Material.ECHO_SHARD, "Oscuridad del Warden.", true),
      WARDEN_SOUL("Alma de Warden", 16000, Material.SCULK_CATALYST, "Partículas del Sonic Boom.", true),
      BLACK_HOLE("Agujero Negro", 20000, Material.OBSIDIAN, "Colapsa el espacio.", true),
      ALIEN("Abducción", 25000, Material.END_ROD, "Un rayo tractor se lo lleva.", true),
      GRAVITY("Gravedad 0", 30000, Material.SHULKER_SHELL, "Todo flota.", true);

      public String name;
      public int cost;
      public Material icon;
      public String desc;
      public boolean vip;

      private KillEffect(String param3, int param4, Material param5, String param6, boolean param7) {
         this.name = n;
         this.cost = c;
         this.icon = i;
         this.desc = d;
         this.vip = v;
      }

      // $FF: synthetic method
      private static CosmeticsManager.KillEffect[] $values() {
         return new CosmeticsManager.KillEffect[]{NONE, BLOOD, TNT, THIEF, GHOST, LIGHTNING, SQUID, FIREWORK, VORTEX, METEOR, ICE_SHATTER, ZEUS, HOLY_RAY, PINATA, LAUNCH, TORNADO, SCULK_EXPLOSION, WARDEN_SOUL, BLACK_HOLE, ALIEN, GRAVITY};
      }
   }

   public static enum ProjectileTrail {
      NONE("Ninguno", 0, Material.BARRIER, "Flechas normales.", false),
      SMOKE("Humo", 2000, Material.FLINT_AND_STEEL, "Rastro de humo negro.", false),
      WATER("Agua", 2500, Material.WATER_BUCKET, "Goteo de agua.", false),
      SLIME("Slime", 3000, Material.SLIME_BALL, "Rastro pegajoso.", false),
      FIRE("Fuego", 3500, Material.BLAZE_POWDER, "Tus flechas dejan fuego.", false),
      LAVA("Lava", 4000, Material.LAVA_BUCKET, "Goteo de lava.", false),
      SNOW("Nieve", 4500, Material.SNOWBALL, "Copo de nieve.", false),
      ASH("Ceniza", 5000, Material.BASALT, "Restos volcánicos.", false),
      SPARK("Chispas", 5500, Material.LIGHTNING_ROD, "Electricidad.", true),
      CLOUD("Nube", 6000, Material.WHITE_WOOL, "Nubes blancas.", true),
      NOTE("Música", 6500, Material.JUKEBOX, "Notas musicales.", true),
      CRIT("Crítico", 7000, Material.DIAMOND_SWORD, "Golpes críticos.", true),
      GLOW("Brillo", 7500, Material.GLOW_INK_SAC, "Tinta brillante.", true),
      INK("Tinta", 8000, Material.INK_SAC, "Oscuridad pura.", true),
      LOVE("Amor", 8500, Material.POPPY, "Corazones persiguiendo.", true),
      MAGIC("Mágico", 9000, Material.EXPERIENCE_BOTTLE, "Magia azul.", true),
      TOTEM("Totem", 9500, Material.TOTEM_OF_UNDYING, "Partículas de vida.", true),
      WITCH("Brujería", 10000, Material.POTION, "Magia morada.", true),
      SOUL("Almas", 11000, Material.SOUL_SAND, "Fuego de almas.", true),
      CHERRY("Cerezo", 12000, Material.PINK_PETALS, "Pétalos rosas.", true),
      HEARTBREAK("Corazones Rotos", 13000, Material.FERMENTED_SPIDER_EYE, "Corazones negros.", true),
      VOID("Vacío", 14000, Material.SCULK, "Oscuridad profunda.", true),
      END("End Rod", 15000, Material.END_ROD, "Luz del End.", true),
      ARCTIC("Hielo Ártico", 18000, Material.BLUE_ICE, "Rastro gélido y copos.", true),
      GOLD("Oro", 20000, Material.GOLD_INGOT, "Destellos dorados.", true),
      EMERALD("Esmeralda", 22000, Material.EMERALD, "Destellos verdes.", true),
      RAINBOW("Arcoiris", 25000, Material.NAME_TAG, "Cambia de color.", true);

      public String name;
      public int cost;
      public Material icon;
      public String desc;
      public boolean vip;

      private ProjectileTrail(String param3, int param4, Material param5, String param6, boolean param7) {
         this.name = n;
         this.cost = c;
         this.icon = i;
         this.desc = d;
         this.vip = v;
      }

      // $FF: synthetic method
      private static CosmeticsManager.ProjectileTrail[] $values() {
         return new CosmeticsManager.ProjectileTrail[]{NONE, SMOKE, WATER, SLIME, FIRE, LAVA, SNOW, ASH, SPARK, CLOUD, NOTE, CRIT, GLOW, INK, LOVE, MAGIC, TOTEM, WITCH, SOUL, CHERRY, HEARTBREAK, VOID, END, ARCTIC, GOLD, EMERALD, RAINBOW};
      }
   }

   public static enum DeathSound {
      NONE("Ninguno", 0, Material.BARRIER, "Sin sonido.", false),
      ANVIL("Yunque", 1000, Material.ANVIL, "Sonido pesado.", false),
      VILLAGER("Aldeano", 1000, Material.EMERALD, "Huh.", false),
      BURP("Eructo", 1500, Material.COOKED_BEEF, "Muy tóxico.", false),
      BELL("Campana", 2000, Material.BELL, "Ding dong.", false),
      CAT("Gato Triste", 3000, Material.COD, "Miau.", true),
      GLASS("Cristal", 3500, Material.GLASS, "Vidrio roto.", true),
      LEVEL_UP("Level Up", 4000, Material.EXPERIENCE_BOTTLE, "Sonido de nivel 30.", true),
      EXPLODE("Explosión", 5000, Material.TNT, "Boom fuerte.", true),
      ENDERMAN("Enderman", 6000, Material.ENDER_PEARL, "Grito de terror.", true),
      GOAT("Cabra Gritona", 8000, Material.GOAT_HORN, "Grito infernal.", true);

      public String name;
      public int cost;
      public Material icon;
      public String desc;
      public boolean vip;

      private DeathSound(String param3, int param4, Material param5, String param6, boolean param7) {
         this.name = n;
         this.cost = c;
         this.icon = i;
         this.desc = d;
         this.vip = v;
      }

      // $FF: synthetic method
      private static CosmeticsManager.DeathSound[] $values() {
         return new CosmeticsManager.DeathSound[]{NONE, ANVIL, VILLAGER, BURP, BELL, CAT, GLASS, LEVEL_UP, EXPLODE, ENDERMAN, GOAT};
      }
   }

   public static enum PetCategory {
      NONE("Ninguna", 0, Material.BARRIER, "Sin mascota."),
      CHICKEN("Gallina", 2000, Material.CHICKEN_SPAWN_EGG, "Clásica."),
      PIG("Cerdo", 2500, Material.PIG_SPAWN_EGG, "Oink oink."),
      COW("Vaca", 3000, Material.COW_SPAWN_EGG, "Muuu."),
      SHEEP("Oveja", 3500, Material.SHEEP_SPAWN_EGG, "Suavecita."),
      RABBIT("Conejo", 4000, Material.RABBIT_SPAWN_EGG, "Saltarín."),
      SLIME("Slime", 5000, Material.SLIME_SPAWN_EGG, "Un slime pequeño."),
      FROG("Rana", 5500, Material.FROG_SPAWN_EGG, "Croac."),
      BEE("Abeja", 6000, Material.BEE_SPAWN_EGG, "Zumbido."),
      TURTLE("Tortuga", 6500, Material.TURTLE_SPAWN_EGG, "Lenta."),
      CAT("Gato", 7000, Material.CAT_SPAWN_EGG, "Felino."),
      PARROT("Loro", 7500, Material.PARROT_SPAWN_EGG, "Tropical."),
      WOLF("Lobo", 8000, Material.WOLF_SPAWN_EGG, "Fiel."),
      FOX("Zorro", 8500, Material.FOX_SPAWN_EGG, "Astuto."),
      PANDA("Panda", 9000, Material.PANDA_SPAWN_EGG, "Juguetón."),
      POLAR_BEAR("Oso Polar", 9500, Material.POLAR_BEAR_SPAWN_EGG, "Del frío."),
      LLAMA("Llama", 10000, Material.LLAMA_SPAWN_EGG, "Escupe."),
      AXOLOTL("Axolote", 11000, Material.AXOLOTL_SPAWN_EGG, "Acuático."),
      ALLAY("Allay", 12000, Material.ALLAY_SPAWN_EGG, "Espíritu."),
      VEX("Vex", 13000, Material.VEX_SPAWN_EGG, "Fantasma enojado."),
      SNOWMAN("Muñeco Nieve", 14000, Material.SNOW_GOLEM_SPAWN_EGG, "Frío."),
      ZOMBIE("Zombie", 15000, Material.ZOMBIE_SPAWN_EGG, "Muerto viviente."),
      SKELETON("Esqueleto", 16000, Material.SKELETON_SPAWN_EGG, "Arquero."),
      STRIDER("Strider", 17000, Material.STRIDER_SPAWN_EGG, "Caminante de lava."),
      CAMEL("Camello", 18000, Material.CAMEL_SPAWN_EGG, "Del desierto."),
      SNIFFER("Sniffer", 20000, Material.SNIFFER_SPAWN_EGG, "Prehistórico."),
      PHANTOM("Phantom", 25000, Material.PHANTOM_SPAWN_EGG, "Terror aéreo."),
      IRON_GOLEM("Iron Golem", 30000, Material.IRON_GOLEM_SPAWN_EGG, "Guardaespaldas."),
      RAVAGER("Devastador", 35000, Material.RAVAGER_SPAWN_EGG, "Bestia de raid."),
      WARDEN("Warden", 50000, Material.WARDEN_SPAWN_EGG, "El Ciego."),
      WITHER("Wither", 75000, Material.WITHER_SKELETON_SKULL, "Jefe Supremo."),
      GIANT("Gigante", 100000, Material.ZOMBIE_HEAD, "EL REY ZOMBIE.");

      public String name;
      public int cost;
      public Material icon;
      public String desc;

      private PetCategory(String param3, int param4, Material param5, String param6) {
         this.name = n;
         this.cost = c;
         this.icon = i;
         this.desc = d;
      }

      // $FF: synthetic method
      private static CosmeticsManager.PetCategory[] $values() {
         return new CosmeticsManager.PetCategory[]{NONE, CHICKEN, PIG, COW, SHEEP, RABBIT, SLIME, FROG, BEE, TURTLE, CAT, PARROT, WOLF, FOX, PANDA, POLAR_BEAR, LLAMA, AXOLOTL, ALLAY, VEX, SNOWMAN, ZOMBIE, SKELETON, STRIDER, CAMEL, SNIFFER, PHANTOM, IRON_GOLEM, RAVAGER, WARDEN, WITHER, GIANT};
      }
   }

   public static enum DeathMessage {
      DEFAULT("Default", 0, Material.PAPER, "Mensaje estándar.", "<victim> murió a manos de <killer>.", false),
      KNIGHT("Caballero", 1500, Material.IRON_SWORD, "Mensaje de honor.", "<victim> cayó con honor ante <killer>.", false),
      PIRATE("Pirata", 2000, Material.SPYGLASS, "Caminó por la plancha.", "<victim> caminó por la plancha de <killer>.", false),
      TOXIC("Tóxico", 3000, Material.FERMENTED_SPIDER_EYE, "Dice 'Ez' al matar.", "<victim> fue humillado (EZ) por <killer>.", false),
      WINDOWS("Windows Crash", 4000, Material.GLASS_PANE, "Dejó de funcionar.", "<victim>.exe dejó de funcionar por culpa de <killer>.", false),
      MATH("Matemático", 5000, Material.WRITABLE_BOOK, "Restado de la ecuación.", "<victim> fue restado de la ecuación por <killer>.", true),
      ERROR_404("Error 404", 6000, Material.REDSTONE_TORCH, "Skill not found.", "Error 404: Skill de <victim> no encontrada vs <killer>.", true),
      MEME("Meme", 7000, Material.PAINTING, "Acusa de hacks.", "<victim> olvidó activar el killaura contra <killer>.", true),
      ANIME("Anime", 10000, Material.DIAMOND_SWORD, "Omae wa mou shindeiru.", "Omae wa mou shindeiru. <victim> fue aniquilado por <killer>.", true),
      DISCORD("Discord", 12000, Material.NOTE_BLOCK, "Baneado.", "<victim> fue baneado del servidor de la vida por <killer>.", true);

      public String name;
      public int cost;
      public Material icon;
      public String desc;
      public String text;
      public boolean vip;

      private DeathMessage(String param3, int param4, Material param5, String param6, String param7, boolean param8) {
         this.name = n;
         this.cost = c;
         this.icon = i;
         this.desc = d;
         this.text = t;
         this.vip = v;
      }

      // $FF: synthetic method
      private static CosmeticsManager.DeathMessage[] $values() {
         return new CosmeticsManager.DeathMessage[]{DEFAULT, KNIGHT, PIRATE, TOXIC, WINDOWS, MATH, ERROR_404, MEME, ANIME, DISCORD};
      }
   }

   public static enum JoinMessage {
      DEFAULT("Default", 0, Material.PAPER, "Aburrido.", "&7[&a+&7] &f<player> ha entrado."),
      PIZZA("Pizza", 2000, Material.COOKED_BEEF, "¿Alguien pidió pizza?", "&aDing dong... &f¡Llegó <player> con la pizza!"),
      MOM("Mamá", 2500, Material.COOKIE, "Te trajo el almuerzo.", "&d❤ &f¡La mamá de <player> lo dejó en la guardería!"),
      FBI("FBI", 3000, Material.IRON_DOOR, "Open up!", "&c&lFBI! &f<player> ha tirado la puerta abajo."),
      DAD("Papá", 3500, Material.LEATHER_CHESTPLATE, "Fue a por cigarros.", "&8<player> ha vuelto (al fin)."),
      PARTY("Fiesta", 4000, Material.CAKE, "A celebrar.", "&d\ud83c\udf89 &f¡Llegó <player>, que empiece la fiesta! &d\ud83c\udf89"),
      STREAMER("Streamer", 5000, Material.PURPLE_WOOL, "En vivo.", "&d\ud83c\udfa5 &f<player> está en directo. ¡Saluden!"),
      WILD("Salvaje", 5500, Material.GRASS_BLOCK, "Apareció un pokémon.", "&a¡Un <player> salvaje apareció!"),
      HANDSOME("El Guapo", 6000, Material.DIAMOND, "Demasiado brillo.", "&b✨ Aparten la mirada, llegó &l<player> &b✨"),
      GHOST("Fantasma", 7000, Material.SOUL_LANTERN, "Boo.", "&7Una sombra llamada <player> apareció..."),
      HEROBRINE("Herobrine", 8000, Material.GOLDEN_SWORD, "Miedo.", "&f&l<player> &7se ha unido a la partida..."),
      HACKER("Hacker", 9000, Material.COMMAND_BLOCK, "Consola.", "&c[ALERT] &f<player> ha inyectado el servidor."),
      ADMIN_FAKE("Falso Admin", 10000, Material.REDSTONE_BLOCK, "Asusta a todos.", "&4&lADMIN &8» &c<player> ha entrado en modo oculto."),
      MVP("MVP", 12000, Material.NETHER_STAR, "Jugador valioso.", "&b&lMVP &3» &f¡<player> ha aterrizado en la arena!"),
      KING("Rey", 15000, Material.GOLDEN_HELMET, "Realeza.", "&e\ud83d\udc51 ¡Su majestad <player> honra el servidor! \ud83d\udc51"),
      GOD("Dios", 20000, Material.BEACON, "Entrada épica.", "&6⚡ &e¡EL DIOS <player> HA LLEGADO! &6⚡");

      public String name;
      public int cost;
      public Material icon;
      public String desc;
      public String text;

      private JoinMessage(String param3, int param4, Material param5, String param6, String param7) {
         this.name = n;
         this.cost = c;
         this.icon = i;
         this.desc = d;
         this.text = t;
      }

      // $FF: synthetic method
      private static CosmeticsManager.JoinMessage[] $values() {
         return new CosmeticsManager.JoinMessage[]{DEFAULT, PIZZA, MOM, FBI, DAD, PARTY, STREAMER, WILD, HANDSOME, GHOST, HEROBRINE, HACKER, ADMIN_FAKE, MVP, KING, GOD};
      }
   }

   public static enum CatVariant {
      BLACK,
      BRITISH_SHORTHAIR,
      CALICO,
      JELLIE,
      PERSIAN,
      RAGDOLL,
      RED,
      SIAMESE,
      TABBY,
      WHITE,
      ALL_BLACK;

      // $FF: synthetic method
      private static CosmeticsManager.CatVariant[] $values() {
         return new CosmeticsManager.CatVariant[]{BLACK, BRITISH_SHORTHAIR, CALICO, JELLIE, PERSIAN, RAGDOLL, RED, SIAMESE, TABBY, WHITE, ALL_BLACK};
      }
   }

   public static enum RabbitVariant {
      BROWN,
      WHITE,
      BLACK,
      BLACK_AND_WHITE,
      GOLD,
      SALT_AND_PEPPER,
      THE_KILLER_BUNNY;

      // $FF: synthetic method
      private static CosmeticsManager.RabbitVariant[] $values() {
         return new CosmeticsManager.RabbitVariant[]{BROWN, WHITE, BLACK, BLACK_AND_WHITE, GOLD, SALT_AND_PEPPER, THE_KILLER_BUNNY};
      }
   }

   public static enum AxolotlVariant {
      LUCY,
      WILD,
      GOLD,
      CYAN,
      BLUE;

      // $FF: synthetic method
      private static CosmeticsManager.AxolotlVariant[] $values() {
         return new CosmeticsManager.AxolotlVariant[]{LUCY, WILD, GOLD, CYAN, BLUE};
      }
   }

   public static enum FoxVariant {
      RED,
      SNOW;

      // $FF: synthetic method
      private static CosmeticsManager.FoxVariant[] $values() {
         return new CosmeticsManager.FoxVariant[]{RED, SNOW};
      }
   }

   public static enum HorseColor {
      WHITE,
      CREAMY,
      CHESTNUT,
      BROWN,
      BLACK,
      GRAY,
      DARK_BROWN;

      // $FF: synthetic method
      private static CosmeticsManager.HorseColor[] $values() {
         return new CosmeticsManager.HorseColor[]{WHITE, CREAMY, CHESTNUT, BROWN, BLACK, GRAY, DARK_BROWN};
      }
   }

   public static enum LlamaColor {
      CREAMY,
      WHITE,
      BROWN,
      GRAY;

      // $FF: synthetic method
      private static CosmeticsManager.LlamaColor[] $values() {
         return new CosmeticsManager.LlamaColor[]{CREAMY, WHITE, BROWN, GRAY};
      }
   }

   public static enum PandaGene {
      NORMAL,
      LAZY,
      WORRIED,
      PLAYFUL,
      BROWN,
      WEAK,
      AGGRESSIVE;

      // $FF: synthetic method
      private static CosmeticsManager.PandaGene[] $values() {
         return new CosmeticsManager.PandaGene[]{NORMAL, LAZY, WORRIED, PLAYFUL, BROWN, WEAK, AGGRESSIVE};
      }
   }

   public static enum ParrotVariant {
      RED,
      BLUE,
      GREEN,
      CYAN,
      GRAY;

      // $FF: synthetic method
      private static CosmeticsManager.ParrotVariant[] $values() {
         return new CosmeticsManager.ParrotVariant[]{RED, BLUE, GREEN, CYAN, GRAY};
      }
   }

   public static enum FrogVariant {
      TEMPERATE,
      WARM,
      COLD;

      // $FF: synthetic method
      private static CosmeticsManager.FrogVariant[] $values() {
         return new CosmeticsManager.FrogVariant[]{TEMPERATE, WARM, COLD};
      }
   }

   public static enum ZombieStyle {
      NORMAL,
      GOLD,
      DIAMOND;

      // $FF: synthetic method
      private static CosmeticsManager.ZombieStyle[] $values() {
         return new CosmeticsManager.ZombieStyle[]{NORMAL, GOLD, DIAMOND};
      }
   }

   public static enum WitherStyle {
      NORMAL,
      CHARGED;

      // $FF: synthetic method
      private static CosmeticsManager.WitherStyle[] $values() {
         return new CosmeticsManager.WitherStyle[]{NORMAL, CHARGED};
      }
   }

   public static enum SimpleAgeVariant {
      ADULT,
      BABY;

      // $FF: synthetic method
      private static CosmeticsManager.SimpleAgeVariant[] $values() {
         return new CosmeticsManager.SimpleAgeVariant[]{ADULT, BABY};
      }
   }

   public static enum TrimDesign {
      SENTRY("Centinela", TrimPattern.SENTRY, Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE),
      DUNE("Duna", TrimPattern.DUNE, Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE),
      COAST("Costa", TrimPattern.COAST, Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE),
      WILD("Salvaje", TrimPattern.WILD, Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE),
      WARD("Guardián", TrimPattern.WARD, Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE),
      EYE("Ojo", TrimPattern.EYE, Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE),
      RIB("Costilla", TrimPattern.RIB, Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE),
      SPIRE("Aguja", TrimPattern.SPIRE, Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE),
      VEX("Vex", TrimPattern.VEX, Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE),
      TIDE("Marea", TrimPattern.TIDE, Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE),
      SNOUT("Hocico", TrimPattern.SNOUT, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);

      public String name;
      public TrimPattern pat;
      public Material icon;
      public int cost = 1000;
      public String desc = "";

      private TrimDesign(String param3, TrimPattern param4, Material param5) {
         this.name = n;
         this.pat = p;
         this.icon = i;
      }

      // $FF: synthetic method
      private static CosmeticsManager.TrimDesign[] $values() {
         return new CosmeticsManager.TrimDesign[]{SENTRY, DUNE, COAST, WILD, WARD, EYE, RIB, SPIRE, VEX, TIDE, SNOUT};
      }
   }

   public static enum TrimColor {
      REDSTONE("Rojo", TrimMaterial.REDSTONE, Material.REDSTONE),
      LAPIS("Azul", TrimMaterial.LAPIS, Material.LAPIS_LAZULI),
      QUARTZ("Blanco", TrimMaterial.QUARTZ, Material.QUARTZ),
      AMETHYST("Morado", TrimMaterial.AMETHYST, Material.AMETHYST_SHARD),
      GOLD("Dorado", TrimMaterial.GOLD, Material.GOLD_INGOT),
      EMERALD("Verde", TrimMaterial.EMERALD, Material.EMERALD),
      DIAMOND("Diamante", TrimMaterial.DIAMOND, Material.DIAMOND),
      NETHERITE("Negro", TrimMaterial.NETHERITE, Material.NETHERITE_INGOT);

      public String name;
      public TrimMaterial mat;
      public Material icon;
      public int cost = 500;
      public String desc = "";

      private TrimColor(String param3, TrimMaterial param4, Material param5) {
         this.name = n;
         this.mat = m;
         this.icon = i;
      }

      // $FF: synthetic method
      private static CosmeticsManager.TrimColor[] $values() {
         return new CosmeticsManager.TrimColor[]{REDSTONE, LAPIS, QUARTZ, AMETHYST, GOLD, EMERALD, DIAMOND, NETHERITE};
      }
   }
}
