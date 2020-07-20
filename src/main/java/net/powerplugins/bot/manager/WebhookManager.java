package net.powerplugins.bot.manager;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.powerplugins.bot.PowerPlugins;
import org.bukkit.plugin.Plugin;

public class WebhookManager{
    
    private final PowerPlugins bot;
    private final WebhookClient client;
    
    public WebhookManager(PowerPlugins bot, String url){
        this.bot = bot;
        this.client = new WebhookClientBuilder(url).build();
    }
    
    public void sendUpdate(Plugin plugin){
        FileManager.PluginFile config = bot.getFileManager().getPluginFile(plugin);
        if(config == null) {
            bot.getLogger().warning("Skipped plugin " + plugin.getName() + ". No plugin file present...");
            return;
        }
    
        if(!bot.getFileManager().isDifferent(plugin, config))
            return;
    
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
                .setColor(0xF39C12);
        
        if(config.isNew()){
            bot.getLogger().info(plugin.getName() + " is new. Sending message...");
            
            builder.setTitle(
                    new WebhookEmbed.EmbedTitle(
                            "Added " + plugin.getName() + " to the Server!", 
                            null
                    )
            )
            .setDescription(String.format(
                    "%s has been added to PowerPlugins and can now be discovered on the server.\n" +
                    "Use the command `/pl %s` on the server for more info.",
                    plugin.getName(),
                    plugin.getName()
            ));
        }else{
            bot.getLogger().info(plugin.getName() + " was updated. Sending message...");
            
            builder.setTitle(
                    new WebhookEmbed.EmbedTitle(
                            "Updated " + plugin.getName() + "!",
                            config.getUrl()
                    )
            )
            .setDescription(String.format(
                    "%s has been updated to %s",
                    plugin.getName(),
                    plugin.getDescription().getVersion()
            ))
            .addField(new WebhookEmbed.EmbedField(
                    false,
                    "Spigot page:",
                    config.getUrl()
            ));
        }
        
        String roleId = bot.getConfig().getString("guild.role");
        String tag;
        if(roleId == null){
            tag = "\u200E"; // Zero width space
        }else{
            tag = "<@&" + roleId + ">";
        }
        
        WebhookMessage msg = new WebhookMessageBuilder()
                .setContent(tag)
                .addEmbeds(builder.build())
                .build();
        
        client.send(msg);
    }
    
    public void finish(){
        client.close();
    }
}
