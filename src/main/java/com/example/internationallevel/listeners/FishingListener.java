package com.example.internationallevel.listeners;

import com.example.internationallevel.InternationalLevel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishingListener implements Listener {

    private final InternationalLevel plugin;

    public FishingListener(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        boolean isTreasure = false;

        if (event.getCaught() != null) {
            String caughtType = event.getCaught().getType().name();
            if (caughtType.contains("CHEST") || caughtType.contains("ENCHANTED") ||
                    config.getStringList("exp.fishing.treasure-items").contains(caughtType)) {
                isTreasure = true;
            }
        }

        String fishType = isTreasure ? "treasure" : "fish";

        // Начисление обычного опыта
        String fishPath = "exp.fishing." + fishType;
        if (config.contains(fishPath + ".min") || config.contains(fishPath + ".max")) {
            double min = config.getDouble(fishPath + ".min", 0);
            double max = config.getDouble(fishPath + ".max", 0);
            double exp = (max == min) ? min : min + Math.random() * (max - min);

            // Обычный опыт со своим множителем
            double multiplier = getExperienceMultiplier(player, "exp-multipliers");
            long totalExp = Math.round(exp * multiplier);
            if (totalExp > 0) {
                plugin.getLevelManager().addExperience(player, totalExp, isTreasure ? "сокровище" : "рыбалка");
            }

            // Опыт ветерана - те же значения, но со своим множителем
            if (config.getBoolean("veteran-exp.enabled", true)) {
                double veteranMultiplier = getExperienceMultiplier(player, "veteran-multipliers");
                long totalVeteranExp = Math.round(exp * veteranMultiplier);
                if (totalVeteranExp > 0) {
                    plugin.getLevelManager().addVeteranExperience(player, totalVeteranExp, isTreasure ? "сокровище" : "рыбалка");
                }
            }
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