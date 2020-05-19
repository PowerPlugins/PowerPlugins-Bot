package net.powerplugins.bot.manager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.powerplugins.bot.PowerPlugins;
import org.bukkit.plugin.Plugin;

public class MessageManager{
    
    private final PowerPlugins bot;
    
    public MessageManager(PowerPlugins bot){
        this.bot = bot;
    }
    
    public void sendUpdate(Plugin plugin){
        if(!bot.isReady()){
            bot.getLogger().info("Cannot send message. Bot is not marked READY.");
            return;
        }
        
        String name = plugin.getName().toLowerCase();
        FileManager.PluginFile config = bot.getFileManager().getPluginFile(plugin);
        
        if(config == null)
            return;
    
        if(!bot.getFileManager().isDifferent(plugin, config))
            return;
        
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(0xF39C12);
        
        if(config.isNew()){
            embed.setTitle("Added " + plugin.getName() + " to the Server!")
                    .setDescription(String.format(
                            "%s has been added to PowerPlugins and can be found on the server!\n" +
                            "Use `/pl %s` for more information.",
                            plugin.getName(),
                            name
                    ));
        }else{
            embed.setTitle("Updated " + plugin.getName() + "!")
                    .setDescription(String.format(
                            "%s has been updated to %s",
                            plugin.getName(),
                            plugin.getDescription().getVersion()
                    ))
                    .addField(
                            "Spigot Page:",
                            config.getUrl(),
                            false
                    );
        }
        
        String channelId = bot.getConfig().getString("guild.channels.pluginUpdates.id");
        String roleId = bot.getConfig().getString("guild.role");
        if(channelId == null ||  roleId == null){
            bot.getLogger().warning("Could not send Update message! Channel or Role ID was invalid!");
            return;
        }
        
        TextChannel tc = bot.getJda().getTextChannelById(channelId);
        if(tc == null){
            bot.getLogger().warning("Could not send Update message! Channel was invalid!");
            return;
        }
    
        Role role = bot.getJda().getRoleById(roleId);
        if(role == null){
            bot.getLogger().warning("Could not send Update message! Role was invalid!");
            return;
        }
        
        tc.sendMessage(role.getAsMention()).embed(embed.build()).queue();
    }
    
    public void updateList(Plugin... plugins){
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0xF39C12)
                .setTitle("Plugin List")
                .setDescription(
                        "This is a list of all plugins available on the server.\n" +
                        "It will be updated on each server restart to keep it up to date!"
                );
        
        StringBuilder sb = new StringBuilder();
        for(Plugin plugin : plugins){
            if(sb.length() + plugin.getName().length() + 10 >= MessageEmbed.VALUE_MAX_LENGTH){
                builder.addField(
                        EmbedBuilder.ZERO_WIDTH_SPACE,
                        String.format(
                                "```\n" +
                                "%s" +
                                "```",
                                sb.toString()
                        ),
                        false
                );
    
                sb = new StringBuilder(plugin.getName()).append("\n");
                continue;
            }
            
            sb.append(plugin.getName()).append("\n");
        }
        
        if(sb.length() > 0)
            builder.addField(
                    EmbedBuilder.ZERO_WIDTH_SPACE,
                    String.format(
                            "```\n" +
                            "%s" +
                            "```",
                            sb.toString()
                    ),
                    false
            );
        
        String msgId = null;
        if(bot.getConfig().get("guild.channels.plugin.messageId") != null)
            msgId = bot.getConfig().getString("guild.channels.plugins.messageId");
        
        String channelId = bot.getConfig().getString("guild.channels.plugins.id");
        if(channelId == null)
            return;
        
        TextChannel tc = bot.getJda().getTextChannelById(channelId);
        if(tc == null)
            return;
        
        if(msgId != null){
            Message msg = tc.retrieveMessageById(msgId).complete();
            if(!msg.getAuthor().equals(bot.getJda().getSelfUser()))
                return;
            
            msg.editMessage(builder.build()).override(true).queue();
        }else{
            tc.sendMessage(builder.build()).queue(message -> {
                bot.getConfig().set("guild.channels.plugins.messageId", message.getId());
                bot.saveConfig();
            });
        }
    }
}
