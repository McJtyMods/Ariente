package mcjty.ariente.dimension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import mcjty.ariente.ai.CityAI;
import mcjty.ariente.ai.CityAISystem;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.*;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EditMode {

    public static boolean editMode = false;

    public static void enableEditMode(EntityPlayer player) {
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
                        Character c = newPart.getPaletteChar(dx, dy, dz);
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

    public static void saveCity(EntityPlayer player) throws FileNotFoundException {
        BlockPos start = player.getPosition();
        int cx = (start.getX() >> 4);
        int cz = (start.getZ() >> 4);

        ArienteChunkGenerator generator = (ArienteChunkGenerator) (((WorldServer) player.getEntityWorld()).getChunkProvider().chunkGenerator);
        City city = CityTools.getNearestCity(generator, cx, cz);
        if (city == null) {
            player.sendMessage(new TextComponentString("No city can be found!"));
            return;
        }

        CityPlan plan = city.getPlan();
        List<String> pattern = plan.getPlan();
        int dimX = pattern.get(0).length();
        int dimZ = pattern.size();

        JsonArray array = new JsonArray();
        AtomicInteger idx = new AtomicInteger();
        Map<IBlockState, Character> mapping = new HashMap<>();
        Palette palette = new Palette(plan.getPalette());
        CompiledPalette compiledPalette = CompiledPalette.getCompiledPalette(plan.getPalette());
        for (Character character : compiledPalette.getCharacters()) {
            IBlockState state = compiledPalette.getStraight(character);
            if (state != null) {
                palette.addMapping(character, state);
                mapping.put(state, character);
            }
        }

        array.add(plan.writeToJSon());

        cx = city.getCenter().getChunkX();
        cz = city.getCenter().getChunkZ();

        Set<Character> paletteUsage = new HashSet<>();
        Map<String, BuildingPart> editedParts = new HashMap<>();
        for (int dx = cx - dimX / 2 - 1; dx <= cx + dimX / 2 + 1; dx++) {
            for (int dz = cz - dimZ / 2 - 1; dz <= cz + dimZ / 2 + 1; dz++) {
                int y = CityTools.getLowestHeight(city, generator, dx, dz);
                List<BuildingPart> parts = CityTools.getBuildingParts(city, dx, dz);
                for (BuildingPart part : parts) {
                    BuildingPart newpart = exportPart(part, player.world, new BlockPos(dx * 16 + 8, start.getY() - 1, dz * 16 + 8),
                            y, palette, paletteUsage, mapping, idx);
                    editedParts.put(newpart.getName(), newpart);
                    y += part.getSliceCount();
                }
            }
        }

        StringBuilder affectedParts = new StringBuilder();
        city.getPlan().getPartPalette().values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .stream()
                .sorted(String::compareTo)
                .map(name -> {
                    if (editedParts.containsKey(name)) {
                        affectedParts.append(name);
                        affectedParts.append(' ');
                        return editedParts.get(name).writeToJSon();
                    } else {
                        return AssetRegistries.PARTS.get(name).writeToJSon();
                    }
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
        player.sendMessage(new TextComponentString("Save city '" + plan.getName() + "'!"));
    }

    private static BuildingPart exportPart(BuildingPart part, World world, BlockPos start, int y, Palette palette,
                                           Set<Character> paletteUsage,
                                           Map<IBlockState, Character> mapping, AtomicInteger idx) throws FileNotFoundException {
        String palettechars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-+=*^%$#@_()[]{}:;,.?!";
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
                    Character character;

                    if (state.getBlock() == ModBlocks.invisibleDoorBlock) {
                        character = ' ';
                    } else {
                        character = mapping.get(state);
                    }
                    if (character == null) {
                        while (true) {
                            try {
                                character = (state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER)
                                        ? ' ' : palettechars.charAt(idx.getAndIncrement());
                            } catch (Exception e) {
                                character = ' ';
                                System.out.println("PALETTE OVERFLOW SAVING PART " + part.getName() + "!");
                            }
                            if (!palette.getPalette().containsKey(character)) {
                                break;
                            }
                        }
                        palette.addMapping(character, state);
                        mapping.put(state, character);
                    }
                    paletteUsage.add(character);
                    slice.sequence[z * 16 + x] = String.valueOf(character);
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

        String[] sl = new String[part.getSliceCount()];
        for (int i = 0; i < part.getSliceCount(); i++) {
            sl[i] = StringUtils.join(slices.get(i).sequence);
        }

        return new BuildingPart(part.getName(), 16, 16, sl, teData);
    }

    public static class Slice {
        String sequence[] = new String[256];
    }
}
