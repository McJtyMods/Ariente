package mcjty.ariente.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import mcjty.ariente.cities.BuildingPart;
import mcjty.ariente.cities.CompiledPalette;
import mcjty.ariente.cities.Palette;
import mcjty.ariente.dimension.ArienteCityGenerator;
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
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class CommandExportPart implements ICommand {

    @Override
    public String getName() {
        return "ar_exportpart";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName() + " <file>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String filename = args[0];
        try {
            EntityPlayer player = (EntityPlayer) sender;
            BlockPos start = player.getPosition().down();
            exportPart(sender.getEntityWorld(), filename, start);
        } catch (FileNotFoundException e) {
            sender.sendMessage(new TextComponentString("Error writing to file '" + filename + "'!"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportPart(World world, String filename, BlockPos start) throws FileNotFoundException {
        JsonArray array = new JsonArray();
        String palettechars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int idx = 0;
        Map<IBlockState, Character> mapping = new HashMap<>();
        Palette palette = new Palette("old");
        Palette paletteNew = new Palette("new");
        CompiledPalette compiledPalette = ArienteCityGenerator.compiledPalette;
        for (Character character : compiledPalette.getCharacters()) {
            IBlockState state = compiledPalette.getStraight(character);
            if (state != null) {
                palette.addMapping(character, state);
                mapping.put(state, character);
            }
        }

        List<Slice> slices = new ArrayList<>();
        int f = 0;
        int cntSlices = 0;
        while (true) {
            int cx = (start.getX() >> 4) * 16;
            int cy = start.getY() + f;
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
                            character = state.getBlock() == Blocks.AIR ? ' ' : palettechars.charAt(idx);
                            idx++;
                            if (!palette.getPalette().containsKey(character) && !paletteNew.getPalette().containsKey(character)) {
                                break;
                            }
                        }
                        paletteNew.addMapping(character, state);
                        mapping.put(state, character);
                    }
                    slice.sequence[z*16+x] = String.valueOf(character);
                    if (character != ' ') {
                        allempty = false;
                    }
                }
            }
            f++;
            if (!allempty) {
                cntSlices = f;
            }
        }

        String[] sl = new String[cntSlices];
        for (int i = 0 ; i < cntSlices ; i++) {
            sl[i] = StringUtils.join(slices.get(i).sequence);
        }

        BuildingPart part = new BuildingPart("part", 16, 16, sl);
        array.add(part.writeToJSon());

        array.add(palette.writeToJSon());
        array.add(paletteNew.writeToJSon());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.print(gson.toJson(array));
            writer.flush();
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
}
