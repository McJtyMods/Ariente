package mcjty.ariente.blocks.defense;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.sounds.ISoundProducer;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static mcjty.ariente.config.ArienteConfiguration.SHIELD_PANEL_LIFE;

public class ForceFieldTile extends GenericTileEntity implements IGuiTile, ITickable, ISoundProducer {

    private static final float SCALE = 28.0f;

    private PanelInfo[] panelInfo = new PanelInfo[PentakisDodecahedron.MAX_TRIANGLES];
    private AxisAlignedBB aabb = null;

    public ForceFieldTile() {
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            panelInfo[i] = null;
        }
    }

    public double getScale() {
        return SCALE;
    }

    public PanelInfo[] getPanelInfo() {
        return panelInfo;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            updateShield();
            collideWithEntities();
        } else {
            ForceFieldRenderer.register(pos);
            ForceFieldSounds.doSounds(this);
        }
    }

    private AxisAlignedBB getShieldAABB() {
        if (aabb == null) {
            double x = pos.getX() + .5;
            double y = pos.getY() + .5;
            double z = pos.getZ() + .5;
            double s = getScale() * 1.03;
            aabb = new AxisAlignedBB(x-s, y-s, z-s, x+s, y+s, z+s);
        }
        return aabb;
    }

    public boolean entityNearField(Entity entity) {
        double x = pos.getX() + .5;
        double y = pos.getY() + .5;
        double z = pos.getZ() + .5;
        double squaredRadius = getScale() * getScale();

        Vec3d fieldCenter = new Vec3d(x, y, z);
        Vec3d entityCenter = entity.getEntityBoundingBox().getCenter();
        double squareDist = fieldCenter.squareDistanceTo(entityCenter);
        return Math.abs(squareDist - squaredRadius) < 10*10;
    }

    private void collideWithEntities() {
        double x = pos.getX() + .5;
        double y = pos.getY() + .5;
        double z = pos.getZ() + .5;
        Vec3d fieldCenter = new Vec3d(x, y, z);
        double squaredRadius = getScale() * getScale();

        boolean changed = false;

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getShieldAABB(), entity -> {
            if (entity instanceof IProjectile) {
                Vec3d entityPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
                double squareDist = fieldCenter.squareDistanceTo(entityPos);
                if (Math.abs(squareDist - squaredRadius) < 6*6) {
                    return true;
                }
                entityPos = new Vec3d((entity.posX + entity.prevPosX) / 2.0, (entity.posY + entity.prevPosY) / 2.0, (entity.posZ + entity.prevPosZ) / 2.0);
                squareDist = fieldCenter.squareDistanceTo(entityPos);
                if (Math.abs(squareDist - squaredRadius) < 6*6) {
                    return true;
                }
            } else if (entity instanceof EntityLivingBase) {
                Vec3d entityCenter = entity.getEntityBoundingBox().getCenter();
                double squareDist = fieldCenter.squareDistanceTo(entityCenter);
                if (Math.abs(squareDist - squaredRadius) < 6*6) {
                    return true;
                }
            }
            return false;
        });

        for (Entity entity : entities) {
            if (entity instanceof IProjectile) {
                Vec3d p1 = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
                Vec3d p2 = new Vec3d(entity.posX, entity.posY, entity.posZ);
                for (PanelInfo info : getPanelInfo()) {
                    if (info != null && info.getLife() > 0) {
                        Vec3d intersection = info.testCollisionSegment(p1, p2, getScale());
                        if (intersection != null) {
//                            world.newExplosion(entity, entity.posX, entity.posY, entity.posZ, 2.0f, false, false);
                            entity.setDead();
                            int life = info.getLife();
                            life -= 10; // @todo make dependant on arrow
                            if (life <= 0) {
                                panelInfo[info.getIndex()] = null;
                                world.newExplosion(entity, entity.posX, entity.posY, entity.posZ, 2.0f, false, false);
                            } else {
                                info.setLife(life);
                                System.out.println("life = " + life + " (index " + info.getIndex() + ")");
                                ArienteMessages.INSTANCE.sendToDimension(
                                        new PacketDamageForcefield(pos, info.getIndex(), intersection),
                                        world.provider.getDimension());
                            }
                            changed = true;
                        }
                    }
                }
            } else {
                for (PanelInfo info : getPanelInfo()) {
                    if (info != null && info.getLife() > 0) {
                        if (info.testCollisionEntity(entity, getScale())) {
                            entity.attackEntityFrom(DamageSource.GENERIC, 20.0f);
                        }
                    }
                }
            }
        }
        if (changed) {
            markDirtyClient();
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

    private void updateShield() {
        boolean changed = false;
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            if (panelInfo[i] == null) {
                createPanelInfo(i);
                // Set a random life
                panelInfo[i].setLife(-(world.rand.nextInt(50)+50));
                changed = true;
            } else {
                PanelInfo info = this.panelInfo[i];
                int life = info.getLife();
                if (life < 0) {
                    // Building up
                    life++;
                    if (life == 0) {
                        life = SHIELD_PANEL_LIFE;
                    }
                    info.setLife(life);
                    changed = true;
                }
            }
        }
        if (changed) {
            markDirtyClient();
        }
    }

    private void createPanelInfo(int i) {
        Triangle triangle = PentakisDodecahedron.getTriangle(i);
        Vec3d offs = triangle.getMid().scale(getScale());
        double x = pos.getX()+.5 + offs.x;
        double y = pos.getY()+.5 + offs.y;
        double z = pos.getZ()+.5 + offs.z;
        panelInfo[i] = new PanelInfo(i, x, y, z);
    }

    private void disableShield() {
        for (int i = 0; i < PentakisDodecahedron.MAX_TRIANGLES; i++) {
            if (panelInfo[i] != null) {
                panelInfo[i] = null;
            }
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
        for (int i = 0 ; i < panelInfo.length ; i++) {
            panelInfo[i] = null;
        }
        int[] lifeIdx = tagCompound.getIntArray("life");
        for (int i = 0 ; i < lifeIdx.length ; i++) {
            if (lifeIdx[i] == 0) {
                panelInfo[i] = null;
            } else {
                createPanelInfo(i);
                panelInfo[i].setLife(lifeIdx[i]);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound = super.writeToNBT(tagCompound);
        int[] lifeIdx = new int[PentakisDodecahedron.MAX_TRIANGLES];
        int i = 0;
        for (PanelInfo info : panelInfo) {
            if (info != null) {
                lifeIdx[i++] = info.getLife();
            } else {
                lifeIdx[i++] = 0;
            }
        }

        tagCompound.setIntArray("life", lifeIdx);
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
