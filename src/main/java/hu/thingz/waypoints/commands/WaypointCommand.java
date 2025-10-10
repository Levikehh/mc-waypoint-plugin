package hu.thingz.waypoints.commands;

import hu.thingz.waypoints.utils.CoordinateParser;
import hu.thingz.waypoints.utils.MessageBuilder;
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

import net.md_5.bungee.api.chat.ClickEvent;

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
                player.spigot().sendMessage(new MessageBuilder().addSuccess("Waypoint ").addVariable(name).addSuccess(" has been saved successfully!").build());
            } else {
                player.spigot().sendMessage(new MessageBuilder().addError("Waypoint already exists with this name: ").addVariable(name).build());
            }
        } catch (IllegalArgumentException e) {
            player.spigot().sendMessage(new MessageBuilder().addError(e.getMessage()).build());
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
                player.spigot().sendMessage(MessageBuilder.internalError());
                return;
            }

            if (!hasWaypoints.getValue()) {
                player.spigot().sendMessage(new MessageBuilder().addError("You don't have any waypoints saved yet.").build());
                return;
            }

            Result<Boolean> result = this.repository.delete(playerId, name);
            if (!result.isSuccess()) {
                player.spigot().sendMessage(MessageBuilder.internalError());
                return;
            }

            if (!result.getValue()) {
                player.spigot().sendMessage(new MessageBuilder().addError("Could not find waypoint with this name.").build());
                return;
            }

            player.spigot().sendMessage(new MessageBuilder().addSuccess("Waypoint ").addVariable(name).addSuccess(" has been removed successfully!").build());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void listWaypoints(Player player) {
        try {
            UUID playerId = player.getUniqueId();

            Result<Boolean> hasWaypoints = this.repository.hasAny(playerId);
            if (!hasWaypoints.isSuccess()) {
                player.spigot().sendMessage(MessageBuilder.internalError());
                return;
            }

            if (!hasWaypoints.getValue()) {
                player.spigot().sendMessage(new MessageBuilder().addError("You don't have any waypoints saved yet.").build());
                return;
            }

            Result<List<Waypoint>> result = this.repository.list(playerId);
            if (!result.isSuccess()) {
                player.spigot().sendMessage(MessageBuilder.internalError());
                return;
            }

            List<Waypoint> waypoints = result.getValue();
            
            MessageBuilder message = new MessageBuilder();
            message
                .addSuccess("=== Your waypoints (")
                .addVariable(String.format("%d", waypoints.size()))
                .addSuccess(") ===\n");

            for (Waypoint waypoint : waypoints) {
                Location wLocation = waypoint.getLocation();
                
                message
                    .addSuccess(" - ")
                    .addClickable(
                        waypoint.getName(),
                        new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            String.format("/tp %.2f %.2f %.2f", wLocation.getX(), wLocation.getY(), wLocation.getZ()) 
                        ),
                        null
                    )
                    .addSuccess(" @ ")
                    .addLocation(MessageBuilder.LocationFormat.DEFAULT, wLocation)
                    .addSuccess("\n");
            }

            player.spigot().sendMessage(message.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
