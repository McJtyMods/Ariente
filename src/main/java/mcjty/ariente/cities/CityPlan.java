package mcjty.ariente.cities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityPlan implements IAsset {

    private String name;
    private String palette;
    private Map<Character, List<String>> partPalette = new HashMap<>();
    private List<String> cellar = new ArrayList<>();
    private List<String> plan = new ArrayList<>();
    private List<String> layer2 = new ArrayList<>();
    private int minLayer2 = 1;
    private int maxLayer2 = 2;

    public CityPlan(JsonObject object) {
        readFromJSon(object);
    }

    public CityPlan(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<Character, List<String>> getPartPalette() {
        return partPalette;
    }

    public List<String> getPlan() {
        return plan;
    }

    public List<String> getCellar() {
        return cellar;
    }

    public List<String> getLayer2() {
        return layer2;
    }

    public int getMinLayer2() {
        return minLayer2;
    }

    public int getMaxLayer2() {
        return maxLayer2;
    }

    public String getPalette() {
        return palette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        palette = object.get("palette").getAsString();
        if (object.has("minlayer2")) {
            minLayer2 = object.get("minlayer2").getAsInt();
        }
        if (object.has("maxlayer2")) {
            maxLayer2 = object.get("maxlayer2").getAsInt();
        }
        JsonArray paletteArray = object.get("partpalette").getAsJsonArray();
        parsePaletteArray(paletteArray);

        parsePlan(object, "plan", plan);
        parsePlan(object, "cellar", cellar);
        parsePlan(object, "layer2", layer2);
    }

    private void parsePlan(JsonObject object, String name, List<String> plan) {
        plan.clear();
        if (object.has(name)) {
            JsonArray planArray = object.get(name).getAsJsonArray();
            for (JsonElement element : planArray) {
                String slice = element.getAsString();
                plan.add(slice);
            }
        }
    }

    public void parsePaletteArray(JsonArray paletteArray) {
        for (JsonElement element : paletteArray) {
            JsonObject o = element.getAsJsonObject();
            Object value = null;
            Character c = o.get("char").getAsCharacter();
            if (o.has("parts")) {
                JsonArray array = o.get("parts").getAsJsonArray();

                List<String> parts = new ArrayList<>();
                for (JsonElement el : array) {
                    String part = el.getAsString();
                    parts.add(part);
                }
                partPalette.put(c, parts);
            } else {
                throw new RuntimeException("Illegal palette!");
            }
        }
    }

    public JsonObject writeToJSon() {
        JsonObject object = new JsonObject();
        object.add("type", new JsonPrimitive("plan"));
        object.add("name", new JsonPrimitive(name));
        object.add("palette", new JsonPrimitive(palette));
        object.add("minlayer2", new JsonPrimitive(minLayer2));
        object.add("maxlayer2", new JsonPrimitive(maxLayer2));

        JsonArray array = new JsonArray();
        for (Map.Entry<Character, List<String>> entry : partPalette.entrySet()) {
            JsonObject o = new JsonObject();
            o.add("char", new JsonPrimitive(entry.getKey()));

            JsonArray partArray = new JsonArray();
            for (String part : entry.getValue()) {
                partArray.add(new JsonPrimitive(part));
            }
            o.add("parts", partArray);
            array.add(o);
        }
        object.add("partpalette", array);
        writePlan(object, "plan", plan);
        writePlan(object, "cellar", cellar);
        writePlan(object, "layer2", layer2);

        return object;
    }

    private void writePlan(JsonObject object, String name, List<String> plan) {
        if (!plan.isEmpty()) {
            JsonArray planArray = new JsonArray();
            for (String p : plan) {
                planArray.add(new JsonPrimitive(p));
            }
            object.add(name, planArray);
        }
    }
}
