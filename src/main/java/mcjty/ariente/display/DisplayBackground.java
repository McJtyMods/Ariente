package mcjty.ariente.display;

public enum DisplayBackground {
    INVISIBLE("Invisible"),
    TRANSPARENT("Transparent"),
    SOLID("Solid");

    private final String title;

    DisplayBackground(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public DisplayBackground next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static DisplayBackground from(int index) {
        return values()[Math.floorMod(index, values().length)];
    }
}
