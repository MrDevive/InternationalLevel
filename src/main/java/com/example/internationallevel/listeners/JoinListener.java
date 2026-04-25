package com.example.internationallevel.listeners;

import com.example.internationallevel.InternationalLevel;
import com.example.internationallevel.models.PlayerData;
import com.example.internationallevel.models.VeteranLevelData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

public class JoinListener implements Listener {

    private final InternationalLevel plugin;

    public JoinListener(InternationalLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getLevelManager().getPlayerData(player);
        data.setPlayerName(player.getName()); // обновляем имя на случай смены

        VeteranLevelData veteranData = plugin.getLevelManager().getCurrentVeteranLevelData(player);

        plugin.getLevelManager().savePlayerData();

        // Приветственное сообщение
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("level", String.valueOf(data.getLevel()));
        placeholders.put("level_name", plugin.getLevelManager().getCurrentLevelData(player).getName());
        placeholders.put("veteran_level", String.valueOf(data.getVeteranLevel()));
        placeholders.put("veteran_name", veteranData != null ? veteranData.getName() : "Нет ранга");

        plugin.getMessageManager().sendMessage(player, "join-message", placeholders);
    }
}