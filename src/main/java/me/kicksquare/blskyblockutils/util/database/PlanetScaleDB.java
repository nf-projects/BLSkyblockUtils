package me.kicksquare.blskyblockutils.util.database;


import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Properties;

public class PlanetScaleDB {
    private final BLSkyblockUtils plugin;

    private final String host;
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;

    public PlanetScaleDB(BLSkyblockUtils plugin, String host, String database, String username, String password) {
        this.plugin = plugin;

        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;

        connect();
    }

    public void connect() {
        // create the connection
        try {
            // JDBC connection properties
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useSSL", "true"); // Enable SSL

            String url = "jdbc:mysql://" + host + "/" + database;
            connection = DriverManager.getConnection(url, props);

            // set the session variable for caching here
            // Create a Statement object
            Statement stmt = connection.createStatement();

            // Set the session variable for caching
            String sql = "SET @@boost_cached_queries = true";
            stmt.execute(sql);

            plugin.getLogger().info("Connected to PlanetScale database!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to PlanetScale database!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close PlanetScale database connection!");
            e.printStackTrace();
        }
    }

    public void reconnect() {
        close();
        connect();
    }

    /**
     * Executes a SQL query with optional parameters.
     * Example:
     * try {
     *     String query = "SELECT * FROM your_table WHERE column_name = ? AND another_column = ?";
     *     Object param1 = "some_value";
     *     Object param2 = 42;
     *     ResultSet resultSet = database.query(query, param1, param2);
     *     while (resultSet.next()) {
     *         // Process the results here
     *     }
     *     resultSet.close();
     * } catch (SQLException e) {
     *     e.printStackTrace();
     * } finally {
     *     // Close the database connection when done
     *     database.closeConnection();
     * }
     * ```
     *
     * @param query  the SQL query to execute
     * @param params optional parameters to bind to the query
     * @return a ResultSet containing the query results
     * @throws SQLException if an SQL error occurs
     */
    public ResultSet query(String query, Object... params) {
        Bukkit.broadcastMessage("query");
        try {
            // Create a PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // Set parameters (if any)
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            // Execute query
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to query PlanetScale database!");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Executes an SQL update query with optional parameters.
     * @param query the SQL query to execute
     * @param params optional parameters to bind to the query
     */
    public void update(String query, Object... params) {
        try {
            // Create a PreparedStatement
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            // Set parameters (if any)
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            // Execute query
            preparedStatement.executeUpdate();

            // Close resources
            preparedStatement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update PlanetScale database!");
            e.printStackTrace();
        }
    }
}
