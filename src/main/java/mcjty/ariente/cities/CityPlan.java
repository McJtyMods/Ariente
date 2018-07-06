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
    private List<String> plan = new ArrayList<>();

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

    public String getPalette() {
        return palette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        palette = object.get("palette").getAsString();
        JsonArray paletteArray = object.get("partpalette").getAsJsonArray();
        parsePaletteArray(paletteArray);
        JsonArray planArray = object.get("plan").getAsJsonArray();
        for (JsonElement element : planArray) {
            String slice = element.getAsString();
            plan.add(slice);
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

        JsonArray planArray = new JsonArray();
        for (String p : plan) {
            planArray.add(new JsonPrimitive(p));
        }
        object.add("plan", planArray);

        return object;
    }
}
