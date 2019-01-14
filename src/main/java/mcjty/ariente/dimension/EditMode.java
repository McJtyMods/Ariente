package mcjty.ariente.dimension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import mcjty.ariente.ai.CityAI;
import mcjty.ariente.ai.CityAISystem;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.*;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class EditMode {

    public static final PaletteIndex PALETTE_AIR = new PaletteIndex(' ', ' ');
    public static boolean editMode = false;

    public static void setVariant(EntityPlayer player, String variant) {
        City city = getCurrentCity(player);
        if (city == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No city!"), false);
            return;
        }
        BlockPos pos = player.getPosition();
        int cx = (pos.getX() >> 4);
        int cz = (pos.getZ() >> 4);

        PartPalette found = getCurrentPartPalette(player, city, pos, cx, cz);

        if (found == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No part palette!"), false);
            return;
        }

        found.setVariant(variant);

        List<String> palette = found.getPalette();
        Map<String, Integer> variants = city.getPlan().getVariants();
        int count = variants.getOrDefault(variant, 0);
        String firstPartName = palette.get(0);
        BuildingPart firstPart = AssetRegistries.PARTS.get(firstPartName);
        while (palette.size() < count) {
            String newpartname = findValidNewPartNameBasedOnOther(firstPartName, palette.size());
            BuildingPart copy = firstPart.createCopy(newpartname);
            AssetRegistries.PARTS.put(newpartname, copy);
            player.sendStatusMessage(new TextComponentString("Created part: " + newpartname), false);
            palette.add(newpartname);
        }

        player.sendStatusMessage(new TextComponentString("Variant set to: " + variant), false);
        updateCity(player, city);
    }

    private static String findValidNewPartNameBasedOnOther(String oldname, int number) {
        if (oldname.contains("@")) {
            oldname = oldname.substring(0, oldname.indexOf("@"));
        }
        String newpartName = oldname + "@" + number;
        while (AssetRegistries.PARTS.get(newpartName) != null) {
            number++;
            newpartName = oldname + "@" + number;
        }
        return newpartName;
    }

    public static void getVariant(EntityPlayer player) {
        City city = getCurrentCity(player);
        if (city == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No city!"), false);
            return;
        }
        BlockPos pos = player.getPosition();
        int cx = (pos.getX() >> 4);
        int cz = (pos.getZ() >> 4);

        PartPalette found = getCurrentPartPalette(player, city, pos, cx, cz);

        if (found == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No part palette!"), false);
            return;
        }

        player.sendStatusMessage(new TextComponentString("Variant is: " + found.getVariant()), false);

    }

    public static void listVariants(EntityPlayer player) {
        City city = getCurrentCity(player);
        if (city == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No city!"), false);
            return;
        }

        Map<String, Integer> baseVariants = city.getPlan().getVariants();
        Map<String, Integer> variantSelections = CityTools.getVariantSelections(city.getCenter());
        for (Map.Entry<String, Integer> entry : variantSelections.entrySet()) {
            Integer maxCount = baseVariants.get(entry.getKey());
            player.sendStatusMessage(new TextComponentString("Variant: " + entry.getKey() + " (" + entry.getValue() + "/" + maxCount + ")"), false);
        }
    }

    public static void createVariant(EntityPlayer player, String variant, String maxS) {
        Integer max = Integer.parseInt(maxS);

        City city = getCurrentCity(player);
        if (city == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No city!"), false);
            return;
        }

        Map<String, Integer> variants = city.getPlan().getVariants();
        Map<String, Integer> variantSelections = CityTools.getVariantSelections(city.getCenter());
        variants.put(variant, max);
        variantSelections.put(variant, 0);
    }

    public static void switchVariant(EntityPlayer player, String variant, String indexS) {
        Integer index = Integer.parseInt(indexS);

        City city = getCurrentCity(player);
        if (city == null) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "No city!"), false);
            return;
        }

        Map<String, Integer> variants = city.getPlan().getVariants();
        Map<String, Integer> variantSelections = CityTools.getVariantSelections(city.getCenter());
        if (!variantSelections.containsKey(variant)) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Variant not found!"), false);
            return;
        }

        saveCity(player);

        variantSelections.put(variant, Math.min(index, variants.get(variant)-1));
        player.sendStatusMessage(new TextComponentString("Variant " + variant + " set to " + index), false);

        updateCity(player, city);
    }

    private static void updateCity(EntityPlayer player, City city) {
        player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "Updated city!"), false);
        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);

        CityPlan plan = city.getPlan();

        loadCityOrStation(player, city.getCenter(), plan, 0,
                (x, z) -> CityTools.getLowestHeight(city, generator, x, z),
                (x, z) -> CityTools.getBuildingParts(city, x, z), false);
    }

    private static PartPalette getCurrentPartPalette(EntityPlayer player, City city, BlockPos pos, int cx, int cz) {
        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);
        int lowesty = CityTools.getLowestHeight(city, generator, cx, cz);
        List<PartPalette> partPalettes = CityTools.getPartPalettes(city, cx, cz);
        PartPalette found = null;
        int partY = -1;
        for (PartPalette partPalette : partPalettes) {
            List<String> palette = partPalette.getPalette();
            int count = palette.isEmpty() ? 0 : AssetRegistries.PARTS.get(palette.get(0)).getSliceCount();
            if (pos.getY() >= lowesty && pos.getY() < lowesty + count) {
                found = partPalette;
                partY = lowesty;
                break;
            }
            lowesty += count;
        }
        return found;
    }

    public static void enableEditMode(EntityPlayer player) {
        // Restore city from parts
        loadCity(player);

        editMode = true;
        City city = getCurrentCity(player);
        if (city == null) {
            return;
        }

        CityAI cityAI = CityAISystem.getCityAISystem(player.getEntityWorld()).getCityAI(city.getCenter());
        cityAI.enableEditMode(player.getEntityWorld());
    }

    public static City getCurrentCity(EntityPlayer player) {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);

        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);
        City city = CityTools.getNearestCity(generator, cx, cz);
        if (city == null) {
            player.sendMessage(new TextComponentString("No city can be found!"));
            return null;
        }
        return city;
    }

    public static void cityInfo(EntityPlayer player) {
        City city = getCurrentCity(player);
        if (city == null) {
            return;
        }

        BlockPos pos = player.getPosition();
        ChunkCoord coord = ChunkCoord.getChunkCoordFromPos(pos);
        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);
        int lowesty = CityTools.getLowestHeight(city, generator, coord.getChunkX(), coord.getChunkZ());
        List<BuildingPart> parts = CityTools.getBuildingParts(city, coord.getChunkX(), coord.getChunkZ());

        BuildingPart found = null;
        int partY = -1;
        for (int i = 0; i < parts.size(); i++) {
            int count = parts.get(i).getSliceCount();
            if (pos.getY() >= lowesty && pos.getY() < lowesty + count) {
                found = parts.get(i);
                partY = lowesty;
                break;
            }
            lowesty += count;
        }

        if (found != null) {
            player.sendMessage(new TextComponentString("Part: " + found.getName()));
        }
    }

    public static void copyBlock(City city, World world, IBlockState placedBlock, BuildingPart found, int relX, int relY, int relZ) {
        int cx;
        int cz;
        CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();

        cx = city.getCenter().getChunkX();
        cz = city.getCenter().getChunkZ();

        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) world).getChunkProvider().chunkGenerator);

        for (int dx = cx - dimX / 2 - 1; dx <= cx + dimX / 2 + 1; dx++) {
            for (int dz = cz - dimZ / 2 - 1; dz <= cz + dimZ / 2 + 1; dz++) {
                int y = CityTools.getLowestHeight(city, generator, dx, dz);
                List<BuildingPart> parts = CityTools.getBuildingParts(city, dx, dz);
                for (BuildingPart part : parts) {
                    if (part == found) {
                        world.setBlockState(new BlockPos(dx * 16 + relX, y + relY, dz * 16 + relZ), placedBlock, 3);
                    }
                    y += part.getSliceCount();
                }
            }
        }
    }

    public static void breakBlock(City city, World world, BuildingPart found, int relX, int relY, int relZ) {
        int cx;
        int cz;
        CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();

        cx = city.getCenter().getChunkX();
        cz = city.getCenter().getChunkZ();

        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) world).getChunkProvider().chunkGenerator);

        for (int dx = cx - dimX / 2 - 1; dx <= cx + dimX / 2 + 1; dx++) {
            for (int dz = cz - dimZ / 2 - 1; dz <= cz + dimZ / 2 + 1; dz++) {
                int y = CityTools.getLowestHeight(city, generator, dx, dz);
                List<BuildingPart> parts = CityTools.getBuildingParts(city, dx, dz);
                for (BuildingPart part : parts) {
                    if (part == found) {
                        world.setBlockToAir(new BlockPos(dx * 16 + relX, y + relY, dz * 16 + relZ));
                    }
                    y += part.getSliceCount();
                }
            }
        }
    }

    public static void loadCity(EntityPlayer player) {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);

        if (CityTools.isStationChunk(cx, cz) && start.getY() >= CityTools.getStationHeight() && start.getY() <= CityTools.getStationHeight() + 10 /* @todo */) {
            loadStation(player);
            return;
        }

        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);
        City city = CityTools.getNearestCity(generator, cx, cz);
        if (city == null) {
            player.sendMessage(new TextComponentString("No city or station can be found!"));
            return;
        }

        CityPlan plan = city.getPlan();

        loadCityOrStation(player, city.getCenter(), plan, 0,
                (x, z) -> CityTools.getLowestHeight(city, generator, x, z),
                (x, z) -> CityTools.getBuildingParts(city, x, z), true);
    }

    public static void loadStation(EntityPlayer player) {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);
        CityPlan plan = AssetRegistries.CITYPLANS.get("station");
        loadCityOrStation(player, CityTools.getNearestStationCenter(cx, cz), plan, 0,
                (x, z) -> CityTools.getStationHeight(),
                CityTools::getStationParts, true);
    }

    private static void loadCityOrStation(EntityPlayer player,
                                          ChunkCoord center, CityPlan plan, int offset,
                                          BiFunction<Integer, Integer, Integer> heightGetter,
                                          BiFunction<Integer, Integer, List<BuildingPart>> partsGetter,
                                          boolean doVoid) {
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();

        Palette palette = new Palette(plan.getPalette());
        CompiledPalette compiledPalette = CompiledPalette.getCompiledPalette(plan.getPalette());
        for (PaletteIndex character : compiledPalette.getCharacters()) {
            IBlockState state = compiledPalette.getStraight(character);
            if (state != null) {
                palette.addMapping(character, state);
            }
        }

        int cx = center.getChunkX();
        int cz = center.getChunkZ();

        for (int dx = cx - dimX / 2 - 1 - offset; dx <= cx + dimX / 2 + 1 - offset; dx++) {
            for (int dz = cz - dimZ / 2 - 1 - offset; dz <= cz + dimZ / 2 + 1 - offset; dz++) {
                int y = heightGetter.apply(dx, dz);
                List<BuildingPart> parts = partsGetter.apply(dx, dz);
                if (parts.isEmpty()) {
                    // Void this chunk
                    if (doVoid) {
                        voidChunk(player.world, dx, dz);
                    }
                } else {
                    for (BuildingPart part : parts) {
                        restorePart(part, player.world, new BlockPos(dx * 16 + 8, y /*unused*/, dz * 16 + 8),
                                y, palette);
                        y += part.getSliceCount();
                    }
                }
            }
        }
    }

    private static void voidChunk(World world, int chunkX, int chunkZ) {
        for (int y = 1 ; y < 100 ; y++) {
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    world.setBlockToAir(new BlockPos(chunkX * 16 + x, y, chunkZ * 16 + z));
                }
            }
        }
    }

    private static void restorePart(BuildingPart part, World world, BlockPos start, int y, Palette palette) {
        part.clearVSlices();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        for (int x = 0; x < 16; x++) {
            int cx = (start.getX() >> 4) * 16;

            for (int z = 0; z < 16; z++) {
                int cz = (start.getZ() >> 4) * 16;

                BuildingPart.PalettedSlice vs = part.getVSlice(x, z);
                if (vs != null) {

                    for (int f = 0; f < part.getSliceCount(); f++) {
                        int cy = y + f;
                        if (cy > 255) {
                            break;
                        }
                        pos.setPos(cx + x, cy, cz + z);
                        PaletteIndex c = vs.getSlice().get(f);
                        IBlockState original = palette.getPalette().get(c);
                        IBlockState current = world.getBlockState(pos);

                        if (!current.equals(original) && original != null) {
                            world.setBlockState(pos, original, 3);
                        }
                    }
                }
            }
        }
    }


    public static void saveCity(EntityPlayer player) {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);

        if (CityTools.isStationChunk(cx, cz) && start.getY() >= CityTools.getStationHeight() && start.getY() <= CityTools.getStationHeight() + 10 /* @todo */) {
            try {
                saveStation(player);
            } catch (FileNotFoundException e) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Error saving station!"));
                e.printStackTrace();
            }
            return;
        }

        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);
        City city = CityTools.getNearestCity(generator, cx, cz);
        if (city == null) {
            player.sendMessage(new TextComponentString("No city or station can be found!"));
            return;
        }

        CityPlan plan = city.getPlan();

        try {
            saveCityOrStation(player, city.getCenter(), plan, 0,
                    (x, z) -> CityTools.getLowestHeight(city, generator, x, z),
                    (x, z) -> CityTools.getBuildingParts(city, x, z));
        } catch (FileNotFoundException e) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "Error saving city!"));
            e.printStackTrace();
        }
    }

    public static void saveStation(EntityPlayer player) throws FileNotFoundException {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);
        CityPlan plan = AssetRegistries.CITYPLANS.get("station");
        saveCityOrStation(player, CityTools.getNearestStationCenter(cx, cz), plan, 0,
                (x, z) -> CityTools.getStationHeight(),
                CityTools::getStationParts);
    }

    private static void saveCityOrStation(EntityPlayer player,
                                          ChunkCoord center, CityPlan plan, int offset,
                                          BiFunction<Integer, Integer, Integer> heightGetter,
                                          BiFunction<Integer, Integer, List<BuildingPart>> partsGetter)
            throws FileNotFoundException {
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();

        JsonArray array = new JsonArray();
        AtomicInteger idx = new AtomicInteger(1);
        Map<IBlockState, PaletteIndex> mapping = new HashMap<>();
        Palette palette = new Palette(plan.getPalette());
        CompiledPalette compiledPalette = CompiledPalette.getNewCompiledPalette(plan.getPalette());
        for (PaletteIndex character : compiledPalette.getCharacters()) {
            IBlockState state = compiledPalette.getStraight(character);
            if (state != null) {
                palette.addMapping(character, state);
                mapping.put(state, character);
            }
        }

        array.add(plan.writeToJSon());

        int cx = center.getChunkX();
        int cz = center.getChunkZ();

        Set<PaletteIndex> paletteUsage = new HashSet<>();
        Map<String, BuildingPart> editedParts = new HashMap<>();
        for (int dx = cx - dimX / 2 - 1 - offset; dx <= cx + dimX / 2 + 1 - offset; dx++) {
            for (int dz = cz - dimZ / 2 - 1 - offset; dz <= cz + dimZ / 2 + 1 - offset; dz++) {
                int y = heightGetter.apply(dx, dz);
                List<BuildingPart> parts = partsGetter.apply(dx, dz);
                for (BuildingPart part : parts) {
                    BuildingPart newpart = exportPart(part, player.world, new BlockPos(dx * 16 + 8, y /*unused*/, dz * 16 + 8),
                            y, palette, paletteUsage, mapping, idx);
                    editedParts.put(newpart.getName(), newpart);
                    AssetRegistries.PARTS.put(newpart.getName(), newpart);
                    y += part.getSliceCount();
                }
            }
        }

        StringBuilder affectedParts = new StringBuilder();
        plan.getPartPalette().values()
                .stream()
                .flatMap(partPalette -> partPalette.getPalette().stream())
                .collect(Collectors.toSet())
                .stream()
                .sorted(String::compareTo)
                .map(name -> {
                    if (editedParts.containsKey(name)) {
                        affectedParts.append(name);
                        affectedParts.append(' ');
//                        return editedParts.get(name).writeToJSon();
                    }
                    return AssetRegistries.PARTS.get(name).writeToJSon();
                })
                .forEach(array::add);

        palette.optimize(paletteUsage);
        array.add(palette.writeToJSon());
        AssetRegistries.PALETTES.register(palette);
        System.out.println("Affected parts " + affectedParts);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintWriter writer = new PrintWriter(new File(plan.getName() + ".json"))) {
            writer.print(gson.toJson(array));
            writer.flush();
        }
        player.sendMessage(new TextComponentString("Save city/station '" + plan.getName() + "'!"));
    }

    private static PaletteIndex createNewIndex(int i) {
        String palettechars = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return new PaletteIndex(palettechars.charAt(i % palettechars.length()),
                palettechars.charAt(i / palettechars.length()));
    }

    private static BuildingPart exportPart(BuildingPart part, World world, BlockPos start, int y, Palette palette,
                                           Set<PaletteIndex> paletteUsage,
                                           Map<IBlockState, PaletteIndex> mapping, AtomicInteger idx) {
        Map<BlockPos, Map<String, Object>> teData = new HashMap<>();
        List<Slice> slices = new ArrayList<>();
        for (int f = 0; f < part.getSliceCount(); f++) {
            int cx = (start.getX() >> 4) * 16;
            int cy = y + f;
            int cz = (start.getZ() >> 4) * 16;
            if (cy > 255) {
                break;
            }
            Slice slice = new Slice();
            slices.add(slice);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(cx, cy, cz);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    pos.setPos(cx + x, cy, cz + z);
                    IBlockState state = world.getBlockState(pos);
                    PaletteIndex character;

                    if (state.getBlock() == ModBlocks.invisibleDoorBlock) {
                        character = PALETTE_AIR;
                    } else {
                        character = mapping.get(state);
                    }
                    if (character == null) {
                        while (true) {
                            character = (state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER)
                                    ? PALETTE_AIR : createNewIndex(idx.getAndIncrement());
                            if (!palette.getPalette().containsKey(character)) {
                                break;
                            }
                        }
                        palette.addMapping(character, state);
                        mapping.put(state, character);
                    }
                    paletteUsage.add(character);
                    slice.sequence[z * 16 + x] = character;
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof ICityEquipment) {
                        Map<String, Object> saved = ((ICityEquipment) te).save();
                        if (saved != null) {
                            teData.put(new BlockPos(x, f, z), saved);
                        }
                    }
                }
            }
        }

        BuildingPart.PalettedSlice[] sl = new BuildingPart.PalettedSlice[part.getSliceCount()];
        for (int i = 0; i < part.getSliceCount(); i++) {
            sl[i] = new BuildingPart.PalettedSlice(slices.get(i).sequence);
        }

        return new BuildingPart(part.getName(), 16, 16, sl, teData);
    }

    public static class Slice {
        PaletteIndex sequence[] = new PaletteIndex[256];
    }
}
