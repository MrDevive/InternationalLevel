package com.example.internationallevel.models;

import java.util.List;

public class Reward {
    private final int level;
    private final List<String> commands;
    private final String message;

    public Reward(int level, List<String> commands, String message) {
        this.level = level;
        this.commands = commands;
        this.message = message;
    }

    public int getLevel() { return level; }
    public List<String> getCommands() { return commands; }
    public String getMessage() { return message; }
}