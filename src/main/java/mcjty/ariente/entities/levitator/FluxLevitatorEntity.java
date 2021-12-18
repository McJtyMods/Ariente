package mcjty.ariente.entities.levitator;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IFluxLevitatorEntity;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.CloseStrategy;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FluxLevitatorEntity extends Entity implements IFluxLevitatorEntity {

    private static final EntityDataAccessor<Integer> ROLLING_AMPLITUDE = SynchedEntityData.defineId(FluxLevitatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROLLING_DIRECTION = SynchedEntityData.defineId(FluxLevitatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPEED = SynchedEntityData.defineId(FluxLevitatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FluxLevitatorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> HOLO_FRONT = SynchedEntityData.defineId(FluxLevitatorEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> HOLO_BACK = SynchedEntityData.defineId(FluxLevitatorEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private boolean isInReverse;
    private static final int[][][] MATRIX = new int[][][]{
            {{0, 0, -1}, {0, 0, 1}},
            {{-1, 0, 0}, {1, 0, 0}},
            {{-1, -1, 0}, {1, 0, 0}},
            {{-1, 0, 0}, {1, -1, 0}},
            {{0, 0, -1}, {0, -1, 1}},
            {{0, -1, -1}, {0, 0, 1}},
            {{0, 0, 1}, {1, 0, 0}},
            {{0, 0, 1}, {-1, 0, 0}},
            {{0, 0, -1}, {-1, 0, 0}},
            {{0, 0, -1}, {1, 0, 0}}};

    private int turnProgress;
    private double levitatorX;
    private double levitatorY;
    private double levitatorZ;
    private double levitatorYaw;
    private double levitatorPitch;
    private double velocityX;       // Client side
    private double velocityY;       // Client side
    private double velocityZ;       // Client side
    private int timeUntilPortal;

    public static final float DEFAULT_MAX_SPEED_AIR_LATERAL = 0.4f;
    public static final double DEFAULT_DRAG_AIR = 0.95D;
    public static final double SLOPE_ADJUSTMENT = 0.0078125D;
    private static final float LENGTH = 2.5f;

    /**
     * The carts max speed when traveling on rails. Carts going faster
     * than 1.1 cause issues with chunk loading. Carts cant traverse slopes or
     * corners at greater than 0.5 - 0.6. This value is compared with the rails
     * max speed and the carts current speed cap to determine the carts current
     * max speed. A normal rail's max speed is 0.4.
     */
    public static final float MAX_CART_SPEED_ON_RAIL = 1.2f;

    private IHoloGuiEntity holoGuiFront;
    private IHoloGuiEntity holoGuiBack;

    private BlockPos desiredDestination = null;

    public FluxLevitatorEntity(EntityType<? extends FluxLevitatorEntity> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.blocksBuilding = true;
        // @todo 1.14
//        this.setSize(1.30F, 0.9F);
    }


    public int getSpeed() {
        return entityData.get(SPEED);
    }

    @Override
    public void changeSpeed(int speed) {
        if (speed < -80) {
            speed = -80;
        } else if (speed > 80) {
            speed = 80;
        }
        this.entityData.set(SPEED, speed);
    }

    // @todo 1.14
//    @Override
//    protected void setSize(float width, float height) {
//        if (width != this.width || height != this.height) {
//            float f = this.width;
//            this.width = width;
//            this.height = height;
//
//            if (this.width < f) {
//                double dx = LENGTH / 2.0D;
//                double d0 = width / 2.0D;
//                this.setEntityBoundingBox(new AxisAlignedBB(this.getPosX() - dx, this.getPosY(), this.getPosZ() - d0, this.getPosX() + dx, this.getPosY() + this.height, this.getPosZ() + d0));
//                return;
//            }
//
//            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
//            this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + this.LENGTH, axisalignedbb.minY + this.height, axisalignedbb.minZ + this.width));
//
//            if (this.width > f && !this.firstUpdate && !world.isRemote) {
//                this.move(MoverType.SELF, (f - this.LENGTH), 0.0D, (f - this.width));
//            }
//        }
//    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return true;    // @todo
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return null;
    }

    @Override
    public void positionRider(Entity passenger) {
        if (this.hasPassenger(passenger)) {
            if (!(passenger instanceof IHoloGuiEntity)) {
                super.positionRider(passenger);
            }
        }
    }

    public IHoloGuiEntity getHoloGuiFront() {
        if (holoGuiFront == null) {
            if (getHoloFrontUUID() != null) {
                for (Entity entity : level.getEntitiesOfClass(Ariente.guiHandler.getHoloEntityClass(), getBoundingBox().inflate(10))) {
                    if (entity instanceof IHoloGuiEntity && getHoloFrontUUID().equals(entity.getUUID())) {
                        holoGuiFront = (IHoloGuiEntity) entity;
                        entity.startRiding(this);
                        break;
                    }
                }
            }
            if (holoGuiFront == null && !level.isClientSide ) {
                IHoloGuiEntity holoGui = Ariente.guiHandler.openHoloGuiRelative(this, new Vec3(0, .5, 1), ModGuis.GUI_LEVITATOR);
                holoGui.setScale(0.75f);
                holoGui.setCloseStrategy(CloseStrategy.NEVER);
                holoGui.getEntity().startRiding(this);
                this.holoGuiFront = holoGui;
                setHoloFrontUUID(holoGui.getEntity().getUUID());
            }
        }
        return holoGuiFront;
    }

    public IHoloGuiEntity getHoloGuiBack() {
        if (holoGuiBack == null) {
            if (getHoloBackUUID() != null) {
                for (Entity entity : level.getEntitiesOfClass(Ariente.guiHandler.getHoloEntityClass(), getBoundingBox().inflate(10))) {
                    if (getHoloBackUUID().equals(entity.getUUID())) {
                        holoGuiBack = (IHoloGuiEntity) entity;
                        entity.startRiding(this);
                        break;
                    }
                }
            }
            if (holoGuiBack == null && !level.isClientSide ) {
                IHoloGuiEntity holoGui = Ariente.guiHandler.openHoloGuiRelative(this, new Vec3(0, .5, 1), ModGuis.GUI_LEVITATOR);
                holoGui.setScale(0.75f);
                holoGui.setCloseStrategy(CloseStrategy.NEVER);
                holoGui.getEntity().startRiding(this);
                this.holoGuiBack = holoGui;
                setHoloBackUUID(holoGui.getEntity().getUUID());
            }
        }
        return holoGuiBack;
    }

    public static FluxLevitatorEntity create(Level worldIn, double x, double y, double z) {
        FluxLevitatorEntity entity = new FluxLevitatorEntity(Registration.ENTITY_FLUX_LEVITATOR.get(), worldIn);
        entity.setPos(x, y, z);
        entity.setDeltaMovement(0, 0, 0);
        entity.xo = x;
        entity.yo = y;
        entity.zo = z;
        return entity;
    }

    @Override
    public void move(MoverType typeIn, Vec3 pos) {
        super.move(typeIn, pos);
        updateHoloGui();        // @todo check if needed
    }

    @Override
    public void moveTo(double x, double y, double z, float yaw, float pitch) {
        super.moveTo(x, y, z, yaw, pitch);
        updateHoloGui();        // @todo check if needed
    }

    @Override
    public void absMoveTo(double x, double y, double z, float yaw, float pitch) {
        super.absMoveTo(x, y, z, yaw, pitch);
        updateHoloGui();        // @todo check if needed
    }

    public void setHoloFrontUUID(UUID holoFront) {
        this.entityData.set(HOLO_FRONT, Optional.ofNullable(holoFront));
    }

    public UUID getHoloFrontUUID() {
        return (UUID) ((Optional) this.entityData.get(HOLO_FRONT)).orElse(null);
    }


    public void setHoloBackUUID(UUID holoBack) {
        this.entityData.set(HOLO_BACK, Optional.ofNullable(holoBack));
    }

    public UUID getHoloBackUUID() {
        return (UUID) ((Optional) this.entityData.get(HOLO_BACK)).orElse(null);
    }


    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return MovementEmission.EVENTS;
    }

    @Override
    protected boolean canRide(Entity entityIn) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ROLLING_AMPLITUDE, 0);
        this.entityData.define(ROLLING_DIRECTION, 0);
        this.entityData.define(SPEED, 0);
        this.entityData.define(DAMAGE, 0.0F);
        this.entityData.define(HOLO_FRONT, Optional.empty());
        this.entityData.define(HOLO_BACK, Optional.empty());
    }

    // @todo 1.16 @Override
    @Nullable
    public AABB getCollisionBox(Entity entityIn) {
        return entityIn.isPushable() ? entityIn.getBoundingBox() : null;
    }

    // @todo 1.16 @Override
    @Nullable
    public AABB getCollisionBoundingBox() {
        return null;
    }

    @Override
    public boolean isPushable() {
        return true;
    }


    @Override
    public double getPassengersRidingOffset() {
        return 0.0D;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level.isClientSide && this.isAlive()) {
            if (this.isInvulnerableTo(source)) {
                return false;
            } else {
                this.setRollingDirection(-this.getRollingDirection());
                this.setRollingAmplitude(10);
                this.markHurt();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean flag = source.getEntity() instanceof Player && ((Player) source.getEntity()).getAbilities().instabuild;

                if (flag || this.getDamage() > 40.0F) {
                    this.ejectPassengers();

                    if (flag && !this.hasCustomName()) {
                        this.remove(RemovalReason.DISCARDED);
                    } else {
                        this.killLevitator(source);
                    }
                }

                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (getHoloGuiFront() != null) {
            holoGuiFront.getEntity().stopRiding();
            holoGuiFront.getEntity().remove(reason);
            holoGuiFront = null;
        }
        if (getHoloGuiBack() != null) {
            holoGuiBack.getEntity().stopRiding();
            holoGuiBack.getEntity().remove(reason);
            holoGuiBack = null;
        }
        super.remove(reason);
    }

    public void killLevitator(DamageSource source) {
        this.remove(RemovalReason.KILLED);

        if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemstack = new ItemStack(Registration.FLUX_LEVITATOR.get(), 1);

            if (this.hasCustomName()) {
                itemstack.setHoverName(this.getCustomName());
            }

            this.spawnAtLocation(itemstack, 0.0F);
        }
    }

    @Override
    public void animateHurt() {
        this.setRollingDirection(-this.getRollingDirection());
        this.setRollingAmplitude(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean isPickable() {
        return this.isAlive();
    }

    @Override
    public Direction getMotionDirection() {
        return isInReverse
                ? this.getDirection().getOpposite().getClockWise()
                : this.getDirection().getClockWise();
//        return this.getHorizontalFacing().rotateY();
    }

    @Override
    public void tick() {
        if (this.getRollingAmplitude() > 0) {
            this.setRollingAmplitude(this.getRollingAmplitude() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.getY() < -64.0D) {
            this.outOfWorld();
        }

        handlePortal();

        if (level.isClientSide) {
            onUpdateClient();
        } else {
            onUpdateServer();
        }

        updateHoloGui();
    }

    private void onUpdateServer() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (!this.isNoGravity()) {
            setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y - 0.04D, getDeltaMovement().z);
        }

        int floorX = Mth.floor(this.getX());
        int floorY = Mth.floor(this.getY());
        int floorZ = Mth.floor(this.getZ());

        Block block = level.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockPos blockpos = new BlockPos(floorX, floorY, floorZ);
        BlockState state = level.getBlockState(blockpos);

        if (isValidBeamBlock(state.getBlock())) {
            this.moveAlongTrack(blockpos, state);
        } else {
            this.moveDerailedLevitator();
        }

        this.checkInsideBlocks();
        this.setXRot(0.0F);
        double dx = this.xo - this.getX();
        double dz = this.zo - this.getZ();

        if (dx * dx + dz * dz > 0.001D) {
            this.setYRot((float) (Mth.atan2(dz, dx) * 180.0D / Math.PI));
            if (isInReverse) {
                setYRot(getYRot()+180);
            }
        } else {
            // If we couldn't move for some reason we slowly decrease speed
            if (getSpeed() > 0) {
                changeSpeed(getSpeed() - 1);
            } else if (getSpeed() < 0) {
                changeSpeed(getSpeed() + 1);
            }
        }

        double angle = Mth.wrapDegrees(this.getYRot() - this.yRotO);
        if (angle < -170.0D || angle >= 170.0D) {
            setYRot(getYRot()+180);
//            this.isInReverse = !this.isInReverse;
            isInReverse = false;
        }

        this.setRot(this.getYRot(), this.getXRot());

        handleEntityCollision();
        // @todo 1.16 handleWaterMovement();
    }

    private boolean isValidBeamBlock(Block block) {
        return block == Registration.FLUX_BEAM.get() || block == Registration.FLUX_BEND_BEAM.get();
    }

    private void handleEntityCollision() {
        AABB box;
        box = this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D);

        if (getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z > 0.01D) {
            List<Entity> list = level.getEntities(this, box, entity -> true); // @todo 1.14 EntitySelectors.getTeamCollisionPredicate(this));

            if (!list.isEmpty()) {
                for (Entity ent : list) {
                    if (!(ent instanceof Player) && !(ent instanceof IronGolem) && !(ent instanceof FluxLevitatorEntity) && !this.isVehicle() && !ent.isPassenger()) {
                        ent.startRiding(this);
                    } else {
                        ent.push(this);
                    }
                }
            }
        } else {
            for (Entity entity : level.getEntities(this, box)) {
                if (!this.hasPassenger(entity) && entity.isPushable() && entity instanceof FluxLevitatorEntity) {
                    entity.push(this);
                }
            }
        }
    }

    private void onUpdateClient() {
        if (turnProgress > 0) {
            double turnX = getX() + (levitatorX - getX()) / turnProgress;
            double turnY = getY() + (levitatorY - getY()) / turnProgress;
            double turnZ = getZ() + (levitatorZ - getZ()) / turnProgress;
            double yaw = Mth.wrapDegrees(levitatorYaw - getYRot());
            setYRot((float) (getYRot() + yaw / turnProgress));
            setXRot((float) (getXRot() + (levitatorPitch - getXRot()) / turnProgress));
            --turnProgress;
            setPos(turnX, turnY, turnZ);
            setRot(getYRot(), getXRot());
        } else {
            setPos(getX(), getY(), getZ());
            setRot(getYRot(), getXRot());
        }
    }

    private void handlePortal() {
        if (!level.isClientSide && level instanceof ServerLevel) {
            MinecraftServer minecraftserver = level.getServer();
            int i = getPortalWaitTime();

            if (this.isInsidePortal) {
                if (minecraftserver.isNetherEnabled()) {
                    if (!this.isPassenger() && this.portalTime++ >= i) {
                        this.portalTime = i;
                        this.timeUntilPortal = this.getDimensionChangingDelay();
                        ServerLevel id;

                        if (this.level.dimension() == Level.NETHER) {
                            id = minecraftserver.getLevel(Level.OVERWORLD);
                        } else {
                            id = minecraftserver.getLevel(Level.NETHER);
                        }

                        this.changeDimension(id);
                    }

                    this.isInsidePortal = false;
                }
            } else {
                if (this.portalTime > 0) {
                    this.portalTime -= 4;
                }

                if (this.portalTime < 0) {
                    this.portalTime = 0;
                }
            }

            if (this.timeUntilPortal > 0) {
                --this.timeUntilPortal;
            }
        }
    }

    public void updateHoloGui() {
        if (getHoloGuiFront() != null) {
            updateHoloGui(this.holoGuiFront, 1);
        }
        if (getHoloGuiBack() != null) {
            updateHoloGui(this.holoGuiBack, -1);
        }
    }

    // Client side only
    private void updateHoloGui(IHoloGuiEntity holo, int offset) {
        Pair<Float, Float> pair = calculateYawPitch();
        float yaw = pair.getLeft() + offset * 90;
        float pitch = pair.getRight();

        Vec3 vec3d = getPosOffset(getX(), getY(), getZ(), offset);
        if (vec3d != null) {
            double x = vec3d.x;
            double y = vec3d.y + .38;
            double z = vec3d.z;

            holo.getEntity().moveTo(x, y, z, yaw, pitch);
            holo.getEntity().teleportTo(x, y, z);
        }
    }

    private double getMaximumSpeed() {
        return Math.abs(getSpeed()) / 25.0;
    }

    private void moveDerailedLevitator() {
        double speed = onGround ? this.getMaximumSpeed() : DEFAULT_MAX_SPEED_AIR_LATERAL;
        double motionX = getDeltaMovement().x;
        double motionY = getDeltaMovement().y;
        double motionZ = getDeltaMovement().z;
        motionX = Mth.clamp(motionX, -speed, speed);
        motionZ = Mth.clamp(motionZ, -speed, speed);

        double moveY = motionY;

        if (this.onGround) {
            motionX *= 0.5D;
            motionY *= 0.5D;
            motionZ *= 0.5D;
        }

        setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, moveY, motionZ));

        if (!this.onGround) {
            motionX = getDeltaMovement().x;
            motionY = getDeltaMovement().y;
            motionZ = getDeltaMovement().z;
            motionX *= DEFAULT_DRAG_AIR;
            motionY *= DEFAULT_DRAG_AIR;
            motionZ *= DEFAULT_DRAG_AIR;
            setDeltaMovement(motionX, motionY, motionZ);
        }
    }

    private void moveAlongTrack(BlockPos pos, BlockState state) {
        this.fallDistance = 0.0F;
        Vec3 oldPos = this.getPos(this.getX(), this.getY(), this.getZ());

        // @todo 1.15 is setRawPosition right?
        //        this.posY = pos.getY();
        this.setPosRaw(getX(), pos.getY(), getZ());

        int speed = getSpeed();
        boolean powered = speed != 0;    // Like powered
        boolean unpowered = speed == 0;

        RailShape dir = getBeamDirection(state);
//        handleBeamAscend(dir);        // Ascend not supported

        double motionX = getDeltaMovement().x;
        double motionY = getDeltaMovement().y;
        double motionZ = getDeltaMovement().z;

        int[][] aint = MATRIX[dir.ordinal()];// @todo 1.15 is this right?
        double ddx = (aint[1][0] - aint[0][0]);
        double ddz = (aint[1][2] - aint[0][2]);
        double ddist = Math.sqrt(ddx * ddx + ddz * ddz);
        double direction = motionX * ddx + motionZ * ddz;
        if (direction < 0.0D) {
            ddx = -ddx;
            ddz = -ddz;
        }

        double motionLength = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (motionLength > 2.0D) {
            motionLength = 2.0D;
        }

        motionX = motionLength * ddx / ddist;
        motionZ = motionLength * ddz / ddist;
        setDeltaMovement(motionX, motionY, motionZ);

        if (speed != 0) {
            handleLivingMotion(speed, dir);
        }

        if (unpowered) {
            restrictMotionUnpowered();
        }

        double dx1 = pos.getX() + 0.5D + aint[0][0] * 0.5D;
        double dx2 = pos.getX() + 0.5D + aint[1][0] * 0.5D;
        double dz1 = pos.getZ() + 0.5D + aint[0][2] * 0.5D;
        double dz2 = pos.getZ() + 0.5D + aint[1][2] * 0.5D;
        ddx = dx2 - dx1;
        ddz = dz2 - dz1;
        double d10;

        if (ddx == 0.0D) {
            d10 = this.getZ() - pos.getZ();
        } else if (ddz == 0.0D) {
            d10 = this.getX() - pos.getX();
        } else {
            double d11 = this.getX() - dx1;
            double d12 = this.getZ() - dz1;
            d10 = (d11 * ddx + d12 * ddz) * 2.0D;
        }

        double posX = dx1 + ddx * d10;
        double posZ = dz1 + ddz * d10;
        this.setPos(posX, this.getY(), posZ);
        this.moveLevitatorOnBeam(pos);

        if (aint[0][1] != 0 && Mth.floor(this.getX()) - pos.getX() == aint[0][0] && Mth.floor(this.getZ()) - pos.getZ() == aint[0][2]) {
            this.setPos(this.getX(), this.getY() + aint[0][1], this.getZ());
        } else if (aint[1][1] != 0 && Mth.floor(this.getX()) - pos.getX() == aint[1][0] && Mth.floor(this.getZ()) - pos.getZ() == aint[1][2]) {
            this.setPos(this.getX(), this.getY() + aint[1][1], this.getZ());
        }

        this.applyDrag();
        Vec3 newPos = this.getPos(this.getX(), this.getY(), this.getZ());

        if (newPos != null && oldPos != null) {
            double d14 = (oldPos.y - newPos.y) * 0.05D;
            motionLength = Math.sqrt(motionX * motionX + motionZ * motionZ);

            if (motionLength > 0.0D) {
                motionX = getDeltaMovement().x;
                motionY = getDeltaMovement().y;
                motionZ = getDeltaMovement().z;

                motionX = motionX / motionLength * (motionLength + d14);
                motionZ = motionZ / motionLength * (motionLength + d14);
                setDeltaMovement(motionX, motionY, motionZ);
            }

            this.setPos(this.getX(), newPos.y, this.getZ());
        }

        int floorX = Mth.floor(this.getX());
        int floorZ = Mth.floor(this.getZ());

        if (floorX != pos.getX() || floorZ != pos.getZ()) {
            motionLength = Math.sqrt(motionX * motionX + motionZ * motionZ);
            motionX = getDeltaMovement().x;
            motionY = getDeltaMovement().y;
            motionZ = getDeltaMovement().z;
            motionX = motionLength * (floorX - pos.getX());
            motionZ = motionLength * (floorZ - pos.getZ());
            setDeltaMovement(motionX, motionY, motionZ);
        }

        if (powered) {
            handlePoweredMotion(pos, dir);
        }
    }

