package mcjty.ariente.client;

import net.minecraft.client.renderer.item.ItemModels;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Consumer;
import mcjty.ariente.items.EnergyLanceItem;
import mcjty.ariente.items.LanceAim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

/** Extract owner-dependent aiming once; submitted geometry never reads a live entity. */
public final class LanceModel implements ItemModel, SpecialModelRenderer<LanceModel.AimState> {

    private final CuboidItemModelWrapper fallback;
    private final List<BakedQuad> quads;
    private final Vector3fc[] extents;

    private LanceModel(QuadCollection geometry, ModelRenderProperties properties, Matrix4fc transform) {
        fallback = new CuboidItemModelWrapper(List.of(), geometry, properties, transform);
        quads = geometry.getAll();
        extents = CuboidItemModelWrapper.computeExtents(quads);
    }

    public static void register() {
        ItemModels.ID_MAPPER.put(Identifier.parse("ariente:lance"), Unbaked.CODEC);
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext context,
                       ClientLevel level, ItemOwner owner, int seed) {
        Minecraft mc = Minecraft.getInstance();
        LivingEntity holder = owner == null ? null : owner.asLivingEntity();
        boolean firstPerson = context.firstPerson() && holder == mc.player;
        boolean thirdPerson = context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        if (holder == null || !EnergyLanceItem.canFire(holder) || !(firstPerson || thirdPerson)) {
            fallback.update(output, item, resolver, context, level, owner, seed);
            return;
        }
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 eye = holder.getEyePosition(partialTick);
        Vec3 forward = holder.getViewVector(partialTick);
        if (holder instanceof Mob mob && mob.getTarget() != null) {
            forward = mob.getTarget().getEyePosition(partialTick).subtract(eye);
        }
        float yaw = Mth.rotLerp(partialTick, holder.yRotO, holder.getYRot());
        LanceAim aim = LanceAim.calculate(holder, eye, forward, yaw);
        Vec3 direction = aim.direction();
        Vec3 right = LanceAim.rightVector(direction, yaw);
        Vec3 normal = right.cross(direction);
        Matrix4f orientation = new Matrix4f()
                .m00((float) right.x).m01((float) right.y).m02((float) right.z)
                .m10((float) direction.x).m11((float) direction.y).m12((float) direction.z)
                .m20((float) normal.x).m21((float) normal.y).m22((float) normal.z);
        var camera = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        float scale = firstPerson ? .75f : 1;
        Vec3 origin = aim.muzzle().subtract(direction.scale(1.5 * scale)).subtract(camera.pos);
        Matrix4f transform = new Matrix4f().translation((float) origin.x, (float) origin.y, (float) origin.z).mul(orientation)
                .scale(scale).translate(-.5f, -.5f, -.5f);
        Matrix4f handCorrection = null;
        if (firstPerson) {
            var window = mc.gameRenderer.gameRenderState().windowRenderState;
            Matrix4f handProjection = new Matrix4f().perspective(camera.hudFov * Mth.DEG_TO_RAD,
                    (float) window.width / window.height, .05f, 100);
            handCorrection = handProjection.invert().mul(camera.projectionMatrix).mul(camera.viewRotationMatrix);
        }
        output.setAnimated();
        var layer = output.newLayer();
        layer.setExtents(() -> extents);
        layer.setupSpecialModel(this, new AimState(transform, handCorrection, context));
        if (item.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        }
    }

    public record AimState(Matrix4f transform, Matrix4f handCorrection, ItemDisplayContext context) {
    }

    @Override
    public void submit(AimState state, PoseStack poses, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
        PoseStack aimed = new PoseStack();
        Matrix4f transform = new Matrix4f();
        if (state.handCorrection() != null) {
            transform.set(RenderSystem.getModelViewMatrixCopy()).invert().mul(state.handCorrection());
        }
        transform.mul(state.transform());
        aimed.last().pose().set(transform);
        aimed.last().normal().set(new Matrix3f(transform).invert().transpose());
        collector.submitItem(aimed, state.context(), light, overlay, outline, new int[0], ItemQuads.split(quads),
                foil ? ItemStackRenderState.FoilType.STANDARD : ItemStackRenderState.FoilType.NONE);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (Vector3fc extent : extents) {
            output.accept(extent);
        }
    }

    @Override
    public AimState extractArgument(ItemStack stack) {
        return null;
    }

    public record Unbaked(Identifier model) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = Identifier.CODEC.fieldOf("model").xmap(Unbaked::new, Unbaked::model);

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolved = baker.getModel(model);
            TextureSlots slots = resolved.getTopTextureSlots();
            return new LanceModel(resolved.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY),
                    ModelRenderProperties.fromResolvedModel(baker, resolved, slots), transform);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
