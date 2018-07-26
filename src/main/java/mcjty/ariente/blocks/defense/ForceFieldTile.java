package mcjty.ariente.blocks.defense;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.ariente.varia.Triangle;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ForceFieldTile extends GenericTileEntity implements IGuiTile, ITickable {

    private static final float SCALE = 28.0f;

    private PanelInfo[] panelInfo = new PanelInfo[PentakisDodecahedron.MAX_TRIANGLES];
//    private int[] entityIds = new int[PentakisDodecahedron.MAX_TRIANGLES];
    private AxisAlignedBB aabb = null;

    public ForceFieldTile() {
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
//            entityIds[i] = -1;
            panelInfo[i] = null;
        }
    }

//    public int[] getEntityIds() {
//        return entityIds;
//    }


    public PanelInfo[] getPanelInfo() {
        return panelInfo;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            activateShield();
            collideWithEntities();
        } else {
            ForceFieldRenderer.register(pos);
        }
    }

    private AxisAlignedBB getShieldAABB() {
        if (aabb == null) {
            double x = pos.getX() + .5;
            double y = pos.getY() + .5;
            double z = pos.getZ() + .5;
            float s = SCALE * 1.03f;
            aabb = new AxisAlignedBB(x-s, y-s, z-s, x+s, y+s, z+s);
        }
        return aabb;
    }

    private void collideWithEntities() {
        double x = pos.getX() + .5;
        double y = pos.getY() + .5;
        double z = pos.getZ() + .5;
        Vec3d fieldCenter = new Vec3d(x, y, z);
        double squaredRadius = SCALE * SCALE;

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getShieldAABB(), entity -> {
            if (entity instanceof IProjectile || entity instanceof EntityLivingBase) {
                Vec3d entityCenter = entity.getEntityBoundingBox().getCenter();
                double squareDist = fieldCenter.squareDistanceTo(entityCenter);
                if (Math.abs(squareDist - squaredRadius) < 6*6) {
                    return true;
                }
            }
            return false;
        });

        for (Entity entity : entities) {
            for (PanelInfo info : getPanelInfo()) {
                if (info != null) {
                    if (info.testCollision(entity, entity instanceof IProjectile ? 3.0 : 0.0)) {
                        System.out.println("ForceFieldTile.collideWithEntities: " + entity.getName());
                        if (entity instanceof IProjectile) {
                            // Use entity prevPosX to trace the path with the triangle
                            world.newExplosion(entity, entity.posX, entity.posY, entity.posZ, 2.0f, false, false);
                            entity.setDead();
                        } else if (entity instanceof EntityLivingBase) {
                            entity.attackEntityFrom(DamageSource.GENERIC, 20.0f);
                        }
                    }
                }
            }


//            for (int id : getEntityIds()) {
//                if (id != -1) {
//                    Entity pan = world.getEntityByID(id);
//                    if (pan instanceof ForceFieldPanelEntity) {
//                        ForceFieldPanelEntity panel = (ForceFieldPanelEntity) pan;
//                        if (panel.testCollision(entity, entity instanceof IProjectile ? 3.0 : 0.0)) {
//                            System.out.println("ForceFieldTile.collideWithEntities: " + entity.getName());
//                            if (entity instanceof IProjectile) {
//                                // Use entity prevPosX to trace the path with the triangle
//                                world.newExplosion(pan, entity.posX, entity.posY, entity.posZ, 2.0f, false, false);
//                                entity.setDead();
//                            } else if (entity instanceof EntityLivingBase) {
//                                entity.attackEntityFrom(DamageSource.GENERIC, 20.0f);
//                            }
//                        }
//                    }
//                }
//            }
        }
    }

    @Override
    public void invalidate() {
        disableShield();
        if (world.isRemote) {
            ForceFieldRenderer.unregister(pos);
        }
        super.invalidate();
    }

    private void activateShield() {
        boolean changed = false;
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            if (panelInfo[i] == null) {
                Triangle triangle = PentakisDodecahedron.getTriangle(i);
                Vec3d offs = triangle.getMid().scale(SCALE);
                double x = pos.getX()+.5 + offs.x;
                double y = pos.getY()+.5 + offs.y;
                double z = pos.getZ()+.5 + offs.z;
                panelInfo[i] = new PanelInfo(i, x, y, z, SCALE);
                changed = true;
            }
//            if (entityIds[i] == -1) {
//                Triangle triangle = PentakisDodecahedron.getTriangle(i);
//                Vec3d offs = triangle.getMid().scale(SCALE);
//                ForceFieldPanelEntity entity = new ForceFieldPanelEntity(world, i, SCALE, offs);
//                double x = pos.getX()+.5 + offs.x;
//                double y = pos.getY()+.5 + offs.y;
//                double z = pos.getZ()+.5 + offs.z;
//                entity.setPosition(x, y, z);
//                entity.setLocationAndAngles(x, y, z, 0, 0);
//                world.spawnEntity(entity);
//                entityIds[i] = entity.getEntityId();
//                changed = true;
//            }
        }
        if (changed) {
            markDirtyClient();
        }
    }

    private void disableShield() {
        for (int i = 0; i < PentakisDodecahedron.MAX_TRIANGLES; i++) {
            if (panelInfo[i] != null) {
                panelInfo[i] = null;
            }
//            if (entityIds[i] != -1) {
//                Entity entity = world.getEntityByID(entityIds[i]);
//                if (entity != null) {
//                    entity.setDead();
//                }
//                entityIds[i] = -1;
//            }
        }
        markDirtyClient();
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
//        int[] entities = tagCompound.getIntArray("entities");
//        if (entities.length == entityIds.length) {
//            System.arraycopy(entities, 0, entityIds, 0, entities.length);
//        }
        for (int i = 0 ; i < panelInfo.length ; i++) {
            panelInfo[i] = null;
        }
        NBTTagList list = tagCompound.getTagList("panels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            int index = compound.getInteger("idx");
            double x = compound.getDouble("x");
            double y = compound.getDouble("y");
            double z = compound.getDouble("z");
            double scale = compound.getDouble("scale");
            panelInfo[index] = new PanelInfo(index, x, y, z, scale);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound = super.writeToNBT(tagCompound);
        NBTTagList list = new NBTTagList();
        for (PanelInfo info : panelInfo) {
            if (info != null) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger("idx", info.getIndex());
                compound.setDouble("x", info.getX());
                compound.setDouble("y", info.getY());
                compound.setDouble("z", info.getZ());
                compound.setDouble("scale", info.getScale());
                list.appendTag(compound);
            }
        }

        tagCompound.setTag("panels", list);
//        tagCompound.setIntArray("entities", entityIds);
        // @todo
        return tagCompound;
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
    public IGuiComponent createGui(HoloGuiEntity entity, String tag) {
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(0, 0, 1, 1, "0", 0xffffff))
                .add(new HoloText(1, 0, 1, 1, "1", 0xffffff))
                .add(new HoloText(2, 0, 1, 1, "2", 0xffffff))
                .add(new HoloText(3, 0, 1, 1, "3", 0xffffff))
                .add(new HoloText(4, 0, 1, 1, "4", 0xffffff))
                .add(new HoloText(5, 0, 1, 1, "5", 0xffffff))
                .add(new HoloText(6, 0, 1, 1, "6", 0xffffff))
                .add(new HoloText(7, 0, 1, 1, "7", 0xffffff))
                .add(new HoloText(0, 1, 1, 1, "1", 0x00ff00))
                .add(new HoloText(0, 2, 1, 1, "2", 0x00ff00))
                .add(new HoloText(0, 3, 1, 1, "3", 0x00ff00))
                .add(new HoloText(0, 4, 1, 1, "4", 0x00ff00))
                .add(new HoloText(0, 5, 1, 1, "5", 0x00ff00))
                .add(new HoloText(0, 6, 1, 1, "6", 0x00ff00))
                .add(new HoloText(0, 7, 1, 1, "7", 0x00ff00))
                .add(new HoloText(7, 7, 1, 1, "X", 0xff0000));
    }

    @Override
    public void syncToServer() {

    }
}
