package net.powerplugins.bot.events;

import net.powerplugins.bot.PowerPlugins;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerEvents implements Listener{
    
    private final PowerPlugins bot;
    
    public ServerEvents(PowerPlugins bot){
        this.bot = bot;
        bot.getServer().getPluginManager().registerEvents(this, bot);
    }
    
    @EventHandler
    public void onServerLoad(ServerLoadEvent ignored){
        if(bot.getWebhookManager() == null) {
            bot.getLogger().warning("WebhookManager is not activated! Skipping Plugin checks...");
            return;
        }
        
        bot.checkPlugins();
    }
}
