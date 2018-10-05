package mcjty.ariente.cities;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * More efficient representation of a palette useful for a single chunk
 */
public class CompiledPalette {

    private final Map<PaletteIndex, Character> palette = new HashMap<>();

    private static final Map<String, CompiledPalette> compiledPaletteMap = new HashMap<>();

    public static CompiledPalette getCompiledPalette(String name) {
        if (!compiledPaletteMap.containsKey(name)) {
            compiledPaletteMap.put(name, new CompiledPalette(AssetRegistries.PALETTES.get(name)));
        }
        return compiledPaletteMap.get(name);
    }

    public static CompiledPalette getNewCompiledPalette(String name) {
        compiledPaletteMap.remove(name);
        return getCompiledPalette(name);
    }

    public CompiledPalette(CompiledPalette other, Palette... palettes) {
        this.palette.putAll(other.palette);
        addPalettes(palettes);
    }

    public CompiledPalette(Palette... palettes) {
        addPalettes(palettes);
    }

    public void addPalettes(Palette[] palettes) {
        // First add the straight palette entries
        for (Palette p : palettes) {
            for (Map.Entry<PaletteIndex, IBlockState> entry : p.getPalette().entrySet()) {
                IBlockState value = entry.getValue();
                palette.put(entry.getKey(), (char) Block.BLOCK_STATE_IDS.get((value)));
            }
        }
    }

    public Set<PaletteIndex> getCharacters() {
        return palette.keySet();
    }

    public IBlockState getStraight(PaletteIndex c) {
        try {
            Character o = palette.get(c);
            return Block.BLOCK_STATE_IDS.getByValue((Character) o);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Character get(PaletteIndex c) {
        return palette.get(c);
    }
}
