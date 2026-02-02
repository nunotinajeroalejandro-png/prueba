package me.ethernova.ultimateffa;

import java.util.Iterator;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
   private final Main plugin;

   public CommandHandler(Main plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         return true;
      } else {
         Player p = (Player)sender;
         String pos;
         if (command.getName().equalsIgnoreCase("ffa")) {
            if (args.length == 0) {
               this.plugin.gameManager.openArenasGUI(p);
               return true;
            } else {
               String sub = args[0].toLowerCase();
               if (!sub.equals("help") && !sub.equals("ayuda")) {
                  if (!sub.equals("settings") && !sub.equals("opciones") && !sub.equals("ajustes")) {
                     if (!sub.equals("bypass") && !sub.equals("build")) {
                        String name;
                        if (sub.equals("setkit")) {
                           if (!p.hasPermission("ffa.admin")) {
                              p.sendMessage("§cSin permisos.");
                              return true;
                           } else if (args.length < 3) {
                              p.sendMessage("Uso: /ffa setkit <arena> <kit>");
                              return true;
                           } else {
                              name = args[1];
                              pos = args[2];
                              this.plugin.arenasConfig.set("ffa." + name + ".kit", pos);
                              this.plugin.saveArenas();
                              p.sendMessage("§aKit " + pos + " vinculado a " + name);
                              return true;
                           }
                        } else if (sub.equals("join")) {
                           if (args.length < 2) {
                              this.plugin.gameManager.openArenasGUI(p);
                           } else {
                              this.plugin.gameManager.joinFFA(p, args[1]);
                           }

                           return true;
                        } else if (!sub.equals("leave") && !sub.equals("lobby")) {
                           if (sub.equals("shop")) {
                              this.plugin.cosmeticsManager.openShopGUI(p);
                              return true;
                           } else if (sub.equals("top")) {
                              this.plugin.cosmeticsManager.openTopKillsMainMenu(p);
                              return true;
                           } else if (sub.equals("stats")) {
                              this.plugin.gameManager.openRankingMainMenu(p);
                              return true;
                           } else if (!sub.equals("missions") && !sub.equals("misiones")) {
                              if (!sub.equals("daily") && !sub.equals("diario")) {
                                 int amount;
                                 int cur;
                                 boolean deleted;
                                 Player target;
                                 if (sub.equals("bounty")) {
                                    if (args.length < 3) {
                                       p.sendMessage("§cUso: /ffa bounty <jugador> <cantidad>");
                                       return true;
                                    } else {
                                       target = Bukkit.getPlayer(args[1]);
                                       if (target == null) {
                                          p.sendMessage("§cJugador no encontrado.");
                                          return true;
                                       } else if (target.equals(p)) {
                                          p.sendMessage("§cNo puedes ponerte precio a ti mismo.");
                                          return true;
                                       } else {
                                          deleted = false;

                                          try {
                                             amount = Integer.parseInt(args[2]);
                                          } catch (NumberFormatException var11) {
                                             return true;
                                          }

                                          if (this.plugin.cosmeticsManager.getBalance(p) >= amount) {
                                             this.plugin.cosmeticsManager.addMoney(p, -amount);
                                             cur = this.plugin.statsConfig.getInt("players." + target.getUniqueId() + ".bounty", 0);
                                             this.plugin.statsConfig.set("players." + target.getUniqueId() + ".bounty", cur + amount);
                                             this.plugin.saveStats();
                                             Bukkit.broadcastMessage("§b§lBOUNTY §8» §e" + p.getName() + " §fpuso §6$" + amount + " §fpor la cabeza de §c" + target.getName());
                                          } else {
                                             p.sendMessage("§cNo tienes dinero.");
                                          }

                                          return true;
                                       }
                                    }
                                 } else if (sub.equals("setstreak")) {
                                    if (!p.hasPermission("ffa.admin")) {
                                       return true;
                                    } else if (args.length < 3) {
                                       p.sendMessage("§cUso: /ffa setstreak <jugador> <cantidad>");
                                       return true;
                                    } else {
                                       target = Bukkit.getPlayer(args[1]);
                                       if (target == null) {
                                          return true;
                                       } else {
                                          pos = this.plugin.gameManager.getPlayerArena(target);
                                          if (pos == null) {
                                             p.sendMessage("§cEl jugador no está dentro de una arena FFA.");
                                             return true;
                                          } else {
                                             try {
                                                cur = Integer.parseInt(args[2]);
                                                this.plugin.gameManager.setStreakManual(target, pos, cur);
                                                p.sendMessage("§aRacha de §e" + target.getName() + " §afijada en §e" + cur + " §7(Arena: " + pos + ")");
                                             } catch (Exception var12) {
                                                p.sendMessage("§cNúmero inválido.");
                                             }

                                             return true;
                                          }
                                       }
                                    }
                                 } else if (sub.equals("createarena")) {
                                    if (!p.hasPermission("ffa.admin")) {
                                       p.sendMessage("§cSin permisos.");
                                       return true;
                                    } else if (args.length < 2) {
                                       p.sendMessage(ChatColor.RED + "Uso: /ffa createarena <Nombre>");
                                       return true;
                                    } else {
                                       name = args[1];
                                       this.plugin.arenasConfig.createSection("ffa." + name);
                                       this.plugin.arenasConfig.set("ffa." + name + ".created", true);
                                       this.plugin.saveArenas();
                                       p.sendMessage(ChatColor.GREEN + "Arena FFA '" + name + "' creada.");
                                       p.sendMessage(ChatColor.GRAY + "1. Pon el spawn: /ffa set spawn " + name);
                                       p.sendMessage(ChatColor.GRAY + "2. Crea un kit con el MISMO nombre: /ffasetkit " + name);
                                       return true;
                                    }
                                 } else if (sub.equals("set") && args.length >= 4 && args[1].equalsIgnoreCase("allowbuild")) {
                                    name = args[2];
                                    deleted = Boolean.parseBoolean(args[3]);
                                    if (!this.plugin.arenasConfig.contains("ffa." + name)) {
                                       p.sendMessage(ChatColor.RED + "La arena no existe.");
                                       return true;
                                    } else {
                                       this.plugin.arenasConfig.set("ffa." + name + ".allowBuild", deleted);
                                       this.plugin.saveArenas();
                                       p.sendMessage(ChatColor.GREEN + "Modo construcción en arena '" + name + "' establecido a: " + deleted);
                                       return true;
                                    }
                                 } else if (sub.equals("kitsettings") && args.length >= 4 && args[1].equalsIgnoreCase("build")) {
                                    if (!p.hasPermission("ffa.admin")) {
                                       p.sendMessage("§cSin permisos.");
                                       return true;
                                    } else {
                                       name = args[2];
                                       deleted = Boolean.parseBoolean(args[3]);
                                       if (!this.plugin.kitsConfig.contains("kits." + name)) {
                                          p.sendMessage(ChatColor.RED + "El kit no existe.");
                                          return true;
                                       } else {
                                          this.plugin.kitsConfig.set("kits." + name + ".allowBuild", deleted);
                                          this.plugin.saveKits();
                                          p.sendMessage(ChatColor.GREEN + "Modo construcción para kit '" + name + "' establecido a: " + deleted);
                                          return true;
                                       }
                                    }
                                 } else if (sub.equals("setkit")) {
                                    if (args.length < 3) {
                                       p.sendMessage(ChatColor.RED + "Uso: /ffa setkit <nombre_arena> <nombre_kit>");
                                       return true;
                                    } else {
                                       name = args[1];
                                       pos = args[2];
                                       if (!this.plugin.arenasConfig.contains("ffa." + name)) {
                                          p.sendMessage(ChatColor.RED + "La arena '" + name + "' no existe.");
                                          return true;
                                       } else if (!this.plugin.kitsConfig.contains("kits." + pos)) {
                                          p.sendMessage(ChatColor.RED + "El kit '" + pos + "' no existe. Crea uno con /ffasetkit.");
                                          return true;
                                       } else {
                                          this.plugin.arenasConfig.set("ffa." + name + ".kit", pos);
                                          this.plugin.saveArenas();
                                          p.sendMessage(ChatColor.GREEN + "✅ Kit '" + pos + "' vinculado a la arena '" + name + "'.");
                                          return true;
                                       }
                                    }
                                 } else if (!p.hasPermission("ffa.admin")) {
                                    p.sendMessage(ChatColor.RED + "No tienes rango administrativo.");
                                    return true;
                                 } else if (sub.equals("deletelobby") || sub.equals("delete") && args.length > 1 && args[1].equalsIgnoreCase("lobby")) {
                                    this.plugin.gameManager.deleteLobby();
                                    p.sendMessage(ChatColor.GREEN + "✅ Spawn del Lobby eliminado correctamente.");
                                    return true;
                                 } else if (!sub.equals("announce") && !sub.equals("anuncio")) {
                                    OfflinePlayer target;
                                    if (!sub.equals("setprestige") && !sub.equals("addprestige") && !sub.equals("removeprestige")) {
                                       if (sub.equals("setmoney")) {
                                          if (args.length < 3) {
                                             p.sendMessage(ChatColor.RED + "/ffa setmoney <p> <n>");
                                             return true;
                                          } else {
                                             target = Bukkit.getOfflinePlayer(args[1]);

                                             try {
                                                amount = Integer.parseInt(args[2]);
                                                this.plugin.statsConfig.set("players." + target.getUniqueId() + ".balance", amount);
                                                this.plugin.saveStats();
                                                p.sendMessage(ChatColor.GREEN + "Balance de " + target.getName() + " fijado en $" + amount);
                                             } catch (Exception var13) {
                                                p.sendMessage(ChatColor.RED + "Cantidad inválida.");
                                             }

                                             return true;
                                          }
                                       } else if (sub.equals("addmoney")) {
                                          if (args.length < 3) {
                                             p.sendMessage(ChatColor.RED + "/ffa addmoney <p> <n>");
                                             return true;
                                          } else {
                                             target = Bukkit.getPlayer(args[1]);
                                             if (target == null) {
                                                return true;
                                             } else {
                                                try {
                                                   amount = Integer.parseInt(args[2]);
                                                   this.plugin.cosmeticsManager.addMoney(target, amount);
                                                   p.sendMessage(ChatColor.GREEN + "Diste $" + amount + " a " + target.getName());
                                                } catch (Exception var14) {
                                                   p.sendMessage(ChatColor.RED + "Cantidad inválida.");
                                                }

                                                return true;
                                             }
                                          }
                                       } else if (sub.equals("setkills")) {
                                          if (args.length < 3) {
                                             p.sendMessage(ChatColor.RED + "/ffa setkills <p> <n>");
                                             return true;
                                          } else {
                                             target = Bukkit.getOfflinePlayer(args[1]);

                                             try {
                                                amount = Integer.parseInt(args[2]);
                                                this.plugin.statsConfig.set("players." + target.getUniqueId() + ".global.kills", amount);
                                                this.plugin.saveStats();
                                                p.sendMessage(ChatColor.GREEN + "Kills de " + target.getName() + " fijadas en " + amount);
                                             } catch (Exception var15) {
                                                p.sendMessage(ChatColor.RED + "Cantidad inválida.");
                                             }

                                             return true;
                                          }
                                       } else if (!sub.equals("set")) {
                                          if (sub.equals("deletearena")) {
                                             if (!p.hasPermission("ffa.admin")) {
                                                return true;
                                             } else if (args.length < 2) {
                                                p.sendMessage(ChatColor.RED + "Uso: /ffa deletearena <Nombre>");
                                                return true;
                                             } else {
                                                name = args[1];
                                                deleted = false;
                                                if (this.plugin.arenasConfig.contains("ffa." + name)) {
                                                   this.plugin.arenasConfig.set("ffa." + name, (Object)null);
                                                   deleted = true;
                                                }

                                                if (this.plugin.arenasConfig.contains("duels." + name)) {
                                                   this.plugin.arenasConfig.set("duels." + name, (Object)null);
                                                   deleted = true;
                                                }

                                                if (deleted) {
                                                   this.plugin.saveArenas();
                                                   p.sendMessage(ChatColor.GREEN + "✅ Arena '" + name + "' eliminada correctamente (FFA/Duel).");
                                                } else {
                                                   p.sendMessage(ChatColor.RED + "❌ No se encontró ninguna arena llamada '" + name + "'.");
                                                }

                                                return true;
                                             }
                                          } else if (sub.equals("tparena")) {
                                             if (args.length < 2) {
                                                return true;
                                             } else {
                                                if (this.plugin.arenasConfig.contains("ffa." + args[1])) {
                                                   p.teleport((Location)this.plugin.arenasConfig.get("ffa." + args[1] + ".spawn"));
                                                   p.sendMessage(ChatColor.AQUA + "Teletransportado a: " + args[1]);
                                                }

                                                return true;
                                             }
                                          } else if (sub.equals("listarenas")) {
                                             if (!this.plugin.arenasConfig.contains("ffa")) {
                                                return true;
                                             } else {
                                                p.sendMessage(ChatColor.GOLD + "Arenas FFA: " + ChatColor.WHITE + String.join(", ", this.plugin.arenasConfig.getConfigurationSection("ffa").getKeys(false)));
                                                return true;
                                             }
                                          } else if (sub.equals("admin")) {
                                             this.plugin.gameManager.openAdminGUI(p);
                                             return true;
                                          } else if (sub.equals("reload")) {
                                             this.plugin.reloadFiles();
                                             p.sendMessage(ChatColor.AQUA + "UltimateFFA: Archivos recargados.");
                                             return true;
                                          } else if (sub.equals("resetplayer")) {
                                             if (args.length < 2) {
                                                return true;
                                             } else {
                                                target = Bukkit.getOfflinePlayer(args[1]);
                                                this.plugin.statsConfig.set("players." + target.getUniqueId(), (Object)null);
                                                this.plugin.saveStats();
                                                p.sendMessage(ChatColor.RED + "Datos de " + target.getName() + " borrados.");
                                                return true;
                                             }
                                          } else if (sub.equals("setlobby")) {
                                             this.plugin.gameManager.setLobby(p);
                                             return true;
                                          } else {
                                             return true;
                                          }
                                       } else if (args.length >= 4 && args[1].equalsIgnoreCase("duelspawn")) {
                                          if (!p.hasPermission("ffa.admin")) {
                                             return true;
                                          } else {
                                             name = args[2];
                                             pos = args[3];
                                             if (!pos.equals("1") && !pos.equals("2")) {
                                                p.sendMessage(ChatColor.RED + "Uso: /ffa set duelspawn <NombreArena> <1/2>");
                                             } else {
                                                this.plugin.arenasConfig.set("duels." + name + "." + pos, p.getLocation());
                                                this.plugin.saveArenas();
                                                p.sendMessage("§b§lFFA §8» §f§aSpawn " + pos + " establecido para la arena de duelo: §e" + name);
                                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
                                             }

                                             return true;
                                          }
                                       } else if (args.length < 2) {
                                          p.sendMessage(ChatColor.RED + "/ffa set <lobby|spawn> [n]");
                                          return true;
                                       } else {
                                          name = args[1].toLowerCase();
                                          if (name.equals("lobby")) {
                                             this.plugin.gameManager.setLobby(p);
                                          } else if (name.equals("spawn")) {
                                             if (args.length < 3) {
                                                p.sendMessage(ChatColor.RED + "/ffa set spawn <nombre>");
                                                return true;
                                             }

                                             this.plugin.arenasConfig.set("ffa." + args[2] + ".spawn", p.getLocation());
                                             this.plugin.saveArenas();
                                             p.sendMessage(ChatColor.GREEN + "Spawn de arena '" + args[2] + "' guardado.");
                                          }

                                          return true;
                                       }
                                    } else if (args.length < 3) {
                                       p.sendMessage(ChatColor.RED + "/ffa " + sub + " <p> <n>");
                                       return true;
                                    } else {
                                       target = Bukkit.getOfflinePlayer(args[1]);

                                       try {
                                          amount = Integer.parseInt(args[2]);
                                          cur = this.plugin.statsConfig.getInt("players." + target.getUniqueId() + ".prestige", 0);
                                          int res = amount;
                                          if (sub.equals("addprestige")) {
                                             res = cur + amount;
                                          } else if (sub.equals("removeprestige")) {
                                             res = Math.max(0, cur - amount);
                                          }

                                          this.plugin.statsConfig.set("players." + target.getUniqueId() + ".prestige", res);
                                          this.plugin.saveStats();
                                          p.sendMessage(ChatColor.GREEN + "Prestigio de " + target.getName() + " fijado en " + res);
                                       } catch (Exception var16) {
                                          p.sendMessage(ChatColor.RED + "Valor inválido.");
                                       }

                                       return true;
                                    }
                                 } else if (args.length < 2) {
                                    p.sendMessage(ChatColor.RED + "Uso: /ffa announce <mensaje>");
                                    return true;
                                 } else {
                                    StringBuilder sb = new StringBuilder();

                                    for(amount = 1; amount < args.length; ++amount) {
                                       sb.append(args[amount]).append(" ");
                                    }

                                    pos = ChatColor.translateAlternateColorCodes('&', sb.toString().trim());
                                    Iterator var27 = Bukkit.getOnlinePlayers().iterator();

                                    while(var27.hasNext()) {
                                       Player all = (Player)var27.next();
                                       all.sendTitle(ChatColor.GOLD + "¡AVISO!", pos, 10, 70, 20);
                                       all.playSound(all.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 0.8F);
                                       all.sendMessage("§b§lFFA §8» §f" + pos);
                                    }

                                    return true;
                                 }
                              } else {
                                 this.plugin.gameManager.openDailyRewardsGUI(p);
                                 return true;
                              }
                           } else {
                              this.plugin.gameManager.openMissionsGUI(p);
                              return true;
                           }
                        } else {
                           if (this.plugin.gameManager.combatTag.containsKey(p.getUniqueId())) {
                              long timeLeft = ((Long)this.plugin.gameManager.combatTag.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000L;
                              if (timeLeft > 0L) {
                                 p.sendMessage("§c§lCOMBATE §8» §7Espera §e" + timeLeft + "s §7para salir.");
                                 return true;
                              }
                           }

                           if (this.plugin.gameManager.isInDuel(p)) {
                              GameManager.DuelMatch match = this.plugin.gameManager.getMatch(p);
                              if (match != null) {
                                 Player winner = match.p1.equals(p.getUniqueId()) ? Bukkit.getPlayer(match.p2) : Bukkit.getPlayer(match.p1);
                                 this.plugin.gameManager.endDuel(match, winner, p);
                                 return true;
                              }
                           }

                           p.sendMessage("§aEnviando al lobby...");
                           this.plugin.gameManager.sendToLobby(p);
                           return true;
                        }
                     } else if (!p.hasPermission("ffa.admin")) {
                        p.sendMessage("§cSin permisos.");
                        return true;
                     } else {
                        boolean estado = this.plugin.gameManager.toggleBuildMode(p);
                        if (estado) {
                           p.sendMessage(ChatColor.GREEN + "\ud83d\udd13 Modo Constructor ACTIVADO.");
                           p.setGameMode(GameMode.CREATIVE);
                        } else {
                           p.sendMessage(ChatColor.RED + "\ud83d\udd12 Modo Constructor DESACTIVADO.");
                           p.setGameMode(GameMode.ADVENTURE);
                        }

                        return true;
                     }
                  } else {
                     this.plugin.cosmeticsManager.openSettingsGUI(p);
                     return true;
                  }
               } else {
                  p.sendMessage("§8§l§m     §b§l ULTIMATE FFA §8§l§m     ");
                  p.sendMessage("§7¡Bienvenido §b" + p.getName() + "§7! Aquí tienes la guía:");
                  p.sendMessage("");
                  p.sendMessage("§e§l▶ COMANDOS DIARIOS");
                  p.sendMessage(" §e§l● §f/ffa §bmissions §8- §7Consulta tus retos diarios.");
                  p.sendMessage(" §e§l● §f/ffa §bdaily §8- §7Reclama tu recompensa por entrar.");
                  p.sendMessage("");
                  p.sendMessage("§e§l▶ COMBATE Y ESTADÍSTICAS");
                  p.sendMessage(" §e§l● §f/ffa §bjoin §7<mapa> §8- §7Entra a una arena de combate.");
                  p.sendMessage(" §e§l● §f/ffa §bshop §8- §7Tienda de cosméticos y prestigio.");
                  p.sendMessage(" §e§l● §f/ffa §bstats §8- §7Ver tu perfil y estadísticas.");
                  p.sendMessage(" §e§l● §f/ffa §btop §8- §7Ver el ranking de mejores asesinos.");
                  p.sendMessage(" §e§l● §f/ffa §bbounty §7<p> <n> §8- §7Pon precio a una cabeza.");
                  p.sendMessage("");
                  p.sendMessage("§e§l▶ DUELOS Y PROGRESO");
                  p.sendMessage(" §e§l● §f/1v1 §binvite §7<p> §8- §7Retar a un jugador a duelo.");
                  p.sendMessage(" §e§l● §f/1v1 §baccept §7<p> §8- §7Aceptar un reto pendiente.");
                  p.sendMessage(" §e§l● §f/prestige §8- §7Subir nivel (Costo: 100 Kills).");
                  p.sendMessage("");
                  if (p.hasPermission("ffa.admin")) {
                     p.sendMessage("§c§l▶ ADMINISTRACIÓN");
                     p.sendMessage(" §c§l● §f/ffa §bannounce §7<msg> §8- §7Aviso épico global.");
                     p.sendMessage(" §c§l● §f/ffa §bsetprestige §7<p> <n> §8- §7Nivel de prestigio.");
                     p.sendMessage(" §c§l● §f/ffa §bsetmoney §7<p> <n> §8- §7Editar balance.");
                     p.sendMessage(" §c§l● §f/ffa §bsetkills §7<p> <n> §8- §7Editar asesinatos.");
                     p.sendMessage(" §c§l● §f/ffa §bresetplayer §7<p> §8- §7Borrar todos los datos.");
                     p.sendMessage("");
                     p.sendMessage(" §c§l● §f/ffa §bset lobby §8- §7Establecer punto de aparición.");
                     p.sendMessage(" §c§l● §f/ffa §bset spawn §7<n> §8- §7Crear/Setear arena.");
                     p.sendMessage(" §c§l● §f/ffa §bdeletearena §7<n> §8- §7Eliminar un mapa.");
                     p.sendMessage(" §c§l● §f/ffa §blistarenas §8- §7Listar mapas configurados.");
                     p.sendMessage(" §c§l● §f/ffa §breload §8- §7Recargar archivos de configuración.");
                     if (sub.equals("deletelobby")) {
                        this.plugin.gameManager.deleteLobby();
                        p.sendMessage(" §c§l● §f/ffa §bdeletelobby §8- §7Eliminado Correctamente.");
                        return true;
                     }
                  }

                  p.sendMessage("");
                  p.sendMessage("§8§l§m                         ");
                  return true;
               }
            }
         } else {
            int betAmount;
            if (command.getName().equalsIgnoreCase("prestige")) {
               int kills = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".global.kills");
               betAmount = this.plugin.statsConfig.getInt("players." + p.getUniqueId() + ".prestige", 0);
               if (betAmount >= 5) {
                  p.sendMessage(ChatColor.RED + "¡Ya tienes el prestigio máximo!");
                  return true;
               } else if (kills < 100) {
                  p.sendMessage(ChatColor.RED + "Necesitas 100 kills para evolucionar.");
                  return true;
               } else {
                  this.plugin.statsConfig.set("players." + p.getUniqueId() + ".global.kills", kills - 100);
                  this.plugin.statsConfig.set("players." + p.getUniqueId() + ".prestige", betAmount + 1);
                  this.plugin.saveStats();
                  p.sendTitle("§6§l¡EVOLUCIÓN!", "§fAhora eres Prestigio §e" + (betAmount + 1), 10, 70, 20);
                  p.getWorld().strikeLightningEffect(p.getLocation());
                  Bukkit.broadcastMessage("§8§m--------------------------------------");
                  Bukkit.broadcastMessage("   §d§l⚡ §b" + p.getName() + " §fha subido al prestigio §e" + (betAmount + 1) + " ⭐");
                  Bukkit.broadcastMessage("§8§m--------------------------------------");
                  return true;
               }
            } else if (command.getName().equalsIgnoreCase("1v1")) {
               if (args.length == 0) {
                  this.plugin.gameManager.openDuelMenu(p);
                  return true;
               } else {
                  Player target;
                  if (args[0].equalsIgnoreCase("invite")) {
                     if (args.length < 2) {
                        p.sendMessage(ChatColor.RED + "Uso: /1v1 invite <jugador> [cantidad]");
                        return true;
                     } else {
                        target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                           p.sendMessage(ChatColor.RED + "Jugador no encontrado.");
                           return true;
                        } else if (target == p) {
                           p.sendMessage(ChatColor.RED + "No puedes retarte a ti mismo.");
                           return true;
                        } else if (!this.plugin.gameManager.acceptsDuels(target)) {
                           p.sendMessage("§cEste jugador ha desactivado las solicitudes de duelo.");
                           return true;
                        } else {
                           betAmount = 0;
                           if (args.length >= 3) {
                              try {
                                 betAmount = Integer.parseInt(args[2]);
                              } catch (NumberFormatException var17) {
                                 p.sendMessage("§cCantidad inválida.");
                                 return true;
                              }
                           }

                           if (betAmount > 0) {
                              if (this.plugin.cosmeticsManager.getBalance(p) < betAmount) {
                                 p.sendMessage("§cNo tienes suficiente dinero.");
                                 return true;
                              }

                              this.plugin.gameManager.tempBets.put(p.getUniqueId(), betAmount);
                              p.sendMessage("§eApuesta de §6$" + betAmount + " §eregistrada.");
                           }

                           this.plugin.gameManager.pendingInvite.put(p.getUniqueId(), target.getUniqueId());
                           this.plugin.gameManager.openKitSelector(p, false);
                           return true;
                        }
                     }
                  } else if (args[0].equalsIgnoreCase("accept")) {
                     UUID myID = p.getUniqueId();
                     if (!this.plugin.gameManager.duelRequests.containsKey(myID)) {
                        p.sendMessage("§cNo tienes invitaciones pendientes.");
                        return true;
                     } else {
                        UUID senderID = (UUID)this.plugin.gameManager.duelRequests.get(myID);
                        pos = (String)this.plugin.gameManager.duelKitRequests.get(myID);
                        Player challenger = Bukkit.getPlayer(senderID);
                        if (challenger != null && challenger.isOnline()) {
                           p.sendMessage("§a¡Has aceptado el duelo!");
                           challenger.sendMessage("§a" + p.getName() + " ha aceptado tu duelo.");
                           this.plugin.gameManager.duelRequests.remove(myID);
                           this.plugin.gameManager.duelKitRequests.remove(myID);
                           this.plugin.gameManager.startDuel(challenger, p, pos);
                           return true;
                        } else {
                           p.sendMessage("§cEl jugador que te invitó se ha desconectado.");
                           this.plugin.gameManager.duelRequests.remove(myID);
                           this.plugin.gameManager.duelKitRequests.remove(myID);
                           return true;
                        }
                     }
                  } else if (args.length <= 0 || !args[0].equalsIgnoreCase("deny") && !args[0].equalsIgnoreCase("rechazar")) {
                     if (args.length > 0 && (args[0].equalsIgnoreCase("spectate") || args[0].equalsIgnoreCase("ver"))) {
                        if (args.length < 2) {
                           p.sendMessage(ChatColor.RED + "Uso: /1v1 spectate <jugador>");
                           return true;
                        } else {
                           target = Bukkit.getPlayer(args[1]);
                           if (target != null && !this.plugin.gameManager.acceptsSpectators(target)) {
                              p.sendMessage("§cEste jugador tiene desactivado el modo espectador.");
                              return true;
                           } else {
                              this.plugin.gameManager.spectateDuel(p, target);
                              return true;
                           }
                        }
                     } else {
                        this.plugin.gameManager.openDuelMenu(p);
                        return true;
                     }
                  } else {
                     this.plugin.gameManager.pendingInvites.remove(p.getUniqueId());
                     p.sendMessage(ChatColor.YELLOW + "Reto rechazado.");
                     return true;
                  }
               }
            } else if (command.getName().equalsIgnoreCase("ffasetkit")) {
               if (!p.hasPermission("ffa.admin")) {
                  return true;
               } else if (args.length < 1) {
                  p.sendMessage(ChatColor.RED + "/ffasetkit <n>");
                  return true;
               } else {
                  this.plugin.kitsConfig.set("kits." + args[0] + ".inventory", p.getInventory().getContents());
                  this.plugin.kitsConfig.set("kits." + args[0] + ".armor", p.getInventory().getArmorContents());
                  this.plugin.saveKits();
                  this.plugin.reloadKits();
                  p.sendMessage(ChatColor.GREEN + "Kit actualizado en tiempo real.");
                  p.sendMessage(ChatColor.GREEN + "¡Kit " + args[0] + " guardado!");
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }
}
