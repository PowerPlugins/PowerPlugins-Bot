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
    
    private final PowerPlugins instance;
    private final WebhookClient client;
    
    public WebhookManager(PowerPlugins instance, String url){
        this.instance = instance;
        this.client = new WebhookClientBuilder(url).build();
    }
    
    public void sendUpdate(Plugin plugin){
        FileManager.PluginFile config = instance.getFileManager().getPluginFile(plugin);
        if(config == null) {
            instance.getLogger().warning("Plugin " + plugin.getName() + "was skipped. No plugin file present...");
            return;
        }
    
        if(!instance.getFileManager().isDifferent(plugin, config))
            return;
        
        WebhookEmbed embed = getEmbed(plugin, config.getUrl(), config.isNew());
        
        String roleId = instance.getConfig().getString("guild.role");
        String tag;
        if(roleId == null){
            tag = "\u200E"; // Zero width space
        }else{
            tag = "<@&" + roleId + ">";
        }
        
        WebhookMessage msg = new WebhookMessageBuilder()
                .setContent(tag)
                .addEmbeds(embed)
                .build();
        
        client.send(msg);
    }
    
    public void finish(){
        client.close();
    }
    
    private WebhookEmbed getEmbed(Plugin plugin, String url, boolean isNew){
        WebhookEmbed embed;
        if(isNew){
            instance.getLogger().info(plugin.getName() + " was added to the Server. Sending message...");
            embed = new WebhookEmbedBuilder()
                    .setColor(0xF39C12)
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(
                                    "Added " + plugin.getName() + " to the Server",
                                    null
                            )
                    )
                    .setDescription(String.format(
                            "`%s` has been added to PowerPlugins and can now be found on the Server.\n" +
                            "Use `/pl %s` on the server for more info.",
                            plugin.getName(),
                            plugin.getName()
                    ))
                    .addField(
                            new WebhookEmbed.EmbedField(
                                    false,
                                    "Plugin Author(s):",
                                    instance.getAuthors(plugin.getDescription().getAuthors())
                            )
                    )
                    .build(); 
        }else{
            instance.getLogger().info(plugin.getName() + " was updated. Sending message...");
            embed = new WebhookEmbedBuilder()
                    .setColor(0xF39C12)
                    .setTitle(
                            new WebhookEmbed.EmbedTitle(
                                    "Updated " + plugin.getName() + "!",
                                    url
                            )
                    )
                    .setDescription(String.format(
                            "`%s` has been updated to `%s`",
                            plugin.getName(),
                            plugin.getDescription().getVersion()
                    ))
                    .addField(
                            new WebhookEmbed.EmbedField(
                                    false,
                                    "Plugin Author(s):",
                                    instance.getAuthors(plugin.getDescription().getAuthors())
                            )
                    )
                    .addField(
                            new WebhookEmbed.EmbedField(
                                    false,
                                    "Plugin Page:",
                                    url
                            )
                    )
                    .build();
        }
        
        return embed;
    }
}
