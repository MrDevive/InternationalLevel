package com.example.internationallevel.models;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String playerName;
    private int level;
    private long experience;
    private long totalPlaytime; // в секундах
    private double totalDistance; // в блоках

    // Поля для системы ветерана
    private int veteranLevel;
    private long veteranExperience;

    public PlayerData(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.level = 1;
        this.experience = 0;
        this.totalPlaytime = 0;
        this.totalDistance = 0;
        this.veteranLevel = 0;
        this.veteranExperience = 0;
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getExperience() { return experience; }
    public void setExperience(long experience) { this.experience = experience; }
    public void addExperience(long amount) { this.experience += amount; }

    public long getTotalPlaytime() { return totalPlaytime; }
    public void setTotalPlaytime(long totalPlaytime) { this.totalPlaytime = totalPlaytime; }
    public void addPlaytime(long amount) { this.totalPlaytime += amount; }

    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }
    public void addDistance(double amount) { this.totalDistance += amount; }

    // Геттеры и сеттеры для системы ветерана
    public int getVeteranLevel() { return veteranLevel; }
    public void setVeteranLevel(int veteranLevel) { this.veteranLevel = veteranLevel; }
    public long getVeteranExperience() { return veteranExperience; }
    public void setVeteranExperience(long veteranExperience) { this.veteranExperience = veteranExperience; }
    public void addVeteranExperience(long amount) { this.veteranExperience += amount; }
}