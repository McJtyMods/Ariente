package mcjty.ariente.commands;

import mcjty.ariente.dimension.EditMode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandVariant implements ICommand {

    @Override
    public String getName() {
        return "ar_variant";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName() + " [set|get|list|switch] [<variantname> [<number>]]";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = (EntityPlayer) sender;
        try {
            String c = args[1].toLowerCase();
            if ("set".equals(c) || "s".equals(c)) {
                EditMode.setVariant(player, args[2]);
            } else if ("get".equals(c) || "g".equals(c)) {
                EditMode.getVariant(player);
            } else if ("list".equals(c)) {
                EditMode.listVariants(player);
            } else if ("switch".equals(c)) {
                EditMode.switchVariant(player, args[2], args[3]);
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

}
