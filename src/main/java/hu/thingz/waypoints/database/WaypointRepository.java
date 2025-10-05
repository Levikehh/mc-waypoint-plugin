package hu.thingz.waypoints.database;

import hu.thingz.waypoints.models.Waypoint;
import hu.thingz.waypoints.utils.Result;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WaypointRepository {
    private static WaypointRepository instance;
    private final DatabaseManager databaseManager;

    private WaypointRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public static WaypointRepository getInstance(DatabaseManager databaseManager) {
        if (instance == null) {
            instance = new WaypointRepository(databaseManager);
        }

        return instance;
    }

    public Result<Void> add(UUID playerId, String name, Location location) {
        String sql = "INSERT INTO waypoints (player_uuid, name, world, x, y, z, color) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = this.databaseManager.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, name);
            stmt.setString(3, location.getWorld().getName());
            stmt.setDouble(4, location.getX());
            stmt.setDouble(5, location.getY());
            stmt.setDouble(6, location.getZ());
            stmt.setString(7, "WHITE");

            stmt.executeUpdate();

            return Result.success();
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure(e);
        }
    }

    public Result<Waypoint> get(UUID playerId, String name) {
        String sql = "SELECT world, x, y, z, color FROM waypoints WHERE player_uuid = ? AND name = ?;";
        try (Connection connection = this.databaseManager.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, name);

            ResultSet resultSet = stmt.executeQuery(); 
            String world = resultSet.getString("world");
            double x = resultSet.getDouble("x");
            double y = resultSet.getDouble("y");
            double z = resultSet.getDouble("z");
            String color = resultSet.getString("color");

            Location location = new Location(Bukkit.getWorld(world), x, y, z);
            return Result.success(new Waypoint(playerId, location, name));
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure(e);
        }

    }

    public Result<List<Waypoint>> list(UUID playerId) {
        List<Waypoint> waypoints = new ArrayList<>();

        String sql = "SELECT name, world, x, y, z, color FROM waypoints WHERE player_uuid = ?;";
        try (Connection connection = this.databaseManager.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String world = resultSet.getString("world");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                double z = resultSet.getDouble("z");
                String color = resultSet.getString("color");

                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                waypoints.add(new Waypoint(playerId, location, name));
            }

            return Result.success(waypoints);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure(e);
        }

    }

    public Result<Boolean> delete(UUID playerId, String name) {
        String sql = "DELETE FROM waypoints WHERE player_uuid = ? AND name = ?;";
         try (Connection connection = this.databaseManager.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, name);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                return Result.success(false);
            }

            return Result.success(true);

        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure(e);
        }
    }

    public Result<Boolean> hasAny(UUID playerId) {
        String sql = "SELECT 1 FROM waypoints WHERE player_uuid = ? LIMIT 1;";
        try (Connection connection = this.databaseManager.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                return Result.success(false);
            }

            return Result.success(true);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure(e);
        }
    }
}
