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
        this.blocksBuilding = true;
    }

    public static FluxShipEntity create(World worldIn, double x, double y, double z) {
        FluxShipEntity entity = new FluxShipEntity(Registration.ENTITY_FLUX_SHIP.get(), worldIn);
        entity.setPos(x, y, z);
        entity.setDeltaMovement(0, 0, 0);
        entity.xo = x;
        entity.yo = y;
        entity.zo = z;
        return entity;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
//        this.levitatorX = x;
//        this.levitatorY = y;
//        this.levitatorZ = z;
//        this.levitatorYaw = yaw;
//        this.levitatorPitch = pitch;
//        this.turnProgress = posRotationIncrements + 2;
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        setDeltaMovement(velocityX, velocityY, velocityZ);
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        setDeltaMovement(x, y, z);
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
    }


    public void handleAction(FlyAction action) {
        Vector3d look = getViewVector(1.0f);
        switch (action) {
            case FORWARD:
                setDeltaMovement(look.x * 1, look.y * 1, look.z * 1);
                break;
            case BACKWARD:
                setDeltaMovement(look.x * -1, look.y * -1, look.z * -1);
                break;
            case TURNLEFT:
                setRot(yRot-.1f, xRot);
                break;
            case TURNRIGHT:
                setRot(yRot+.1f, xRot);
                break;
            case UP:
                setDeltaMovement(getDeltaMovement().x, .2f, getDeltaMovement().z);
                break;
            case DOWN:
                setDeltaMovement(getDeltaMovement().x, -.2f, getDeltaMovement().z);
                break;
            case START:
                break;
            case LAND:
                break;
        }
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (player.isShiftKeyDown()) {
            return ActionResultType.PASS;
//        } else if (this.isBeingRidden()) {    // @todo
//            return true;
        } else {
            if (!this.level.isClientSide) {
                player.startRiding(this);
            }

            return ActionResultType.SUCCESS;
        }
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
    public void positionRider(Entity passenger) {
        if (this.hasPassenger(passenger)) {
            if (!(passenger instanceof IHoloGuiEntity)) {
                super.positionRider(passenger);
            }
        }
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected boolean canRide(Entity entityIn) {
        return true;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {

    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    //@Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.isPushable() ? entityIn.getBoundingBox() : null;
    }

    //@Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0D;
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
    }

    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    @Override
    public void push(Entity entityIn) {
    }
}