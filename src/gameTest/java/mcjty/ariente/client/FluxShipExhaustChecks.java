package mcjty.ariente.client;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipExhaustRender;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static mcjty.ariente.blocks.generators.NegariteTankRenderer.NEGARITE_BEAM;
import static mcjty.ariente.blocks.generators.PosiriteTankRenderer.POSIRITE_BEAM;

/** Check real exhaust vertices, visibility and OpenGL state in the opt-in ship flight run. */
public class FluxShipExhaustChecks {

    private static Mesh cruise;

    public static void hiddenFromPilot(FluxShipEntity ship) {
        require(ship.getExhaustStrength(1) > .5f, "Flight must build a substantial exhaust strength");
        require(mesh(ship, .5f).points().isEmpty(), "The pilot's first-person view must hide its own exhaust");
    }

    public static void flying(FluxShipEntity ship) {
        Minecraft mc = Minecraft.getInstance();
        cruise = mesh(ship, .25f);
        Mesh next = mesh(ship, .5f);
        require(cruise.points().size() >= 100 && cruise.points().size() % 4 == 0,
                "Third-person flight must produce multiple segmented exhaust strands");
        require(cruise.negarite() && cruise.posirite(), "Exhaust must use both tanks' original power textures");
        require(cruise.points().stream().allMatch(p -> p.z <= -2.4999 && Math.abs(p.x) < 1 && Math.abs(p.y) < .4),
                "All exhaust must start outside the open rear engine bay");
        require(cruise.tail() < -7 && cruise.glow() > .2f, "Cruise speed must produce long, bright exhaust");
        double largestStep = 0;
        for (int i = 0; i < cruise.points().size(); i++) {
            largestStep = Math.max(largestStep, cruise.points().get(i).distanceTo(next.points().get(i)));
        }
        require(largestStep > .00001 && largestStep < .2,
                "Strands must animate continuously between frames, without random jumps: " + largestStep);
        Vec3 tip = ship.position().add(ship.rotateCabinOffset(new Vec3(0, .5, cruise.tail())));
        require(ship.getBoundingBoxForCulling().contains(tip), "Frustum culling must include the exhaust tip");

        CameraType originalType = mc.options.getCameraType();
        var originalViewer = mc.getCameraEntity();
        try {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.setCameraEntity(new ArmorStand(mc.level, 0, 220, 0));
            require(!mesh(ship, .5f).points().isEmpty(), "A first-person observer must still see another ship's exhaust");
        } finally {
            mc.setCameraEntity(originalViewer);
            mc.options.setCameraType(originalType);
        }

        ArienteRenderType.SHIP_EXHAUST.setupRenderState();
        try {
            require(GL11.glIsEnabled(GL11.GL_BLEND)
                            && GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB) == GL11.GL_ONE
                            && GL11.glGetInteger(GL14.GL_BLEND_DST_RGB) == GL11.GL_ONE,
                    "The exhaust must use additive blending");
            require(GL11.glIsEnabled(GL11.GL_DEPTH_TEST) && !GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),
                    "The glow must respect walls without writing depth over other strands");
            require(!GL11.glIsEnabled(GL11.GL_CULL_FACE), "The ribbons must be visible from both sides");
        } finally {
            ArienteRenderType.SHIP_EXHAUST.clearRenderState();
        }
    }

    public static void braking(FluxShipEntity ship) {
        Minecraft mc = Minecraft.getInstance();
        CameraType original = mc.options.getCameraType();
        try {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            Mesh slow = mesh(ship, .5f);
            require(!slow.points().isEmpty() && slow.tail() > cruise.tail() && slow.glow() < cruise.glow() / 4,
                    "Braking must shorten and dim the exhaust with the ship's speed");
        } finally {
            mc.options.setCameraType(original);
        }
    }

    public static void stopped(FluxShipEntity ship) {
        Minecraft mc = Minecraft.getInstance();
        CameraType original = mc.options.getCameraType();
        try {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            require(mesh(ship, .5f).points().isEmpty(), "Exhaust must fade out completely at rest");
        } finally {
            mc.options.setCameraType(original);
        }
        Ariente.setup.getLogger().info("FLUX SHIP EXHAUST CHECKS PASSED: both textures, rear attachment, frame continuity, speed scaling, pilot/observer visibility, additive blend, depth state and culling; graphics={}",
                mc.options.graphicsMode().get());
    }

    private static Mesh mesh(FluxShipEntity ship, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        var atlas = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite negarite = atlas.apply(NEGARITE_BEAM);
        TextureAtlasSprite posirite = atlas.apply(POSIRITE_BEAM);
        require(negarite.contents().name().equals(NEGARITE_BEAM) && posirite.contents().name().equals(POSIRITE_BEAM),
                "The original tank beam textures must be present in the block atlas");
        BufferBuilder builder = new BufferBuilder(32768);
        builder.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.NEW_ENTITY);
        FluxShipExhaustRender.render(ship, partialTick, new PoseStack(), type -> {
            require(type == ArienteRenderType.SHIP_EXHAUST, "All strands must share the additive exhaust pass");
            return builder;
        });
        BufferBuilder.RenderedBuffer rendered = builder.end();
        try {
            ByteBuffer data = rendered.vertexBuffer();
            List<Vec3> points = new ArrayList<>();
            float glow = 0;
            double tail = 0;
            boolean hasNegarite = false;
            boolean hasPosirite = false;
            int stride = DefaultVertexFormat.NEW_ENTITY.getVertexSize();
            for (int i = 0; i < rendered.drawState().vertexCount(); i++) {
                int offset = i * stride;
                Vec3 point = new Vec3(data.getFloat(offset), data.getFloat(offset + 4), data.getFloat(offset + 8));
                require(Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z),
                        "Camera-facing exhaust must never produce invalid vertices");
                points.add(point);
                tail = Math.min(tail, point.z);
                glow = Math.max(glow, (data.get(offset + 12) & 255) / 255f);
                float u = data.getFloat(offset + 16);
                float v = data.getFloat(offset + 20);
                hasNegarite |= inside(negarite, u, v);
                hasPosirite |= inside(posirite, u, v);
            }
            return new Mesh(points, glow, tail, hasNegarite, hasPosirite);
        } finally {
            rendered.release();
        }
    }

    private static boolean inside(TextureAtlasSprite sprite, float u, float v) {
        return u >= sprite.getU0() && u <= sprite.getU1() && v >= sprite.getV0() && v <= sprite.getV1();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record Mesh(List<Vec3> points, float glow, double tail, boolean negarite, boolean posirite) {
    }
}
