package hu.thingz.waypoints;

import hu.thingz.waypoints.commands.WaypointCommand;
import hu.thingz.waypoints.commands.WaypointTabCompleter;
import hu.thingz.waypoints.database.DatabaseManager;
import hu.thingz.waypoints.database.WaypointRepository;

import org.bukkit.plugin.java.JavaPlugin;

public class WaypointPlugin extends JavaPlugin {
    private DatabaseManager databaseManager;
    private WaypointRepository waypointRepository;

    @Override
    public void onEnable() {
        this.databaseManager = DatabaseManager.getInstance(this);
        this.databaseManager.initialize();

        this.waypointRepository = WaypointRepository.getInstance(this.databaseManager);

        registerCommands();

        getLogger().info("Waypoint plugin enabled");
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }

        getLogger().info("Waypoint plugin disabled");
    }

    private void registerCommands() {
        WaypointCommand waypointCommand = new WaypointCommand(this, this.waypointRepository);
        WaypointTabCompleter waypointTabCompleter = new WaypointTabCompleter(this.waypointRepository);

        getCommand("waypoint").setExecutor(waypointCommand);
        getCommand("waypoint").setTabCompleter(waypointTabCompleter);
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public WaypointRepository getWaypointRepository() {
        return this.waypointRepository;
    }
}

