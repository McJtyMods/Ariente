package mcjty.ariente.blocks.defense;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.Config;
import mcjty.ariente.config.DamageConfiguration;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ISoundProducer;
import mcjty.ariente.varia.Triangle;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
// @todo 1.18 import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.ariente.config.Config.SHIELD_PANEL_LIFE;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ForceFieldTile extends GenericTileEntity implements IGuiTile, /* @todo 1.18 ITickableTileEntity, */ ISoundProducer, IPowerReceiver, ICityEquipment, IAlarmMode, IForceFieldTile, IPowerUser {

    private final PanelInfo[] panelInfo = new PanelInfo[PentakisDodecahedron.MAX_TRIANGLES];
    private int[] panelDestroyTimeout = new int[PentakisDodecahedron.MAX_TRIANGLES];    // @todo persist to NBT?
    private AABB aabb = null;
    private int scale = 10;
    private ChunkPos cityCenter;

    // Transient
    private long usingPower = 0;
    private int breakDownSkip = 5;  // After the world is just loaded we skip a few low-power ticks to be sure the powergenerators can start up

    private static int[] shuffledIndices = null;

    public ForceFieldTile(BlockPos pos, BlockState state) {
        super(Registration.FORCEFIELD_TILE.get(), pos, state);
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            panelInfo[i] = null;
            panelDestroyTimeout[i] = 0;
        }
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(ForceFieldTile::new)
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

    @Override
    public long getUsingPower() {
        return usingPower;
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
                quality += panelInfo[i].getLife() / (float) Config.SHIELD_PANEL_LIFE;
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

    //@Override
    public void tickServer() {
        usingPower = 0;
        long desiredPower = calculateIdleUsage();
        if ((!isMachineEnabled()) || !PowerReceiverSupport.consumePower(level, worldPosition, desiredPower, true)) {
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
    }

    //@Override
    public void tickClient() {
        ForceFieldRenderer.register(worldPosition);
        ForceFieldSounds.doSounds(this);
    }

    private long calculateIdleUsage() {
        return scale * 30;
    }

    private AABB getShieldAABB() {
        if (aabb == null) {
            double x = worldPosition.getX() + .5;
            double y = worldPosition.getY() + .5;
            double z = worldPosition.getZ() + .5;
            double s = getScaleDouble() * 1.05;
            aabb = new AABB(x-s, y-s, z-s, x+s, y+s, z+s);
        }
        return aabb;
    }

    public boolean entityNearField(Entity entity) {
        double x = worldPosition.getX() + .5;
        double y = worldPosition.getY() + .5;
        double z = worldPosition.getZ() + .5;
        double radius = getScaleDouble();

        Vec3 fieldCenter = new Vec3(x, y, z);
        AABB box = entity.getBoundingBox();
        Vec3 entityCenter = new Vec3(box.minX + (box.maxX - box.minX) * 0.5D, box.minY + (box.maxY - box.minY) * 0.5D, box.minZ + (box.maxZ - box.minZ) * 0.5D);
        double squareDist = fieldCenter.distanceToSqr(entityCenter);
        return Math.abs(Math.sqrt(squareDist) - radius) < 10;
    }

    private void collideWithEntities() {
        double x = worldPosition.getX() + .5;
        double y = worldPosition.getY() + .5;
        double z = worldPosition.getZ() + .5;
        Vec3 fieldCenter = new Vec3(x, y, z);
        double squaredRadius = getScaleDouble() * getScaleDouble();

        boolean changed = false;

        List<Entity> entities = level.getEntitiesOfClass(Entity.class, getShieldAABB(), entity -> {
            if (entity instanceof Projectile) {
                Vec3 entityPos = entity.position();
                double squareDist = fieldCenter.distanceToSqr(entityPos);
                if (Math.abs(squareDist - squaredRadius) < 10 * 10) {
                    return true;
                }
                entityPos = new Vec3((entity.getX() + entity.xo) / 2.0, (entity.getY() + entity.yo) / 2.0, (entity.getZ() + entity.zo) / 2.0);
                squareDist = fieldCenter.distanceToSqr(entityPos);
                if (Math.abs(squareDist - squaredRadius) < 10 * 10) {
                    return true;
                }
            } else {
                if (entity instanceof IForcefieldImmunity && ((IForcefieldImmunity) entity).isImmuneToForcefield(this)) {
                    return false;
                }
                if (entity instanceof LivingEntity) {
                    if (entity instanceof Player && cityCenter != null) {
                        ICityAISystem system = ArienteWorldCompat.getCityAISystem(level);
                        ICityAI cityAI = system.getCityAI(cityCenter);
                        if (cityAI != null) {
                            Player player = (Player) entity;
                            String forcefieldId = cityAI.getForcefieldId();
                            if (KeyCardItem.hasPlayerKeycard(player, forcefieldId)) {
                                return false;   // No damage, player is protected
                            }
                        }
                    }
                    AABB box = entity.getBoundingBox();
                    Vec3 entityCenter = new Vec3(box.minX + (box.maxX - box.minX) * 0.5D, box.minY + (box.maxY - box.minY) * 0.5D, box.minZ + (box.maxZ - box.minZ) * 0.5D);

                    double squareDist = fieldCenter.distanceToSqr(entityCenter);
                    if (Math.abs(squareDist - squaredRadius) < 10*10) {
                        return true;
                    }
                }
            }
            return false;
        });

        for (Entity entity : entities) {
            if (entity instanceof Projectile) {
                Vec3 p1 = new Vec3(entity.xo, entity.yo, entity.zo);
                Vec3 p2 = entity.position();
                for (PanelInfo info : getPanelInfo()) {
                    if (info != null && info.getLife() > 0) {
                        Vec3 intersection = info.testCollisionSegment(p1, p2, getScaleDouble());
                        if (intersection != null) {
//                            world.newExplosion(entity, entity.getPosX(), entity.getPosY(), entity.getPosZ(), 2.0f, false, false);
                            entity.remove(Entity.RemovalReason.DISCARDED);
                            int life = info.getLife();
                            life -= 10; // @todo make dependant on arrow
                            if (life <= 0) {
                                panelDestroyTimeout[info.getIndex()] = 100;
                                panelInfo[info.getIndex()] = null;
                                level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 2.0f, false, Explosion.BlockInteraction.DESTROY);
                            } else {
                                info.setLife(life);
                                System.out.println("life = " + life + " (index " + info.getIndex() + ")");
                                if (cityCenter != null) {
                                    Player player = determineAttacker(entity);
                                    if (player != null) {
                                        ICityAISystem system = ArienteWorldCompat.getCityAISystem(level);
                                        ICityAI cityAI = system.getCityAI(cityCenter);
                                        if (cityAI != null) {
                                            cityAI.alertCity(player);
                                            system.saveSystem();
                                        }
                                    }
                                }

                                ArienteMessages.INSTANCE.send(PacketDistributor.DIMENSION.with(() -> level.dimension()),
                                        new PacketDamageForcefield(worldPosition, info.getIndex(), intersection));
                            }
                            changed = true;
                        }
                    }
                }
            } else {
                for (PanelInfo info : getPanelInfo()) {
                    if (info != null && info.getLife() > 0) {
                        if (info.testCollisionEntity(entity, getScaleDouble())) {
                            entity.hurt(DamageSource.GENERIC, (float) (double) DamageConfiguration.FORCEFIELD_DAMAGE.get());
                            ((LivingEntity)entity).knockback(1.0f, worldPosition.getX() - entity.getX(), worldPosition.getZ() - entity.getZ());
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
    private Player determineAttacker(Entity entity) {
        if (entity instanceof Projectile) {
            Entity shootingEntity = ((Projectile) entity).getOwner();
            if (shootingEntity instanceof Player) {
                return (Player) shootingEntity;
            }
        } else if (entity instanceof Player) {
            return (Player) entity;
        }
        return null;
    }


    @Override
    public void setRemoved() {
        disableShield();
        if (level.isClientSide) {
            ForceFieldRenderer.unregister(worldPosition);
        }
        super.setRemoved();
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
                    if (PowerReceiverSupport.consumePower(level, worldPosition, PowerConfiguration.FORCEFIELD_BUILDUP_POWER.get(), true)) {
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
        Vec3 offs = triangle.getMid().scale(getScaleDouble());
        double x = worldPosition.getX()+.5 + offs.x;
        double y = worldPosition.getY()+.5 + offs.y;
        double z = worldPosition.getZ()+.5 + offs.z;
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
    public void setup(ICityAI cityAI, Level world, boolean firstTime) {
        if (firstTime) {
            setRSMode(RedstoneMode.REDSTONE_IGNORED);
        }
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
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
        CompoundTag info = tagCompound.getCompound("Info");
        if (info.contains("scale")) {
            scale = info.getInt("scale");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        int[] lifeIdx = new int[PentakisDodecahedron.MAX_TRIANGLES];
        int i = 0;
        for (PanelInfo info : panelInfo) {
            if (info != null) {
                lifeIdx[i++] = info.getLife();
            } else {
                lifeIdx[i++] = 0;
            }
        }

        tagCompound.putIntArray("life", lifeIdx);
        getOrCreateInfo(tagCompound).putInt("scale", scale);
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
