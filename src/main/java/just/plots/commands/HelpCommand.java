package just.plots.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends SubCommand {

    private List<SubCommand> commands = new ArrayList<>();

    public HelpCommand() {
        super("List all commands", "help");
    }

    public void addCommand(SubCommand command) {
        commands.add(command);
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        int pages = (int) Math.ceil(commands.size() / 8.0);
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
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextPage))
                    .append(" to view the next page").reset().color(ChatColor.GRAY).create());
        } else {
            sender.sendMessage(ChatColor.GRAY + "This is the last page");
        }

        for (int i = page * 8 - 8; i < page * 8 && i < commands.size(); i++) {
            SubCommand command = commands.get(i);
            String usage = "/p " + command.getName();

            sender.spigot().sendMessage(new ComponentBuilder(usage).color(ChatColor.AQUA)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(usage).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, usage))
                    .append(": " + command.getDescription()).reset().create());
        }

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        int pages = commands.size() / 8;

        if (args.length >= 1) {
            for (int i = 1; i <= pages; i++) {
                if (Integer.toString(i).startsWith(args[0])) {
                    tabCompletion.add(Integer.toString(i));
                }
            }
        }
    }

}