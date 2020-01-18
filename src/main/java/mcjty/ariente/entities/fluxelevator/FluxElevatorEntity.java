package mcjty.ariente.entities.fluxelevator;

import com.google.common.base.Optional;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.items.ModItems;
import mcjty.hologui.api.CloseStrategy;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class FluxElevatorEntity extends Entity {

    private static final DataParameter<Integer> SPEED = EntityDataManager.createKey(FluxElevatorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(FluxElevatorEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Optional<UUID>> HOLO = EntityDataManager.createKey(FluxElevatorEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final float LENGTH = 2.5f;
    public static final float MAX_SPEED = 1.2f;

    private double elevatorX;
    private double elevatorY;
    private double elevatorZ;
    private double elevatorYaw;
    private double elevatorPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    private IHoloGuiEntity holoGui;

    private BlockPos desiredDestination = null;

    public FluxElevatorEntity(World worldIn) {
        super(worldIn);
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

            if (this.width > f && !this.firstUpdate && !this.world.isRemote) {
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

    public IHoloGuiEntity getHoloGui() {
        if (holoGui == null) {
            if (getHoloUUID() != null) {
                for (Entity entity : world.getEntitiesWithinAABB(Ariente.guiHandler.getHoloEntityClass(), getEntityBoundingBox().grow(10))) {
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

    public FluxElevatorEntity(World worldIn, double x, double y, double z) {
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

    public void setHoloUUID(UUID holoFront) {
        this.dataManager.set(HOLO, Optional.fromNullable(holoFront));
    }

    public UUID getHoloUUID() {
        return (UUID) ((Optional) this.dataManager.get(HOLO)).orNull();
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
        this.dataManager.register(SPEED, Integer.valueOf(0));
        this.dataManager.register(DAMAGE, Float.valueOf(0.0F));
        this.dataManager.register(HOLO, Optional.absent());
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
        if (!this.world.isRemote && !this.isDead) {
            if (this.isEntityInvulnerable(source)) {
                return false;
            } else {
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
        if (getHoloGui() != null) {
            holoGui.getEntity().dismountRidingEntity();
            holoGui.getEntity().setDead();
            holoGui = null;
        }
        super.setDead();
    }

    public void killLevitator(DamageSource source) {
        this.setDead();

        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
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
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public Direction getAdjustedHorizontalFacing() {
        return this.getHorizontalFacing().rotateY();
    }

    @Override
    public void onUpdate() {
        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.posY < -64.0D) {
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
        BlockState state = this.world.getBlockState(blockpos);

        if (isValidBeamBlock(state.getBlock())) {
            this.moveAlongTrack(blockpos, state);
        } else {
            motionY = 0;        // Stop
        }

        this.doBlockCollisions();
        this.rotationPitch = 0.0F;
        double dx = this.prevPosX - this.posX;
        double dz = this.prevPosZ - this.posZ;

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
        return block == ModBlocks.fluxBeamBlock || block == ModBlocks.fluxBendBeamBlock;
    }

    private void handleEntityCollision() {
        AxisAlignedBB box;
        box = this.getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D);

        if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D) {
            List<Entity> list = this.world.getEntitiesInAABBexcluding(this, box, EntitySelectors.getTeamCollisionPredicate(this));

            if (!list.isEmpty()) {
                for (Entity ent : list) {
                    if (!(ent instanceof PlayerEntity) && !(ent instanceof EntityIronGolem) && !(ent instanceof FluxElevatorEntity) && !this.isBeingRidden() && !ent.isRiding()) {
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
        this.setPosition(this.posX, this.posY, this.posZ);
        this.setRotation(this.rotationYaw, this.rotationPitch);
    }

    private void handlePortal() {
        if (!this.world.isRemote && this.world instanceof WorldServer) {
            MinecraftServer minecraftserver = this.world.getMinecraftServer();
            int i = this.getMaxInPortalTime();

            if (this.inPortal) {
                if (minecraftserver.getAllowNether()) {
                    if (!this.isRiding() && this.portalCounter++ >= i) {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        int id;

                        if (this.world.provider.getDimensionType().getId() == -1) {
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
        if (getHoloGui() != null) {
            updateHoloGui(this.holoGui, 1);
        }
    }

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

    protected void moveAlongTrack(BlockPos pos, BlockState state) {
        this.fallDistance = 0.0F;
        Vec3d oldPos = this.getPos(this.posX, this.posY, this.posZ);
        this.posY = pos.getY();
        int speed = getSpeed();
        boolean powered = speed != 0;    // Like powered
        boolean unpowered = speed == 0;

        EnumRailDirection dir = getBeamDirection(state);
//        handleBeamAscend(dir);        // Ascend not supported

        if (speed != 0) {
            handleLivingMotion(speed, dir);
        }

        if (unpowered) {
            restrictMotionUnpowered();
        }

        this.setPosition(this.posX, this.posY, this.posZ);
        this.moveLevitatorOnBeam(pos);

        this.applyDrag();
        Vec3d newPos = this.getPos(this.posX, this.posY, this.posZ);

        if (newPos != null && oldPos != null) {
            double d14 = (oldPos.y - newPos.y) * 0.05D;
            double motionLength = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (motionLength > 0.0D) {
                this.motionX = this.motionX / motionLength * (motionLength + d14);
                this.motionZ = this.motionZ / motionLength * (motionLength + d14);
            }

            this.setPosition(this.posX, newPos.y, this.posZ);
        }

        int floorX = MathHelper.floor(this.posX);
        int floorZ = MathHelper.floor(this.posZ);

        if (powered) {
            handlePoweredMotion(pos, dir);
        }
    }

    private void handleLivingMotion(int speed, EnumRailDirection dir) {

        float yaw;
        if (dir == EnumRailDirection.NORTH_SOUTH) {
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
        double dist = this.motionX * this.motionX + this.motionZ * this.motionZ;

        if (dist < 0.01D) {
//            this.motionX += dx * 0.1D;
//            this.motionZ += dz * 0.1D;
            if (dx > 0) {
                this.motionX = Math.abs(motionX) + dx * 0.1D;
            } else {
                this.motionX = -Math.abs(motionX) + dx * 0.1D;
            }
            if (dz > 0) {
                this.motionZ = Math.abs(motionZ) + dz * 0.1D;
            } else {
                this.motionZ = -Math.abs(motionZ) + dz * 0.1D;
            }
        } else {
            if (dx > 0) {
                this.motionX = Math.abs(motionX);
            } else {
                this.motionX = -Math.abs(motionX);
            }
            if (dz > 0) {
                this.motionZ = Math.abs(motionZ);
            } else {
                this.motionZ = -Math.abs(motionZ);
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
    }

    private void handlePoweredMotion(BlockPos pos, EnumRailDirection dir) {
        double length = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (length > 0.01D) {
            this.motionX += this.motionX / length * 0.06D;
            this.motionZ += this.motionZ / length * 0.06D;
        } else if (dir == EnumRailDirection.EAST_WEST) {
            if (this.world.getBlockState(pos.west()).isNormalCube()) {
                this.motionX = 0.02D;
            } else if (this.world.getBlockState(pos.east()).isNormalCube()) {
                this.motionX = -0.02D;
            }
        } else if (dir == EnumRailDirection.NORTH_SOUTH) {
            if (this.world.getBlockState(pos.north()).isNormalCube()) {
                this.motionZ = 0.02D;
            } else if (this.world.getBlockState(pos.south()).isNormalCube()) {
                this.motionZ = -0.02D;
            }
        }
    }

    private void restrictMotionUnpowered() {
        double length = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (length < 0.03D) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        } else {
            this.motionX *= 0.5D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.5D;
        }
    }

    public static EnumRailDirection getBeamDirection(BlockState state) {
        if (state.getBlock() == ModBlocks.fluxBeamBlock) {
            Direction facing = state.getValue(BaseBlock.FACING_HORIZ);
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                return EnumRailDirection.EAST_WEST;
            } else {
                return EnumRailDirection.NORTH_SOUTH;
            }
        } else {
            Direction facing = state.getValue(BaseBlock.FACING_HORIZ);
            switch (facing) {
                case NORTH:
                    return EnumRailDirection.NORTH_EAST;
                case SOUTH:
                    return EnumRailDirection.NORTH_WEST;
                case WEST:
                    return EnumRailDirection.SOUTH_WEST;
                case EAST:
                    return EnumRailDirection.SOUTH_EAST;
                default:
                    break;
            }
        }
        throw new IllegalStateException("This didn't happen");
    }

    protected void applyDrag() {
        if (this.isBeingRidden()) {
            this.motionX *= 0.997D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.997D;
        } else {
            this.motionX *= 0.96D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.96D;
        }
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float f = this.width / 2.0F;
        float f1 = this.height;
        this.setEntityBoundingBox(new AxisAlignedBB(x - f, y, z - f, x + f, y + f1, z + f));
    }

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

            Vec3d newpos = posDown.addVector(-posUp.x, -posUp.y, -posUp.z);

            if (newpos.lengthVector() != 0.0D) {
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
            EnumRailDirection dir = getBeamDirection(state);
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
            EnumRailDirection dir = getBeamDirection(state);
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
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    protected void readEntityFromNBT(CompoundNBT compound) {
        if (compound.hasUniqueId("holo")) {
            setHoloUUID(compound.getUniqueId("holo"));
        }
        changeSpeed(compound.getInteger("speed"));
        if (compound.hasKey("desiredDestX")) {
            desiredDestination = new BlockPos(compound.getInteger("desiredDestX"),
                    compound.getInteger("desiredDestY"),
                    compound.getInteger("desiredDestZ"));
        }
    }

    @Override
    protected void writeEntityToNBT(CompoundNBT compound) {
        if (getHoloUUID() != null) {
            compound.setUniqueId("holo", getHoloUUID());
        }
        compound.setInteger("speed", getSpeed());
        if (desiredDestination != null) {
            compound.setInteger("desiredDestX", desiredDestination.getX());
            compound.setInteger("desiredDestY", desiredDestination.getY());
            compound.setInteger("desiredDestZ", desiredDestination.getZ());
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!this.world.isRemote) {
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

                        if (entityIn instanceof FluxElevatorEntity) {
                            double ddx = entityIn.posX - this.posX;
                            double ddz = entityIn.posZ - this.posZ;
                            Vec3d vec3d = (new Vec3d(ddx, 0.0D, ddz)).normalize();
                            Vec3d vec3d1 = (new Vec3d(MathHelper.cos(this.rotationYaw * 0.017453292F), 0.0D, MathHelper.sin(this.rotationYaw * 0.017453292F))).normalize();
                            double d6 = Math.abs(vec3d.dotProduct(vec3d1));

                            if (d6 < 0.8D) {
                                return;
                            }

                            double d7 = entityIn.motionX + this.motionX;
                            double d8 = entityIn.motionZ + this.motionZ;

                            d7 = d7 / 2.0D;
                            d8 = d8 / 2.0D;
                            this.motionX *= 0.2D;
                            this.motionZ *= 0.2D;
                            this.addVelocity(d7 - dx, 0.0D, d8 - dz);
                            entityIn.motionX *= 0.2D;
                            entityIn.motionZ *= 0.2D;
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
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.elevatorX = x;
        this.elevatorY = y;
        this.elevatorZ = z;
        this.elevatorYaw = yaw;
        this.elevatorPitch = pitch;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;

        updateHoloGui();
    }

    public void setDamage(float damage) {
        this.dataManager.set(DAMAGE, Float.valueOf(damage));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        this.velocityX = this.motionX;
        this.velocityY = this.motionY;
        this.velocityZ = this.motionZ;
    }

    public float getDamage() {
        return this.dataManager.get(DAMAGE).floatValue();
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        if (player.isSneaking()) {
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
        double mX = this.motionX;
        double mZ = this.motionZ;

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