package net.lowkeycraft.stats.database;

import net.lowkeycraft.stats.LowkeycraftStats;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final LowkeycraftStats plugin;
    private Connection connection;

    public DatabaseManager(LowkeycraftStats plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {
        setupConnection();
        createTables();
    }

    private void setupConnection() throws SQLException {
        String dbType = plugin.getConfigManager().getDatabaseType();

        if ("sqlite".equalsIgnoreCase(dbType)) {
            setupSQLite();
        } else if ("mysql".equalsIgnoreCase(dbType)) {
            setupMySQL();
        } else {
            throw new SQLException("Unsupported database type: " + dbType);
        }

        plugin.getLogger().info("Connected to " + dbType.toUpperCase() + " database successfully!");
    }

    private void setupSQLite() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getDatabaseFile());
        if (!dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }

        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
    }

    private void setupMySQL() throws SQLException {
        String host = plugin.getConfigManager().getMySQLHost();
        int port = plugin.getConfigManager().getMySQLPort();
        String database = plugin.getConfigManager().getMySQLDatabase();
        String username = plugin.getConfigManager().getMySQLUsername();
        String password = plugin.getConfigManager().getMySQLPassword();

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        // Players table
        String playersTable = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(16) NOT NULL,
                first_join BIGINT NOT NULL,
                last_seen BIGINT NOT NULL,
                playtime BIGINT DEFAULT 0,
                is_online BOOLEAN DEFAULT FALSE
            )
        """;

        // Basic statistics table
        String statsTable = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid VARCHAR(36) PRIMARY KEY,
                blocks_broken INT DEFAULT 0,
                blocks_placed INT DEFAULT 0,
                deaths INT DEFAULT 0,
                player_kills INT DEFAULT 0,
                mob_kills INT DEFAULT 0,
                distance_traveled DOUBLE DEFAULT 0,
                items_crafted INT DEFAULT 0,
                food_consumed INT DEFAULT 0,
                FOREIGN KEY (uuid) REFERENCES players(uuid)
            )
        """;

        // Live data table (current status)
        String liveDataTable = """
            CREATE TABLE IF NOT EXISTS player_live_data (
                uuid VARCHAR(36) PRIMARY KEY,
                health DOUBLE DEFAULT 20.0,
                food_level INT DEFAULT 20,
                saturation DOUBLE DEFAULT 5.0,
                experience_level INT DEFAULT 0,
                experience_points FLOAT DEFAULT 0,
                location_x DOUBLE DEFAULT 0,
                location_y DOUBLE DEFAULT 0,
                location_z DOUBLE DEFAULT 0,
                world VARCHAR(50) DEFAULT 'world',
                inventory TEXT,
                equipment TEXT,
                last_updated BIGINT DEFAULT 0,
                FOREIGN KEY (uuid) REFERENCES players(uuid)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(playersTable);
            stmt.execute(statsTable);
            stmt.execute(liveDataTable);
        }

        plugin.getLogger().info("Database tables created successfully!");
    }

    // Player operations
    public void addPlayer(Player player) {
        String sql = "INSERT OR REPLACE INTO players (uuid, username, first_join, last_seen, is_online) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            long currentTime = System.currentTimeMillis();
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());

            // Only set first_join if player is new
            if (!playerExists(player.getUniqueId().toString())) {
                stmt.setLong(3, currentTime);
            } else {
                stmt.setLong(3, getFirstJoin(player.getUniqueId().toString()));
            }

            stmt.setLong(4, currentTime);
            stmt.setBoolean(5, true);
            stmt.executeUpdate();

            // Initialize stats and live data
            initializePlayerStats(player.getUniqueId().toString());
            updateLiveData(player);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding player: " + e.getMessage());
        }
    }

    public void removePlayer(Player player) {
        String sql = "UPDATE players SET is_online = FALSE, last_seen = ? WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error removing player: " + e.getMessage());
        }
    }

    private boolean playerExists(String uuid) {
        String sql = "SELECT uuid FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private long getFirstJoin(String uuid) {
        String sql = "SELECT first_join FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("first_join");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting first join: " + e.getMessage());
        }
        return System.currentTimeMillis();
    }

    private void initializePlayerStats(String uuid) {
        String sql = "INSERT OR IGNORE INTO player_stats (uuid) VALUES (?)";
        String liveSql = "INSERT OR IGNORE INTO player_live_data (uuid) VALUES (?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             PreparedStatement liveStmt = connection.prepareStatement(liveSql)) {

            stmt.setString(1, uuid);
            stmt.executeUpdate();

            liveStmt.setString(1, uuid);
            liveStmt.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error initializing player stats: " + e.getMessage());
        }
    }

    // Statistics operations
    public void incrementStat(String uuid, String statName, int amount) {
        String sql = "UPDATE player_stats SET " + statName + " = " + statName + " + ? WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, amount);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error incrementing stat " + statName + ": " + e.getMessage());
        }
    }

    public void updatePlaytime(String uuid, long additionalTime) {
        String sql = "UPDATE players SET playtime = playtime + ? WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, additionalTime);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating playtime: " + e.getMessage());
        }
    }

    // Live data operations
    public void updateLiveData(Player player) {
        String sql = """
            UPDATE player_live_data SET
                health = ?, food_level = ?, saturation = ?, experience_level = ?, experience_points = ?,
                location_x = ?, location_y = ?, location_z = ?, world = ?, last_updated = ?
            WHERE uuid = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, player.getHealth());
            stmt.setInt(2, player.getFoodLevel());
            stmt.setFloat(3, player.getSaturation());
            stmt.setInt(4, player.getLevel());
            stmt.setFloat(5, player.getExp());
            stmt.setDouble(6, player.getLocation().getX());
            stmt.setDouble(7, player.getLocation().getY());
            stmt.setDouble(8, player.getLocation().getZ());
            stmt.setString(9, player.getWorld().getName());
            stmt.setLong(10, System.currentTimeMillis());
            stmt.setString(11, player.getUniqueId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating live data: " + e.getMessage());
        }
    }

    // Data retrieval
    public Map<String, Object> getPlayerData(String username) {
        String sql = """
            SELECT p.*, ps.*, pld.* FROM players p
            LEFT JOIN player_stats ps ON p.uuid = ps.uuid
            LEFT JOIN player_live_data pld ON p.uuid = pld.uuid
            WHERE p.username = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("username", rs.getString("username"));
                data.put("uuid", rs.getString("uuid"));
                data.put("first_join", rs.getLong("first_join"));
                data.put("last_seen", rs.getLong("last_seen"));
                data.put("playtime", rs.getLong("playtime"));
                data.put("is_online", rs.getBoolean("is_online"));

                // Stats
                data.put("blocks_broken", rs.getInt("blocks_broken"));
                data.put("blocks_placed", rs.getInt("blocks_placed"));
                data.put("deaths", rs.getInt("deaths"));
                data.put("player_kills", rs.getInt("player_kills"));
                data.put("mob_kills", rs.getInt("mob_kills"));
                data.put("distance_traveled", rs.getDouble("distance_traveled"));
                data.put("items_crafted", rs.getInt("items_crafted"));
                data.put("food_consumed", rs.getInt("food_consumed"));

                // Live data
                data.put("health", rs.getDouble("health"));
                data.put("food_level", rs.getInt("food_level"));
                data.put("saturation", rs.getDouble("saturation"));
                data.put("experience_level", rs.getInt("experience_level"));
                data.put("experience_points", rs.getFloat("experience_points"));
                data.put("location_x", rs.getDouble("location_x"));
                data.put("location_y", rs.getDouble("location_y"));
                data.put("location_z", rs.getDouble("location_z"));
                data.put("world", rs.getString("world"));

                return data;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting player data: " + e.getMessage());
        }

        return null;
    }

    public List<Map<String, Object>> getOnlinePlayers() {
        String sql = "SELECT username, uuid FROM players WHERE is_online = TRUE";
        List<Map<String, Object>> players = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("username", rs.getString("username"));
                player.put("uuid", rs.getString("uuid"));
                players.add(player);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting online players: " + e.getMessage());
        }

        return players;
    }

    public int getTotalPlayers() {
        String sql = "SELECT COUNT(*) as count FROM players";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting total players: " + e.getMessage());
        }
        return 0;
    }

    public int getOnlinePlayerCount() {
        String sql = "SELECT COUNT(*) as count FROM players WHERE is_online = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting online player count: " + e.getMessage());
        }
        return 0;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }
}