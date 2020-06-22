package net.justminecraft.plots.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    private final String usage;
    private final String name;
    private final String description;
    private final String[] aliases;

    /**
     * Whether this command has been added by an external plugin or not
     */
    private boolean customCommand = false;

    /**
     * A command to be run by the /plot command
     * @param usage The usage of this command (eg {@code "/p add <player>"}
     * @param description The description of this command
     * @param aliases The name of this command, and any aliases
     */
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

    public String getPermission() {
        return null;
    }

    /**
     * Whether this command has been added by an external plugin or not
     * @return Returns true if this command has been added by an external plugin
     */
    public final boolean isCustomCommand() {
        return customCommand;
    }

    final void setCustomCommand(boolean customCommand) {
        this.customCommand = customCommand;
    }
}
