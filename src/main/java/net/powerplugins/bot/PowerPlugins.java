package net.powerplugins.bot;

import net.powerplugins.bot.commands.CmdPowerPlugins;
import net.powerplugins.bot.events.CommandListener;
import net.powerplugins.bot.events.ServerEvents;
import net.powerplugins.bot.manager.FileManager;
import net.powerplugins.bot.manager.WebhookManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class PowerPlugins extends JavaPlugin{
    
    private FileManager fileManager;
    private WebhookManager webhookManager;
    
    private CmdPowerPlugins cmdPowerPlugins;
    
    @Override
    public void onLoad(){
        getLogger().info("Loading config and dependencies...");
        saveDefaultConfig();
        
        String url = getConfig().getString("guild.webhook");
        if(url == null || url.isEmpty()){
            getLogger().warning("Unable to setup webhook! Disabling WebhookManager...");
            webhookManager = null;
        }else{
            getLogger().info("Setting up the WebhookManager...");
            webhookManager = new WebhookManager(this, url);
        }
        
        fileManager = new FileManager(this);
    }
    
    @Override
    public void onEnable(){
        getLogger().info("Loading Command and Command Listener...");
        PluginCommand command = getCommand("powerplugins");
        if(command == null){
            getLogger().warning("Unable to register command. Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        cmdPowerPlugins = new CmdPowerPlugins(this);
        command.setExecutor(cmdPowerPlugins);
        
        getLogger().info("Loaded command!");
        
        getLogger().info("Waiting for the server to be ready...");
        new ServerEvents(this);
        new CommandListener(this);
    }
    
    @Override
    public void onDisable(){
        getLogger().info("Disabling plugin. Good bye!");
    }
    
    public void checkPlugins(){
        getLogger().info("Performing plugin checks...");
        for(Plugin plugin : retrievePlugins())
            webhookManager.checkUpdate(plugin);
        
        getLogger().info("Plugin checks finished! Sending webhooks...");
        webhookManager.send();
        
        getLogger().info("Finished sending of webhooks. Cleaning up WebhookManager...");
        webhookManager.finish();
    }
    
    public List<Plugin> retrievePlugins(){
        return Arrays.stream(getServer().getPluginManager().getPlugins())
                .sorted(Comparator.comparing(Plugin::getName))
                .collect(Collectors.toList());
    }
    
    public WebhookManager getWebhookManager(){
        return webhookManager;
    }
    
    public FileManager getFileManager(){
        return fileManager;
    }
    
    public CmdPowerPlugins getCmdPowerPlugins(){
        return cmdPowerPlugins;
    }
    
    public String getAuthors(List<String> authorsList){
        if(authorsList.isEmpty())
            return "Unknown";
        
        String authors = String.join(", ", authorsList);
        
        if(!authors.contains(","))
            return authors;
    
        StringBuilder builder = new StringBuilder(authors);
        builder.replace(authors.lastIndexOf(","), authors.lastIndexOf(",") + 1, " and");
    
        return builder.toString();
    }
}
