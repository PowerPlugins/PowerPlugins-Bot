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
                    String.format(
                            "There are currently `%d` plugins on the Server!\n" +
                            "Use `%splugin <plugin>` to get some information about a specific plugin.",
                            bot.getPlugins().size(),
                            bot.getPrefix()
                    )
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
                            "The requested Plugin is a **Bukkit** plugin.\n" +
                            "I cannot retrieve information about plugins only available on bukkit."
                    );
                    break;
                
                case PRIVATE:
                    bot.sendMessage(
                            tc,
                            "The requested Plugin is a **Private** plugin.\n" +
                            "I cannot get information of plugins that aren't on Spigot."
                    );
                    break;
                
                case HTTP_ERROR:
                    bot.sendMessage(
                            tc,
                            "The Spiget API responded with a non-successful response (`%d`).\n" +
                            "Please try again later.",
                            resourceInfo.getResponseCode()
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
        String title = info.getTitle();
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(0xF39C12)
                .setTitle(
                        title.length() > MessageEmbed.TITLE_MAX_LENGTH ? title.substring(0, 250) + "..." : title, 
                        info.getUrl()
                )
                .setDescription(info.getTag())
                .addField(
                        "Latest Version",
                        String.format(
                                "`%s`",
                                info.getCurrentVersion()
                        ),
                        true
                )
                .addField(
                        "Downloads",
                        String.format(
                                "`%s`",
                                getDownloads(info)
                        ),
                        true
                )
                .addField(
                        "Rating",
                        String.format(
                                "Total Ratings: `%s`\n" +
                                "Average: %s",
                                info.getStats().getReviews(),
                                getRatingIcons(info)
                        ),
                        true
                )
                .addField(
                        "Author",
                        String.format(
                                "[`%s`](%s)",
                                info.getAuthor().getUsername(),
                                info.getAuthor().getUrl()
                        ),
                        true
                );
        
        if(info.getPremium().isPremium())
            builder.addField(
                    "Price",
                    String.format(
                            "`%s %s`",
                            info.getPremium().getPrice(),
                            info.getPremium().getCurrency().toUpperCase()
                    ),
                    true
            );
        
        return builder.build();
    }
    
    private String getDownloads(ResourceInfoManager.ResourceInfo info){
        int downloads;
        try{
            downloads = Integer.parseInt(info.getStats().getDownloads());
        }catch(NumberFormatException ex){
            downloads = -1;
        }
        
        if(downloads == -1)
            return "?";
        
        return new DecimalFormat("#,###,###").format(downloads);
    }
    
    private String getRatingIcons(ResourceInfoManager.ResourceInfo info){
        final String LIT_STAR = "<:lit_star:714592297500803083>";
        final String UNLIT_STAR = "<:unlit_star:714592297689415770>";
    
        ResourceInfoManager.Stats stats = info.getStats();
        double number;
        try{
            number = Double.parseDouble(stats.getRating());
        }catch(NullPointerException | NumberFormatException ex){
            number = 0.0;
        }
        
        DecimalFormat format = new DecimalFormat("#.##");
        String rounded = format.format(number);
        
        long lit_stars = Math.round(number);
        
        if(lit_stars == 5){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %s')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    info.getUrl(),
                    stats.getRating()
            );
        }else
        if(lit_stars == 4){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %s')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    stats.getRating()
            );
        }else
        if(lit_stars == 3){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %s')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    LIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    stats.getRating()
            );
        }else
        if(lit_stars == 2){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %s')",
                    rounded,
                    LIT_STAR,
                    LIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    stats.getRating()
            );
        }else
        if(lit_stars == 1){
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %s')",
                    rounded,
                    LIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    stats.getRating()
            );
        }else{
            return String.format(
                    "[`%s` %s%s%s%s%s](%s 'Full Rating: %s')",
                    rounded,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    UNLIT_STAR,
                    info.getUrl(),
                    stats.getRating()
            );
        }
    }
}
