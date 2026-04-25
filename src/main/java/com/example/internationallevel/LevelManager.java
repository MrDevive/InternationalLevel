package com.example.internationallevel;

import com.example.internationallevel.events.ExpGainEvent;
import com.example.internationallevel.models.LevelData;
import com.example.internationallevel.models.PlayerData;
import com.example.internationallevel.models.Reward;
import com.example.internationallevel.models.VeteranLevelData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LevelManager {

    private final InternationalLevel plugin;
    private final Map<UUID, PlayerData> playerData;
    private final SortedMap<Integer, LevelData> levels;
    private final Map<Integer, Reward> rewards;
    private final SortedMap<Integer, VeteranLevelData> veteranLevels;

    private final DatabaseManager databaseManager;

    public LevelManager(InternationalLevel plugin, boolean useMySQL) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.playerData = new ConcurrentHashMap<>();
        this.levels = new TreeMap<>();
        this.rewards = new HashMap<>();
        this.veteranLevels = new TreeMap<>();
    }

    /**
     * Загрузка настроек уровней из level_settings.yml
     */
    public void loadLevels() {
        File levelFile = new File(plugin.getDataFolder(), "level_settings.yml");
        if (!levelFile.exists()) {
            plugin.saveResource("level_settings.yml", false);
        }
        FileConfiguration levelConfig = YamlConfiguration.loadConfiguration(levelFile);
        levels.clear();

        if (levelConfig.contains("levels")) {
            ConfigurationSection sec = levelConfig.getConfigurationSection("levels");
            if (sec != null) {
                for (String levelStr : sec.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        long requiredExp = sec.getLong(levelStr + ".required-exp");
                        String name = sec.getString(levelStr + ".name", "Уровень " + level);
                        String suffix = sec.getString(levelStr + ".suffix", "");
                        String league = sec.getString(levelStr + ".league", "Нет лиги");
                        double health = sec.getDouble(levelStr + ".health-bonus", 0);
                        double damage = sec.getDouble(levelStr + ".damage-bonus", 0);
                        double speed = sec.getDouble(levelStr + ".speed-bonus", 0);
                        double defense = sec.getDouble(levelStr + ".defense-bonus", 0);

                        levels.put(level, new LevelData(level, requiredExp, name, suffix, league,
                                health, damage, speed, defense));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Ошибка загрузки уровня " + levelStr + ": " + e.getMessage());
                    }
                }
            }
        }

        plugin.getLogger().info("Загружено уровней: " + levels.size());
    }

    /**
     * Загрузка уровней ветерана
     */
    public void loadVeteranLevels() {
        File veteranFile = new File(plugin.getDataFolder(), "veteran_levels.yml");
        if (!veteranFile.exists()) {
            plugin.saveResource("veteran_levels.yml", false);
        }
        FileConfiguration veteranConfig = YamlConfiguration.loadConfiguration(veteranFile);
        veteranLevels.clear();

        if (veteranConfig.contains("veteran-levels")) {
            ConfigurationSection sec = veteranConfig.getConfigurationSection("veteran-levels");
            if (sec != null) {
                for (String levelStr : sec.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        long requiredExp = sec.getLong(levelStr + ".required-exp");
                        String name = sec.getString(levelStr + ".name", "Ветеран " + level);
                        String description = sec.getString(levelStr + ".description", "");
                        List<String> rewards = sec.getStringList(levelStr + ".rewards");

                        veteranLevels.put(level, new VeteranLevelData(level, requiredExp, name, description, rewards));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Ошибка загрузки уровня ветерана " + levelStr + ": " + e.getMessage());
                    }
                }
            }
        }

        plugin.getLogger().info("Загружено уровней ветерана: " + veteranLevels.size());
    }

    /**
     * Загрузка наград из level_rewards.yml
     */
    public void loadRewards() {
        File rewardFile = new File(plugin.getDataFolder(), "level_rewards.yml");
        if (!rewardFile.exists()) {
            plugin.saveResource("level_rewards.yml", false);
        }
        FileConfiguration rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
        rewards.clear();

        if (rewardConfig.contains("rewards")) {
            ConfigurationSection sec = rewardConfig.getConfigurationSection("rewards");
            if (sec != null) {
                for (String levelStr : sec.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        List<String> commands = sec.getStringList(levelStr + ".commands");
                        String message = sec.getString(levelStr + ".message", "");
                        rewards.put(level, new Reward(level, commands, message));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Ошибка загрузки награды для уровня " + levelStr + ": " + e.getMessage());
                    }
                }
            }
        }

        plugin.getLogger().info("Загружено наград: " + rewards.size());
    }

    /**
     * Загрузка данных игроков из MySQL
     */
    public void loadPlayerData() {
        playerData.clear();
        plugin.getLogger().info("Данные игроков будут загружаться из MySQL по мере необходимости");
    }

    /**
     * Сохранение данных игроков в MySQL
     */
    public void savePlayerData() {
        for (PlayerData data : playerData.values()) {
            databaseManager.savePlayerData(data);
        }
    }

    /**
     * Получение данных игрока
     */
    public PlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), k -> {
            try {
                return databaseManager.loadPlayerData(k, player.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка загрузки данных из MySQL: " + e.getMessage());
                return new PlayerData(k, player.getName());
            }
        });
    }

    /**
     * Получить все данные игроков
     */
    public Collection<PlayerData> getAllPlayerData() {
        return playerData.values();
    }

    public LevelData getLevelData(int level) {
        return levels.get(level);
    }

    public LevelData getCurrentLevelData(Player player) {
        int lvl = getPlayerData(player).getLevel();
        return levels.get(lvl);
    }

    public LevelData getNextLevelData(Player player) {
        int current = getPlayerData(player).getLevel();
        for (int next : levels.keySet()) {
            if (next > current) return levels.get(next);
        }
        return null;
    }

    public long getExpToNextLevel(Player player) {
        LevelData next = getNextLevelData(player);
        if (next == null) return -1;
        long currentExp = getPlayerData(player).getExperience();
        return next.getRequiredExp() - currentExp;
    }

    public double getProgress(Player player) {
        PlayerData pd = getPlayerData(player);
        LevelData current = getCurrentLevelData(player);
        if (current == null) return 100;
        long required = current.getRequiredExp();
        LevelData next = getNextLevelData(player);
        long nextRequired = (next != null) ? next.getRequiredExp() : required;
        long exp = pd.getExperience();
        if (exp >= nextRequired) return 100;
        if (exp <= required) return 0;
        return (double) (exp - required) / (nextRequired - required) * 100;
    }

    /**
     * Добавить опыт игроку
     */
    public void addExperience(Player player, long amount, String source) {
        PlayerData pd = getPlayerData(player);
        long newExp = pd.getExperience() + amount;
        pd.setExperience(newExp);

        ExpGainEvent expEvent = new ExpGainEvent(player, amount, source);
        Bukkit.getPluginManager().callEvent(expEvent);

        boolean leveledUp = false;
        int oldLevel = pd.getLevel();
        int newLevel = oldLevel;

        while (true) {
            LevelData next = getNextLevelData(player);
            if (next == null) break;
            if (pd.getExperience() >= next.getRequiredExp()) {
                newLevel = next.getLevel();
                pd.setLevel(newLevel);
                leveledUp = true;
            } else {
                break;
            }
        }

        savePlayerData();

        if (leveledUp) {
            Reward reward = rewards.get(newLevel);
            if (reward != null) {
                for (String cmd : reward.getCommands()) {
                    String parsedCmd = cmd.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
                }
                if (!reward.getMessage().isEmpty()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("level", String.valueOf(newLevel));
                    placeholders.put("level_name", getCurrentLevelData(player).getName());
                    plugin.getMessageManager().sendMessage(player, "levelup", placeholders);
                }
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("level", String.valueOf(newLevel));
            placeholders.put("level_name", getCurrentLevelData(player).getName());
            placeholders.put("league", getCurrentLevelData(player).getLeague());
            plugin.getMessageManager().sendMessage(player, "levelup", placeholders);
        }

        if (plugin.getConfig().getBoolean("show-exp-messages", true)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("exp", String.valueOf(amount));
            placeholders.put("source", source);
            plugin.getMessageManager().sendMessage(player, "exp-gain", placeholders);
        }
    }

    /**
     * Добавить опыт ветерана
     */
    public void addVeteranExperience(Player player, long amount, String source) {
        PlayerData pd = getPlayerData(player);
        pd.setVeteranExperience(pd.getVeteranExperience() + amount);

        boolean leveledUp = false;
        int oldLevel = pd.getVeteranLevel();
        int newLevel = oldLevel;

        while (true) {
            VeteranLevelData next = getNextVeteranLevelData(player);
            if (next == null) break;
            if (pd.getVeteranExperience() >= next.getRequiredExp()) {
                newLevel = next.getLevel();
                pd.setVeteranLevel(newLevel);
                leveledUp = true;
            } else {
                break;
            }
        }

        if (leveledUp) {
            VeteranLevelData newLevelData = veteranLevels.get(newLevel);
            if (newLevelData != null && !newLevelData.getRewards().isEmpty()) {
                for (String cmd : newLevelData.getRewards()) {
                    String parsedCmd = cmd.replace("%player%", player.getName())
                            .replace("%veteran_level%", String.valueOf(newLevel));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
                }
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("level", String.valueOf(newLevel));
            placeholders.put("level_name", newLevelData != null ? newLevelData.getName() : "???");
            placeholders.put("description", newLevelData != null ? newLevelData.getDescription() : "");
            plugin.getMessageManager().sendMessage(player, "veteran.levelup", placeholders);
        }

        savePlayerData();

        if (plugin.getConfig().getBoolean("veteran-exp.show-messages", true)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("exp", String.valueOf(amount));
            placeholders.put("source", source);
            plugin.getMessageManager().sendMessage(player, "veteran.exp-gain", placeholders);
        }
    }

    public void setLevel(Player player, int level) {
        PlayerData pd = getPlayerData(player);
        LevelData target = getLevelData(level);
        if (target != null) {
            pd.setLevel(level);
            pd.setExperience(target.getRequiredExp());
            savePlayerData();
        }
    }

    public void setExperience(Player player, long exp) {
        PlayerData pd = getPlayerData(player);
        pd.setExperience(exp);

        while (true) {
            LevelData next = getNextLevelData(player);
            if (next == null) break;
            if (pd.getExperience() >= next.getRequiredExp()) {
                pd.setLevel(next.getLevel());
            } else {
                break;
            }
        }
        savePlayerData();
    }

    public VeteranLevelData getCurrentVeteranLevelData(Player player) {
        int lvl = getPlayerData(player).getVeteranLevel();
        return veteranLevels.get(lvl);
    }

    public VeteranLevelData getNextVeteranLevelData(Player player) {
        int current = getPlayerData(player).getVeteranLevel();
        for (int next : veteranLevels.keySet()) {
            if (next > current) return veteranLevels.get(next);
        }
        return null;
    }

    public long getVeteranExpToNextLevel(Player player) {
        VeteranLevelData next = getNextVeteranLevelData(player);
        if (next == null) return -1;
        long currentExp = getPlayerData(player).getVeteranExperience();
        return next.getRequiredExp() - currentExp;
    }

    public double getVeteranProgress(Player player) {
        PlayerData pd = getPlayerData(player);
        VeteranLevelData current = getCurrentVeteranLevelData(player);
        if (current == null) return 100;

        long required = current.getRequiredExp();
        VeteranLevelData next = getNextVeteranLevelData(player);
        long nextRequired = (next != null) ? next.getRequiredExp() : required;
        long exp = pd.getVeteranExperience();

        if (exp >= nextRequired) return 100;
        if (exp <= required) return 0;
        return (double) (exp - required) / (nextRequired - required) * 100;
    }

    public void setVeteranLevel(Player player, int level) {
        PlayerData pd = getPlayerData(player);
        VeteranLevelData target = veteranLevels.get(level);
        if (target != null) {
            pd.setVeteranLevel(level);
            pd.setVeteranExperience(target.getRequiredExp());
            savePlayerData();
        }
    }

    public void setVeteranExperience(Player player, long exp) {
        PlayerData pd = getPlayerData(player);
        pd.setVeteranExperience(exp);

        while (true) {
            VeteranLevelData next = getNextVeteranLevelData(player);
            if (next == null) break;
            if (pd.getVeteranExperience() >= next.getRequiredExp()) {
                pd.setVeteranLevel(next.getLevel());
            } else {
                break;
            }
        }
        savePlayerData();
    }

    public void resetVeteran(Player player) {
        PlayerData pd = getPlayerData(player);
        pd.setVeteranLevel(0);
        pd.setVeteranExperience(0);
        savePlayerData();
    }

    public int getMaxVeteranLevel() {
        return veteranLevels.isEmpty() ? 0 : veteranLevels.lastKey();
    }

    public int getVeteranLevelCount() {
        return veteranLevels.size();
    }

    public Collection<VeteranLevelData> getAllVeteranLevels() {
        return veteranLevels.values();
    }

    public int getLevelCount() {
        return levels.size();
    }

    public int getMaxLevel() {
        return levels.isEmpty() ? 1 : levels.lastKey();
    }

    public Collection<LevelData> getAllLevels() {
        return levels.values();
    }
}