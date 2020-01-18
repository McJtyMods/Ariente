package mcjty.ariente.entities.levitator;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IFluxLevitatorEntity;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.items.ModItems;
import mcjty.hologui.api.CloseStrategy;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FluxLevitatorEntity extends Entity implements IFluxLevitatorEntity {

    private static final DataParameter<Integer> ROLLING_AMPLITUDE = EntityDataManager.createKey(FluxLevitatorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ROLLING_DIRECTION = EntityDataManager.createKey(FluxLevitatorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPEED = EntityDataManager.createKey(FluxLevitatorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(FluxLevitatorEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Optional<UUID>> HOLO_FRONT = EntityDataManager.createKey(FluxLevitatorEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> HOLO_BACK = EntityDataManager.createKey(FluxLevitatorEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);

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

    public FluxLevitatorEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.preventEntitySpawning = true;
        this.setSize(1.30F, 0.9F);
    }


    public int getSpeed() {
        return dataManager.get(SPEED);
    }

    @Override
    public void changeSpeed(int speed) {
        if (speed < -80) {
            speed = -80;
        } else if (speed > 80) {
            speed = 80;
        }
        this.dataManager.set(SPEED, speed);
    }

    @Override
    protected void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            float f = this.width;
            this.width = width;
            this.height = height;

            if (this.width < f) {
                double dx = LENGTH / 2.0D;
                double d0 = width / 2.0D;
                this.setEntityBoundingBox(new AxisAlignedBB(this.posX - dx, this.posY, this.posZ - d0, this.posX + dx, this.posY + this.height, this.posZ + d0));
                return;
            }

            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + this.LENGTH, axisalignedbb.minY + this.height, axisalignedbb.minZ + this.width));

            if (this.width > f && !this.firstUpdate && !world.isRemote) {
                this.move(MoverType.SELF, (f - this.LENGTH), 0.0D, (f - this.width));
            }
        }
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
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            if (!(passenger instanceof IHoloGuiEntity)) {
                super.updatePassenger(passenger);
            }
        }
    }

    public IHoloGuiEntity getHoloGuiFront() {
        if (holoGuiFront == null) {
            if (getHoloFrontUUID() != null) {
                for (Entity entity : world.getEntitiesWithinAABB(Ariente.guiHandler.getHoloEntityClass(), getEntityBoundingBox().grow(10))) {
                    if (entity instanceof IHoloGuiEntity && getHoloFrontUUID().equals(entity.getUniqueID())) {
                        holoGuiFront = (IHoloGuiEntity) entity;
                        entity.startRiding(this);
                        break;
                    }
                }
            }
            if (holoGuiFront == null && !world.isRemote ) {
                IHoloGuiEntity holoGui = Ariente.guiHandler.openHoloGuiRelative(this, new Vec3d(0, .5, 1), ModGuis.GUI_LEVITATOR);
                holoGui.setScale(0.75f);
                holoGui.setCloseStrategy(CloseStrategy.NEVER);
                holoGui.getEntity().startRiding(this);
                this.holoGuiFront = holoGui;
                setHoloFrontUUID(holoGui.getEntity().getUniqueID());
            }
        }
        return holoGuiFront;
    }

    public IHoloGuiEntity getHoloGuiBack() {
        if (holoGuiBack == null) {
            if (getHoloBackUUID() != null) {
                for (Entity entity : world.getEntitiesWithinAABB(Ariente.guiHandler.getHoloEntityClass(), getEntityBoundingBox().grow(10))) {
                    if (getHoloBackUUID().equals(entity.getUniqueID())) {
                        holoGuiBack = (IHoloGuiEntity) entity;
                        entity.startRiding(this);
                        break;
                    }
                }
            }
            if (holoGuiBack == null && !world.isRemote ) {
                IHoloGuiEntity holoGui = Ariente.guiHandler.openHoloGuiRelative(this, new Vec3d(0, .5, 1), ModGuis.GUI_LEVITATOR);
                holoGui.setScale(0.75f);
                holoGui.setCloseStrategy(CloseStrategy.NEVER);
                holoGui.getEntity().startRiding(this);
                this.holoGuiBack = holoGui;
                setHoloBackUUID(holoGui.getEntity().getUniqueID());
            }
        }
        return holoGuiBack;
    }

    public FluxLevitatorEntity(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        super.move(type, x, y, z);
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

    public void setHoloFrontUUID(UUID holoFront) {
        this.dataManager.set(HOLO_FRONT, Optional.fromNullable(holoFront));
    }

    public UUID getHoloFrontUUID() {
        return (UUID) ((Optional) this.dataManager.get(HOLO_FRONT)).orNull();
    }


    public void setHoloBackUUID(UUID holoBack) {
        this.dataManager.set(HOLO_BACK, Optional.fromNullable(holoBack));
    }

    public UUID getHoloBackUUID() {
        return (UUID) ((Optional) this.dataManager.get(HOLO_BACK)).orNull();
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
    protected void entityInit() {
        this.dataManager.register(ROLLING_AMPLITUDE, 0);
        this.dataManager.register(ROLLING_DIRECTION, 0);
        this.dataManager.register(SPEED, 0);
        this.dataManager.register(DAMAGE, 0.0F);
        this.dataManager.register(HOLO_FRONT, Optional.absent());
        this.dataManager.register(HOLO_BACK, Optional.absent());
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
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
        if (!world.isRemote && !this.isDead) {
            if (this.isEntityInvulnerable(source)) {
                return false;
            } else {
                this.setRollingDirection(-this.getRollingDirection());
                this.setRollingAmplitude(10);
                this.markVelocityChanged();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean flag = source.getTrueSource() instanceof PlayerEntity && ((PlayerEntity) source.getTrueSource()).capabilities.isCreativeMode;

                if (flag || this.getDamage() > 40.0F) {
                    this.removePassengers();

                    if (flag && !this.hasCustomName()) {
                        this.setDead();
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
    public void setDead() {
        if (getHoloGuiFront() != null) {
            holoGuiFront.getEntity().dismountRidingEntity();
            holoGuiFront.getEntity().setDead();
            holoGuiFront = null;
        }
        if (getHoloGuiBack() != null) {
            holoGuiBack.getEntity().dismountRidingEntity();
            holoGuiBack.getEntity().setDead();
            holoGuiBack = null;
        }
        super.setDead();
    }

    public void killLevitator(DamageSource source) {
        this.setDead();

        if (world.getGameRules().getBoolean("doEntityDrops")) {
            ItemStack itemstack = new ItemStack(ModItems.fluxLevitatorItem, 1);

            if (this.hasCustomName()) {
                itemstack.setStackDisplayName(this.getCustomNameTag());
            }

            this.entityDropItem(itemstack, 0.0F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {
        this.setRollingDirection(-this.getRollingDirection());
        this.setRollingAmplitude(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public Direction getAdjustedHorizontalFacing() {
        return isInReverse
                ? this.getHorizontalFacing().getOpposite().rotateY()
                : this.getHorizontalFacing().rotateY();
//        return this.getHorizontalFacing().rotateY();
    }

    @Override
    public void onUpdate() {
        if (this.getRollingAmplitude() > 0) {
            this.setRollingAmplitude(this.getRollingAmplitude() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.posY < -64.0D) {
            this.outOfWorld();
        }

        handlePortal();

        if (world.isRemote) {
            onUpdateClient();
        } else {
            onUpdateServer();
        }

        updateHoloGui();
    }

    private void onUpdateServer() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (!this.hasNoGravity()) {
            this.motionY -= 0.04D;
        }

        int floorX = MathHelper.floor(this.posX);
        int floorY = MathHelper.floor(this.posY);
        int floorZ = MathHelper.floor(this.posZ);

        Block block = world.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockPos blockpos = new BlockPos(floorX, floorY, floorZ);
        BlockState state = world.getBlockState(blockpos);

        if (isValidBeamBlock(state.getBlock())) {
            this.moveAlongTrack(blockpos, state);
        } else {
            this.moveDerailedLevitator();
        }

        this.doBlockCollisions();
        this.rotationPitch = 0.0F;
        double dx = this.prevPosX - this.posX;
        double dz = this.prevPosZ - this.posZ;

        if (dx * dx + dz * dz > 0.001D) {
            this.rotationYaw = (float) (MathHelper.atan2(dz, dx) * 180.0D / Math.PI);
            if (isInReverse) {
                rotationYaw += 180;
            }
        } else {
            // If we couldn't move for some reason we slowly decrease speed
            if (getSpeed() > 0) {
                changeSpeed(getSpeed() - 1);
            } else if (getSpeed() < 0) {
                changeSpeed(getSpeed() + 1);
            }
        }

        double angle = MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw);
        if (angle < -170.0D || angle >= 170.0D) {
            this.rotationYaw += 180.0F;
//            this.isInReverse = !this.isInReverse;
            isInReverse = false;
        }

        this.setRotation(this.rotationYaw, this.rotationPitch);

        handleEntityCollision();
        handleWaterMovement();
    }

    private boolean isValidBeamBlock(Block block) {
        return block == ModBlocks.fluxBeamBlock || block == ModBlocks.fluxBendBeamBlock;
    }

    private void handleEntityCollision() {
        AxisAlignedBB box;
        box = this.getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D);

        if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D) {
            List<Entity> list = world.getEntitiesInAABBexcluding(this, box, EntitySelectors.getTeamCollisionPredicate(this));

            if (!list.isEmpty()) {
                for (Entity ent : list) {
                    if (!(ent instanceof PlayerEntity) && !(ent instanceof EntityIronGolem) && !(ent instanceof FluxLevitatorEntity) && !this.isBeingRidden() && !ent.isRiding()) {
                        ent.startRiding(this);
                    } else {
                        ent.applyEntityCollision(this);
                    }
                }
            }
        } else {
            for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, box)) {
                if (!this.isPassenger(entity) && entity.canBePushed() && entity instanceof FluxLevitatorEntity) {
                    entity.applyEntityCollision(this);
                }
            }
        }
    }

    private void onUpdateClient() {
        if (turnProgress > 0) {
            double turnX = posX + (levitatorX - posX) / turnProgress;
            double turnY = posY + (levitatorY - posY) / turnProgress;
            double turnZ = posZ + (levitatorZ - posZ) / turnProgress;
            double yaw = MathHelper.wrapDegrees(levitatorYaw - rotationYaw);
            rotationYaw = (float) (rotationYaw + yaw / turnProgress);
            rotationPitch = (float) (rotationPitch + (levitatorPitch - rotationPitch) / turnProgress);
            --turnProgress;
            setPosition(turnX, turnY, turnZ);
            setRotation(rotationYaw, rotationPitch);
        } else {
            setPosition(posX, posY, posZ);
            setRotation(rotationYaw, rotationPitch);
        }
    }

    private void handlePortal() {
        if (!world.isRemote && world instanceof WorldServer) {
            MinecraftServer minecraftserver = world.getMinecraftServer();
            int i = getMaxInPortalTime();

            if (this.inPortal) {
                if (minecraftserver.getAllowNether()) {
                    if (!this.isRiding() && this.portalCounter++ >= i) {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        int id;

                        if (world.provider.getDimensionType().getId() == -1) {
                            id = 0;
                        } else {
                            id = -1;
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

        Vec3d vec3d = getPosOffset(posX, posY, posZ, offset);
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

    private void moveDerailedLevitator() {
        double speed = onGround ? this.getMaximumSpeed() : DEFAULT_MAX_SPEED_AIR_LATERAL;
        this.motionX = MathHelper.clamp(this.motionX, -speed, speed);
        this.motionZ = MathHelper.clamp(this.motionZ, -speed, speed);

        double moveY = motionY;

        if (this.onGround) {
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        }

        this.move(MoverType.SELF, this.motionX, moveY, this.motionZ);

        if (!this.onGround) {
            this.motionX *= DEFAULT_DRAG_AIR;
            this.motionY *= DEFAULT_DRAG_AIR;
            this.motionZ *= DEFAULT_DRAG_AIR;
        }
    }

    private void moveAlongTrack(BlockPos pos, BlockState state) {
        this.fallDistance = 0.0F;
        Vec3d oldPos = this.getPos(this.posX, this.posY, this.posZ);
        this.posY = pos.getY();
        int speed = getSpeed();
        boolean powered = speed != 0;    // Like powered
        boolean unpowered = speed == 0;

        EnumRailDirection dir = getBeamDirection(state);
//        handleBeamAscend(dir);        // Ascend not supported

        int[][] aint = MATRIX[dir.getMetadata()];
        double ddx = (aint[1][0] - aint[0][0]);
        double ddz = (aint[1][2] - aint[0][2]);
        double ddist = Math.sqrt(ddx * ddx + ddz * ddz);
        double direction = this.motionX * ddx + this.motionZ * ddz;
        if (direction < 0.0D) {
            ddx = -ddx;
            ddz = -ddz;
        }

        double motionLength = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (motionLength > 2.0D) {
            motionLength = 2.0D;
        }

        this.motionX = motionLength * ddx / ddist;
        this.motionZ = motionLength * ddz / ddist;

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
            this.posX = pos.getX() + 0.5D;
            d10 = this.posZ - pos.getZ();
        } else if (ddz == 0.0D) {
            this.posZ = pos.getZ() + 0.5D;
            d10 = this.posX - pos.getX();
        } else {
            double d11 = this.posX - dx1;
            double d12 = this.posZ - dz1;
            d10 = (d11 * ddx + d12 * ddz) * 2.0D;
        }

        this.posX = dx1 + ddx * d10;
        this.posZ = dz1 + ddz * d10;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.moveLevitatorOnBeam(pos);

        if (aint[0][1] != 0 && MathHelper.floor(this.posX) - pos.getX() == aint[0][0] && MathHelper.floor(this.posZ) - pos.getZ() == aint[0][2]) {
            this.setPosition(this.posX, this.posY + aint[0][1], this.posZ);
        } else if (aint[1][1] != 0 && MathHelper.floor(this.posX) - pos.getX() == aint[1][0] && MathHelper.floor(this.posZ) - pos.getZ() == aint[1][2]) {
            this.setPosition(this.posX, this.posY + aint[1][1], this.posZ);
        }

        this.applyDrag();
        Vec3d newPos = this.getPos(this.posX, this.posY, this.posZ);

        if (newPos != null && oldPos != null) {
            double d14 = (oldPos.y - newPos.y) * 0.05D;
            motionLength = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (motionLength > 0.0D) {
                this.motionX = this.motionX / motionLength * (motionLength + d14);
                this.motionZ = this.motionZ / motionLength * (motionLength + d14);
            }

            this.setPosition(this.posX, newPos.y, this.posZ);
        }

        int floorX = MathHelper.floor(this.posX);
        int floorZ = MathHelper.floor(this.posZ);

        if (floorX != pos.getX() || floorZ != pos.getZ()) {
            motionLength = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.motionX = motionLength * (floorX - pos.getX());
            this.motionZ = motionLength * (floorZ - pos.getZ());
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
//                ++this.posY;
//                break;
//            case ASCENDING_WEST:
//                this.motionX += slopeAdjustment;
//                ++this.posY;
//                break;
//            case ASCENDING_NORTH:
//                this.motionZ += slopeAdjustment;
//                ++this.posY;
//                break;
//            case ASCENDING_SOUTH:
//                this.motionZ -= slopeAdjustment;
//                ++this.posY;
//        }
//    }

    private void handleLivingMotion(int speed, RailShape dir) {

        double maxMotion;

        double motionX = this.getMotion().x;
        double motionY = this.getMotion().y;
        double motionZ = this.getMotion().z;

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
            setMotion(motionX, motionY, motionZ);

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
        setMotion(motionX, motionY, motionZ);
    }

    private void handlePoweredMotion(BlockPos pos, RailShape dir) {
        double motionX = this.getMotion().x;
        double motionY = this.getMotion().y;
        double motionZ = this.getMotion().z;

        double length = Math.sqrt(motionX * motionX + motionZ * motionZ);

        if (length > 0.01D) {
            motionX += motionX / length * 0.06D;
            motionZ += motionZ / length * 0.06D;
        } else if (dir == RailShape.EAST_WEST) {
            if (world.getBlockState(pos.west()).isNormalCube()) {
                motionX = 0.02D;
            } else if (world.getBlockState(pos.east()).isNormalCube()) {
                motionX = -0.02D;
            }
        } else if (dir == RailShape.NORTH_SOUTH) {
            if (world.getBlockState(pos.north()).isNormalCube()) {
                motionZ = 0.02D;
            } else if (world.getBlockState(pos.south()).isNormalCube()) {
                motionZ = -0.02D;
            }
        }
        setMotion(motionX, motionY, motionZ);
    }

    private void restrictMotionUnpowered() {
        double motionX = this.getMotion().x;
        double motionY = this.getMotion().y;
        double motionZ = this.getMotion().z;

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
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        if (state.getBlock() == ModBlocks.fluxBeamBlock) {
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
        double motionX = this.getMotion().x;
        double motionY = this.getMotion().y;
        double motionZ = this.getMotion().z;

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
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float f = this.getWidth() / 2.0F;
        float f1 = this.getHeight();
        this.setBoundingBox(new AxisAlignedBB(x - f, y, z - f, x + f, y + f1, z + f));
    }

    // Client side only
    // Calculate yaw and pitch based on block below the levitator
    private Pair<Float, Float> calculateYawPitch() {
        Vec3d oldPos = getPos(posX, posY, posZ);
        float yaw = rotationYaw;
        float pitch = rotationPitch;

        if (oldPos != null) {
            Vec3d posUp = getPosOffset(posX, posY, posZ, 0.3D);
            Vec3d posDown = getPosOffset(posX, posY, posZ, -0.3D);

            if (posUp == null) {
                posUp = oldPos;
            }

            if (posDown == null) {
                posDown = oldPos;
            }

            Vec3d newpos = posDown.add(-posUp.x, -posUp.y, -posUp.z);

            if (newpos.lengthSquared() != 0.0D) {
                newpos = newpos.normalize();
                yaw = (float) (Math.atan2(newpos.z, newpos.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(newpos.y) * 73.0D);
            }
        }
        return Pair.of(yaw, pitch);
    }

    // Client only
    @Nullable
    public Vec3d getPosOffset(double x, double y, double z, double offset) {
        int floorX = MathHelper.floor(x);
        int floorY = MathHelper.floor(y);
        int floorZ = MathHelper.floor(z);

        Block block = world.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock();
        if (isValidBeamBlock(block)) {
            --floorY;
        }

        BlockState state = world.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (isValidBeamBlock(state.getBlock())) {
            RailShape dir = getBeamDirection(state);
            y = floorY;

            if (dir.isAscending()) {
                y++;
            }

            int[][] aint = MATRIX[dir.getMeta()];
            double dx = (aint[1][0] - aint[0][0]);
            double dz = (aint[1][2] - aint[0][2]);
            double length = Math.sqrt(dx * dx + dz * dz);
            dx = dx / length;
            dz = dz / length;
            x = x + dx * offset;
            z = z + dz * offset;

            if (aint[0][1] != 0 && MathHelper.floor(x) - floorX == aint[0][0] && MathHelper.floor(z) - floorZ == aint[0][2]) {
                y += aint[0][1];
            } else if (aint[1][1] != 0 && MathHelper.floor(x) - floorX == aint[1][0] && MathHelper.floor(z) - floorZ == aint[1][2]) {
                y += aint[1][1];
            }

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

        BlockState state = world.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (isValidBeamBlock(state.getBlock())) {
            RailShape dir = getBeamDirection(state);
            int[][] aint = MATRIX[dir.getMeta()];
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

            return new Vec3d(x, y, z);
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
//        return this.getEntityBoundingBox();
//    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        if (compound.hasUniqueId("holoFront")) {
            setHoloFrontUUID(compound.getUniqueId("holoFront"));
        }
        if (compound.hasUniqueId("holoBack")) {
            setHoloBackUUID(compound.getUniqueId("holoBack"));
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
        if (getHoloFrontUUID() != null) {
            compound.putUniqueId("holoFront", getHoloFrontUUID());
        }
        if (getHoloBackUUID() != null) {
            compound.putUniqueId("holoBack", getHoloBackUUID());
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
        if (!world.isRemote) {
            if (!entityIn.noClip && !this.noClip) {
                if (!this.isPassenger(entityIn)) {
                    double dx = entityIn.posX - this.posX;
                    double dz = entityIn.posZ - this.posZ;
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

                        if (entityIn instanceof FluxLevitatorEntity) {
                            double ddx = entityIn.posX - this.posX;
                            double ddz = entityIn.posZ - this.posZ;
                            Vec3d vec3d = (new Vec3d(ddx, 0.0D, ddz)).normalize();
                            Vec3d vec3d1 = (new Vec3d(MathHelper.cos(this.rotationYaw * 0.017453292F), 0.0D, MathHelper.sin(this.rotationYaw * 0.017453292F))).normalize();
                            double d6 = Math.abs(vec3d.dotProduct(vec3d1));

                            if (d6 < 0.8D) {
                                return;
                            }

                            double motionX = this.getMotion().x;
                            double motionY = this.getMotion().y;
                            double motionZ = this.getMotion().z;
                            double emotionX = entityIn.getMotion().x;
                            double emotionY = entityIn.getMotion().y;
                            double emotionZ = entityIn.getMotion().z;
                            double d7 = emotionX + motionX;
                            double d8 = emotionZ + motionZ;

                            d7 = d7 / 2.0D;
                            d8 = d8 / 2.0D;
                            motionX *= 0.2D;
                            motionZ *= 0.2D;
                            this.addVelocity(d7 - dx, 0.0D, d8 - dz);
                            emotionX *= 0.2D;
                            emotionZ *= 0.2D;
                            entityIn.setMotion(emotionX, emotionY, emotionZ);
                            entityIn.addVelocity(d7 + dx, 0.0D, d8 + dz);

                            this.setMotion(motionX, motionY, motionZ);
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
        this.levitatorX = x;
        this.levitatorY = y;
        this.levitatorZ = z;
        this.levitatorYaw = yaw;
        this.levitatorPitch = pitch;
        this.turnProgress = posRotationIncrements + 2;
        this.setMotion(velocityX, velocityY, velocityZ);

        updateHoloGui();
    }

    public void setDamage(float damage) {
        this.dataManager.set(DAMAGE, damage);
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

    public void setRollingAmplitude(int rollingAmplitude) {
        this.dataManager.set(ROLLING_AMPLITUDE, rollingAmplitude);
    }

    public int getRollingAmplitude() {
        return this.dataManager.get(ROLLING_AMPLITUDE).intValue();
    }

    public void setRollingDirection(int rollingDirection) {
        this.dataManager.set(ROLLING_DIRECTION, rollingDirection);
    }

    public int getRollingDirection() {
        return this.dataManager.get(ROLLING_DIRECTION).intValue();
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        if (player.isSneaking()) {
            return false;
//        } else if (this.isBeingRidden()) {    // @todo
//            return true;
        } else {
            if (!world.isRemote) {
                player.startRiding(this);
            }

            return true;
        }
    }


    private BlockPos getCurrentRailPosition() {
        int x = MathHelper.floor(this.posX);
        int y = MathHelper.floor(this.posY);
        int z = MathHelper.floor(this.posZ);

        Block block = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
        if (isValidBeamBlock(block)) {
            y--;
        }
        return new BlockPos(x, y, z);
    }

    protected double getMaxSpeed() {
        BlockPos pos = this.getCurrentRailPosition();
        BlockState state = world.getBlockState(pos);
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
        double mX = this.getMotion().x;
        double mZ = this.getMotion().z;

        if (this.isBeingRidden()) {
            mX *= 0.75D;
            mZ *= 0.75D;
        }

        double max = this.getMaxSpeed();
        mX = MathHelper.clamp(mX, -max, max);
        mZ = MathHelper.clamp(mZ, -max, max);
        this.move(MoverType.SELF, mX, 0.0D, mZ);
    }
}