package mcjty.ariente.entities.fluxelevator;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.CloseStrategy;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FluxElevatorEntity extends Entity {

    private static final EntityDataAccessor<Integer> SPEED = SynchedEntityData.defineId(FluxElevatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FluxElevatorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> HOLO = SynchedEntityData.defineId(FluxElevatorEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final float LENGTH = 2.5f;
    public static final float MAX_SPEED = 1.2f;

    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private int timeUntilPortal;

    private IHoloGuiEntity holoGui;

    private BlockPos desiredDestination = null;

    public FluxElevatorEntity(EntityType<? extends FluxElevatorEntity> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.blocksBuilding = true;
        this.setSize(1.30F, 0.9F);
    }


    public int getSpeed() {
        return entityData.get(SPEED);
    }

    public void changeSpeed(int speed) {
        if (speed < -80) {
            speed = -80;
        } else if (speed > 80) {
            speed = 80;
        }
        this.entityData.set(SPEED, speed);
    }

    // @todo 1.14, this should override from Entity!
    protected void setSize(float width, float height) {
        // @todo 1.14
//        if (width != this.getWidth() || height != this.getHeight()) {
//            float f = this.getWidth();
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
//            if (this.width > f && !this.firstUpdate && !this.world.isRemote) {
//                this.move(MoverType.SELF, (f - this.LENGTH), 0.0D, (f - this.width));
//            }
//        }
    }

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
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void positionRider(Entity passenger) {
        if (this.hasPassenger(passenger)) {
            if (!(passenger instanceof IHoloGuiEntity)) {
                super.positionRider(passenger);
            }
        }
    }

    public IHoloGuiEntity getHoloGui() {
        if (holoGui == null) {
            if (getHoloUUID() != null) {
                for (Entity entity : level.getEntitiesOfClass(Ariente.guiHandler.getHoloEntityClass(), getBoundingBox().inflate(10))) {
                    if (entity instanceof IHoloGuiEntity && getHoloUUID().equals(entity.getUUID())) {
                        holoGui = (IHoloGuiEntity) entity;
                        entity.startRiding(this);
                        break;
                    }
                }
            }
            if (holoGui == null && !level.isClientSide ) {
                IHoloGuiEntity holoGui = Ariente.guiHandler.openHoloGuiRelative(this, new Vec3(0, .5, 1), ModGuis.GUI_ELEVATOR);
                holoGui.setScale(0.75f);
                holoGui.setCloseStrategy(CloseStrategy.NEVER);
                holoGui.getEntity().startRiding(this);
                this.holoGui = holoGui;
                setHoloUUID(holoGui.getEntity().getUUID());
            }
        }
        return holoGui;
    }

    public static FluxElevatorEntity create(Level worldIn, double x, double y, double z) {
        FluxElevatorEntity entity = new FluxElevatorEntity(Registration.ENTITY_ELEVATOR.get(), worldIn);
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

    public void setHoloUUID(UUID holoFront) {
        this.entityData.set(HOLO, Optional.ofNullable(holoFront));
    }

    public UUID getHoloUUID() {
        return (UUID) ((Optional) this.entityData.get(HOLO)).orElse(null);
    }


    // @todo 1.18 @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected boolean canRide(Entity entityIn) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SPEED, Integer.valueOf(0));
        this.entityData.define(DAMAGE, Float.valueOf(0.0F));
        this.entityData.define(HOLO, Optional.empty());
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
        if (!this.level.isClientSide && this.isAlive()) {
            if (this.isInvulnerableTo(source)) {
                return false;
            } else {
                this.markHurt();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean flag = source.getEntity() instanceof Player && ((Player) source.getEntity()).getAbilities().instabuild;

                if (flag || this.getDamage() > 40.0F) {
                    this.ejectPassengers();

                    if (flag && !this.hasCustomName()) {
                        this.remove(RemovalReason.KILLED);
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
        if (getHoloGui() != null) {
            holoGui.getEntity().stopRiding();
            holoGui.getEntity().remove(reason);
            holoGui = null;
        }
        super.remove(reason);
    }

    public void killLevitator(DamageSource source) {
        this.remove(RemovalReason.KILLED);

        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemstack = new ItemStack(Registration.FLUX_LEVITATOR.get(), 1);

            if (this.hasCustomName()) {
                itemstack.setHoverName(this.getCustomName());
            }

            this.spawnAtLocation(itemstack, 0.0F);
        }
    }

    @Override
    public void animateHurt() {
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean isPickable() {
        return this.isAlive();
    }

    @Override
    public Direction getMotionDirection() {
        return this.getDirection().getClockWise();
    }

    @Override
    public void tick() {
        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.getY() < -64.0D) {
            this.outOfWorld();
        }

        handlePortal();

        if (this.level.isClientSide) {
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

        double motionX = getDeltaMovement().x;
        double motionY = getDeltaMovement().y;
        double motionZ = getDeltaMovement().z;

        if (!this.isNoGravity()) {
            motionY -= 0.04D;
        }

        int floorX = Mth.floor(this.getX());
        int floorY = Mth.floor(this.getY());
        int floorZ = Mth.floor(this.getZ());

        Block block = level.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockPos blockpos = new BlockPos(floorX, floorY, floorZ);
        BlockState state = this.level.getBlockState(blockpos);

        if (isValidBeamBlock(state.getBlock())) {
            this.moveAlongTrack(blockpos, state);
        } else {
            motionY = 0;        // Stop
        }
        setDeltaMovement(motionX, motionY, motionZ);

        this.checkInsideBlocks();
        this.setXRot(0.0F);
        double dx = this.xo - this.getX();
        double dz = this.zo - this.getZ();

        if (dx * dx + dz * dz > 0.001D) {
            this.setYRot((float) (Mth.atan2(dz, dx) * 180.0D / Math.PI));
        } else {
            if (getSpeed() > 0) {
                changeSpeed(getSpeed() - 1);
            } else if (getSpeed() < 0) {
                changeSpeed(getSpeed() + 1);
            }
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

        Vec3 motion = getDeltaMovement();
        if (motion.x * motion.x + motion.z * motion.z > 0.01D) {
            List<Entity> list = this.level.getEntities(this, box, entity -> true);//@todo 1.14 EntitySelectors.getTeamCollisionPredicate(this));

            if (!list.isEmpty()) {
                for (Entity ent : list) {
                    if (!(ent instanceof Player) && !(ent instanceof IronGolem) && !(ent instanceof FluxElevatorEntity) && !this.isVehicle() && !ent.isPassenger()) {
                        ent.startRiding(this);
                    } else {
                        ent.push(this);
                    }
                }
            }
        } else {
            for (Entity entity : this.level.getEntities(this, box)) {
                if (!this.hasPassenger(entity) && entity.isPushable() && entity instanceof FluxElevatorEntity) {
                    entity.push(this);
                }
            }
        }
    }

    private void onUpdateClient() {
        this.setPos(this.getX(), this.getY(), this.getZ());
        this.setRot(this.getYRot(), this.getXRot());
    }

    private void handlePortal() {
        if (!this.level.isClientSide && this.level instanceof ServerLevel) {
            MinecraftServer minecraftserver = this.level.getServer();
            int i = this.getPortalWaitTime();

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
        if (getHoloGui() != null) {
            updateHoloGui(this.holoGui, 1);
        }
    }

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

    protected void moveAlongTrack(BlockPos pos, BlockState state) {
        this.fallDistance = 0.0F;
        Vec3 oldPos = this.getPos(this.getX(), this.getY(), this.getZ());
        setPosRaw(getX(), pos.getY(), getZ());   // @todo 1.15 is this right?
        int speed = getSpeed();
        boolean powered = speed != 0;    // Like powered
        boolean unpowered = speed == 0;

        RailShape dir = getBeamDirection(state);
//        handleBeamAscend(dir);        // Ascend not supported

        if (speed != 0) {
            handleLivingMotion(speed, dir);
        }

        if (unpowered) {
            restrictMotionUnpowered();
        }

        this.setPos(this.getX(), this.getY(), this.getZ());
        this.moveLevitatorOnBeam(pos);

        this.applyDrag();
        Vec3 newPos = this.getPos(this.getX(), this.getY(), this.getZ());

        if (newPos != null && oldPos != null) {
            double motionX = getDeltaMovement().x;
            double motionY = getDeltaMovement().y;
            double motionZ = getDeltaMovement().z;

            double d14 = (oldPos.y - newPos.y) * 0.05D;
            double motionLength = Math.sqrt(motionX * motionX + motionZ * motionZ);

            if (motionLength > 0.0D) {
                motionX = motionX / motionLength * (motionLength + d14);
                motionZ = motionZ / motionLength * (motionLength + d14);
            }

            setDeltaMovement(motionX, motionY, motionZ);
            this.setPos(this.getX(), newPos.y, this.getZ());
        }

        int floorX = Mth.floor(this.getX());
        int floorZ = Mth.floor(this.getZ());

        if (powered) {
            handlePoweredMotion(pos, dir);
        }
    }

    private void handleLivingMotion(int speed, RailShape dir) {

        float yaw;
        if (dir == RailShape.NORTH_SOUTH) {
            if (speed > 0) {
                yaw = -358;
            } else {
                yaw = -178;
            }
        } else {
            if (speed > 0) {
                yaw = -88;
            } else {
                yaw = -278;
            }
        }

        double dx = -Math.sin((yaw * 0.017453292F));
        double dz = Math.cos((yaw * 0.017453292F));
        double motionX = getDeltaMovement().x;
        double motionY = getDeltaMovement().y;
        double motionZ = getDeltaMovement().z;
        double dist = motionX * motionX + motionZ * motionZ;

        if (dist < 0.01D) {
//            motionX += dx * 0.1D;
//            motionZ += dz * 0.1D;
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
        } else {
            if (dx > 0) {
                motionX = Math.abs(motionX);
            } else {
                motionX = -Math.abs(motionX);
            }
            if (dz > 0) {
                motionZ = Math.abs(motionZ);
            } else {
                motionZ = -Math.abs(motionZ);
            }
        }

        double maxMotion = Math.abs(speed / 25.0f);
        if (Math.abs(motionX) > maxMotion) {
            motionZ /= Math.abs(motionX) / maxMotion;
            if (motionX < 0) {
                motionX = -maxMotion;
            } else {
                motionX = maxMotion;
            }
        }
        if (Math.abs(motionZ) > maxMotion) {
            motionX /= Math.abs(motionZ) / maxMotion;
            if (motionZ < 0) {
                motionZ = -maxMotion;
            } else {
                motionZ = maxMotion;
            }
        }
        setDeltaMovement(motionX, motionY, motionZ);
    }

    private void handlePoweredMotion(BlockPos pos, RailShape dir) {
        double motionX = getDeltaMovement().x;
        double motionY = getDeltaMovement().y;
        double motionZ = getDeltaMovement().z;

        double length = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (length > 0.01D) {
            motionX += motionX / length * 0.06D;
            motionZ += motionZ / length * 0.06D;
        } else if (dir == RailShape.EAST_WEST) {
            if (this.level.getBlockState(pos.west()).isRedstoneConductor(level, pos.west())) {
                motionX = 0.02D;
            } else if (this.level.getBlockState(pos.east()).isRedstoneConductor(level, pos.east())) {
                motionX = -0.02D;
            }
        } else if (dir == RailShape.NORTH_SOUTH) {
            if (this.level.getBlockState(pos.north()).isRedstoneConductor(level, pos.north())) {
                motionZ = 0.02D;
            } else if (this.level.getBlockState(pos.south()).isRedstoneConductor(level, pos.south())) {
                motionZ = -0.02D;
            }
        }
        setDeltaMovement(motionX, motionY, motionZ);
    }

    private void restrictMotionUnpowered() {
        double motionX = getDeltaMovement().x;
        double motionY = getDeltaMovement().y;
        double motionZ = getDeltaMovement().z;
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
        if (state.getBlock() == Registration.FLUX_BEAM.get()) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                return RailShape.EAST_WEST;
            } else {
                return RailShape.NORTH_SOUTH;
            }
        } else {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            switch (facing) {
                case NORTH:
                    return RailShape.NORTH_EAST;
                case SOUTH:
                    return RailShape.NORTH_WEST;
                case WEST:
                    return RailShape.SOUTH_WEST;
                case EAST:
                    return RailShape.SOUTH_EAST;
                default:
                    break;
            }
        }
        throw new IllegalStateException("This didn't happen");
    }

    protected void applyDrag() {
        double motionX = getDeltaMovement().x;
        double motionY = getDeltaMovement().y;
        double motionZ = getDeltaMovement().z;
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

            if (newpos.length() != 0.0D) {
                newpos = newpos.normalize();
                yaw = (float) (Math.atan2(newpos.z, newpos.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(newpos.y) * 73.0D);
            }
        }
        return Pair.of(yaw, pitch);
    }

    @Nullable
    public Vec3 getPosOffset(double x, double y, double z, double offset) {
        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);

        Block block = level.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockState state = this.level.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (isValidBeamBlock(state.getBlock())) {
            RailShape dir = getBeamDirection(state);
            y = floorY;

            if (dir.isAscending()) {
                y = (floorY + 1);
            }

//            if (aint[0][1] != 0 && MathHelper.floor(x) - floorX == aint[0][0] && MathHelper.floor(z) - floorZ == aint[0][2]) {
//                y += aint[0][1];
//            } else if (aint[1][1] != 0 && MathHelper.floor(x) - floorX == aint[1][0] && MathHelper.floor(z) - floorZ == aint[1][2]) {
//                y += aint[1][1];
//            }

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

        BlockState state = this.level.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (isValidBeamBlock(state.getBlock())) {
            RailShape dir = getBeamDirection(state);
            return new Vec3(x, y, z);
        } else {
            return null;
        }
    }

    public BlockPos getDesiredDestination() {
        return desiredDestination;
    }

    public void setDesiredDestination(BlockPos desiredDestination) {
        this.desiredDestination = desiredDestination;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID("holo")) {
            setHoloUUID(compound.getUUID("holo"));
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
        if (getHoloUUID() != null) {
            compound.putUUID("holo", getHoloUUID());
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
        if (!this.level.isClientSide) {
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

                        if (entityIn instanceof FluxElevatorEntity) {
                            double ddx = entityIn.getX() - this.getX();
                            double ddz = entityIn.getZ() - this.getZ();
                            Vec3 vec3d = (new Vec3(ddx, 0.0D, ddz)).normalize();
                            Vec3 vec3d1 = (new Vec3(Mth.cos(this.getYRot() * 0.017453292F), 0.0D, Mth.sin(this.getYRot() * 0.017453292F))).normalize();
                            double d6 = Math.abs(vec3d.dot(vec3d1));

                            if (d6 < 0.8D) {
                                return;
                            }

                            double motionX = entityIn.getDeltaMovement().x;
                            double motionY = entityIn.getDeltaMovement().y;
                            double motionZ = entityIn.getDeltaMovement().z;
                            double d7 = motionX + motionX;
                            double d8 = motionZ + motionZ;

                            d7 = d7 / 2.0D;
                            d8 = d8 / 2.0D;
                            motionX *= 0.2D;
                            motionZ *= 0.2D;
                            entityIn.setDeltaMovement(motionX, motionY, motionZ);
                            this.push(d7 - dx, 0.0D, d8 - dz);
                            motionX *= 0.2D;
                            motionZ *= 0.2D;
                            entityIn.setDeltaMovement(motionX, motionY, motionZ);
                            entityIn.push(d7 + dx, 0.0D, d8 + dz);
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
        setDeltaMovement(velocityX, velocityY, velocityZ);

        updateHoloGui();
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, Float.valueOf(damage));
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

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
//        } else if (this.isBeingRidden()) {    // @todo
//            return true;
        } else {
            if (!this.level.isClientSide) {
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
        BlockState state = this.level.getBlockState(pos);
        if (!isValidBeamBlock(state.getBlock())) {
            return getMaximumSpeed();
        }

        float railMaxSpeed = 5; // @todo ((BlockRailBase) state.getBlock()).getRailMaxSpeed(world, this, pos);
        return Math.min(railMaxSpeed, MAX_SPEED);
    }

    /**
     * Moved to allow overrides.
     * This code handles levitator movement and speed capping when on a rail.
     */
    public void moveLevitatorOnBeam(BlockPos pos) {
        double mX = getDeltaMovement().x;
        double mZ = getDeltaMovement().z;

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