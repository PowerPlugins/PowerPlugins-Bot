package net.powerplugins.bot.commands;

import com.github.rainestormee.jdacommand.AbstractCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public interface Command extends AbstractCommand<Message>{
    
    @Override
    default void execute(Message message, String s){
        String[] args = s.isEmpty() ? new String[0] : s.split("\\s+");
        
        if(message == null)
            return;
        
        run(message.getGuild(), message.getTextChannel(), message, message.getMember(), args);
    }
    
    void run(Guild guild, TextChannel tc, Message msg, Member member, String... args);
}
