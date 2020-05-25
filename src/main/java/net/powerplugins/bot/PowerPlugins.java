package net.powerplugins.bot;

import com.github.rainestormee.jdacommand.CommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.powerplugins.bot.commands.CommandListener;
import net.powerplugins.bot.commands.CommandLoader;
import net.powerplugins.bot.events.BotEvents;
import net.powerplugins.bot.events.ServerEvents;
import net.powerplugins.bot.manager.FileManager;
import net.powerplugins.bot.manager.MessageManager;
import net.powerplugins.bot.manager.ResourceInfoManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.stream.Collectors;

public class PowerPlugins extends JavaPlugin{
    
    private JDA jda;
    private BotState state;
    
    private String prefix;
    private List<Plugin> plugins;
    
    private FileManager fileManager;
    private MessageManager messageManager;
    private ResourceInfoManager resourceInfoManager;
    
    private final CommandHandler<Message> cmdHandler = new CommandHandler<>();
    
    @Override
    public void onLoad(){
        getLogger().info("Loading config and dependencies...");
        saveDefaultConfig();
        
        prefix = getConfig().getString("guild.prefix");
        plugins = new ArrayList<>();
        
        fileManager = new FileManager(this);
        messageManager = new MessageManager(this);
        resourceInfoManager = new ResourceInfoManager(this);
    }
    
    @Override
    public void onEnable(){
        new ServerEvents(this);
    }
    
    @Override
    public void onDisable(){
        getLogger().info("Disabling plugin. Good bye!");
        
        if(state.equals(BotState.READY))
            jda.shutdown();
    }
    
    public void startBot(){
        Thread startupThread = new Thread(this::setup, "PowerPlugins - Startup Thread");
        startupThread.setUncaughtExceptionHandler((t, ex) -> {
            getLogger().severe("Unable to startup Bot! " + ex.getMessage());
            ex.printStackTrace();
        });
        startupThread.start();
    }
    
    private void setup(){
        getLogger().info("Server is ready! Starting bot...");
        try{
            state = BotState.STARTING;
            jda = JDABuilder.createDefault(getConfig().getString("bot.token"))
                    .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "Starting..."))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .disableCache(
                            CacheFlag.CLIENT_STATUS,
                            CacheFlag.ACTIVITY,
                            CacheFlag.VOICE_STATE
                    )
                    .addEventListeners(
                            new BotEvents(this)
                    )
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .build();
        }catch(LoginException ex){
            getLogger().severe("Unable to startup Bot! " + ex.getMessage());
            getLogger().severe("Plugin will be disabled...");
            ex.printStackTrace();
            
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    public void checkPlugins(){
        plugins = Arrays.stream(getServer().getPluginManager().getPlugins())
                .sorted(Comparator.comparing(Plugin::getName))
                .collect(Collectors.toList());
        
        for(Plugin plugin : plugins)
            messageManager.sendUpdate(plugin);
        
        messageManager.updateList(plugins);
        
        getLogger().info("Plugin checks finished!");
    }
    
    public void setupCommands(JDA jda){
        cmdHandler.registerCommands(new HashSet<>(new CommandLoader(this).getCommands()));
        jda.addEventListener(new CommandListener(this, cmdHandler));
    }
    
    public JDA getJda(){
        return jda;
    }
    
    public boolean isReady(){
        return state.equals(BotState.READY);
    }
    
    public void setState(BotState state){
        this.state = state;
    }
    
    public String getPrefix(){
        return prefix;
    }
    
    public List<Plugin> getPlugins(){
        return plugins;
    }
    
    public FileManager getFileManager(){
        return fileManager;
    }
    
    public ResourceInfoManager getResourceInfoManager(){
        return resourceInfoManager;
    }
    
    public CommandHandler<Message> getCmdHandler(){
        return cmdHandler;
    }
    
    public enum BotState{
        STARTING,
        READY,
        RECONNECTING
    }
    
    public void updateTopic(){
        String pluginUpdatesId = getConfig().getString("guild.channels.pluginUpdates.id");
        if(pluginUpdatesId == null)
            return;
        
        TextChannel pluginUpdatesChannel = getJda().getTextChannelById(pluginUpdatesId);
        if(pluginUpdatesChannel == null)
            return;
    
        pluginUpdatesChannel.getManager().setTopic(
                String.join("\n", getConfig().getStringList("guild.channels.pluginUpdates.topic"))
                      .replace("%plugins%", String.valueOf(getServer().getPluginManager().getPlugins().length))
        ).queue();
        
        String pluginsId = getConfig().getString("guild.channels.plugins.id");
        if(pluginsId == null)
            return;
        
        TextChannel pluginsChannel = getJda().getTextChannelById(pluginsId);
        if(pluginsChannel == null)
            return;
        
        pluginsChannel.getManager().setTopic(
                String.join("\n", getConfig().getStringList("guild.channels.plugins.topic"))
                      .replace("%plugins%", String.valueOf(getServer().getPluginManager().getPlugins().length))
        ).queue();
    }
    
    public void sendMessage(TextChannel channel, String message){
        messageManager.sendMessage(channel, message);
    }
    
    public void sendMessage(TextChannel channel, String message, Object... args){
        sendMessage(channel, String.format(message, args));
    }
}
