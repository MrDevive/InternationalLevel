package com.example.internationallevel.listeners;

import com.example.internationallevel.InternationalLevel;
import com.example.internationallevel.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaytimeListener implements Listener {

    private final InternationalLevel plugin;
    private final Map<UUID, Long> joinTimes;

    public PlaytimeListener(InternationalLevel plugin) {
        this.plugin = plugin;
        this.joinTimes = new ConcurrentHashMap<>();
        startPlaytimeTask();
    }

    private void startPlaytimeTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            FileConfiguration config = plugin.getConfig();

            long minExp = config.getLong("exp.playtime.min", 1);
            long maxExp = config.getLong("exp.playtime.max", 5);
            boolean veteranEnabled = config.getBoolean("veteran-exp.enabled", true);

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                long exp = minExp + (long)(Math.random() * (maxExp - minExp + 1));

                // Обычный опыт
                double multiplier = getExperienceMultiplier(player, "exp-multipliers");
                long totalExp = Math.round(exp * multiplier);
                if (totalExp > 0) {
                    plugin.getLevelManager().addExperience(player, totalExp, "время в игре");
                }

                // Опыт ветерана - те же значения, но со своим множителем
                if (veteranEnabled) {
                    double veteranMultiplier = getExperienceMultiplier(player, "veteran-multipliers");
                    long totalVeteranExp = Math.round(exp * veteranMultiplier);
                    if (totalVeteranExp > 0) {
                        plugin.getLevelManager().addVeteranExperience(player, totalVeteranExp, "время в игре");
                    }
                }

                PlayerData data = plugin.getLevelManager().getPlayerData(player);
                data.addPlaytime(60);
            }
            plugin.getLevelManager().savePlayerData();
        }, 20L * 60, 20L * 60);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Long joinTime = joinTimes.remove(uuid);

        if (joinTime != null) {
            long sessionTime = (System.currentTimeMillis() - joinTime) / 1000;
            PlayerData data = plugin.getLevelManager().getPlayerData(player);
            data.addPlaytime(sessionTime);
            plugin.getLevelManager().savePlayerData();
        }
    }

    private double getExperienceMultiplier(Player player, String configPath) {
        double multiplier = 1.0;
        FileConfiguration config = plugin.getConfig();

        String[] perms = {
                "internationallevel.multiplier.elite",
                "internationallevel.multiplier.premium",
                "internationallevel.multiplier.vip"
        };

        String[] paths = {
                configPath + ".internationallevel.multiplier.elite",
                configPath + ".internationallevel.multiplier.premium",
                configPath + ".internationallevel.multiplier.vip"
        };

        for (int i = 0; i < perms.length; i++) {
            if (player.hasPermission(perms[i])) {
                double value = config.getDouble(paths[i], 1.0);
                if (value > multiplier) {
                    multiplier = value;
                }
            }
        }

        return multiplier;
    }
}