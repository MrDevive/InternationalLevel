package com.example.internationallevel.listeners;

import com.example.internationallevel.InternationalLevel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    private final InternationalLevel plugin;

    public EntityDeathListener(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String mobType = event.getEntity().getType().name();
        FileConfiguration config = plugin.getConfig();

        // Начисление обычного опыта
        String mobPath = "exp.mobs." + mobType;
        if (config.contains(mobPath + ".min") || config.contains(mobPath + ".max")) {
            double min = config.getDouble(mobPath + ".min", 0);
            double max = config.getDouble(mobPath + ".max", 0);
            double exp = (max == min) ? min : min + Math.random() * (max - min);

            // Обычный опыт со своим множителем
            double multiplier = getExperienceMultiplier(killer, "exp-multipliers");
            long totalExp = Math.round(exp * multiplier);
            if (totalExp > 0) {
                plugin.getLevelManager().addExperience(killer, totalExp, "мобы");
            }

            // Опыт ветерана - те же значения, но со своим множителем
            if (config.getBoolean("veteran-exp.enabled", true)) {
                double veteranMultiplier = getExperienceMultiplier(killer, "veteran-multipliers");
                long totalVeteranExp = Math.round(exp * veteranMultiplier);
                if (totalVeteranExp > 0) {
                    plugin.getLevelManager().addVeteranExperience(killer, totalVeteranExp, "мобы");
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