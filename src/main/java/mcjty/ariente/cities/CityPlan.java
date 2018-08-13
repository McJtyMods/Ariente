package mcjty.ariente.cities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

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
    private List<String> top = new ArrayList<>();
    private int minLayer2 = 1;
    private int maxLayer2 = 2;

    private List<Loot> loot = new ArrayList<>();

    private int minSentinels = 0;
    private int maxSentinels = 0;
    private int sentinelDistance = 20;
    private int sentinelRelHeight = 14;

    private int dronesMinimum1 = 1;
    private int dronesMinimum2 = 1;
    private int dronesMinimumN = 2;
    private int dronesWaveMax1 = 2;
    private int dronesWaveMax2 = 3;
    private int dronesWaveMaxN = 5;

    private double masterChance = 0.0;
    private int soldiersMinimum1 = 1;
    private int soldiersMinimum2 = 1;
    private int soldiersMinimumN = 2;
    private int soldiersWaveMax1 = 2;
    private int soldiersWaveMax2 = 3;
    private int soldiersWaveMaxN = 5;

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

    public List<String> getTop() {
        return top;
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

    public int getMinSentinels() {
        return minSentinels;
    }

    public int getMaxSentinels() {
        return maxSentinels;
    }

    public int getSentinelDistance() {
        return sentinelDistance;
    }

    public int getSentinelRelHeight() {
        return sentinelRelHeight;
    }

    public int getDronesMinimum1() {
        return dronesMinimum1;
    }

    public int getDronesMinimum2() {
        return dronesMinimum2;
    }

    public int getDronesMinimumN() {
        return dronesMinimumN;
    }

    public int getDronesWaveMax1() {
        return dronesWaveMax1;
    }

    public int getDronesWaveMax2() {
        return dronesWaveMax2;
    }

    public int getDronesWaveMaxN() {
        return dronesWaveMaxN;
    }

    public int getSoldiersMinimum1() {
        return soldiersMinimum1;
    }

    public int getSoldiersMinimum2() {
        return soldiersMinimum2;
    }

    public int getSoldiersMinimumN() {
        return soldiersMinimumN;
    }

    public int getSoldiersWaveMax1() {
        return soldiersWaveMax1;
    }

    public int getSoldiersWaveMax2() {
        return soldiersWaveMax2;
    }

    public int getSoldiersWaveMaxN() {
        return soldiersWaveMaxN;
    }

    public double getMasterChance() {
        return masterChance;
    }

    public List<Loot> getLoot() {
        return loot;
    }

    @Override
    public void readFromJSon(JsonObject object) {
        name = object.get("name").getAsString();
        palette = object.get("palette").getAsString();
        minLayer2 = getMin(object, "layers", 1);
        maxLayer2 = getMax(object, "layers", 1);
        minSentinels = getMin(object, "sentinels", 0);
        maxSentinels = getMax(object, "sentinels", 0);
        sentinelDistance = object.get("sentinelDistance").getAsInt();
        sentinelRelHeight = object.get("sentinelRelHeight").getAsInt();
        dronesMinimum1 = object.get("dronesMinimum1").getAsInt();
        dronesMinimum2 = object.get("dronesMinimum2").getAsInt();
        dronesMinimumN = object.get("dronesMinimumN").getAsInt();
        dronesWaveMax1 = object.get("dronesWaveMax1").getAsInt();
        dronesWaveMax2 = object.get("dronesWaveMax2").getAsInt();
        dronesWaveMaxN = object.get("dronesWaveMaxN").getAsInt();
        soldiersMinimum1 = object.get("soldiersMinimum1").getAsInt();
        soldiersMinimum2 = object.get("soldiersMinimum2").getAsInt();
        soldiersMinimumN = object.get("soldiersMinimumN").getAsInt();
        soldiersWaveMax1 = object.get("soldiersWaveMax1").getAsInt();
        soldiersWaveMax2 = object.get("soldiersWaveMax2").getAsInt();
        soldiersWaveMaxN = object.get("soldiersWaveMaxN").getAsInt();
        masterChance = object.get("masterChance").getAsDouble();

        JsonArray lootArray = object.get("loot").getAsJsonArray();
        parseLoot(lootArray);

        JsonArray paletteArray = object.get("partpalette").getAsJsonArray();
        parsePaletteArray(paletteArray);

        parsePlan(object, "plan", plan);
        parsePlan(object, "cellar", cellar);
        parsePlan(object, "layer2", layer2);
        parsePlan(object, "top", top);
    }

    private int getMin(JsonObject object, String tag, int def) {
        if (object.has(tag)) {
            String value = object.get(tag).getAsString();
            String[] split = StringUtils.split(value, '-');
            return Integer.parseInt(split[0]);
        }
        return def;
    }

    private int getMax(JsonObject object, String tag, int def) {
        if (object.has(tag)) {
            String value = object.get(tag).getAsString();
            String[] split = StringUtils.split(value, '-');
            if (split.length > 1) {
                return Integer.parseInt(split[1]);
            } else {
                return Integer.parseInt(split[0]);
            }
        }
        return def;
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

    public void parseLoot(JsonArray lootArray) {
        loot.clear();
        for (JsonElement element : lootArray) {
            JsonObject o = element.getAsJsonObject();
            String id = o.get("id").getAsString();
            int meta = 0;
            if (o.has("meta")) {
                meta = o.get("meta").getAsInt();
            }
            float chance = o.get("chance").getAsFloat();
            int maxAmount = o.get("max").getAsInt();
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null) {
                loot.add(new Loot(new ResourceLocation(id), meta, maxAmount, chance));
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
        object.add("layers", new JsonPrimitive(minLayer2 + "-" + maxLayer2));
        object.add("sentinels", new JsonPrimitive(minSentinels + "-" + maxSentinels));
        object.add("sentinelDistance", new JsonPrimitive(sentinelDistance));
        object.add("sentinelRelHeight", new JsonPrimitive(sentinelRelHeight));
        object.add("dronesMinimum1", new JsonPrimitive(dronesMinimum1));
        object.add("dronesMinimum2", new JsonPrimitive(dronesMinimum2));
        object.add("dronesMinimumN", new JsonPrimitive(dronesMinimumN));
        object.add("dronesWaveMax1", new JsonPrimitive(dronesWaveMax1));
        object.add("dronesWaveMax2", new JsonPrimitive(dronesWaveMax2));
        object.add("dronesWaveMaxN", new JsonPrimitive(dronesWaveMaxN));
        object.add("soldiersMinimum1", new JsonPrimitive(soldiersMinimum1));
        object.add("soldiersMinimum2", new JsonPrimitive(soldiersMinimum2));
        object.add("soldiersMinimumN", new JsonPrimitive(soldiersMinimumN));
        object.add("soldiersWaveMax1", new JsonPrimitive(soldiersWaveMax1));
        object.add("soldiersWaveMax2", new JsonPrimitive(soldiersWaveMax2));
        object.add("soldiersWaveMaxN", new JsonPrimitive(soldiersWaveMaxN));
        object.add("masterChance", new JsonPrimitive(masterChance));

        JsonArray lootArray = new JsonArray();
        for (Loot l : loot) {
            JsonObject o = new JsonObject();
            o.add("id", new JsonPrimitive(l.getId().toString()));
            o.add("meta", new JsonPrimitive(l.getMeta()));
            o.add("max", new JsonPrimitive(l.getMaxAmount()));
            o.add("chance", new JsonPrimitive(l.getChance()));
            lootArray.add(o);
        }
        object.add("loot", lootArray);

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
        writePlan(object, "top", top);

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
