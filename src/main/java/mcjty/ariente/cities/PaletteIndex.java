package mcjty.ariente.cities;

import java.util.Objects;

public class PaletteIndex {
    private final char i1;
    private final char i2;

    public PaletteIndex(char i1, char i2) {
        this.i1 = i1;
        this.i2 = i2;
    }

    public char getI1() {
        return i1;
    }

    public char getI2() {
        return i2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaletteIndex that = (PaletteIndex) o;
        return i1 == that.i1 &&
                i2 == that.i2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i1, i2);
    }

    @Override
    public String toString() {
        return "IDX{" + i1 + i2 + '}';
    }
}
