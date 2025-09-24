package net.lowkeycraft.stats.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.lowkeycraft.stats.LowkeycraftStats;
import spark.Request;
import spark.Response;

import java.text.SimpleDateFormat;
import java.util.*;

import static spark.Spark.*;

public class WebServer {

    private final LowkeycraftStats plugin;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;

    public WebServer(LowkeycraftStats plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm");
    }

    public void start() {
        int port = plugin.getConfigManager().getWebServerPort();
        port(port);

        // Enable CORS if configured
        if (plugin.getConfigManager().isCorsEnabled()) {
            enableCors();
        }

        // Routes
        setupRoutes();

        plugin.getLogger().info("Web API server started on port " + port);
    }

    public void stop() {
        spark.Spark.stop();
        plugin.getLogger().info("Web API server stopped");
    }

    private void enableCors() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        });
    }

    private void setupRoutes() {
        // Health check
        get("/api/health", this::healthCheck);

        // Server stats
        get("/api/server/stats", this::getServerStats);

        // Player endpoints
        get("/api/player/:username", this::getPlayerStats);
        get("/api/players/online", this::getOnlinePlayers);
        get("/api/players/all", this::getAllPlayers);

        // Leaderboards (future implementation)
        get("/api/leaderboard/:stat", this::getLeaderboard);

        // Error handling
        exception(Exception.class, this::handleException);
    }

    private String healthCheck(Request req, Response res) {
        res.type("application/json");

        JsonObject response = new JsonObject();
        response.addProperty("status", "ok");
        response.addProperty("plugin", "LowkeycraftStats");
        response.addProperty("version", "1.0.0");
        response.addProperty("timestamp", System.currentTimeMillis());

        return gson.toJson(response);
    }

    private String getServerStats(Request req, Response res) {
        res.type("application/json");

        try {
            JsonObject response = new JsonObject();
            response.addProperty("success", true);

            JsonObject data = new JsonObject();
            data.addProperty("totalPlayers", plugin.getDatabaseManager().getTotalPlayers());
            data.addProperty("onlinePlayers", plugin.getDatabaseManager().getOnlinePlayerCount());
            data.addProperty("serverName", plugin.getServer().getServerName());
            data.addProperty("maxPlayers", plugin.getServer().getMaxPlayers());
            data.addProperty("timestamp", System.currentTimeMillis());

            response.add("data", data);
            return gson.toJson(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to get server stats: " + e.getMessage());
        }
    }

    private String getPlayerStats(Request req, Response res) {
        res.type("application/json");
        String username = req.params(":username");

        try {
            Map<String, Object> playerData = plugin.getDatabaseManager().getPlayerData(username);

            if (playerData == null) {
                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("playerFound", false);
                response.addProperty("message", "Player not found");
                return gson.toJson(response);
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("playerFound", true);

            // Convert player data to JSON-friendly format
            JsonObject data = convertPlayerDataToJson(playerData);
            response.add("data", data);

            return gson.toJson(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to get player stats: " + e.getMessage());
        }
    }

    private String getOnlinePlayers(Request req, Response res) {
        res.type("application/json");

        try {
            List<Map<String, Object>> onlinePlayers = plugin.getDatabaseManager().getOnlinePlayers();

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("data", gson.toJsonTree(onlinePlayers));

            return gson.toJson(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to get online players: " + e.getMessage());
        }
    }

    private String getAllPlayers(Request req, Response res) {
        res.type("application/json");

        try {
            // This would be a more complex query - for now, return basic info
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("totalPlayers", plugin.getDatabaseManager().getTotalPlayers());
            response.addProperty("message", "Full player list endpoint not yet implemented");

            return gson.toJson(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to get all players: " + e.getMessage());
        }
    }

    private String getLeaderboard(Request req, Response res) {
        res.type("application/json");
        String stat = req.params(":stat");

        JsonObject response = new JsonObject();
        response.addProperty("success", false);
        response.addProperty("message", "Leaderboard endpoint not yet implemented for stat: " + stat);

        return gson.toJson(response);
    }

    private JsonObject convertPlayerDataToJson(Map<String, Object> playerData) {
        JsonObject json = new JsonObject();

        // Basic info
        json.addProperty("username", (String) playerData.get("username"));
        json.addProperty("uuid", (String) playerData.get("uuid"));
        json.addProperty("isOnline", (Boolean) playerData.get("is_online"));

        // Timestamps
        long firstJoin = (Long) playerData.get("first_join");
        long lastSeen = (Long) playerData.get("last_seen");
        json.addProperty("firstJoinTimestamp", firstJoin);
        json.addProperty("lastSeenTimestamp", lastSeen);
        json.addProperty("firstJoin", dateFormat.format(new Date(firstJoin)));
        json.addProperty("lastSeen", (Boolean) playerData.get("is_online") ? "Now" :
            dateFormat.format(new Date(lastSeen)));

        // Playtime formatting
        long playtimeMs = (Long) playerData.get("playtime");
        json.addProperty("playtimeMs", playtimeMs);
        json.addProperty("playtime", formatPlaytime(playtimeMs));

        // Statistics
        json.addProperty("blocksBreaken", (Integer) playerData.get("blocks_broken")); // Keep original typo for compatibility
        json.addProperty("blocksPlaced", (Integer) playerData.get("blocks_placed"));
        json.addProperty("deaths", (Integer) playerData.get("deaths"));
        json.addProperty("playerKills", (Integer) playerData.get("player_kills"));
        json.addProperty("mobKills", (Integer) playerData.get("mob_kills"));
        json.addProperty("distanceTraveled", (Double) playerData.get("distance_traveled"));
        json.addProperty("itemsCrafted", (Integer) playerData.get("items_crafted"));
        json.addProperty("foodConsumed", (Integer) playerData.get("food_consumed"));

        // Live data (only if online)
        if ((Boolean) playerData.get("is_online")) {
            JsonObject liveData = new JsonObject();
            liveData.addProperty("health", (Double) playerData.get("health"));
            liveData.addProperty("foodLevel", (Integer) playerData.get("food_level"));
            liveData.addProperty("saturation", (Double) playerData.get("saturation"));
            liveData.addProperty("experienceLevel", (Integer) playerData.get("experience_level"));
            liveData.addProperty("experiencePoints", (Float) playerData.get("experience_points"));

            JsonObject location = new JsonObject();
            location.addProperty("x", (Double) playerData.get("location_x"));
            location.addProperty("y", (Double) playerData.get("location_y"));
            location.addProperty("z", (Double) playerData.get("location_z"));
            location.addProperty("world", (String) playerData.get("world"));
            liveData.add("location", location);

            json.add("liveData", liveData);
        }

        return json;
    }

    private String formatPlaytime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return "< 1m";
        }
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("success", false);
        response.addProperty("error", message);
        response.addProperty("timestamp", System.currentTimeMillis());
        return gson.toJson(response);
    }

    private void handleException(Exception e, Request req, Response res) {
        plugin.getLogger().severe("API Error: " + e.getMessage());
        e.printStackTrace();

        res.status(500);
        res.type("application/json");
        res.body(createErrorResponse("Internal server error"));
    }
}