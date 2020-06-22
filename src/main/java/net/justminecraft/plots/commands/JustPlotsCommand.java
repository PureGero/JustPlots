package net.justminecraft.plots.commands;

import net.justminecraft.plots.JustPlots;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JustPlotsCommand implements CommandExecutor, TabCompleter {

    private final JustPlots plots;
    private HashMap<String, SubCommand> commands = new HashMap<>();
    private HelpCommand helpCommand = new HelpCommand();

    public JustPlotsCommand(JustPlots plots) {
        this.plots = plots;

        plots.getCommand("justplots").setExecutor(this);
        plots.getCommand("justplots").setTabCompleter(this);

        // Add the commands in the order they will appear in /p help
        addCommand(new InfoCommand());
        addCommand(new ListCommand());
        addCommand(new ClaimCommand());
        addCommand(new AutoCommand());
        addCommand(new VisitCommand());
        addCommand(new AddCommand());
        addCommand(new RemoveCommand());
        addCommand(new ClearCommand());
        addCommand(new ResetCommand());
        addCommand(new DisposeCommand());
        addCommand(new MiddleCommand());
        addCommand(new WeanywhereCommand());
        addCommand(helpCommand);
    }

    private void addCommand(SubCommand command) {
        for (String alias : command.getAliases()) {
            commands.put(alias, command);
        }

        helpCommand.addCommand(command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            SubCommand subCommand = commands.get(args[0].toLowerCase());

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
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> tabCompletion = new ArrayList<>();

        if (args.length >= 2) {
            SubCommand subCommand = commands.get(args[0].toLowerCase());

            if (subCommand != null) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                subCommand.onTabComplete(sender, newArgs, tabCompletion);
            }

        } else if (args.length == 1) {
            for (String key : commands.keySet()) {
                if (key.startsWith(args[0].toLowerCase())) {
                    tabCompletion.add(key);
                }
            }
        }

        return tabCompletion;
    }
}
