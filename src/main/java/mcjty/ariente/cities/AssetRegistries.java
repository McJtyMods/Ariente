package mcjty.ariente.cities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.ariente.Ariente;
import mcjty.ariente.varia.Counter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AssetRegistries {

    public static final AbstractAssetRegistry<BuildingPart> PARTS = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<Palette> PALETTES = new AbstractAssetRegistry<>();
    public static final AbstractAssetRegistry<CityPlan> CITYPLANS = new AbstractAssetRegistry<>();

    public static final void reset() {
        PARTS.reset();
        PALETTES.reset();
        CITYPLANS.reset();
    }

    public static void load(File file) {
        try(FileInputStream in = new FileInputStream(file)) {
            load(in, file.getName());
        } catch (FileNotFoundException e) {
            // Not an error
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void add(Map<Character, Set<String>> map, Character character, String partName) {
        if (!map.containsKey(character)) {
            map.put(character, new HashSet<>());
        }
        map.get(character).add(partName);
    }

    private static void printChars(String prefix, Counter<Character> counter) {
        List<Character> chars = new ArrayList<>(counter.getMap().keySet());
        chars.sort((o1, o2) -> o1.compareTo(o2));
        String s = "";
        for (Character character : chars) {
            s += character;
        }
        Ariente.logger.info(prefix + s);
    }

    private static void printMap(Map<Character, Set<String>> usersPerCharacter, List<Map.Entry<Character, Integer>> map) {
        for (Map.Entry<Character, Integer> entry : map) {
            Set<String> users = usersPerCharacter.get(entry.getKey());
            String s = "    " + entry.getKey() + ": " + entry.getValue() + " Uses";
            if (users.size() < 10) {
                s += ", Used by: ";
                for (String user : users) {
                    s += user + ",";
                }
            } else {
                s += ", Used " + users.size() + " times";
            }
            Ariente.logger.info(s);
        }
    }

    public static void load(InputStream inputstream, String filename) {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            for (JsonElement entry : element.getAsJsonArray()) {
                JsonObject object = entry.getAsJsonObject();
                String type = object.get("type").getAsString();
                if ("palette".equals(type)) {
                    PALETTES.register(new Palette(object));
                } else if ("part".equals(type)) {
                    PARTS.register(new BuildingPart(object));
                } else if ("plan".equals(type)) {
                    CITYPLANS.register(new CityPlan(object));
                } else {
                    throw new RuntimeException("Unknown type '" + type + " in " + filename + "'!");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
