package fr.mrcyci6.honormc.commands;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.mrcyci6.honormc.HonorRanking;
import fr.mrcyci6.honormc.managers.ClassementManager;
import fr.mrcyci6.honormc.utils.FileUtils;
import fr.mrcyci6.honormc.managers.SerializationManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class RankingCommand implements CommandExecutor {

    private HonorRanking plugin;
    private File saveDir;

    public RankingCommand(HonorRanking plugin) {
        this.plugin = plugin;
        this.saveDir = new File(plugin.getDataFolder(), "/classement/");
    }

    private boolean checkPerm(Player player) {
        return !player.hasPermission(this.plugin.getConfig().getString("permission"));
    }

    private Faction getFaction(String string) {

        Faction f;
        Player player;
        FPlayer fplayer;

        f = Factions.getInstance().getByTag(string);
        player = Bukkit.getPlayer(string);

        if(f != null) {
            return f;
        }

        if(player != null) {
            fplayer = FPlayers.getInstance().getByPlayer(player);

            if(fplayer.getFaction().getTag().equals(Factions.getInstance().getWilderness().getTag())) {
                return null;
            }

            return fplayer.getFaction();
        }

        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String arg, String[] args) {

        final String prefix = this.plugin.getConfig().getString("prefix").replaceAll("&", "§");

        if(args.length < 1) {
            this.plugin.sendHelpMessage(sender);
            return false;
        }

        Faction faction;
        String type;
        int fpoint = 0;
        int cpoint = 0;
        File file;
        SerializationManager serializationManager;

        switch (args[0]) {
            case "reload":
                if(sender instanceof Player) {
                    if (checkPerm((Player) sender)) {
                        sender.sendMessage(prefix + this.plugin.getConfig().getString("permission-error").replaceAll("&", "§"));
                        return false;
                    }
                }

                this.plugin.reloadConfig();
                sender.sendMessage(prefix + this.plugin.getConfig().getString("reload-plugin-success").replaceAll("&", "§"));

                break;
            case "update":
                if(sender instanceof Player) {
                    if (checkPerm((Player) sender)) {
                        sender.sendMessage(prefix + this.plugin.getConfig().getString("permission-error").replaceAll("&", "§"));
                        return false;
                    }
                }

                try {
                    this.plugin.getDatabaseManager().updateDatabase(sender);
                } catch (IOException | SQLException e) {
                    sender.sendMessage(prefix + "&cAn error as occurred, more info on Console");
                }

                break;
            case "info":

                if(args.length < 2) {
                    this.plugin.sendHelpMessage(sender);
                    break;
                }

                faction = getFaction(args[1]);

                if(faction == null) {
                    sender.sendMessage(prefix + this.plugin.getConfig().getString("faction-not-found").replaceAll("&", "§"));
                    break;
                }

                file = new File(saveDir, faction.getTag() + ".json");
                serializationManager = this.plugin.getSerializationManager();
                int cpoints;
                int fpoints;

                if(file.exists()) {

                    String json = FileUtils.loadContent(file);
                    ClassementManager classementManager = serializationManager.deserialize(json);

                    cpoints = classementManager.getcPoints();
                    fpoints = classementManager.getfPoints();
                } else {
                    fpoints = 0;
                    cpoints = 0;
                }

                this.plugin.getConfig().getStringList("info-output").forEach(message -> {
                    sender.sendMessage(message.replaceAll("&", "§").replaceAll("%s1", faction.getTag()).replaceAll("%s2", String.valueOf(cpoints)).replaceAll("%s3", String.valueOf(fpoints)));
                });
                break;
            case "addpoints":
                if(sender instanceof Player) {
                    if (checkPerm((Player) sender)) {
                        sender.sendMessage(prefix + this.plugin.getConfig().getString("permission-error").replaceAll("&", "§"));
                        return false;
                    }
                }

                if(args.length < 4) {
                    this.plugin.sendHelpMessage(sender);
                    break;
                }

                faction = getFaction(args[1]);
                type = args[2];

                if(faction == null) {
                    sender.sendMessage(prefix + this.plugin.getConfig().getString("faction-not-found").replaceAll("&", "§"));
                    break;
                }

                if(type.toLowerCase().equals("competitive")) {
                    cpoint = Integer.parseInt(args[3]);
                } else if(type.toLowerCase().equals("farming")){
                    fpoint = Integer.parseInt(args[3]);
                } else {
                    this.plugin.sendHelpMessage(sender);
                    break;
                }

                file = new File(saveDir, faction.getTag() + ".json");
                serializationManager = this.plugin.getSerializationManager();

                if(file.exists()) {

                    String json = FileUtils.loadContent(file);
                    ClassementManager classementManager = serializationManager.deserialize(json);
                    classementManager.setfPoints(classementManager.getfPoints() + fpoint);
                    classementManager.setcPoints(classementManager.getcPoints() + cpoint);

                    json = serializationManager.serialize(classementManager);
                    FileUtils.save(file, json);

                    sender.sendMessage(prefix + this.plugin.getConfig().getString("add-points-success").replaceAll("&", "§").replaceAll("%s1", args[3]).replaceAll("%s2", faction.getTag()));
                } else {

                    final ClassementManager classementManager = new ClassementManager(faction.getTag(), cpoint, fpoint);
                    final String json = serializationManager.serialize(classementManager);

                    FileUtils.save(file, json);
                    sender.sendMessage(prefix + this.plugin.getConfig().getString("add-points-success").replaceAll("&", "§").replaceAll("%s1", args[3]).replaceAll("%s2", faction.getTag()));
                }

                break;
            case "removepoints":
                if(sender instanceof Player) {
                    if (checkPerm((Player) sender)) {
                        sender.sendMessage(prefix + this.plugin.getConfig().getString("permission-error").replaceAll("&", "§"));
                        return false;
                    }
                }

                if(args.length < 3) {
                    this.plugin.sendHelpMessage(sender);
                    break;
                }

                faction = getFaction(args[1]);
                type = args[2];

                if(faction == null) {
                    sender.sendMessage(prefix + this.plugin.getConfig().getString("faction-not-found").replaceAll("&", "§"));
                    break;
                }

                if(type.toLowerCase().equals("competitive")) {
                    cpoint = Integer.parseInt(args[3]);
                } else if(type.toLowerCase().equals("farming")){
                    fpoint = Integer.parseInt(args[3]);
                } else {
                    this.plugin.sendHelpMessage(sender);
                    break;
                }

                file = new File(saveDir, faction.getTag() + ".json");
                serializationManager = this.plugin.getSerializationManager();

                if(file.exists()) {

                    String json = FileUtils.loadContent(file);
                    ClassementManager classementManager = serializationManager.deserialize(json);
                    classementManager.setfPoints(classementManager.getfPoints() - fpoint);
                    classementManager.setcPoints(classementManager.getcPoints() - cpoint);

                    json = serializationManager.serialize(classementManager);
                    FileUtils.save(file, json);

                    sender.sendMessage(prefix + this.plugin.getConfig().getString("remove-points-success").replaceAll("&", "§").replaceAll("%s1", args[3]).replaceAll("%s2", faction.getTag()));
                } else {

                    final ClassementManager classementManager = new ClassementManager(faction.getTag(), cpoint, fpoint);
                    final String json = serializationManager.serialize(classementManager);

                    FileUtils.save(file, json);
                    sender.sendMessage(prefix + this.plugin.getConfig().getString("remove-points-success").replaceAll("&", "§").replaceAll("%s1", args[3]).replaceAll("%s2", faction.getTag()));
                }
                break;
            case "reset":
                if(sender instanceof Player) {
                    if (checkPerm((Player) sender)) {
                        sender.sendMessage(prefix + this.plugin.getConfig().getString("permission-error").replaceAll("&", "§"));
                        return false;
                    }
                }

                if(args.length < 3) {
                    this.plugin.sendHelpMessage(sender);
                    break;
                }

                faction = getFaction(args[1]);
                type = args[2];

                if(faction == null) {
                    sender.sendMessage(prefix + this.plugin.getConfig().getString("faction-not-found").replaceAll("&", "§"));
                    break;
                }

                file = new File(saveDir, faction.getTag() + ".json");
                serializationManager = this.plugin.getSerializationManager();

                if(file.exists()) {

                    String json = FileUtils.loadContent(file);
                    ClassementManager classementManager = serializationManager.deserialize(json);

                    if(type.toLowerCase().equals("competitive")) {
                        cpoint = 0;
                        fpoint = classementManager.getfPoints();
                    } else if(type.toLowerCase().equals("farming")){
                        cpoint = classementManager.getcPoints();
                        fpoint = 0;
                    } else {
                        this.plugin.sendHelpMessage(sender);
                        break;
                    }

                    classementManager.setfPoints(fpoint);
                    classementManager.setcPoints(cpoint);

                    json = serializationManager.serialize(classementManager);
                    FileUtils.save(file, json);

                    sender.sendMessage(prefix + this.plugin.getConfig().getString("reset-points-success").replaceAll("&", "§").replaceAll("%s", faction.getTag()));
                } else {

                    final ClassementManager classementManager = new ClassementManager(faction.getTag(), 0, 0);
                    final String json = serializationManager.serialize(classementManager);

                    FileUtils.save(file, json);
                    sender.sendMessage(prefix + this.plugin.getConfig().getString("reset-points-success").replaceAll("&", "§").replaceAll("%s", faction.getTag()));
                }

                break;
            default:

                this.plugin.sendHelpMessage(sender);
                break;
        }


        return false;
    }
}
