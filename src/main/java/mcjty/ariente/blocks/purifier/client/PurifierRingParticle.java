package mcjty.ariente.blocks.purifier.client;

import mcjty.ariente.blocks.purifier.PurifierTubeTile;
import mcjty.ariente.config.PowerConfiguration;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;

/** A red spark follows a circular path, rather than drifting away from its emission point. */
public class PurifierRingParticle extends TextureSheetParticle {

    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final Direction.Axis axis;
    private final double radius;
    private final double angularSpeed;
    private final BlockPos tubePos;
    private double angle;

    public PurifierRingParticle(ClientLevel level, double x, double y, double z, double angle, double radius,
                                double angularSpeed, Direction.Axis axis, SpriteSet sprites) {
        super(level, x, y, z);
        centerX = x;
        centerY = y;
        centerZ = z;
        tubePos = BlockPos.containing(x, y, z);
        this.angle = angle;
        this.radius = radius;
        this.angularSpeed = angularSpeed;
        this.axis = axis;
        lifetime = PowerConfiguration.PURIFIER_RING_TRAIL_TICKS.get();
        hasPhysics = false;
        quadSize = .045f;
        setColor(1, .25f, .025f);
        pickSprite(sprites);
        updateOrbit();
        xo = this.x;
        yo = this.y;
        zo = this.z;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (++age >= lifetime || !(level.getBlockEntity(tubePos) instanceof PurifierTubeTile tube) || !tube.isRunning()) {
            remove();
            return;
        }
        angle += angularSpeed;
        updateOrbit();
        float remaining = 1 - (float) age / lifetime;
        alpha = remaining * remaining;
        gCol = .025f + .225f * remaining;
        quadSize = .025f + .02f * remaining;
    }

    private void updateOrbit() {
        double across = Math.cos(angle) * radius;
        setPos(centerX + (axis == Direction.Axis.Z ? across : 0), centerY + Math.sin(angle) * radius,
                centerZ + (axis == Direction.Axis.X ? across : 0));
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xf000f0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;
        private final Direction.Axis axis;

        public Provider(SpriteSet sprites, Direction.Axis axis) {
            this.sprites = sprites;
            this.axis = axis;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double angle, double radius, double angularSpeed) {
            return new PurifierRingParticle(level, x, y, z, angle, radius, angularSpeed, axis, sprites);
        }
    }
}
