package com.drhalley.gunlukodul.database;

import org.bukkit.entity.Player;

import java.sql.*;

public class Database {
    private final Connection connection;
    public Database(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try(Statement statement = connection.createStatement();){
            statement.execute("CREATE TABLE IF NOT EXISTS varsayilan ("
                    + "uuid TEXT PRIMARY KEY, "
                    + "username TEXT NOT NULL, "
                    + "last_claimed LONG NOT NULL DEFAULT 0, "
                    + "next_claim_start LONG NOT NULL DEFAULT 0, "
                    + "next_claim_end LONG NOT NULL DEFAULT 0, "
                    + "streak INT NOT NULL DEFAULT 0)");

        }



    }
    public Connection getConnection(){
        return connection;
    }

    public void closeConnection() throws SQLException{
        if(connection != null && !connection.isClosed()){
            connection.close();
        }
    }

    public void addPlayer(Player player) throws SQLException {
        //this should error if the player already exists
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO varsayilan (uuid, username) VALUES (?, ?)")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setString(2, player.getName());
            preparedStatement.executeUpdate();
        }
    }

    public boolean playerExists(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM varsayilan WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public void setPlayerStreak(Player player, int streak) throws SQLException{

        //if the player doesn't exist, add them
        if (!playerExists(player)){
            addPlayer(player);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE varsayilan SET streak = ? WHERE uuid = ?")) {
            preparedStatement.setInt(1, streak);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public void setPlayerLastClaimed(Player player, long last_claimed) throws SQLException{

        //if the player doesn't exist, add them
        if (!playerExists(player)){
            addPlayer(player);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE varsayilan SET last_claimed = ? WHERE uuid = ?")) {
            preparedStatement.setLong(1, last_claimed);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public void setPlayerNextClaimStart(Player player, long next_claim_start) throws SQLException{

        //if the player doesn't exist, add them
        if (!playerExists(player)){
            addPlayer(player);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE varsayilan SET next_claim_start = ? WHERE uuid = ?")) {
            preparedStatement.setLong(1, next_claim_start);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public void setPlayerNextClaimEnd(Player player, long next_claim_end) throws SQLException{

        //if the player doesn't exist, add them
        if (!playerExists(player)){
            addPlayer(player);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE varsayilan SET next_claim_end = ? WHERE uuid = ?")) {
            preparedStatement.setLong(1, next_claim_end);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public int getPlayerStreak(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT streak FROM varsayilan WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("streak");
            } else {
                return 0; // Return 0 if the player has no points
            }
        }
    }

    public long getPlayerLastClaimed(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT last_claimed FROM varsayilan WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("last_claimed");
            } else {
                return 0; // Return 0 if the player has no points
            }
        }
    }

    public long getPlayerNextClaimStart(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT next_claim_start FROM varsayilan WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("next_claim_start");
            } else {
                return 0; // Return 0 if the player has no points
            }
        }
    }

    public long getPlayerNextClaimEnd(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT next_claim_end FROM varsayilan WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("next_claim_end");
            } else {
                return 0; // Return 0 if the player has no points
            }
        }
    }

}
