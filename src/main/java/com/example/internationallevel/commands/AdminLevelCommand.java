package com.example.internationallevel.commands;

import com.example.internationallevel.InternationalLevel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdminLevelCommand implements CommandExecutor {

    private final InternationalLevel plugin;

    public AdminLevelCommand(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("internationallevel.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            plugin.getMessageManager().sendMessage(sender, "admin.usage");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Map<String, String> ph = new HashMap<>();
            ph.put("player", args[0]);
            plugin.getMessageManager().sendMessage(sender, "player-not-found", ph);
            return true;
        }

        String action = args[1].toLowerCase();

        try {
            switch (action) {
                case "addxp":
                    if (args.length < 3) {
                        plugin.getMessageManager().sendMessage(sender, "admin.usage");
                        return true;
                    }
                    long exp = Long.parseLong(args[2]);
                    plugin.getLevelManager().addExperience(target, exp, "admin");
                    plugin.getMessageManager().sendMessage(sender, "admin.xp-added", createPlaceholders(target.getName(), String.valueOf(exp)));
                    break;

                case "setlevel":
                    if (args.length < 3) {
                        plugin.getMessageManager().sendMessage(sender, "admin.usage");
                        return true;
                    }
                    int level = Integer.parseInt(args[2]);
                    plugin.getLevelManager().setLevel(target, level);
                    plugin.getMessageManager().sendMessage(sender, "admin.level-set", createPlaceholders(target.getName(), String.valueOf(level)));
                    break;

                case "setexp":
                    if (args.length < 3) {
                        plugin.getMessageManager().sendMessage(sender, "admin.usage");
                        return true;
                    }
                    long newexp = Long.parseLong(args[2]);
                    plugin.getLevelManager().setExperience(target, newexp);
                    plugin.getMessageManager().sendMessage(sender, "admin.exp-set", createPlaceholders(target.getName(), String.valueOf(newexp)));
                    break;

                case "addveteranxp":
                    if (args.length < 3) {
                        plugin.getMessageManager().sendMessage(sender, "admin.veteran-usage");
                        return true;
                    }
                    long veteranExp = Long.parseLong(args[2]);
                    plugin.getLevelManager().addVeteranExperience(target, veteranExp, "admin");
                    plugin.getMessageManager().sendMessage(sender, "admin.veteran-xp-added", createPlaceholders(target.getName(), String.valueOf(veteranExp)));
                    break;

                case "setveteranlevel":
                    if (args.length < 3) {
                        plugin.getMessageManager().sendMessage(sender, "admin.veteran-usage");
                        return true;
                    }
                    int veteranLevel = Integer.parseInt(args[2]);
                    plugin.getLevelManager().setVeteranLevel(target, veteranLevel);
                    plugin.getMessageManager().sendMessage(sender, "admin.veteran-level-set", createPlaceholders(target.getName(), String.valueOf(veteranLevel)));
                    break;

                case "setveteranexp":
                    if (args.length < 3) {
                        plugin.getMessageManager().sendMessage(sender, "admin.veteran-usage");
                        return true;
                    }
                    long newVeteranExp = Long.parseLong(args[2]);
                    plugin.getLevelManager().setVeteranExperience(target, newVeteranExp);
                    plugin.getMessageManager().sendMessage(sender, "admin.veteran-exp-set", createPlaceholders(target.getName(), String.valueOf(newVeteranExp)));
                    break;

                case "resetveteran":
                    plugin.getLevelManager().resetVeteran(target);
                    Map<String, String> resetPh = new HashMap<>();
                    resetPh.put("player", target.getName());
                    plugin.getMessageManager().sendMessage(sender, "admin.veteran-reset", resetPh);
                    break;

                case "reload":
                    plugin.reloadPlugin();
                    plugin.getMessageManager().sendMessage(sender, "admin.reload-success");
                    break;

                default:
                    plugin.getMessageManager().sendMessage(sender, "admin.usage");
                    break;
            }
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(sender, "admin.invalid-number");
        }
        return true;
    }

    private Map<String, String> createPlaceholders(String player, String value) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", player);
        ph.put("value", value);
        return ph;
    }
}