package com.example.internationallevel;

import com.example.internationallevel.commands.AdminLevelCommand;
import com.example.internationallevel.commands.LevelCommand;
import com.example.internationallevel.commands.VeteranCommand;
import com.example.internationallevel.hooks.PlaceholderAPIHook;
import com.example.internationallevel.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class InternationalLevel extends JavaPlugin {

    private static InternationalLevel instance;
    private LevelManager levelManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Создаем правильный конфиг только если его нет
        createCorrectConfigIfNotExists();

        // Сохраняем остальные конфиги по умолчанию
        saveDefaultConfig();
        saveResource("level_settings.yml", false);
        saveResource("level_rewards.yml", false);
        saveResource("messages.yml", false);
        saveResource("veteran_levels.yml", false);

        // Инициализация менеджера сообщений
        messageManager = new MessageManager(this);
        messageManager.loadMessages();

        // Инициализация MySQL
        databaseManager = new DatabaseManager(this);
        databaseManager.loadConfig();

        boolean useMySQL = getConfig().getBoolean("mysql.enabled", true);

        if (!useMySQL) {
            getLogger().severe("MySQL должен быть включен! Плагин выключается...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Подключение к MySQL
        boolean connected = databaseManager.connect();
        if (!connected) {
            getLogger().severe("Не удалось подключиться к MySQL! Плагин выключается...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        levelManager = new LevelManager(this, true);
        getLogger().info("Используется MySQL для хранения данных");

        // Загрузка всех данных
        levelManager.loadLevels();
        levelManager.loadRewards();
        levelManager.loadVeteranLevels();
        levelManager.loadPlayerData();

        // Регистрация команд
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("leveladmin").setExecutor(new AdminLevelCommand(this));
        getCommand("veteran").setExecutor(new VeteranCommand(this));

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new FishingListener(this), this);
        getServer().getPluginManager().registerEvents(new DistanceListener(this), this);
        getServer().getPluginManager().registerEvents(new FarmingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlaytimeListener(this), this);

        // Подключение PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI hook успешно зарегистрирован!");
        } else {
            getLogger().warning("PlaceholderAPI не найден! Плейсхолдеры не будут работать.");
        }

        getLogger().info("=========================================");
        getLogger().info("InternationalLevel успешно загружен!");
        getLogger().info("Уровней: " + levelManager.getLevelCount());
        getLogger().info("=========================================");
    }

    private void createCorrectConfigIfNotExists() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            return;
        }

        getLogger().info("Создание нового config.yml...");

        // Основные настройки
        getConfig().set("show-exp-messages", true);
        getConfig().set("show-join-message", true);

        // MySQL настройки
        getConfig().set("mysql.enabled", true);
        getConfig().set("mysql.host", "127.0.0.1");
        getConfig().set("mysql.port", 3306);
        getConfig().set("mysql.database", "international_minecraft_auth");
        getConfig().set("mysql.username", "root");
        getConfig().set("mysql.password", "root");

        // Настройка опыта за мобов
        getConfig().set("exp.mobs.ZOMBIE.min", 5);
        getConfig().set("exp.mobs.ZOMBIE.max", 10);
        getConfig().set("exp.mobs.SKELETON.min", 6);
        getConfig().set("exp.mobs.SKELETON.max", 12);
        getConfig().set("exp.mobs.CREEPER.min", 8);
        getConfig().set("exp.mobs.CREEPER.max", 15);

        // Настройка опыта за блоки
        getConfig().set("exp.blocks.STONE.min", 1);
        getConfig().set("exp.blocks.STONE.max", 1);
        getConfig().set("exp.blocks.COBBLESTONE.min", 1);
        getConfig().set("exp.blocks.COBBLESTONE.max", 1);
        getConfig().set("exp.blocks.DIAMOND_ORE.min", 20);
        getConfig().set("exp.blocks.DIAMOND_ORE.max", 30);

        // Множители опыта
        getConfig().set("exp-multipliers.internationallevel.multiplier.vip", 1.5);
        getConfig().set("exp-multipliers.internationallevel.multiplier.premium", 2.0);
        getConfig().set("exp-multipliers.internationallevel.multiplier.elite", 3.0);

        // Базовое здоровье
        getConfig().set("stats.base-health", 20.0);

        saveConfig();
        getLogger().info("config.yml успешно создан!");
    }

    @Override
    public void onDisable() {
        if (levelManager != null) {
            levelManager.savePlayerData();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("InternationalLevel выгружен!");
    }

    public static InternationalLevel getInstance() {
        return instance;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void reloadPlugin() {
        // Перезагружаем config.yml
        reloadConfig();

        // Перезагружаем сообщения
        messageManager.loadMessages();

        // Перезагружаем MySQL настройки
        databaseManager.loadConfig();

        // Перезагружаем уровни и награды
        levelManager.loadLevels();
        levelManager.loadRewards();
        levelManager.loadVeteranLevels();

        // Перезагружаем данные игроков
        levelManager.loadPlayerData();

        getLogger().info("Плагин успешно перезагружен!");
    }
}