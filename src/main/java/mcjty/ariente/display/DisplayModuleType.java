package mcjty.ariente.display;

import java.util.List;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Server-safe extension point. Rendering is registered separately on the client. */
public interface DisplayModuleType {

    String title();

    Size size(ItemStack stack);

    default int observationDistance() {
        return 64;
    }

    default boolean canLink(Level level, BlockPos pos, Direction side) {
        return false;
    }

    default Component link(ItemStack stack, CompoundTag link, Component targetName) {
        DisplayModuleItem.editConfig(stack, config -> config.put("Link", link));
        return Component.literal("Linked to " + targetName.getString());
    }

    default void clearLinks(ItemStack stack) {
        DisplayModuleItem.editConfig(stack, config -> config.remove("Link"));
    }

    default void appendLinkTooltip(ItemStack stack, List<Component> lines) {
        if (DisplayModuleItem.config(stack).contains("Link")) {
            CompoundTag link = DisplayModuleItem.config(stack).getCompound("Link");
            BlockPos pos = BlockPos.of(link.getLong("Pos"));
            lines.add(Component.literal("Linked: " + pos.toShortString() + " (" + link.getString("Dimension") + ")").withStyle(ChatFormatting.GREEN));
        }
    }

    default boolean refreshOnConfiguration(ItemStack before, ItemStack after) {
        return false;
    }

    /** Called once per second for observed displays; return only data needed by the renderer. */
    default CompoundTag sample(Level level, ItemStack stack, CompoundTag previous) {
        return new CompoundTag();
    }

    void configure(IGuiComponentRegistry registry, IPanel panel, ModuleSettings settings);

    /** Reserved for interactive modules. Coordinates are pixels relative to the module. */
    default void interact(ServerPlayer player, ItemStack stack, CompoundTag snapshot, boolean leftClick, int x, int y) {
    }

    record Size(int width, int height) {

        public Size {
            if (width < 1 || height < 1 || width > 512 || height > 636) {
                throw new IllegalArgumentException("Invalid display module dimensions");
            }
        }
    }
}
