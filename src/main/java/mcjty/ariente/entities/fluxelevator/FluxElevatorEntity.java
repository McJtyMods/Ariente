package mcjty.ariente.entities.fluxelevator;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.CloseStrategy;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FluxElevatorEntity extends Entity {

    private static final DataParameter<Integer> SPEED = EntityDataManager.createKey(FluxElevatorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(FluxElevatorEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Optional<UUID>> HOLO = EntityDataManager.createKey(FluxElevatorEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final float LENGTH = 2.5f;
    public static final float MAX_SPEED = 1.2f;

    private double velocityX;
    private double velocityY;
    private double velocityZ;

    private IHoloGuiEntity holoGui;

    private BlockPos desiredDestination = null;

    public FluxElevatorEntity(EntityType<? extends FluxElevatorEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.preventEntitySpawning = true;
        this.setSize(1.30F, 0.9F);
    }


    public int getSpeed() {
        return dataManager.get(SPEED);
    }

    public void changeSpeed(int speed) {
        if (speed < -80) {
            speed = -80;
        } else if (speed > 80) {
            speed = 80;
        }
        this.dataManager.set(SPEED, speed);
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
    protected boolean canFitPassenger(Entity passenger) {
        return true;    // @todo
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return null;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            if (!(passenger instanceof IHoloGuiEntity)) {
                super.updatePassenger(passenger);
            }
        }
    }

    public IHoloGuiEntity getHoloGui() {
        if (holoGui == null) {
            if (getHoloUUID() != null) {
                for (Entity entity : world.getEntitiesWithinAABB(Ariente.guiHandler.getHoloEntityClass(), getBoundingBox().grow(10))) {
                    if (entity instanceof IHoloGuiEntity && getHoloUUID().equals(entity.getUniqueID())) {
                        holoGui = (IHoloGuiEntity) entity;
                        entity.startRiding(this);
                        break;
                    }
                }
            }
            if (holoGui == null && !world.isRemote ) {
                IHoloGuiEntity holoGui = Ariente.guiHandler.openHoloGuiRelative(this, new Vec3d(0, .5, 1), ModGuis.GUI_ELEVATOR);
                holoGui.setScale(0.75f);
                holoGui.setCloseStrategy(CloseStrategy.NEVER);
                holoGui.getEntity().startRiding(this);
                this.holoGui = holoGui;
                setHoloUUID(holoGui.getEntity().getUniqueID());
            }
        }
        return holoGui;
    }

    public static FluxElevatorEntity create(World worldIn, double x, double y, double z) {
        FluxElevatorEntity entity = new FluxElevatorEntity(Registration.ENTITY_ELEVATOR.get(), worldIn);
        entity.setPosition(x, y, z);
        entity.setMotion(0, 0, 0);
        entity.prevPosX = x;
        entity.prevPosY = y;
        entity.prevPosZ = z;
        return entity;
    }

    @Override
    public void move(MoverType typeIn, Vec3d pos) {
        super.move(typeIn, pos);
        updateHoloGui();        // @todo check if needed
    }

    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        super.setLocationAndAngles(x, y, z, yaw, pitch);
        updateHoloGui();        // @todo check if needed
    }

    @Override
    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        super.setPositionAndRotation(x, y, z, yaw, pitch);
        updateHoloGui();        // @todo check if needed
    }

    public void setHoloUUID(UUID holoFront) {
        this.dataManager.set(HOLO, Optional.ofNullable(holoFront));
    }

    public UUID getHoloUUID() {
        return (UUID) ((Optional) this.dataManager.get(HOLO)).orElse(null);
    }


    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return true;
    }

    @Override
    protected void registerData() {
        this.dataManager.register(SPEED, Integer.valueOf(0));
        this.dataManager.register(DAMAGE, Float.valueOf(0.0F));
        this.dataManager.register(HOLO, Optional.empty());
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() ? entityIn.getBoundingBox() : null;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }


    @Override
    public double getMountedYOffset() {
        return 0.0D;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!this.world.isRemote && this.isAlive()) {
            if (this.isInvulnerableTo(source)) {
                return false;
            } else {
                this.markVelocityChanged();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean flag = source.getTrueSource() instanceof PlayerEntity && ((PlayerEntity) source.getTrueSource()).abilities.isCreativeMode;

                if (flag || this.getDamage() > 40.0F) {
                    this.removePassengers();

                    if (flag && !this.hasCustomName()) {
                        this.remove();
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
    public void remove() {
        if (getHoloGui() != null) {
            holoGui.getEntity().stopRiding();
            holoGui.getEntity().remove();
            holoGui = null;
        }
        super.remove();
    }

    public void killLevitator(DamageSource source) {
        this.remove();

        if (this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack itemstack = new ItemStack(Registration.FLUX_LEVITATOR.get(), 1);

            if (this.hasCustomName()) {
                itemstack.setDisplayName(this.getCustomName());
            }

            this.entityDropItem(itemstack, 0.0F);
        }
    }

    @Override
    public void performHurtAnimation() {
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }

    @Override
    public Direction getAdjustedHorizontalFacing() {
        return this.getHorizontalFacing().rotateY();
    }

    @Override
    public void tick() {
        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.getPosY() < -64.0D) {
            this.outOfWorld();
        }

        handlePortal();

        if (this.world.isRemote) {
            onUpdateClient();
        } else {
            onUpdateServer();
        }

        updateHoloGui();
    }

    private void onUpdateServer() {
        this.prevPosX = this.getPosX();
        this.prevPosY = this.getPosY();
        this.prevPosZ = this.getPosZ();

        double motionX = getMotion().x;
        double motionY = getMotion().y;
        double motionZ = getMotion().z;

        if (!this.hasNoGravity()) {
            motionY -= 0.04D;
        }

        int floorX = MathHelper.floor(this.getPosX());
        int floorY = MathHelper.floor(this.getPosY());
        int floorZ = MathHelper.floor(this.getPosZ());

        Block block = world.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockPos blockpos = new BlockPos(floorX, floorY, floorZ);
        BlockState state = this.world.getBlockState(blockpos);

        if (isValidBeamBlock(state.getBlock())) {
            this.moveAlongTrack(blockpos, state);
        } else {
            motionY = 0;        // Stop
        }
        setMotion(motionX, motionY, motionZ);

        this.doBlockCollisions();
        this.rotationPitch = 0.0F;
        double dx = this.prevPosX - this.getPosX();
        double dz = this.prevPosZ - this.getPosZ();

        if (dx * dx + dz * dz > 0.001D) {
            this.rotationYaw = (float) (MathHelper.atan2(dz, dx) * 180.0D / Math.PI);
        } else {
            if (getSpeed() > 0) {
                changeSpeed(getSpeed() - 1);
            } else if (getSpeed() < 0) {
                changeSpeed(getSpeed() + 1);
            }
        }

        this.setRotation(this.rotationYaw, this.rotationPitch);

        handleEntityCollision();
        handleWaterMovement();
    }

    private boolean isValidBeamBlock(Block block) {
        return block == Registration.FLUX_BEAM.get() || block == Registration.FLUX_BEND_BEAM.get();
    }

    private void handleEntityCollision() {
        AxisAlignedBB box;
        box = this.getBoundingBox().grow(0.2D, 0.0D, 0.2D);

        Vec3d motion = getMotion();
        if (motion.x * motion.x + motion.z * motion.z > 0.01D) {
            List<Entity> list = this.world.getEntitiesInAABBexcluding(this, box, entity -> true);//@todo 1.14 EntitySelectors.getTeamCollisionPredicate(this));

            if (!list.isEmpty()) {
                for (Entity ent : list) {
                    if (!(ent instanceof PlayerEntity) && !(ent instanceof IronGolemEntity) && !(ent instanceof FluxElevatorEntity) && !this.isBeingRidden() && !ent.isPassenger()) {
                        ent.startRiding(this);
                    } else {
                        ent.applyEntityCollision(this);
                    }
                }
            }
        } else {
            for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, box)) {
                if (!this.isPassenger(entity) && entity.canBePushed() && entity instanceof FluxElevatorEntity) {
                    entity.applyEntityCollision(this);
                }
            }
        }
    }

    private void onUpdateClient() {
        this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
        this.setRotation(this.rotationYaw, this.rotationPitch);
    }

    private void handlePortal() {
        if (!this.world.isRemote && this.world instanceof ServerWorld) {
            MinecraftServer minecraftserver = this.world.getServer();
            int i = this.getMaxInPortalTime();

            if (this.inPortal) {
                if (minecraftserver.getAllowNether()) {
                    if (!this.isPassenger() && this.portalCounter++ >= i) {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        DimensionType id;

                        if (this.world.getDimension().getType() == DimensionType.THE_NETHER) {
                            id = DimensionType.OVERWORLD;
                        } else {
                            id = DimensionType.THE_NETHER;
                        }

                        this.changeDimension(id);
                    }

                    this.inPortal = false;
                }
            } else {
                if (this.portalCounter > 0) {
                    this.portalCounter -= 4;
                }

                if (this.portalCounter < 0) {
                    this.portalCounter = 0;
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

        Vec3d vec3d = getPosOffset(getPosX(), getPosY(), getPosZ(), offset);
        if (vec3d != null) {
            double x = vec3d.x;
            double y = vec3d.y + .38;
            double z = vec3d.z;

            holo.getEntity().setLocationAndAngles(x, y, z, yaw, pitch);
            holo.getEntity().setPositionAndUpdate(x, y, z);
        }
    }

    private double getMaximumSpeed() {
        return Math.abs(getSpeed()) / 25.0;
    }

    protected void moveAlongTrack(BlockPos pos, BlockState state) {
        this.fallDistance = 0.0F;
        Vec3d oldPos = this.getPos(this.getPosX(), this.getPosY(), this.getPosZ());
        setRawPosition(getPosX(), pos.getY(), getPosZ());   // @todo 1.15 is this right?
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

        this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
        this.moveLevitatorOnBeam(pos);

        this.applyDrag();
        Vec3d newPos = this.getPos(this.getPosX(), this.getPosY(), this.getPosZ());

        if (newPos != null && oldPos != null) {
            double motionX = getMotion().x;
            double motionY = getMotion().y;
            double motionZ = getMotion().z;

            double d14 = (oldPos.y - newPos.y) * 0.05D;
            double motionLength = Math.sqrt(motionX * motionX + motionZ * motionZ);

            if (motionLength > 0.0D) {
                motionX = motionX / motionLength * (motionLength + d14);
                motionZ = motionZ / motionLength * (motionLength + d14);
            }

            setMotion(motionX, motionY, motionZ);
            this.setPosition(this.getPosX(), newPos.y, this.getPosZ());
        }

        int floorX = MathHelper.floor(this.getPosX());
        int floorZ = MathHelper.floor(this.getPosZ());

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
        double motionX = getMotion().x;
        double motionY = getMotion().y;
        double motionZ = getMotion().z;
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
        setMotion(motionX, motionY, motionZ);
    }

    private void handlePoweredMotion(BlockPos pos, RailShape dir) {
        double motionX = getMotion().x;
        double motionY = getMotion().y;
        double motionZ = getMotion().z;

        double length = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (length > 0.01D) {
            motionX += motionX / length * 0.06D;
            motionZ += motionZ / length * 0.06D;
        } else if (dir == RailShape.EAST_WEST) {
            if (this.world.getBlockState(pos.west()).isNormalCube(world, pos.west())) {
                motionX = 0.02D;
            } else if (this.world.getBlockState(pos.east()).isNormalCube(world, pos.east())) {
                motionX = -0.02D;
            }
        } else if (dir == RailShape.NORTH_SOUTH) {
            if (this.world.getBlockState(pos.north()).isNormalCube(world, pos.north())) {
                motionZ = 0.02D;
            } else if (this.world.getBlockState(pos.south()).isNormalCube(world, pos.south())) {
                motionZ = -0.02D;
            }
        }
        setMotion(motionX, motionY, motionZ);
    }

    private void restrictMotionUnpowered() {
        double motionX = getMotion().x;
        double motionY = getMotion().y;
        double motionZ = getMotion().z;
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
        setMotion(motionX, motionY, motionZ);
    }

    public static RailShape getBeamDirection(BlockState state) {
        if (state.getBlock() == Registration.FLUX_BEAM.get()) {
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                return RailShape.EAST_WEST;
            } else {
                return RailShape.NORTH_SOUTH;
            }
        } else {
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
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
        double motionX = getMotion().x;
        double motionY = getMotion().y;
        double motionZ = getMotion().z;
        if (this.isBeingRidden()) {
            motionX *= 0.997D;
            motionY *= 0.0D;
            motionZ *= 0.997D;
        } else {
            motionX *= 0.96D;
            motionY *= 0.0D;
            motionZ *= 0.96D;
        }
        setMotion(motionX, motionY, motionZ);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        setRawPosition(x, y, z);    // @todo 1.15 is this right?
        float f = this.getWidth() / 2.0F;
        float f1 = this.getHeight();
        this.setBoundingBox(new AxisAlignedBB(x - f, y, z - f, x + f, y + f1, z + f));
    }

    // Calculate yaw and pitch based on block below the levitator
    private Pair<Float, Float> calculateYawPitch() {
        Vec3d oldPos = getPos(getPosX(), getPosY(), getPosZ());
        float yaw = rotationYaw;
        float pitch = rotationPitch;

        if (oldPos != null) {
            Vec3d posUp = getPosOffset(getPosX(), getPosY(), getPosZ(), 0.3D);
            Vec3d posDown = getPosOffset(getPosX(), getPosY(), getPosZ(), -0.3D);

            if (posUp == null) {
                posUp = oldPos;
            }

            if (posDown == null) {
                posDown = oldPos;
            }

            Vec3d newpos = posDown.add(-posUp.x, -posUp.y, -posUp.z);

            if (newpos.length() != 0.0D) {
                newpos = newpos.normalize();
                yaw = (float) (Math.atan2(newpos.z, newpos.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(newpos.y) * 73.0D);
            }
        }
        return Pair.of(yaw, pitch);
    }

    @Nullable
    public Vec3d getPosOffset(double x, double y, double z, double offset) {
        int floorX = MathHelper.floor(x);
        int floorY = MathHelper.floor(y);
        int floorZ = MathHelper.floor(z);

        Block block = world.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockState state = this.world.getBlockState(new BlockPos(floorX, floorY, floorZ));

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
    public Vec3d getPos(double x, double y, double z) {
        int floorX = MathHelper.floor(x);
        int floorY = MathHelper.floor(y);
        int floorZ = MathHelper.floor(z);

        Block block = world.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockState state = this.world.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (isValidBeamBlock(state.getBlock())) {
            RailShape dir = getBeamDirection(state);
            return new Vec3d(x, y, z);
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
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getBoundingBox();
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        if (compound.hasUniqueId("holo")) {
            setHoloUUID(compound.getUniqueId("holo"));
        }
        changeSpeed(compound.getInt("speed"));
        if (compound.contains("desiredDestX")) {
            desiredDestination = new BlockPos(compound.getInt("desiredDestX"),
                    compound.getInt("desiredDestY"),
                    compound.getInt("desiredDestZ"));
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (getHoloUUID() != null) {
            compound.putUniqueId("holo", getHoloUUID());
        }
        compound.putInt("speed", getSpeed());
        if (desiredDestination != null) {
            compound.putInt("desiredDestX", desiredDestination.getX());
            compound.putInt("desiredDestY", desiredDestination.getY());
            compound.putInt("desiredDestZ", desiredDestination.getZ());
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!this.world.isRemote) {
            if (!entityIn.noClip && !this.noClip) {
                if (!this.isPassenger(entityIn)) {
                    double dx = entityIn.getPosX() - this.getPosX();
                    double dz = entityIn.getPosZ() - this.getPosZ();
                    double length = dx * dx + dz * dz;

                    if (length >= .0001D) {
                        length = MathHelper.sqrt(length);
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
                        dx = dx * (1.0F - this.entityCollisionReduction);
                        dz = dz * (1.0F - this.entityCollisionReduction);
                        dx = dx * 0.5D;
                        dz = dz * 0.5D;

                        if (entityIn instanceof FluxElevatorEntity) {
                            double ddx = entityIn.getPosX() - this.getPosX();
                            double ddz = entityIn.getPosZ() - this.getPosZ();
                            Vec3d vec3d = (new Vec3d(ddx, 0.0D, ddz)).normalize();
                            Vec3d vec3d1 = (new Vec3d(MathHelper.cos(this.rotationYaw * 0.017453292F), 0.0D, MathHelper.sin(this.rotationYaw * 0.017453292F))).normalize();
                            double d6 = Math.abs(vec3d.dotProduct(vec3d1));

                            if (d6 < 0.8D) {
                                return;
                            }

                            double motionX = entityIn.getMotion().x;
                            double motionY = entityIn.getMotion().y;
                            double motionZ = entityIn.getMotion().z;
                            double d7 = motionX + motionX;
                            double d8 = motionZ + motionZ;

                            d7 = d7 / 2.0D;
                            d8 = d8 / 2.0D;
                            motionX *= 0.2D;
                            motionZ *= 0.2D;
                            entityIn.setMotion(motionX, motionY, motionZ);
                            this.addVelocity(d7 - dx, 0.0D, d8 - dz);
                            motionX *= 0.2D;
                            motionZ *= 0.2D;
                            entityIn.setMotion(motionX, motionY, motionZ);
                            entityIn.addVelocity(d7 + dx, 0.0D, d8 + dz);
                        } else {
                            this.addVelocity(-dx, 0.0D, -dz);
                            entityIn.addVelocity(dx / 4.0D, 0.0D, dz / 4.0D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        setMotion(velocityX, velocityY, velocityZ);

        updateHoloGui();
    }

    public void setDamage(float damage) {
        this.dataManager.set(DAMAGE, Float.valueOf(damage));
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        setMotion(x, y, z);
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
    }

    public float getDamage() {
        return this.dataManager.get(DAMAGE).floatValue();
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        if (player.isShiftKeyDown()) {
            return false;
//        } else if (this.isBeingRidden()) {    // @todo
//            return true;
        } else {
            if (!this.world.isRemote) {
                player.startRiding(this);
            }

            return true;
        }
    }


    private BlockPos getCurrentRailPosition() {
        int x = MathHelper.floor(this.getPosX());
        int y = MathHelper.floor(this.getPosY());
        int z = MathHelper.floor(this.getPosZ());

        Block block = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
        if (isValidBeamBlock(block)) {
            y--;
        }
        return new BlockPos(x, y, z);
    }

    protected double getMaxSpeed() {
        BlockPos pos = this.getCurrentRailPosition();
        BlockState state = this.world.getBlockState(pos);
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
        double mX = getMotion().x;
        double mZ = getMotion().z;

        if (this.isBeingRidden()) {
            mX *= 0.75D;
            mZ *= 0.75D;
        }

        double max = this.getMaxSpeed();
        mX = MathHelper.clamp(mX, -max, max);
        mZ = MathHelper.clamp(mZ, -max, max);
        this.move(MoverType.SELF, new Vec3d(mX, 0.0D, mZ));
    }
}