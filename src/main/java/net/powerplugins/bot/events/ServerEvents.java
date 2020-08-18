package net.powerplugins.bot.events;

import net.powerplugins.bot.PowerPlugins;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerEvents implements Listener{
    
    private final PowerPlugins plugin;
    
    public ServerEvents(PowerPlugins plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onServerLoad(ServerLoadEvent ignored){
        if(plugin.getWebhookManager() == null) {
            plugin.getLogger().warning("WebhookManager is not activated! Skipping Plugin checks...");
            return;
        }
        
        plugin.checkPlugins();
    }
}
