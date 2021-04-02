package net.justminecraft.plots.commands;

import net.justminecraft.plots.JustPlots;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JustPlotsCommand implements CommandExecutor, TabCompleter {

    private final JustPlots plots;
    private HashMap<String, SubCommand> commands = new HashMap<>();
    private HelpCommand helpCommand = new HelpCommand();
    private boolean addCustomCommands;

    public JustPlotsCommand(JustPlots plots) {
        this.plots = plots;

        PluginCommand justPlots = plots.getCommand("p");

        if (justPlots == null) {
            throw new RuntimeException("Could not find command /justplots (is it registered in the plugin.yml?)");
        }

        justPlots.setExecutor(this);
        justPlots.setTabCompleter(this);

        // Add the commands in the order they will appear in /p help
        addCommand(new InfoCommand());
        addCommand(new ListCommand());
        addCommand(new ClaimCommand());
        addCommand(new AutoCommand());
        addCommand(new VisitCommand());
        addCommand(new AddCommand());
        addCommand(new RemoveCommand());
        addCommand(new DenyCommand());
        addCommand(new UnDenyCommand());
        addCommand(new ClearCommand());
        addCommand(new ResetCommand());
        addCommand(new DisposeCommand());
        addCommand(new MiddleCommand());
        addCommand(new WeanywhereCommand());
        addCommand(new PluginInfoCommand());
        addCommand(helpCommand);

        // Any future commands added will be marked as custom commands
        addCustomCommands = true;
    }

    /**
     * Add a sub command to /plot
     * @param command The command to add
     */
    public void addCommand(@NotNull SubCommand command) {
        command.setCustomCommand(addCustomCommands);

        for (String alias : command.getAliases()) {
            commands.put(alias, command);
        }

        helpCommand.addCommand(command);
    }

    /**
     * Get a sub command of /plot
     * @param alias The name or an alias of this command
     * @return The command, or null if not found
     */
    @Nullable
    public SubCommand getCommand(@NotNull String alias) {
        return commands.get(alias);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 1) {
            SubCommand subCommand = getCommand(args[0].toLowerCase());

            if (subCommand != null) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                Bukkit.getScheduler().runTaskAsynchronously(plots, () -> {
                    try {
                        subCommand.onCommand(sender, args[0], newArgs);
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Unhandled expection: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                return true;
            }
        }

        return helpCommand.onCommand(sender, "", args);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> tabCompletion = new ArrayList<>();

        if (args.length >= 2) {
            SubCommand subCommand = commands.get(args[0].toLowerCase());

            if (subCommand != null) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                subCommand.onTabComplete(sender, newArgs, tabCompletion);
            }

        } else if (args.length == 1) {
            commands.forEach((key, subCommand) -> {
                if ((subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))
                        && key.startsWith(args[0].toLowerCase())) {
                    tabCompletion.add(key);
                }
            });
        }

        return tabCompletion;
    }
}
