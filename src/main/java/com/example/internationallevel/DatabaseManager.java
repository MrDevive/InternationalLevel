package com.example.internationallevel;

import com.example.internationallevel.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final InternationalLevel plugin;
    private Connection connection;
    private String host, database, username, password;
    private int port;

    public DatabaseManager(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.host = config.getString("mysql.host", "127.0.1.31");
        this.port = config.getInt("mysql.port", 3306);
        this.database = config.getString("mysql.database", "international_minecraft_auth");
        this.username = config.getString("mysql.username", "root");
        this.password = config.getString("mysql.password", "root");
    }

    /**
     * Синхронное подключение к MySQL
     */
    public boolean connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                    "?useSSL=false&autoReconnect=true&characterEncoding=utf8";
            connection = DriverManager.getConnection(url, username, password);
            createTables();
            plugin.getLogger().info("MySQL подключен успешно!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка подключения к MySQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Асинхронное подключение к MySQL
     */
    public CompletableFuture<Boolean> connectAsync() {
        return CompletableFuture.supplyAsync(this::connect);
    }

    private void createTables() throws SQLException {
        String playersTable = "CREATE TABLE IF NOT EXISTS international_level (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "player_name VARCHAR(32) NOT NULL," +
                "level INT DEFAULT 1," +
                "experience BIGINT DEFAULT 0," +
                "veteran_level INT DEFAULT 0," +
                "veteran_experience BIGINT DEFAULT 0," +
                "total_playtime BIGINT DEFAULT 0," +
                "total_distance DOUBLE DEFAULT 0," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(playersTable);
        }
    }

    /**
     * Загрузка данных игрока из MySQL
     */
    public PlayerData loadPlayerData(UUID uuid, String playerName) {
        String sql = "SELECT * FROM international_level WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PlayerData data = new PlayerData(uuid, playerName);
                data.setLevel(rs.getInt("level"));
                data.setExperience(rs.getLong("experience"));
                data.setVeteranLevel(rs.getInt("veteran_level"));
                data.setVeteranExperience(rs.getLong("veteran_experience"));
                data.setTotalPlaytime(rs.getLong("total_playtime"));
                data.setTotalDistance(rs.getDouble("total_distance"));
                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка загрузки данных игрока " + playerName + ": " + e.getMessage());
        }
        return new PlayerData(uuid, playerName);
    }

    /**
     * Асинхронная загрузка данных игрока
     */
    public CompletableFuture<PlayerData> loadPlayerDataAsync(UUID uuid, String playerName) {
        return CompletableFuture.supplyAsync(() -> loadPlayerData(uuid, playerName));
    }

    /**
     * Сохранение данных игрока в MySQL
     */
    public void savePlayerData(PlayerData data) {
        String sql = "INSERT INTO international_level (uuid, player_name, level, experience, " +
                "veteran_level, veteran_experience, total_playtime, total_distance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "player_name = ?, level = ?, experience = ?, veteran_level = ?, " +
                "veteran_experience = ?, total_playtime = ?, total_distance = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getPlayerName());
            stmt.setInt(3, data.getLevel());
            stmt.setLong(4, data.getExperience());
            stmt.setInt(5, data.getVeteranLevel());
            stmt.setLong(6, data.getVeteranExperience());
            stmt.setLong(7, data.getTotalPlaytime());
            stmt.setDouble(8, data.getTotalDistance());

            stmt.setString(9, data.getPlayerName());
            stmt.setInt(10, data.getLevel());
            stmt.setLong(11, data.getExperience());
            stmt.setInt(12, data.getVeteranLevel());
            stmt.setLong(13, data.getVeteranExperience());
            stmt.setLong(14, data.getTotalPlaytime());
            stmt.setDouble(15, data.getTotalDistance());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения данных игрока " + data.getPlayerName() + ": " + e.getMessage());
        }
    }

    /**
     * Асинхронное сохранение данных игрока
     */
    public CompletableFuture<Void> savePlayerDataAsync(PlayerData data) {
        return CompletableFuture.runAsync(() -> savePlayerData(data));
    }

    /**
     * Миграция данных из YAML в MySQL
     */
    public int migrateFromYamlToMySQL() {
        int count = 0;
        try {
            connection.setAutoCommit(false);

            // Очищаем таблицу перед миграцией
            String deleteSql = "DELETE FROM international_level";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(deleteSql);
            }

            String insertSql = "INSERT INTO international_level (uuid, player_name, level, experience, " +
                    "veteran_level, veteran_experience, total_playtime, total_distance) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                for (PlayerData data : plugin.getLevelManager().getAllPlayerData()) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setString(2, data.getPlayerName());
                    stmt.setInt(3, data.getLevel());
                    stmt.setLong(4, data.getExperience());
                    stmt.setInt(5, data.getVeteranLevel());
                    stmt.setLong(6, data.getVeteranExperience());
                    stmt.setLong(7, data.getTotalPlaytime());
                    stmt.setDouble(8, data.getTotalDistance());
                    stmt.addBatch();
                    count++;
                }
                stmt.executeBatch();
                connection.commit();
                plugin.getLogger().info("Миграция завершена! Перенесено " + count + " игроков в MySQL");
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            plugin.getLogger().severe("Ошибка миграции: " + e.getMessage());
            return -1;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    /**
     * Асинхронная миграция данных
     */
    public CompletableFuture<Integer> migrateFromYamlToMySQLAsync() {
        return CompletableFuture.supplyAsync(this::migrateFromYamlToMySQL);
    }

    /**
     * Проверка подключения к MySQL
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Отключение от MySQL
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("MySQL отключен");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}