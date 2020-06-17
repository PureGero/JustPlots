package just.plots.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    private final String name;
    private final String description;
    private final String[] aliases;

    public SubCommand(String description, String... aliases) {
        this.name = aliases[0];
        this.description = description;
        this.aliases = aliases;
    }

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
}
