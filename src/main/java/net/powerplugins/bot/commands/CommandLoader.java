package net.powerplugins.bot.commands;

import net.powerplugins.bot.PowerPlugins;
import net.powerplugins.bot.commands.help.CmdHelp;
import net.powerplugins.bot.commands.server.CmdPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandLoader{
    
    private final Set<Command> commands = new HashSet<>();
    
    public CommandLoader(PowerPlugins bot){
        loadCommands(
                new CmdHelp(bot),
                new CmdPlugin(bot)
        );
    }
    
    private void loadCommands(Command... commands){
        this.commands.addAll(Arrays.asList(commands));
    }
    
    public Set<Command> getCommands(){
        return commands;
    }
}
