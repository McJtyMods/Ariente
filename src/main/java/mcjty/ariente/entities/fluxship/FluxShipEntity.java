package mcjty.ariente.entities.fluxship;

import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class FluxShipEntity extends Entity {

    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public FluxShipEntity(EntityType<? extends FluxShipEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.preventEntitySpawning = true;
    }

    public static FluxShipEntity create(World worldIn, double x, double y, double z) {
        FluxShipEntity entity = new FluxShipEntity(Registration.ENTITY_FLUX_SHIP.get(), worldIn);
        entity.setPosition(x, y, z);
        entity.setMotion(0, 0, 0);
        entity.prevPosX = x;
        entity.prevPosY = y;
        entity.prevPosZ = z;
        return entity;
    }

    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
//        this.levitatorX = x;
//        this.levitatorY = y;
//        this.levitatorZ = z;
//        this.levitatorYaw = yaw;
//        this.levitatorPitch = pitch;
//        this.turnProgress = posRotationIncrements + 2;
        super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        setMotion(velocityX, velocityY, velocityZ);
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        setMotion(x, y, z);
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
    }


    public void handleAction(FlyAction action) {
        Vector3d look = getLook(1.0f);
        switch (action) {
            case FORWARD:
                setMotion(look.x * 1, look.y * 1, look.z * 1);
                break;
            case BACKWARD:
                setMotion(look.x * -1, look.y * -1, look.z * -1);
                break;
            case TURNLEFT:
                setRotation(rotationYaw-.1f, rotationPitch);
                break;
            case TURNRIGHT:
                setRotation(rotationYaw+.1f, rotationPitch);
                break;
            case UP:
                setMotion(getMotion().x, .2f, getMotion().z);
                break;
            case DOWN:
                setMotion(getMotion().x, -.2f, getMotion().z);
                break;
            case START:
                break;
            case LAND:
                break;
        }
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (player.isSneaking()) {
            return ActionResultType.PASS;
//        } else if (this.isBeingRidden()) {    // @todo
//            return true;
        } else {
            if (!this.world.isRemote) {
                player.startRiding(this);
            }

            return ActionResultType.SUCCESS;
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

    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    //@Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() ? entityIn.getBoundingBox() : null;
    }

    //@Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Override
    public double getMountedYOffset() {
        return 0.0D;
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
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getBoundingBox();
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }
}