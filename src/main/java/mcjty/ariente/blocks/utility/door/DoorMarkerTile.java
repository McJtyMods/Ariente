package mcjty.ariente.blocks.utility.door;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.blocks.utility.ILockTarget;
import mcjty.ariente.security.AreaLocks;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.api.ISoldier;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.IImage;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;

import java.util.List;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class DoorMarkerTile extends TickingTileEntity implements IGuiTile, ILockTarget {

    private static final ResourceLocation PATTERN_TEXTURE = new ResourceLocation(Ariente.MODID, "textures/effects/doormarkers.png");

    public static final VoxelShape BLOCK_AABB = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D/16.0, 1.0D);
    public static final VoxelShape OPEN_BLOCK_AABB = Shapes.empty();
    public static final VoxelShape CLOSED_BLOCK_AABB = Shapes.block();

    private AABB detectionBox = null;
    private AABB renderBox = null;

    private boolean open = false;
    private int iconIndex = 0;
    private boolean locked = false;

    // Client side only
    private int opening;  // 0 is closed, 1000 is open
    private long lastTime = -1;  // For rendering

    public DoorMarkerTile(BlockPos pos, BlockState state) {
        super(Registration.DOOR_MARKER_TILE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        AreaLocks.loaded(this);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of().mapColor(MapColor.METAL)
                        .dynamicShape())
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(DoorMarkerTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
                return DoorMarkerTile.getCollisionShape(state, worldIn, pos);
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return BLOCK_AABB;
            }

            @Deprecated
            @Override
            public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType pathType) {
                return DoorMarkerTile.isPathfindable(state, world, pos, pathType);
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
        setInvisibleBlocks();
        if (!locked) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getDetectionBox(),
                    entity -> entity instanceof Player || entity instanceof ISoldier);
            boolean o = !entities.isEmpty();
            setOpen(o);
        }
    }

    private AABB getDetectionBox() {
        if (detectionBox == null) {
            detectionBox = new AABB(worldPosition.getX()-3, worldPosition.getY()-2, worldPosition.getZ()-3, worldPosition.getX()+4, worldPosition.getY()+6, worldPosition.getZ()+4);
        }
        return detectionBox;
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    private void setInvisibleBlocks() {
        Direction facing = getFacing();
        if (facing == null) {
            return;
        }
        BlockPos pos = worldPosition.above();
        for (int i = 0; i < UtilityConfiguration.MAX_DOOR_HEIGHT.get(); i++) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Registration.INVISIBLE_DOOR.get())) {
                if (state.getValue(BlockStateProperties.HORIZONTAL_FACING) != facing) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing), Block.UPDATE_CLIENTS);
                }
            } else if (state.isAir()) {
                level.setBlock(pos, Registration.INVISIBLE_DOOR.get().defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), Block.UPDATE_CLIENTS);
            } else {
                return;
            }
            pos = pos.above();
        }
    }

    private void clearInvisibleBlocks() {
        BlockPos p = worldPosition.above();
        for (int i = 0 ; i < UtilityConfiguration.MAX_DOOR_HEIGHT.get() ; i++) {
            if (level.getBlockState(p).getBlock() == Registration.INVISIBLE_DOOR.get()) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            } else {
                return;
            }
            p = p.above();
        }
    }

    private Direction getFacing() {
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() != Registration.DOOR_MARKER.get()) {
            return null;
        }
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    private void setOpen(boolean o) {
        if (open == o) {
            return;
        }
        open = o;
        level.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), ModSounds.door.get(), SoundSource.BLOCKS, 0.6f, 1.0f);
        markDirtyClient();
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
        if (locked) {
            setOpen(false);
        }
        markDirtyClient();
    }

    public int getIconIndex() {
        return iconIndex;
    }

    // Client side only
    public int getOpening() {
        return opening;
    }

    // Client side only
    public void setOpening(int opening) {
        this.opening = opening;
    }

    // Client side only
    public long getLastTime() {
        return lastTime;
    }

    // Client side only
    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public static boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType pathType) {
        BlockEntity te = world.getBlockEntity(pos);

        if (te instanceof DoorMarkerTile door) {
            return door.isOpen();
        }

        return false;
    }

    public static VoxelShape getCollisionShape(BlockState blockState, BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof DoorMarkerTile) {
            DoorMarkerTile door = (DoorMarkerTile) te;
            if (door.isOpen()) {
                return OPEN_BLOCK_AABB;
            }
        }
        return BLOCK_AABB;
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        AreaLocks.loadTarget(this, tagCompound);
        open = tagCompound.getBoolean("open");
        CompoundTag info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            iconIndex = info.getInt("icon");
            locked = info.getBoolean("locked");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        AreaLocks.saveTarget(this, tagCompound);
        tagCompound.putBoolean("open", open);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("icon", iconIndex);
        info.putBoolean("locked", locked);
        super.saveAdditional(tagCompound);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag) {
        saveAdditional(tag);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        super.onReplaced(world, pos, state, newstate);
        clearInvisibleBlocks();
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(getBlockPos()).inflate(.3);
        }
        return renderBox;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (isLocked()) {
            return Ariente.guiHandler.createNoAccessPanel();
        }
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.DOOR.createHelpGui(registry);
        }
        IPanel panel = GuiHelp.DOOR.createPanel(registry);
        for (int i = 0; i < 16; i++) {
            int pattern = i;
            panel.add(registry.iconToggle(1 + (i % 4) * 2, 1 + (i / 4) * 2, 1, 1)
                    .name("pattern" + pattern)
                    .getter(player -> isIconSelected(pattern))
                    .icon(patternImage(pattern, false))
                    .selected(patternImage(pattern, true))
                    .hitEvent((component, player, entity, x, y) -> setIconIndex(pattern)));
        }
        return panel;
    }

    private static IImage patternImage(int pattern, boolean selected) {
        // The door sheet contains four columns of patterns, followed by their highlighted variants.
        return new IImage() {
            @Override
            public ResourceLocation getImage() {
                return PATTERN_TEXTURE;
            }

            @Override
            public int getWidth() {
                return 128;
            }

            @Override
            public int getHeight() {
                return 64;
            }

            @Override
            public int getU() {
                return (pattern % 4) * 16 + (selected ? 64 : 0);
            }

            @Override
            public int getV() {
                return (pattern / 4) * 16;
            }
        };
    }

    private void setIconIndex(int idx) {
        if (isLocked()) {
            return;
        }
        this.iconIndex = idx;
        markDirtyClient();
    }

    private boolean isIconSelected(int idx) {
        return iconIndex == idx;
    }

    @Override
    public void syncToClient() {
    }
}
