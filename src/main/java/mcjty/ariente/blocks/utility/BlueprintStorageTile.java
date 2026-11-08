package mcjty.ariente.blocks.utility;

import mcjty.hologui.api.*;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.PlayerInventoryPanel;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;
import static mcjty.lib.container.SlotDefinition.generic;

public class BlueprintStorageTile extends ArienteBlockEntity implements IGuiTile, ICityEquipment {

    public static final int BLUEPRINTS = 6;
    public static final int SLOT_BLUEPRINT = 0;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(BLUEPRINTS)
            .box(generic().in().out(), SLOT_BLUEPRINT, 0, 0, BLUEPRINTS, 1));

    private final GenericItemHandler items = createItemHandler();

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
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.BLUEPRINT_STORAGE.createHelpGui(registry);
        }
        return createBlueprintGui(registry);
    }

    private IGuiComponent<?> createBlueprintGui(IGuiComponentRegistry registry) {
        return GuiHelp.BLUEPRINT_STORAGE.createPanel(registry)
                .add(registry.text(0, 0, 8, 1).text("Blueprints").color(registry.color(StyledColor.LABEL)))

                .add(registry.stackIcon(0, 1.2, 1, 1).itemStack(new ItemStack(Registration.CONSTRUCTOR.get())))
                .add(registry.slots(1.5, 1.2, 6, 1)
                        .name("slots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity))
                        .itemHandler(items))

                .add(registry.iconButton(2, 2.8, 1, 1).icon(registry.image(GRAY_ARROW_UP)).hover(registry.image(WHITE_ARROW_UP))
                        .hitEvent((component, player, entity, x, y) -> transferToMachine(player, entity)))
                .add(registry.iconButton(3, 2.8, 1, 1).icon(registry.image(GRAY_ARROW_DOWN)).hover(registry.image(WHITE_ARROW_DOWN))
                        .hitEvent((component, player, entity, x, y) -> transferToPlayer(player, entity)))

                .add(new PlayerInventoryPanel("Double-click bright blueprints")
                        .name("playerslots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToMachine(player, entity))
                        .filter((stack, index) -> stack.getItem() instanceof BlueprintItem))
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
                    if (extracted.getItem() instanceof BlueprintItem) {
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
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Items", items.serializeNBT(provider));
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        items.deserializeNBT(provider, tag.getCompound("Items"));
    }

    @Override
    public void syncToClient() {
    }

    private GenericItemHandler createItemHandler() {
        return new GenericItemHandler(BlueprintStorageTile.this, CONTAINER_FACTORY.get()) {

            @Override
            protected void onUpdate(int index, ItemStack stack) {
                super.onUpdate(index, stack);
                if (level != null && !level.isClientSide) {
                    markDirtyClient();
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == Registration.BLUEPRINT.get();
            }
        };
    }

    @Override
    public IItemHandler getAutomationHandler(Direction side) {
        return items;
    }

}
