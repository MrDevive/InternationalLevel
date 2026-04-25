package com.example.internationallevel.commands;

import com.example.internationallevel.InternationalLevel;
import com.example.internationallevel.models.LevelData;
import com.example.internationallevel.models.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class LevelCommand implements CommandExecutor {

    private final InternationalLevel plugin;

    public LevelCommand(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "only-player");
            return true;
        }

        Player player = (Player) sender;
        PlayerData data = plugin.getLevelManager().getPlayerData(player);
        LevelData current = plugin.getLevelManager().getCurrentLevelData(player);
        LevelData next = plugin.getLevelManager().getNextLevelData(player);
        long expToNext = plugin.getLevelManager().getExpToNextLevel(player);
        double progress = plugin.getLevelManager().getProgress(player);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("level", String.valueOf(data.getLevel()));
        placeholders.put("level_name", current != null ? current.getName() : "???");
        placeholders.put("level_suffix", current != null ? current.getSuffix() : "");
        placeholders.put("level_league", current != null ? current.getLeague() : "");
        placeholders.put("exp", String.valueOf(data.getExperience()));
        placeholders.put("exp_required", String.valueOf(current != null ? current.getRequiredExp() : 0));
        placeholders.put("exp_next", next != null ? String.valueOf(next.getRequiredExp()) : "MAX");
        placeholders.put("exp_to_next", expToNext == -1 ? "0" : String.valueOf(expToNext));
        placeholders.put("progress", String.format("%.1f", progress));

        plugin.getMessageManager().sendMessage(player, "level.info", placeholders);
        return true;
    }
}