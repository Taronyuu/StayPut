package nl.zandervdm.stayput.Commands;

import com.google.common.collect.*;
import nl.zandervdm.stayput.StayPut;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class StayputCommand implements CommandExecutor {

    protected StayPut plugin;

    public StayputCommand(StayPut plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(strings.length == 0){
            sendInfoMessage(commandSender);
            return true;
        }

        String executedCommand = strings[0];

        if(executedCommand.equals("reload")){
            if(!commandSender.hasPermission("stayput.admin")){
                this.sendMessage(commandSender, "You don't have permission to execute this command.");
                return true;
            }
            this.plugin.reloadConfig();
            this.plugin.setupConfig();
            this.plugin.checkTableRebuild();
            // Re-setup the dimensions if the config got altered.
            this.plugin.setupDimensions();
            this.sendMessage(commandSender, "Config has been reloaded!");
            return true;
        }

        if(executedCommand.equals("listworlds")) {
            this.sendMessage(commandSender, "-- Dimension List --");
            ImmutableMultimap<String,String> dimensions = this.plugin.getDimensionManager().getDimensions();
            for(String dimension_name : dimensions.keySet() ) {
                this.sendMessage(commandSender, dimension_name + ":");
                dimensions.get(dimension_name).forEach(world_name -> {
                    this.sendMessage(commandSender," - " + world_name);
                });
            }
        }

        if(executedCommand.equals("rebuildTables")) {
            this.sendMessage(commandSender, "-- Deleting and Rebuilding Position --");
            this.plugin.rebuildTables();
        }

        return true;
    }

    protected void sendInfoMessage(CommandSender commandSender){
        this.sendMessage(commandSender, "Available commands:");
        this.sendMessage(commandSender, "/stayput reload - Reloads the config files");
        this.sendMessage(commandSender, "/stayput listdimensions - Lists all the dimensions");
        this.sendMessage(commandSender, "/stayput rebuildTables - DO NOT RUN THIS");
    }

    protected void sendMessage(CommandSender commandSender, String message){
        commandSender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "StayPut" + ChatColor.GRAY + "] " + message);
    }
}
