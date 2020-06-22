package net.justminecraft.plots;

public class PlotId {
    private final int x;
    private final int z;

    public PlotId(String id) throws IndexOutOfBoundsException, NumberFormatException {
        String[] parts = id.split(";");

        this.x = Integer.parseInt(parts[0]);
        this.z = Integer.parseInt(parts[1]);
    }

    public PlotId(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PlotId && ((PlotId) other).x == x && ((PlotId) other).z == z;
    }

    @Override
    public int hashCode() {
        return (z << 16) ^ x;
    }

    @Override
    public String toString() {
        return x + ";" + z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
