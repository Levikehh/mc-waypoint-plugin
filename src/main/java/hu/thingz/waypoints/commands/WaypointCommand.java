package hu.thingz.waypoints.commands;

import hu.thingz.waypoints.utils.CoordinateParser;
import hu.thingz.waypoints.utils.MessageFormatter;
import hu.thingz.waypoints.utils.Result;
import hu.thingz.waypoints.WaypointPlugin;
import hu.thingz.waypoints.database.WaypointRepository;
import hu.thingz.waypoints.models.Waypoint;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WaypointCommand implements CommandExecutor {
    private final WaypointPlugin plugin;
    private final WaypointRepository repository;

    public WaypointCommand(WaypointPlugin plugin, WaypointRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    // If onCommand returns false it will display the plugin.yml command block's 'usage' property
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageFormatter.error("Only players can run this command!"));
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("waypoint")) {
            if (args.length == 0) {
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "add":
                    addWaypoint(player, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "remove":
                    removeWaypoint(player, Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "list":
                    listWaypoints(player);
                    break;
                default:
                    return false;
            }
        }

        return true;
    }

    private void addWaypoint(Player player, String[] args) {
        try {
            if (args.length == 0 || (args.length > 1 && args.length < 4)) {
                player.sendMessage(MessageFormatter.error("Usage: /waypoint add <name> [x y z]"));
                return;
            }

            String name = args[0];
            Location location = player.getLocation();

            // If coordinates were provided, parse them
            if (args.length == 4) {
                location = CoordinateParser.parseCoordinates(player, args[1], args[2], args[3]);
            }

            Result<Void> result = this.repository.add(player.getUniqueId(), name, location);

            if (result.isSuccess()) {        
                player.sendMessage(MessageFormatter.success("Waypoint ") + MessageFormatter.variable(name) + MessageFormatter.success(" has been saved successfully!"));
            } else {
                player.sendMessage(MessageFormatter.error("Waypoint already exists with this name: ") + MessageFormatter.variable(name));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(MessageFormatter.error(e.getMessage()));
        }
    }

    private void removeWaypoint(Player player, String[] args) {
        try {
            if (args.length == 0) {
                player.sendMessage(MessageFormatter.error("Usage: /waypoint remove <name>"));
                return;
            }

            UUID playerId = player.getUniqueId();
            String name = args[0];

            Result<Boolean> hasWaypoints = this.repository.hasAny(playerId);
            if (!hasWaypoints.isSuccess()) {
                player.sendMessage(ChatColor.GRAY + "An unexpected error occured, please try again later.");
                return;
            }

            if (!hasWaypoints.getValue()) {
                player.sendMessage(MessageFormatter.error("You don't have any waypoints saved yet."));
                return;
            }

            Result<Boolean> result = this.repository.delete(playerId, name);
            if (!result.isSuccess()) {
                player.sendMessage(ChatColor.GRAY + "An unexpected error occured, please try again later.");
                return;
            }

            if (!result.getValue()) {
                player.sendMessage(MessageFormatter.error("Could not find waypoint with this name."));
                return;
            }

            player.sendMessage(MessageFormatter.success("Waypoint ") + MessageFormatter.variable(name) + MessageFormatter.success(" has been removed successfully!"));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void listWaypoints(Player player) {
        try {
            UUID playerId = player.getUniqueId();

            Result<Boolean> hasWaypoints = this.repository.hasAny(playerId);
            if (!hasWaypoints.isSuccess()) {
                player.sendMessage(ChatColor.GRAY + "An unexpected error occured, please try again later.");
                return;
            }

            if (!hasWaypoints.getValue()) {
                player.sendMessage(MessageFormatter.error("You don't have any waypoints saved yet."));
                return;
            }

            Result<List<Waypoint>> result = this.repository.list(playerId);
            if (!result.isSuccess()) {
                player.sendMessage(ChatColor.GRAY + "An unexpected error occured, please try again later.");
                return;
            }

            List<Waypoint> waypoints = result.getValue();
            
            StringBuilder message = new StringBuilder(MessageFormatter.success("=== Your waypoints (" + waypoints.size() + ") ===\n"));
            for (Waypoint waypoint : waypoints) {
                Location wLocation = waypoint.getLocation();
                
                message.append(
                        MessageFormatter.success(" - ") +
                        MessageFormatter.clickable(waypoint.getName()) + 
                        MessageFormatter.success(" @ ") +
                        MessageFormatter.formatLocation(wLocation) +
                        MessageFormatter.success("\n"));
            }

            player.sendMessage(message.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
