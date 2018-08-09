package mcjty.ariente.cities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.ariente.varia.Counter;
import mcjty.ariente.varia.Tools;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements IAsset {

    private String name;
    final Map<Character, Object> palette = new HashMap<>();

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

    public void optimize(Counter<Character> paletteUsage) {
        Set<Character> characters = new HashSet<>(palette.keySet());
        for (Character character : characters) {
            if (paletteUsage.get(character) == 0) {
                palette.remove(character);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<Character, Object> getPalette() {
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
            Character c = o.get("char").getAsCharacter();
            if (o.has("block")) {
                String block = o.get("block").getAsString();
                IBlockState state = Tools.stringToState(block);
                palette.put(c, state);
            } else if (o.has("frompalette")) {
                value = o.get("frompalette").getAsString();
                palette.put(c, value);
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
        for (Map.Entry<Character, Object> entry : palette.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("char", new JsonPrimitive(entry.getKey()));
            if (entry.getValue() instanceof IBlockState) {
                IBlockState state = (IBlockState) entry.getValue();
                o.add("block", new JsonPrimitive(Tools.stateToString(state)));
            } else if (entry.getValue() instanceof String) {
                o.add("frompalette", new JsonPrimitive((String) entry.getValue()));
            } else {
                o.add("test", new JsonPrimitive("@todo"));
            }
            array.add(o);
        }
        object.add("palette", array);
        return object;
    }

    public Palette addMapping(char c, IBlockState state) {
        palette.put(c, state);
        return this;
    }

    @SafeVarargs
    private final Palette addMappingViaState(char c, Pair<Integer, IBlockState>... randomBlocks) {
        palette.put(c, randomBlocks);
        return this;
    }
}
