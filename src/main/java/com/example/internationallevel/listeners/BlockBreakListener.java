package com.example.internationallevel.listeners;

import com.example.internationallevel.InternationalLevel;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final InternationalLevel plugin;

    public BlockBreakListener(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        FileConfiguration config = plugin.getConfig();

        // Начисление обычного опыта
        String blockPath = "exp.blocks." + blockType.name();
        if (config.contains(blockPath + ".min") || config.contains(blockPath + ".max")) {
            double min = config.getDouble(blockPath + ".min", 0);
            double max = config.getDouble(blockPath + ".max", 0);
            double exp = (max == min) ? min : min + Math.random() * (max - min);

            // Обычный опыт со своим множителем
            double multiplier = getExperienceMultiplier(player, "exp-multipliers");
            long totalExp = Math.round(exp * multiplier);
            if (totalExp > 0) {
                plugin.getLevelManager().addExperience(player, totalExp, "блоки");
            }

            // Опыт ветерана - те же значения, но со своим множителем
            if (config.getBoolean("veteran-exp.enabled", true)) {
                double veteranMultiplier = getExperienceMultiplier(player, "veteran-multipliers");
                long totalVeteranExp = Math.round(exp * veteranMultiplier);
                if (totalVeteranExp > 0) {
                    plugin.getLevelManager().addVeteranExperience(player, totalVeteranExp, "блоки");
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