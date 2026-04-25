package com.example.internationallevel.models;

import org.bukkit.ChatColor;

public class LevelData {
    private final int level;
    private final long requiredExp;
    private final String name;
    private final String suffix;
    private final String league;

    public LevelData(int level, long requiredExp, String name, String suffix, String league,
                     double healthBonus, double damageBonus, double speedBonus, double defenseBonus) {
        this.level = level;
        this.requiredExp = requiredExp;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.suffix = ChatColor.translateAlternateColorCodes('&', suffix);
        this.league = ChatColor.translateAlternateColorCodes('&', league);
    }

    public int getLevel() { return level; }
    public long getRequiredExp() { return requiredExp; }
    public String getName() { return name; }
    public String getSuffix() { return suffix; }
    public String getLeague() { return league; }
}