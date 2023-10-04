package fr.mrcyci6.honormc.tasks;

import fr.mrcyci6.honormc.HonorRanking;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.SQLException;

public class UpdateTask extends BukkitRunnable {

    private int timer = HonorRanking.getInstance().getConfig().getInt("auto-update");
    private HonorRanking plugin;

    public UpdateTask(HonorRanking plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        if(timer == 0) {
            if(this.plugin.getConfig().getBoolean("auto-update-enable")) {
                try {
                    this.plugin.getDatabaseManager().updateDatabase(Bukkit.getConsoleSender());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            timer = this.plugin.getConfig().getInt("auto-update");
        }

        timer--;
    }
}
