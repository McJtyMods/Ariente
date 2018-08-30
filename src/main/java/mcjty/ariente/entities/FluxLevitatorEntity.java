package mcjty.ariente.entities;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.items.ModItems;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class FluxLevitatorEntity extends Entity {

    private static final DataParameter<Integer> ROLLING_AMPLITUDE = EntityDataManager.<Integer>createKey(FluxLevitatorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ROLLING_DIRECTION = EntityDataManager.<Integer>createKey(FluxLevitatorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DAMAGE = EntityDataManager.<Float>createKey(FluxLevitatorEntity.class, DataSerializers.FLOAT);
    private boolean isInReverse;
    private static final int[][][] MATRIX = new int[][][]{{{0, 0, -1}, {0, 0, 1}}, {{-1, 0, 0}, {1, 0, 0}}, {{-1, -1, 0}, {1, 0, 0}}, {{-1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, {-1, 0, 0}}, {{0, 0, -1}, {-1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
    private int turnProgress;
    private double levitatorX;
    private double levitatorY;
    private double levitatorZ;
    private double levitatorYaw;
    private double levitatorPitch;
    @SideOnly(Side.CLIENT)
    private double velocityX;
    @SideOnly(Side.CLIENT)
    private double velocityY;
    @SideOnly(Side.CLIENT)
    private double velocityZ;

    public static float defaultMaxSpeedAirLateral = 0.4f;
    public static float defaultMaxSpeedAirVertical = -1f;
    public static double defaultDragAir = 0.94999998807907104D;
    protected boolean canUseRail = true;
    protected boolean canBePushed = true;

    /* Instance versions of the above physics properties */
    private float currentSpeedRail = getMaxCartSpeedOnRail();
    protected float maxSpeedAirLateral = defaultMaxSpeedAirLateral;
    protected float maxSpeedAirVertical = defaultMaxSpeedAirVertical;
    protected double dragAir = defaultDragAir;

    private float length;

    public FluxLevitatorEntity(World worldIn) {
        super(worldIn);
        this.preventEntitySpawning = true;
        length = 2.5f;
        this.setSize(1.30F, 0.9F);
    }

    @Override
    protected void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            float f = this.width;
            this.width = width;
            this.height = height;

            if (this.width < f) {
                double dx = (double) length / 2.0D;
                double d0 = (double) width / 2.0D;
                this.setEntityBoundingBox(new AxisAlignedBB(this.posX - dx, this.posY, this.posZ - d0, this.posX + dx, this.posY + (double) this.height, this.posZ + d0));
                return;
            }

            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double) this.length, axisalignedbb.minY + (double) this.height, axisalignedbb.minZ + (double) this.width));

            if (this.width > f && !this.firstUpdate && !this.world.isRemote) {
                this.move(MoverType.SELF, (double) (f - this.length), 0.0D, (double) (f - this.width));
            }
        }
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
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return true;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ROLLING_AMPLITUDE, Integer.valueOf(0));
        this.dataManager.register(ROLLING_DIRECTION, Integer.valueOf(1));
        this.dataManager.register(DAMAGE, Float.valueOf(0.0F));
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
        return canBePushed;
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
                this.setRollingDirection(-this.getRollingDirection());
                this.setRollingAmplitude(10);
                this.markVelocityChanged();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean flag = source.getTrueSource() instanceof EntityPlayer && ((EntityPlayer) source.getTrueSource()).capabilities.isCreativeMode;

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
        this.setRollingDirection(-this.getRollingDirection());
        this.setRollingAmplitude(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public EnumFacing getAdjustedHorizontalFacing() {
        return this.isInReverse ? this.getHorizontalFacing().getOpposite().rotateY() : this.getHorizontalFacing().rotateY();
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

        if (this.world.isRemote) {
            if (this.turnProgress > 0) {
                double turnX = this.posX + (this.levitatorX - this.posX) / this.turnProgress;
                double turnY = this.posY + (this.levitatorY - this.posY) / this.turnProgress;
                double turnZ = this.posZ + (this.levitatorZ - this.posZ) / this.turnProgress;
                double yaw = MathHelper.wrapDegrees(this.levitatorYaw - this.rotationYaw);
                this.rotationYaw = (float) (this.rotationYaw + yaw / this.turnProgress);
                this.rotationPitch = (float) (this.rotationPitch + (this.levitatorPitch - this.rotationPitch) / this.turnProgress);
                --this.turnProgress;
                this.setPosition(turnX, turnY, turnZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            } else {
                this.setPosition(this.posX, this.posY, this.posZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
        } else {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (!this.hasNoGravity()) {
                this.motionY -= 0.04D;
            }

            int floorX = MathHelper.floor(this.posX);
            int floorY = MathHelper.floor(this.posY);
            int floorZ = MathHelper.floor(this.posZ);

            if (world.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock() == ModBlocks.fluxBeamBlock) {
                --floorY;
            }

            BlockPos blockpos = new BlockPos(floorX, floorY, floorZ);
            IBlockState state = this.world.getBlockState(blockpos);

            if (canUseRail() && state.getBlock() == ModBlocks.fluxBeamBlock) {
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

                if (this.isInReverse) {
                    this.rotationYaw += 180.0F;
                }
            }

            double yaw = MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw);

            if (yaw < -170.0D || yaw >= 170.0D) {
                this.rotationYaw += 180.0F;
                this.isInReverse = !this.isInReverse;
            }

            this.setRotation(this.rotationYaw, this.rotationPitch);

            AxisAlignedBB box;
            box = this.getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D);

            if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D) {
                List<Entity> list = this.world.getEntitiesInAABBexcluding(this, box, EntitySelectors.getTeamCollisionPredicate(this));

                if (!list.isEmpty()) {
                    for (Entity ent : list) {
                        if (!(ent instanceof EntityPlayer) && !(ent instanceof EntityIronGolem) && !(ent instanceof FluxLevitatorEntity) && !this.isBeingRidden() && !ent.isRiding()) {
                            ent.startRiding(this);
                        } else {
                            ent.applyEntityCollision(this);
                        }
                    }
                }
            } else {
                for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, box)) {
                    if (!this.isPassenger(entity) && entity.canBePushed() && entity instanceof FluxLevitatorEntity) {
                        entity.applyEntityCollision(this);
                    }
                }
            }

            this.handleWaterMovement();
        }
    }

    protected double getMaximumSpeed() {
        return 0.8D;
    }

    protected void moveDerailedLevitator() {
        double speed = onGround ? this.getMaximumSpeed() : getMaxSpeedAirLateral();
        this.motionX = MathHelper.clamp(this.motionX, -speed, speed);
        this.motionZ = MathHelper.clamp(this.motionZ, -speed, speed);

        double moveY = motionY;
        if (getMaxSpeedAirVertical() > 0 && motionY > getMaxSpeedAirVertical()) {
            moveY = getMaxSpeedAirVertical();
            if (Math.abs(motionX) < 0.3f && Math.abs(motionZ) < 0.3f) {
                moveY = 0.15f;
                motionY = moveY;
            }
        }

        if (this.onGround) {
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        }

        this.move(MoverType.SELF, this.motionX, moveY, this.motionZ);

        if (!this.onGround) {
            this.motionX *= getDragAir();
            this.motionY *= getDragAir();
            this.motionZ *= getDragAir();
        }
    }

    protected void moveAlongTrack(BlockPos pos, IBlockState state) {
        this.fallDistance = 0.0F;
        Vec3d vec3d = this.getPos(this.posX, this.posY, this.posZ);
        this.posY = pos.getY();
        boolean flag = true;    // Like powered
        boolean flag1 = false;

//        BlockRailBase blockrailbase = (BlockRailBase) state.getBlock();
//        if (blockrailbase == Blocks.GOLDEN_RAIL) {
//            flag = state.getValue(BlockRailPowered.POWERED).booleanValue();
//            flag1 = !flag;
//        }

        double slopeAdjustment = getSlopeAdjustment();
        BlockRailBase.EnumRailDirection dir = getBeamDirection(state);

        switch (dir) {
            case ASCENDING_EAST:
                this.motionX -= slopeAdjustment;
                ++this.posY;
                break;
            case ASCENDING_WEST:
                this.motionX += slopeAdjustment;
                ++this.posY;
                break;
            case ASCENDING_NORTH:
                this.motionZ += slopeAdjustment;
                ++this.posY;
                break;
            case ASCENDING_SOUTH:
                this.motionZ -= slopeAdjustment;
                ++this.posY;
        }

        int[][] aint = MATRIX[dir.getMetadata()];
        double d1 = (aint[1][0] - aint[0][0]);
        double d2 = (aint[1][2] - aint[0][2]);
        double d3 = Math.sqrt(d1 * d1 + d2 * d2);
        double d4 = this.motionX * d1 + this.motionZ * d2;

        if (d4 < 0.0D) {
            d1 = -d1;
            d2 = -d2;
        }

        double d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (d5 > 2.0D) {
            d5 = 2.0D;
        }

        this.motionX = d5 * d1 / d3;
        this.motionZ = d5 * d2 / d3;
        Entity entity = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);

        if (entity instanceof EntityLivingBase) {
            double d6 = ((EntityLivingBase) entity).moveForward;

            if (d6 > 0.0D) {
                double dx = -Math.sin((entity.rotationYaw * 0.017453292F));
                double dz = Math.cos((entity.rotationYaw * 0.017453292F));
                double d9 = this.motionX * this.motionX + this.motionZ * this.motionZ;

                if (d9 < 0.01D) {
                    this.motionX += dx * 0.1D;
                    this.motionZ += dz * 0.1D;
                    flag1 = false;
                }
            }
        }

        if (flag1) {
            double d17 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (d17 < 0.03D) {
                this.motionX *= 0.0D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.0D;
            } else {
                this.motionX *= 0.5D;
                this.motionY *= 0.0D;
                this.motionZ *= 0.5D;
            }
        }

        double d18 = pos.getX() + 0.5D + aint[0][0] * 0.5D;
        double d19 = pos.getZ() + 0.5D + aint[0][2] * 0.5D;
        double d20 = pos.getX() + 0.5D + aint[1][0] * 0.5D;
        double d21 = pos.getZ() + 0.5D + aint[1][2] * 0.5D;
        d1 = d20 - d18;
        d2 = d21 - d19;
        double d10;

        if (d1 == 0.0D) {
            this.posX = pos.getX() + 0.5D;
            d10 = this.posZ - pos.getZ();
        } else if (d2 == 0.0D) {
            this.posZ = pos.getZ() + 0.5D;
            d10 = this.posX - pos.getX();
        } else {
            double d11 = this.posX - d18;
            double d12 = this.posZ - d19;
            d10 = (d11 * d1 + d12 * d2) * 2.0D;
        }

        this.posX = d18 + d1 * d10;
        this.posZ = d19 + d2 * d10;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.moveLevitatorOnBeam(pos);

        if (aint[0][1] != 0 && MathHelper.floor(this.posX) - pos.getX() == aint[0][0] && MathHelper.floor(this.posZ) - pos.getZ() == aint[0][2]) {
            this.setPosition(this.posX, this.posY + aint[0][1], this.posZ);
        } else if (aint[1][1] != 0 && MathHelper.floor(this.posX) - pos.getX() == aint[1][0] && MathHelper.floor(this.posZ) - pos.getZ() == aint[1][2]) {
            this.setPosition(this.posX, this.posY + aint[1][1], this.posZ);
        }

        this.applyDrag();
        Vec3d vec3d1 = this.getPos(this.posX, this.posY, this.posZ);

        if (vec3d1 != null && vec3d != null) {
            double d14 = (vec3d.y - vec3d1.y) * 0.05D;
            d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (d5 > 0.0D) {
                this.motionX = this.motionX / d5 * (d5 + d14);
                this.motionZ = this.motionZ / d5 * (d5 + d14);
            }

            this.setPosition(this.posX, vec3d1.y, this.posZ);
        }

        int floorX = MathHelper.floor(this.posX);
        int floorZ = MathHelper.floor(this.posZ);

        if (floorX != pos.getX() || floorZ != pos.getZ()) {
            d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.motionX = d5 * (floorX - pos.getX());
            this.motionZ = d5 * (floorZ - pos.getZ());
        }


        if (flag) {
            double d15 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            if (d15 > 0.01D) {
                double d16 = 0.06D;
                this.motionX += this.motionX / d15 * 0.06D;
                this.motionZ += this.motionZ / d15 * 0.06D;
            } else if (dir == BlockRailBase.EnumRailDirection.EAST_WEST) {
                if (this.world.getBlockState(pos.west()).isNormalCube()) {
                    this.motionX = 0.02D;
                } else if (this.world.getBlockState(pos.east()).isNormalCube()) {
                    this.motionX = -0.02D;
                }
            } else if (dir == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
                if (this.world.getBlockState(pos.north()).isNormalCube()) {
                    this.motionZ = 0.02D;
                } else if (this.world.getBlockState(pos.south()).isNormalCube()) {
                    this.motionZ = -0.02D;
                }
            }
        }
    }

    public static BlockRailBase.EnumRailDirection getBeamDirection(IBlockState state) {
        EnumFacing facing = state.getValue(BaseBlock.FACING_HORIZ);
        if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
            return BlockRailBase.EnumRailDirection.EAST_WEST;
        } else {
            return BlockRailBase.EnumRailDirection.NORTH_SOUTH;
        }
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

    @Nullable
    @SideOnly(Side.CLIENT)
    public Vec3d getPosOffset(double x, double y, double z, double offset) {
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);

        if (world.getBlockState(new BlockPos(i, j - 1, k)).getBlock() == ModBlocks.fluxBeamBlock) {
            --j;
        }

        IBlockState state = this.world.getBlockState(new BlockPos(i, j, k));

        if (state.getBlock() == ModBlocks.fluxBeamBlock) {
            BlockRailBase.EnumRailDirection dir = getBeamDirection(state);
            y = j;

            if (dir.isAscending()) {
                y = (j + 1);
            }

            int[][] aint = MATRIX[dir.getMetadata()];
            double d0 = (aint[1][0] - aint[0][0]);
            double d1 = (aint[1][2] - aint[0][2]);
            double d2 = Math.sqrt(d0 * d0 + d1 * d1);
            d0 = d0 / d2;
            d1 = d1 / d2;
            x = x + d0 * offset;
            z = z + d1 * offset;

            if (aint[0][1] != 0 && MathHelper.floor(x) - i == aint[0][0] && MathHelper.floor(z) - k == aint[0][2]) {
                y += aint[0][1];
            } else if (aint[1][1] != 0 && MathHelper.floor(x) - i == aint[1][0] && MathHelper.floor(z) - k == aint[1][2]) {
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

        if (world.getBlockState(new BlockPos(floorX, floorY - 1, floorZ)).getBlock() == ModBlocks.fluxBeamBlock) {
            --floorY;
        }

        IBlockState state = this.world.getBlockState(new BlockPos(floorX, floorY, floorZ));

        if (state.getBlock() == ModBlocks.fluxBeamBlock) {
            BlockRailBase.EnumRailDirection dir = getBeamDirection(state);
            int[][] aint = MATRIX[dir.getMetadata()];
            double d0 = floorX + 0.5D + aint[0][0] * 0.5D;
            double d1 = floorY + 0.0625D + aint[0][1] * 0.5D;
            double d2 = floorZ + 0.5D + aint[0][2] * 0.5D;
            double d3 = floorX + 0.5D + aint[1][0] * 0.5D;
            double d4 = floorY + 0.0625D + aint[1][1] * 0.5D;
            double d5 = floorZ + 0.5D + aint[1][2] * 0.5D;
            double d6 = d3 - d0;
            double d7 = (d4 - d1) * 2.0D;
            double d8 = d5 - d2;
            double d9;

            if (d6 == 0.0D) {
                d9 = z - floorZ;
            } else if (d8 == 0.0D) {
                d9 = x - floorX;
            } else {
                double d10 = x - d0;
                double d11 = z - d2;
                d9 = (d10 * d6 + d11 * d8) * 2.0D;
            }

            x = d0 + d6 * d9;
            y = d1 + d7 * d9;
            z = d2 + d8 * d9;

            if (d7 < 0.0D) {
                ++y;
            }

            if (d7 > 0.0D) {
                y += 0.5D;
            }

            return new Vec3d(x, y, z);
        } else {
            return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (!this.world.isRemote) {
            if (!entityIn.noClip && !this.noClip) {
                if (!this.isPassenger(entityIn)) {
                    double dx = entityIn.posX - this.posX;
                    double dz = entityIn.posZ - this.posZ;
                    double d2 = dx * dx + dz * dz;

                    if (d2 >= 9.999999747378752E-5D) {
                        d2 = MathHelper.sqrt(d2);
                        dx = dx / d2;
                        dz = dz / d2;
                        double d3 = 1.0D / d2;

                        if (d3 > 1.0D) {
                            d3 = 1.0D;
                        }

                        dx = dx * d3;
                        dz = dz * d3;
                        dx = dx * 0.1D;
                        dz = dz * 0.1D;
                        dx = dx * (1.0F - this.entityCollisionReduction);
                        dz = dz * (1.0F - this.entityCollisionReduction);
                        dx = dx * 0.5D;
                        dz = dz * 0.5D;

                        if (entityIn instanceof FluxLevitatorEntity) {
                            double d4 = entityIn.posX - this.posX;
                            double d5 = entityIn.posZ - this.posZ;
                            Vec3d vec3d = (new Vec3d(d4, 0.0D, d5)).normalize();
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
        this.levitatorX = x;
        this.levitatorY = y;
        this.levitatorZ = z;
        this.levitatorYaw = yaw;
        this.levitatorPitch = pitch;
        this.turnProgress = posRotationIncrements + 2;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
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

    public void setRollingAmplitude(int rollingAmplitude) {
        this.dataManager.set(ROLLING_AMPLITUDE, Integer.valueOf(rollingAmplitude));
    }

    public int getRollingAmplitude() {
        return this.dataManager.get(ROLLING_AMPLITUDE).intValue();
    }

    public void setRollingDirection(int rollingDirection) {
        this.dataManager.set(ROLLING_DIRECTION, Integer.valueOf(rollingDirection));
    }

    public int getRollingDirection() {
        return this.dataManager.get(ROLLING_DIRECTION).intValue();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            return false;
        } else if (this.isBeingRidden()) {
            return true;
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

        if (world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() == ModBlocks.fluxBeamBlock) {
            y--;
        }
        return new BlockPos(x, y, z);
    }

    protected double getMaxSpeed() {
        if (!canUseRail()) {
            return getMaximumSpeed();
        }
        BlockPos pos = this.getCurrentRailPosition();
        IBlockState state = this.world.getBlockState(pos);
        if (state.getBlock() != ModBlocks.fluxBeamBlock) {
            return getMaximumSpeed();
        }

        float railMaxSpeed = 5; // @todo ((BlockRailBase) state.getBlock()).getRailMaxSpeed(world, this, pos);
        return Math.min(railMaxSpeed, getCurrentCartSpeedCapOnRail());
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

    /**
     * This function returns an ItemStack that represents this cart.
     * This should be an ItemStack that can be used by the player to place the cart,
     * but is not necessary the item the cart drops when destroyed.
     *
     * @return An ItemStack that can be used to place the cart.
     */
    public ItemStack getCartItem() {
        return new ItemStack(ModItems.fluxLevitatorItem);
    }

    /**
     * Returns true if this cart can currently use rails.
     * This function is mainly used to gracefully detach a levitator from a beam.
     *
     * @return True if the levitator can use rails.
     */
    public boolean canUseRail() {
        return canUseRail;
    }

    /**
     * Set whether the levitator can use rails.
     * This function is mainly used to gracefully detach a levitator from a rail.
     *
     * @param use Whether the levitator can currently use rails.
     */
    public void setCanUseRail(boolean use) {
        canUseRail = use;
    }

    /**
     * Getters/setters for physics variables
     */

    /**
     * Returns the carts max speed when traveling on rails. Carts going faster
     * than 1.1 cause issues with chunk loading. Carts cant traverse slopes or
     * corners at greater than 0.5 - 0.6. This value is compared with the rails
     * max speed and the carts current speed cap to determine the carts current
     * max speed. A normal rail's max speed is 0.4.
     *
     * @return Carts max speed.
     */
    public float getMaxCartSpeedOnRail() {
        return 1.2f;
    }

    /**
     * Returns the current speed cap for the cart when traveling on rails. This
     * functions differs from getMaxCartSpeedOnRail() in that it controls
     * current movement and cannot be overridden. The value however can never be
     * higher than getMaxCartSpeedOnRail().
     *
     * @return
     */
    public final float getCurrentCartSpeedCapOnRail() {
        return currentSpeedRail;
    }

    public final void setCurrentCartSpeedCapOnRail(float value) {
        value = Math.min(value, getMaxCartSpeedOnRail());
        currentSpeedRail = value;
    }

    public float getMaxSpeedAirLateral() {
        return maxSpeedAirLateral;
    }

    public void setMaxSpeedAirLateral(float value) {
        maxSpeedAirLateral = value;
    }

    public float getMaxSpeedAirVertical() {
        return maxSpeedAirVertical;
    }

    public void setMaxSpeedAirVertical(float value) {
        maxSpeedAirVertical = value;
    }

    public double getDragAir() {
        return dragAir;
    }

    public void setDragAir(double value) {
        dragAir = value;
    }

    public double getSlopeAdjustment() {
        return 0.0078125D;
    }

    /**
     * Called from Detector Rails to retrieve a redstone power level for comparators.
     */
    public int getComparatorLevel() {
        return -1;
    }
}