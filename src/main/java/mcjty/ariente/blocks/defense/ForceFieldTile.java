package mcjty.ariente.blocks.defense;

import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.ConfigSetup;
import mcjty.ariente.config.DamageConfiguration;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.sounds.ISoundProducer;
import mcjty.ariente.varia.Triangle;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.RedstoneMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

import static mcjty.ariente.config.ConfigSetup.SHIELD_PANEL_LIFE;
import static mcjty.hologui.api.Icons.*;

public class ForceFieldTile extends GenericTileEntity implements IGuiTile, ITickable, ISoundProducer, IPowerReceiver, ICityEquipment, IAlarmMode, IForceFieldTile {

    private PanelInfo[] panelInfo = new PanelInfo[PentakisDodecahedron.MAX_TRIANGLES];
    private int[] panelDestroyTimeout = new int[PentakisDodecahedron.MAX_TRIANGLES];    // @todo persist to NBT?
    private AxisAlignedBB aabb = null;
    private int scale = 10;
    private ChunkPos cityCenter;

    // Transient
    private long usingPower = 0;
    private int breakDownSkip = 5;  // After the world is just loaded we skip a few low-power ticks to be sure the powergenerators can start up

    private static int[] shuffledIndices = null;

    public ForceFieldTile() {
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            panelInfo[i] = null;
            panelDestroyTimeout[i] = 0;
        }
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public boolean isHighAlert() {
        return false;
    }

    @Override
    public void setCityCenter(ChunkPos cityCenter) {
        this.cityCenter = cityCenter;
    }

    public double getScaleDouble() {
        return (double) scale;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
        aabb = null;
    }

