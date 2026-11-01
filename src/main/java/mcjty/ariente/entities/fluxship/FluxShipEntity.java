package mcjty.ariente.entities.fluxship;

import mcjty.ariente.setup.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class FluxShipEntity extends Entity {

    private int forward;
    private int turn;
    private int vertical;
    private int inputTimeout;
    private int lerpSteps;
    private Vec3 lerpTarget = Vec3.ZERO;
    private float lerpYaw;

    public FluxShipEntity(EntityType<? extends FluxShipEntity> type, Level level) {
        super(type, level);
        blocksBuilding = true;
    }

    public static FluxShipEntity create(Level level, double x, double y, double z) {
        FluxShipEntity ship = new FluxShipEntity(Registration.ENTITY_FLUX_SHIP.get(), level);
        ship.setPos(x, y, z);
        ship.setDeltaMovement(Vec3.ZERO);
        return ship;
    }

    public void handleInput(Player player, int forward, int turn, int vertical) {
        if (getControllingPassenger() == player && Math.abs(forward) <= 1 && Math.abs(turn) <= 1 && Math.abs(vertical) <= 1) {
            this.forward = forward;
            this.turn = turn;
            this.vertical = vertical;
            inputTimeout = 5;
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() || isVehicle()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof Player && getPassengers().isEmpty();
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player ? player : null;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        // Movement is simulated on the server. Do not send vanilla client vehicle-position packets.
        return !level().isClientSide;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.4;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && player.isShiftKeyDown() && !isVehicle()) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    ItemStack drop = new ItemStack(Registration.FLUX_SHIP.get());
                    if (hasCustomName()) {
                        drop.setHoverName(getCustomName());
                    }
                    spawnAtLocation(drop);
                }
                discard();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        lerpTarget = new Vec3(x, y, z);
        lerpYaw = yaw;
        lerpSteps = Math.max(1, steps);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (lerpSteps > 0) {
                setPos(position().lerp(lerpTarget, 1.0 / lerpSteps));
                setYRot(getYRot() + Mth.wrapDegrees(lerpYaw - getYRot()) / lerpSteps);
                lerpSteps--;
            }
            return;
        }
        if (getControllingPassenger() == null || inputTimeout-- <= 0) {
            forward = turn = vertical = 0;
        }
        setYRot(getYRot() + turn * 3);
        Vec3 target = Vec3.directionFromRotation(0, getYRot()).scale(forward * .65).add(0, vertical * .35, 0);
        setDeltaMovement(getDeltaMovement().lerp(target, .3));
        move(MoverType.SELF, getDeltaMovement());
        fallDistance = 0;
        if (getControllingPassenger() != null) {
            getControllingPassenger().fallDistance = 0;
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Controls are transient. A loaded, unattended ship must remain parked.
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(2);
    }
}
