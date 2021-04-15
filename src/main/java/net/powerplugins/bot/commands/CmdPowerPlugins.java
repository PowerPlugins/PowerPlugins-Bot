package net.powerplugins.bot.commands;

import com.google.common.collect.Lists;
import me.rayzr522.jsonmessage.JSONMessage;
import net.powerplugins.bot.PowerPlugins;
import net.powerplugins.bot.manager.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdPowerPlugins implements CommandExecutor, TabCompleter{
    // Headers
    public static final String HEADER_FREE    = "&b&m---------------&b[ &aFree &b]&m---------------";
    public static final String HEADER_PREMIUM = "&b&m--------------&b[ &6Premium &b]&m--------------";
    public static final String HEADER_PRIVATE = "&b&m--------------&b[ &7Private &b]&m--------------";
    
    // Footers
    public static final String FOOTER_MAIN = "&b&m-------------------------------------";
    public static final String FOOTER_PAGE = "&b&m--------------&b[ &9Page {page} &b]&m--------------";
    
    // The [<] Nav item
    public static final String NAV_PREV_ACTIVE   = "&b[&a<&b]";
    
    // Active and Inactive [>] Nav item
    public static final String NAV_NEXT_ACTIVE   = "&b[&a>&b]";
    public static final String NAV_NEXT_INACTIVE = "&b[&7>&b]";
    
    // Plugin infos
    public static final String PLUGIN_SIMPLE       = "&b{name} &7- &f{author} &7[&f{version}&7]";
    public static final String PLUGIN_NAME         = "&b{name} &7[&f{version}&7]";
    public static final String PLUGIN_AUTHORS      = "&7Authors: &b{authors}";
    public static final String PLUGIN_DEPENDENCIES = "&7Dependencies:";
    public static final String PLUGIN_URL          = "&7Plugin Page: &b{url}";
    public static final String PLUGIN_DESCRIPTION  = "&7Description:";
    
    // Plugin Categories
    public static final String CAT_FREE    = "&aFree";
    public static final String CAT_PREMIUM = "&6Premium";
    public static final String CAT_PRIVATE = "&7Private";
    
    private final PowerPlugins plugin;
    
    public CmdPowerPlugins(PowerPlugins plugin){
        this.plugin = plugin;
    }
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(color("&cOnly Players can execute this command!"));
            return true;
        }
        
        Player player = (Player)sender;
        if(args.length == 0){
            clear(player);
            
            JSONMessage json = JSONMessage.create(color(FOOTER_MAIN))
                    .newline()
                    .newline()
                    .then(color("&7Please choose a Category.")).newline()
                    .newline()
                    .then(color("&7[&aFree Plugins&7]"))
                    .tooltip(color(
                            "&7Plugins that are available for free!\n" +
                            "\n" +
                            "&a/plugins free"
                    ))
                    .runCommand("/powerplugins free")
                    .newline()
                    .then(color("&7[&6Premium Plugins&7]"))
                    .tooltip(color(
                            "&7Plugins that you have to pay for.\n" +
                            "\n" +
                            "&a/plugins premium"
                    ))
                    .runCommand("/powerplugins premium")
                    .newline()
                    .then(color("&7[Private Plugins&7]"))
                    .tooltip(color(
                            "&7Plugins made for this particular Server.\n" +
                            "\n" +
                            "&a/plugins private"
                    ))
                    .runCommand("/powerplugins private")
                    .newline()
                    .newline()
                    .then(color(FOOTER_MAIN));
            
            json.send(player);
            return true;
        }else
        if(args[0].equalsIgnoreCase("free")){
            List<JSONMessage> free = getPages("free", color(HEADER_FREE));
            if(free.isEmpty()){
                player.sendMessage(color("&cThis category doesn't have any Plugins listed."));
                return true;
            }
            
            if(args.length == 1){
                clear(player);
                JSONMessage json = free.get(0);
                
                json.send(player);
                return true;
            }else{
                int page;
                try{
                    page = Integer.parseInt(args[1]);
                }catch(NumberFormatException ex){
                    player.sendMessage(color("&cInvalid argument provided! Expected number but got %s", args[1]));
                    return true;
                }
                
                if(page <= 0){
                    player.sendMessage(color("&cYou can't provide a number smaller than 1."));
                    return true;
                }else
                if(page > free.size()){
                    player.sendMessage(color("&cThe provided Number was larger than the available pages."));
                    player.sendMessage(color("&cCategory Free has a total of %d pages.", free.size()));
                    return true;
                }else{
                    clear(player);
                    JSONMessage json = free.get(page - 1);
                    
                    json.send(player);
                    return true;
                }
            }
        }else
        if(args[0].equalsIgnoreCase("premium")){
            List<JSONMessage> premium = getPages("premium", color(HEADER_PREMIUM));
            if(premium.isEmpty()){
                player.sendMessage(color("&cThis category doesn't have any Plugins listed."));
                return true;
            }
    
            if(args.length == 1){
                clear(player);
                JSONMessage json = premium.get(0);
        
                json.send(player);
                return true;
            }else{
                int page;
                try{
                    page = Integer.parseInt(args[1]);
                }catch(NumberFormatException ex){
                    player.sendMessage(color("&cInvalid argument provided! Expected number but got %s", args[1]));
                    return true;
                }
        
                if(page <= 0){
                    player.sendMessage(color("&cYou can't provide a number smaller than 1."));
                    return true;
                }else
                if(page > premium.size()){
                    player.sendMessage(color("&cThe provided Number was larger than the available pages."));
                    player.sendMessage(color("&cCategory Premium has a total of %d", premium.size()));
                    return true;
                }else{
                    clear(player);
                    JSONMessage json = premium.get(page - 1);
            
                    json.send(player);
                    return true;
                }
            }
        }else
        if(args[0].equalsIgnoreCase("private")){
            List<JSONMessage> priv = getPages("private", color(HEADER_PRIVATE));
            if(priv.isEmpty()){
                player.sendMessage(color("&cThis category doesn't have any Plugins listed."));
                return true;
            }
    
            if(args.length == 1){
                clear(player);
                JSONMessage json = priv.get(0);
        
                json.send(player);
                return true;
            }else{
                int page;
                try{
                    page = Integer.parseInt(args[1]);
                }catch(NumberFormatException ex){
                    player.sendMessage(color("&cInvalid argument provided! Expected number but got %s", args[1]));
                    return true;
                }
        
                if(page <= 0){
                    player.sendMessage(color("&cYou can't provide a number smaller than 1."));
                    return true;
                }else
                if(page > priv.size()){
                    player.sendMessage(color("&cThe provided Number was larger than the available pages."));
                    player.sendMessage(color("&cCategory Private has a total of %d", priv.size()));
                    return true;
                }else{
                    clear(player);
                    JSONMessage json = priv.get(page - 1);
            
                    json.send(player);
                    return true;
                }
            }
        }else
        if(args[0].equalsIgnoreCase("info")){
            if(args.length == 1){
                player.sendMessage(color("&cPlease mention a plugin you want info from!"));
                return true;
            }
            
            Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
            if(pl == null){
                player.sendMessage(color("&cThe provided plugin %s doesn't exist!", args[1]));
                return true;
            }
    
            FileManager.PluginFile pluginFile = plugin.getFileManager().getPluginFile(pl);
            if(pluginFile == null){
                player.sendMessage(color("&cThe provided plugin %s doesn't exist!", args[1]));
                return true;
            }
    
            clear(player);
            JSONMessage json = getPluginInfo(pluginFile);
            
            json.send(player);
            return true;
        }else{
            player.sendMessage(color("&cUnknown argument %s. Run /pl for the plugins.", args[0]));
            return true;
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args){
        List<String> arguments = new ArrayList<>();
        
        arguments.add("free");
        arguments.add("premium");
        arguments.add("private");
        arguments.add("info");
        
        arguments.addAll(Stream.of(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .sorted()
                .collect(Collectors.toList()));
        
        return arguments;
    }
    
    private void clear(Player player){
        for(int i = 0; i < 20; i++)
            player.sendMessage("");
    }
    
    private String color(String text, Object... replace){
        return ChatColor.translateAlternateColorCodes('&', String.format(text, replace));
    }
    
    private List<FileManager.PluginFile> getPlugins(String category){
        return plugin.retrievePlugins().stream()
                .map(plugin.getFileManager()::getPluginFile)
                .filter(pl -> pl.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
    
    private List<JSONMessage> getPages(String category, String title){
        List<List<FileManager.PluginFile>> rawPages = Lists.partition(getPlugins(category), 7);
        List<JSONMessage> pages = new ArrayList<>();
        int curPage = 0;
        final int total = rawPages.size();
        
        for(List<FileManager.PluginFile> page : rawPages){
            curPage++;
            JSONMessage json = JSONMessage.create();
            
            if(curPage > 1){
                json.then(color(NAV_PREV_ACTIVE))
                        .tooltip(color("&7Page %d", curPage - 1))
                        .runCommand("/powerplugins " + category + " " + (curPage - 1));
            }else{
                json.then(color(NAV_PREV_ACTIVE))
                        .tooltip(color("&7Back to selection."))
                        .runCommand("/powerplugins");
            }
            
            json.then(title);
    
            if(curPage < total){
                json.then(color(NAV_NEXT_ACTIVE))
                        .tooltip(color("&7Page %d", curPage + 1))
                        .runCommand("/powerplugins " + category + " " + (curPage + 1));
            }else{
                json.then(color(NAV_NEXT_INACTIVE));
            }
            
            for(FileManager.PluginFile file : page){
                String author = file.getAuthors().isEmpty() ? "Unknown" : file.getAuthors().get(0);
                
                json.newline()
                    .then(color(PLUGIN_SIMPLE
                            .replace("{name}", file.getName())
                            .replace("{author}", author)
                            .replace("{version}", file.getVersion())
                    ))
                    .tooltip(ChatColor.translateAlternateColorCodes('&', String.format(
                            "%s\n" +
                            "\n" +
                            "&aClick for more information.",
                            file.getDescription().length() > 30 ? file.getDescription().substring(0, 25) + "..." : file.getDescription()
                    )))
                    .runCommand("/powerplugins info " + file.getName());
            }
            
            if(page.size() < 7){
                int padding = page.size();
                
                while(padding < 7){
                    padding++;
                    
                    json.newline();
                }
            }
            
            json.newline();
            
            if(curPage > 1){
                json.then(color(NAV_PREV_ACTIVE))
                    .tooltip(color("&7Page %d", curPage - 1))
                    .runCommand("/powerplugins " + category + " " + (curPage - 1));
            }else{
                json.then(color(NAV_PREV_ACTIVE))
                    .tooltip(color("&7Back to selection."))
                    .runCommand("/powerplugins");
            }
            
            json.then(color(FOOTER_PAGE.replace("{page}", String.valueOf(curPage))));
            
            if(curPage < total){
                json.then(color(NAV_NEXT_ACTIVE))
                    .tooltip(color("&7Page %d", curPage + 1))
                    .runCommand("/powerplugins " + category + " " + (curPage + 1));
            }else{
                json.then(color(NAV_NEXT_INACTIVE));
            }
            
            pages.add(json);
        }
        
        return pages;
    }
    
    private JSONMessage getPluginInfo(FileManager.PluginFile pluginFile){
        String category;
        String title;
        switch(pluginFile.getCategory()){
            case "free":
                category = color(CAT_FREE);
                title = color(HEADER_FREE);
                break;
            case "premium":
                category = color(CAT_PREMIUM);
                title = color(HEADER_PREMIUM);
                break;
            default:
            case "private":
                category = color(CAT_PRIVATE);
                title = color(HEADER_PRIVATE);
                break;
        }
        
        JSONMessage json = JSONMessage.create(color(NAV_PREV_ACTIVE))
                .tooltip(color("&7Back to Plugin category %s.", category))
                .runCommand("/powerplugins " + pluginFile.getCategory())
                .then(color(title))
                .then(color(NAV_NEXT_INACTIVE))
                .newline()
                .then(color(PLUGIN_NAME
                        .replace("{name}", pluginFile.getName())
                        .replace("{version}", pluginFile.getVersion())
                ))
                .newline()
                .newline()
                .then(color(PLUGIN_AUTHORS.replace("{authors}", plugin.getAuthors(pluginFile.getAuthors()))));
        
        if(!pluginFile.getDepends().isEmpty() || !pluginFile.getSoftDepends().isEmpty()){
            Map<String, Boolean> dependencies = new HashMap<>();
            
            if(!pluginFile.getDepends().isEmpty()){
                for(String depends : pluginFile.getDepends())
                    dependencies.put(depends, true);
            }
            
            if(!pluginFile.getSoftDepends().isEmpty()){
                for(String softDepend : pluginFile.getSoftDepends())
                    dependencies.put(softDepend, false);
            }
            
            Map<String, Boolean> sorted = new TreeMap<>(dependencies);
            
            json.newline()
                .then(color(PLUGIN_DEPENDENCIES));
            for(String dependency : sorted.keySet()){
                json.newline()
                    .then(color("&7- &b%s", dependency))
                    .tooltip(color(
                            "&7Type: &b%s\n" +
                            "\n" +
                            "&7Click to view Information about %s.\n" +
                            "&cNote: The dependency Type is based on the plugins' plugin.yml!",
                            sorted.get(dependency) ? "Depend &7[&cRequired&7]" : "Softdepend &7[&aNot Required&7]",
                            dependency
                    ))
                    .runCommand("/powerplugins info " + dependency);
            }
        }
        
        json.newline()
            .then(color(PLUGIN_URL.replace("{url}", pluginFile.getUrl())))
            .tooltip(color("&7Click to view the plugin page."))
            .openURL(pluginFile.getUrl())
            .newline()
            .then(color(PLUGIN_DESCRIPTION))
            .newline()
            .then(color("&b%s", pluginFile.getDescription()))
            .newline()
            .then(color(NAV_PREV_ACTIVE))
            .tooltip(color("&7Back to Plugin category %s.", category))
            .runCommand("/powerplugins " + pluginFile.getCategory())
            .then(color(FOOTER_MAIN))
            .then(color(NAV_NEXT_INACTIVE));
        
        return json;
    }
}
