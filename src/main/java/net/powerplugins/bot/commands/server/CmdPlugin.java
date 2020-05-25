package net.powerplugins.bot.commands.server;

import com.github.rainestormee.jdacommand.CommandDescription;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.powerplugins.bot.PowerPlugins;
import net.powerplugins.bot.commands.Command;
import net.powerplugins.bot.manager.ResourceInfoManager;
import org.bukkit.plugin.Plugin;

import java.text.DecimalFormat;

@CommandDescription(
        name = "Plugin",
        description = "allows you to see Spigot info from a plugin.",
        triggers = {"plugin", "plugins", "pl"}
)
public class CmdPlugin implements Command{
    
    private final PowerPlugins bot;
    
    public CmdPlugin(PowerPlugins bot){
        this.bot = bot;
    }
    
    @Override
    public void run(Guild guild, TextChannel tc, Message msg, Member member, String... args){
        if(args.length == 0){
            bot.sendMessage(
                    tc,
                    "Missing arguments!\n" +
                    "**Usage**: `%splugin <plugin>`",
                    bot.getPrefix()
            );
            return;
        }
        
        if(bot.getPlugins().stream().noneMatch(o -> o.getName().equalsIgnoreCase(args[0]))){
            bot.sendMessage(
                    tc,
                    "The provided name `%s` doesn't match any Plugin on the Server.\n" +
                    "Please check that the plugin is actually on the server (<#712384295901069402>)!",
                    args[0]
            );
            return;
        }
    
        Plugin temp = bot.getPlugins().stream()
                .filter(plugin -> plugin.getName().equalsIgnoreCase(args[0]))
                .findFirst()
                .orElse(null);
        
        if(temp == null){
            bot.sendMessage(
                    tc,
                    "The provided name `%s` was invalid!\n" +
                    "Double-check that the plugin is actually on the server (<#712384295901069402>)!",
                    args[0]
            );
            return;
        }
    
        bot.getResourceInfoManager().retrieveResourceInfo(temp).whenComplete(((resourceInfo, throwable) -> {
            switch(resourceInfo.getType()){
                case BUKKIT:
                    bot.sendMessage(
                            tc,
                            "The requested Plugin is a Bukkit plugin.\n" +
                            "I cannot retrieve information about plugins only available on bukkit."
                    );
                    break;
                
                case PRIVATE:
                    bot.sendMessage(
                            tc,
                            "The requested Plugin is a private plugin.\n" +
                            "I cannot get information of plugins that aren't on Spigot."
                    );
                    break;
                
                case HTTP_ERROR:
                    bot.sendMessage(
                            tc,
                            "The Spiget API responded with a non-successful response (`%d`).\n" +
                            "Please try again later.",
                            resourceInfo.getResponse()
                    );
                    break;
                
                case JSON_ERROR:
                    bot.sendMessage(
                            tc,
                            "There was an error with the returned JSON from the API."
                    );
                    break;
                
                case SPIGOT:
                    tc.sendMessage(getInfo(resourceInfo)).queue();
                    break;
    
                default:
                case UNKNOWN_ERROR:
                    bot.sendMessage(
                            tc,
                            "There was an unknown error from the API."
                    );
                    break;
            }
        })).join();
    }
    
    private MessageEmbed getInfo(ResourceInfoManager.ResourceInfo info){
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0xF39C12)
                .setTitle(info.getName(), info.getUrl())
                .setThumbnail(info.getIcon().getUrl())
                .setDescription(info.getTag())
                .addField(
                        "Downloads",
                        String.valueOf(info.getDownloads()),
                        true
                )
                .addField(
                        "Rating",
                        String.format(
                                "Total Ratings: `%d`\n" +
                                "Average: %s",
                                info.getRating().getCount(),
                                getRatingIcons(info)
                        ),
                        true
                );
        
        if(info.isPremium())
            builder.addField(
                    "Price",
                    String.format(
                            "`%d %s`",
                            info.getPrice(),
                            info.getCurrency()
                    ),
                    true
            );
        else
            builder.addBlankField(true);
        
        return builder.build();
    }
    
    private String getRatingIcons(ResourceInfoManager.ResourceInfo info){
        final String LIT_STAR = "<:lit_star:714592297500803083>";
        final String UNLIT_STAR = "<:unlit_star:714592297689415770>";
        
        ResourceInfoManager.Rating rating = info.getRating();
        
        DecimalFormat format = new DecimalFormat("#.##");
        String rounded = format.format(rating.getAverage());
        
        long lit_stars = Math.round(rating.getAverage());
        
        if(lit_stars == 5){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %f')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    info.getUrl(),
                    rating.getAverage()
            );
        }else
        if(lit_stars == 4){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %f')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    rating.getAverage()
            );
        }else
        if(lit_stars == 3){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %f')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    rating.getAverage()
            );
        }else
        if(lit_stars == 2){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %f')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    rating.getAverage()
            );
        }else
        if(lit_stars == 1){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %f')",
                    rounded,
                    LIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    rating.getAverage()
            );
        }else{
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %f')",
                    rounded,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    rating.getAverage()
            );
        }
    }
}
