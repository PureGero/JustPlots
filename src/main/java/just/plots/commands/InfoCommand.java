package just.plots.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public class InfoCommand extends SubCommand {

    public InfoCommand() {
        super("Get info about the plot", "info", "i");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        return false;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
        // Do nothing, /p info takes no arguments
    }

}
