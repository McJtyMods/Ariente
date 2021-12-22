package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class BlueprintStorageTile extends GenericTileEntity implements IGuiTile, ICityEquipment {

    public static final int BLUEPRINTS = 6;
    public static final int SLOT_BLUEPRINT = 0;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(BLUEPRINTS));// @todo 1.14 new ResourceLocation(Ariente.MODID, "gui/blueprint_storage.gui"));

    private final GenericItemHandler items = createItemHandler();
    private final LazyOptional<GenericItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<AutomationFilterItemHander> automationItemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    public BlueprintStorageTile(BlockPos pos, BlockState state) {
        super(Registration.BLUEPRINT_STORAGE_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(BlueprintStorageTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }


    // @todo 1.14 loot
    public void readRestorableFromNBT(CompoundTag tagCompound) {
//        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    public void writeRestorableToNBT(CompoundTag tagCompound) {
//        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    public GenericItemHandler getItems() {
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
    public void setup(ICityAI cityAI, Level world, boolean firstTime) {

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

                .add(registry.stackIcon(0, 4.5, 1, 1).itemStack(new ItemStack(Registration.CONSTRUCTOR.get())))
                .add(registry.slots(1.5, 4.5, 6, 1)
                        .name("slots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity))
                        .itemHandler(items))
                ;
    }

    private void transferToPlayer(Player player, IHoloGuiEntity entity) {
        entity.findComponent("slots").ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = items.extractItem(selected, 64, false);
                    if (!extracted.isEmpty()) {
                        if (!player.getInventory().add(extracted)) {
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

    private void transferToMachine(Player player, IHoloGuiEntity entity) {
        entity.findComponent("playerslots").ifPresent(component -> {
            if (component instanceof IPlayerSlots) {
                int selected = ((IPlayerSlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.getInventory().getItem(selected);
                    if (!extracted.isEmpty()) {
                        ItemStack notInserted = ItemHandlerHelper.insertItem(items, extracted, false);
                        player.getInventory().setItem(selected, notInserted);
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

    private GenericItemHandler createItemHandler() {
        return new GenericItemHandler(BlueprintStorageTile.this, CONTAINER_FACTORY.get()) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == Registration.BLUEPRINT.get();
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return automationItemHandler.cast();
        }
        return super.getCapability(cap);
    }
}
