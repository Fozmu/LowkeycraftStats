package net.lowkeycraft.stats.utils;

import net.lowkeycraft.stats.LowkeycraftStats;
import org.bukkit.ChatColor;

public class ConfigManager {

    private final LowkeycraftStats plugin;

    public ConfigManager(LowkeycraftStats plugin) {
        this.plugin = plugin;
    }

    // Database settings
    public String getDatabaseType() {
        return plugin.getConfig().getString("database.type", "sqlite");
    }

    public String getDatabaseFile() {
        return plugin.getConfig().getString("database.file", "stats.db");
    }

    public String getMySQLHost() {
        return plugin.getConfig().getString("database.mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return plugin.getConfig().getInt("database.mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return plugin.getConfig().getString("database.mysql.database", "lowkeycraft_stats");
    }

    public String getMySQLUsername() {
        return plugin.getConfig().getString("database.mysql.username", "stats");
    }

    public String getMySQLPassword() {
        return plugin.getConfig().getString("database.mysql.password", "password123");
    }

    // Web server settings
    public boolean isWebServerEnabled() {
        return plugin.getConfig().getBoolean("web-server.enabled", true);
    }

    public int getWebServerPort() {
        return plugin.getConfig().getInt("web-server.port", 8080);
    }

    public boolean isCorsEnabled() {
        return plugin.getConfig().getBoolean("web-server.cors", true);
    }

    public String getApiKey() {
        return plugin.getConfig().getString("web-server.api-key", "");
    }

    // Tracking settings
    public boolean isInventoryTracked() {
        return plugin.getConfig().getBoolean("tracking.inventory", true);
    }

    public boolean isLocationTracked() {
        return plugin.getConfig().getBoolean("tracking.location", true);
    }

    public boolean isHealthFoodTracked() {
        return plugin.getConfig().getBoolean("tracking.health-food", true);
    }

    public int getUpdateInterval() {
        return plugin.getConfig().getInt("tracking.update-interval", 30);
    }

    // Statistics settings
    public boolean isPlaytimeTracked() {
        return plugin.getConfig().getBoolean("statistics.playtime", true);
    }

    public boolean areBlocksBrokenTracked() {
        return plugin.getConfig().getBoolean("statistics.blocks-broken", true);
    }

    public boolean areBlocksPlacedTracked() {
        return plugin.getConfig().getBoolean("statistics.blocks-placed", true);
    }

    public boolean areDeathsTracked() {
        return plugin.getConfig().getBoolean("statistics.deaths", true);
    }

    public boolean arePlayerKillsTracked() {
        return plugin.getConfig().getBoolean("statistics.player-kills", true);
    }

    public boolean areMobKillsTracked() {
        return plugin.getConfig().getBoolean("statistics.mob-kills", true);
    }

    public boolean isDistanceTraveledTracked() {
        return plugin.getConfig().getBoolean("statistics.distance-traveled", true);
    }

    public boolean areItemsCraftedTracked() {
        return plugin.getConfig().getBoolean("statistics.items-crafted", true);
    }

    public boolean isFoodConsumedTracked() {
        return plugin.getConfig().getBoolean("statistics.food-consumed", true);
    }

    public boolean areAchievementsTracked() {
        return plugin.getConfig().getBoolean("statistics.achievements", false);
    }

    public boolean isEconomyTracked() {
        return plugin.getConfig().getBoolean("statistics.economy", false);
    }

    // Messages
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.prefix", "&a[LowkeycraftStats]&f"));
    }

    public String getNoPermissionMessage() {
        return ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command."));
    }

    public String getPlayerNotFoundMessage() {
        return ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found or has never joined the server."));
    }

    public String getStatsHeaderMessage() {
        return ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.stats-header", "&6=== Stats for {player} ==="));
    }

    public String getReloadSuccessMessage() {
        return ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("messages.reload-success", "&aConfiguration reloaded successfully!"));
    }

    // Debug
    public boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean("debug", false);
    }
}