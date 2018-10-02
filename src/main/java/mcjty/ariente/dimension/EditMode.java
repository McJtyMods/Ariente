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
            return;
        }
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);
        CityIndex index = CityTools.getCityIndex(cx, cz);
        if (index == null) {
            return;
        }

        CityPlan plan = city.getPlan();
        Map<Character, PartPalette> partPalette = plan.getPartPalette();
//        char partChar = pattern.get(index.getZOffset()).charAt(index.getXOffset());

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

    private static City getCurrentCity(EntityPlayer player) {
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

    public static void switchPart(EntityPlayer player, String partName) {
        City city = getCurrentCity(player);
        if (city == null) {
            return;
        }

        BuildingPart newPart = AssetRegistries.PARTS.get(partName);
        if (newPart == null) {
            player.sendMessage(new TextComponentString("Cannot find part '" + partName + "'!"));
            return;
        }

        BlockPos pos = player.getPosition();
        ChunkCoord coord = ChunkCoord.getChunkCoordFromPos(pos);
        World world = player.getEntityWorld();
        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) world).getChunkProvider().chunkGenerator);
        int cx = coord.getChunkX();
        int cz = coord.getChunkZ();
        int lowesty = CityTools.getLowestHeight(city, generator, cx, cz);
        List<BuildingPart> parts = CityTools.getBuildingParts(city, cx, cz);

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
            for (int dy = 0; dy < found.getSliceCount(); dy++) {
                for (int dx = 0; dx < 16; dx++) {
                    for (int dz = 0; dz < 16; dz++) {
                        world.setBlockToAir(new BlockPos(cx * 16 + dx, partY + dy, cz * 16 + dz));
                    }
                }
            }
            String paletteName = city.getPlan().getPalette();
            Palette palette = AssetRegistries.PALETTES.get(paletteName);
            CompiledPalette compiledPalette = new CompiledPalette(palette);
            for (int dy = 0; dy < newPart.getSliceCount(); dy++) {
                for (int dx = 0; dx < 16; dx++) {
                    for (int dz = 0; dz < 16; dz++) {
                        PaletteIndex c = newPart.getPaletteChar(dx, dy, dz);
                        IBlockState state = compiledPalette.getStraight(c);
                        world.setBlockState(new BlockPos(cx * 16 + dx, partY + dy, cz * 16 + dz), state, 3);
                    }
                }
            }
            player.sendMessage(new TextComponentString("Switched from " + found.getName() + " to " + partName));
        }

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
                (x, z) -> CityTools.getBuildingParts(city, x, z));
    }

    public static void loadStation(EntityPlayer player) {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);
        CityPlan plan = AssetRegistries.CITYPLANS.get("station");
        loadCityOrStation(player, CityTools.getNearestStationCenter(cx, cz), plan, 0,
                (x, z) -> CityTools.getStationHeight(),
                CityTools::getStationParts);
    }

    private static void loadCityOrStation(EntityPlayer player,
                                          ChunkCoord center, CityPlan plan, int offset,
                                          BiFunction<Integer, Integer, Integer> heightGetter,
                                          BiFunction<Integer, Integer, List<BuildingPart>> partsGetter) {
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
                    voidChunk(player.world, dx, dz);
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


    public static void saveCity(EntityPlayer player) throws FileNotFoundException {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);

        if (CityTools.isStationChunk(cx, cz) && start.getY() >= CityTools.getStationHeight() && start.getY() <= CityTools.getStationHeight() + 10 /* @todo */) {
            saveStation(player);
            return;
        }

        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);
        City city = CityTools.getNearestCity(generator, cx, cz);
        if (city == null) {
            player.sendMessage(new TextComponentString("No city or station can be found!"));
            return;
        }

        CityPlan plan = city.getPlan();

        saveCityOrStation(player, city.getCenter(), plan, 0,
                (x, z) -> CityTools.getLowestHeight(city, generator, x, z),
                (x, z) -> CityTools.getBuildingParts(city, x, z));
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
        CompiledPalette compiledPalette = CompiledPalette.getCompiledPalette(plan.getPalette());
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
                    AssetRegistries.PARTS.replace(newpart.getName(), newpart);
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

        player.sendMessage(new TextComponentString("Affected parts " + affectedParts));

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
                                           Map<IBlockState, PaletteIndex> mapping, AtomicInteger idx) throws FileNotFoundException {
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
