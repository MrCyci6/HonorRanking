package fr.mrcyci6.honormc;

import fr.mrcyci6.honormc.commands.RankingCommand;
import fr.mrcyci6.honormc.listeners.PlayerListener;
import fr.mrcyci6.honormc.managers.DatabaseManager;
import fr.mrcyci6.honormc.managers.SerializationManager;
import fr.mrcyci6.honormc.tasks.UpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;

public class HonorRanking extends JavaPlugin {

    private static HonorRanking instance;
    private DatabaseManager databaseManager;
    private SerializationManager serializationManager;
    private String prefix = "[" + getName() + "] ";

    @Override
    public void onEnable() {
        instance = this;

        // CONFIG
        saveDefaultConfig();
        this.serializationManager = new SerializationManager();

        // DATABASE
        String host = this.getConfig().getString("database.dbhost");
        int port = this.getConfig().getInt("database.dbport");
        String name = this.getConfig().getString("database.dbname");
        String user = this.getConfig().getString("database.dbuser");
        String password = this.getConfig().getString("database.dbpassword");
        databaseManager = new DatabaseManager(this, host, port, name, user, password);
        try {
            databaseManager.initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // COMMANDS
        getCommand("ranking").setExecutor(new RankingCommand(this));

        // LISTENERS
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        // TIMERS AUTO UPDATE
        UpdateTask task = new UpdateTask(this);
        task.runTaskTimer(this, 0, 20);

        sendLog("§e" + getName() + " V" + getDescription().getVersion() + " is now §aenabled");
        sendLog("§eBy " + getDescription().getAuthors());
    }

    @Override
    public void onDisable() {

        try {
            if(databaseManager != null && databaseManager.getConnection() != null && !databaseManager.getConnection().isClosed()) {
                try {
                    databaseManager.updateDatabase(Bukkit.getConsoleSender());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                databaseManager.closeConnection();
                sendLog("§cDatabase successfully deconnected");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        sendLog("§e" + getName() + " V" + getDescription().getVersion() + " is now §cdisabled");
        sendLog("§eBy " + getDescription().getAuthors());
    }

    public void sendLog(String text) {
        Bukkit.getConsoleSender().sendMessage(prefix + text);
    }

    public static HonorRanking getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SerializationManager getSerializationManager() {
        return serializationManager;
    }

    public void sendHelpMessage(CommandSender player) {
        this.getConfig().getStringList("help-message").forEach(message -> {
            player.sendMessage(this.getConfig().getString("prefix").replaceAll("&", "§") + message.replaceAll("&", "§"));
        });
    }
}