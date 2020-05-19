package net.powerplugins.bot.events;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.powerplugins.bot.PowerPlugins;
import net.powerplugins.bot.PowerPlugins.BotState;

import javax.annotation.Nonnull;

public class BotEvents extends ListenerAdapter{
    
    private final PowerPlugins bot;
    
    public BotEvents(PowerPlugins bot){
        this.bot = bot;
    }
    
    @Override
    public void onReady(@Nonnull ReadyEvent event){
        bot.getLogger().info("Bot is ready! Performing plugin checks...");
        bot.setState(BotState.READY);
        
        event.getJDA().getPresence().setPresence(
                OnlineStatus.ONLINE, 
                Activity.of(Activity.ActivityType.WATCHING, "plugins updating")
        );
        bot.updateTopic();
        
        bot.checkPlugins();
    }
    
    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event){
        bot.setState(BotState.RECONNECTING);
    }
    
    @Override
    public void onReconnect(@Nonnull ReconnectedEvent event){
        bot.setState(BotState.READY);
    }
}
