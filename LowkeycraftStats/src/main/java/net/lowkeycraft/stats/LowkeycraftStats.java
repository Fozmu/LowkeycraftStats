package net.lowkeycraft.stats;

import net.lowkeycraft.stats.commands.StatsCommand;
import net.lowkeycraft.stats.database.DatabaseManager;
import net.lowkeycraft.stats.listeners.PlayerListener;
import net.lowkeycraft.stats.api.WebServer;
import net.lowkeycraft.stats.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LowkeycraftStats extends JavaPlugin {

    private static LowkeycraftStats instance;
    private DatabaseManager databaseManager;
    private WebServer webServer;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        getLogger().info("Starting LowkeycraftStats plugin...");

        try {
            // Initialize database
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            getLogger().info("Database initialized successfully!");

            // Register event listeners
            registerListeners();
            getLogger().info("Event listeners registered!");

            // Register commands
            registerCommands();
            getLogger().info("Commands registered!");

            // Start web server if enabled
            if (getConfig().getBoolean("web-server.enabled", true)) {
                webServer = new WebServer(this);
                webServer.start();
                getLogger().info("Web API server started on port " + getConfig().getInt("web-server.port", 8080));
            }

            getLogger().info("LowkeycraftStats plugin enabled successfully!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable LowkeycraftStats: " + e.getMessage());
            e.printStackTrace();
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down LowkeycraftStats plugin...");

        // Stop web server
        if (webServer != null) {
            webServer.stop();
            getLogger().info("Web server stopped");
        }

        // Close database connections
        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("Database connections closed");
        }

        getLogger().info("LowkeycraftStats plugin disabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private void registerCommands() {
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("statsreload").setExecutor(new StatsCommand(this));
    }

    public void reloadPluginConfig() {
        reloadConfig();
        configManager = new ConfigManager(this);

        // Restart web server if needed
        if (webServer != null) {
            webServer.stop();
        }

        if (getConfig().getBoolean("web-server.enabled", true)) {
            webServer = new WebServer(this);
            webServer.start();
        }
    }

    // Getters
    public static LowkeycraftStats getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public WebServer getWebServer() {
        return webServer;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}