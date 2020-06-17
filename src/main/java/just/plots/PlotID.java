package just.plots;

public class PlotID {
    private final int x;
    private final int z;

    public PlotID(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PlotID && ((PlotID) other).x == x && ((PlotID) other).z == z;
    }

    @Override
    public int hashCode() {
        return (z << 16) ^ x;
    }

    @Override
    public String toString() {
        return x + ";" + z;
    }
}
