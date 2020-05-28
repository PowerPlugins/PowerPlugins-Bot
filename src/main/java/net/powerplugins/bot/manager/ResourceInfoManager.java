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
    
    private final String SPIGET_URL = "https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=%s";
    
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
                    info.setResponseCode(200);
                    
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
        private int responseCode;
        
        private final String id;
        
        private final String title;
        private final String tag;
        
        private final String current_version;
        
        private final Author author;
        
        private final Premium premium;
        
        private final Stats stats;
        
        public ResourceInfo(Type type){
            this(type, 200);
        }
        
        public ResourceInfo(Type type, int responseCode){
            this.type = type;
            this.responseCode = responseCode;
            
            this.id = null;
            
            this.title = null;
            this.tag = null;
            
            this.current_version = null;
            
            this.author = new Author();
            
            this.premium = new Premium();
            
            this.stats = new Stats();
        }
    
        public Type getType(){
            return type;
        }
    
        public int getResponseCode(){
            return responseCode;
        }
    
        private int getId(){
            return id == null ? -1 : Integer.parseInt(id);
        }
        
        public String getUrl(){
            return "https://www.spigotmc.org/resources/" + (getId() == -1 ? "" : getId());
        }
    
        public String getTitle(){
            return title;
        }
        
        public String getTag(){
            return tag;
        }
    
        public String getCurrentVersion(){
            return current_version;
        }
    
        public Author getAuthor(){
            return author;
        }
    
        public Premium getPremium(){
            return premium;
        }
    
        public Stats getStats(){
            return stats;
        }
        
        public void setType(Type type){
            this.type = type;
        }
    
        public void setResponseCode(int responseCode){
            this.responseCode = responseCode;
        }
    }
    
    public static class Author{
        private final String id;
        private final String username;
        
        public Author(){
            this.id = null;
            this.username = null;
        }
    
        public String getUrl(){
            return "https://www.spigotmc.org/resources/authors/" + id;
        }
    
        public String getUsername(){
            return username;
        }
    }
    
    public static class Premium{
        private final String price;
        private final String currency;
        
        public Premium(){
            this.price = "0.00";
            this.currency = null;
        }
    
        public String getPrice(){
            return price;
        }
    
        public String getCurrency(){
            return currency;
        }
        
        public boolean isPremium(){
            return !price.equals("0.00");
        }
    }
    
    public static class Stats{
        private final String downloads;
        private final String reviews;
        private final String rating;
        
        public Stats(){
            this.downloads = null;
            this.reviews = null;
            this.rating = null;
        }
    
        public String getDownloads(){
            return downloads;
        }
    
        public String getReviews(){
            return reviews;
        }
    
        public String getRating(){
            return rating;
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
