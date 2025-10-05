package hu.thingz.waypoints.models;

import org.bukkit.Location;

import java.util.UUID;

public class Waypoint {
    private UUID playerId;
    private Location loc;
    private String name;

    public Waypoint(UUID playerId, Location location, String name) {
        this.playerId = playerId;
        this.loc = location;
        this.name = name;
    }

    public Location getLocation() {
        return this.loc;
    }

    public Location setLocation(Location loc) {
        this.loc = loc;
        return this.loc;
    }

    public String getName() {
        return this.name;
    }

    public String setName(String name) {
        this.name = name;
        return this.name;
    }
}
