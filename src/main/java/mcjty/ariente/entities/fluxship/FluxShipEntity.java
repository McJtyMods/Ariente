package mcjty.ariente.entities.fluxship;

import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class FluxShipEntity extends Entity {

    private double velocityX;
    private double velocityY;
    private double velocityZ;

    public FluxShipEntity(EntityType<? extends FluxShipEntity> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.blocksBuilding = true;
    }

    public static FluxShipEntity create(Level worldIn, double x, double y, double z) {
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
        Vec3 look = getViewVector(1.0f);
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
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    //@Override
    @Nullable
    public AABB getCollisionBox(Entity entityIn) {
        return entityIn.isPushable() ? entityIn.getBoundingBox() : null;
    }

    //@Override
    @Nullable
    public AABB getCollisionBoundingBox() {
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
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    @Override
    public void push(Entity entityIn) {
    }
}