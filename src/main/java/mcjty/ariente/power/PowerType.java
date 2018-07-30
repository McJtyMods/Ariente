package mcjty.ariente.power;

import mcjty.ariente.cables.CableColor;

public enum PowerType {
    NEGARITE,
    POSIRITE;

    CableColor getCableColor() {
        switch (this) {
            case NEGARITE:
                return CableColor.NEGARITE;
            case POSIRITE:
                return CableColor.POSIRITE;
        }
        throw new IllegalStateException("Cannot happen!");
    }

    public boolean supportsPowerType(CableColor color) {
        if (color == CableColor.COMBINED) {
            return true;
        }
        switch (this) {
            case NEGARITE:
                return color == CableColor.NEGARITE;
            case POSIRITE:
                return color == CableColor.POSIRITE;
        }
        throw new IllegalStateException("Cannot happen!");
    }
}
