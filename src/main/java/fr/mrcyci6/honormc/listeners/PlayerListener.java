package fr.mrcyci6.honormc.listeners;

import fr.mrcyci6.honormc.HonorRanking;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {

    private HonorRanking plugin;

    public PlayerListener(HonorRanking plugin) {
        this.plugin = plugin;
    }
}
