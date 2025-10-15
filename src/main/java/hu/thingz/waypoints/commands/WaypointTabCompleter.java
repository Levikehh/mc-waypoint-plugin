package hu.thingz.waypoints.commands;

import hu.nomindz.devkit.utils.Result;
import hu.thingz.waypoints.database.WaypointRepository;
import hu.thingz.waypoints.models.Waypoint;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WaypointTabCompleter implements TabCompleter {
    private final WaypointRepository repository;
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "add", "remove", "list"
            );

    public WaypointTabCompleter(WaypointRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        } 

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(SUBCOMMANDS);
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "add":
                    completions.add("<name>");
                    break;
                case "tp":
                case "remove":
                    Result<List<Waypoint>> result = this.repository.list(player.getUniqueId());
                    if (!result.isSuccess()) {
                        player.sendMessage(ChatColor.GRAY + "An unexpected error occured, please try again later.");
                        return filterCompletions(completions, args[args.length - 1]);
                    }

                    List<Waypoint> waypoints = result.getValue();
                    for (Waypoint waypoint : waypoints) {
                        completions.add(waypoint.getName());
                    }
                    break;
                case "list":
                default:
                    break;
            }
        }

        return filterCompletions(completions, args[args.length - 1]);
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        String lowerInput = input.toLowerCase();

        return completions.stream()
            .filter(completion -> completion.toLowerCase().startsWith(lowerInput))
            .collect(Collectors.toList());
    }
}
