package mcjty.ariente.blocks.utility.door;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class DoorMarkerTile extends GenericTileEntity implements ITickable, IGuiTile, ILockable {

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    public static final AxisAlignedBB OPEN_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

    private AxisAlignedBB detectionBox = null;
    private AxisAlignedBB renderBox = null;

    private boolean open = false;
    private int iconIndex = 0;
    private boolean locked = false;

    // Client side only
    private int opening;  // 0 is closed, 1000 is open
    private long lastTime = -1;  // For rendering

    @Override
    public void update() {
        if (!world.isRemote) {
            setInvisibleBlocks();
            if (!locked) {
                List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, getDetectionBox(),
                        entity -> entity instanceof EntityPlayer || entity instanceof SoldierEntity);
                boolean o = !entities.isEmpty();
                setOpen(o);
            }
        }
    }

    private AxisAlignedBB getDetectionBox() {
        if (detectionBox == null) {
            detectionBox = new AxisAlignedBB(pos.getX()-3, pos.getY()-2, pos.getZ()-3, pos.getX()+4, pos.getY()+6, pos.getZ()+4);
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
        BlockPos p = pos.up();
        for (int i = 0 ; i < UtilityConfiguration.MAX_DOOR_HEIGHT ; i++) {
            if (world.isAirBlock(p)) {
                EnumFacing facing = getFacing();
                if (facing == null) {
                    return;
                }
                world.setBlockState(p, ModBlocks.invisibleDoorBlock.getDefaultState().withProperty(BaseBlock.FACING_HORIZ, facing), 3);
            } else {
                return;
            }
            p = p.up();
        }
    }

    private void clearInvisibleBlocks() {
        BlockPos p = pos.up();
        for (int i = 0 ; i < UtilityConfiguration.MAX_DOOR_HEIGHT ; i++) {
            if (world.getBlockState(p).getBlock() == ModBlocks.invisibleDoorBlock) {
                world.setBlockToAir(p);
            } else {
                return;
            }
            p = p.up();
        }
    }

    private EnumFacing getFacing() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != ModBlocks.doorMarkerBlock) {
            return null;
        }
        return state.getValue(BaseBlock.FACING_HORIZ);
    }

    private void setOpen(boolean o) {
        if (open == o) {
            return;
        }
        open = o;
        world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), ModSounds.door, SoundCategory.BLOCKS, 0.6f, 1.0f);
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

    @Nullable
    public static PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof DoorMarkerTile) {
            DoorMarkerTile door = (DoorMarkerTile) te;
            if (door.isOpen()) {
                return PathNodeType.OPEN;
            }
        }
        return PathNodeType.BLOCKED;
    }

    public static AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof DoorMarkerTile) {
            DoorMarkerTile door = (DoorMarkerTile) te;
            if (door.isOpen()) {
                return OPEN_BLOCK_AABB;
            }
        }
        return BLOCK_AABB;
    }

    public static boolean addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof DoorMarkerTile) {
            DoorMarkerTile door = (DoorMarkerTile) te;
            if (!door.isOpen()) {
                AxisAlignedBB box = Block.FULL_BLOCK_AABB.offset(pos);
                if (entityBox.intersects(box)) {
                    collidingBoxes.add(box);
                }
            }
        }
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        open = tagCompound.getBoolean("open");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setBoolean("open", open);
        return super.writeToNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        iconIndex = tagCompound.getInteger("icon");
        locked = tagCompound.getBoolean("locked");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("icon", iconIndex);
        tagCompound.setBoolean("locked", locked);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        Boolean working = isWorking();
//        if (working) {
//            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    @Override
    public void onBlockBreak(World workd, BlockPos pos, IBlockState state) {
        super.onBlockBreak(workd, pos, state);
        clearInvisibleBlocks();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AxisAlignedBB(getPos()).grow(.3);
        }
        return renderBox;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (isLocked()) {
            return Ariente.guiHandler.createNoAccessPanel();
        }
        return registry.panel(0, 0, 8, 8)
                .add(registry.iconToggle(1, 1, 1, 1).getter((player) -> isIconSelected(0)).icon(4*16, 12*16).selected(64+4*16, 12*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(0)))
                .add(registry.iconToggle(3, 1, 1, 1).getter((player) -> isIconSelected(1)).icon(5*16, 12*16).selected(64+5*16, 12*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(1)))
                .add(registry.iconToggle(5, 1, 1, 1).getter((player) -> isIconSelected(2)).icon(6*16, 12*16).selected(64+6*16, 12*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(2)))
                .add(registry.iconToggle(7, 1, 1, 1).getter((player) -> isIconSelected(3)).icon(7*16, 12*16).selected(64+7*16, 12*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(3)))

                .add(registry.iconToggle(1, 3, 1, 1).getter((player) -> isIconSelected(4)).icon(4*16, 13*16).selected(64+4*16, 13*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(4)))
                .add(registry.iconToggle(3, 3, 1, 1).getter((player) -> isIconSelected(5)).icon(5*16, 13*16).selected(64+5*16, 13*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(5)))
                .add(registry.iconToggle(5, 3, 1, 1).getter((player) -> isIconSelected(6)).icon(6*16, 13*16).selected(64+6*16, 13*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(6)))
                .add(registry.iconToggle(7, 3, 1, 1).getter((player) -> isIconSelected(7)).icon(7*16, 13*16).selected(64+7*16, 13*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(7)))

                .add(registry.iconToggle(1, 5, 1, 1).getter((player) -> isIconSelected(8)).icon(4*16, 14*16).selected(64+4*16, 14*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(8)))
                .add(registry.iconToggle(3, 5, 1, 1).getter((player) -> isIconSelected(9)).icon(5*16, 14*16).selected(64+5*16, 14*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(9)))
                .add(registry.iconToggle(5, 5, 1, 1).getter((player) -> isIconSelected(10)).icon(6*16, 14*16).selected(64+6*16, 14*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(10)))
                .add(registry.iconToggle(7, 5, 1, 1).getter((player) -> isIconSelected(11)).icon(7*16, 14*16).selected(64+7*16, 14*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(11)))

                .add(registry.iconToggle(1, 7, 1, 1).getter((player) -> isIconSelected(12)).icon(4*16, 15*16).selected(64+4*16, 15*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(12)))
                .add(registry.iconToggle(3, 7, 1, 1).getter((player) -> isIconSelected(13)).icon(5*16, 15*16).selected(64+5*16, 15*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(13)))
                .add(registry.iconToggle(5, 7, 1, 1).getter((player) -> isIconSelected(14)).icon(6*16, 15*16).selected(64+6*16, 15*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(14)))
                .add(registry.iconToggle(7, 7, 1, 1).getter((player) -> isIconSelected(15)).icon(7*16, 15*16).selected(64+7*16, 15*16).hitEvent((component, player, entity1, x, y) -> setIconIndex(15)))
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