    private int getFieldIntegrity() {
        float quality = 0;
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            if (panelInfo[i] != null && panelInfo[i].getLife() > 0) {
                quality += panelInfo[i].getLife() / (float) ConfigSetup.SHIELD_PANEL_LIFE;
            }
        }
        return (int) (quality * 100 / PentakisDodecahedron.MAX_TRIANGLES);
    }

    private void changeScale(int dy) {
        scale += dy;
        if (scale < 3) {
            scale = 3;
        } else if (scale > 50) {
            scale = 50;
        }
        aabb = null;
        disableShield();
        markDirtyClient();
    }



    public PanelInfo[] getPanelInfo() {
        return panelInfo;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            usingPower = 0;
            long desiredPower = calculateIdleUsage();
            if ((!isMachineEnabled()) || !PowerReceiverSupport.consumePower(world, pos, desiredPower, true)) {
                if (breakDownSkip > 0) {
                    breakDownSkip--;
                } else {
                    breakDownShield();
                }
            } else {
                breakDownSkip = 5;
                usingPower += desiredPower;
                updateShield();
            }
            collideWithEntities();
        } else {
            ForceFieldRenderer.register(pos);
            ForceFieldSounds.doSounds(this);
        }
    }

    private long calculateIdleUsage() {
        return scale * 30;
    }

    private AxisAlignedBB getShieldAABB() {
        if (aabb == null) {
            double x = pos.getX() + .5;
            double y = pos.getY() + .5;
            double z = pos.getZ() + .5;
            double s = getScaleDouble() * 1.05;
            aabb = new AxisAlignedBB(x-s, y-s, z-s, x+s, y+s, z+s);
        }
        return aabb;
    }

    public boolean entityNearField(Entity entity) {
        double x = pos.getX() + .5;
        double y = pos.getY() + .5;
        double z = pos.getZ() + .5;
        double radius = getScaleDouble();

        Vec3d fieldCenter = new Vec3d(x, y, z);
        AxisAlignedBB box = entity.getEntityBoundingBox();
        Vec3d entityCenter = new Vec3d(box.minX + (box.maxX - box.minX) * 0.5D, box.minY + (box.maxY - box.minY) * 0.5D, box.minZ + (box.maxZ - box.minZ) * 0.5D);
        double squareDist = fieldCenter.squareDistanceTo(entityCenter);
        return Math.abs(Math.sqrt(squareDist) - radius) < 10;
    }

    private void collideWithEntities() {
        double x = pos.getX() + .5;
        double y = pos.getY() + .5;
        double z = pos.getZ() + .5;
        Vec3d fieldCenter = new Vec3d(x, y, z);
        double squaredRadius = getScaleDouble() * getScaleDouble();

        boolean changed = false;

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getShieldAABB(), entity -> {
            if (entity instanceof IProjectile) {
                Vec3d entityPos = new Vec3d(entity.posX, entity.posY, entity.posZ);
                double squareDist = fieldCenter.squareDistanceTo(entityPos);
                if (Math.abs(squareDist - squaredRadius) < 10 * 10) {
                    return true;
                }
                entityPos = new Vec3d((entity.posX + entity.prevPosX) / 2.0, (entity.posY + entity.prevPosY) / 2.0, (entity.posZ + entity.prevPosZ) / 2.0);
                squareDist = fieldCenter.squareDistanceTo(entityPos);
                if (Math.abs(squareDist - squaredRadius) < 10 * 10) {
                    return true;
                }
            } else {
                if (entity instanceof IForcefieldImmunity && ((IForcefieldImmunity) entity).isImmuneToForcefield(this)) {
                    return false;
                }
                if (entity instanceof EntityLivingBase) {
                    if (entity instanceof EntityPlayer && cityCenter != null) {
                        ICityAISystem system = ArienteWorldCompat.getCityAISystem(world);
                        ICityAI cityAI = system.getCityAI(cityCenter);
                        if (cityAI != null) {
                            EntityPlayer player = (EntityPlayer) entity;
                            String forcefieldId = cityAI.getForcefieldId();
                            if (KeyCardItem.hasPlayerKeycard(player, forcefieldId)) {
                                return false;   // No damage, player is protected
                            }
                        }
                    }
                    AxisAlignedBB box = entity.getEntityBoundingBox();
                    Vec3d entityCenter = new Vec3d(box.minX + (box.maxX - box.minX) * 0.5D, box.minY + (box.maxY - box.minY) * 0.5D, box.minZ + (box.maxZ - box.minZ) * 0.5D);

                    double squareDist = fieldCenter.squareDistanceTo(entityCenter);
                    if (Math.abs(squareDist - squaredRadius) < 10*10) {
                        return true;
                    }
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
                        Vec3d intersection = info.testCollisionSegment(p1, p2, getScaleDouble());
                        if (intersection != null) {
//                            world.newExplosion(entity, entity.posX, entity.posY, entity.posZ, 2.0f, false, false);
                            entity.setDead();
                            int life = info.getLife();
                            life -= 10; // @todo make dependant on arrow
                            if (life <= 0) {
                                panelDestroyTimeout[info.getIndex()] = 100;
                                panelInfo[info.getIndex()] = null;
                                world.newExplosion(entity, entity.posX, entity.posY, entity.posZ, 2.0f, false, false);
                            } else {
                                info.setLife(life);
                                System.out.println("life = " + life + " (index " + info.getIndex() + ")");
                                if (cityCenter != null) {
                                    EntityPlayer player = determineAttacker(entity);
                                    if (player != null) {
                                        ICityAISystem system = ArienteWorldCompat.getCityAISystem(world);
                                        ICityAI cityAI = system.getCityAI(cityCenter);
                                        if (cityAI != null) {
                                            cityAI.alertCity(player);
                                            system.saveSystem();
                                        }
                                    }
                                }

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
                        if (info.testCollisionEntity(entity, getScaleDouble())) {
                            entity.attackEntityFrom(DamageSource.GENERIC, (float) DamageConfiguration.FORCEFIELD_DAMAGE.get());
                            ((EntityLivingBase)entity).knockBack(entity, 1.0f, pos.getX() - entity.posX, pos.getZ() - entity.posZ);
                        }
                    }
                }
            }
        }
        if (changed) {
            markDirtyClient();
        }
    }

    @Nullable
    private EntityPlayer determineAttacker(Entity entity) {
        if (entity instanceof EntityArrow) {
            Entity shootingEntity = ((EntityArrow) entity).shootingEntity;
            if (shootingEntity instanceof EntityPlayer) {
                return (EntityPlayer) shootingEntity;
            }
        } else if (entity instanceof EntityThrowable) {
            EntityLivingBase thrower = ((EntityThrowable) entity).getThrower();
            if (thrower instanceof EntityPlayer) {
                return (EntityPlayer) thrower;
            }
        } else if (entity instanceof EntityPlayer) {
            return (EntityPlayer) entity;
        }
        return null;
    }

    @Override
    public void invalidate() {
        disableShield();
        if (world.isRemote) {
            ForceFieldRenderer.unregister(pos);
        }
        super.invalidate();
    }

    private static int[] getShuffledIndices() {
        if (shuffledIndices == null) {
            shuffledIndices = new int[PentakisDodecahedron.MAX_TRIANGLES];
            List<Integer> idx = new ArrayList<>();
            for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
                idx.add(i);
            }
            Collections.shuffle(idx);
            for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
                shuffledIndices[i] = idx.get(i);
            }
        }
        return shuffledIndices;
    }

    private void breakDownShield() {
        boolean changed = false;
        for (int i = 0; i < PentakisDodecahedron.MAX_TRIANGLES; i++) {
            int randomI = getShuffledIndices()[i];
            if (panelInfo[randomI] != null) {
                PanelInfo info = this.panelInfo[randomI];
                int life = info.getLife();
                if (life <= 0) {
                    changed = true;
                    panelInfo[randomI] = null;
                } else {
                    if (life > 10) {
                        life /= 2;
                    } else {
                        life--;
                    }
                    info.setLife(life);
                    changed = true;
                    if (life == 0) {
                        panelInfo[randomI] = null;
                    }
                }
            }
        }
        if (changed) {
            markDirtyClient();
        }
    }

    private void updateShield() {
        boolean changed = false;
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            int randomI = getShuffledIndices()[i];
            if (panelInfo[randomI] == null) {
                if (panelDestroyTimeout[randomI] > 0) {
                    panelDestroyTimeout[randomI]--;
                } else {
                    createPanelInfo(randomI);
                    // Set a random life
                    panelInfo[randomI].setLife(-100);      // @todo configurable
                    changed = true;
                }
            } else {
                PanelInfo info = this.panelInfo[randomI];
                int life = info.getLife();
                if (life < 0) {
                    // Building up!
                    if (PowerReceiverSupport.consumePower(world, pos, PowerConfiguration.FORCEFIELD_BUILDUP_POWER.get(), true)) {
                        usingPower += PowerConfiguration.FORCEFIELD_BUILDUP_POWER.get();
                        life++;
                        if (life == 0) {
                            life = SHIELD_PANEL_LIFE;
                        }
                        info.setLife(life);
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            markDirtyClient();
        }
    }

    private void createPanelInfo(int i) {
        Triangle triangle = PentakisDodecahedron.getTriangle(i);
        Vec3d offs = triangle.getMid().scale(getScaleDouble());
        double x = pos.getX()+.5 + offs.x;
        double y = pos.getY()+.5 + offs.y;
        double z = pos.getZ()+.5 + offs.z;
        panelInfo[i] = new PanelInfo(i, x, y, z);
        panelDestroyTimeout[i] = 0;
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
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("scale", getScale());
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        if (data.get("scale") instanceof Integer) {
            setScale((Integer) data.get("scale"));
        }
    }

    @Override
    public void setup(ICityAI cityAI, World world, boolean firstTime) {
        if (firstTime) {
            setRSMode(RedstoneMode.REDSTONE_IGNORED);
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        scale = tagCompound.getInteger("scale");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("scale", scale);
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
        probeInfo.text(TextStyleClass.LABEL + "Using: " + TextStyleClass.INFO + usingPower + " flux");
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
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 1, 1, 1).text("Radius").color(registry.color(StyledColor.LABEL)))
                .add(registry.number(3, 2, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> getScale()))

                .add(registry.iconButton(1, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeScale(-8)))
                .add(registry.iconButton(2, 2, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeScale(-1)))
                .add(registry.iconButton(5, 2, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeScale(1)))
                .add(registry.iconButton(6, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeScale(8)))

                .add(registry.text(0, 4, 1, 1).text("Field Integrity").color(registry.color(StyledColor.LABEL)))
                .add(registry.text(1, 5, 1, 1).text(() -> getFieldIntegrity() + "%").color(registry.color(StyledColor.INFORMATION)))


                .add(registry.iconChoice(7, 6, 1, 1)
                        .getter((player) -> getRSModeInt())
                        .addImage(registry.image(REDSTONE_DUST))
                        .addImage(registry.image(REDSTONE_OFF))
                        .addImage(registry.image(REDSTONE_ON))
                        .hitEvent((component, player, entity1, x, y) -> changeMode()))
                ;
    }

    private void changeMode() {
        int current = rsMode.ordinal() + 1;
        if (current >= RedstoneMode.values().length) {
            current = 0;
        }
        setRSMode(RedstoneMode.values()[current]);
        markDirtyClient();
    }


    @Override
    public void syncToClient() {
    }
}
