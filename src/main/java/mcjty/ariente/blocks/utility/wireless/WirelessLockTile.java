package mcjty.ariente.blocks.utility.wireless;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import java.util.Map;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.MountedPanelShapes;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.gui.CompactNumber;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.security.AreaLocks;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static mcjty.ariente.blocks.BlockProperties.LOCKED;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.*;

public class WirelessLockTile extends SignalChannelTileEntity implements ILockable, IGuiTile {

    private boolean locked = false;
    private int horizontalRange = 5;
    private int verticalRange = 3;

    private static final MountedPanelShapes SHAPES = new MountedPanelShapes(2, 3);

    public WirelessLockTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.WIRELESS_LOCK_TILE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        AreaLocks.loaded(this);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header(), parameter("channel", (stack -> { var tag = stack.getOrDefault(mcjty.ariente.setup.Registration.MACHINE_DATA.get(), CustomData.EMPTY).copyTag().getCompoundOrEmpty("Info"); return Integer.toString(tag.contains("channel") ? tag.getIntOr("channel", 0) : -1); })))
                .topDriver(DRIVER)
                .tileEntitySupplier(WirelessLockTile::new)
        ) {
            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(LOCKED);
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return SHAPES.get(state.getValue(BlockStateProperties.FACING));
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        setVisualState(LOCKED, locked);
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(getLevel());
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                setLocked(ch.getValue() <= 0);
            } else {
                setLocked(true);
            }
        }
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        boolean locked = isLocked();

        super.onDataPacket(net, input);

        if (level.isClientSide()) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        setVisualState(LOCKED, locked);
        if (locked == this.locked) {
            return;
        }
        this.locked = locked;
        doLock(locked);
        markDirtyClient();
    }

    private void doLock(boolean l) {
        AreaLocks.update(this, horizontalRange, verticalRange, l, null);
    }

    public int getHorizontalRange() {
        return horizontalRange;
    }

    public void setHorizontalRange(int horizontalRange) {
        this.horizontalRange = horizontalRange;
        markDirtyQuick();
        doLock(locked);
    }

    public int getVerticalRange() {
        return verticalRange;
    }

    public void setVerticalRange(int verticalRange) {
        this.verticalRange = verticalRange;
        markDirtyQuick();
        doLock(locked);
    }

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        if (state.getBlock() != newstate.getBlock()) {
            AreaLocks.remove(this);
        }
        super.onReplaced(world, pos, state, newstate);
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = super.save();
        data.put("vertical", verticalRange);
        data.put("horizontal", horizontalRange);
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        super.load(data);
        if (data.get("horizontal") instanceof Integer) {
            setHorizontalRange((Integer) data.get("horizontal"));
        }
        if (data.get("vertical") instanceof Integer) {
            setVerticalRange((Integer) data.get("vertical"));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        locked = tagCompound.getBooleanOr("locked", false);
        CompoundTag info = tagCompound.getCompoundOrEmpty("Info");
        if (!info.isEmpty()) {
            if (info.contains("vertical")) {
                verticalRange = info.getIntOr("vertical", 0);
            }
            if (info.contains("horizontal")) {
                horizontalRange = info.getIntOr("horizontal", 0);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        tagCompound.putBoolean("locked", locked);
        super.saveAdditional(tagCompound, provider);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("vertical", verticalRange);
        info.putInt("horizontal", horizontalRange);
    }

    private void changeHorizontalRange(int dy) {
        int h = horizontalRange + dy;
        if (h < 1) {
            h = 1;
        } else if (h > 20) {
            h = 20;
        }
        setHorizontalRange(h);
    }

    private void changeVerticalRange(int dy) {
        int h = verticalRange + dy;
        if (h < 1) {
            h = 1;
        } else if (h > 10) {
            h = 10;
        }
        setVerticalRange(h);
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (isLocked()) {
            return Ariente.guiHandler.createNoAccessPanel();
        }
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.WIRELESS_LOCK.createHelpGui(registry);
        }
        return GuiHelp.WIRELESS_LOCK.createPanel(registry)
                .add(registry.text(0, 1, 1, 1).text("Horizontal").color(registry.color(StyledColor.LABEL)))
                .add(new CompactNumber(3, 2, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,h) -> getHorizontalRange()))

                .add(registry.iconButton(1, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(-8)))
                .add(registry.iconButton(2, 2, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(-1)))
                .add(registry.iconButton(5, 2, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(1)))
                .add(registry.iconButton(6, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(8)))

                .add(registry.text(0, 4, 1, 1).text("Vertical").color(registry.color(StyledColor.LABEL)))
                .add(new CompactNumber(3, 5, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,h) -> getVerticalRange()))

                .add(registry.iconButton(1, 5, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(-8)))
                .add(registry.iconButton(2, 5, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(-1)))
                .add(registry.iconButton(5, 5, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(1)))
                .add(registry.iconButton(6, 5, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(8)))
                ;
    }

    @Override
    public void syncToClient() {
    }

}
