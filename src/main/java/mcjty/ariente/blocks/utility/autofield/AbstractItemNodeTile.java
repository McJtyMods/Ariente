package mcjty.ariente.blocks.utility.autofield;

import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.IPanel;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.api.components.ISlots;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.BooleanSupplier;

public abstract class AbstractItemNodeTile extends AbstractNodeTile {

    public static final int FILTER_AMOUNT = 12;

    public AbstractItemNodeTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void addFilterConditions(IGuiComponentRegistry registry, IPanel panel) {
        panel.add(registry.text(.25, .5, 1.7, .6).text("Positive").scale(.38f).color(registry.color(0x76eaff)))
                .add(registry.text(4.1, .5, 1.7, .6).text("Negative").scale(.38f).color(registry.color(0x76eaff)));
        addFilterChoice(registry, panel, 0, false, 2, .5, .65);
        addFilterChoice(registry, panel, 1, false, 2.85, .5, .65);
        addFilterChoice(registry, panel, 2, true, 6, .5, .65);
        addFilterChoice(registry, panel, 3, true, 6.85, .5, .65);
    }

    protected void addFilterOption(IGuiComponentRegistry registry, IPanel panel, String name, double y,
                                   String off, String on, BooleanSupplier enabled, Runnable toggle) {
        panel.add(registry.textChoice(.25, y, 2.3, .6).name(name).scale(.35f)
                .addText(off).addText(on)
                .getter(player -> enabled.getAsBoolean() ? 1 : 0)
                .hitEvent((component, player, entity, x, hitY) -> toggle.run()));
    }

    protected void addToFilter(Player player, IHoloGuiEntity entity, SimpleItemHandler filter) {
        entity.findComponent("playerSlots").ifPresent(component -> {
            if (component instanceof IPlayerSlots) {
                int selected = ((IPlayerSlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.getInventory().getItem(selected);
                    if (!extracted.isEmpty()) {
                        ItemHandlerHelper.insertItem(filter, extracted, false);
                        notifyField();
                    }
                }
            }
        });
    }

    protected void removeFromFilter(Player player, IHoloGuiEntity entity, SimpleItemHandler filter) {
        entity.findComponent("slots").ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                if (selected != -1) {
                    filter.setItemStack(selected, ItemStack.EMPTY);
                    notifyField();
                }
            }
        });
    }
}
