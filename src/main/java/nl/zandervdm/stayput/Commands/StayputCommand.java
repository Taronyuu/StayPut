package nl.zandervdm.stayput.Commands;

import nl.zandervdm.stayput.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StayputCommand implements CommandExecutor {

    protected Main plugin;

    public StayputCommand(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(strings.length == 0){
            sendInfoMessage(commandSender);
            return true;
        }

        String executedCommand = strings[0];

        if(executedCommand.equals("reload")){
            this.plugin.reloadConfig();
            this.plugin.setupConfig();
            this.sendMessage(commandSender, "Config has been reloaded!");
            return true;
        }

        return true;
    }

    protected void sendInfoMessage(CommandSender commandSender){
        this.sendMessage(commandSender, "Available commands:");
        this.sendMessage(commandSender, "/stayput reload - Reloads the config files");
    }

    protected void sendMessage(CommandSender commandSender, String message){
        commandSender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "StayPut" + ChatColor.GRAY + "] " + message);
    }
}
