package net.lowkeycraft.stats.listeners;

import net.lowkeycraft.stats.LowkeycraftStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final LowkeycraftStats plugin;
    private final Map<UUID, Long> joinTimes = new HashMap<>();

    public PlayerListener(LowkeycraftStats plugin) {
        this.plugin = plugin;
        startLiveDataUpdater();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Record join time for playtime calculation
        joinTimes.put(uuid, System.currentTimeMillis());

        // Add player to database
        plugin.getDatabaseManager().addPlayer(player);

        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Player " + player.getName() + " joined - data recorded");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Calculate and update playtime
        if (joinTimes.containsKey(uuid)) {
            long sessionTime = System.currentTimeMillis() - joinTimes.get(uuid);
            plugin.getDatabaseManager().updatePlaytime(uuid.toString(), sessionTime);
            joinTimes.remove(uuid);
        }

        // Update player as offline
        plugin.getDatabaseManager().removePlayer(player);

        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("Player " + player.getName() + " quit - session data updated");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().areBlocksBrokenTracked()) return;

        Player player = event.getPlayer();
        plugin.getDatabaseManager().incrementStat(player.getUniqueId().toString(), "blocks_broken", 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().areBlocksPlacedTracked()) return;

        Player player = event.getPlayer();
        plugin.getDatabaseManager().incrementStat(player.getUniqueId().toString(), "blocks_placed", 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().areDeathsTracked()) return;

        Player player = event.getEntity();
        plugin.getDatabaseManager().incrementStat(player.getUniqueId().toString(), "deaths", 1);

        // Check if it was a PvP kill
        if (plugin.getConfigManager().arePlayerKillsTracked() && player.getKiller() != null) {
            Player killer = player.getKiller();
            plugin.getDatabaseManager().incrementStat(killer.getUniqueId().toString(), "player_kills", 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getConfigManager().areMobKillsTracked()) return;
        if (event.getEntity().getKiller() == null) return;
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player player = (Player) event.getEntity().getKiller();

        // Only count mob kills, not player kills
        if (!(event.getEntity() instanceof Player)) {
            plugin.getDatabaseManager().incrementStat(player.getUniqueId().toString(), "mob_kills", 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isDistanceTraveledTracked()) return;
        if (event.getFrom().equals(event.getTo())) return; // No actual movement

        Player player = event.getPlayer();
        double distance = event.getFrom().distance(event.getTo());

        // Convert to integer (blocks traveled)
        int blocksDistance = (int) Math.round(distance);
        if (blocksDistance > 0) {
            plugin.getDatabaseManager().incrementStat(player.getUniqueId().toString(), "distance_traveled", blocksDistance);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(PlayerItemConsumeEvent event) {
        if (!plugin.getConfigManager().isFoodConsumedTracked()) return;

        Player player = event.getPlayer();
        // Check if item is food
        if (event.getItem().getType().isEdible()) {
            plugin.getDatabaseManager().incrementStat(player.getUniqueId().toString(), "food_consumed", 1);
        }
    }

    // Live data updater
    private void startLiveDataUpdater() {
        int interval = plugin.getConfigManager().getUpdateInterval();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Update live data for all online players
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.getConfigManager().isHealthFoodTracked() ||
                        plugin.getConfigManager().isLocationTracked()) {

                        plugin.getDatabaseManager().updateLiveData(player);
                    }
                }

                if (plugin.getConfigManager().isDebugEnabled() && !plugin.getServer().getOnlinePlayers().isEmpty()) {
                    plugin.getLogger().info("Updated live data for " + plugin.getServer().getOnlinePlayers().size() + " online players");
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L * interval, 20L * interval); // Convert seconds to ticks
    }

    // Handle server shutdown - update all playtimes
    public void onDisable() {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<UUID, Long> entry : joinTimes.entrySet()) {
            long sessionTime = currentTime - entry.getValue();
            plugin.getDatabaseManager().updatePlaytime(entry.getKey().toString(), sessionTime);
        }

        joinTimes.clear();
        plugin.getLogger().info("Updated playtime for all online players on shutdown");
    }
}