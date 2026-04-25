package com.example.internationallevel.commands;

import com.example.internationallevel.InternationalLevel;
import com.example.internationallevel.models.PlayerData;
import com.example.internationallevel.models.VeteranLevelData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class VeteranCommand implements CommandExecutor {

    private final InternationalLevel plugin;

    public VeteranCommand(InternationalLevel plugin) {
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
        VeteranLevelData current = plugin.getLevelManager().getCurrentVeteranLevelData(player);
        VeteranLevelData next = plugin.getLevelManager().getNextVeteranLevelData(player);
        long expToNext = plugin.getLevelManager().getVeteranExpToNextLevel(player);
        double progress = plugin.getLevelManager().getVeteranProgress(player);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("level", String.valueOf(data.getVeteranLevel()));
        placeholders.put("level_name", current != null ? current.getName() : "Нет уровня");
        placeholders.put("description", current != null ? current.getDescription() : "");
        placeholders.put("exp", String.valueOf(data.getVeteranExperience()));
        placeholders.put("exp_next", next != null ? String.valueOf(next.getRequiredExp()) : "MAX");
        placeholders.put("exp_to_next", expToNext == -1 ? "0" : String.valueOf(expToNext));
        placeholders.put("progress", String.format("%.1f", progress));
        placeholders.put("max_level", String.valueOf(plugin.getLevelManager().getMaxVeteranLevel()));

        plugin.getMessageManager().sendMessage(player, "veteran.info", placeholders);
        return true;
    }
}