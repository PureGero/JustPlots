package just.plots.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    private final String usage;
    private final String name;
    private final String description;
    private final String[] aliases;

    public SubCommand(String usage, String description, String... aliases) {
        this.usage = usage;
        this.name = aliases[0];
        this.description = description;
        this.aliases = aliases;
    }

    /**
     * This will be run async, make sure to be thread safe!
     */
    public abstract boolean onCommand(CommandSender sender, String label, String[] args);

    public abstract void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion);

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getUsage() {
        return usage;
    }
}
