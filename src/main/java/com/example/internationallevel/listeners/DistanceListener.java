package com.example.internationallevel.listeners;

import com.example.internationallevel.InternationalLevel;
import com.example.internationallevel.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DistanceListener implements Listener {

    private final InternationalLevel plugin;
    private final Map<UUID, Location> lastLocations;
    private final Map<UUID, Double> tempDistance;
    private final Map<UUID, Double> tempVeteranDistance;

    public DistanceListener(InternationalLevel plugin) {
        this.plugin = plugin;
        this.lastLocations = new ConcurrentHashMap<>();
        this.tempDistance = new ConcurrentHashMap<>();
        this.tempVeteranDistance = new ConcurrentHashMap<>();
        startDistanceTask();
    }

    private void startDistanceTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            FileConfiguration config = plugin.getConfig();

            double intervalDistance = config.getDouble("exp.distance.interval-blocks", 100);
            long minExp = config.getLong("exp.distance.min", 5);
            long maxExp = config.getLong("exp.distance.max", 15);

            boolean veteranEnabled = config.getBoolean("veteran-exp.enabled", true);

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();

                // Начисление обычного опыта за расстояние
                double totalDistance = tempDistance.getOrDefault(uuid, 0.0);
                long intervals = (long)(totalDistance / intervalDistance);
                if (intervals > 0) {
                    long expPerInterval = minExp + (long)(Math.random() * (maxExp - minExp + 1));
                    long totalExp = expPerInterval * intervals;
                    if (totalExp > 0) {
                        double multiplier = getExperienceMultiplier(player, "exp-multipliers");
                        long finalExp = Math.round(totalExp * multiplier);
                        plugin.getLevelManager().addExperience(player, finalExp, "пройденное расстояние");
                    }
                    tempDistance.put(uuid, totalDistance - (intervals * intervalDistance));

                    // Опыт ветерана - те же значения, но со своим множителем
                    if (veteranEnabled) {
                        double veteranMultiplier = getExperienceMultiplier(player, "veteran-multipliers");
                        long finalVeteranExp = Math.round(totalExp * veteranMultiplier);
                        if (finalVeteranExp > 0) {
                            plugin.getLevelManager().addVeteranExperience(player, finalVeteranExp, "пройденное расстояние");
                        }
                    }
                }
            }
        }, 20L * 10, 20L * 10);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Игнорируем перемещение по вертикали или если блок не изменился
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        Location lastLoc = lastLocations.get(uuid);
        Location currentLoc = event.getTo();

        if (lastLoc != null) {
            // Считаем только горизонтальное расстояние
            double distance = Math.sqrt(Math.pow(currentLoc.getX() - lastLoc.getX(), 2) +
                    Math.pow(currentLoc.getZ() - lastLoc.getZ(), 2));

            // Ограничиваем максимальную дистанцию за один тик (телепорты)
            if (distance < 50 && distance > 0) {
                // Обновляем PlayerData
                PlayerData data = plugin.getLevelManager().getPlayerData(player);
                data.addDistance(distance);

                // Обновляем временные счетчики для выдачи опыта
                double currentTemp = tempDistance.getOrDefault(uuid, 0.0);
                tempDistance.put(uuid, currentTemp + distance);

                double currentVeteranTemp = tempVeteranDistance.getOrDefault(uuid, 0.0);
                tempVeteranDistance.put(uuid, currentVeteranTemp + distance);
            }
        }

        lastLocations.put(uuid, currentLoc.clone());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastLocations.remove(uuid);
        tempDistance.remove(uuid);
        tempVeteranDistance.remove(uuid);
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