package mcjty.ariente.cities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.ariente.varia.Tools;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements IAsset {

    private String name;
    private final Map<PaletteIndex, Object> palette = new HashMap<>();

    public Palette() {
    }

    public Palette(JsonObject object) {
        readFromJSon(object);
    }

    public Palette(String name) {
        this.name = name;
    }

    public void merge(Palette other) {
        palette.putAll(other.palette);
    }

    public void optimize(Set<PaletteIndex> paletteUsage) {
        Set<PaletteIndex> characters = new HashSet<>(palette.keySet());
        for (PaletteIndex character : characters) {
            if (!paletteUsage.contains(character)) {
                System.out.println("Possibly remove character = " + character);
//                palette.remove(character);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<PaletteIndex, Object> getPalette() {
        return palette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        JsonArray paletteArray = object.get("palette").getAsJsonArray();
        parsePaletteArray(paletteArray);
    }

    public void parsePaletteArray(JsonArray paletteArray) {
        for (JsonElement element : paletteArray) {
            JsonObject o = element.getAsJsonObject();
            Object value = null;
            String cidx = o.get("char").getAsString();
            PaletteIndex c = cidx.length() > 1 ? new PaletteIndex(cidx.charAt(0), cidx.charAt(1)) :
                    new PaletteIndex(cidx.charAt(0), ' ');
            if (o.has("block")) {
                String block = o.get("block").getAsString();
                IBlockState state = Tools.stringToState(block);
                palette.put(c, state);
            } else if (o.has("blocks")) {
                JsonArray array = o.get("blocks").getAsJsonArray();
                List<Pair<Integer, IBlockState>> blocks = new ArrayList<>();
                for (JsonElement el : array) {
                    JsonObject ob = el.getAsJsonObject();
                    Integer f = ob.get("random").getAsInt();
                    String block = ob.get("block").getAsString();
                    IBlockState state = Tools.stringToState(block);
                    blocks.add(Pair.of(f, state));
                }
                addMappingViaState(c, blocks.toArray(new Pair[blocks.size()]));
            } else {
                throw new RuntimeException("Illegal palette!");
            }
        }
    }

    private void getOrientation(Map<String, Integer> or, JsonObject torchObj, String orientation) {
        if (torchObj.has(orientation)) {
            or.put(orientation, torchObj.get(orientation).getAsInt());
        } else {
            or.put(orientation, 0);
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("palette"));
        object.add("name", new JsonPrimitive(name));
        JsonArray array = new JsonArray();
        for (Map.Entry<PaletteIndex, Object> entry : palette.entrySet()) {
            JsonObject o = new JsonObject();
            PaletteIndex idx = entry.getKey();
            o.add("char", new JsonPrimitive(String.valueOf(idx.getI1()) + idx.getI2()));
            if (entry.getValue() instanceof IBlockState) {
                IBlockState state = (IBlockState) entry.getValue();
                o.add("block", new JsonPrimitive(Tools.stateToString(state)));
            }
            array.add(o);
        }
        object.add("palette", array);
        return object;
    }

    public Palette addMapping(PaletteIndex c, IBlockState state) {
        palette.put(c, state);
        return this;
    }

    @SafeVarargs
    private final Palette addMappingViaState(PaletteIndex c, Pair<Integer, IBlockState>... randomBlocks) {
        palette.put(c, randomBlocks);
        return this;
    }
}
