package mcjty.ariente.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import mcjty.ariente.cities.BuildingPart;
import mcjty.ariente.cities.City;
import mcjty.ariente.cities.CityPlan;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

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

            ChunkCoord cityCenter = City.getNearestCityCenter(cx, cz);
            if (cityCenter == null) {
                sender.sendMessage(new TextComponentString("No city can be found!"));
                return;
            }

            CityPlan plan = City.getRandomCityPlan(cityCenter);
            List<String> pattern = plan.getPlan();
            int dimX = pattern.get(0).length();
            int dimZ = pattern.size();

            for (int dx = cx - dimX / 2 - 1 ; dx <= cx + dimX / 2 + 1 ; dx++) {
                for (int dz = cz - dimZ / 2 - 1 ; dz <= cz + dimZ / 2 + 1 ; dz++) {
                    BuildingPart part = City.getBuildingPart(dx, dz);
                    if (part != null) {
                        CommandExportPart.exportPart(((EntityPlayer) sender).world, part.getName() + ".json", new BlockPos(dx * 16 + 8, start.getY()-1, dz * 16 + 8));
                    }
                }
            }
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
}
