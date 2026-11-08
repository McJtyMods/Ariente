package mcjty.ariente.blocks.purifier;

import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** A tube only holds a visual snapshot. Its start block owns all processing and items. */
public class PurifierTubeTile extends ArienteBlockEntity {

    public static final double RING_RADIUS = .4;

    private BlockPos controller = BlockPos.ZERO;
    private Direction direction = Direction.NORTH;
    private int index;
    private int tubeCount = 1;
    private ItemStack ore = ItemStack.EMPTY;
    private ItemStack raw = ItemStack.EMPTY;
    private int progress;
    private int duration = 160;
    private boolean running;
    private long updateTime;

    public PurifierTubeTile(BlockPos pos, BlockState state) {
        super(Registration.PURIFIER_TUBE_TILE.get(), pos, state);
    }

    public void setVisuals(BlockPos controller, Direction direction, int index, int tubeCount, ItemStack ore,
                           ItemStack raw, int progress, int duration, boolean running) {
        this.controller = controller.immutable();
        this.direction = direction;
        this.index = index;
        this.tubeCount = tubeCount;
        this.ore = ore.copy();
        this.raw = raw.copy();
        this.progress = progress;
        this.duration = Math.max(1, duration);
        this.running = running;
        updateTime = level.getGameTime();
        markDirtyClient();
    }

    public void clearVisuals() {
        ore = ItemStack.EMPTY;
        raw = ItemStack.EMPTY;
        running = false;
        markDirtyClient();
    }

    @Override
    public void tickServer() {
        if (!ore.isEmpty() && (!level.hasChunkAt(controller) || !(level.getBlockEntity(controller) instanceof PurifierTile)
                || level.getGameTime() - updateTime > 20)) {
            if (running) {
                running = false;
                markDirtyClient();
            }
            if (level.getGameTime() - updateTime > 40) {
                clearVisuals();
            }
        }
    }

    @Override
    public void tickClient() {
        if (!running || level.getGameTime() % 2 != 0) {
            return;
        }
        int particles = PowerConfiguration.PURIFIER_RING_PARTICLES.get();
        if (particles == 0) {
            return;
        }
        int streams = Math.min(particles, PowerConfiguration.PURIFIER_RING_STREAMS.get());
        double angularSpeed = direction.getAxisDirection().getStep() * Math.PI * 2 / PowerConfiguration.PURIFIER_RING_PERIOD.get();
        double phase = level.getGameTime() * angularSpeed + index * .7;
        SimpleParticleType type = direction.getAxis() == Direction.Axis.X
                ? Registration.PURIFIER_RING_X.get() : Registration.PURIFIER_RING_Z.get();
        for (int point = 0; point < particles; point++) {
            double angle = phase + (point % streams) * Math.PI * 2 / streams
                    - direction.getAxisDirection().getStep() * (point / streams) * .08;
            // The provider interprets the speed arguments as phase, radius and angular velocity.
            // Older particles orbit more slowly than the emission head, leaving a moving trail.
            level.addParticle(type, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5,
                    angle, RING_RADIUS, angularSpeed * .55);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public float getFraction(float partialTicks) {
        float elapsed = running ? Math.min(10, Math.max(0, level.getGameTime() - updateTime) + partialTicks) : 0;
        return Math.min(1, (progress + elapsed) / duration);
    }

    public Direction getDirection() {
        return direction;
    }

    public int getIndex() {
        return index;
    }

    public int getTubeCount() {
        return tubeCount;
    }

    public ItemStack getOre() {
        return ore;
    }

    public ItemStack getRaw() {
        return raw;
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Ore", ore.saveOptional(provider));
        tag.put("Raw", raw.saveOptional(provider));
        tag.putInt("Direction", direction.get3DDataValue());
        tag.putInt("Index", index);
        tag.putInt("Tubes", tubeCount);
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        tag.putBoolean("Running", running);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        ore = ItemStack.parseOptional(provider, tag.getCompound("Ore"));
        raw = ItemStack.parseOptional(provider, tag.getCompound("Raw"));
        direction = Direction.from3DDataValue(tag.getInt("Direction"));
        index = tag.getInt("Index");
        tubeCount = Math.max(1, tag.getInt("Tubes"));
        progress = tag.getInt("Progress");
        duration = Math.max(1, tag.getInt("Duration"));
        running = tag.getBoolean("Running");
        updateTime = level == null ? 0 : level.getGameTime();
    }
}
