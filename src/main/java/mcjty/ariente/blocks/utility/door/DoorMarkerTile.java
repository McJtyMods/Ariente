package mcjty.ariente.blocks.utility.door;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
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

public class DoorMarkerTile extends TickingTileEntity implements IGuiTile, ILockable {

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

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of(Material.METAL)
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
                    entity -> entity instanceof Player || entity instanceof SoldierEntity);
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
        BlockPos p = worldPosition.above();
        for (int i = 0 ; i < UtilityConfiguration.MAX_DOOR_HEIGHT.get() ; i++) {
            if (level.isEmptyBlock(p)) {
                Direction facing = getFacing();
                if (facing == null) {
                    return;
                }
                level.setBlock(p, Registration.INVISIBLE_DOOR.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
            } else {
                return;
            }
            p = p.above();
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
        level.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), ModSounds.door, SoundSource.BLOCKS, 0.6f, 1.0f);
        markDirtyClient();
    }

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
        open = tagCompound.getBoolean("open");
        CompoundTag info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            iconIndex = info.getInt("icon");
            locked = info.getBoolean("locked");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        tagCompound.putBoolean("open", open);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("icon", iconIndex);
        info.putBoolean("locked", locked);
        super.saveAdditional(tagCompound);
    }

    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 0;
//    }


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
        return registry.panel(0, 0, 8, 8)
                .add(registry.iconToggle(1, 1, 1, 1).getter((player) -> isIconSelected(0)).icon(registry.image(4*16, 12*16)).selected(registry.image(64+4*16, 12*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(0)))
                .add(registry.iconToggle(3, 1, 1, 1).getter((player) -> isIconSelected(1)).icon(registry.image(5*16, 12*16)).selected(registry.image(64+5*16, 12*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(1)))
                .add(registry.iconToggle(5, 1, 1, 1).getter((player) -> isIconSelected(2)).icon(registry.image(6*16, 12*16)).selected(registry.image(64+6*16, 12*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(2)))
                .add(registry.iconToggle(7, 1, 1, 1).getter((player) -> isIconSelected(3)).icon(registry.image(7*16, 12*16)).selected(registry.image(64+7*16, 12*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(3)))

                .add(registry.iconToggle(1, 3, 1, 1).getter((player) -> isIconSelected(4)).icon(registry.image(4*16, 13*16)).selected(registry.image(64+4*16, 13*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(4)))
                .add(registry.iconToggle(3, 3, 1, 1).getter((player) -> isIconSelected(5)).icon(registry.image(5*16, 13*16)).selected(registry.image(64+5*16, 13*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(5)))
                .add(registry.iconToggle(5, 3, 1, 1).getter((player) -> isIconSelected(6)).icon(registry.image(6*16, 13*16)).selected(registry.image(64+6*16, 13*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(6)))
                .add(registry.iconToggle(7, 3, 1, 1).getter((player) -> isIconSelected(7)).icon(registry.image(7*16, 13*16)).selected(registry.image(64+7*16, 13*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(7)))

                .add(registry.iconToggle(1, 5, 1, 1).getter((player) -> isIconSelected(8)).icon(registry.image(4*16, 14*16)).selected(registry.image(64+4*16, 14*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(8)))
                .add(registry.iconToggle(3, 5, 1, 1).getter((player) -> isIconSelected(9)).icon(registry.image(5*16, 14*16)).selected(registry.image(64+5*16, 14*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(9)))
                .add(registry.iconToggle(5, 5, 1, 1).getter((player) -> isIconSelected(10)).icon(registry.image(6*16, 14*16)).selected(registry.image(64+6*16, 14*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(10)))
                .add(registry.iconToggle(7, 5, 1, 1).getter((player) -> isIconSelected(11)).icon(registry.image(7*16, 14*16)).selected(registry.image(64+7*16, 14*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(11)))

                .add(registry.iconToggle(1, 7, 1, 1).getter((player) -> isIconSelected(12)).icon(registry.image(4*16, 15*16)).selected(registry.image(64+4*16, 15*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(12)))
                .add(registry.iconToggle(3, 7, 1, 1).getter((player) -> isIconSelected(13)).icon(registry.image(5*16, 15*16)).selected(registry.image(64+5*16, 15*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(13)))
                .add(registry.iconToggle(5, 7, 1, 1).getter((player) -> isIconSelected(14)).icon(registry.image(6*16, 15*16)).selected(registry.image(64+6*16, 15*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(14)))
                .add(registry.iconToggle(7, 7, 1, 1).getter((player) -> isIconSelected(15)).icon(registry.image(7*16, 15*16)).selected(registry.image(64+7*16, 15*16)).hitEvent((component, player, entity1, x, y) -> setIconIndex(15)))
                ;
    }

    private void setIconIndex(int idx) {
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
