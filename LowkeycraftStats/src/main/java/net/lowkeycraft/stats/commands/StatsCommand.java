package net.lowkeycraft.stats.commands;

import net.lowkeycraft.stats.LowkeycraftStats;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class StatsCommand implements CommandExecutor {

    private final LowkeycraftStats plugin;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm");

    public StatsCommand(LowkeycraftStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Handle reload command
        if (command.getName().equalsIgnoreCase("statsreload")) {
            return handleReload(sender);
        }

        // Handle stats command
        if (command.getName().equalsIgnoreCase("stats")) {
            return handleStats(sender, args);
        }

        return false;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("lowkeycraft.stats.admin")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + " " +
                plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }

        try {
            plugin.reloadPluginConfig();
            sender.sendMessage(plugin.getConfigManager().getPrefix() + " " +
                plugin.getConfigManager().getReloadSuccessMessage());
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + " " +
                ChatColor.RED + "Error reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
        }

        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lowkeycraft.stats.view")) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + " " +
                plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }

        String targetPlayer;

        // Determine target player
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getPrefix() + " " +
                    ChatColor.RED + "You must specify a player name when using this command from console.");
                return true;
            }
            targetPlayer = sender.getName();
        } else {
            targetPlayer = args[0];
        }

        // Get player data from database
        Map<String, Object> playerData = plugin.getDatabaseManager().getPlayerData(targetPlayer);

        if (playerData == null) {
            sender.sendMessage(plugin.getConfigManager().getPrefix() + " " +
                plugin.getConfigManager().getPlayerNotFoundMessage());
            return true;
        }

        // Display stats
        displayPlayerStats(sender, playerData);
        return true;
    }

    private void displayPlayerStats(CommandSender sender, Map<String, Object> data) {
        String username = (String) data.get("username");

        // Header
        String header = plugin.getConfigManager().getStatsHeaderMessage()
            .replace("{player}", username);
        sender.sendMessage(header);

        // Basic info
        boolean isOnline = (Boolean) data.get("is_online");
        String status = isOnline ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline";
        sender.sendMessage(ChatColor.GRAY + "Status: " + status);

        // First join and last seen
        long firstJoin = (Long) data.get("first_join");
        long lastSeen = (Long) data.get("last_seen");

        sender.sendMessage(ChatColor.GRAY + "First Join: " + ChatColor.WHITE +
            dateFormat.format(new Date(firstJoin)));

        if (!isOnline) {
            sender.sendMessage(ChatColor.GRAY + "Last Seen: " + ChatColor.WHITE +
                dateFormat.format(new Date(lastSeen)));
        }

        // Playtime
        long playtimeMs = (Long) data.get("playtime");
        String playtimeFormatted = formatPlaytime(playtimeMs);
        sender.sendMessage(ChatColor.GRAY + "Playtime: " + ChatColor.GOLD + playtimeFormatted);

        // Basic statistics
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "=== Statistics ===");

        int blocksBroken = (Integer) data.get("blocks_broken");
        int blocksPlaced = (Integer) data.get("blocks_placed");
        int deaths = (Integer) data.get("deaths");
        int playerKills = (Integer) data.get("player_kills");
        int mobKills = (Integer) data.get("mob_kills");
        double distanceTraveled = (Double) data.get("distance_traveled");
        int itemsCrafted = (Integer) data.get("items_crafted");
        int foodConsumed = (Integer) data.get("food_consumed");

        sender.sendMessage(ChatColor.GRAY + "Blocks Broken: " + ChatColor.GREEN + formatNumber(blocksBroken));
        sender.sendMessage(ChatColor.GRAY + "Blocks Placed: " + ChatColor.GREEN + formatNumber(blocksPlaced));
        sender.sendMessage(ChatColor.GRAY + "Deaths: " + ChatColor.RED + formatNumber(deaths));
        sender.sendMessage(ChatColor.GRAY + "Player Kills: " + ChatColor.DARK_RED + formatNumber(playerKills));
        sender.sendMessage(ChatColor.GRAY + "Mob Kills: " + ChatColor.BLUE + formatNumber(mobKills));
        sender.sendMessage(ChatColor.GRAY + "Distance Traveled: " + ChatColor.AQUA +
            decimalFormat.format(distanceTraveled) + " blocks");
        sender.sendMessage(ChatColor.GRAY + "Items Crafted: " + ChatColor.LIGHT_PURPLE + formatNumber(itemsCrafted));
        sender.sendMessage(ChatColor.GRAY + "Food Consumed: " + ChatColor.YELLOW + formatNumber(foodConsumed));

        // Live data (only if player is online)
        if (isOnline) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "=== Live Status ===");

            double health = (Double) data.get("health");
            int foodLevel = (Integer) data.get("food_level");
            double saturation = (Double) data.get("saturation");
            int expLevel = (Integer) data.get("experience_level");
            float expPoints = (Float) data.get("experience_points");

            sender.sendMessage(ChatColor.GRAY + "Health: " + ChatColor.RED +
                decimalFormat.format(health) + "/20.0 ❤");
            sender.sendMessage(ChatColor.GRAY + "Food: " + ChatColor.GOLD +
                foodLevel + "/20 \uD83C\uDF56");
            sender.sendMessage(ChatColor.GRAY + "Saturation: " + ChatColor.YELLOW +
                decimalFormat.format(saturation));
            sender.sendMessage(ChatColor.GRAY + "Experience: " + ChatColor.GREEN +
                "Level " + expLevel + " (" + decimalFormat.format(expPoints * 100) + "%)");

            // Location
            double x = (Double) data.get("location_x");
            double y = (Double) data.get("location_y");
            double z = (Double) data.get("location_z");
            String world = (String) data.get("world");

            sender.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.AQUA +
                world + " (" +
                (int) Math.round(x) + ", " +
                (int) Math.round(y) + ", " +
                (int) Math.round(z) + ")");
        }

        sender.sendMessage(ChatColor.GRAY + "===============================");
    }

    private String formatPlaytime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return decimalFormat.format(number / 1000000.0) + "M";
        } else if (number >= 1000) {
            return decimalFormat.format(number / 1000.0) + "K";
        } else {
            return String.valueOf(number);
        }
    }
}