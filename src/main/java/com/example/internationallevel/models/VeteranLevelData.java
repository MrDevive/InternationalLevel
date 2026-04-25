package com.example.internationallevel.models;

import org.bukkit.ChatColor;
import java.util.List;

public class VeteranLevelData {
    private final int level;
    private final long requiredExp;
    private final String name;
    private final String description;
    private final List<String> rewards;

    public VeteranLevelData(int level, long requiredExp, String name, String description,
                            List<String> rewards) {
        this.level = level;
        this.requiredExp = requiredExp;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.description = ChatColor.translateAlternateColorCodes('&', description);
        this.rewards = rewards;
    }

    public int getLevel() { return level; }
    public long getRequiredExp() { return requiredExp; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getRewards() { return rewards; }
}