package mcjty.ariente.cities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * A structure part
 */
public class BuildingPart implements IAsset {

    private String name;

    // Data per height level
    private PalettedSlice[] slices;

    // Dimension (should be less then 16x16)
    private int xSize;
    private int zSize;

    // Optimized version of this part which is organized in xSize*ySize vertical strings
    private PalettedSlice[] vslices = null;

    private Map<BlockPos, Map<String, Object>> teInfo = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();

    public BuildingPart(JsonObject object) {
        readFromJSon(object);
    }

    public BuildingPart(String name, int xSize, int zSize, PalettedSlice[] slices, Map<BlockPos, Map<String, Object>> teData) {
        this.name = name;
        this.slices = slices;
        this.xSize = xSize;
        this.zSize = zSize;
        for (Map.Entry<BlockPos, Map<String, Object>> entry : teData.entrySet()) {
            teInfo.put(entry.getKey(), entry.getValue());
        }
    }

    public Character getMetaChar(String key) {
        return (Character) metadata.get(key);
    }

    public Integer getMetaInteger(String key) {
        return (Integer) metadata.get(key);
    }

    public boolean getMetaBoolean(String key) {
        Object o = metadata.get(key);
        return o instanceof Boolean ? (Boolean) o : false;
    }
    public Float getMetaFloat(String key) {
        return (Float) metadata.get(key);
    }
    public String getMetaString(String key) {
        return (String) metadata.get(key);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<BlockPos, Map<String, Object>> getTeInfo() {
        return teInfo;
    }

    /**
     * Vertical slices, organized by z*xSize+x
     */
    public PalettedSlice[] getVslices() {
        if (vslices == null) {
            vslices = new PalettedSlice[xSize * zSize];
            for (int x = 0 ; x < xSize ; x++) {
                for (int z = 0 ; z < zSize ; z++) {
                    PalettedSlice vs = new PalettedSlice();
                    boolean empty = true;
                    for (int y = 0; y < slices.length; y++) {
                        PaletteIndex c = getC(x, y, z);
                        vs.getSlice().add(c);
                    }
                    // @todo: allow empty slices for other types of parts?
//                    if (empty) {
//                        vslices[z*xSize+x] = null;
//                    } else {
                        vslices[z*xSize+x] = vs;
//                    }
                }
            }
        }
        return vslices;
    }

    public PalettedSlice getVSlice(int x, int z) {
        return getVslices()[z*xSize + x];
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        xSize = object.get("xsize").getAsInt();
        zSize = object.get("zsize").getAsInt();
        JsonArray sliceArray = object.get("slices").getAsJsonArray();
        slices = new PalettedSlice[sliceArray.size()];
        int i = 0;
        for (JsonElement element : sliceArray) {
            JsonArray a = element.getAsJsonArray();
            PalettedSlice slice = new PalettedSlice();
            for (JsonElement el : a) {
                String line = el.getAsString();
                if (line.length() < xSize * 2) {
                    // Old format
                    for (int j = 0 ; j < xSize ; j++) {
                        slice.getSlice().add(new PaletteIndex(line.charAt(j), ' '));
                    }
                } else {
                    for (int j = 0 ; j < xSize ; j++) {
                        slice.getSlice().add(new PaletteIndex(line.charAt(j * 2), line.charAt(j * 2 + 1)));
                    }
                }
            }
            slices[i++] = slice;
        }

        if (object.has("meta")) {
            JsonArray metaArray = object.get("meta").getAsJsonArray();
            for (JsonElement element : metaArray) {
                JsonObject o = element.getAsJsonObject();
                String key = o.get("key").getAsString();
                if (o.has("integer")) {
                    metadata.put(key, o.get("integer").getAsInt());
                } else if (o.has("float")) {
                    metadata.put(key, o.get("float").getAsFloat());
                } else if (o.has("boolean")) {
                    metadata.put(key, o.get("boolean").getAsBoolean());
                } else if (o.has("char")) {
                    metadata.put(key, o.get("char").getAsCharacter());
                } else if (o.has("character")) {
                    metadata.put(key, o.get("character").getAsCharacter());
                } else if (o.has("string")) {
                    metadata.put(key, o.get("string").getAsString());
                }
            }
        }
        if (object.has("tedata")) {
            JsonArray dataArray = object.get("tedata").getAsJsonArray();
            for (JsonElement element : dataArray) {
                JsonObject o = element.getAsJsonObject();
                Integer x = o.get("x").getAsInt();
                Integer y = o.get("y").getAsInt();
                Integer z = o.get("z").getAsInt();
                BlockPos key = new BlockPos(x, y, z);
                Map<String, Object> valueMap = new HashMap<>();
                JsonArray values = o.get("values").getAsJsonArray();
                for (JsonElement el : values) {
                    JsonObject value = el.getAsJsonObject();
                    String k = value.get("key").getAsString();
                    if (value.has("integer")) {
                        valueMap.put(k, value.get("integer").getAsInt());
                    } else if (value.has("float")) {
                        valueMap.put(k, value.get("float").getAsFloat());
                    } else if (value.has("boolean")) {
                        valueMap.put(k, value.get("boolean").getAsBoolean());
                    } else if (value.has("char")) {
                        valueMap.put(k, value.get("char").getAsCharacter());
                    } else if (value.has("character")) {
                        valueMap.put(k, value.get("character").getAsCharacter());
                    } else if (value.has("string")) {
                        valueMap.put(k, value.get("string").getAsString());
                    }
                }
                teInfo.put(key, valueMap);
            }
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("part"));
        object.add("name", new JsonPrimitive(name));
        object.add("xsize", new JsonPrimitive(xSize));
        object.add("zsize", new JsonPrimitive(zSize));
        JsonArray sliceArray = new JsonArray();
        for (PalettedSlice slice : slices) {
            List<PaletteIndex> list = slice.getSlice();
            JsonArray a = new JsonArray();
            while (!list.isEmpty()) {
                StringBuilder l = new StringBuilder();
                List<PaletteIndex> sub = list.subList(0, xSize);
                for (PaletteIndex idx : sub) {
                    l.append(idx.getI1());
                    l.append(idx.getI2());
                }
                a.add(new JsonPrimitive(l.toString()));

                list = list.subList(xSize, list.size());
            }
            sliceArray.add(a);
        }
        /*
                    while (!slice.isEmpty()) {
                String left = StringUtils.left(slice, xSize);
                a.add(new JsonPrimitive(left));
                slice = slice.substring(left.length());

         */
        object.add("slices", sliceArray);

        JsonArray metaArray = new JsonArray();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("key", new JsonPrimitive(entry.getKey()));
            Object v = entry.getValue();
            if (v instanceof Integer) {
                o.add("integer", new JsonPrimitive((Integer) v));
            } else if (v instanceof Float) {
                o.add("float", new JsonPrimitive((Float) v));
            } else if (v instanceof Boolean) {
                o.add("boolean", new JsonPrimitive((Boolean) v));
            } else if (v instanceof String) {
                o.add("string", new JsonPrimitive((String) v));
            }
            metaArray.add(o);
        }
        object.add("meta", metaArray);

        JsonArray dataArray = new JsonArray();
        for (Map.Entry<BlockPos, Map<String, Object>> entry : teInfo.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("x", new JsonPrimitive(entry.getKey().getX()));
            o.add("y", new JsonPrimitive(entry.getKey().getY()));
            o.add("z", new JsonPrimitive(entry.getKey().getZ()));
            JsonArray values = new JsonArray();
            for (Map.Entry<String, Object> objectEntry : entry.getValue().entrySet()) {
                JsonObject value = new JsonObject();
                value.add("key", new JsonPrimitive(objectEntry.getKey()));
                Object v = objectEntry.getValue();
                if (v instanceof Integer) {
                    value.add("integer", new JsonPrimitive((Integer) v));
                } else if (v instanceof Float) {
                    value.add("float", new JsonPrimitive((Float) v));
                } else if (v instanceof Boolean) {
                    value.add("boolean", new JsonPrimitive((Boolean) v));
                } else if (v instanceof String) {
                    value.add("string", new JsonPrimitive((String) v));
                }
                values.add(value);
            }
            o.add("values", values);
            dataArray.add(o);
        }
        object.add("tedata", dataArray);

        return object;
    }

    public int getSliceCount() {
        return slices.length;
    }

    public PalettedSlice getSlice(int i) {
        return slices[i];
    }

    public PalettedSlice[] getSlices() {
        return slices;
    }

    public int getXSize() {
        return xSize;
    }

    public int getZSize() {
        return zSize;
    }

    public PaletteIndex getPaletteChar(int x, int y, int z) {
        return slices[y].getSlice().get(z * xSize + x);
    }

    public Character get(CompiledPalette palette, int x, int y, int z) {
        return palette.get(slices[y].getSlice().get(z * xSize + x));
    }

    public PaletteIndex getC(int x, int y, int z) {
        return slices[y].getSlice().get(z * xSize + x);
    }

    public static class PalettedSlice {
        private final List<PaletteIndex> slice = new ArrayList<>();

        public PalettedSlice() {
        }

        public PalettedSlice(PaletteIndex[] slice) {
            Collections.addAll(this.slice, slice);
        }

        public List<PaletteIndex> getSlice() {
            return slice;
        }
    }
}
