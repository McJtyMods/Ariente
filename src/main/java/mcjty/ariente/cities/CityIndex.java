package mcjty.ariente.cities;

public class CityIndex {

    private final int dimX;
    private final int dimZ;
    private final int xOffset;
    private final int zOffset;

    public CityIndex(int dimX, int dimZ, int xOffset, int zOffset) {
        this.dimX = dimX;
        this.dimZ = dimZ;
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    public boolean isCorner() {
        return (xOffset == 0 && zOffset == 0) || (xOffset == 0 && zOffset == dimZ-1) ||
                (xOffset == dimX-1 && zOffset == 0) || (xOffset == dimX-1 && zOffset == dimZ-1);
    }

    public boolean isTopLeft() {
        return xOffset == 0 && zOffset == 0;
    }

    public boolean isTopRight() {
        return xOffset == dimX-1 && zOffset == 0;
    }

    public boolean isBottomLeft() {
        return xOffset == 0 && zOffset == dimZ-1;
    }

    public boolean isBottomRight() {
        return xOffset == dimX-1 && zOffset == dimZ-1;
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimZ() {
        return dimZ;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getZOffset() {
        return zOffset;
    }
}
