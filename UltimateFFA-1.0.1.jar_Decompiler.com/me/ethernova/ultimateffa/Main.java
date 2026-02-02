package me.ethernova.ultimateffa;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   public static final String PREFIX = "§b§lFFA §8» §f";
   public GameManager gameManager;
   public CosmeticsManager cosmeticsManager;
   public BlockRestoreManager restoreManager;
   public FileConfiguration arenasConfig;
   public File arenasFile;
   public FileConfiguration statsConfig;
   public File statsFile;
   public FileConfiguration kitsConfig;
   public File kitsFile;
   public FileConfiguration messagesConfig;
   public File messagesFile;

   public void onEnable() {
      this.createConfigs();
      this.messagesFile = new File(this.getDataFolder(), "messages.yml");
      if (!this.messagesFile.exists()) {
         this.saveResource("messages.yml", false);
      }

      this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
      this.restoreManager = new BlockRestoreManager(this);
      this.cosmeticsManager = new CosmeticsManager(this);
      this.gameManager = new GameManager(this);
      this.gameManager.startFFAMapResetTask();
      CommandHandler handler = new CommandHandler(this);
      this.getCommand("ffa").setExecutor(handler);
      this.getCommand("ffa").setTabCompleter(new FFATabCompleter(this));
      this.getCommand("ffasetkit").setExecutor(handler);
      this.getCommand("1v1").setExecutor(handler);
      this.getCommand("1v1").setTabCompleter(new FFATabCompleter(this));
      this.getCommand("prestige").setExecutor(handler);
      this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
      this.getServer().getPluginManager().registerEvents(new QueueListener(this), this);
      this.gameManager.startGlobalTasks();
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         (new FFAExpansion(this)).register();
      }

      this.getLogger().info("UltimateFFA v" + this.getDescription().getVersion() + " habilitado correctamente.");
   }

   public void reloadKits() {
      if (this.kitsFile == null) {
         this.kitsFile = new File(this.getDataFolder(), "kits.yml");
      }

      this.kitsConfig = YamlConfiguration.loadConfiguration(this.kitsFile);
   }

   public void onDisable() {
      if (this.gameManager != null) {
         this.gameManager.restoreAllArenasForce();
      }

      this.saveStats();
      this.getLogger().info("UltimateFFA deshabilitado.");
   }

   public void createConfigs() {
      if (!this.getDataFolder().exists()) {
         this.getDataFolder().mkdirs();
      }

      this.saveDefaultConfig();
      this.arenasFile = new File(this.getDataFolder(), "arenas.yml");
      if (!this.arenasFile.exists()) {
         this.saveResource("arenas.yml", false);
      }

      this.arenasConfig = YamlConfiguration.loadConfiguration(this.arenasFile);
      this.statsFile = new File(this.getDataFolder(), "stats.yml");
      if (!this.statsFile.exists()) {
         this.saveResource("stats.yml", false);
      }

      this.statsConfig = YamlConfiguration.loadConfiguration(this.statsFile);
      this.kitsFile = new File(this.getDataFolder(), "kits.yml");
      if (!this.kitsFile.exists()) {
         this.saveResource("kits.yml", false);
      }

      this.kitsConfig = YamlConfiguration.loadConfiguration(this.kitsFile);
   }

   public void reloadFiles() {
      this.reloadConfig();
      this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
      this.arenasConfig = YamlConfiguration.loadConfiguration(this.arenasFile);
      this.statsConfig = YamlConfiguration.loadConfiguration(this.statsFile);
      this.kitsConfig = YamlConfiguration.loadConfiguration(this.kitsFile);
   }

   public void saveArenas() {
      try {
         this.arenasConfig.save(this.arenasFile);
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }

   public void saveStats() {
      try {
         this.statsConfig.save(this.statsFile);
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }

   public void saveKits() {
      try {
         this.kitsConfig.save(this.kitsFile);
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }

   public String msg(String path) {
      String message = this.messagesConfig.getString(path);
      return message == null ? "§c[Error: " + path + " no existe]" : ChatColor.translateAlternateColorCodes('&', message);
   }
}
