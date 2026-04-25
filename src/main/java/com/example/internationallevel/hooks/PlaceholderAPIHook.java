package com.example.internationallevel.hooks;

import com.example.internationallevel.InternationalLevel;
import com.example.internationallevel.models.LevelData;
import com.example.internationallevel.models.PlayerData;
import com.example.internationallevel.models.VeteranLevelData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final InternationalLevel plugin;

    public PlaceholderAPIHook(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "internationallevel";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Devive";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        PlayerData data = plugin.getLevelManager().getPlayerData(player);
        LevelData current = plugin.getLevelManager().getCurrentLevelData(player);
        LevelData next = plugin.getLevelManager().getNextLevelData(player);
        VeteranLevelData currentVeteran = plugin.getLevelManager().getCurrentVeteranLevelData(player);
        VeteranLevelData nextVeteran = plugin.getLevelManager().getNextVeteranLevelData(player);

        switch (identifier) {
            // Обычные уровни
            case "level":
                return String.valueOf(data.getLevel());
            case "level_name":
                return current != null ? current.getName() : "???";
            case "level_suffix":
                return current != null ? current.getSuffix() : "";
            case "level_league":
                return current != null ? current.getLeague() : "";
            case "exp":
                return String.valueOf(data.getExperience());
            case "exp_current":
                return String.valueOf(data.getExperience());
            case "exp_required":
                return String.valueOf(current != null ? current.getRequiredExp() : 0);
            case "exp_next":
                return next != null ? String.valueOf(next.getRequiredExp()) : "MAX";
            case "exp_to_next":
                long toNext = plugin.getLevelManager().getExpToNextLevel(player);
                return toNext == -1 ? "0" : String.valueOf(toNext);
            case "exp_progress":
                double progress = plugin.getLevelManager().getProgress(player);
                return String.format("%.1f", progress);
            case "exp_percent":
                return String.valueOf((int) plugin.getLevelManager().getProgress(player));
            case "max_level":
                return String.valueOf(plugin.getLevelManager().getMaxLevel());
            case "next_level":
                return next != null ? String.valueOf(next.getLevel()) : "MAX";
            case "next_level_name":
                return next != null ? next.getName() : "Максимум";

            // Плейсхолдеры ветерана
            case "veteran_level":
                return String.valueOf(data.getVeteranLevel());
            case "veteran_level_name":
                return currentVeteran != null ? currentVeteran.getName() : "Нет ранга";
            case "veteran_level_description":
                return currentVeteran != null ? currentVeteran.getDescription() : "";
            case "veteran_exp":
                return String.valueOf(data.getVeteranExperience());
            case "veteran_exp_current":
                return String.valueOf(data.getVeteranExperience());
            case "veteran_exp_required":
                return String.valueOf(currentVeteran != null ? currentVeteran.getRequiredExp() : 0);
            case "veteran_exp_next":
                return nextVeteran != null ? String.valueOf(nextVeteran.getRequiredExp()) : "MAX";
            case "veteran_exp_to_next":
                long vetToNext = plugin.getLevelManager().getVeteranExpToNextLevel(player);
                return vetToNext == -1 ? "0" : String.valueOf(vetToNext);
            case "veteran_exp_progress":
                double vetProgress = plugin.getLevelManager().getVeteranProgress(player);
                return String.format("%.1f", vetProgress);
            case "veteran_exp_percent":
                return String.valueOf((int) plugin.getLevelManager().getVeteranProgress(player));
            case "veteran_max_level":
                return String.valueOf(plugin.getLevelManager().getMaxVeteranLevel());
            case "veteran_next_level":
                return nextVeteran != null ? String.valueOf(nextVeteran.getLevel()) : "MAX";
            case "veteran_next_level_name":
                return nextVeteran != null ? nextVeteran.getName() : "Максимум";

            // Статистика
            case "playtime_seconds":
                return String.valueOf(data.getTotalPlaytime());
            case "playtime_minutes":
                return String.valueOf(data.getTotalPlaytime() / 60);
            case "playtime_hours":
                return String.valueOf(data.getTotalPlaytime() / 3600);
            case "distance":
                return String.format("%.1f", data.getTotalDistance());
            case "distance_km":
                return String.format("%.2f", data.getTotalDistance() / 1000);

            // Комбинированный плейсхолдер для чата
            case "full_display":
                String levelDisplay = current != null ? current.getSuffix() : "";
                String veteranDisplay = currentVeteran != null && data.getVeteranLevel() > 0 ?
                        " &8[&e" + currentVeteran.getName() + "&8]" : "";
                return levelDisplay + veteranDisplay;

            default:
                return null;
        }
    }

    @Override
    public boolean persist() {
        return true;
    }
}