//    private void handleBeamAscend(EnumRailDirection dir) {
//        double slopeAdjustment = SLOPE_ADJUSTMENT;
//        switch (dir) {
//            case ASCENDING_EAST:
//                this.motionX -= slopeAdjustment;
//                ++this.getPosY();
//                break;
//            case ASCENDING_WEST:
//                this.motionX += slopeAdjustment;
//                ++this.getPosY();
//                break;
//            case ASCENDING_NORTH:
//                this.motionZ += slopeAdjustment;
//                ++this.getPosY();
//                break;
//            case ASCENDING_SOUTH:
//                this.motionZ -= slopeAdjustment;
//                ++this.getPosY();
//        }
//    }

    private void handleLivingMotion(int speed, RailShape dir) {

        double maxMotion;

        double motionX = this.getDeltaMovement().x;
        double motionY = this.getDeltaMovement().y;
        double motionZ = this.getDeltaMovement().z;

        float yaw;
        if (dir == RailShape.NORTH_SOUTH) {
            if (speed > 0) {
                yaw = -360;
            } else {
                yaw = -180;
            }
        } else if (dir == RailShape.EAST_WEST) {
            if (speed > 0) {
                yaw = -90;
            } else {
                yaw = -270;
            }
        } else {
            // Slow down in bends
            double dist = Math.sqrt(motionX * motionX + motionZ * motionZ) ;
            maxMotion = Math.abs(speed / 200.0f);
            if (dist > maxMotion) {
                motionX /= dist / maxMotion;
                motionZ /= dist / maxMotion;
            }
            setDeltaMovement(motionX, motionY, motionZ);

            return;
        }

        maxMotion = Math.abs(speed / 25.0f);

        double dx = -Math.sin((yaw * 0.017453292F));
        double dz = Math.cos((yaw * 0.017453292F));
        double dist = motionX * motionX + motionZ * motionZ;

        if (dist < 0.01D) {
            // On a straight track we kickstart some motion
            if (dx > 0) {
                motionX = Math.abs(motionX) + dx * 0.1D;
            } else {
                motionX = -Math.abs(motionX) + dx * 0.1D;
            }
            if (dz > 0) {
                motionZ = Math.abs(motionZ) + dz * 0.1D;
            } else {
                motionZ = -Math.abs(motionZ) + dz * 0.1D;
            }
        }

        // Restrict speed
        dist = Math.sqrt(motionX * motionX + motionZ * motionZ) ;
        if (dist > maxMotion) {
            motionX /= dist / maxMotion;
            motionZ /= dist / maxMotion;
        }
        setDeltaMovement(motionX, motionY, motionZ);
    }

    private void handlePoweredMotion(BlockPos pos, RailShape dir) {
        double motionX = this.getDeltaMovement().x;
        double motionY = this.getDeltaMovement().y;
        double motionZ = this.getDeltaMovement().z;

        double length = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (length > 0.01D) {
            motionX += motionX / length * 0.06D;
            motionZ += motionZ / length * 0.06D;
        } else if (dir == RailShape.EAST_WEST) {
            if (level.getBlockState(pos.west()).isRedstoneConductor(level, pos.west())) {
                motionX = 0.02D;
            } else if (level.getBlockState(pos.east()).isRedstoneConductor(level, pos.east())) {
                motionX = -0.02D;
            }
        } else if (dir == RailShape.NORTH_SOUTH) {
            if (level.getBlockState(pos.north()).isRedstoneConductor(level, pos.north())) {
                motionZ = 0.02D;
            } else if (level.getBlockState(pos.south()).isRedstoneConductor(level, pos.south())) {
                motionZ = -0.02D;
            }
        }
        setDeltaMovement(motionX, motionY, motionZ);
    }

    private void restrictMotionUnpowered() {
        double motionX = this.getDeltaMovement().x;
        double motionY = this.getDeltaMovement().y;
        double motionZ = this.getDeltaMovement().z;

        double length = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (length < 0.03D) {
            motionX = 0.0D;
            motionY = 0.0D;
            motionZ = 0.0D;
        } else {
            motionX *= 0.5D;
            motionY *= 0.0D;
            motionZ *= 0.5D;
        }
        setDeltaMovement(motionX, motionY, motionZ);
    }

    public static RailShape getBeamDirection(BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (state.getBlock() == Registration.FLUX_BEAM.get()) {
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                return RailShape.EAST_WEST;
            } else {
                return RailShape.NORTH_SOUTH;
            }
        } else {
            switch (facing) {
                case NORTH:
                    return RailShape.SOUTH_WEST;
                case SOUTH:
                    return RailShape.NORTH_EAST;
                case WEST:
                    return RailShape.SOUTH_EAST;
                case EAST:
                    return RailShape.NORTH_WEST;
                default:
                    break;
            }
        }
        throw new IllegalStateException("This didn't happen");
    }

    private void applyDrag() {
        double motionX = this.getDeltaMovement().x;
        double motionY = this.getDeltaMovement().y;
        double motionZ = this.getDeltaMovement().z;

        if (this.isVehicle()) {
            motionX *= 0.997D;
            motionY *= 0.0D;
            motionZ *= 0.997D;
        } else {
            motionX *= 0.96D;
            motionY *= 0.0D;
            motionZ *= 0.96D;
        }
        setDeltaMovement(motionX, motionY, motionZ);
    }

    @Override
    public void setPos(double x, double y, double z) {
        setPosRaw(x, y, z);    // @todo 1.15 is this right?
        float f = this.getBbWidth() / 2.0F;
        float f1 = this.getBbHeight();
        this.setBoundingBox(new AABB(x - f, y, z - f, x + f, y + f1, z + f));
    }

    // Client side only
    // Calculate yaw and pitch based on block below the levitator
    private Pair<Float, Float> calculateYawPitch() {
        Vec3 oldPos = getPos(getX(), getY(), getZ());
        float yaw = getYRot();
        float pitch = getXRot();

        if (oldPos != null) {
            Vec3 posUp = getPosOffset(getX(), getY(), getZ(), 0.3D);
            Vec3 posDown = getPosOffset(getX(), getY(), getZ(), -0.3D);

            if (posUp == null) {
                posUp = oldPos;
            }

            if (posDown == null) {
                posDown = oldPos;
            }

            Vec3 newpos = posDown.add(-posUp.x, -posUp.y, -posUp.z);

            if (newpos.lengthSqr() != 0.0D) {
                newpos = newpos.normalize();
                yaw = (float) (Math.atan2(newpos.z, newpos.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(newpos.y) * 73.0D);
            }
        }
        return Pair.of(yaw, pitch);
    }

    // Client only
    @Nullable
    public Vec3 getPosOffset(double x, double y, double z, double offset) {
        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);

        Block block = level.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockState state = level.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (isValidBeamBlock(state.getBlock())) {
            RailShape dir = getBeamDirection(state);
            y = floorY;

            if (dir.isAscending()) {
                y++;
            }

            int[][] aint = MATRIX[dir.ordinal()]; // @todo 1.15 is this right?
            double dx = (aint[1][0] - aint[0][0]);
            double dz = (aint[1][2] - aint[0][2]);
            double length = Math.sqrt(dx * dx + dz * dz);
            dx = dx / length;
            dz = dz / length;
            x = x + dx * offset;
            z = z + dz * offset;

            if (aint[0][1] != 0 && Mth.floor(x) - floorX == aint[0][0] && Mth.floor(z) - floorZ == aint[0][2]) {
                y += aint[0][1];
            } else if (aint[1][1] != 0 && Mth.floor(x) - floorX == aint[1][0] && Mth.floor(z) - floorZ == aint[1][2]) {
                y += aint[1][1];
            }

            return this.getPos(x, y, z);
        } else {
            return null;
        }
    }

    @Nullable
    public Vec3 getPos(double x, double y, double z) {
        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);

        Block block = level.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockState state = level.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (isValidBeamBlock(state.getBlock())) {
            RailShape dir = getBeamDirection(state);
            int[][] aint = MATRIX[dir.ordinal()];  // @todo 1.15 is this right?
            double dx1 = floorX + 0.5D + aint[0][0] * 0.5D;
            double dy1 = floorY + 0.0625D + aint[0][1] * 0.5D;
            double dz1 = floorZ + 0.5D + aint[0][2] * 0.5D;
            double dx2 = floorX + 0.5D + aint[1][0] * 0.5D;
            double dy2 = floorY + 0.0625D + aint[1][1] * 0.5D;
            double dz2 = floorZ + 0.5D + aint[1][2] * 0.5D;
            double dx = dx2 - dx1;
            double dy = (dy2 - dy1) * 2.0D;
            double dz = dz2 - dz1;
            double d9;

            if (dx == 0.0D) {
                d9 = z - floorZ;
            } else if (dz == 0.0D) {
                d9 = x - floorX;
            } else {
                double d10 = x - dx1;
                double d11 = z - dz1;
                d9 = (d10 * dx + d11 * dz) * 2.0D;
            }

            x = dx1 + dx * d9;
            y = dy1 + dy * d9;
            z = dz1 + dz * d9;

            if (dy < 0.0D) {
                ++y;
            }

            if (dy > 0.0D) {
                y += 0.5D;
            }

            return new Vec3(x, y, z);
        } else {
            return null;
        }
    }

    @Override
    public BlockPos getDesiredDestination() {
        return desiredDestination;
    }

    @Override
    public void setDesiredDestination(BlockPos desiredDestination) {
        this.desiredDestination = desiredDestination;
    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public AxisAlignedBB getRenderBoundingBox() {
//        return this.getBoundingBox();
//    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("holoFront")) {
            setHoloFrontUUID(compound.getUUID("holoFront"));
        }
        if (compound.hasUUID("holoBack")) {
            setHoloBackUUID(compound.getUUID("holoBack"));
        }
        changeSpeed(compound.getInt("speed"));
        if (compound.contains("desiredDestX")) {
            desiredDestination = new BlockPos(compound.getInt("desiredDestX"),
                    compound.getInt("desiredDestY"),
                    compound.getInt("desiredDestZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (getHoloFrontUUID() != null) {
            compound.putUUID("holoFront", getHoloFrontUUID());
        }
        if (getHoloBackUUID() != null) {
            compound.putUUID("holoBack", getHoloBackUUID());
        }
        compound.putInt("speed", getSpeed());
        if (desiredDestination != null) {
            compound.putInt("desiredDestX", desiredDestination.getX());
            compound.putInt("desiredDestY", desiredDestination.getY());
            compound.putInt("desiredDestZ", desiredDestination.getZ());
        }
    }

    @Override
    public void push(Entity entityIn) {
        if (!level.isClientSide) {
            if (!entityIn.noPhysics && !this.noPhysics) {
                if (!this.hasPassenger(entityIn)) {
                    double dx = entityIn.getX() - this.getX();
                    double dz = entityIn.getZ() - this.getZ();
                    double length = dx * dx + dz * dz;

                    if (length >= .0001D) {
                        length = Mth.sqrt((float)length);
                        dx = dx / length;
                        dz = dz / length;
                        double invLength = 1.0D / length;

                        if (invLength > 1.0D) {
                            invLength = 1.0D;
                        }

                        dx = dx * invLength;
                        dz = dz * invLength;
                        dx = dx * 0.1D;
                        dz = dz * 0.1D;
                        // @todo 1.18 dx = dx * (1.0F - this.pushthrough);
                        // @todo 1.18 dz = dz * (1.0F - this.pushthrough);
                        dx = dx * 0.5D;
                        dz = dz * 0.5D;

                        if (entityIn instanceof FluxLevitatorEntity) {
                            double ddx = entityIn.getX() - this.getX();
                            double ddz = entityIn.getZ() - this.getZ();
                            Vec3 vec3d = (new Vec3(ddx, 0.0D, ddz)).normalize();
                            Vec3 vec3d1 = (new Vec3(Mth.cos(this.getYRot() * 0.017453292F), 0.0D, Mth.sin(this.getYRot() * 0.017453292F))).normalize();
                            double d6 = Math.abs(vec3d.dot(vec3d1));

                            if (d6 < 0.8D) {
                                return;
                            }

                            double motionX = this.getDeltaMovement().x;
                            double motionY = this.getDeltaMovement().y;
                            double motionZ = this.getDeltaMovement().z;
                            double emotionX = entityIn.getDeltaMovement().x;
                            double emotionY = entityIn.getDeltaMovement().y;
                            double emotionZ = entityIn.getDeltaMovement().z;
                            double d7 = emotionX + motionX;
                            double d8 = emotionZ + motionZ;

                            d7 = d7 / 2.0D;
                            d8 = d8 / 2.0D;
                            motionX *= 0.2D;
                            motionZ *= 0.2D;
                            this.push(d7 - dx, 0.0D, d8 - dz);
                            emotionX *= 0.2D;
                            emotionZ *= 0.2D;
                            entityIn.setDeltaMovement(emotionX, emotionY, emotionZ);
                            entityIn.push(d7 + dx, 0.0D, d8 + dz);

                            this.setDeltaMovement(motionX, motionY, motionZ);
                        } else {
                            this.push(-dx, 0.0D, -dz);
                            entityIn.push(dx / 4.0D, 0.0D, dz / 4.0D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.levitatorX = x;
        this.levitatorY = y;
        this.levitatorZ = z;
        this.levitatorYaw = yaw;
        this.levitatorPitch = pitch;
        this.turnProgress = posRotationIncrements + 2;
        this.setDeltaMovement(velocityX, velocityY, velocityZ);

        updateHoloGui();
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        setDeltaMovement(x, y, z);
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE).floatValue();
    }

    public void setRollingAmplitude(int rollingAmplitude) {
        this.entityData.set(ROLLING_AMPLITUDE, rollingAmplitude);
    }

    public int getRollingAmplitude() {
        return this.entityData.get(ROLLING_AMPLITUDE).intValue();
    }

    public void setRollingDirection(int rollingDirection) {
        this.entityData.set(ROLLING_DIRECTION, rollingDirection);
    }

    public int getRollingDirection() {
        return this.entityData.get(ROLLING_DIRECTION).intValue();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
//        } else if (this.isBeingRidden()) {    // @todo
//            return true;
        } else {
            if (!level.isClientSide) {
                player.startRiding(this);
            }

            return InteractionResult.SUCCESS;
        }
    }


    private BlockPos getCurrentRailPosition() {
        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getY());
        int z = Mth.floor(this.getZ());

        Block block = level.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
        if (isValidBeamBlock(block)) {
            y--;
        }
        return new BlockPos(x, y, z);
    }

    protected double getMaxSpeed() {
        BlockPos pos = this.getCurrentRailPosition();
        BlockState state = level.getBlockState(pos);
        if (!isValidBeamBlock(state.getBlock())) {
            return getMaximumSpeed();
        }

        float railMaxSpeed = 5; // @todo ((BlockRailBase) state.getBlock()).getRailMaxSpeed(world, this, pos);
        return Math.min(railMaxSpeed, MAX_CART_SPEED_ON_RAIL);
    }

    /**
     * Moved to allow overrides.
     * This code handles levitator movement and speed capping when on a rail.
     */
    public void moveLevitatorOnBeam(BlockPos pos) {
        double mX = this.getDeltaMovement().x;
        double mZ = this.getDeltaMovement().z;

        if (this.isVehicle()) {
            mX *= 0.75D;
            mZ *= 0.75D;
        }

        double max = this.getMaxSpeed();
        mX = Mth.clamp(mX, -max, max);
        mZ = Mth.clamp(mZ, -max, max);
        this.move(MoverType.SELF, new Vec3(mX, 0.0D, mZ));
    }
}