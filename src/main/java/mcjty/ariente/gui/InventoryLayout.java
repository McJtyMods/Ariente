package mcjty.ariente.gui;

/** Shared slot geometry for storage cubes and machine player inventories. */
public class InventoryLayout {

    public static final double CELL = .8;
    public static final double GRID_X = .4;
    public static final double PLAYER_Y = 5.05;
    public static final double HOTBAR_Y = PLAYER_Y + 3 * CELL + .3;
    public static final double WIDTH = 9 * CELL;
    public static final double HEIGHT = HOTBAR_Y + CELL - PLAYER_Y;

    public static int playerSlotAt(double x, double y) {
        int column = (int) Math.floor((x - GRID_X) / CELL);
        if (column < 0 || column >= 9) {
            return -1;
        }
        int row = (int) Math.floor((y - PLAYER_Y) / CELL);
        if (row >= 0 && row < 3) {
            return 9 + row * 9 + column;
        }
        if (y >= HOTBAR_Y && y < HOTBAR_Y + CELL) {
            return column;
        }
        return -1;
    }
}
