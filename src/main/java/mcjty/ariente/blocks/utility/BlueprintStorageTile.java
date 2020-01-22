package mcjty.ariente.blocks.utility;

import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.ModItems;
import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static mcjty.hologui.api.Icons.*;

public class BlueprintStorageTile extends GenericTileEntity implements IGuiTile, ICityEquipment {

    public static final int BLUEPRINTS = 6;
    public static final int SLOT_BLUEPRINT = 0;
    public static int[] slots = null;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(BLUEPRINTS);// @todo 1.14 new ResourceLocation(Ariente.MODID, "gui/blueprint_storage.gui"));
//    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, BLUEPRINTS);

    private NoDirectionItemHander items = createItemHandler();
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(() -> items);
    private LazyOptional<AutomationFilterItemHander> automationItemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    public BlueprintStorageTile(TileEntityType<?> type) {
        super(type);
    }

    // @todo 1.14 loot
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
//        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    public void writeRestorableToNBT(CompoundNBT tagCompound) {
//        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    // @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//    }


    public NoDirectionItemHander getItems() {
        return items;
    }

    @Nullable
    @Override
    public Map<String, Object> save() {
        return null;
    }

    @Override
    public void load(Map<String, Object> data) {

    }

    @Override
    public void setup(ICityAI cityAI, World world, boolean firstTime) {

    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return createBlueprintGui(registry);
    }

    private IGuiComponent<?> createBlueprintGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 0, 8, 1).text("Blueprints").color(registry.color(StyledColor.LABEL)))

                .add(registry.icon(0, 2, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(registry.playerSlots(1.5, 1.5, 6, 2)
                        .name("playerslots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToMachine(player, entity))
                        .filter((stack, index) -> stack.getItem() instanceof BlueprintItem))

                .add(registry.iconButton(2, 3.5, 1, 1).icon(registry.image(GRAY_ARROW_DOWN)).hover(registry.image(WHITE_ARROW_DOWN))
                        .hitEvent((component, player, entity, x, y) -> transferToMachine(player, entity)))
                .add(registry.iconButton(3, 3.5, 1, 1).icon(registry.image(GRAY_ARROW_UP)).hover(registry.image(WHITE_ARROW_UP))
                        .hitEvent((component, player, entity, x, y) -> transferToPlayer(player, entity)))

                .add(registry.stackIcon(0, 4.5, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock.get())))
                .add(registry.slots(1.5, 4.5, 6, 1)
                        .name("slots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity))
                        .itemHandler(items))
                ;
    }

    private void transferToPlayer(PlayerEntity player, IHoloGuiEntity entity) {
        entity.findComponent("slots").ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = items.extractItem(selected, 64, false);
                    if (!extracted.isEmpty()) {
                        if (!player.inventory.addItemStackToInventory(extracted)) {
                            items.insertItem(selected, extracted, false);
                        } else {
                            ((ISlots) component).setSelection(-1);
                        }
                        markDirtyClient();
                    }
                }
            }
        });
    }

    private void transferToMachine(PlayerEntity player, IHoloGuiEntity entity) {
        entity.findComponent("playerslots").ifPresent(component -> {
            if (component instanceof IPlayerSlots) {
                int selected = ((IPlayerSlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.inventory.getStackInSlot(selected);
                    if (!extracted.isEmpty()) {
                        ItemStack notInserted = ItemHandlerHelper.insertItem(items, extracted, false);
                        player.inventory.setInventorySlotContents(selected, notInserted);
                        if (notInserted.isEmpty()) {
                            ((IPlayerSlots) component).setSelection(-1);
                        }
                        markDirtyClient();
                    }
                }
            }
        });
    }

    @Override
    public void syncToClient() {
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(BlueprintStorageTile.this, CONTAINER_FACTORY) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == ModItems.blueprintItem;
            }
        };
    }
}
