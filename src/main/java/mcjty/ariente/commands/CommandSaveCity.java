package mcjty.ariente.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import mcjty.ariente.cities.*;
import mcjty.ariente.dimension.ArienteChunkGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandSaveCity implements ICommand {

    @Override
    public String getName() {
        return "ar_savecity";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName();
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        try {
            EntityPlayer player = (EntityPlayer) sender;
            BlockPos start = player.getPosition();
            int cx = (start.getX() >> 4);
            int cz = (start.getZ() >> 4);

            ArienteChunkGenerator generator = (ArienteChunkGenerator)(((WorldServer)sender.getEntityWorld()).getChunkProvider().chunkGenerator);
            City city = CityTools.getNearestCity(generator, cx, cz);
            if (city == null) {
                sender.sendMessage(new TextComponentString("No city can be found!"));
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

            for (int dx = cx - dimX / 2 - 1 ; dx <= cx + dimX / 2 + 1 ; dx++) {
                for (int dz = cz - dimZ / 2 - 1 ; dz <= cz + dimZ / 2 + 1 ; dz++) {
                    int y = CityTools.getLowestHeight(city, dx, dz);
                    List<BuildingPart> parts = CityTools.getBuildingParts(city, dx, dz);
                    for (BuildingPart part : parts) {
                        BuildingPart newpart = exportPart(part, ((EntityPlayer) sender).world, new BlockPos(dx * 16 + 8, start.getY() - 1, dz * 16 + 8),
                                y, palette, mapping, idx);
                        array.add(newpart.writeToJSon());
                        y += part.getSliceCount();
                    }
                }
            }

            array.add(palette.writeToJSon());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try(PrintWriter writer = new PrintWriter(new File(plan.getName() + ".json"))) {
                writer.print(gson.toJson(array));
                writer.flush();
            }
            sender.sendMessage(new TextComponentString("Save city '" + plan.getName() + "'!"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }

    public static class Slice {
        String sequence[] = new String[256];
    }

    private static BuildingPart exportPart(BuildingPart part, World world, BlockPos start, int y, Palette palette,
                                           Map<IBlockState, Character> mapping, AtomicInteger idx) throws FileNotFoundException {
        String palettechars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        List<Slice> slices = new ArrayList<>();
        for (int f = 0 ; f < part.getSliceCount() ; f++) {
            int cx = (start.getX() >> 4) * 16;
            int cy = y + f;
            int cz = (start.getZ() >> 4) * 16;
            if (cy > 255) {
                break;
            }
            Slice slice = new Slice();
            slices.add(slice);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(cx, cy, cz);
            boolean allempty = true;
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    pos.setPos(cx + x, cy, cz + z);
                    IBlockState state = world.getBlockState(pos);
                    Character character = mapping.get(state);
                    if (character == null) {
                        while (true) {
                            character = state.getBlock() == Blocks.AIR ? ' ' : palettechars.charAt(idx.getAndIncrement());
                            if (!palette.getPalette().containsKey(character)) {
                                break;
                            }
                        }
                        palette.addMapping(character, state);
                        mapping.put(state, character);
                    }
                    slice.sequence[z*16+x] = String.valueOf(character);
                }
            }
        }

        String[] sl = new String[part.getSliceCount()];
        for (int i = 0 ; i < part.getSliceCount() ; i++) {
            sl[i] = StringUtils.join(slices.get(i).sequence);
        }

        return new BuildingPart(part.getName(), 16, 16, sl);
    }
}
