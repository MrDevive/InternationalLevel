package com.example.internationallevel.listeners;

import com.example.internationallevel.InternationalLevel;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

public class FarmingListener implements Listener {

    private final InternationalLevel plugin;

    public FarmingListener(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHarvestCrops(PlayerHarvestBlockEvent event) {
        // Для сладких ягод и подобного - они собираются правой кнопкой
        Player player = event.getPlayer();
        Material cropType = event.getHarvestedBlock().getType();

        // Проверяем, что культура полностью выросла
        if (isFullyGrown(event.getHarvestedBlock())) {
            giveFarmingExp(player, cropType, "урожай");
        }
    }

    @EventHandler
    public void onBreakCrops(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Список культур, которые растут (Ageable)
        if (isAgeableCrop(blockType)) {
            // Проверяем, что культура полностью выросла
            if (isFullyGrown(block)) {
                giveFarmingExp(player, blockType, "сбор");
            }
            return;
        }

        // Для не-Ageable культур (сахарный тростник, кактус, бамбук, келп)
        if (isTallPlant(blockType)) {
            // Проверяем, что это нижний блок (есть такой же сверху)
            Block above = block.getRelative(0, 1, 0);
            if (above.getType() == blockType) {
                // Даем опыт только за слом нижнего блока
                giveFarmingExp(player, blockType, "сбор");
            }
            return;
        }

        // Для адского нароста (Nether Wart)
        if (blockType == Material.NETHER_WART) {
            if (isFullyGrown(block)) {
                giveFarmingExp(player, blockType, "сбор");
            }
            return;
        }

        // Для какао-бобов
        if (blockType == Material.COCOA) {
            if (isFullyGrown(block)) {
                giveFarmingExp(player, blockType, "сбор");
            }
            return;
        }

        // Для тыквы и арбуза
        if (blockType == Material.PUMPKIN || blockType == Material.MELON) {
            FileConfiguration config = plugin.getConfig();
            String cropPath = "exp.farming.crops." + blockType.name();
            if (config.contains(cropPath + ".min") || config.contains("exp.farming.default.min")) {
                giveFarmingExp(player, blockType, "сбор");
            }
            return;
        }

        // Для грибов
        if (blockType == Material.BROWN_MUSHROOM || blockType == Material.RED_MUSHROOM) {
            FileConfiguration config = plugin.getConfig();
            String cropPath = "exp.farming.crops." + blockType.name();
            if (config.contains(cropPath + ".min") || config.contains("exp.farming.default.min")) {
                giveFarmingExp(player, blockType, "сбор");
            }
        }
    }

    private boolean isAgeableCrop(Material material) {
        return material == Material.WHEAT ||
                material == Material.CARROTS ||
                material == Material.POTATOES ||
                material == Material.BEETROOTS ||
                material == Material.SWEET_BERRY_BUSH;
    }

    private boolean isTallPlant(Material material) {
        return material == Material.SUGAR_CANE ||
                material == Material.BAMBOO ||
                material == Material.KELP_PLANT ||
                material == Material.KELP ||
                material == Material.CACTUS;
    }

    private boolean isFullyGrown(Block block) {
        BlockData data = block.getBlockData();

        if (data instanceof Ageable ageable) {
            return ageable.getAge() == ageable.getMaximumAge();
        }

        Material type = block.getType();

        if (isTallPlant(type)) {
            Block above = block.getRelative(0, 1, 0);
            return above.getType() == type;
        }

        if (type == Material.PUMPKIN || type == Material.MELON) {
            return true;
        }

        return false;
    }

    private void giveFarmingExp(Player player, Material cropType, String action) {
        FileConfiguration config = plugin.getConfig();

        String cropPath = "exp.farming.crops." + cropType.name();
        double min, max;

        if (config.contains(cropPath + ".min") && config.contains(cropPath + ".max")) {
            min = config.getDouble(cropPath + ".min");
            max = config.getDouble(cropPath + ".max");
        } else {
            String defaultPath = "exp.farming.default";
            if (!config.contains(defaultPath + ".min") && !config.contains(defaultPath + ".max")) {
                return;
            }
            min = config.getDouble(defaultPath + ".min", 1);
            max = config.getDouble(defaultPath + ".max", 5);
        }

        if (min <= 0 && max <= 0) return;

        double exp = (max == min) ? min : min + Math.random() * (max - min);

        // Обычный опыт со своим множителем
        double multiplier = getExperienceMultiplier(player, "exp-multipliers");
        long totalExp = Math.round(exp * multiplier);
        if (totalExp > 0) {
            plugin.getLevelManager().addExperience(player, totalExp, "фермерство (" + action + ")");
        }

        // Опыт ветерана - те же значения, но со своим множителем
        if (config.getBoolean("veteran-exp.enabled", true)) {
            double veteranMultiplier = getExperienceMultiplier(player, "veteran-multipliers");
            long totalVeteranExp = Math.round(exp * veteranMultiplier);
            if (totalVeteranExp > 0) {
                plugin.getLevelManager().addVeteranExperience(player, totalVeteranExp, "фермерство");
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