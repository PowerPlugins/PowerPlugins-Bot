package net.powerplugins.bot.manager;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.collect.Lists;
import net.powerplugins.bot.PowerPlugins;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class WebhookManager{
    
    private final PowerPlugins instance;
    private final WebhookClient client;
    
    private final List<WebhookEmbed> embeds = new ArrayList<>();
    
    public WebhookManager(PowerPlugins instance, String url){
        this.instance = instance;
        this.client = new WebhookClientBuilder(url).build();
    }
    
    public void checkUpdate(Plugin plugin){
        FileManager.PluginFile config = instance.getFileManager().getPluginFile(plugin);
        if(config == null) {
            instance.getLogger().warning("Plugin " + plugin.getName() + " was skipped. No plugin file present...");
            return;
        }
    
        if(!instance.getFileManager().isDifferent(plugin, config))
            return;
        
        embeds.add(getEmbed(plugin, config.getUrl(), config.isNew()));
    }
    
    public void send(){
        if(embeds.isEmpty()){
            instance.getLogger().info("No updates present. Skipping...");
            return;
        }
        
        List<List<WebhookEmbed>> groups = Lists.partition(embeds, 10);
        
        for(int i = 0; i < groups.size(); i++){
            String roleId = instance.getConfig().getString("guild.role");
            String tag;
            if(i == 0 && roleId != null){
                tag = "<@&" + roleId + ">";
            }else{
                tag = "\u200E";
            }
    
            List<WebhookEmbed> embeds = groups.get(i);
            
            WebhookMessage msg = new WebhookMessageBuilder()
                    .setContent(tag)
                    .addEmbeds(embeds)
                    .build();
            
            instance.getLogger().info("Sending Notification about " + embeds.size() + " plugins...");
            
            client.send(msg);
        }
    }
    
    public void finish(){
        client.close();
    }
    
    private WebhookEmbed getEmbed(Plugin plugin, String url, boolean isNew){
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
                .setColor(0xF39C12)
                .setTitle(new WebhookEmbed.EmbedTitle(
                        isNew ? "Added " + plugin.getName() + " to the Server!" : "Updated " + plugin.getName() + "!",
                        null
                ));
        
        if(isNew){
            builder.setDescription(String.format(
                    "The Plugin `%s` can now be found on the Server!\n" +
                    "Use `/pl info %s` on the MC Server to get Infos about it.",
                    plugin.getName(),
                    plugin.getName()
            ));
        }else{
            builder.setDescription(String.format(
                    "The Plugin `%s` has been updated to `%s`",
                    plugin.getName(),
                    plugin.getDescription().getVersion()
            )).addField(new WebhookEmbed.EmbedField(
                    false,
                    "Plugin Page:",
                    url
            ));
        }
        
        builder.addField(new WebhookEmbed.EmbedField(
                false,
                "Plugin Author(s):",
                instance.getAuthors(plugin.getDescription().getAuthors())
        ));
        
        return builder.build();
    }
}
