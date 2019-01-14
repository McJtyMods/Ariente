package mcjty.ariente.commands;

import mcjty.ariente.ai.CityAI;
import mcjty.ariente.ai.CityAISystem;
import mcjty.ariente.cities.City;
import mcjty.ariente.dimension.EditMode;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.items.ModItems;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandCityCard implements ICommand {

    @Override
    public String getName() {
        return "ar_citycard";
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
        EntityPlayer player = (EntityPlayer) sender;
        City city = EditMode.getCurrentCity(player);
        if (city == null) {
            return;
        }
        CityAI cityAI = CityAISystem.getCityAISystem(server.getEntityWorld()).getCityAI(city.getCenter());
        ItemStack stack = new ItemStack(ModItems.keyCardItem);
        KeyCardItem.addSecurityTag(stack, cityAI.getStorageKeyId());
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.entityDropItem(stack, 1.05f);
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
