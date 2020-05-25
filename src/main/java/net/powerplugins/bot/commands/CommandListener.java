package net.powerplugins.bot.commands;

import com.github.rainestormee.jdacommand.CommandHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.powerplugins.bot.PowerPlugins;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandListener extends ListenerAdapter{
    
    private final ThreadGroup cmdThread = new ThreadGroup("CommandThread");
    private final Executor cmdExecutor = Executors.newCachedThreadPool(
            r -> new Thread(cmdThread, r, "CommandPool")
    );
    
    private final PowerPlugins bot;
    private final CommandHandler<Message> cmdHandler;
    
    public CommandListener(PowerPlugins bot, CommandHandler<Message> cmdHandler){
        this.bot = bot;
        this.cmdHandler = cmdHandler;
    }
    
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        cmdExecutor.execute(
                () -> {
                    if(!bot.isReady())
                        return;
                    
                    Message msg = event.getMessage();
                    Guild guild = event.getGuild();
                    User user = event.getAuthor();
                    
                    if(user.isBot())
                        return;
                    
                    Pattern prefixPattern = Pattern.compile(
                            Pattern.quote(bot.getPrefix()) + "(?<command>[^\\s].+)",
                            Pattern.CASE_INSENSITIVE
                    );
                    
                    String raw = msg.getContentRaw();
    
                    Matcher matcher = prefixPattern.matcher(raw);
                    if(!matcher.matches())
                        return;
    
                    TextChannel tc = event.getChannel();
                    Member self = guild.getSelfMember();
                    
                    if(!self.hasPermission(tc, Permission.MESSAGE_WRITE))
                        return;
                    
                    raw = matcher.group("command");
                    String[] args = Arrays.copyOf(raw.split("\\s+", 2), 2);
                    
                    if(args[0] == null)
                        return;
                    
                    Command command = (Command)cmdHandler.findCommand(args[0].toLowerCase());
                    if(command == null)
                        return;
                    
                    if(!self.hasPermission(tc, Permission.MESSAGE_EMBED_LINKS)){
                        bot.sendMessage(
                                tc,
                                "I lack the permission `%s` to perform commands!",
                                Permission.MESSAGE_EMBED_LINKS.getName()
                        );
                        return;
                    }
                    
                    try{
                        cmdHandler.execute(command, msg, args[1] == null ? "" : args[1]);
                    }catch(Exception ex){
                        bot.getLogger().warning(String.format(
                                "Unable to perform command! %s",
                                ex.getMessage()
                        ));
                        bot.sendMessage(
                                tc,
                                "I am unable to execute a command!\n" +
                                "Reason: `%s`",
                                ex.getMessage()
                        );
                    }
                });
    }
}
