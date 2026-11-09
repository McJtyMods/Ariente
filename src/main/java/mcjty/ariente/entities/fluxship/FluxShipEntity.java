package mcjty.ariente.entities.fluxship;


import net.minecraft.util.Prediction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.PositionPath;
import org.joml.Vector3fc;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import javax.annotation.Nullable;
import mcjty.ariente.api.IFluxShipPilot;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.items.ItemData;
import mcjty.hologui.api.CloseStrategy;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class FluxShipEntity extends Entity {
    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return new InterpolationHandler.NoOpInterpolationHandler() {

            @Override
            public boolean interpolateTo(PositionPath position, float yRot, float xRot, boolean hasRotation) {
                if (position != null) {
                    Vec3 target = position.endPosition();
                    lerpTo(target.x, target.y, target.z, hasRotation ? yRot : getYRot(), hasRotation ? xRot : getXRot(), 3);
                }
                return true;
            }
        };
    }

    private static final EntityDataAccessor<Float> BANK = SynchedEntityData.defineId(FluxShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(FluxShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(FluxShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(FluxShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3fc> MOTION = SynchedEntityData.defineId(FluxShipEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> MOUSE_FLIGHT = SynchedEntityData.defineId(FluxShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CHARGE = SynchedEntityData.defineId(FluxShipEntity.class, EntityDataSerializers.INT);
    private static final double MAX_SPEED = 5.2;
    private static final double MAX_VERTICAL_SPEED = .35;
    private static final double UNPILOTED_DESCENT_SPEED = .2;
    private static final double UNPOWERED_DESCENT_SPEED = .05;
    private static final double HORIZONTAL_RESPONSE = .06;
    private static final double VERTICAL_RESPONSE = .08;
    private static final float MAX_TURN_SPEED = 3;
    private static final float MAX_BANK = 22;
    private static final float MAX_PITCH = 25;

    private int forward;
    private int turn;
    private int vertical;
    private int inputTimeout;
    private LivingEntity inputPilot;
    private Vec3 flightDirection;
    private double flightSpeed;
    private boolean firing;
    private int laserCooldown;
    private int hurtTime;
    private int lerpSteps;
    private Vec3 lerpTarget = Vec3.ZERO;
    private float turnSpeed;
    private float bank;
    private float previousBank;
    private float exhaustStrength;
    private float previousExhaustStrength;
    private float yawChange;
    private int lastPilotTurnTick = -1;
    private final FluxShipMotion remoteMotion = new FluxShipMotion();
    private boolean wasRemote;
    private boolean mouseSteering;
    private float aimYaw;
    private float aimPitch;
    private boolean localMouseFlight;
    private boolean localFollowHull = true;

    public FluxShipEntity(EntityType<? extends FluxShipEntity> type, Level level) {
        super(type, level);
        blocksBuilding = true;
    }

    public static FluxShipEntity create(Level level, double x, double y, double z) {
        FluxShipEntity ship = new FluxShipEntity(mcjty.ariente.setup.Registration.ENTITY_FLUX_SHIP.get(), level);
        ship.setPos(x, y, z);
        ship.setDeltaMovement(Vec3.ZERO);
        return ship;
    }

    public void handleInput(Player player, int forward, int turn, int vertical) {
        handleInput(player, forward, turn, vertical, false);
    }

    public void handleInput(Player player, int forward, int turn, int vertical, boolean firing) {
        handleInput(player, forward, turn, vertical, firing, false, false, 0, 0);
    }

    public void handleInput(Player player, int forward, int turn, int vertical, boolean firing,
                            boolean mouseFlight, boolean mouseSteering, float yaw, float pitch) {
        if (getControllingPassenger() == player && forward >= -1 && forward <= 1
                && turn >= -1 && turn <= 1 && vertical >= -1 && vertical <= 1
                && Float.isFinite(yaw) && Float.isFinite(pitch)) {
            this.forward = forward;
            this.turn = turn;
            this.vertical = vertical;
            this.firing = firing;
            flightDirection = null;
            inputPilot = player;
            inputTimeout = 5;
            entityData.set(MOUSE_FLIGHT, mouseFlight);
            this.mouseSteering = mouseFlight && mouseSteering;
            aimYaw = Mth.wrapDegrees(yaw);
            aimPitch = Mth.clamp(pitch, -55, 55);
        }
    }

    public void setLocalMouseControls(boolean enabled, boolean followHull) {
        localMouseFlight = enabled;
        localFollowHull = followHull;
    }

    /** Companion-mod pilots request flight; the ship still limits turns, pitch and acceleration. */

    public void flyTowards(LivingEntity pilot, Vec3 direction, double speed) {
        if (!level().isClientSide() && pilot instanceof IFluxShipPilot && pilot == getControllingPassenger()
                && direction.lengthSqr() > .000001 && Double.isFinite(direction.lengthSqr()) && Double.isFinite(speed)) {
            flightDirection = direction.normalize();
            flightSpeed = Mth.clamp(speed, 0, MAX_SPEED);
            inputPilot = pilot;
            inputTimeout = 5;
            entityData.set(MOUSE_FLIGHT, false);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (!isAlive() || getControllingPassenger() != null) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide()) {
            if (player.isShiftKeyDown()) {
                player.getInventory().placeItemBackInInventory(asItem(), Prediction.SERVER_ONLY);
                discard();
            } else {
                player.startRiding(this);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return (passenger instanceof Player || passenger instanceof LivingEntity && passenger instanceof IFluxShipPilot)
                && getControllingPassenger() == null
                || passenger instanceof IHoloGuiEntity && getHoloGui() == null;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getPassengers().stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).findFirst().orElse(null);
    }

    @Nullable
    public IHoloGuiEntity getHoloGui() {
        return getPassengers().stream().filter(IHoloGuiEntity.class::isInstance)
                .map(IHoloGuiEntity.class::cast).findFirst().orElse(null);
    }

    /** Actual travel speed, in blocks per second, including climbing and collisions. */

    public float getSpeed() {
        return entityData.get(SPEED);
    }

    public int getCharge() {
        return entityData.get(CHARGE);
    }

    public float getChargeRatio() {
        return (float) getCharge() / FluxShipEnergy.CAPACITY;
    }

    public void setCharge(int charge) {
        entityData.set(CHARGE, Mth.clamp(charge, 0, FluxShipEnergy.CAPACITY));
    }

    public ItemStack asItem() {
        ItemStack stack = new ItemStack(mcjty.ariente.setup.Registration.FLUX_SHIP.get());
        ItemData.update(stack, data -> data.putInt(FluxShipEnergy.CHARGE_TAG, getCharge()));
        if (hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, getCustomName());
        }
        return stack;
    }

    public float getBank(float partialTick) {
        return Mth.lerp(partialTick, previousBank, bank);
    }

    public float getExhaustStrength(float partialTick) {
        return Mth.lerp(partialTick, previousExhaustStrength, exhaustStrength);
    }

    public float getFlightPitch(float partialTick) {
        return Mth.lerp(partialTick, xRotO, getXRot());
    }

    public float getFlightYaw(float partialTick) {
        return Mth.rotLerp(partialTick, yRotO, getYRot());
    }

    /** Cabin coordinates use +Z forward, matching the flight and render rotations. */

    public Vec3 rotateCabinOffset(Vec3 offset) {
        return rotateCabinOffset(offset, 1);
    }

    public Vec3 rotateCabinOffset(Vec3 offset, float partialTick) {
        return offset.zRot(-getBank(partialTick) * Mth.DEG_TO_RAD).xRot(-getFlightPitch(partialTick) * Mth.DEG_TO_RAD)
                .yRot(-getFlightYaw(partialTick) * Mth.DEG_TO_RAD);
    }

    public Vec3 getPanelPosition(float partialTick) {
        return FluxShipLaser.panelCenter(this, partialTick).subtract(rotateCabinOffset(new Vec3(0, .5, 0), partialTick));
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!hasPassenger(passenger)) {
            return;
        }
        if (passenger instanceof IHoloGuiEntity) {
            Vec3 position = getPanelPosition(1);
            moveFunction.accept(passenger, position.x, position.y, position.z);
            passenger.setYRot(getYRot());
            passenger.setXRot(getXRot());
        } else {
            // Keep the seated player's eyes in the cabin as the hull tilts.
            double eyeHeight = getPilotSeatHeight(passenger) + passenger.getEyeHeight();
            Vec3 seat = rotateCabinOffset(new Vec3(0, eyeHeight - .5, 0)).add(0, .5 - passenger.getEyeHeight(), 0);
            moveFunction.accept(passenger, getX() + seat.x, getY() + seat.y, getZ() + seat.z);
            if (level().isClientSide() && lastPilotTurnTick != tickCount
                    && (!(passenger instanceof Player player) || !player.isLocalPlayer() || !localMouseFlight || localFollowHull)) {
                // The passenger has now copied its previous rotation. Apply the turn
                // here so its camera interpolates with the hull rather than jumping ahead.
                turnPilot(yawChange);
                lastPilotTurnTick = tickCount;
            }
        }
    }

    private void turnPilot(float yawChange) {
        LivingEntity pilot = getControllingPassenger();
        if (pilot != null) {
            pilot.setYRot(pilot.getYRot() + yawChange);
            pilot.setYHeadRot(pilot.getYHeadRot() + yawChange);
            pilot.setYBodyRot(pilot.yBodyRot + yawChange);
        }
    }

    private void ensureHoloGui() {
        if (getHoloGui() == null) {
            HoloGuiEntity panel = new HoloGuiEntity(mcjty.ariente.setup.Registration.ENTITY_SHIP_PANEL.get(), level());
            panel.setGuiId(ModGuis.GUI_FLUX_SHIP);
            panel.setScale(.85f);
            panel.setCloseStrategy(CloseStrategy.NEVER);
            panel.setPos(position());
            level().addFreshEntity(panel);
            panel.startRiding(this, true, true);
            positionRider(panel);
            panel.xOld = panel.xo = panel.getX();
            panel.yOld = panel.yo = panel.getY();
            panel.zOld = panel.zo = panel.getZ();
            panel.yRotO = panel.getYRot();
            panel.xRotO = panel.getXRot();
        }
    }

    @Override
    public boolean isClientAuthoritative() {
        // Movement is simulated on the server. Do not send vanilla client vehicle-position packets.
        return false;
    }

    @Override
    protected boolean isLocalClientAuthoritative() {
        return false;
    }

    public double getPilotSeatHeight(Entity pilot) {
        // The hull uses the 1.20 player seat offset; newer vehicle attachments lower it by .25 blocks.
        return getPassengersRidingOffset() - (pilot instanceof Player ? .35 : pilot.getVehicleAttachmentPoint(this).y);
    }

    public double getPassengersRidingOffset() {
        return 0.4;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        LivingEntity pilot = getControllingPassenger();
        if (pilot instanceof IFluxShipPilot || pilot != null && source.is(LaserEntity.SHIP_DAMAGE)) {
            // Return fire reaches NPC pilots; cannon hits also reach players inside their own ships.
            boolean laser = source.getDirectEntity() instanceof LaserEntity || source.is(LaserEntity.SHIP_DAMAGE);
            boolean damaged = pilot.hurtServer(serverLevel, source, pilot instanceof IFluxShipPilot && laser ? amount * .5f : amount);
            if (damaged && !level().isClientSide()) {
                hurtTime = 10;
                level().broadcastEntityEvent(this, (byte) 2);
            }
            return damaged;
        }
        if (source.getEntity() instanceof Player player && player.isShiftKeyDown() && getControllingPassenger() == null) {
            if (!level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    spawnAtLocation((ServerLevel) level(), asItem());
                }
                discard();
            }
            return true;
        }
        return false;
    }

    public int getHurtTime() {
        return hurtTime;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 2) {
            hurtTime = 10;
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy()) {
            IHoloGuiEntity holo = getHoloGui();
            if (holo != null) {
                holo.getEntity().discard();
            }
        }
        super.remove(reason);
    }

    private void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        lerpTarget = new Vec3(x, y, z);
        remoteMotion.accept(lerpTarget);
        // Attitude uses full-precision synced data, independent of position packets
        // and vanilla's byte-quantized yaw/pitch.
        lerpSteps = Math.max(1, steps);
    }

    @Override
    public void tick() {
        super.tick();
        if (hurtTime > 0) {
            hurtTime--;
        }
        previousBank = bank;
        previousExhaustStrength = exhaustStrength;
        float oldYaw = getYRot();
        if (level().isClientSide()) {
            boolean remote = !(getControllingPassenger() instanceof Player player && player.isLocalPlayer());
            if (remote) {
                if (!wasRemote) {
                    remoteMotion.reset(lerpSteps > 0 ? lerpTarget : position());
                }
                Vector3f motion = new Vector3f(entityData.get(MOTION));
                Vec3 next = remoteMotion.advance(position(), new Vec3(motion.x, motion.y, motion.z));
                boolean teleport = next.distanceToSqr(position()) > 64 * 64;
                setPos(next);
                if (teleport) {
                    setOldPosAndRot();
                }
            } else if (lerpSteps > 0) {
                setPos(position().lerp(lerpTarget, 1.0 / lerpSteps));
                lerpSteps--;
            }
            wasRemote = remote;
            bank = Mth.lerp(.35f, bank, entityData.get(BANK));
            setXRot(Mth.lerp(.35f, getXRot(), entityData.get(PITCH)));
            setYRot(Mth.rotLerp(.35f, getYRot(), entityData.get(YAW)));
            yawChange = Mth.wrapDegrees(getYRot() - oldYaw);
            float thrust = getCharge() > 0 ? Mth.clamp(getSpeed() / (float) (MAX_SPEED * 20), 0, 1) : 0;
            exhaustStrength = Mth.lerp(.2f, exhaustStrength, (float) Math.sqrt(thrust));
            if (exhaustStrength < .0001f) {
                exhaustStrength = 0;
            }
            return;
        }
        LivingEntity pilot = getControllingPassenger();
        boolean powered = getCharge() > 0;
        boolean livePilot = pilot != null && pilot.isAlive();
        if (pilot instanceof IFluxShipPilot controller && pilot.isAlive()) {
            controller.controlShip(this);
            if (isRemoved()) {
                return;
            }
        } else {
            ensureHoloGui();
        }
        if (pilot == null || !pilot.isAlive() || pilot != inputPilot || inputTimeout-- <= 0) {
            forward = turn = vertical = 0;
            firing = false;
            inputPilot = null;
            flightDirection = null;
            entityData.set(MOUSE_FLIGHT, false);
            mouseSteering = false;
        }
        boolean autopilot = flightDirection != null && pilot instanceof IFluxShipPilot;
        boolean mouseFlight = pilot instanceof Player && entityData.get(MOUSE_FLIGHT);
        float requestedTurn = turn * MAX_TURN_SPEED;
        if (autopilot) {
            float heading = (float) (Mth.atan2(-flightDirection.x, flightDirection.z) * Mth.RAD_TO_DEG);
            requestedTurn = Mth.clamp(Mth.wrapDegrees(heading - getYRot()) * .12f, -2.4f, 2.4f);
            float pitch = (float) (-Mth.atan2(flightDirection.y, flightDirection.horizontalDistance()) * Mth.RAD_TO_DEG);
            setXRot(Mth.approachDegrees(getXRot(), Mth.lerp(.12f, getXRot(), Mth.clamp(pitch, -55, 55)), 2));
        } else if (mouseFlight) {
            if (mouseSteering) {
                requestedTurn += Mth.clamp(Mth.wrapDegrees(aimYaw - getYRot()) * .15f, -MAX_TURN_SPEED, MAX_TURN_SPEED);
            }
            requestedTurn = Mth.clamp(requestedTurn, -MAX_TURN_SPEED, MAX_TURN_SPEED);
            setXRot(Mth.approachDegrees(getXRot(), Mth.lerp(.15f, getXRot(), aimPitch), 2));
        }
        turnSpeed = Mth.lerp(.15f, turnSpeed, requestedTurn);
        if (Math.abs(turnSpeed) < .001f) {
            turnSpeed = 0;
        }
        setYRot(getYRot() + turnSpeed);
        entityData.set(YAW, getYRot());
        Vec3 heading = Vec3.directionFromRotation(0, getYRot());
        double targetVerticalSpeed = !livePilot ? -UNPILOTED_DESCENT_SPEED
                : !powered ? -UNPOWERED_DESCENT_SPEED : vertical * MAX_VERTICAL_SPEED;
        Vec3 target = heading.scale(forward * MAX_SPEED).add(0, targetVerticalSpeed, 0);
        if (autopilot) {
            Vec3 nose = Vec3.directionFromRotation(getXRot(), getYRot());
            // Slow for a tight turn instead of sliding sideways at cruising speed.
            target = nose.scale(flightSpeed * Mth.clamp(nose.dot(flightDirection), .3, 1));
        } else if (mouseFlight) {
            target = Vec3.directionFromRotation(getXRot(), getYRot()).scale(forward * MAX_SPEED).add(0, targetVerticalSpeed, 0);
            if (target.lengthSqr() > MAX_SPEED * MAX_SPEED) {
                target = target.normalize().scale(MAX_SPEED);
            }
        }
        if (!powered) {
            // A living pilot retains the slower emergency descent after losing power.
            target = new Vec3(0, targetVerticalSpeed, 0);
        }
        Vec3 velocity = getDeltaMovement();
        // Ease acceleration, braking, reversals and unattended descent through the same momentum.
        velocity = new Vec3(Mth.lerp(HORIZONTAL_RESPONSE, velocity.x, target.x),
                Mth.lerp(VERTICAL_RESPONSE, velocity.y, target.y), Mth.lerp(HORIZONTAL_RESPONSE, velocity.z, target.z));
        setDeltaMovement(velocity.lengthSqr() < .000001 ? Vec3.ZERO : velocity);
        Vec3 before = position();
        move(MoverType.SELF, getDeltaMovement());
        Vec3 travel = position().subtract(before);
        entityData.set(MOTION, travel.toVector3f());
        entityData.set(SPEED, (float) (travel.length() * 20));
        if (powered && livePilot && (!onGround() || travel.horizontalDistanceSqr() > .0001)) {
            setCharge(getCharge() - FluxShipEnergy.flightCost(travel.horizontalDistance(), travel.y));
        }
        double forwardSpeed = travel.dot(heading);
        float targetBank = autopilot
                ? (float) (38 * turnSpeed / 2.4 * Mth.clamp(forwardSpeed / .35, -1, 1))
                : (float) (MAX_BANK * turnSpeed / MAX_TURN_SPEED * Mth.clamp(forwardSpeed / MAX_SPEED, -1, 1));
        bank = Mth.lerp(.18f, bank, targetBank);
        if (Math.abs(bank) < .001f) {
            bank = 0;
        }
        entityData.set(BANK, bank);
        float targetPitch = !livePilot || !powered ? 0
                : (float) (-Mth.atan2(travel.y, Math.max(.25, travel.horizontalDistance())) * Mth.RAD_TO_DEG);
        // In reverse, the tail leads the climb and the nose points down.
        if (forwardSpeed < -.05) {
            targetPitch = -targetPitch;
        }
        float pitch = Mth.lerp(.15f, getXRot(), Mth.clamp(targetPitch, -MAX_PITCH, MAX_PITCH));
        if (!powered || !autopilot && !mouseFlight) {
            setXRot(Math.abs(pitch) < .001f ? 0 : pitch);
        }
        entityData.set(PITCH, getXRot());
        if (laserCooldown > 0) {
            laserCooldown--;
        }
        boolean trigger = autopilot ? ((IFluxShipPilot) pilot).shouldFireShipLaser(this) : firing;
        if (trigger && pilot != null && FluxShipLaser.fire(this, pilot, laserCooldown)) {
            laserCooldown = FluxShipLaser.SHOT_INTERVAL;
        }
        fallDistance = 0;
        if (pilot != null) {
            pilot.fallDistance = 0;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BANK, 0f);
        builder.define(PITCH, 0f);
        builder.define(YAW, 0f);
        builder.define(SPEED, 0f);
        builder.define(MOTION, new Vector3f());
        builder.define(MOUSE_FLIGHT, false);
        builder.define(CHARGE, FluxShipEnergy.CAPACITY);
    }

    protected void readEntityData(CompoundTag tag) {
        setCharge(tag.contains(FluxShipEnergy.CHARGE_TAG) ? tag.getIntOr(FluxShipEnergy.CHARGE_TAG, 0) : FluxShipEnergy.CAPACITY);
        // Clear transient controls; an unattended ship resumes its gentle descent on the next tick.
        setDeltaMovement(Vec3.ZERO);
        forward = turn = vertical = inputTimeout = lerpSteps = 0;
        inputPilot = null;
        flightDirection = null;
        flightSpeed = 0;
        firing = false;
        laserCooldown = 0;
        hurtTime = 0;
        turnSpeed = bank = previousBank = yawChange = 0;
        exhaustStrength = previousExhaustStrength = 0;
        lastPilotTurnTick = -1;
        entityData.set(BANK, 0f);
        entityData.set(PITCH, 0f);
        entityData.set(YAW, getYRot());
        entityData.set(SPEED, 0f);
        entityData.set(MOTION, new Vector3f());
        remoteMotion.reset(position());
        wasRemote = false;
        entityData.set(MOUSE_FLIGHT, false);
        mouseSteering = localMouseFlight = false;
        localFollowHull = true;
        aimYaw = aimPitch = 0;
        setXRot(0);
        xRotO = 0;
    }

    protected void writeEntityData(CompoundTag tag) {
        tag.putInt(FluxShipEnergy.CHARGE_TAG, getCharge());
    }

    public AABB getBoundingBoxForCulling() {
        // Keep the rear exhaust visible when the hull itself is just outside the view.
        return getBoundingBox().inflate(26);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        readEntityData(input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        CompoundTag tag = new CompoundTag();
        writeEntityData(tag);
        output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
    }

}
