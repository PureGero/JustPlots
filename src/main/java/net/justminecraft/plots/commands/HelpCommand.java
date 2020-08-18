package net.justminecraft.plots.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends SubCommand {

    private List<SubCommand> commands = new ArrayList<>();

    public HelpCommand() {
        super("/p help [page]", "List all commands", "help");
    }

    public void addCommand(SubCommand command) {
        commands.add(command);
    }

    private List<SubCommand> getCommandsFor(Permissible permissible) {
        List<SubCommand> commandList = new ArrayList<>();

        for (SubCommand command : commands) {
            if (command.getPermission() == null || permissible.hasPermission(command.getPermission())) {
                commandList.add(command);
            }
        }

        return commandList;
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        List<SubCommand> commandList = getCommandsFor(sender);

        int pages = (int) Math.ceil(commandList.size() / 8.0);
        int page = 1;

        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1 || page > pages) {
                    sender.sendMessage(ChatColor.RED + "Invalid help page number '" + page + "'");
                    return false;
                }
            } catch (NumberFormatException ignore) {}
        }

        sender.sendMessage(ChatColor.AQUA + "--------- " + ChatColor.WHITE + "Help: JustPlots (" + page + "/" + pages + ")" + ChatColor.AQUA + " -------------------");

        if (page < pages) {
            String nextPage = "/p " + (label.isEmpty() ? "" : label + " ") + (page + 1);

            sender.spigot().sendMessage(new ComponentBuilder("Use ").color(ChatColor.GRAY).append(nextPage)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(nextPage).create()))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, nextPage))
                    .append(" to view the next page").reset().color(ChatColor.GRAY).create());
        } else {
            sender.sendMessage(ChatColor.GRAY + "This is the last page");
        }

        for (int i = page * 8 - 8; i < page * 8 && i < commandList.size(); i++) {
            SubCommand command = commandList.get(i);
            String usage = command.getUsage();

            ComponentBuilder commandText = new ComponentBuilder();

            if (command.isCustomCommand()) {
                commandText.append("* ").color(ChatColor.GOLD).bold(true);
            }

            commandText.append(usage).color(ChatColor.AQUA).bold(false)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(usage).create()))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
                    .append(": " + command.getDescription()).reset();

            sender.spigot().sendMessage(commandText.create());
        }

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        List<SubCommand> commandList = getCommandsFor(sender);

        int pages = commandList.size() / 8;

        if (args.length >= 1) {
            for (int i = 1; i <= pages + 1; i++) {
                if (Integer.toString(i).startsWith(args[0])) {
                    tabCompletion.add(Integer.toString(i));
                }
            }
        }
    }

}
