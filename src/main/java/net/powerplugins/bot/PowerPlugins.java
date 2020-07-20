package net.powerplugins.bot;

import net.powerplugins.bot.events.ServerEvents;
import net.powerplugins.bot.manager.FileManager;
import net.powerplugins.bot.manager.WebhookManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class PowerPlugins extends JavaPlugin{
    
    private FileManager fileManager;
    private WebhookManager webhookManager;
    
    @Override
    public void onLoad(){
        getLogger().info("Loading config and dependencies...");
        saveDefaultConfig();
        
        String url = getConfig().getString("guild.webhook");
        if(url == null){
            getLogger().warning("Unable to setup webhook! Disabling WebhookManager...");
            fileManager = null;
        }else{
            getLogger().info("Setting up the WebhookManager...");
            webhookManager = new WebhookManager(this, url);
        }
        
        fileManager = new FileManager(this);
    }
    
    @Override
    public void onEnable(){
        getLogger().info("Waiting for the server to be ready...");
        new ServerEvents(this);
    }
    
    @Override
    public void onDisable(){
        getLogger().info("Disabling plugin. Good bye!");
    }
    
    public void checkPlugins(){
        for(Plugin plugin : retrievePlugins())
            webhookManager.sendUpdate(plugin);
        
        getLogger().info("Plugin checks finished!");
        webhookManager.finish();
    }
    
    private List<Plugin> retrievePlugins(){
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
}
