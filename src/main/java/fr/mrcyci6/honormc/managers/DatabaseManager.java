package fr.mrcyci6.honormc.managers;

import fr.mrcyci6.honormc.HonorRanking;
import fr.mrcyci6.honormc.utils.FileUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager {

    private HonorRanking plugin;
    private Connection connection;

    public DatabaseManager(HonorRanking plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void initializeDatabase() throws SQLException, IOException {
        if (connection != null && !connection.isClosed()) {
            this.plugin.sendLog("§aDatabase initialization");
            if (!tableExists()) {
                createTables();
                this.plugin.sendLog("§aTable \"classement\" initialized");
            } else {
                Path path = Paths.get("plugins/" + this.plugin.getName() + "/classement/");
                String query = "SELECT name, cpoints, fpoints FROM classement";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    int cpoints = resultSet.getInt("cpoints");
                    int fpoints = resultSet.getInt("fpoints");

                    try (DirectoryStream<Path> pathStream = Files.newDirectoryStream(path, "*.json")) {
                        for (Path filePath : pathStream) {
                            File ok = new File(path + "\\" + name + ".json");
                            if(!ok.exists()) {
                                SerializationManager serializationManager = this.plugin.getSerializationManager();
                                final ClassementManager classementManager = new ClassementManager(name, cpoints, fpoints);
                                final String json = serializationManager.serialize(classementManager);

                                FileUtils.save(ok, json);
                                this.plugin.sendLog("§a" + name + " Successfully updated");
                            }
                        }
                    }
                }

                resultSet.close();
                preparedStatement.close();
            }
            this.plugin.sendLog("§aDatabase successfully connected");
        } else {
            this.plugin.sendLog("§cAn error as occurred while connecting to the database");
        }
    }

    public void updateDatabase(CommandSender sender) throws IOException, SQLException {
        if(connection != null && !connection.isClosed()) {
            Path path = Paths.get("plugins/" + this.plugin.getName() + "/classement/");
            SerializationManager serializationManager = this.plugin.getSerializationManager();
            String query = "INSERT INTO classement (name, cpoints, fpoints) VALUES (?, ?, ?)"
                    + "ON DUPLICATE KEY UPDATE cpoints = ?, fpoints = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            try (DirectoryStream<Path> pathStream = Files.newDirectoryStream(path, "*.json")) {
                for (Path filePath : pathStream) {
                    String jsonContent = new String(Files.readAllBytes(filePath));

                    ClassementManager classementManager = serializationManager.deserialize(jsonContent);

                    preparedStatement.setString(1, classementManager.getName());
                    preparedStatement.setInt(2, classementManager.getcPoints());
                    preparedStatement.setInt(3, classementManager.getfPoints());
                    preparedStatement.setInt(4, classementManager.getcPoints());
                    preparedStatement.setInt(5, classementManager.getfPoints());

                    preparedStatement.executeUpdate();
                    this.plugin.sendLog("§a" + classementManager.getName() + " successfully updated");
                }

                sender.sendMessage(this.plugin.getConfig().getString("prefix").replaceAll("&", "§") + this.plugin.getConfig().getString("update-success").replaceAll("&", "§"));
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(this.plugin.getConfig().getString("prefix").replaceAll("&", "§") + "§cAn error as occurred while connecting to the database");
        }
    }


    private void createTables() throws SQLException {
        String createTableSQL = "CREATE TABLE classement ("
                + "name VARCHAR(255) PRIMARY KEY,"
                + "cpoints INT,"
                + "fpoints INT"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableExists() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, "classement", null);
        return resultSet.next();
    }
}


