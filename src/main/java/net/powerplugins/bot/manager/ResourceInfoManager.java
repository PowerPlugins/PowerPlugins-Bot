package net.powerplugins.bot.manager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.powerplugins.bot.PowerPlugins;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceInfoManager{
    
    private final String SPIGET_URL = "https://api.spiget.org/v2/resources/%s";
    private static final String ICON_URL = "https://static.spigotmc.org/styles/spigot/xenresource/resource_icon.png";
    
    private final String USER_AGENT = "PowerPlugins - ResourceInfoManager";
    
    private final Pattern SPIGOT_URL_PATTERN = Pattern.compile(
            "https://(?:www\\.)?spigotmc\\.org/resources/(?<id>\\d+)", 
            Pattern.CASE_INSENSITIVE
    );
    private final Pattern BUKKIT_URL_PATTERN = Pattern.compile(
            "https://(?:www\\.)?dev\\.bukkit\\.org/projects/.+",
            Pattern.CASE_INSENSITIVE
    );
    
    private final PowerPlugins bot;
    private final Gson GSON;
    
    public ResourceInfoManager(PowerPlugins bot){
        this.bot = bot;
        this.GSON = new Gson();
    }
    
    public CompletableFuture<ResourceInfo> retrieveResourceInfo(Plugin plugin){
        return CompletableFuture.supplyAsync(() -> {
            FileManager.PluginFile file = bot.getFileManager().getPluginFile(plugin);
            
            Matcher bukkitMatcher = BUKKIT_URL_PATTERN.matcher(file.getUrl());
            Matcher spigotMatcher = SPIGOT_URL_PATTERN.matcher(file.getUrl());
            if(bukkitMatcher.matches())
                return new ResourceInfo(Type.BUKKIT);
            
            if(!spigotMatcher.matches())
                return new ResourceInfo(Type.PRIVATE);
            
            String id = spigotMatcher.group("id");
            try{
                URL url = new URL(String.format(SPIGET_URL, id));
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.addRequestProperty("User-Agent", USER_AGENT);
    
                InputStream stream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                int response = connection.getResponseCode();
                
                if(response > 299)
                    return new ResourceInfo(Type.HTTP_ERROR, response);
                
                JsonElement json = new JsonParser().parse(reader);
                if(json.isJsonArray())
                    return new ResourceInfo(Type.JSON_ERROR);
                else
                if(json.isJsonObject()){
                    ResourceInfo info = GSON.fromJson(json, ResourceInfo.class);
                    info.setType(Type.SPIGOT);
                    info.setResponse(200);
                    
                    return info;
                }else{
                    return new ResourceInfo(Type.UNKNOWN_ERROR);
                }
            }catch(IOException ex){
                ex.printStackTrace();
                return new ResourceInfo(Type.UNKNOWN_ERROR);
            }
        });
    }
    
    public static class ResourceInfo{
        
        private Type type;
        private int response;
        
        private final int id;
        
        private final String name;
        private final String tag;
        
        private final int downloads;
        
        private final Rating rating;
        
        private final Icon icon;
        
        private final boolean premium;
        private final int price;
        private final String currency;
    
        public ResourceInfo(Type type){
            this(type, 200);
        }
        
        public ResourceInfo(Type type, int response){
            this.type = type;
            this.response = response;
            
            this.id = -1;
            
            this.name = null;
            this.tag = null;
            
            this.downloads = -1;
            
            this.rating = new Rating();
            
            this.icon = new Icon();
            
            this.premium = false;
            this.price = -1;
            this.currency = null;
        }
        
        public Type getType(){
            return type;
        }
    
        public int getResponse(){
            return response;
        }
        
        public String getUrl(){
            return "https://www.spigotmc.org/resources/" + id;
        }
    
        public String getName(){
            return name;
        }
    
        public String getTag(){
            return tag;
        }
    
        public int getDownloads(){
            return downloads;
        }
    
        public Rating getRating(){
            return rating;
        }
    
        public Icon getIcon(){
            return icon;
        }
    
        public boolean isPremium(){
            return premium;
        }
    
        public int getPrice(){
            return price;
        }
    
        public String getCurrency(){
            return currency;
        }
    
        public void setType(Type type){
            this.type = type;
        }
        
        public void setResponse(int response){
            this.response = response;
        }
    }
    
    public static class Rating{
        private final int count;
        private final double average;
    
        public Rating(){
            this.count = -1;
            this.average = -1;
        }
        
        public int getCount(){
            return count;
        }
    
        public double getAverage(){
            return average;
        }
    }
    
    public static class Icon{
        private final String url = null;
        
        public Icon(){}
    
        public String getUrl(){
            return url.isEmpty() ? ICON_URL : "https://www.spigotmc.org/" + url;
        }
    }
    
    public enum Type{
        SPIGOT,
        BUKKIT,
        PRIVATE,
        
        // Used for errors and similar.
        HTTP_ERROR,
        JSON_ERROR,
        UNKNOWN_ERROR
    }
}
