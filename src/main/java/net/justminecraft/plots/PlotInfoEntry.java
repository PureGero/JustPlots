package net.justminecraft.plots;

import net.justminecraft.plots.commands.InfoCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An entry of the /plot info
 */
public abstract class PlotInfoEntry {

    private String key;

    /**
     * Create an entry into /plot info
     * @param key A human readable key for this entry
     */
    public PlotInfoEntry(@NotNull String key) {
        this.key = key;

        InfoCommand.addEntry(this);
    }

    /**
     * Get the value of this entry for a certain plot
     * @param plot The plot to get the value of
     * @return A human readable value, or null to not show the entry
     */
    @Nullable
    public abstract BaseComponent[] getValue(@NotNull Plot plot);

    /**
     * Get the key for this entry
     * @return A human readable key for this entry
     */
    @NotNull
    public String getKey() {
        return key;
    }
}
