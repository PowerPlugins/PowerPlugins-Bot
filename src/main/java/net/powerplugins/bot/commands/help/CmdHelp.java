package net.powerplugins.bot.commands.help;

import com.github.rainestormee.jdacommand.CommandDescription;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.powerplugins.bot.PowerPlugins;
import net.powerplugins.bot.commands.Command;

import java.util.stream.Collectors;

@CommandDescription(
        name = "Help",
        description = "Shows the commands available",
        triggers = {"help", "h", "commands"}
)
public class CmdHelp implements Command{
    
    private final PowerPlugins bot;
    
    public CmdHelp(PowerPlugins bot){
        this.bot = bot;
    }
    
    @Override
    public void run(Guild guild, TextChannel tc, Message msg, Member member, String... args){
        StringBuilder builder = new StringBuilder();
        for(Command command : bot.getCmdHandler().getCommands().stream().map(c -> (Command)c).collect(Collectors.toList())){
            if(builder.length() + getCommandInfo(command).length() == Message.MAX_CONTENT_LENGTH){
                bot.sendMessage(tc, builder.toString());
                
                builder = new StringBuilder();
                continue;
            }
            
            if(builder.length() > 0)
                builder.append("\n\n");
            
            builder.append(getCommandInfo(command));
        }
        
        if(builder.length() > 0)
            bot.sendMessage(tc, builder.toString());
    }
    
    private String getCommandInfo(Command command){
        return String.format(
                "`%s%s`\n" +
                "%s",
                bot.getPrefix(),
                command.getDescription().name(),
                command.getDescription().description()
        );
    }
}
