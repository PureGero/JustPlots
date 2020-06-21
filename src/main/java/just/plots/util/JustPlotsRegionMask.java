package just.plots.util;

import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.regions.CuboidRegion;
import just.plots.Plot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Links WorldEdit Masks with their assigned Plot
 */
public class JustPlotsRegionMask extends RegionMask {

    /**
     * The plot that this mask was assigned to
     */
    private final Plot plot;

    /**
     * Constructs a new RegionMask for a certain Plot
     * @param cuboidRegion The region for this mask
     * @param plot The plot for this mask
     */
    public JustPlotsRegionMask(@NotNull CuboidRegion cuboidRegion, @Nullable Plot plot) {
        super(cuboidRegion);
        this.plot = plot;
    }

    /**
     * Get the plot that this mask was assigned to
     * @return The plot for this mask, or null if this mask is assigned no plot
     */
    @Nullable
    public Plot getPlot() {
        return plot;
    }
}
