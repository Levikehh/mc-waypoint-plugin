package hu.thingz.waypoints.commands;

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
            sender.sendMessage("Only players can run this command!");
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
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Usage: /waypoint add <name> [x y z]");
                return;
            }

            String name = args[0];
            Location location = player.getLocation();

            Result<Void> result = this.repository.add(player.getUniqueId(), name, location);

            if (result.isSuccess()) {        
                player.sendMessage(ChatColor.GOLD + "Waypoint " + ChatColor.AQUA + name + ChatColor.GOLD + " has been saved successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "Waypoint already exists with this name: " + ChatColor.AQUA + name);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void removeWaypoint(Player player, String[] args) {
        try {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Usage: /waypoint add <name> [x y z]");
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
                player.sendMessage(ChatColor.RED + "You don't have any waypoints saved yet.");
                return;
            }

            Result<Boolean> result = this.repository.delete(playerId, name);
            if (!result.isSuccess()) {
                player.sendMessage(ChatColor.GRAY + "An unexpected error occured, please try again later.");
                return;
            }

            if (!result.getValue()) {
                player.sendMessage(ChatColor.RED + "Could not find waypoint with this name.");
                return;
            }

            player.sendMessage(ChatColor.GOLD + "Waypoint " + ChatColor.AQUA+ name + ChatColor.GOLD + " has been removed successfully!");
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
                player.sendMessage("You don't have any waypoints saved yet.");
                return;
            }

            Result<List<Waypoint>> result = this.repository.list(playerId);
            if (!result.isSuccess()) {
                player.sendMessage(ChatColor.GRAY + "An unexpected error occured, please try again later.");
                return;
            }

            List<Waypoint> waypoints = result.getValue();
            
            StringBuilder message = new StringBuilder(ChatColor.GOLD + "Your saved waypoints are:\n");
            for (Waypoint waypoint : waypoints) {
                Location wLocation = waypoint.getLocation();

                StringBuilder locationString = new StringBuilder();
                locationString.append(ChatColor.GOLD + "x:" + ChatColor.AQUA + wLocation.getX() + " ");
                locationString.append(ChatColor.GOLD + "y:" + ChatColor.AQUA + wLocation.getY() + " ");
                locationString.append(ChatColor.GOLD + "z:" + ChatColor.AQUA + wLocation.getZ() + " ");

                message.append(ChatColor.GOLD + "\t- " + ChatColor.AQUA + waypoint.getName() + ChatColor.GOLD + " @ " + locationString + "\n");
            }

            player.sendMessage(message.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
