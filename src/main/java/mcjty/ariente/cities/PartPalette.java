package mcjty.ariente.cities;

import java.util.ArrayList;
import java.util.List;

public class PartPalette {

    private List<String> palette = new ArrayList<>();
    private String variant;

    public List<String> getPalette() {
        return palette;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }
}
