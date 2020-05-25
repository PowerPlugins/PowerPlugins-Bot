package net.powerplugins.bot.manager;

import net.powerplugins.bot.PowerPlugins;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class FileManager{
    
    private static final String URL_SPIGOT = "https://spigotmc.org/resources/";
    
    private final PowerPlugins instance;
    
    private final File folder;
    
    public FileManager(PowerPlugins instance){
        this.instance = instance;
        this.folder = new File(instance.getDataFolder() + "/plugins");
    }
    
    public PluginFile getPluginFile(Plugin plugin){
        File file = getFile(plugin.getName().toLowerCase());
        
        if(file == null)
            return null;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        if(config.get("info.version") == null){
            config.set("info.version", plugin.getDescription().getVersion());
            config.set("info.url", URL_SPIGOT);
            
            try{
                config.save(file);
                
                return new PluginFile(plugin.getDescription().getVersion(), URL_SPIGOT, config);
            }catch(IOException ex){
                return null;
            }
        }else{
            return new PluginFile(
                    config.getString("info.version"), 
                    config.getString("info.url"),
                    config
            );
        }
        
    }
    
    private File getFile(String name){
        File file = new File(instance.getDataFolder() + "/plugins", name + ".yml");
        if(folder.mkdirs())
            instance.getLogger().info("Created folder 'plugins'");
        
        if(file.exists())
            return file;
        
        try{
            if(file.createNewFile())
                instance.getLogger().info("Created file " + name + ".yml!");
            
            return file;
        }catch(IOException ex){
            instance.getLogger().warning("Could not load or create file " + name + ".yml!");
            return null;
        }
    }
    
    public boolean isDifferent(Plugin plugin, PluginFile pluginFile){
        if(pluginFile.isNew())
            return true;
        
        String currentVersion = pluginFile.getVersion();
        String newVersion = plugin.getDescription().getVersion();
        
        if(!currentVersion.equals(newVersion)){
            try{
                YamlConfiguration config = pluginFile.getConfiguration();
                File file = getFile(plugin.getName().toLowerCase());
                
                if(file == null)
                    return false;
                
                config.set("info.version", plugin.getDescription().getVersion());
                
                config.save(file);
                return true;
            }catch(IOException ex){
                instance.getLogger().warning("Could not update file " + plugin.getName().toLowerCase() + ".yml!");
                ex.printStackTrace();
                
                return false;
            }
        }
        
        return false;
    }
    
    public static class PluginFile{
        
        private final String version;
        private final String url;
        private final YamlConfiguration configuration;
        
        public PluginFile(String version, String url, YamlConfiguration configuration){
            this.version = version;
            this.url = url;
            this.configuration = configuration;
        }
    
        public String getVersion(){
            return version;
        }
    
        public String getUrl(){
            return url;
        }
    
        public YamlConfiguration getConfiguration(){
            return configuration;
        }
        
        public boolean isNew(){
            return url.equalsIgnoreCase(URL_SPIGOT);
        }
    }
}
