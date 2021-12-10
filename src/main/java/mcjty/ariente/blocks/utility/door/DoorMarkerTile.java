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
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class DoorMarkerTile extends GenericTileEntity implements ITickableTileEntity, IGuiTile, ILockable {

    public static final VoxelShape BLOCK_AABB = VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D/16.0, 1.0D);
    public static final VoxelShape OPEN_BLOCK_AABB = VoxelShapes.empty();
    public static final VoxelShape CLOSED_BLOCK_AABB = VoxelShapes.block();

    private AxisAlignedBB detectionBox = null;
    private AxisAlignedBB renderBox = null;

    private boolean open = false;
    private int iconIndex = 0;
    private boolean locked = false;

    // Client side only
    private int opening;  // 0 is closed, 1000 is open
    private long lastTime = -1;  // For rendering

    public DoorMarkerTile() {
        super(Registration.DOOR_MARKER_TILE.get());
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
            public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
                return DoorMarkerTile.getCollisionShape(state, worldIn, pos);
            }

            @Override
            public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
                return BLOCK_AABB;
            }

            @Nullable
            @Override
            public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, @Nullable MobEntity entity) {
                return DoorMarkerTile.getAiPathNodeType(state, world, pos);
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            setInvisibleBlocks();
            if (!locked) {
                List<Entity> entities = level.getEntitiesOfClass(LivingEntity.class, getDetectionBox(),
                        entity -> entity instanceof PlayerEntity || entity instanceof SoldierEntity);
                boolean o = !entities.isEmpty();
                setOpen(o);
            }
        }
    }



    private AxisAlignedBB getDetectionBox() {
        if (detectionBox == null) {
            detectionBox = new AxisAlignedBB(worldPosition.getX()-3, worldPosition.getY()-2, worldPosition.getZ()-3, worldPosition.getX()+4, worldPosition.getY()+6, worldPosition.getZ()+4);
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
        level.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), ModSounds.door, SoundCategory.BLOCKS, 0.6f, 1.0f);
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

    @Nonnull
    public static PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof DoorMarkerTile) {
            DoorMarkerTile door = (DoorMarkerTile) te;
            if (door.isOpen()) {
                return PathNodeType.OPEN;
            }
        }
        return PathNodeType.BLOCKED;
    }

    public static VoxelShape getCollisionShape(BlockState blockState, IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof DoorMarkerTile) {
            DoorMarkerTile door = (DoorMarkerTile) te;
            if (door.isOpen()) {
                return OPEN_BLOCK_AABB;
            }
        }
        return BLOCK_AABB;
    }

    @Override
    public void load(CompoundNBT tagCompound) {
        super.load(tagCompound);
        open = tagCompound.getBoolean("open");
        CompoundNBT info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            iconIndex = info.getInt("icon");
            locked = info.getBoolean("locked");
        }
    }

    @Override
    public void saveAdditional(CompoundNBT tagCompound) {
        tagCompound.putBoolean("open", open);
        CompoundNBT info = getOrCreateInfo(tagCompound);
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
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
        super.onReplaced(world, pos, state, newstate);
        clearInvisibleBlocks();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AxisAlignedBB(getBlockPos()).inflate(.3);
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
